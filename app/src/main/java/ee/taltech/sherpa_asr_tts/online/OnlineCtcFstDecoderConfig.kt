package ee.taltech.sherpa_asr_tts.online

data class OnlineCtcFstDecoderConfig(
    var graph: String = "",
    var maxActive: Int = 3000,
)