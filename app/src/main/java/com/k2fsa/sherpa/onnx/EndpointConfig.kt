package com.k2fsa.sherpa.onnx

data class EndpointConfig(
    var rule1: EndpointRule = EndpointRule(false, 2.4f, 0.0f),
    var rule2: EndpointRule = EndpointRule(true, 1.4f, 0.0f),
    var rule3: EndpointRule = EndpointRule(false, 0.0f, 20.0f)
) {
    companion object {
        fun getEndpointConfig(): EndpointConfig {
            return EndpointConfig(
                rule1 = EndpointRule(false, 2.4f, 0.0f),
                rule2 = EndpointRule(true, 1.4f, 0.0f),
                rule3 = EndpointRule(false, 0.0f, 20.0f)
            )
        }
    }
}

