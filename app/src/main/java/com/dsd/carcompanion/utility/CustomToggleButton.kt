package com.dsd.carcompanion.utility

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.dsd.carcompanion.R
import com.google.android.material.card.MaterialCardView

class CustomToggleButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val icon: ImageView

    var isToggled: Boolean = false
        set(value) {
            field = value
            updateState()
        }

    init {
        // Inflate the layout
        inflate(context, R.layout.custom_toggle, this)

        // Initialize child views
        icon = findViewById(R.id.toggle_button_icon)

        // Set click listener
        setOnClickListener {
            isToggled = !isToggled
        }
    }

    private fun updateState() {
        if (isToggled) {
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.main_green))
            icon.setColorFilter(ContextCompat.getColor(context, R.color.white))
            background = ContextCompat.getDrawable(context, R.drawable.toggled_bg)
        } else {
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.main_gray))
            icon.setColorFilter(ContextCompat.getColor(context, R.color.main_gray))
            background = ContextCompat.getDrawable(context, R.drawable.untoggled_bg)
        }
    }


    fun setToggleIcon(drawableResId: Int) {
        icon.setImageResource(drawableResId)
    }
}
