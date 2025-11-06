package com.k2fsa.sherpa.onnx

data class OnlineLMConfig(
    var model: String = "",
    var scale: Float = 0.5f,
) {
    companion object {
        fun getOnlineLMConfig(type: Int): OnlineLMConfig {
            when (type) {
                0 -> {
                    val modelDir = "sherpa-onnx-streaming-zipformer-bilingual-zh-en-2023-02-20"
                    return OnlineLMConfig(
                        model = "$modelDir/with-state-epoch-99-avg-1.int8.onnx",
                        scale = 0.5f,
                    )
                }
            }
            return OnlineLMConfig()
        }
    }
}
