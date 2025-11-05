package ee.taltech.sherpa_asr_tts

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import ee.taltech.sherpa_asr_tts.ui.theme.SherpaasrttsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SherpaasrttsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionsCheck(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsCheck(modifier: Modifier = Modifier) {
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

        MainView(modifier)
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
fun MainView(modifier: Modifier = Modifier) {
    Text("Foobar", modifier = modifier)
}

