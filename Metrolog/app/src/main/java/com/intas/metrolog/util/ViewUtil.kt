package com.intas.metrolog.util

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
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
    }
}