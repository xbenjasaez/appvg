package cl.ipvg.docentecalma.data.local

/**
 * Nombres de archivos de persistencia local. Un solo lugar evita renombrados
 * inconsistentes entre Room, DataStore y documentación de soporte.
 *
 * **No cambiar** los valores en dispositivos ya publicados: implicaría una BD
 * nueva o un archivo de preferencias distinto (pérdida de datos locales).
 */
object AppPersistenceIds {
    const val ROOM_DATABASE_FILE_NAME: String = "docente_calma.db"

    /** Archivo único compartido por onboarding y feedback post-uso. */
    const val DATASTORE_ONBOARDING_FILE: String = "docente_calma_onboarding"
}
