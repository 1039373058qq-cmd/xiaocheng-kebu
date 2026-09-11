package com.xingchen.xiaochengkebu.course.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.atomic.AtomicInteger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeachingDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TeachingDatabase::class.java,
    )

    private val createdDatabaseNames = mutableSetOf<String>()

    @After
    fun deleteTestDatabases() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        createdDatabaseNames.forEach(context::deleteDatabase)
    }

    @Test
    fun emptyV1DatabaseMigratesAndCreatesUniqueSemesterIndex() {
        val databaseName = databaseName("empty")
        createV1Database(databaseName).close()

        val migrated = helper.runMigrationsAndValidate(databaseName, 2, true, MIGRATION_1_2)
        migrated.use { db ->
            assertEquals(0, countRows(db, "semesters"))
            assertEquals(0, countRows(db, "teaching_records"))
            assertTrue(hasUniqueSemesterIndex(db))
        }
    }

    @Test
    fun teachingRecordsArePreservedExactly() {
        val databaseName = databaseName("records")
        val original = createV1Database(databaseName)
        original.execSQL(
            """
            INSERT INTO teaching_records
                (dateEpochDay, lessonCount, eveningSupportCount, note, createdAt, updatedAt)
            VALUES (739500, 6, 2, '晚课备注', 123456789, 123456999)
            """.trimIndent(),
        )
        original.close()

        val migrated = helper.runMigrationsAndValidate(databaseName, 2, true, MIGRATION_1_2)
        migrated.use { db ->
            db.query(
                "SELECT dateEpochDay, lessonCount, eveningSupportCount, note, createdAt, updatedAt FROM teaching_records",
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(739500L, cursor.getLong(0))
                assertEquals(6, cursor.getInt(1))
                assertEquals(2, cursor.getInt(2))
                assertEquals("晚课备注", cursor.getString(3))
                assertEquals(123456789L, cursor.getLong(4))
                assertEquals(123456999L, cursor.getLong(5))
                assertTrue(!cursor.moveToNext())
            }
        }
    }

    @Test
    fun duplicateSemesterGroupInheritsCurrentFlagAndKeepsOneRow() {
        val databaseName = databaseName("duplicate")
        val original = createV1Database(databaseName)
        original.execSQL(
            """
            INSERT INTO semesters
                (id, name, startEpochDay, endEpochDay, isCurrent, createdAt)
            VALUES
                (10, '2026 春季', 100, 200, 0, 1200),
                (11, '2026 春季', 100, 200, 1, 1100)
            """.trimIndent(),
        )
        original.close()

        val migrated = helper.runMigrationsAndValidate(databaseName, 2, true, MIGRATION_1_2)
        migrated.use { db ->
            assertEquals(1, countRows(db, "semesters"))
            db.query("SELECT isCurrent, createdAt FROM semesters").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
                assertEquals(1100L, cursor.getLong(1))
            }
        }
    }

    @Test
    fun multipleCurrentSemestersAreNormalizedToLatestStartThenId() {
        val databaseName = databaseName("current")
        val original = createV1Database(databaseName)
        original.execSQL(
            """
            INSERT INTO semesters
                (id, name, startEpochDay, endEpochDay, isCurrent, createdAt)
            VALUES
                (20, '较早学期', 300, 400, 1, 2000),
                (21, '较晚学期', 500, 600, 1, 2100),
                (22, '同日学期 A', 500, 700, 1, 2200)
            """.trimIndent(),
        )
        original.close()

        val migrated = helper.runMigrationsAndValidate(databaseName, 2, true, MIGRATION_1_2)
        migrated.use { db ->
            assertEquals(1, countRows(db, "semesters", "isCurrent = 1"))
            db.query("SELECT name FROM semesters WHERE isCurrent = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("同日学期 A", cursor.getString(0))
            }
        }
    }

    @Test
    fun migratedUniqueIndexRejectsDuplicateSemesterKeys() {
        val databaseName = databaseName("constraint")
        val original = createV1Database(databaseName)
        original.execSQL(
            "INSERT INTO semesters (name, startEpochDay, endEpochDay, isCurrent, createdAt) VALUES ('唯一学期', 1, 2, 0, 1)",
        )
        original.close()

        val migrated = helper.runMigrationsAndValidate(databaseName, 2, true, MIGRATION_1_2)
        migrated.use { db ->
            var rejected = false
            try {
                db.execSQL(
                    "INSERT INTO semesters (name, startEpochDay, endEpochDay, isCurrent, createdAt) VALUES ('唯一学期', 1, 2, 0, 2)",
                )
            } catch (_: SQLiteConstraintException) {
                rejected = true
            }
            assertTrue(rejected)
        }
    }

    private fun createV1Database(name: String): SupportSQLiteDatabase {
        createdDatabaseNames += name
        return helper.createDatabase(name, 1).also {
            // The exported v1 schema came from the cleanup build and already
            // contains the v2 index. Drop it to model the original 1.0.1 DB.
            it.execSQL("DROP INDEX IF EXISTS index_semesters_name_startEpochDay_endEpochDay")
        }
    }

    private fun countRows(
        database: SupportSQLiteDatabase,
        table: String,
        selection: String? = null,
    ): Int {
        val query = if (selection == null) {
            "SELECT COUNT(*) FROM $table"
        } else {
            "SELECT COUNT(*) FROM $table WHERE $selection"
        }
        return database.query(query).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
    }

    private fun hasUniqueSemesterIndex(database: SupportSQLiteDatabase): Boolean {
        database.query("PRAGMA index_list('semesters')").use { cursor ->
            val nameColumn = cursor.getColumnIndex("name")
            val uniqueColumn = cursor.getColumnIndex("unique")
            while (cursor.moveToNext()) {
                if (
                    cursor.getString(nameColumn) == "index_semesters_name_startEpochDay_endEpochDay" &&
                    cursor.getInt(uniqueColumn) == 1
                ) {
                    return true
                }
            }
        }
        return false
    }

    private fun databaseName(prefix: String): String =
        "teaching_migration_${prefix}_${databaseCounter.incrementAndGet()}.db"

    private companion object {
        val databaseCounter = AtomicInteger()
    }
}
