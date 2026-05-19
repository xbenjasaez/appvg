package cl.ipvg.docentecalma.safety.di

import cl.ipvg.docentecalma.safety.InstallPseudonymProvider
import cl.ipvg.docentecalma.safety.InstallPseudonymSource
import cl.ipvg.docentecalma.safety.LocalRiskEventSink
import cl.ipvg.docentecalma.safety.RiskEventSink
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Cablea la implementación de [RiskEventSink] usada por la app.
 * Por ahora se inyecta la versión local (sin BD); reemplazar este `@Binds`
 * por una variante remota habilita la cola de revisión humana sin tocar
 * los consumidores.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SafetyModule {

    @Binds
    @Singleton
    abstract fun bindRiskEventSink(impl: LocalRiskEventSink): RiskEventSink

    @Binds
    @Singleton
    abstract fun bindInstallPseudonymSource(
        impl: InstallPseudonymProvider
    ): InstallPseudonymSource
}
