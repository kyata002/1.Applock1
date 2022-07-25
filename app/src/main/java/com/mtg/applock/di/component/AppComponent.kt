package com.mtg.applock.di.component

import com.mtg.applock.AppLockerApplication
import com.mtg.applock.data.database.DatabaseModule
import com.mtg.applock.di.module.ActivityBuilderModule
import com.mtg.applock.di.module.AppModule
import com.mtg.applock.di.module.ServiceBuilderModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, ActivityBuilderModule::class, ServiceBuilderModule::class, AppModule::class, DatabaseModule::class])
interface AppComponent : AndroidInjector<AppLockerApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<AppLockerApplication>()
}