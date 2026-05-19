package cl.ipvg.docentecalma.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun PostUseFeedbackSheetContent(
    onSubmit: (satisfaction: Int, usefulness: Int, ease: Int, comment: String?) -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var satisfaction by rememberSaveable { mutableIntStateOf(0) }
    var usefulness by rememberSaveable { mutableIntStateOf(0) }
    var ease by rememberSaveable { mutableIntStateOf(0) }
    var comment by rememberSaveable { mutableStateOf("") }

    val canSend = satisfaction in 1..5 && usefulness in 1..5 && ease in 1..5

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Text(
            text = "¿Te está sirviendo?",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tres respuestas rápidas (1 = poco, 5 = mucho). Opcional: una línea abajo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        RatingRow(
            label = "¿Qué tan conforme quedaste con la app?",
            value = satisfaction,
            onValue = { satisfaction = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        RatingRow(
            label = "¿Te resultó útil?",
            value = usefulness,
            onValue = { usefulness = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        RatingRow(
            label = "¿Fue fácil de usar?",
            value = ease,
            onValue = { ease = it }
        )

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = comment,
            onValueChange = { new -> if (new.length <= 280) comment = new },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Algo que quieras contar (opcional)") },
            placeholder = { Text("Ej.: me gustaría…") },
            minLines = 2,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onNotNow) {
                Text("Ahora no")
            }
            Button(
                onClick = {
                    onSubmit(satisfaction, usefulness, ease, comment.trim().ifBlank { null })
                },
                enabled = canSend
            ) {
                Text("Listo")
            }
        }
    }
}

@Composable
private fun RatingRow(
    label: String,
    value: Int,
    onValue: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (n in 1..5) {
                val selected = value == n
                FilterChip(
                    selected = selected,
                    onClick = { onValue(n) },
                    label = {
                        Text(
                            text = n.toString(),
                            modifier = Modifier.semantics {
                                contentDescription = "$n de 5"
                            }
                        )
                    }
                )
            }
        }
    }
}
