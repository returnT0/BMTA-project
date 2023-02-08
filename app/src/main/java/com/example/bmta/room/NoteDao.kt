package com.example.bmta.room

import androidx.room.*
import com.example.bmta.model.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM Note ORDER BY date DESC")
    suspend fun getNote() : List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note : Note)

    @Query("DELETE FROM Note WHERE id IN (:ids)")
    suspend fun deleteNote(ids : List<Int>)
}