package com.k2fsa.sherpa.onnx

data class OnlineModelConfig(
    var transducer: OnlineTransducerModelConfig = OnlineTransducerModelConfig(),
    var paraformer: OnlineParaformerModelConfig = OnlineParaformerModelConfig(),
    var zipformer2Ctc: OnlineZipformer2CtcModelConfig = OnlineZipformer2CtcModelConfig(),
    var neMoCtc: OnlineNeMoCtcModelConfig = OnlineNeMoCtcModelConfig(),
    var toneCtc: OnlineToneCtcModelConfig = OnlineToneCtcModelConfig(),
    var tokens: String = "",
    var numThreads: Int = 1,
    var debug: Boolean = false,
    var provider: String = "cpu",
    var modelType: String = "",
    var modelingUnit: String = "",
    var bpeVocab: String = "",
) {
    companion object {
        fun getModelConfig(type: Int): OnlineModelConfig? {
            when (type) {
                0 -> {
                    val modelDir = "streaming-zipformer-large.et-en"
                    return OnlineModelConfig(
                        transducer = OnlineTransducerModelConfig(
                            encoder = "$modelDir/encoder.onnx",
                            decoder = "$modelDir/decoder.onnx",
                            joiner = "$modelDir/joiner.onnx",
                        ),
                        tokens = "$modelDir/tokens.txt",
                        modelType = "zipformer2",
                        //provider = "rknn",
                    )
                }

            }
            return null
        }
    }
}