package com.wojciechkolendo.loadingview

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes

/**
 * @author Wojciech Kolendo
 */
class LoadingDialog : Dialog {

	constructor(context: Context, @StyleRes theme: Int) : this(context, theme, false, null)

	constructor(
		context: Context, @StyleRes theme: Int,
		cancelable: Boolean,
		cancelListener: DialogInterface.OnCancelListener?
	) : super(context, theme) {
		setCancelable(cancelable)
		setCanceledOnTouchOutside(cancelable)
		setOnCancelListener(cancelListener)
		init()
	}

	private fun init() {
		super.setContentView(R.layout.dialog_loading)
	}

	fun setBackgroundAlpha(alpha: Float) {
		val backgroundView = findViewById<View>(R.id.background)
		backgroundView.alpha = alpha
	}

	/**
	 * For custom layout purpose
	 */
	constructor(context: Context, @StyleRes theme: Int, @LayoutRes layoutRes: Int) : super(context, theme) {
		setCancelable(false)
		setCanceledOnTouchOutside(false)

		super.setContentView(layoutRes)
		val backgroundView = findViewById<View>(R.id.background)

		backgroundView.alpha = 0.9f
	}
}