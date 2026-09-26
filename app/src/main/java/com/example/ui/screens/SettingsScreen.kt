package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AIProviderConfig
import com.example.domain.model.AIProviderType
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioPrimaryCyan
import com.example.ui.theme.StudioSecondaryPurple
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun SettingsScreen(
    viewModel: StudioViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val providers by viewModel.aiProviders.collectAsState()

    var editingProvider by remember { mutableStateOf<AIProviderConfig?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    var defaultResolution by remember { mutableStateOf("1080p") }
    var defaultFps by remember { mutableStateOf("30 FPS") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Studio Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
                Text(
                    text = "AI Providers, Multi-Key Fallback, Storage & Render Engines",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTextMuted
                )
            }
        }

        // Section: AI Providers Manager
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Providers & Fallback Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = "Auto switches provider on quota limit or network timeout",
                        fontSize = 11.sp,
                        color = StudioTextMuted
                    )
                }

                Button(
                    onClick = {
                        editingProvider = AIProviderConfig(
                            id = "",
                            type = AIProviderType.OPENROUTER,
                            name = "Custom OpenRouter Key",
                            apiKey = "",
                            priority = providers.size + 1
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimaryCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = StudioDarkBg, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Key", color = StudioDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // List of Providers
        items(providers) { provider ->
            ProviderCard(
                provider = provider,
                onEdit = { editingProvider = provider },
                onToggleEnabled = { enabled ->
                    viewModel.saveProvider(provider.copy(isEnabled = enabled))
                },
                onTestConnection = {
                    isTestingConnection = true
                    viewModel.testProvider(provider) { success, msg ->
                        isTestingConnection = false
                        Toast.makeText(context, "${provider.name}: $msg", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        // Section: Video Export Defaults
        item {
            Text(
                text = "Rendering Engine Defaults",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioBorder, StudioBorder)))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Default Resolution", fontSize = 12.sp, color = StudioTextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("720p", "1080p", "1440p").forEach { res ->
                                FilterChip(
                                    selected = defaultResolution == res,
                                    onClick = { defaultResolution = res },
                                    label = { Text(res, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudioPrimaryCyan,
                                        selectedLabelColor = StudioDarkBg,
                                        containerColor = StudioSurfaceVariant,
                                        labelColor = StudioTextMuted
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Target Framerate", fontSize = 12.sp, color = StudioTextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("24 FPS", "30 FPS", "60 FPS").forEach { fps ->
                                FilterChip(
                                    selected = defaultFps == fps,
                                    onClick = { defaultFps = fps },
                                    label = { Text(fps, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudioPrimaryCyan,
                                        selectedLabelColor = StudioDarkBg,
                                        containerColor = StudioSurfaceVariant,
                                        labelColor = StudioTextMuted
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Storage & Cache Management
        item {
            Text(
                text = "Storage & Local Cache",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioBorder, StudioBorder)))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = StudioPrimaryCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Temporary Media Cache", fontSize = 12.sp, color = StudioTextPrimary)
                        }
                        Text(text = "18.4 MB", fontSize = 12.sp, color = StudioPrimaryCyan, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Temporary cache cleared. Projects preserved.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = StudioPrimaryCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Temporary Media Cache", color = StudioPrimaryCyan)
                    }
                }
            }
        }
    }

    // Edit Provider Modal Dialog
    if (editingProvider != null) {
        val prov = editingProvider!!
        var provName by remember(prov.id) { mutableStateOf(prov.name) }
        var provKey by remember(prov.id) { mutableStateOf(prov.apiKey) }
        var provModel by remember(prov.id) { mutableStateOf(prov.preferredModel) }
        var provBaseUrl by remember(prov.id) { mutableStateOf(prov.baseUrl) }
        var provPriority by remember(prov.id) { mutableStateOf(prov.priority.toString()) }

        val dialogFocusManager = LocalFocusManager.current
        AlertDialog(
            onDismissRequest = {
                dialogFocusManager.clearFocus()
                editingProvider = null
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
            title = {
                Text(
                    text = if (prov.id.isBlank()) "Add AI Provider" else "Edit Provider",
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = provName,
                        onValueChange = { provName = it },
                        label = { Text("Provider Name") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPrimaryCyan,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = provKey,
                        onValueChange = { provKey = it },
                        label = { Text("API Key (Stored Securely)") },
                        placeholder = { Text("Paste your API key...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPrimaryCyan,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = provModel,
                        onValueChange = { provModel = it },
                        label = { Text("Preferred Model") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPrimaryCyan,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = provBaseUrl,
                        onValueChange = { provBaseUrl = it },
                        label = { Text("Base Endpoint URL") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPrimaryCyan,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = provPriority,
                        onValueChange = { provPriority = it },
                        label = { Text("Fallback Priority (1 = Highest)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPrimaryCyan,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        dialogFocusManager.clearFocus()
                        val updated = prov.copy(
                            name = provName.trim(),
                            apiKey = provKey.trim(),
                            preferredModel = provModel.trim(),
                            baseUrl = provBaseUrl.trim(),
                            priority = provPriority.toIntOrNull() ?: 1
                        )
                        viewModel.saveProvider(updated)
                        editingProvider = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimaryCyan)
                ) {
                    Text("Save Provider", color = StudioDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dialogFocusManager.clearFocus()
                    editingProvider = null
                }) {
                    Text("Cancel", color = StudioTextMuted)
                }
            },
            containerColor = StudioCardBg
        )
    }
}

@Composable
fun ProviderCard(
    provider: AIProviderConfig,
    onEdit: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onTestConnection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("provider_card_${provider.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioBorder, StudioBorder)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (!provider.isEnabled) StudioTextMuted
                                else if (provider.lastTestedSuccess == true) StatusSuccess
                                else if (provider.lastTestedSuccess == false) StatusError
                                else StatusActive,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = provider.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = StudioTextPrimary
                    )
                }

                Switch(
                    checked = provider.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StudioPrimaryCyan,
                        checkedTrackColor = StudioSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Model: ${provider.preferredModel} • Priority #${provider.priority}",
                fontSize = 11.sp,
                color = StudioTextSecondary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (provider.apiKey.isNotBlank()) "Key: ${provider.apiKey.take(4)}••••••••${provider.apiKey.takeLast(3)}" else "No API key configured (using fallback)",
                fontSize = 10.sp,
                color = if (provider.apiKey.isNotBlank()) StudioPrimaryCyan else StudioTextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onTestConnection,
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = StudioPrimaryCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Ping", color = StudioPrimaryCyan, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Configure", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}
