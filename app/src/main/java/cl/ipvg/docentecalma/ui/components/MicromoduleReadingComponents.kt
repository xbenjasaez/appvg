package cl.ipvg.docentecalma.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cl.ipvg.docentecalma.domain.model.Micromodule
import cl.ipvg.docentecalma.domain.model.MicromoduleBlock
import cl.ipvg.docentecalma.domain.model.MicromoduleProgressState
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState

// --- Copy centralizado (UX en español claro) ---

internal object MicromoduleReadingCopy {
    const val listTopBarTitle = "Lecturas"
    const val listHeroTitle = "Pequeñas lecturas que te acompañan"
    const val listHeroSubtitle =
        "Son cortitas. Puedes cerrar la app y volver cuando quieras: aquí se guarda cómo vas."

    const val sectionAllTitle = "Para leer con calma"
    const val sectionAllSubtitle = "Elige la que más te haga sentido hoy."

    const val continueInProgressTitle = "Seguimos donde lo dejaste"
    const val continueInProgressCta = "Continuar esta lectura"
    const val continueNotStartedTitle = "¿Por dónde partimos?"
    const val continueNotStartedCta = "Abrir esta lectura"

    const val readerTopBarTitle = "Lectura"
    const val readerDurationPrefix = "Lectura breve · unos"
    const val readerDurationSuffix = "min"

    const val exerciseCta = "Ir al ejercicio que va con esto"
    const val markDoneCta = "Listo, ya la leí"
    const val markDoneDoneCta = "Ya quedó registrada"
}

internal fun friendlyStatusLabel(state: MicromoduleProgressState): String = when (state) {
    MicromoduleProgressState.NOT_STARTED -> "Te espera"
    MicromoduleProgressState.IN_PROGRESS -> "Sigues aquí"
    MicromoduleProgressState.COMPLETED -> "Ya la viste"
}

internal fun readerProgressLine(progress: MicromoduleProgressState): String = when (progress) {
    MicromoduleProgressState.NOT_STARTED -> "Aún no la abres"
    MicromoduleProgressState.IN_PROGRESS -> "La empezaste: puedes seguir cuando quieras"
    MicromoduleProgressState.COMPLETED -> "Ya la revisaste"
}

internal fun isGentleNoteBlock(heading: String): Boolean {
    val h = heading.lowercase()
    return h.startsWith("si hoy") ||
        h.startsWith("si entras") ||
        h.contains("cansad")
}

// --- Componentes reutilizables ---

@Composable
fun FriendlySectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReadingHeroCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Mascot(
                state = MascotState.Greeting,
                contentDescription = "${MascotPersona.NAME} te saluda",
                sizeDp = 76.dp
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.92f)
                )
            }
        }
    }
}

@Composable
fun FriendlyStatusChip(
    state: MicromoduleProgressState,
    modifier: Modifier = Modifier
) {
    val (container, content, label) = when (state) {
        MicromoduleProgressState.NOT_STARTED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            friendlyStatusLabel(state)
        )
        MicromoduleProgressState.IN_PROGRESS -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            friendlyStatusLabel(state)
        )
        MicromoduleProgressState.COMPLETED -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            friendlyStatusLabel(state)
        )
    }
    Surface(
        modifier = modifier.semantics {
            contentDescription = friendlyStatusLabel(state)
        },
        shape = RoundedCornerShape(50),
        color = container,
        contentColor = content
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun ContinueReadingCard(
    module: Micromodule,
    state: MicromoduleProgressState,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInProgress = state == MicromoduleProgressState.IN_PROGRESS
    val title = if (isInProgress) {
        MicromoduleReadingCopy.continueInProgressTitle
    } else {
        MicromoduleReadingCopy.continueNotStartedTitle
    }
    val cta = if (isInProgress) {
        MicromoduleReadingCopy.continueInProgressCta
    } else {
        MicromoduleReadingCopy.continueNotStartedCta
    }
    ElevatedCard(
        onClick = onOpen,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$title. ${module.title}"
            },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = module.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = module.lead,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                ReadingDurationLabel(minutes = module.estimatedMinutes)
                FriendlyStatusChip(state = state)
            }
            Spacer(Modifier.height(14.dp))
            TextButton(onClick = onOpen, modifier = Modifier.align(Alignment.End)) {
                Text(text = cta)
            }
        }
    }
}

@Composable
fun ReadingModuleCard(
    module: Micromodule,
    state: MicromoduleProgressState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${module.title}. ${friendlyStatusLabel(state)}"
            },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = module.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            FriendlyStatusChip(state = state)
            Spacer(Modifier.height(12.dp))
            Text(
                text = module.lead,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            ReadingDurationLabel(minutes = module.estimatedMinutes)
        }
    }
}

@Composable
private fun ReadingDurationLabel(
    minutes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Te lleva unos $minutes min",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ReadingDetailHero(
    module: Micromodule,
    progress: MicromoduleProgressState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = module.lead,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Text(
                            text = "${MicromoduleReadingCopy.readerDurationPrefix} ${module.estimatedMinutes} ${MicromoduleReadingCopy.readerDurationSuffix}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    FriendlyStatusChip(state = progress)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = readerProgressLine(progress),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Mascot(
                state = MascotState.Empathic,
                contentDescription = null,
                sizeDp = 64.dp
            )
        }
    }
}

@Composable
fun StepBlockCard(
    block: MicromoduleBlock,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = block.heading,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(14.dp))
            block.lines.forEachIndexed { index, line ->
                StepItemCard(index = index + 1, text = line)
                if (index != block.lines.lastIndex) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun StepItemCard(
    index: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun GentleNoteCard(
    block: MicromoduleBlock,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = block.heading,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(Modifier.height(10.dp))
            block.lines.forEachIndexed { index, line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (index != block.lines.lastIndex) {
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun ReadingActionFooter(
    showExerciseButton: Boolean,
    onOpenExercises: () -> Unit,
    progress: MicromoduleProgressState,
    onMarkCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completed = progress == MicromoduleProgressState.COMPLETED
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showExerciseButton) {
            OutlinedButton(
                onClick = onOpenExercises,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text(MicromoduleReadingCopy.exerciseCta)
            }
        }
        Button(
            onClick = onMarkCompleted,
            enabled = !completed,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                if (completed) {
                    MicromoduleReadingCopy.markDoneDoneCta
                } else {
                    MicromoduleReadingCopy.markDoneCta
                }
            )
        }
    }
}
