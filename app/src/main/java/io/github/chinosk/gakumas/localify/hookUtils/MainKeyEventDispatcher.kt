package io.github.chinosk.gakumas.localify.hookUtils

import android.view.KeyEvent

object MainKeyEventDispatcher {
    private val targetDbgKeyList: IntArray = intArrayOf(19, 19, 20, 20, 21, 22, 21, 22, 30, 29)
    private var currentIndex = 0

    fun checkDbgKey(code: Int, action: Int): Boolean {
        if (action == KeyEvent.ACTION_UP) return false
        if (targetDbgKeyList[currentIndex] == code) {
            if (currentIndex == targetDbgKeyList.size - 1) {
                currentIndex = 0
                return true
            }
            else {
                currentIndex++
            }
        }
        else {
            currentIndex = 0
        }
        return false
    }

}