package com.example.myapplication

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.entity.StringEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StringDaoTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun crudOperations_updateObservedRows() =
        runBlocking {
            val dao = database.stringDao()
            dao.insertString(StringEntity(value = "first"))
            val inserted = dao.getAllStrings().first().single()

            dao.updateString(inserted.copy(value = "updated"))
            assertEquals("updated", dao.getFirstString()?.value)

            assertEquals(1, dao.deleteStringById(inserted.id))
            assertEquals(emptyList<StringEntity>(), dao.getAllStrings().first())
        }

    @Test
    fun deletingMissingId_returnsZero() =
        runBlocking {
            assertEquals(0, database.stringDao().deleteStringById(999))
        }
}
