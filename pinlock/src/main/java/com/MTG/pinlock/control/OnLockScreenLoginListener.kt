package com.MTG.pinlock.control

/**
 * Login callback interface.
 */
interface OnLockScreenLoginListener {
    /**
     * Callback method for successful login attempt with pin code.
     */
    fun onCodeInputSuccessful()

    /**
     * Callback method for unsuccessful login attempt with pin code.
     */
    fun onPinLoginFailed()

    /**
     * Callback method for successful login attempt with fingerprint.
     */
    fun onFingerprintSuccessful()

    /**
     * Callback method for unsuccessful login attempt with fingerprint.
     */
    fun onFingerprintLoginFailed()
}