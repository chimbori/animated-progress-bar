/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.focus.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import androidx.core.view.ViewCompat

class AnimatedProgressBar : ProgressBar {
  private var mPrimaryAnimator: ValueAnimator? = null
  private val mClosingAnimator = ValueAnimator.ofFloat(0f, 1f)
  private var mClipRegion = 0f
  private var mExpectedProgress = 0
  private var tempRect: Rect? = null
  private var mIsRtl = false
  private val mListener = AnimatorUpdateListener { setProgressImmediately(mPrimaryAnimator!!.animatedValue as Int) }

  constructor(context: Context) : super(context, null) {
    init(context, null)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    init(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    init(context, attrs)
  }

  override fun setProgress(nextProgress: Int) {
    mExpectedProgress = nextProgress.coerceIn(0, max)
    // a dirty-hack for reloading page.
    if (mExpectedProgress < progress && progress == max) {
      setProgressImmediately(0)
    }
    if (mPrimaryAnimator != null) {
      mPrimaryAnimator!!.cancel()
      mPrimaryAnimator!!.setIntValues(progress, nextProgress)
      mPrimaryAnimator!!.start()
    } else {
      setProgressImmediately(nextProgress)
    }
    if (mClosingAnimator != null) {
      if (nextProgress != max) { // stop closing animation
        mClosingAnimator.cancel()
        mClipRegion = 0f
      }
    }
  }

  public override fun onDraw(canvas: Canvas) {
    if (mClipRegion == 0f) {
      super.onDraw(canvas)
    } else {
      canvas.getClipBounds(tempRect)
      val clipWidth = tempRect!!.width() * mClipRegion
      val saveCount = canvas.save()
      if (mIsRtl) {
        canvas.clipRect(tempRect!!.left.toFloat(), tempRect!!.top.toFloat(), tempRect!!.right - clipWidth, tempRect!!.bottom.toFloat())
      } else {
        canvas.clipRect(tempRect!!.left + clipWidth, tempRect!!.top.toFloat(), tempRect!!.right.toFloat(), tempRect!!.bottom.toFloat())
      }
      super.onDraw(canvas)
      canvas.restoreToCount(saveCount)
    }
  }

  override fun setVisibility(value: Int) {
    if (value == View.GONE) {
      if (mExpectedProgress == max) {
        animateClosing()
      } else {
        setVisibilityImmediately(value)
      }
    } else {
      setVisibilityImmediately(value)
    }
  }

  private fun setVisibilityImmediately(value: Int) {
    super.setVisibility(value)
  }

  private fun animateClosing() {
    mIsRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
    mClosingAnimator!!.cancel()
    val handler = handler
    handler?.postDelayed({ mClosingAnimator.start() }, CLOSING_DELAY.toLong())
  }

  private fun setProgressImmediately(progress: Int) {
    super.setProgress(progress)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    tempRect = Rect()
    val a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedProgressBar)
    val duration = a.getInteger(R.styleable.AnimatedProgressBar_shiftDuration, 1000)
    val resID = a.getResourceId(R.styleable.AnimatedProgressBar_shiftInterpolator, 0)
    val wrap = a.getBoolean(R.styleable.AnimatedProgressBar_wrapShiftDrawable, false)
    mPrimaryAnimator = ValueAnimator.ofInt(progress, max)
    mPrimaryAnimator?.interpolator = LinearInterpolator()
    mPrimaryAnimator?.duration = PROGRESS_DURATION.toLong()
    mPrimaryAnimator?.addUpdateListener(mListener)
    mClosingAnimator!!.duration = CLOSING_DURATION.toLong()
    mClosingAnimator.interpolator = LinearInterpolator()
    mClosingAnimator.addUpdateListener { valueAnimator ->
      val region = valueAnimator.animatedValue as Float
      if (mClipRegion != region) {
        mClipRegion = region
        invalidate()
      }
    }
    mClosingAnimator.addListener(object : Animator.AnimatorListener {
      override fun onAnimationStart(animator: Animator) {
        mClipRegion = 0f
      }

      override fun onAnimationEnd(animator: Animator) {
        setVisibilityImmediately(View.GONE)
      }

      override fun onAnimationCancel(animator: Animator) {
        mClipRegion = 0f
      }

      override fun onAnimationRepeat(animator: Animator) {}
    })
    progressDrawable = buildWrapDrawable(progressDrawable, wrap, duration, resID)
    a.recycle()
  }

  private fun buildWrapDrawable(original: Drawable, isWrap: Boolean, duration: Int, resID: Int): Drawable {
    return if (isWrap) {
      val interpolator = if (resID > 0) AnimationUtils.loadInterpolator(context, resID) else null
      ShiftDrawable(original, duration, interpolator)
    } else {
      original
    }
  }

  companion object {
    private const val PROGRESS_DURATION = 200
    private const val CLOSING_DELAY = 300
    private const val CLOSING_DURATION = 300
  }
}