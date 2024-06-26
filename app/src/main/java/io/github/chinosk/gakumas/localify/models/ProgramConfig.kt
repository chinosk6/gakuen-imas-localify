package io.github.chinosk.gakumas.localify.models


data class ProgramConfig (
    var checkBuiltInAssets: Boolean = true,
    var transRemoteZipUrl: String = "",
    var useRemoteAssets: Boolean = false,
    var delRemoteAfterUpdate: Boolean = true,
    var cleanLocalAssets: Boolean = false,

    var p: Boolean = false
)
