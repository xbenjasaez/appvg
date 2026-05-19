package cl.ipvg.docentecalma.data.local

import androidx.room.migration.Migration

/**
 * Migraciones explícitas entre versiones de [AppDatabase].
 *
 * Al subir [AppDatabase.version], añade aquí un [Migration] por cada salto
 * (p. ej. `MIGRATION_1_2 = Migration(1, 2) { db -> ... }`) y regístralo
 * en [ALL]. Evita SQL ambiguo: usa nombres de tabla/columna reales del esquema.
 *
 * Plantilla típica para una columna nueva con default:
 * ```
 * private val MIGRATION_1_2 = Migration(1, 2) { db ->
 *     db.execSQL("ALTER TABLE emotional_check_in ADD COLUMN foo TEXT NOT NULL DEFAULT ''")
 * }
 * ```
 */
object AppDatabaseMigrations {

    private val MIGRATION_3_4 = Migration(3, 4) { db ->
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pilot_analytics_event` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `event_type` TEXT NOT NULL,
                `occurred_at` INTEGER NOT NULL,
                `secondary_key` TEXT,
                `int_meta` INTEGER
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_pilot_analytics_event_event_type` " +
                "ON `pilot_analytics_event` (`event_type`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_pilot_analytics_event_occurred_at` " +
                "ON `pilot_analytics_event` (`occurred_at`)"
        )
    }

    private val MIGRATION_2_3 = Migration(2, 3) { db ->
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `micromodule_progress` (
                `module_id` TEXT NOT NULL,
                `state` TEXT NOT NULL,
                `last_opened_at` INTEGER,
                `started_at` INTEGER,
                `completed_at` INTEGER,
                PRIMARY KEY(`module_id`)
            )
            """.trimIndent()
        )
    }

    private val MIGRATION_1_2 = Migration(1, 2) { db ->
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `self_assessment` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `evaluation_type` TEXT NOT NULL,
                `q1` INTEGER NOT NULL,
                `q2` INTEGER NOT NULL,
                `q3` INTEGER NOT NULL,
                `q4` INTEGER NOT NULL,
                `total_score` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_self_assessment_created_at` " +
                "ON `self_assessment` (`created_at`)"
        )
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4
    )
}
