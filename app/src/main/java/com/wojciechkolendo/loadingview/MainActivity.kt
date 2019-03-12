package com.wojciechkolendo.loadingview

import android.os.Bundle
import android.view.View
import androidx.annotation.ContentView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

@ContentView(R.layout.activity_main)
class MainActivity : AppCompatActivity() {

	private var show = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		button.setOnClickListener { toggleLoading() }
		dialogButton.setOnClickListener { LoadingDialog(this, R.style.AppTheme_ProgressDialog).show() }
	}

	private fun toggleLoading() {
		if (show) {
			loadingView1.visibility = View.INVISIBLE
			loadingView2.visibility = View.INVISIBLE
			loadingView3.visibility = View.INVISIBLE
			loadingView4.visibility = View.INVISIBLE
			loadingView5.visibility = View.INVISIBLE
			loadingView6.visibility = View.INVISIBLE
		} else {
			loadingView1.visibility = View.VISIBLE
			loadingView2.visibility = View.VISIBLE
			loadingView3.visibility = View.VISIBLE
			loadingView4.visibility = View.VISIBLE
			loadingView5.visibility = View.VISIBLE
			loadingView6.visibility = View.VISIBLE
		}
		show = !show
	}
}
