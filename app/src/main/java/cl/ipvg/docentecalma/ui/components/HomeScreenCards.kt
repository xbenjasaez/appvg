package cl.ipvg.docentecalma.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.ipvg.docentecalma.domain.model.EmotionCategory
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotEmotionMapper
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.util.DateTimeFormatters
import cl.ipvg.docentecalma.ui.theme.IpvgBluePrimary
import cl.ipvg.docentecalma.ui.theme.IpvgBlueVirginio
import cl.ipvg.docentecalma.ui.theme.IpvgGrayBrand
import cl.ipvg.docentecalma.ui.theme.IpvgGreen
import cl.ipvg.docentecalma.ui.theme.IpvgGreenSoft
import cl.ipvg.docentecalma.ui.theme.IpvgOrange
import cl.ipvg.docentecalma.ui.theme.IpvgOrangeSoft

enum class HomeEntryTier {
    /** Accesos principales del producto. */
    PrimaryPortal,

    /** Recursos y materiales de apoyo. */
    Resource,

    /** Seguimiento, utilidades y enlaces terciarios. */
    Utility
}

@Composable
fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showDivider: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HomeEntryCard(
    icon: ImageVector,
    title: String,
    description: String,
    tier: HomeEntryTier,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentOverride: Color? = null
) {
    val accent = accentOverride ?: when (tier) {
        HomeEntryTier.PrimaryPortal -> IpvgBlueVirginio
        HomeEntryTier.Resource -> IpvgBluePrimary
        HomeEntryTier.Utility -> IpvgGrayBrand
    }
    val elevation = when (tier) {
        HomeEntryTier.PrimaryPortal -> 2.dp
        HomeEntryTier.Resource -> 1.dp
        HomeEntryTier.Utility -> 0.dp
    }
    val stripeColor = when (tier) {
        HomeEntryTier.PrimaryPortal -> accent
        HomeEntryTier.Resource -> IpvgBlueVirginio.copy(alpha = 0.45f)
        HomeEntryTier.Utility -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }
    val shape = MaterialTheme.shapes.large

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(stripeColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val iconShape = RoundedCornerShape(14.dp)
                val iconBg = when (tier) {
                    HomeEntryTier.PrimaryPortal -> accent.copy(alpha = 0.12f)
                    HomeEntryTier.Resource -> accent.copy(alpha = 0.1f)
                    HomeEntryTier.Utility -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(iconShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HomeLatestCheckInCard(
    checkIn: EmotionalCheckIn,
    modifier: Modifier = Modifier
) {
    val category = checkIn.emotion.category
    val stripe = emotionCategoryStripeColor(category)
    val softWash = when (category) {
        EmotionCategory.DIFFICULT_HIGH_ACTIVATION -> Brush.horizontalGradient(
            colors = listOf(IpvgOrangeSoft.copy(alpha = 0.35f), Color.Transparent)
        )
        EmotionCategory.DIFFICULT_LOW_ENERGY -> Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                Color.Transparent
            )
        )
        EmotionCategory.REGULATED_POSITIVE -> Brush.horizontalGradient(
            colors = listOf(IpvgGreenSoft.copy(alpha = 0.4f), Color.Transparent)
        )
    }
    val mascotState = MascotEmotionMapper.fromEmotion(checkIn.emotion)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(softWash)
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(stripe, RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier.padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Mascot(
                            state = mascotState,
                            contentDescription = "Estado de ${MascotPersona.NAME} acorde a su último chequeo",
                            sizeDp = 64.dp
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Último chequeo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = checkIn.emotion.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Intensidad ${checkIn.intensity} de 5",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = DateTimeFormatters.relative(checkIn.createdAt),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!checkIn.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = checkIn.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun emotionCategoryStripeColor(category: EmotionCategory): Color = when (category) {
    EmotionCategory.DIFFICULT_HIGH_ACTIVATION -> IpvgOrange
    EmotionCategory.DIFFICULT_LOW_ENERGY -> IpvgBlueVirginio
    EmotionCategory.REGULATED_POSITIVE -> IpvgGreen
}