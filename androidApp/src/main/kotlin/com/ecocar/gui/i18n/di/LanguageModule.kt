package com.ecocar.gui.i18n.di

import com.ecocar.gui.i18n.AndroidLanguageRepository
import com.ecocar.gui.i18n.LanguageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LanguageModule {
    @Binds
    @Singleton
    abstract fun bindLanguageRepository(impl: AndroidLanguageRepository): LanguageRepository
}
