package com.mtg.applock.di.module

import com.mtg.applock.di.scope.ActivityScope
import com.mtg.applock.ui.activity.detail.DetailActivity
import com.mtg.applock.ui.activity.detail.DetailListActivity
import com.mtg.applock.ui.activity.first.FirstAllActivity
import com.mtg.applock.ui.activity.intruders.IntrudersPhotosActivity
import com.mtg.applock.ui.activity.intruders.detail.IntrudersPhotosDetailActivity
import com.mtg.applock.ui.activity.main.MainActivity
import com.mtg.applock.ui.activity.main.configuration.ConfigurationActivity
import com.mtg.applock.ui.activity.main.configuration.detail.DetailConfigurationActivity
import com.mtg.applock.ui.activity.main.configuration.edit.EditConfigurationActivity
import com.mtg.applock.ui.activity.main.personal.personallist.PersonalListActivity
import com.mtg.applock.ui.activity.main.settings.SettingsActivity
import com.mtg.applock.ui.activity.main.settings.apply.ApplyThemeActivity
import com.mtg.applock.ui.activity.main.settings.email.EmailActivity
import com.mtg.applock.ui.activity.main.settings.email.emailvalidate.EmailValidateActivity
import com.mtg.applock.ui.activity.main.settings.lockscreen.LockScreenActivity
import com.mtg.applock.ui.activity.main.settings.superpassword.SuperPasswordActivity
import com.mtg.applock.ui.activity.main.settings.superpassword.validate.SuperPasswordValidateActivity
import com.mtg.applock.ui.activity.main.settings.theme.ThemeActivity
import com.mtg.applock.ui.activity.main.settings.theme.UCropActivity
import com.mtg.applock.ui.activity.main.settings.theme.selected.SelectedImageActivity
import com.mtg.applock.ui.activity.move.MoveActivity
import com.mtg.applock.ui.activity.password.changepassword.ChangePasswordActivity
import com.mtg.applock.ui.activity.password.newpattern.CreateNewPatternActivity
import com.mtg.applock.ui.activity.password.overlay.activity.OverlayValidationActivity
import com.mtg.applock.ui.activity.password.overlay.activity.settings.OverlayValidationForSettingsActivity
import com.mtg.applock.ui.activity.policy.PolicyActivity
import com.mtg.applock.ui.activity.selected.SelectedActivity
import com.mtg.applock.ui.activity.splash.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun mainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun personalListActivity(): PersonalListActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun detailListActivity(): DetailListActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun detailActivity(): DetailActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun intrudersPhotosActivity(): IntrudersPhotosActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun detailConfigurationActivity(): DetailConfigurationActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun selectedActivity(): SelectedActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun selectedImageActivity(): SelectedImageActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun splashActivity(): SplashActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun themeActivity(): ThemeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun applyThemeActivity(): ApplyThemeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun moveActivity(): MoveActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun intrudersPhotosDetailActivity(): IntrudersPhotosDetailActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun uCropActivity(): UCropActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun editConfigurationActivity(): EditConfigurationActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun policyActivity(): PolicyActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun createNewPatternActivity(): CreateNewPatternActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun overlayValidationActivity(): OverlayValidationActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun overlayValidationForSettingsActivity(): OverlayValidationForSettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun lockScreenActivity(): LockScreenActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun firstAllActivity(): FirstAllActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun changePasswordActivity(): ChangePasswordActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun superPasswordActivity(): SuperPasswordActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun superPasswordValidateActivity(): SuperPasswordValidateActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun emailActivity(): EmailActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun emailValidateActivity(): EmailValidateActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun settingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun configurationActivity(): ConfigurationActivity
}