package com.MTG.AppLock.di.component

import com.MTG.AppLock.AppLockerApplication
import com.MTG.AppLock.data.database.DatabaseModule
import com.MTG.AppLock.di.module.ActivityBuilderModule
import com.MTG.AppLock.di.module.AppModule
import com.MTG.AppLock.di.module.ServiceBuilderModule
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