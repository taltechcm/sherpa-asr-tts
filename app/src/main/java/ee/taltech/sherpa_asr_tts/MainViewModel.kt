package ee.taltech.sherpa_asr_tts

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

class MainViewModel(
    private val asr: SharedAsr
) : ViewModel() {
    fun getAsrText(): Flow<String> {
        return asr.textFlow()
    }
}