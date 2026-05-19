package cl.ipvg.docentecalma.ui.screens.pilotmetrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.R
import cl.ipvg.docentecalma.data.analytics.PilotEventNames
import cl.ipvg.docentecalma.domain.model.PilotEffectivenessVerdict
import cl.ipvg.docentecalma.domain.model.SelfAssessment
import cl.ipvg.docentecalma.domain.rules.PilotEffectivenessCalculator
import cl.ipvg.docentecalma.domain.rules.SelfAssessmentQuestionnaire
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PilotMetricsScreen(
    onBack: () -> Unit,
    viewModel: PilotMetricsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val eff = uiState.effectiveness
    val intFormat = remember { NumberFormat.getIntegerInstance(Locale("es", "CL")) }
    val sortedRows = remember(uiState.rows) {
        uiState.rows.sortedWith(
            compareBy<PilotMetricRowUi> { PilotMetricLabels.sortIndex(it.eventType) }
                .thenBy { PilotMetricLabels.humanLabel(it.eventType) }
        )
    }
    val hasTechnicalData = uiState.totalEvents > 0 || sortedRows.isNotEmpty()
    val showFlowDurationSection = sortedRows.any { it.eventType == PilotEventNames.FLOW_DURATION_BUCKET }
    val comparablePrePost = eff.baseline != null && eff.latest != null &&
        eff.baseline.id != eff.latest.id

    DocenteCalmaScaffold(
        title = stringResource(R.string.pilot_effectiveness_title),
        onBack = onBack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.pilot_effectiveness_subtitle),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.pilot_effectiveness_intro_short),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            uiState.error?.let { err ->
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = err.ifBlank { stringResource(R.string.pilot_error_load) },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            TextButton(onClick = { viewModel.dismissError() }) {
                                Text(text = "Entendido")
                            }
                        }
                    }
                }
            }

            item {
                InterpretationCard(verdict = eff.verdict)
            }

            item {
                Text(
                    text = stringResource(R.string.pilot_section_kpis),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                PrePostKpiCard(eff = eff, comparablePrePost = comparablePrePost)
            }

            item {
                KpiTextCard(
                    title = stringResource(R.string.pilot_kpi_modules_title),
                    body = stringResource(
                        R.string.pilot_kpi_modules_fmt,
                        eff.modulesCompleted,
                        eff.modulesTotal,
                        eff.moduleCompletionPercent
                    )
                )
            }

            item {
                KpiTextCard(
                    title = stringResource(R.string.pilot_kpi_continuity_title),
                    body = stringResource(
                        R.string.pilot_kpi_continuity_fmt,
                        eff.activeDistinctDaysLast14
                    )
                )
            }

            item {
                FeedbackKpiCard(eff = eff)
            }

            if (eff.perDimension.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.pilot_section_dimensions),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.pilot_section_dimensions_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    DimensionsCard(eff = eff)
                }
            }

            item {
                KnowUnknownSection()
            }

            item {
                NextStepsSection(verdict = eff.verdict)
            }

            item {
                InstitutionalCard()
            }

            item {
                PrivacyContextCard()
            }

            item {
                var technicalOpen by remember { mutableStateOf(false) }
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.pilot_technical_expand),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.pilot_technical_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { technicalOpen = !technicalOpen }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (technicalOpen) "Ocultar" else "Mostrar",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (technicalOpen) Icons.Filled.ExpandLess
                                    else Icons.Filled.ExpandMore,
                                    contentDescription = null
                                )
                            }
                        }
                        AnimatedVisibility(visible = technicalOpen) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                if (!hasTechnicalData) {
                                    EmptyPilotMetricsState()
                                } else {
                                    KpiGrid(
                                        totalEventsLabel = intFormat.format(uiState.totalEvents),
                                        activeDaysLabel = intFormat.format(
                                            uiState.distinctDaysWithEvents.toLong()
                                        ),
                                        firstActivityLabel = uiState.firstEventLabel ?: "—",
                                        lastActivityLabel = uiState.lastEventLabel ?: "—"
                                    )
                                    if (showFlowDurationSection) {
                                        FlowDurationExplainerCard()
                                    }
                                    Text(
                                        text = "Detalle por tipo de actividad",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Cada fila resume cuántas veces ocurrió una acción durante el piloto.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        tonalElevation = 1.dp
                                    ) {
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            sortedRows.forEachIndexed { index, row ->
                                                if (index > 0) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(horizontal = 16.dp),
                                                        color = MaterialTheme.colorScheme.outlineVariant
                                                            .copy(alpha = 0.6f)
                                                    )
                                                }
                                                MetricDetailRow(
                                                    label = PilotMetricLabels.humanLabel(row.eventType),
                                                    valueText = intFormat.format(row.count.toLong())
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InterpretationCard(verdict: PilotEffectivenessVerdict) {
    val (titleRes, bodyRes) = verdictStringRes(verdict)
    val colors = when (verdict) {
        PilotEffectivenessVerdict.INITIAL_POSITIVE_SIGNALS ->
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        PilotEffectivenessVerdict.MIXED_SIGNALS ->
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        PilotEffectivenessVerdict.USAGE_WITHOUT_DEMONSTRATED_SHIFT ->
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        else -> CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = colors,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.pilot_section_interpretation),
                style = MaterialTheme.typography.labelMedium,
                color = colors.contentColor.copy(alpha = 0.85f)
            )
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = colors.contentColor
            )
            Text(
                text = stringResource(bodyRes),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.contentColor.copy(alpha = 0.92f)
            )
        }
    }
}

private fun verdictStringRes(verdict: PilotEffectivenessVerdict): Pair<Int, Int> = when (verdict) {
    PilotEffectivenessVerdict.INSUFFICIENT_EVIDENCE ->
        R.string.pilot_verdict_insufficient_title to R.string.pilot_verdict_insufficient_body
    PilotEffectivenessVerdict.AWAITING_SECOND_SELF_ASSESSMENT ->
        R.string.pilot_verdict_awaiting_second_title to R.string.pilot_verdict_awaiting_second_body
    PilotEffectivenessVerdict.USAGE_WITHOUT_SELF_ASSESSMENT_TREND ->
        R.string.pilot_verdict_usage_no_self_title to R.string.pilot_verdict_usage_no_self_body
    PilotEffectivenessVerdict.USAGE_WITHOUT_DEMONSTRATED_SHIFT ->
        R.string.pilot_verdict_usage_no_shift_title to R.string.pilot_verdict_usage_no_shift_body
    PilotEffectivenessVerdict.INITIAL_POSITIVE_SIGNALS ->
        R.string.pilot_verdict_positive_title to R.string.pilot_verdict_positive_body
    PilotEffectivenessVerdict.MIXED_SIGNALS ->
        R.string.pilot_verdict_mixed_title to R.string.pilot_verdict_mixed_body
    PilotEffectivenessVerdict.POSITIVE_TREND_INCOMPLETE_PICTURE ->
        R.string.pilot_verdict_incomplete_title to R.string.pilot_verdict_incomplete_body
    PilotEffectivenessVerdict.SMALL_OR_UNCLEAR_CHANGE ->
        R.string.pilot_verdict_small_title to R.string.pilot_verdict_small_body
}

@Composable
private fun PrePostKpiCard(eff: PilotEffectivenessCalculator.PilotEffectivenessSnapshot, comparablePrePost: Boolean) {
    val body = when {
        eff.baseline == null -> stringResource(R.string.pilot_kpi_prepost_no_assessment)
        !comparablePrePost -> stringResource(R.string.pilot_kpi_prepost_need_second)
        else -> {
            val pct = eff.improvementPercentOfBaseline ?: 0
            stringResource(
                R.string.pilot_kpi_prepost_pair,
                eff.baseline.totalScore,
                eff.latest!!.totalScore,
                pct,
                SelfAssessment.MAX_TOTAL
            )
        }
    }
    KpiTextCard(
        title = stringResource(R.string.pilot_kpi_prepost_title),
        body = body
    )
}

@Composable
private fun FeedbackKpiCard(eff: PilotEffectivenessCalculator.PilotEffectivenessSnapshot) {
    val body = if (eff.feedbackPresent && eff.satisfaction != null && eff.usefulness != null && eff.ease != null) {
        stringResource(
            R.string.pilot_kpi_feedback_present,
            eff.satisfaction,
            eff.usefulness,
            eff.ease
        )
    } else {
        stringResource(R.string.pilot_kpi_feedback_absent)
    }
    KpiTextCard(
        title = stringResource(R.string.pilot_kpi_feedback_title),
        body = body
    )
}

@Composable
private fun KpiTextCard(title: String, body: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DimensionsCard(eff: PilotEffectivenessCalculator.PilotEffectivenessSnapshot) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            eff.perDimension.forEach { dim ->
                val label = SelfAssessmentQuestionnaire.shortDimensionLabels.getOrElse(dim.index) {
                    "Dimensión ${dim.index + 1}"
                }
                val deltaLabel = when {
                    dim.delta < 0 -> stringResource(R.string.pilot_dim_delta_down)
                    dim.delta > 0 -> stringResource(R.string.pilot_dim_delta_up)
                    else -> stringResource(R.string.pilot_dim_delta_same)
                }
                Text(
                    text = stringResource(
                        R.string.pilot_dim_line,
                        label,
                        dim.baselineAnswer,
                        dim.latestAnswer,
                        deltaLabel
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun KnowUnknownSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.pilot_section_know),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BulletLine(stringResource(R.string.pilot_know_usage_local))
                BulletLine(stringResource(R.string.pilot_know_not_clinical))
            }
        }
        Text(
            text = stringResource(R.string.pilot_section_unknown),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BulletLine(stringResource(R.string.pilot_unknown_causality))
                BulletLine(stringResource(R.string.pilot_unknown_generalize))
                BulletLine(stringResource(R.string.pilot_unknown_institutional))
            }
        }
    }
}

@Composable
private fun NextStepsSection(verdict: PilotEffectivenessVerdict) {
    val lines = nextStepStringRes(verdict).map { stringResource(it) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.pilot_section_next),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lines.forEach { line ->
                    BulletLine(
                        text = line,
                        bulletColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

private fun nextStepStringRes(verdict: PilotEffectivenessVerdict): List<Int> = when (verdict) {
    PilotEffectivenessVerdict.INSUFFICIENT_EVIDENCE -> listOf(
        R.string.pilot_next_explore_app,
        R.string.pilot_next_first_assessment,
        R.string.pilot_next_feedback
    )
    PilotEffectivenessVerdict.AWAITING_SECOND_SELF_ASSESSMENT -> listOf(
        R.string.pilot_next_second_assessment,
        R.string.pilot_next_modules,
        R.string.pilot_next_feedback
    )
    PilotEffectivenessVerdict.USAGE_WITHOUT_SELF_ASSESSMENT_TREND -> listOf(
        R.string.pilot_next_first_assessment,
        R.string.pilot_next_modules,
        R.string.pilot_next_feedback
    )
    PilotEffectivenessVerdict.USAGE_WITHOUT_DEMONSTRATED_SHIFT -> listOf(
        R.string.pilot_next_second_assessment,
        R.string.pilot_next_reflect_mixed,
        R.string.pilot_next_modules
    )
    PilotEffectivenessVerdict.INITIAL_POSITIVE_SIGNALS -> listOf(
        R.string.pilot_next_keep_habit,
        R.string.pilot_next_second_assessment,
        R.string.pilot_next_feedback
    )
    PilotEffectivenessVerdict.MIXED_SIGNALS -> listOf(
        R.string.pilot_next_reflect_mixed,
        R.string.pilot_next_modules,
        R.string.pilot_next_feedback
    )
    PilotEffectivenessVerdict.POSITIVE_TREND_INCOMPLETE_PICTURE -> listOf(
        R.string.pilot_next_feedback,
        R.string.pilot_next_keep_habit,
        R.string.pilot_next_second_assessment
    )
    PilotEffectivenessVerdict.SMALL_OR_UNCLEAR_CHANGE -> listOf(
        R.string.pilot_next_second_assessment,
        R.string.pilot_next_modules,
        R.string.pilot_next_feedback
    )
}

@Composable
private fun InstitutionalCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.pilot_section_institutional),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.pilot_institutional_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PrivacyContextCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.PrivacyTip,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.pilot_privacy_card_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                BulletLine(stringResource(R.string.pilot_privacy_b1))
                BulletLine(stringResource(R.string.pilot_privacy_b2))
                BulletLine(stringResource(R.string.pilot_privacy_b3))
            }
        }
    }
}

@Composable
private fun BulletLine(
    text: String,
    bulletColor: Color? = null,
    textColor: Color? = null
) {
    val tint = bulletColor ?: MaterialTheme.colorScheme.primary
    val body = textColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "·",
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = body
        )
    }
}

@Composable
private fun KpiGrid(
    totalEventsLabel: String,
    activeDaysLabel: String,
    firstActivityLabel: String,
    lastActivityLabel: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiTile(
                label = "Total de eventos",
                value = totalEventsLabel,
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                label = "Días con actividad",
                value = activeDaysLabel,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiTile(
                label = "Primera actividad",
                value = firstActivityLabel,
                modifier = Modifier.weight(1f),
                valueStyle = MaterialTheme.typography.titleMedium
            )
            KpiTile(
                label = "Última actividad",
                value = lastActivityLabel,
                modifier = Modifier.weight(1f),
                valueStyle = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun KpiTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = MaterialTheme.typography.headlineSmall
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f)
            )
            Text(
                text = value,
                style = valueStyle,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun FlowDurationExplainerCard() {
    var expanded by remember { mutableStateOf(false) }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Duración aproximada de uso",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Indica cuánto tiempo estuvo abierta una pantalla al salir de un flujo " +
                            "(chat, autoevaluación o micromódulo). Los números son rangos, no minutos exactos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (expanded) "Ocultar escalas de tiempo" else "Ver escalas de tiempo",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DurationBucketLine("0", "Menos de 30 segundos")
                    DurationBucketLine("1", "Entre 30 segundos y 2 minutos")
                    DurationBucketLine("2", "Entre 2 y 10 minutos")
                    DurationBucketLine("3", "Entre 10 y 30 minutos")
                    DurationBucketLine("4", "Más de 30 minutos")
                }
            }
        }
    }
}

@Composable
private fun DurationBucketLine(code: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricDetailRow(
    label: String,
    valueText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyPilotMetricsState() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Analytics,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
            )
            Text(
                text = "Aún no hay conteos técnicos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Si usas la app, aquí pueden aparecer agregados anónimos para registro académico. " +
                    "No son la lectura principal de efectividad.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
