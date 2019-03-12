package com.wojciechkolendo.loadingview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import java.util.*

/**
 * @author Wojciech Kolendo
 */
class LoadingView : View {

	private val INDETERMINANT_MIN_SWEEP = 15f

	private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
	private val bounds = RectF()
	private var size = 0

	private var isIndeterminate: Boolean = false
	private var autostartAnimation: Boolean = false
	private var currentProgress: Float = 0f
	private var maxProgress: Float = 0f
	private var indeterminateSweep: Float = 0f
	private var indeterminateRotateOffset: Float = 0f
	private var thickness: Int = 0
	private var color: Int = 0
	private var animDuration: Int = 0
	private var animSwoopDuration: Int = 0
	private var animSyncDuration: Int = 0
	private var animSteps: Int = 0

	private val listeners = ArrayList<LoadingViewListener>()

	// Animation related stuff
	private var startAngle: Float = 0f
	private var actualProgress: Float = 0f
	private var startAngleRotate: ValueAnimator? = null
	private var progressAnimator: ValueAnimator? = null
	private var indeterminateAnimator: AnimatorSet? = null
	private var initialStartAngle: Float = 0f

	constructor(context: Context) : super(context) {
		init(null, 0)
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		init(attrs, 0)
	}

	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
		init(attrs, defStyle)
	}

	private fun init(attrs: AttributeSet?, defStyle: Int) {
		initAttributes(attrs, defStyle)
		updatePaint()
	}

	private fun initAttributes(attrs: AttributeSet?, defStyle: Int) {
		val array = context.obtainStyledAttributes(attrs, R.styleable.LoadingView, defStyle, 0)

		currentProgress = array.getFloat(
			R.styleable.LoadingView_loading_progress,
			resources.getInteger(R.integer.loadingview_default_progress).toFloat()
		)
		maxProgress = array.getFloat(
			R.styleable.LoadingView_loading_maxProgress,
			resources.getInteger(R.integer.loadingview_default_max_progress).toFloat()
		)
		thickness = array.getDimensionPixelSize(
			R.styleable.LoadingView_loading_thickness,
			resources.getDimensionPixelSize(R.dimen.loadingview_default_thickness)
		)
		isIndeterminate = array.getBoolean(
			R.styleable.LoadingView_loading_indeterminate,
			resources.getBoolean(R.bool.loadingview_default_is_indeterminate)
		)
		autostartAnimation = array.getBoolean(
			R.styleable.LoadingView_loading_animAutostart,
			resources.getBoolean(R.bool.loadingview_default_anim_autostart)
		)
		initialStartAngle = array.getFloat(
			R.styleable.LoadingView_loading_startAngle,
			resources.getInteger(R.integer.loadingview_default_start_angle).toFloat()
		)
		startAngle = initialStartAngle

		val secondaryColor = resources.getIdentifier("colorSecondary", "attr", context.packageName)

		when {
			// If color explicitly provided
			array.hasValue(R.styleable.LoadingView_loading_color) -> {
				color = array.getColor(
					R.styleable.LoadingView_loading_color,
					resources.getColor(R.color.loadingview_default_color, null)
				)
			}
			// If using Theme.MaterialComponents
			secondaryColor != 0 -> {
				val typedValue = TypedValue()
				context.theme.resolveAttribute(secondaryColor, typedValue, true)
				color = typedValue.data
			}
			// Use default color
			else -> {
				val colors = context.obtainStyledAttributes(intArrayOf(android.R.attr.colorAccent))
				color = colors.getColor(0, resources.getColor(R.color.loadingview_default_color, null))
				colors.recycle()
			}
		}

		animDuration = array.getInteger(
			R.styleable.LoadingView_loading_animDuration,
			resources.getInteger(R.integer.loadingview_default_anim_duration)
		)
		animSwoopDuration = array.getInteger(
			R.styleable.LoadingView_loading_animSwoopDuration,
			resources.getInteger(R.integer.loadingview_default_anim_swoop_duration)
		)
		animSyncDuration = array.getInteger(
			R.styleable.LoadingView_loading_animSyncDuration,
			resources.getInteger(R.integer.loadingview_default_anim_sync_duration)
		)
		animSteps = array.getInteger(
			R.styleable.LoadingView_loading_animSteps,
			resources.getInteger(R.integer.loadingview_default_anim_steps)
		)
		array.recycle()
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		val xPad = paddingLeft + paddingRight
		val yPad = paddingTop + paddingBottom
		val width = measuredWidth - xPad
		val height = measuredHeight - yPad
		size = if (width < height) width else height
		setMeasuredDimension(size + xPad, size + yPad)
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		size = if (w < h) w else h
		updateBounds()
	}

	private fun updateBounds() {
		val thickness = this.thickness.toFloat()
		bounds.set(
			paddingLeft + thickness,
			paddingTop + thickness,
			size - paddingLeft - thickness,
			size - paddingTop - thickness
		)
	}

	private fun updatePaint() {
		paint.apply {
			color = this@LoadingView.color
			style = Paint.Style.STROKE
			strokeWidth = thickness.toFloat()
			strokeCap = Paint.Cap.BUTT
		}
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		val sweepAngle = if (isInEditMode) currentProgress / maxProgress * 360 else actualProgress / maxProgress * 360
		if (!isIndeterminate) {
			canvas.drawArc(bounds, startAngle, sweepAngle, false, paint)
		} else {
			canvas.drawArc(bounds, startAngle + indeterminateRotateOffset, indeterminateSweep, false, paint)
		}
	}

	/**
	 * Sets whether this CircularProgressView is indeterminate or not.
	 * It will reset the animation if the mode has changed.
	 * @param isIndeterminate True if indeterminate.
	 */
	fun setIndeterminate(value: Boolean) {
		val old = this.isIndeterminate
		val reset = this.isIndeterminate != value
		this.isIndeterminate = value
		if (reset) {
			resetAnimation()
		}
		if (old != value) {
			for (listener in listeners) {
				listener.onModeChanged(this.isIndeterminate)
			}
		}
	}

	/**
	 * Sets the thickness of the progress bar arc.
	 * @param thickness the thickness of the progress bar arc
	 */
	fun setThickness(thickness: Int) {
		this.thickness = thickness
		updatePaint()
		updateBounds()
		invalidate()
	}

	/**
	 * Sets the color of the progress bar.
	 * @param color the color of the progress bar
	 */
	fun setColor(color: Int) {
		this.color = color
		updatePaint()
		invalidate()
	}

	/**
	 * Sets the progress value considered to be 100% of the progress bar.
	 * @param maxProgress the maximum progress
	 */
	fun setMaxProgress(maxProgress: Float) {
		this.maxProgress = maxProgress
		invalidate()
	}

	/**
	 * Sets the progress of the progress bar.
	 *
	 * @param currentProgress the new progress.
	 */
	fun setProgress(currentProgress: Float) {
		this.currentProgress = currentProgress
		// Reset the determinate animation to approach the new currentProgress
		if (!isIndeterminate) {
			progressAnimator?.cancel()
			progressAnimator = ValueAnimator.ofFloat(actualProgress, currentProgress)
			progressAnimator?.let {
				it.duration = animSyncDuration.toLong()
				it.interpolator = LinearInterpolator()
				it.addUpdateListener { animation ->
					actualProgress = animation.animatedValue as Float
					invalidate()
				}
				it.addListener(object : AnimatorListenerAdapter() {
					override fun onAnimationEnd(animation: Animator) {
						for (listener in listeners) {
							listener.onProgressUpdateEnd(currentProgress)
						}
					}
				})
				it.start()
			}
		}
		invalidate()
		for (listener in listeners) {
			listener.onProgressUpdate(currentProgress)
		}
	}

	/**
	 * Register a [LoadingViewListener] with this View
	 * @param listener The listener to register
	 */
	fun addListener(listener: LoadingViewListener) {
		listeners.add(listener)
	}

	/**
	 * Unregister a [LoadingViewListener] with this View
	 * @param listener The listener to unregister
	 */
	fun removeListener(listener: LoadingViewListener) {
		listeners.remove(listener)
	}

	/**
	 * Starts the progress bar animation.
	 * *This is an alias of [resetAnimation] so it does the same thing*
	 */
	private fun startAnimation() {
		resetAnimation()
	}

	/**
	 * Resets the animation.
	 */
	private fun resetAnimation() {
		// Cancel all the old animators
		startAngleRotate?.cancel()
		progressAnimator?.cancel()
		indeterminateAnimator?.cancel()

		if (!isIndeterminate) {
			// The cool 360 swoop animation at the start of the animation
			startAngle = initialStartAngle
			startAngleRotate = ValueAnimator.ofFloat(startAngle, startAngle + 360)
			startAngleRotate?.let {
				it.duration = animSwoopDuration.toLong()
				it.interpolator = DecelerateInterpolator(2f)
				it.addUpdateListener { animation ->
					startAngle = animation.animatedValue as Float
					invalidate()
				}
				it.start()
			}

			// The linear animation shown when progress is updated
			actualProgress = 0f
			progressAnimator = ValueAnimator.ofFloat(actualProgress, currentProgress)
			progressAnimator?.let {
				it.duration = animSyncDuration.toLong()
				it.interpolator = LinearInterpolator()
				it.addUpdateListener { animation ->
					actualProgress = animation.animatedValue as Float
					invalidate()
				}
				it.start()
			}
		} else {
			indeterminateSweep = INDETERMINANT_MIN_SWEEP
			// Build the whole AnimatorSet
			indeterminateAnimator = AnimatorSet()
			var prevSet: AnimatorSet? = null
			var nextSet: AnimatorSet
			for (step in 0 until animSteps) {
				nextSet = createIndeterminateAnimator(step.toFloat())
				val builder = indeterminateAnimator!!.play(nextSet)
				if (prevSet != null)
					builder.after(prevSet)
				prevSet = nextSet
			}

			// Listen to end of animation so we can infinitely loop
			indeterminateAnimator!!.addListener(object : AnimatorListenerAdapter() {

				var wasCancelled = false

				override fun onAnimationCancel(animation: Animator) {
					wasCancelled = true
				}

				override fun onAnimationEnd(animation: Animator) {
					if (!wasCancelled)
						resetAnimation()
				}
			})
			indeterminateAnimator!!.start()
			for (listener in listeners) {
				listener.onAnimationReset()
			}
		}// Indeterminate animation
	}

	/**
	 * Stops the animation
	 */
	private fun stopAnimation() {
		startAngleRotate?.cancel()
		progressAnimator?.cancel()
		indeterminateAnimator?.cancel()
		startAngleRotate = null
		progressAnimator = null
		indeterminateAnimator = null
	}

	/**
	 * Creates the animators for one step of the animation
	 */
	private fun createIndeterminateAnimator(step: Float): AnimatorSet {
		val maxSweep = 360f * (animSteps - 1) / animSteps + INDETERMINANT_MIN_SWEEP
		val start = -90f + step * (maxSweep - INDETERMINANT_MIN_SWEEP)

		// Extending the front of the arc
		val frontEndExtend = ValueAnimator.ofFloat(INDETERMINANT_MIN_SWEEP, maxSweep)
		frontEndExtend.duration = (animDuration / animSteps / 2).toLong()
		frontEndExtend.interpolator = DecelerateInterpolator(1f)
		frontEndExtend.addUpdateListener { animation ->
			indeterminateSweep = animation.animatedValue as Float
			invalidate()
		}

		// Overall rotation
		val rotateAnimator1 = ValueAnimator.ofFloat(step * 720f / animSteps, (step + .5f) * 720f / animSteps)
		rotateAnimator1.duration = (animDuration / animSteps / 2).toLong()
		rotateAnimator1.interpolator = LinearInterpolator()
		rotateAnimator1.addUpdateListener { animation -> indeterminateRotateOffset = animation.animatedValue as Float }

		// Retracting the back end of the arc
		val backEndRetract = ValueAnimator.ofFloat(start, start + maxSweep - INDETERMINANT_MIN_SWEEP)
		backEndRetract.duration = (animDuration / animSteps / 2).toLong()
		backEndRetract.interpolator = DecelerateInterpolator(1f)
		backEndRetract.addUpdateListener { animation ->
			startAngle = animation.animatedValue as Float
			indeterminateSweep = maxSweep - startAngle + start
			invalidate()
		}

		// More overall rotation
		val rotateAnimator2 = ValueAnimator.ofFloat((step + .5f) * 720f / animSteps, (step + 1) * 720f / animSteps)
		rotateAnimator2.duration = (animDuration / animSteps / 2).toLong()
		rotateAnimator2.interpolator = LinearInterpolator()
		rotateAnimator2.addUpdateListener { animation -> indeterminateRotateOffset = animation.animatedValue as Float }

		return AnimatorSet().apply {
			play(frontEndExtend).with(rotateAnimator1)
			play(backEndRetract).with(rotateAnimator2).after(rotateAnimator1)
		}
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		if (autostartAnimation) {
			startAnimation()
		}
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		stopAnimation()
	}

	override fun setVisibility(visibility: Int) {
		val currentVisibility = getVisibility()
		super.setVisibility(visibility)
		if (visibility != currentVisibility) {
			if (visibility == View.VISIBLE) {
				resetAnimation()
			} else {
				stopAnimation()
			}
		}
	}
}