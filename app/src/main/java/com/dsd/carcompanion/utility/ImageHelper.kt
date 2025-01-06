package com.dsd.carcompanion.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.widget.ImageView
import androidx.core.content.ContextCompat

object ImageHelper {

    fun applyBlurAndColorFilterToImageView(
        imageView: ImageView,
        context: Context?,
        svgResource: Int,
        blurRadius: Float = 400f,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Load the SVG drawable from the resources
            val svgDrawable = context?.let {
                ContextCompat.getDrawable(it, svgResource)
            } as VectorDrawable

            // Convert the SVG to a Bitmap
            val bitmap = Bitmap.createBitmap(
                svgDrawable.intrinsicWidth,
                svgDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            svgDrawable.setBounds(0, 0, canvas.width, canvas.height)
            svgDrawable.draw(canvas)

            // Apply blur effect to the Bitmap
            val blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)

            // Set the image on the ImageView and apply the RenderEffect (blur)
            imageView.setImageBitmap(bitmap)
            imageView.setRenderEffect(blurEffect)
        }
    }
}