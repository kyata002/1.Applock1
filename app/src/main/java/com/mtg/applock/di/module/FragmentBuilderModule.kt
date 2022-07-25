package com.mtg.applock.di.module

import com.mtg.applock.di.scope.FragmentScope
import com.mtg.applock.ui.activity.main.az.AzFragment
import com.mtg.applock.ui.activity.main.locked.LockedFragment
import com.mtg.applock.ui.activity.main.personal.PersonalFragment
import com.mtg.applock.ui.activity.main.settings.theme.background.BackgroundFragment
import com.mtg.applock.ui.activity.main.settings.theme.keypad.KeypadFragment
import com.mtg.applock.ui.activity.main.settings.theme.pattern.PatternFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun privacyFragment(): AzFragment



    @FragmentScope
    @ContributesAndroidInjector
    abstract fun personalFragment(): PersonalFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun lockedFragment(): LockedFragment



    @FragmentScope
    @ContributesAndroidInjector
    abstract fun patternFragment(): PatternFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun keypadFragment(): KeypadFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun backgroundFragment(): BackgroundFragment
}