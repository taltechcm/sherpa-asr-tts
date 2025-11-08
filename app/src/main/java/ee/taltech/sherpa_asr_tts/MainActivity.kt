package ee.taltech.sherpa_asr_tts

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.k2fsa.sherpa.onnx.OfflineTts
import ee.taltech.sherpa_asr_tts.ui.theme.SherpaasrttsTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

class MainActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var track: AudioTrack
    private lateinit var tts: OfflineTts

    companion object {
        val TAG = this::class.java.declaringClass!!.simpleName
    }

    private val vm by viewModels<MainViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(
                        SharedAsr(
                            applicationContext,
                            GlobalScope
                        )
                    ) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SherpaasrttsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionsCheck(
                        vm = vm,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsCheck(vm: MainViewModel, modifier: Modifier = Modifier) {
    val permissionStates = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.RECORD_AUDIO,
        )
    )
    if (permissionStates.allPermissionsGranted) {
        // launch the service, has 5 sec to turn into fg service
        //val context = LocalContext.current
        //val serviceIntent = Intent(context.applicationContext, LocationService::class.java)
        //context.startForegroundService(serviceIntent)

        MainView(vm = vm, modifier = modifier)
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Please grant all permissions!")
            Button(onClick = {
                permissionStates.launchMultiplePermissionRequest()
            }) {
                Text(text = "Grant permissions")
            }
        }
    }

}

@SuppressLint("MissingPermission")
@Composable
fun MainView(vm: MainViewModel, modifier: Modifier = Modifier) {
    val text = vm.getAsrText()
        .filter { !it.startsWith("final:") }
        .collectAsState(initial = "-")

    val finalText = vm.getAsrText()
        .filter { it.startsWith("final: ") }
        .map { it.removePrefix("final: ") }
        .collectAsState(initial = "-")

    val _fullLog = remember {
        vm.getAsrText()
            .filter { it.startsWith("final: ") }
            .map { it.removePrefix("final: ") }
            .scan("") { accumulator, value ->
                Log.d("value", value)
                val string = if (accumulator.isEmpty()) {
                    value
                } else {
                    "$accumulator\n$value"
                }
                string
            }
    }

    val fullLog by _fullLog
        .collectAsStateWithLifecycle("-")

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .padding(Dp(16f))
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Disable ripple effect
                onClick = { focusManager.clearFocus() }
            ),
        horizontalAlignment = Alignment.Start
    )
    {
        Text("ASR Stream")
        Box(
            modifier = Modifier
                .height(Dp(128f))
                .fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ) {
            Text(text.value)
        }

        Text("Final sentence")
        Text(text = finalText.value)

        // A scrollable, static text area
        val scrollState = rememberScrollState()

        LaunchedEffect(fullLog) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        Text("Full transcription", modifier = Modifier.padding(top = Dp(16f)))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dp(200f))
                //.weight(1f) // This makes the Box fill the remaining space
                .verticalScroll(scrollState)
        ) {
            Text(text = fullLog) // You can bind this to any state you want to display
        }


        ttsView(modifier = modifier)

    }

}

@Composable
fun ttsView(modifier: Modifier = Modifier) {
    var ttsText by remember {
        mutableStateOf(
            "Üle oja mäele, läbi oru jõele.\n" +
                    "Kalli ema hella soojust jagub üle mitme põlve.\n" +
                    "Kuula, kulla külanaine - kuuled külla tulnud vaime.\n" +
                    "Küll küllale liiga ei tee.\n" +
                    "Mahlakas jõhvikas maitses soisel kaldal hää.\n" +
                    "Ämber läks ümber.\n" +
                    "Millal maksan memme vaeva.\n" +
                    "Tilluke talleke tatsas tasasel pinnal.\n" +
                    "Ema tuli koju.\n" +
                    "Pöidlad pihku, pöialpoisid!"
        )
    }
    val context = LocalContext.current
    val sharedTts by remember { mutableStateOf(SharedTts(context, GlobalScope)) }

    TextField(
        value = ttsText,
        onValueChange = { ttsText = it },
        label = { Text("TTS text") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dp(16f))
            .height(Dp(200f))
    )

    Row() {
        Button({
            sharedTts.generateAndPlay(ttsText.replace("\n", " "), 0, 1f)
        }, modifier = Modifier.padding(top = Dp(8f))) {
            Text("Play")
        }
        Button({
            ttsText = ""
        }, modifier = Modifier.padding(top = Dp(8f))) {
            Text("Clear")
        }

    }

}