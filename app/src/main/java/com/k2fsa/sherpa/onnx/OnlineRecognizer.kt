package com.k2fsa.sherpa.onnx

import android.content.res.AssetManager
import android.util.Log
import com.k2fsa.sherpa.onnx.OnlineStream

class OnlineRecognizer(
    assetManager: AssetManager? = null,
    val config: OnlineRecognizerConfig,
) {
    companion object {
        val TAG = this::class.java.declaringClass!!.simpleName
        init {
            Log.d(TAG, "Loading libraries")
            System.loadLibrary("sherpa-onnx-jni")
        }
    }


    private var ptr: Long

    init {
        ptr = if (assetManager != null) {
            newFromAsset(assetManager, config)
        } else {
            newFromFile(config)
        }
    }

    protected fun finalize() {
        if (ptr != 0L) {
            delete(ptr)
            ptr = 0
        }
    }

    fun release() = finalize()

    fun createStream(hotwords: String = ""): OnlineStream {
        val p = createStream(ptr, hotwords)
        return OnlineStream(p)
    }

    fun reset(stream: OnlineStream) = reset(ptr, stream.ptr)
    fun decode(stream: OnlineStream) = decode(ptr, stream.ptr)
    fun isEndpoint(stream: OnlineStream) = isEndpoint(ptr, stream.ptr)
    fun isReady(stream: OnlineStream) = isReady(ptr, stream.ptr)
    fun getResult(stream: OnlineStream): OnlineRecognizerResult {
        val objArray = getResult(ptr, stream.ptr)

        val text = objArray[0] as String
        val tokens = objArray[1] as Array<String>
        val timestamps = objArray[2] as FloatArray
        val ysProbs = objArray[3] as FloatArray

        return OnlineRecognizerResult(
            text = text,
            tokens = tokens,
            timestamps = timestamps,
            ysProbs = ysProbs
        )
    }

    private external fun delete(ptr: Long)

    private external fun newFromAsset(
        assetManager: AssetManager,
        config: OnlineRecognizerConfig,
    ): Long

    private external fun newFromFile(
        config: OnlineRecognizerConfig,
    ): Long

    private external fun createStream(ptr: Long, hotwords: String): Long
    private external fun reset(ptr: Long, streamPtr: Long)
    private external fun decode(ptr: Long, streamPtr: Long)
    private external fun isEndpoint(ptr: Long, streamPtr: Long): Boolean
    private external fun isReady(ptr: Long, streamPtr: Long): Boolean
    private external fun getResult(ptr: Long, streamPtr: Long): Array<Any>

}