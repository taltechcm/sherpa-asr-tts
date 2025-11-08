package ee.taltech.sherpa_asr_tts

import android.content.Context
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import kotlinx.coroutines.CoroutineScope

class SharedTts(
    val context: Context,
    externalScope: CoroutineScope
) {
    companion object {
        val TAG = this::class.java.declaringClass!!.simpleName
    }

    private var stopped: Boolean = false

    private lateinit var tts: OfflineTts
    private lateinit var track: AudioTrack

    init {
        initTts()
        initAudioTrack()
    }

    private fun initTts() {
        var assets: AssetManager? = context.assets

        // The purpose of such a design is to make the CI test easier
        // Please see
        // https://github.com/k2-fsa/sherpa-onnx/blob/master/scripts/apk/generate-tts-apk-script.py

        // VITS -- begin
        val modelName = "model.onnx"
        // VITS -- end

        // Matcha -- begin
        var acousticModelName = null
        var vocoder = null
        // Matcha -- end

        // For Kokoro -- begin
        var voices = null
        // For Kokoro -- end


        val modelDir = "vits-coqui-et-cv"

        var ruleFsts = null
        var ruleFars = null
        var lexicon = null
        var dataDir = null


        val config = OfflineTtsConfig.getOfflineTtsConfig(
            modelDir = modelDir,
            modelName = modelName ?: "",
            acousticModelName = acousticModelName ?: "",
            vocoder = vocoder ?: "",
            voices = voices ?: "",
            lexicon = lexicon ?: "",
            dataDir = dataDir ?: "",
            dictDir = "",
            ruleFsts = ruleFsts ?: "",
            ruleFars = ruleFars ?: "",
            isKitten = false,
        )

        tts = OfflineTts(assetManager = assets, config = config)
    }

    private fun initAudioTrack() {
        val sampleRate = tts.sampleRate()
        val bufLength = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        Log.i(TAG, "sampleRate: $sampleRate, buffLength: $bufLength")

        val attr = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setSampleRate(sampleRate)
            .build()

        track = AudioTrack(
            attr, format, bufLength, AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        track.play()
    }

    // this function is called from C++
    private fun callback(samples: FloatArray): Int {
        if (!stopped) {
            track.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
            return 1
        } else {
            track.stop()
            return 0
        }
    }

    fun generateAndPlay(text: String, speakerId: Int, speed: Float) {
        track.pause()
        track.flush()
        track.play()

        stopped = false
        Thread {
            val audio = tts.generateWithCallback(
                text = text.trim(),
                sid = speakerId,
                speed = speed,
                callback = this::callback
            )

            Log.d(
                TAG,
                "audio.samples.size: ${audio.samples.size} audio.sampleRate: ${audio.sampleRate}"
            )

            /*
            val filename = context.applicationContext.filesDir.absolutePath + "/generated.wav"
            val ok = audio.samples.isNotEmpty() && audio.save(filename)

            if (ok) {
                // emit status?
            }
            */

        }.start()
    }

}