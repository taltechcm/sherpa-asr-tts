package com.k2fsa.sherpa.onnx

data class OnlineCtcFstDecoderConfig(
    var graph: String = "",
    var maxActive: Int = 3000,
)