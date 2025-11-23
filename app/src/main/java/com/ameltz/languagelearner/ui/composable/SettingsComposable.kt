package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsPage(toHomePage: () -> Unit, settingsViewModel: SettingsViewModel) {

    var mediumTimeDelay by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(settingsViewModel.getMediumTimeDelay().toString()))
    }

    var hardTimeDelay by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(settingsViewModel.getHardTimeDelay().toString()))
    }

    LanguageLearnerTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = mediumTimeDelay,
                onValueChange = { mediumTimeDelay = it },
                label = { Text("Medium time delay") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = hardTimeDelay,
                onValueChange = { hardTimeDelay = it },
                label = { Text("Hard time delay") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val mediumValue = mediumTimeDelay.text.toIntOrNull()
                    if (mediumValue != null) {
                        settingsViewModel.saveMediumTimeDelay(mediumValue)
                    }
                    val hardValue = hardTimeDelay.text.toIntOrNull()
                    if (hardValue != null) {
                        settingsViewModel.saveHardTimeDelay(hardValue)
                    }
                    toHomePage()
                },
            ) {
                Text("Save Settings")
            }
        }
    }
}
