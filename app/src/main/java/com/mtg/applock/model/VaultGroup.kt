package com.mtg.applock.model

class VaultGroup {
    var vaultItem: VaultItem? = null
    var vaultList = mutableListOf<VaultItem>()
    var granted: Boolean = false

    constructor(vaultItem: VaultItem, granted: Boolean) {
        this.vaultItem = vaultItem
        this.granted = granted
    }

    constructor(vaultList: MutableList<VaultItem>, granted: Boolean) {
        this.vaultList = vaultList
        this.granted = granted
    }
}