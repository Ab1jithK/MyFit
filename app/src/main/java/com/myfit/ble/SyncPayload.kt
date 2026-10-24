package com.myfit.ble

import com.myfit.core.data.db.entity.SetEntity
import com.myfit.core.data.db.entity.WorkoutSessionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

data class SyncData(
    val sessions: List<WorkoutSessionEntity>,
    val sets: List<SetEntity>
)

object SyncPayload {

    fun serialize(data: SyncData): String {
        val root     = JSONObject()
        val sessArr  = JSONArray()
        data.sessions.forEach { s ->
            sessArr.put(JSONObject().apply {
                put("id",           s.id)
                put("templateId",   s.templateId  ?: JSONObject.NULL)
                put("startedAt",    s.startedAt)
                put("endedAt",      s.endedAt     ?: JSONObject.NULL)
                put("sourceDevice", s.sourceDevice)
                put("notes",        s.notes       ?: JSONObject.NULL)
            })
        }
        val setsArr = JSONArray()
        data.sets.forEach { s ->
            setsArr.put(JSONObject().apply {
                put("id",           s.id)
                put("sessionId",    s.sessionId)
                put("exerciseId",   s.exerciseId)
                put("setNumber",    s.setNumber)
                put("recordedAt",   s.recordedAt)
                put("weightKg",     s.weightKg.toDouble())
                put("reps",         s.reps)
                put("rpe",          s.rpe?.toDouble() ?: JSONObject.NULL)
                put("setType",      s.setType)
                put("restSeconds",  s.restSeconds ?: JSONObject.NULL)
                put("sourceDevice", s.sourceDevice)
            })
        }
        root.put("sessions", sessArr)
        root.put("sets", setsArr)
        return root.toString()
    }

    private const val MAX_ID_LEN    = 64
    private const val MAX_NOTES_LEN = 2_000
    private val SOURCE_DEVICES      = setOf("phone", "watch")

    /** Parses and validates a peer payload; throws on anything malformed or out of range. */
    fun deserialize(json: String): SyncData {
        val root    = JSONObject(json)
        val sessArr = root.getJSONArray("sessions")
        val setsArr = root.getJSONArray("sets")

        val sessions = (0 until sessArr.length()).map { i ->
            val o = sessArr.getJSONObject(i)
            WorkoutSessionEntity(
                id           = o.id("id"),
                templateId   = o.nullableString("templateId", MAX_ID_LEN),
                startedAt    = o.timestamp("startedAt"),
                endedAt      = if (o.isNull("endedAt")) null else o.timestamp("endedAt"),
                sourceDevice = o.sourceDevice(),
                notes        = o.nullableString("notes", MAX_NOTES_LEN)
            )
        }

        val sets = (0 until setsArr.length()).map { i ->
            val o = setsArr.getJSONObject(i)
            SetEntity(
                id           = o.id("id"),
                sessionId    = o.id("sessionId"),
                exerciseId   = o.id("exerciseId"),
                setNumber    = o.intIn("setNumber", 0..1_000),
                recordedAt   = o.timestamp("recordedAt"),
                weightKg     = o.floatIn("weightKg", 0.0..2_000.0),
                reps         = o.intIn("reps", 0..1_000),
                rpe          = if (o.isNull("rpe")) null else o.floatIn("rpe", 0.0..10.0),
                setType      = o.id("setType"),
                restSeconds  = if (o.isNull("restSeconds")) null else o.intIn("restSeconds", 0..86_400),
                sourceDevice = o.sourceDevice()
            )
        }

        return SyncData(sessions, sets)
    }

    private fun JSONObject.id(key: String): String =
        getString(key).also { require(it.isNotEmpty() && it.length <= MAX_ID_LEN) { "invalid $key" } }

    private fun JSONObject.nullableString(key: String, maxLen: Int): String? =
        if (isNull(key)) null
        else getString(key).ifEmpty { null }?.also { require(it.length <= maxLen) { "invalid $key" } }

    private fun JSONObject.timestamp(key: String): Long =
        getLong(key).also { require(it >= 0) { "invalid $key" } }

    private fun JSONObject.intIn(key: String, range: IntRange): Int =
        getInt(key).also { require(it in range) { "invalid $key" } }

    private fun JSONObject.floatIn(key: String, range: ClosedFloatingPointRange<Double>): Float =
        getDouble(key).also { require(it in range) { "invalid $key" } }.toFloat()

    private fun JSONObject.sourceDevice(): String =
        getString("sourceDevice").also { require(it in SOURCE_DEVICES) { "invalid sourceDevice" } }

    fun chunk(json: String, chunkSize: Int = BleConstants.CHUNK_SIZE): List<ByteArray> {
        val bytes = json.toByteArray(Charsets.UTF_8)
        val total = (bytes.size + chunkSize - 1) / chunkSize
        return (0 until total).map { idx ->
            val start   = idx * chunkSize
            val end     = minOf(start + chunkSize, bytes.size)
            val payload = bytes.sliceArray(start until end)
            "$idx:$total:".toByteArray(Charsets.UTF_8) + payload
        }
    }

    fun reassemble(chunks: List<ByteArray>): String {
        val sorted = chunks.map { raw ->
            val prefix = raw.copyOf(minOf(raw.size, 20)).toString(Charsets.UTF_8)
            val sep1   = prefix.indexOf(':')
            val sep2   = prefix.indexOf(':', sep1 + 1)
            val idx    = prefix.substring(0, sep1).toIntOrNull() ?: 0
            idx to raw.sliceArray((sep2 + 1) until raw.size)
        }.sortedBy { it.first }
        return sorted.fold(byteArrayOf()) { acc, (_, data) -> acc + data }.toString(Charsets.UTF_8)
    }

    fun shouldMergeSet(incomingRecordedAt: Long, existingRecordedAt: Long?): Boolean =
        existingRecordedAt == null || incomingRecordedAt >= existingRecordedAt
}

/**
 * Reassembles "idx:total:" framed chunks from a peer. Chunks must arrive in order and the
 * total size is capped, so a misbehaving peer can't grow memory without bound.
 */
class ChunkAssembler(private val maxBytes: Int = BleConstants.MAX_PAYLOAD_BYTES) {
    private val buffer = ByteArrayOutputStream()
    private var expected = -1
    private var next     = 0

    /** Returns the complete payload once the final chunk arrives, otherwise null. */
    @Synchronized
    fun add(raw: ByteArray): String? {
        val prefix = raw.copyOf(minOf(raw.size, 20)).toString(Charsets.UTF_8)
        val sep1   = prefix.indexOf(':')
        val sep2   = prefix.indexOf(':', sep1 + 1)
        val idx    = if (sep1 > 0) prefix.substring(0, sep1).toIntOrNull() else null
        val total  = if (sep2 > sep1 + 1) prefix.substring(sep1 + 1, sep2).toIntOrNull() else null
        if (idx == null || total == null) { reset(); return null }

        if (idx == 0) {
            reset()
            if (total < 1) return null
            expected = total
        }
        // Header is ASCII digits and colons, so the char index of sep2 is also its byte index
        val dataStart = sep2 + 1
        val dataLen   = raw.size - dataStart
        if (idx != next || total != expected || buffer.size() + dataLen > maxBytes) { reset(); return null }

        buffer.write(raw, dataStart, dataLen)
        next++
        if (next < expected) return null
        return buffer.toByteArray().toString(Charsets.UTF_8).also { reset() }
    }

    @Synchronized
    fun reset() {
        buffer.reset(); expected = -1; next = 0
    }
}
