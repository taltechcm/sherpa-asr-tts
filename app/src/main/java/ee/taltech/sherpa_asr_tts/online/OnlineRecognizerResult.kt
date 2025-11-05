package ee.taltech.sherpa_asr_tts.online

data class OnlineRecognizerResult(
    val text: String,
    val tokens: Array<String>,
    val timestamps: FloatArray,
    val ysProbs: FloatArray,
    // TODO(fangjun): Add more fields
)