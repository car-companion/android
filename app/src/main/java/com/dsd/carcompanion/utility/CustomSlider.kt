package com.dsd.carcompanion.utility

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.dsd.carcompanion.R
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider

class CustomSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Slider(context, attrs, defStyleAttr) {

    init {
        // Disable the label formatter
        setLabelFormatter(null)

        // Set custom thumb drawable and track colors
        setCustomThumbDrawable()
        setCustomTrackColors()
    }

    private fun setCustomThumbDrawable() {
        val customThumbDrawable = ContextCompat.getDrawable(context, R.drawable.switch_thumb)
        // Uncomment this to set a custom thumb drawable
        // customThumbDrawable?.let { thumb -> this.setThumbDrawable(thumb) }
    }

    private fun setCustomTrackColors() {
        trackActiveTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.main_green)
        )
        trackInactiveTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.main_gray)
        )
    }
}
