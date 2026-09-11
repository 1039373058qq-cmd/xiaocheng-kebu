package com.xingchen.xiaochengkebu.course.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Upgrades the v1 database in place.
 *
 * Version 1 databases may contain duplicate semesters because the unique index
 * was introduced without a schema-version bump. The rows are collapsed before
 * the index is created; no teaching records are touched by this migration.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS semesters_migration")
        database.execSQL(
            """
            CREATE TABLE semesters_migration (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                startEpochDay INTEGER NOT NULL,
                endEpochDay INTEGER NOT NULL,
                isCurrent INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO semesters_migration (
                id, name, startEpochDay, endEpochDay, isCurrent, createdAt
            )
            WITH grouped AS (
                SELECT
                    name,
                    startEpochDay,
                    endEpochDay,
                    MAX(id) AS retainedId,
                    MAX(isCurrent) AS inheritedCurrent
                FROM semesters
                GROUP BY name, startEpochDay, endEpochDay
            )
            SELECT
                grouped.retainedId,
                grouped.name,
                grouped.startEpochDay,
                grouped.endEpochDay,
                CASE WHEN grouped.inheritedCurrent > 0 THEN 1 ELSE 0 END,
                retained.createdAt
            FROM grouped
            JOIN semesters AS retained ON retained.id = grouped.retainedId
            """.trimIndent(),
        )
        // A v1 database could have more than one current semester.  Keep the
        // latest starting semester, breaking ties by the retained row id.
        database.execSQL(
            """
            UPDATE semesters_migration
            SET isCurrent = CASE
                WHEN id = (
                    SELECT id
                    FROM semesters_migration
                    WHERE isCurrent = 1
                    ORDER BY startEpochDay DESC, id DESC
                    LIMIT 1
                ) THEN 1
                ELSE 0
            END
            """.trimIndent(),
        )
        database.execSQL("DROP TABLE semesters")
        database.execSQL("ALTER TABLE semesters_migration RENAME TO semesters")
        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
                index_semesters_name_startEpochDay_endEpochDay
            ON semesters(name, startEpochDay, endEpochDay)
            """.trimIndent(),
        )
    }
}
