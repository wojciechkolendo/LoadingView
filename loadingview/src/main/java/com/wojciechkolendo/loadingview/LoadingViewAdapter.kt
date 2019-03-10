package com.wojciechkolendo.loadingview

/**
 * @author Wojciech Kolendo
 *
 * This class implements [LoadingViewListener]. Use this if you don't want to implement all methods for the listener.
 */
class LoadingViewAdapter : LoadingViewListener {

    override fun onProgressUpdateEnd(currentProgress: Float) {}

    override fun onAnimationReset() {}

    override fun onModeChanged(isIndeterminate: Boolean) {}

    override fun onProgressUpdate(currentProgress: Float) {}

}