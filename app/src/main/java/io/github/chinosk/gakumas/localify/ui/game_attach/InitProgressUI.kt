package io.github.chinosk.gakumas.localify.ui.game_attach

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.github.chinosk.gakumas.localify.TAG
import io.github.chinosk.gakumas.localify.models.NativeInitProgress
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class InitProgressUI {
    private var uiCreated = false
    private lateinit var rootView: ViewGroup
    private lateinit var container: LinearLayout
    private lateinit var assembliesProgressBar: ProgressBar
    private lateinit var classProgressBar: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var assembliesProgressText: TextView
    private lateinit var classProgressText: TextView


    @SuppressLint("SetTextI18n")
    fun createView(context: Context) {
        if (uiCreated) return
        uiCreated = true
        val activity = context as? Activity ?: return
        rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                marginEnd = 20
                marginStart = 20
                topMargin = 100
            }
            setBackgroundColor(Color.WHITE)
            setPadding(20, 20, 20, 20)
        }

        // Set up the container layout
        assembliesProgressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
            max = 100
        }

        // Set up the class progress bar
        classProgressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
            max = 100
        }

        assembliesProgressBar.progressTintList = ColorStateList.valueOf(Color.parseColor("#FFF89400"))
        classProgressBar.progressTintList = ColorStateList.valueOf(Color.parseColor("#FFF89400"))

        // Set up the text views
        titleText = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
                gravity = Gravity.CENTER_HORIZONTAL
            }
            setTextColor(Color.BLACK)
            text = "Initializing"
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
        }

        val textLayout = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 20
        }

        assembliesProgressText = TextView(context).apply {
            layoutParams = textLayout
            setTextColor(Color.BLACK)
        }

        classProgressText = TextView(context).apply {
            layoutParams = textLayout
            setTextColor(Color.BLACK)
        }

        // Add container to the root view
        context.runOnUiThread {
            // Add views to the container
            container.addView(titleText)
            container.addView(assembliesProgressText)
            container.addView(assembliesProgressBar)
            container.addView(classProgressText)
            container.addView(classProgressBar)

            rootView.addView(container)
        }
    }

    @SuppressLint("SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    fun finishLoad(context: Activity) {
        if (!uiCreated) return
        uiCreated = false
        GlobalScope.launch {
            context.runOnUiThread {
                assembliesProgressBar.progressTintList = ColorStateList.valueOf(Color.parseColor("#FF28B463"))
                classProgressBar.progressTintList = ColorStateList.valueOf(Color.parseColor("#FF28B463"))
                titleText.text = "Finished"
            }
            delay(1500L)
            context.runOnUiThread {
                rootView.removeView(container)
            }
        }
    }

    fun removeView(context: Activity) {
        if (!uiCreated) return
        uiCreated = false
        context.runOnUiThread {
            rootView.removeView(container)
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateData(context: Activity) {
        if (!uiCreated) return
        //return

        context.runOnUiThread {
            val assembliesProgress = NativeInitProgress.assembliesProgress
            val classProgress = NativeInitProgress.classProgress

            assembliesProgressText.text = "${assembliesProgress.current}/${assembliesProgress.total}"
            classProgressText.text = "${classProgress.current}/${classProgress.total}"

            assembliesProgressBar.setProgress((assembliesProgress.current * 100 / assembliesProgress.total).toInt(), true)
            classProgressBar.setProgress((classProgress.current * 100 / classProgress.total).toInt(), true)
        }
    }
}
