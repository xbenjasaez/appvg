package cl.ipvg.docentecalma.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState

@Composable
fun OnboardingScreen(
    onConsentAccepted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var consentChecked by remember { mutableStateOf(false) }

    DocenteCalmaScaffold(title = "Antes de comenzar", onBack = null) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Docente Calma es voluntaria, personal y confidencial. " +
                    "No es una evaluación de tu desempeño laboral ni un sistema de control institucional.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Mascot(
                    state = MascotState.Listening,
                    contentDescription = null,
                    sizeDp = 48.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${MascotPersona.NAME} te acompaña en este espacio privado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            OnboardingCard(
                title = "Para qué sirve",
                body = "Apoyar tu bienestar socioemocional: registrar cómo te sientes, " +
                    "reflexionar con calma, ver tu historial personal y practicar microhábitos útiles en el aula."
            )

            OnboardingCard(
                title = "Qué se guarda en tu teléfono",
                body = "Tus chequeos, notas que escribas, historial y progreso se guardan solo en este dispositivo."
            )

            OnboardingCard(
                title = "Qué no sube a servidores de la app",
                body = "Tus chequeos y notas no se envían a internet desde esta app. " +
                    "Puedes revisar más detalle en Privacidad cuando quieras."
            )

            OnboardingCard(
                title = "Chat de apoyo (IA)",
                body = "Si usas el chat, tus mensajes de esa conversación se envían a la IA para generar una respuesta. " +
                    "El resto de tu información personal de la app no se envía."
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = consentChecked,
                    onCheckedChange = { consentChecked = it }
                )
                Text(
                    text = "He leído lo anterior y acepto usar la app en estos términos, " +
                        "entendiendo que es un apoyo personal y no sustituye ayuda profesional si la necesito.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 12.dp)
                )
            }

            Button(
                onClick = { viewModel.acceptConsent(onConsentAccepted) },
                enabled = consentChecked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Continuar")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OnboardingCard(title: String, body: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
