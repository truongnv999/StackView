package com.viewlibs.stack_view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import kotlin.math.abs

class StackLayoutCustom @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var initialX = 0f
    private var initialY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val gestureDetector: GestureDetector

    // Header TextView displays text at the top edge, always beneath the CardViews.
    // Gravity set to center horizontally.
    private val headerTextView: TextView = TextView(context).apply {
        text = ""
        gravity = Gravity.CENTER_HORIZONTAL
    }

    init {
        // Add header at index 0 so it's always at the bottom (background)
        addView(headerTextView, 0)

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                initialY = e.y
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                getTopChild()?.let { topChild ->
                    val deltaY = e2.y - initialY
                    val newTop = topChild.top + deltaY
                    if (newTop < 0) {
                        topChild.translationY = -topChild.top.toFloat()
                    } else {
                        topChild.translationY = deltaY
                    }
                }
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                getTopChild()?.let { topChild ->
                    if (abs(velocityY) > abs(velocityX) && velocityY < 0) {
                        animateSwipe(topChild, topChild.translationY)
                        return true
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialX = ev.x
                initialY = ev.y
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = abs(ev.x - initialX)
                val dy = abs(ev.y - initialY)
                if (dy > touchSlop && dy > dx) {
                    return ev.y < initialY
                }
                return false
            }
            else -> return false
        }
    }

    /**
     * Gets the topmost CardView (ignoring the header TextView).
     */
    private fun getTopChild(): CardView? {
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            if (child is CardView) {
                return child
            }
        }
        return null
    }

    /**
     * Handles animation when a CardView is swiped off the screen.
     * After animation, the CardView is reset, sent to the back,
     * and added again right after the header (index 1)
     * to create an infinite loop swipe effect.
     */
    private fun animateSwipe(view: CardView, currentTranslationY: Float) {
        val targetTranslationY = if (currentTranslationY > 0) height.toFloat() else -height.toFloat()
        view.animate()
            .translationY(targetTranslationY)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                view.translationY = -view.height.toFloat()
                view.alpha = 1f

                // Remove current view and add it again right after header (index 1)
                removeView(view)
                addView(view, 1)

                view.animate()
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(OvershootInterpolator(1.0f))
                    .start()

                getTopChild()?.let { newTop ->
                    newTop.scaleX = 0.9f
                    newTop.scaleY = 0.9f
                    newTop.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setInterpolator(OvershootInterpolator(1.0f))
                        .start()
                }
            }
            .start()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            getTopChild()?.let { topChild ->
                if (topChild.top + topChild.translationY <= 0) {
                    animateSwipe(topChild, topChild.translationY)
                } else if (abs(topChild.translationY) >= topChild.height / 2) {
                    if (topChild.translationY < 0) {
                        animateSwipe(topChild, topChild.translationY)
                    } else {
                        topChild.animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(200)
                            .start()
                    }
                } else {
                    topChild.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
            }
        }
        return true
    }

    /**
     * Adds a CardView to the stack with a fade-in effect.
     * If the view already belongs to this layout, just animate fade-in.
     * Otherwise, ensure it's removed from its old parent
     * and added at index 1 (right after header).
     */
    fun addStackChild(child: CardView) {
        if (child.parent === this) {
            child.animate().cancel()
            child.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
            return
        }

        // Remove from old parent if needed
        (child.parent as? ViewGroup)?.removeView(child)

        child.isClickable = false
        child.alpha = 0f
        // Add right after header (index 1)
        addView(child, 1)

        child.animate().cancel()
        child.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    /**
     * Removes a CardView from the stack with a fade-out effect.
     */
    fun removeStackChild(child: CardView) {
        child.animate().cancel()
        child.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                if (child.parent == this) {
                    removeView(child)
                }
            }
            .start()
    }

    /**
     * Allows setting text, text size and text color for the header TextView.
     * TextSize and textColor are optional parameters.
     *
     * @param text The content to display.
     * @param textSize (Optional) Text size in sp.
     * @param textColor (Optional) Text color as ARGB int.
     */
    fun setHeaderText(text: String, textSize: Float? = null, textColor: Int? = null) {
        headerTextView.text = text
        textSize?.let { headerTextView.textSize = it }
        textColor?.let { headerTextView.setTextColor(it) }
    }

    /**
     * onMeasure:
     * - Measure header TextView size.
     * - Measure all CardViews with decreasing size for stacked effect.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(parentWidth, parentHeight)

        // Measure header TextView
        measureChild(headerTextView, widthMeasureSpec, heightMeasureSpec)

        val screenWidth = context.resources.displayMetrics.widthPixels

        val cardViews = mutableListOf<CardView>()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is CardView) {
                cardViews.add(child)
            }
        }

        var currentWidth = (parentWidth * 0.95).toInt()
        var currentHeight = (screenWidth * 0.16).toInt()
        for (child in cardViews.reversed()) {
            val childWidthSpec = MeasureSpec.makeMeasureSpec(currentWidth, MeasureSpec.EXACTLY)
            val childHeightSpec = MeasureSpec.makeMeasureSpec(currentHeight, MeasureSpec.EXACTLY)
            child.measure(childWidthSpec, childHeightSpec)
            currentWidth = (currentWidth * 0.9).toInt()
            currentHeight = (currentHeight * 0.9).toInt()
        }
    }

    /**
     * onLayout:
     * - Layout the header TextView at the top.
     * - Center and stack CardViews with offset logic.
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val parentWidth = right - left
        val parentHeight = bottom - top

        // Layout header TextView at the top (full width, wrap height)
        headerTextView.layout(0, 0, parentWidth, headerTextView.measuredHeight)

        val cardViews = mutableListOf<CardView>()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is CardView) {
                cardViews.add(child)
            }
        }

        if (cardViews.isNotEmpty()) {
            val topView = cardViews.last()
            val gap = topView.measuredHeight * 0.15f
            val topViewTop = ((parentHeight - topView.measuredHeight) / 2f)
            for ((index, child) in cardViews.withIndex()) {
                val layerIndex = cardViews.size - 1 - index
                val offset = if (cardViews.size >= 3) {
                    if (layerIndex < 2) layerIndex * gap else 2 * gap
                } else {
                    layerIndex * gap
                }
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                val childLeft = (parentWidth - childWidth) / 2
                val childTop = topViewTop + offset
                child.layout(
                    childLeft,
                    childTop.toInt(),
                    childLeft + childWidth,
                    (childTop + childHeight).toInt()
                )
            }
        }
    }
}