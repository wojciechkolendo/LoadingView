package com.wojciechkolendo.loadingview

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.util.*

/**
 * @author Wojciech Kolendo
 */
class LoadingView : View {

	private val INDETERMINANT_MIN_SWEEP = 15f

	private lateinit var paint: Paint
	private lateinit var bounds: RectF
	private var size = 0

	var isIndeterminate: Boolean = false
		set(value) {
			val old = field
			val reset = field != value
			field = value
			if (reset) {
				resetAnimation()
			}
			if (old != value) {
				for (listener in listeners) {
					listener.onModeChanged(field)
				}
			}
		}


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

	private lateinit var listeners: MutableList<LoadingViewListener>

	// Animation related stuff
	private var startAngle: Float = 0f
	private var actualProgress: Float = 0f
	private var startAngleRotate: ValueAnimator? = null
	private var progressAnimator: ValueAnimator? = null
	private var indeterminateAnimator: AnimatorSet? = null
	private var initialStartAngle: Float = 0f

	constructor(context: Context, thickness: Int) : super(context) {
		this.thickness = thickness
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

	private fun init(attrs: AttributeSet, defStyle: Int) {
		listeners = ArrayList<LoadingViewListener>()

		initAttributes(attrs, defStyle)

		paint = Paint(Paint.ANTI_ALIAS_FLAG)
		updatePaint()

		bounds = RectF()
	}

	private fun initAttributes(attrs: AttributeSet, defStyle: Int) {
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
				color = resources.getColor(R.color.loadingview_default_color, null)
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
			this.color = color
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



	fun addListener(listener: LoadingViewListener) {
		listeners.add(listener)
	}

	fun removeListener(listener: LoadingViewListener) {
		listeners.remove(listener)
	}

	private fun startAnimation() {
		resetAnimation()
	}

	private fun resetAnimation() {

	}

	private fun stopAnimation() {

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