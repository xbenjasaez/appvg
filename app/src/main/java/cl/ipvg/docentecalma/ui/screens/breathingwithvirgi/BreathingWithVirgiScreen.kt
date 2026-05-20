package cl.ipvg.docentecalma.ui.screens.breathingwithvirgi



import androidx.compose.animation.AnimatedContent

import androidx.compose.animation.fadeIn

import androidx.compose.animation.fadeOut

import androidx.compose.animation.togetherWith

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.Button

import androidx.compose.material3.CardDefaults

import androidx.compose.material3.ElevatedCard

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.LinearProgressIndicator

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.OutlinedButton

import androidx.compose.material3.Text

import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip

import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold

import cl.ipvg.docentecalma.ui.mascot.Mascot

import cl.ipvg.docentecalma.ui.mascot.MascotPersona

import cl.ipvg.docentecalma.ui.mascot.MascotState



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun BreathingWithVirgiScreen(

    onBack: () -> Unit,

    viewModel: BreathingWithVirgiViewModel = hiltViewModel()

) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()



    val handleBack: () -> Unit = {

        viewModel.onEvent(BreathingWithVirgiEvent.OnExitSession)

        onBack()

    }



    DocenteCalmaScaffold(

        title = BreathingCopy.title,

        onBack = when (state.screenPhase) {

            BreathingScreenPhase.Running -> null

            else -> handleBack

        },

        actions = {

            if (state.screenPhase == BreathingScreenPhase.Running) {

                TextButton(onClick = handleBack) {

                    Text(BreathingCopy.ctaExitSession)

                }

            }

        }

    ) { padding ->

        when (state.screenPhase) {

            BreathingScreenPhase.Intro -> IntroContent(

                padding = padding,

                onStart = { viewModel.onEvent(BreathingWithVirgiEvent.OnStart) },

                onBack = handleBack

            )

            BreathingScreenPhase.Running -> RunningContent(

                padding = padding,

                state = state

            )

            BreathingScreenPhase.Finished -> FinishedContent(

                padding = padding,

                onRepeat = { viewModel.onEvent(BreathingWithVirgiEvent.OnRepeat) },

                onFinishBack = {

                    viewModel.onEvent(BreathingWithVirgiEvent.OnFinishGoBack)

                    onBack()

                }

            )

        }

    }

}



@Composable

private fun IntroContent(

    padding: PaddingValues,

    onStart: () -> Unit,

    onBack: () -> Unit

) {

    Column(

        modifier = Modifier

            .fillMaxSize()

            .padding(padding)

            .padding(horizontal = 20.dp, vertical = 16.dp)

            .verticalScroll(rememberScrollState()),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.spacedBy(24.dp)

    ) {

        Mascot(

            state = MascotState.Greeting,

            contentDescription = "Mascota ${MascotPersona.NAME}, lista para acompañarte",

            sizeDp = 96.dp,

            animate = false

        )



        ElevatedCard(

            modifier = Modifier.fillMaxWidth(),

            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),

            colors = CardDefaults.elevatedCardColors(

                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)

            )

        ) {

            Column(

                modifier = Modifier.padding(24.dp),

                verticalArrangement = Arrangement.spacedBy(10.dp)

            ) {

                Text(

                    text = BreathingCopy.title,

                    style = MaterialTheme.typography.headlineSmall,

                    color = MaterialTheme.colorScheme.onSurface

                )

                Text(

                    text = BreathingCopy.subtitle,

                    style = MaterialTheme.typography.titleMedium,

                    color = MaterialTheme.colorScheme.onSurface

                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(

                    text = BreathingCopy.introSupport,

                    style = MaterialTheme.typography.bodyLarge,

                    color = MaterialTheme.colorScheme.onSurfaceVariant

                )

            }

        }



        Button(

            onClick = onStart,

            modifier = Modifier.fillMaxWidth()

        ) {

            Text(BreathingCopy.ctaStart)

        }



        TextButton(onClick = onBack) {

            Text(BreathingCopy.ctaBackIntro)

        }

    }

}



@Composable

private fun RunningContent(

    padding: PaddingValues,

    state: BreathingWithVirgiUiState

) {

    val phaseColors = BreathingPhaseTheme.colorsFor(state.phase)

    val mascotState = BreathingSessionConfig.mascotFor(state.phase)



    Column(

        modifier = Modifier

            .fillMaxSize()

            .padding(padding)

            .padding(horizontal = 20.dp, vertical = 12.dp),

        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        LinearProgressIndicator(

            progress = { state.sessionProgress },

            modifier = Modifier

                .fillMaxWidth()

                .clip(RoundedCornerShape(4.dp)),

            color = phaseColors.accent,

            trackColor = phaseColors.progressTrack

        )



        Spacer(modifier = Modifier.height(20.dp))



        Box(

            modifier = Modifier

                .weight(1f)

                .fillMaxWidth(),

            contentAlignment = Alignment.Center

        ) {

            Column(

                horizontalAlignment = Alignment.CenterHorizontally,

                verticalArrangement = Arrangement.spacedBy(0.dp)

            ) {

                AnimatedContent(

                    targetState = mascotState,

                    transitionSpec = {

                        fadeIn(animationSpec = androidx.compose.animation.core.tween(220))

                            .togetherWith(fadeOut(animationSpec = androidx.compose.animation.core.tween(220)))

                    },

                    label = "breathing_mascot"

                ) { currentMascot ->

                    Mascot(

                        state = currentMascot,

                        contentDescription = "Mascota ${MascotPersona.NAME} acompañando la respiración",

                        sizeDp = 88.dp,

                        animate = false

                    )

                }



                Spacer(modifier = Modifier.height(16.dp))



                BreathingOrb(

                    phase = state.phase,

                    cycle = state.currentCycle,

                    phaseColors = phaseColors,

                    secondsRemaining = state.phaseSecondsRemaining,

                    phaseLabel = state.phaseLabel

                )



                Spacer(modifier = Modifier.height(20.dp))



                BreathingSessionHud(

                    secondsRemaining = state.phaseSecondsRemaining,

                    phaseLabel = state.phaseLabel,

                    cycleLabel = state.cycleProgressLabel,

                    accentColor = phaseColors.accent

                )

            }

        }

    }

}



@Composable

private fun FinishedContent(

    padding: PaddingValues,

    onRepeat: () -> Unit,

    onFinishBack: () -> Unit

) {

    Column(

        modifier = Modifier

            .fillMaxSize()

            .padding(padding)

            .padding(horizontal = 20.dp, vertical = 24.dp)

            .verticalScroll(rememberScrollState()),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.spacedBy(20.dp)

    ) {

        Mascot(

            state = MascotState.Cheering,

            contentDescription = "Mascota ${MascotPersona.NAME} celebrando tu pausa",

            sizeDp = 96.dp,

            animate = false

        )



        Column(

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.spacedBy(12.dp)

        ) {

            Text(

                text = BreathingCopy.closingMain,

                style = MaterialTheme.typography.headlineSmall,

                color = MaterialTheme.colorScheme.onSurface

            )

            Text(

                text = BreathingCopy.closingSecondary,

                style = MaterialTheme.typography.bodyLarge,

                color = MaterialTheme.colorScheme.onSurfaceVariant

            )

        }



        Spacer(modifier = Modifier.height(8.dp))



        Button(

            onClick = onFinishBack,

            modifier = Modifier.fillMaxWidth()

        ) {

            Text(BreathingCopy.ctaFinishBack)

        }



        OutlinedButton(

            onClick = onRepeat,

            modifier = Modifier.fillMaxWidth()

        ) {

            Text(BreathingCopy.ctaRepeat)

        }

    }

}

