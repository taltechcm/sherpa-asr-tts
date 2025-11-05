package ee.taltech.sherpa_asr_tts

import android.content.res.AssetManager

object WaveReader {
    fun readWave(
        assetManager: AssetManager,
        filename: String,
    ): WaveData {
        return readWaveFromAsset(assetManager, filename).let {
            WaveData(it[0] as FloatArray, it[1] as Int)
        }
    }

    fun readWave(
        filename: String,
    ): WaveData {
        return readWaveFromFile(filename).let {
            WaveData(it[0] as FloatArray, it[1] as Int)
        }
    }

    // Read a mono wave file asset
    // The returned array has two entries:
    //  - the first entry contains an 1-D float array
    //  - the second entry is the sample rate
    external fun readWaveFromAsset(
        assetManager: AssetManager,
        filename: String,
    ): Array<Any>

    // Read a mono wave file from disk
    // The returned array has two entries:
    //  - the first entry contains an 1-D float array
    //  - the second entry is the sample rate
    external fun readWaveFromFile(
        filename: String,
    ): Array<Any>

    init {
        System.loadLibrary("sherpa-onnx-jni")
    }
}