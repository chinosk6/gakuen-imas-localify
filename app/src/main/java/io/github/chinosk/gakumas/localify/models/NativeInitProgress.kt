package io.github.chinosk.gakumas.localify.models

data class ProgressData(
    var current: Long = 0,
    var total: Long = 1
)

object NativeInitProgress {
    var assembliesProgress = ProgressData()
    var classProgress = ProgressData()
    var startInit: Boolean = false

    fun setAssembliesProgressData(current: Long, total: Long) {
        assembliesProgress.current = current
        assembliesProgress.total = total
    }

    fun setClassProgressData(current: Long, total: Long) {
        classProgress.current = current
        classProgress.total = total
    }

    @JvmStatic
    external fun pluginInitProgressLooper(progress: NativeInitProgress)
}