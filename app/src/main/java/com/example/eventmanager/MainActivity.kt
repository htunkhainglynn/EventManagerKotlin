package com.example.eventmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventmanager.ui.EventManagerApp
import com.example.eventmanager.ui.EventManagerViewModel
import com.example.eventmanager.ui.EventManagerViewModelFactory
import com.example.eventmanager.ui.theme.EventManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val application = application as EventManagerApplication
            val viewModel: EventManagerViewModel = viewModel(
                factory = EventManagerViewModelFactory(application, application.repository)
            )
            EventManagerTheme {
                EventManagerApp(viewModel)
            }
        }
    }
}
