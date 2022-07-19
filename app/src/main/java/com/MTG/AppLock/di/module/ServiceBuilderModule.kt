package com.MTG.AppLock.di.module

import com.MTG.AppLock.service.AppLockerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class ServiceBuilderModule {
    @ContributesAndroidInjector abstract fun appLockerService(): AppLockerService
    //    @ContributesAndroidInjector
    //    abstract fun callBlockerService(): CallBlockerScreeningService
}