package com.intas.metrolog.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import com.intas.metrolog.R

class ViewUtil {
    companion object {
        /**
         * эффект нажатия кнопки
         */
        fun runAnimationButton(context: Context, view: View) {
            val animation = AnimationUtils.loadAnimation(context, R.anim.image_button_animation)
            view.startAnimation(animation)
        }

        /**
         * Скрытие класиатуры для вызываемой [Activity]
         *
         * @param activity [Activity], для которой необходимо скрыть клавиатуру
         */
        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}