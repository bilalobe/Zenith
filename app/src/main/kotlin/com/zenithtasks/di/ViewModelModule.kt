package com.zenithtasks.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zenithtasks.ui.viewmodel.AccountViewModel
import com.zenithtasks.ui.viewmodel.EnergyMatchingViewModel
import com.zenithtasks.ui.viewmodel.FocusViewModel
import com.zenithtasks.ui.viewmodel.TaskAnalyticsViewModel
import com.zenithtasks.ui.viewmodel.TaskDetailViewModel
import com.zenithtasks.ui.viewmodel.TaskListViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.multibindings.IntoMap

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {

    @Binds
    @ViewModelScoped
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(EnergyMatchingViewModel::class)
    abstract fun bindEnergyMatchingViewModel(viewModel: EnergyMatchingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TaskAnalyticsViewModel::class)
    abstract fun bindTaskAnalyticsViewModel(viewModel: TaskAnalyticsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FocusViewModel::class)
    abstract fun bindFocusViewModel(viewModel: FocusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(viewModel: AccountViewModel): ViewModel
    
    @Binds
    @IntoMap
    @ViewModelKey(TaskListViewModel::class)
    abstract fun bindTaskListViewModel(viewModel: TaskListViewModel): ViewModel
    
    @Binds
    @IntoMap
    @ViewModelKey(TaskDetailViewModel::class)
    abstract fun bindTaskDetailViewModel(viewModel: TaskDetailViewModel): ViewModel
}