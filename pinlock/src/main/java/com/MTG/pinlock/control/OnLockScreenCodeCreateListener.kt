package com.MTG.pinlock.control

/**
 * Pin Code create callback interface.
 */
interface OnLockScreenCodeCreateListener {
    /**
     * Callback method for pin code creation.
     *
     * @param encodedCode encoded pin code string.
     */
    fun onCodeCreated(encodedCode: String?)

    /**
     * This will be called if PFFLockScreenConfiguration#newCodeValidation is true.
     * User need to input new code twice. This method will be called when second code isn't
     * the same as first.
     */
    fun onNewCodeValidationFailed()
}