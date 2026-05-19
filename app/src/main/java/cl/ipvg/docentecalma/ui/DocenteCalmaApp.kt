package cl.ipvg.docentecalma.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import cl.ipvg.docentecalma.navigation.DocenteCalmaNavHost
import cl.ipvg.docentecalma.ui.theme.DocenteCalmaTheme

@Composable
fun DocenteCalmaApp() {
    DocenteCalmaTheme {
        val navController = rememberNavController()
        DocenteCalmaNavHost(navController = navController)
    }
}
