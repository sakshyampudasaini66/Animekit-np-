package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.AnimeKitApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AnimeKitViewModel

class MainActivity : ComponentActivity() {
  
  private val animeKitViewModel: AnimeKitViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AnimeKitApp(viewModel = animeKitViewModel)
      }
    }
  }
}

