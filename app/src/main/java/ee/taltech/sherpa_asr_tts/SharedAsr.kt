package ee.taltech.sherpa_asr_tts

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.k2fsa.sherpa.onnx.EndpointConfig
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

@SuppressLint("MissingPermission")
class SharedAsr(
    val context: Context,
    externalScope: CoroutineScope
) {
    companion object {
        val TAG = this::class.java.declaringClass!!.simpleName
    }

    private val sampleRateInHz = 16000
    private var audioRecord: AudioRecord? = null
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO

    // Note: We don't use AudioFormat.ENCODING_PCM_FLOAT
    // since the AudioRecord.read(float[]) needs API level >= 23
    // but we are targeting API level >= 21
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private lateinit var recognizer: OnlineRecognizer

    init {
        initModel()
    }

    private val _textUpdates = callbackFlow<String>
    {
        val micOk = initMicrophone()
        if (!micOk) {
            Log.e(TAG, "Failed to initialize microphone")
            return@callbackFlow
        }
        Log.i(TAG, "state: ${audioRecord?.state}")
        audioRecord!!.startRecording()

        val stream = recognizer.createStream()
        val interval = 0.1 // i.e., 100 ms
        val bufferSize = (interval * sampleRateInHz).toInt() // in samples
        val buffer = ShortArray(bufferSize)

        var prevText: String = ""

        // what coroutine scope to use here?
        while (true) {
            val ret = audioRecord?.read(buffer, 0, buffer.size)
            if (ret != null && ret > 0) {
                val samples = FloatArray(ret) { buffer[it] / 32768.0f }
                stream.acceptWaveform(samples, sampleRate = sampleRateInHz)
                while (recognizer.isReady(stream)) {
                    recognizer.decode(stream)
                }
                val isEndpoint = recognizer.isEndpoint(stream)
                var text = recognizer.getResult(stream).text.trim()

                // For streaming performer, we need to manually add some
                // paddings so that it has enough right context to
                // recognize the last word of this segment
                if (isEndpoint && recognizer.config.modelConfig.paraformer.encoder.isNotBlank()) {
                    val tailPaddings = FloatArray((0.8 * sampleRateInHz).toInt())
                    stream.acceptWaveform(tailPaddings, sampleRate = sampleRateInHz)
                    while (recognizer.isReady(stream)) {
                        recognizer.decode(stream)
                    }
                    text = recognizer.getResult(stream).text.trim()
                }

                if (text.isNotBlank() && text != prevText) {
                    Log.e(TAG, "text: $text")
                    prevText = text
                    trySend(text)
                }

                // end of speech detected, send the final sentence
                if (isEndpoint) {
                    recognizer.reset(stream)
                    if (prevText.isNotBlank()) {
                        Log.e(TAG, "final: $text")
                        trySend("final: $text")
                    }
                    prevText = ""
                }
            }
        }


        // runs when flow has no more subscribers
        awaitClose {
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
        }
    }.shareIn(
        externalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun textFlow(): Flow<String> {
        return _textUpdates
    }

    private fun initModel() {
        var config = OnlineRecognizerConfig(
            featConfig = FeatureConfig.getFeatureConfig(
                sampleRate = sampleRateInHz,
                featureDim = 80
            ),
            modelConfig = OnlineModelConfig.getModelConfig(0)!!,
            // lmConfig = getOnlineLMConfig(type = type),
            endpointConfig = EndpointConfig.getEndpointConfig(),
            enableEndpoint = true,
            decodingMethod = "modified_beam_search",
        )

        recognizer = OnlineRecognizer(
            assetManager = this.context.assets,
            config = config,
        )
    }


    @SuppressLint("MissingPermission")
    private fun initMicrophone(): Boolean {
        val numBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)

        audioRecord = AudioRecord(
            audioSource,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            numBytes * 2 // a sample has two bytes as we are using 16-bit PCM
        )
        return true
    }
}