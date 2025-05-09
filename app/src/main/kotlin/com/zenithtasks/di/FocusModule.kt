package com.zenithtasks.di

import android.content.Context
import com.zenithtasks.focus.FocusRepository
import com.zenithtasks.focus.FocusRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides Focus-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object FocusModule {

    /**
     * Provides a singleton implementation of FocusRepository
     */
    @Provides
    @Singleton
    fun provideFocusRepository(@ApplicationContext context: Context): FocusRepository {
        return FocusRepositoryImpl(context)
    }
}