package cl.ipvg.docentecalma.ui.mascot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Mensaje centrado con la mascota arriba. Para estados vacíos y errores
 * de carga sin repetir layout en cada pantalla.
 *
 * @param state estado visual del personaje ([MascotState.Idle],
 * [MascotState.Listening], [MascotState.ErrorState], etc.).
 * @param message texto debajo de la ilustración.
 * @param isError si es true, el mensaje usa el color [MaterialTheme.colorScheme.error].
 * @param mascotSize tamaño cuadrado de la mascota (64–72 dp recomendado).
 * @param contentDescription para lectores de pantalla; si es null y el mensaje
 * ya describe el contexto, puede omitirse según la pantalla.
 */
@Composable
internal fun MascotEmptyState(
    state: MascotState,
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
    mascotSize: Dp = 72.dp,
    contentDescription: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Mascot(
            state = state,
            contentDescription = contentDescription,
            sizeDp = mascotSize
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 8.dp)
        )
    }
}
