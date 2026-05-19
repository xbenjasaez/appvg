package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.ClassroomScenario

/**
 * Catálogo estático de escenarios de aula. Se lee desde la pantalla
 * `ClassroomGuidance`. Si en el futuro se quiere editarlos en Room, basta
 * con mover este catálogo a una entity + seeder en la capa data.
 */
object ClassroomScenarioCatalog {

    val CONFLICT_WITH_STUDENT = ClassroomScenario(
        id = "conflict_with_student",
        title = "Conflicto con un estudiante",
        summary = "Intercambio tenso o falta de respeto en clase.",
        steps = listOf(
            "Baja tu activación antes de responder: respira y cuenta hasta 3.",
            "Separa lo conductual de lo personal al hablar.",
            "Define un límite claro sin atacar a la persona.",
            "Acuerda una conversación privada al finalizar la clase."
        ),
        redFlags = listOf(
            "Agresión verbal o física sostenida.",
            "Amenazas explícitas al docente o a compañeros."
        ),
        whenToEscalate = "Informa a registro académico y registra el hecho cuando haya agresión sostenida o amenazas."
    )

    val STUDENT_IN_DISTRESS = ClassroomScenario(
        id = "student_in_distress",
        title = "Estudiante con crisis emocional",
        summary = "Llanto, angustia o descompensación dentro de la clase.",
        steps = listOf(
            "Acércate con calma y ofrece pasar a un espacio más tranquilo.",
            "Valida lo que siente sin minimizar ni interpretar.",
            "Ofrece agua y una pausa breve.",
            "Avisa a registro académico apenas sea posible y sigue los conductos " +
                "regulares del instituto (Jefe/a de carrera, intranet, registro académico)."
        ),
        redFlags = listOf(
            "Mención de autolesiones o ideación suicida.",
            "Desorientación, desmayo o síntomas físicos graves."
        ),
        whenToEscalate = "Ante riesgo vital o ideación suicida, contacta de inmediato " +
            "servicios de emergencia y a registro académico; activa también los " +
            "conductos regulares (Jefe/a de carrera, intranet, registro académico)."
    )

    val DISRUPTIVE_CLASS = ClassroomScenario(
        id = "disruptive_class",
        title = "Grupo disruptivo o clima tenso",
        summary = "Clase desordenada, conversaciones cruzadas o apatía grupal.",
        steps = listOf(
            "Pausa la actividad 30 segundos en silencio para restablecer foco.",
            "Recuerda brevemente el objetivo de la clase de hoy.",
            "Propón una actividad corta que requiera participación.",
            "Cierra la pausa con un acuerdo explícito de convivencia."
        ),
        redFlags = listOf(
            "Burlas dirigidas a una misma persona de manera reiterada.",
            "Contenido discriminatorio o acoso entre estudiantes."
        ),
        whenToEscalate = "Reporta a registro académico cuando detectes patrones de acoso o discriminación."
    )

    val TEACHER_OVERLOAD = ClassroomScenario(
        id = "teacher_overload",
        title = "Sobrecarga del docente",
        summary = "Demasiadas clases, correcciones o requerimientos simultáneos.",
        steps = listOf(
            "Lista por escrito las tareas de la semana y su fecha real de entrega.",
            "Identifica qué se puede agrupar, delegar o postergar.",
            "Agenda un bloque sin mail ni mensajes para trabajo profundo.",
            "Si sigue excediéndote, plantea la carga a registro académico con datos."
        ),
        redFlags = listOf(
            "Insomnio frecuente o agotamiento físico persistente.",
            "Sensación de no poder sostener la semana siguiente."
        ),
        whenToEscalate = "Contacta a registro académico o a tu jefe/a de carrera cuando el agotamiento sea persistente o interfiera con la salud."
    )

    val SAFETY_RISK = ClassroomScenario(
        id = "safety_risk",
        title = "Riesgo de seguridad o violencia",
        summary = "Conducta violenta, armas o amenaza concreta.",
        steps = listOf(
            "Prioriza la seguridad: interrumpe la clase y aleja al grupo.",
            "Contacta a seguridad del sede o servicios de emergencia.",
            "No intentes reducir físicamente la situación por tu cuenta.",
            "Registra hora, personas presentes y hechos apenas sea seguro."
        ),
        redFlags = listOf(
            "Presencia de armas o agresión física.",
            "Amenazas directas a personas específicas."
        ),
        whenToEscalate = "Escala siempre a seguridad de la sede y a registro académico en forma inmediata."
    )

    val all: List<ClassroomScenario> = listOf(
        CONFLICT_WITH_STUDENT,
        STUDENT_IN_DISTRESS,
        DISRUPTIVE_CLASS,
        TEACHER_OVERLOAD,
        SAFETY_RISK
    )

    fun byId(id: String): ClassroomScenario? = all.firstOrNull { it.id == id }
}
