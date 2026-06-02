package com.myfit.ble

import com.myfit.core.data.db.entity.SetEntity
import com.myfit.core.data.db.entity.WorkoutSessionEntity
import org.json.JSONArray
import org.json.JSONObject

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

    fun deserialize(json: String): SyncData {
        val root    = JSONObject(json)
        val sessArr = root.getJSONArray("sessions")
        val setsArr = root.getJSONArray("sets")

        val sessions = (0 until sessArr.length()).map { i ->
            val o = sessArr.getJSONObject(i)
            WorkoutSessionEntity(
                id           = o.getString("id"),
                templateId   = o.optString("templateId", "").ifEmpty { null },
                startedAt    = o.getLong("startedAt"),
                endedAt      = if (o.isNull("endedAt")) null else o.getLong("endedAt"),
                sourceDevice = o.getString("sourceDevice"),
                notes        = o.optString("notes", "").ifEmpty { null }
            )
        }

        val sets = (0 until setsArr.length()).map { i ->
            val o = setsArr.getJSONObject(i)
            SetEntity(
                id           = o.getString("id"),
                sessionId    = o.getString("sessionId"),
                exerciseId   = o.getString("exerciseId"),
                setNumber    = o.getInt("setNumber"),
                recordedAt   = o.getLong("recordedAt"),
                weightKg     = o.getDouble("weightKg").toFloat(),
                reps         = o.getInt("reps"),
                rpe          = if (o.isNull("rpe")) null else o.getDouble("rpe").toFloat(),
                setType      = o.getString("setType"),
                restSeconds  = if (o.isNull("restSeconds")) null else o.getInt("restSeconds"),
                sourceDevice = o.getString("sourceDevice")
            )
        }

        return SyncData(sessions, sets)
    }

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
