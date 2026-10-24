package com.myfit.wear.ble

import com.myfit.wear.data.entity.SetEntity
import com.myfit.wear.data.entity.WorkoutSessionEntity
import org.junit.Assert.*
import org.junit.Test

class SyncPayloadTest {

    private fun session() = WorkoutSessionEntity(
        id = "sess-1", templateId = null, startedAt = 1000L,
        endedAt = 2000L, sourceDevice = "watch", notes = null
    )

    private fun set() = SetEntity(
        id = "set-1", sessionId = "sess-1", exerciseId = "ex_squat",
        setNumber = 1, recordedAt = 1500L, weightKg = 100f,
        reps = 5, rpe = 8.5f, setType = "working",
        restSeconds = 120, sourceDevice = "watch"
    )

    @Test fun serialize_deserialize_roundtrip() {
        val data   = SyncData(listOf(session()), listOf(set()))
        val result = SyncPayload.deserialize(SyncPayload.serialize(data))
        assertEquals("sess-1", result.sessions[0].id)
        assertEquals(1000L,    result.sessions[0].startedAt)
        assertEquals("set-1",  result.sets[0].id)
        assertEquals(100f,     result.sets[0].weightKg, 0.01f)
        assertEquals(5,        result.sets[0].reps)
        assertEquals(8.5f,     result.sets[0].rpe!!, 0.01f)
    }

    @Test fun chunk_reassemble_small() {
        val json   = SyncPayload.serialize(SyncData(listOf(session()), listOf(set())))
        val chunks = SyncPayload.chunk(json, 50)
        assertTrue(chunks.size >= 1)
        assertEquals(json, SyncPayload.reassemble(chunks))
    }

    @Test fun chunk_reassemble_large() {
        val sets = (1..30).map { i ->
            SetEntity("set-$i","sess-1","ex_squat",i,1000L+i,80f+i,5,null,"working",120,"watch")
        }
        val json   = SyncPayload.serialize(SyncData(listOf(session()), sets))
        val chunks = SyncPayload.chunk(json, 100)
        assertTrue(chunks.size > 1)
        assertEquals(json, SyncPayload.reassemble(chunks))
    }

    @Test fun shouldMerge_newRecord() {
        assertTrue(SyncPayload.shouldMergeSet(2000L, null))
        assertTrue(SyncPayload.shouldMergeSet(2000L, 1000L))
        assertTrue(SyncPayload.shouldMergeSet(1000L, 1000L))
    }

    @Test fun shouldMerge_staleRecord() {
        assertFalse(SyncPayload.shouldMergeSet(999L, 1000L))
    }

    @Test fun assembler_reassembles_in_order() {
        val json      = SyncPayload.serialize(SyncData(listOf(session()), listOf(set())))
        val assembler = ChunkAssembler()
        val results   = SyncPayload.chunk(json, 40).map { assembler.add(it) }
        assertTrue(results.dropLast(1).all { it == null })
        assertEquals(json, results.last())
    }

    @Test fun assembler_rejects_out_of_order_and_garbage() {
        val chunks    = SyncPayload.chunk("x".repeat(100), 10)
        val assembler = ChunkAssembler()
        assertNull(assembler.add(chunks[0]))
        assertNull(assembler.add(chunks[2]))  // skipped idx 1 → reset
        chunks.drop(1).forEach { assertNull(assembler.add(it)) }  // no idx 0 → never completes
        assertNull(assembler.add("not a chunk".toByteArray()))
    }

    @Test fun assembler_enforces_size_cap() {
        val assembler = ChunkAssembler(maxBytes = 50)
        assertNull(SyncPayload.chunk("x".repeat(100), 10).map { assembler.add(it) }.last())
    }

    @Test(expected = IllegalArgumentException::class)
    fun deserialize_rejects_out_of_range_reps() {
        val json = SyncPayload.serialize(SyncData(emptyList(), listOf(set().copy(reps = 100_000))))
        SyncPayload.deserialize(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun deserialize_rejects_unknown_source_device() {
        val json = SyncPayload.serialize(SyncData(listOf(session().copy(sourceDevice = "evil")), emptyList()))
        SyncPayload.deserialize(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun deserialize_rejects_oversized_notes() {
        val json = SyncPayload.serialize(SyncData(listOf(session().copy(notes = "n".repeat(5_000))), emptyList()))
        SyncPayload.deserialize(json)
    }

    @Test fun deserialize_null_fields() {
        val data   = SyncData(
            sessions = listOf(WorkoutSessionEntity("s","x",1000L,null,"watch",null)),
            sets     = listOf(SetEntity("s1","s","ex_squat",1,1000L,100f,5,null,"working",null,"watch"))
        )
        val result = SyncPayload.deserialize(SyncPayload.serialize(data))
        assertNull(result.sessions[0].endedAt)
        assertNull(result.sets[0].rpe)
        assertNull(result.sets[0].restSeconds)
    }
}
