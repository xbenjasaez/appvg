package cl.ipvg.docentecalma.ai.di

import cl.ipvg.docentecalma.ai.ResilientSupportChatAi
import cl.ipvg.docentecalma.ai.SupportChatAi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Registra la implementación por defecto de [SupportChatAi] como
 * [ResilientSupportChatAi], que compone Gemini + fallback local + reintentos.
 *
 * Para tests de instrumentación o fakes basta con reemplazar este módulo:
 * el ViewModel depende solo de la interfaz.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindSupportChatAi(impl: ResilientSupportChatAi): SupportChatAi
}
