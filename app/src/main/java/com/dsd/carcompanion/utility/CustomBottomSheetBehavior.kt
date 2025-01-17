package com.dsd.carcompanion.utility

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CustomBottomSheetBehavior<V : View>(context: Context, attrs: AttributeSet?) :
    BottomSheetBehavior<V>(context, attrs) {

    private var draggableViewId: Int = View.NO_ID
    private var draggableView: View? = null

    fun setDraggableViewId(id: Int) {
        draggableViewId = id
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (draggableView == null && draggableViewId != View.NO_ID) {
            draggableView = parent.findViewById(draggableViewId)
        }

        val isTouchInsideDraggableView = isTouchInsideView(draggableView, event)
        return if (isTouchInsideDraggableView) {
            super.onInterceptTouchEvent(parent, child, event)
        } else {
            false
        }
    }

    private fun isTouchInsideView(view: View?, event: MotionEvent): Boolean {
        if (view == null) return false
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()

        /*Log.d("CustomBottomSheet", "View bounds: (${location[0]}, ${location[1]}) to (${location[0] + view.width}, ${location[1] + view.height})")
        Log.d("CustomBottomSheet", "Touch: ($x, $y)")
        Log.d("CustomBottomSheet", "First state: ${location[0]} <= $x <= ${location[0] + view.width}")
        Log.d("CustomBottomSheet", "Second state: ${location[1]} <= $y <= ${location[1] + view.height}")
        Log.d("CustomBottomSheet", "Is it true: ${x in location[0]..(location[0] + view.width) && y in location[1]..(location[1] + view.height)}")*/
        return x in location[0]..(location[0] + view.width) &&
                y in location[1]..(location[1] + view.height)
    }
}
