package com.zenithtasks.di

import com.zenithtasks.data.repository.FirebaseSyncRepository
import com.zenithtasks.data.repository.FocusSessionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Entry point for accessing repositories from non-DI classes such as widgets.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RepositoryEntryPoint {
    fun focusSessionRepository(): FocusSessionRepository
    fun firebaseSyncRepository(): FirebaseSyncRepository
}