package cl.ipvg.docentecalma.data.di

import cl.ipvg.docentecalma.data.preferences.OnboardingConsentRepository
import cl.ipvg.docentecalma.data.preferences.OnboardingConsentRepositoryImpl
import cl.ipvg.docentecalma.data.preferences.PostUseFeedbackRepository
import cl.ipvg.docentecalma.data.preferences.PostUseFeedbackRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesBindingModule {

    @Binds
    @Singleton
    abstract fun bindOnboardingConsentRepository(
        impl: OnboardingConsentRepositoryImpl
    ): OnboardingConsentRepository

    @Binds
    @Singleton
    abstract fun bindPostUseFeedbackRepository(
        impl: PostUseFeedbackRepositoryImpl
    ): PostUseFeedbackRepository
}
