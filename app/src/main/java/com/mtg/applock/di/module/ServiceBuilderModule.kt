package com.mtg.applock.di.module

import com.mtg.applock.service.AppLockerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class ServiceBuilderModule {
    @ContributesAndroidInjector abstract fun appLockerService(): AppLockerService
    //    @ContributesAndroidInjector
    //    abstract fun callBlockerService(): CallBlockerScreeningService
}