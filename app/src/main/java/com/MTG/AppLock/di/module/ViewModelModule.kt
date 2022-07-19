package com.MTG.AppLock.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.MTG.AppLock.di.ViewModelFactory
import com.MTG.AppLock.di.key.ViewModelKey
import com.MTG.AppLock.ui.activity.detail.DetailListViewModel
import com.MTG.AppLock.ui.activity.detail.DetailViewModel
import com.MTG.AppLock.ui.activity.first.FistAllViewModel
import com.MTG.AppLock.ui.activity.intruders.IntrudersPhotosViewModel
import com.MTG.AppLock.ui.activity.intruders.detail.IntrudersPhotosDetailViewModel
import com.MTG.AppLock.ui.activity.main.MainViewModel
import com.MTG.AppLock.ui.activity.main.az.AzViewModel
import com.MTG.AppLock.ui.activity.main.configuration.ConfigurationViewModel
import com.MTG.AppLock.ui.activity.main.configuration.detail.DetailConfigurationViewModel
import com.MTG.AppLock.ui.activity.main.configuration.edit.EditConfigurationViewModel
import com.MTG.AppLock.ui.activity.main.locked.LockedViewModel
import com.MTG.AppLock.ui.activity.main.personal.PersonalViewModel
import com.MTG.AppLock.ui.activity.main.personal.personallist.PersonalListViewModel
import com.MTG.AppLock.ui.activity.main.settings.SettingsViewModel
import com.MTG.AppLock.ui.activity.main.settings.apply.ApplyThemeViewModel
import com.MTG.AppLock.ui.activity.main.settings.email.EmailViewModel
import com.MTG.AppLock.ui.activity.main.settings.email.emailvalidate.EmailValidateViewModel
import com.MTG.AppLock.ui.activity.main.settings.lockscreen.LockScreenViewModel
import com.MTG.AppLock.ui.activity.main.settings.superpassword.SuperPasswordViewModel
import com.MTG.AppLock.ui.activity.main.settings.superpassword.validate.SuperPasswordValidateViewModel
import com.MTG.AppLock.ui.activity.main.settings.theme.ThemeViewModel
import com.MTG.AppLock.ui.activity.main.settings.theme.UCropViewModel
import com.MTG.AppLock.ui.activity.main.settings.theme.background.BackgroundViewModel
import com.MTG.AppLock.ui.activity.main.settings.theme.keypad.KeypadViewModel
import com.MTG.AppLock.ui.activity.main.settings.theme.pattern.PatternViewModel
import com.MTG.AppLock.ui.activity.main.settings.theme.selected.SelectedImageViewModel
import com.MTG.AppLock.ui.activity.move.MoveViewModel
import com.MTG.AppLock.ui.activity.password.changepassword.ChangePasswordViewModel
import com.MTG.AppLock.ui.activity.password.newpattern.CreateNewPatternViewModel
import com.MTG.AppLock.ui.activity.password.overlay.activity.OverlayValidationViewModel
import com.MTG.AppLock.ui.activity.policy.PolicyViewModel
import com.MTG.AppLock.ui.activity.selected.SelectedViewModel
import com.MTG.AppLock.ui.activity.splash.SplashModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {
    @IntoMap
    @Binds
    @ViewModelKey(OverlayValidationViewModel::class)
    abstract fun provideFingerPrintOverlayViewModel(overlayValidationViewModel: OverlayValidationViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(CreateNewPatternViewModel::class)
    abstract fun provideCreateNewPatternViewModel(createNewPatternViewModel: CreateNewPatternViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(MainViewModel::class)
    abstract fun provideMainVieWModel(mainViewModel: MainViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(AzViewModel::class)
    abstract fun providePrivacyVieWModel(azViewModel: AzViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(LockedViewModel::class)
    abstract fun provideLockedViewModel(lockedViewModel: LockedViewModel): ViewModel


    @IntoMap
    @Binds
    @ViewModelKey(SettingsViewModel::class)
    abstract fun provideSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(PersonalViewModel::class)
    abstract fun provideVaultViewModelV2(personalViewModel: PersonalViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(PersonalListViewModel::class)
    abstract fun provideVaultListViewModelV2(personalListViewModel: PersonalListViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(DetailListViewModel::class)
    abstract fun provideDetailListViewModel(detailListViewModel: DetailListViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(DetailViewModel::class)
    abstract fun provideDetailViewModel(detailViewModel: DetailViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(IntrudersPhotosViewModel::class)
    abstract fun provideIntrudersPhotosViewModel(intrudersPhotosViewModel: IntrudersPhotosViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(ConfigurationViewModel::class)
    abstract fun provideConfigurationViewModel(configurationViewModel: ConfigurationViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(DetailConfigurationViewModel::class)
    abstract fun provideDetailConfigurationViewModel(detailConfigurationViewModel: DetailConfigurationViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(SelectedViewModel::class)
    abstract fun provideSelectedViewModel(selectedViewModel: SelectedViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(SelectedImageViewModel::class)
    abstract fun provideSelectedImageViewModel(selectedImageViewModel: SelectedImageViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(SplashModel::class)
    abstract fun provideSplashModel(splashModel: SplashModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(ThemeViewModel::class)
    abstract fun provideThemeViewModel(themeViewModel: ThemeViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(ApplyThemeViewModel::class)
    abstract fun provideApplyThemeViewModel(applyThemeViewModel: ApplyThemeViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(MoveViewModel::class)
    abstract fun provideMoveViewModel(moveViewModel: MoveViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(IntrudersPhotosDetailViewModel::class)
    abstract fun provideIntrudersPhotosDetailViewModel(intrudersPhotosDetailViewModel: IntrudersPhotosDetailViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(UCropViewModel::class)
    abstract fun provideUCropViewModel(uCropViewModel: UCropViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(EditConfigurationViewModel::class)
    abstract fun provideEditConfigurationViewModel(editConfigurationViewModel: EditConfigurationViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(PolicyViewModel::class)
    abstract fun providePolicyViewModel(policyViewModel: PolicyViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(LockScreenViewModel::class)
    abstract fun provideLockScreenViewModel(lockScreenViewModel: LockScreenViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(FistAllViewModel::class)
    abstract fun provideFistAllViewModel(firstAllViewModel: FistAllViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(ChangePasswordViewModel::class)
    abstract fun provideChangePasswordViewModel(changePasswordViewModel: ChangePasswordViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(PatternViewModel::class)
    abstract fun providePatternViewModel(patternViewModel: PatternViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(KeypadViewModel::class)
    abstract fun provideKeypadViewModel(keypadViewModel: KeypadViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(BackgroundViewModel::class)
    abstract fun provideBackgroundViewModel(backgroundViewModel: BackgroundViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(SuperPasswordViewModel::class)
    abstract fun provideSuperPasswordViewModel(superPasswordViewModel: SuperPasswordViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(SuperPasswordValidateViewModel::class)
    abstract fun provideSuperPasswordValidateViewModel(superPasswordValidateViewModel: SuperPasswordValidateViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(EmailViewModel::class)
    abstract fun provideEmailViewModel(emailViewModel: EmailViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(EmailValidateViewModel::class)
    abstract fun provideEmailValidateViewModel(emailValidateViewModel: EmailValidateViewModel): ViewModel

    @Binds
    abstract fun provideViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}

