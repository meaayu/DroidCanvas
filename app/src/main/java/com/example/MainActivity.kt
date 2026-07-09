package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.data.RefCanvasDatabase
import com.example.data.RefCanvasRepository
import com.example.ui.RefCanvasScreen
import com.example.ui.RefCanvasViewModel
import com.example.ui.RefCanvasViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private lateinit var viewModel: RefCanvasViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure Coil to support animated GIFs
        val imageLoader = ImageLoader.Builder(applicationContext)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
        Coil.setImageLoader(imageLoader)

        // 1. Initialize local Room database & Repository
        val database = RefCanvasDatabase.getDatabase(applicationContext)
        val repository = RefCanvasRepository(database.refCanvasDao())

        // 2. Instantiate RefCanvas ViewModel
        viewModel = ViewModelProvider(
            this, 
            RefCanvasViewModelFactory(repository, applicationContext)
        )[RefCanvasViewModel::class.java]

        // 3. Handle initial launch intents (e.g. shared images from other apps)
        handleShareIntent(intent)

        setContent {
            val themeMode = viewModel.themeMode
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MyApplicationTheme(
                darkTheme = darkTheme,
                dynamicColor = viewModel.isDynamicColorEnabled,
                pitchBlack = viewModel.isPitchBlackEnabled
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RefCanvasScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle incoming intents when activity is already warm in backstack
        handleShareIntent(intent)
    }

    /**
     * Extracts shared images from other applications and imports them onto the active board.
     */
    private fun handleShareIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type

        try {
            if (type?.startsWith("image/") == true) {
                if (Intent.ACTION_SEND == action) {
                    // Extract single image uri
                    val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    if (imageUri != null) {
                        Log.d(TAG, "Received single shared image: $imageUri")
                        viewModel.addImages(applicationContext, listOf(imageUri))
                    }
                } else if (Intent.ACTION_SEND_MULTIPLE == action) {
                    // Extract multiple image uris
                    val imageUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                    if (!imageUris.isNullOrEmpty()) {
                        val validUris = imageUris.filterIsInstance<Uri>()
                        Log.d(TAG, "Received ${validUris.size} shared images")
                        viewModel.addImages(applicationContext, validUris)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing incoming shared visual media", e)
        }
    }
}
