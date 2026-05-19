package cl.ipvg.docentecalma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import cl.ipvg.docentecalma.data.repository.PilotAnalyticsRepository
import cl.ipvg.docentecalma.ui.DocenteCalmaApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var pilotAnalyticsRepository: PilotAnalyticsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocenteCalmaApp()
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            pilotAnalyticsRepository.recordDayActiveIfNeeded()
        }
    }
}
