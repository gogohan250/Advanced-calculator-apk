package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.engine.SoundFeedback
import com.example.ui.components.*
import com.example.ui.screens.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCalculatorApp(
    settingsManager: SettingsManager,
    historyRepo: HistoryRepository,
    currencyRepo: CurrencyRepository,
    soundFeedback: SoundFeedback,
    modifier: Modifier = Modifier
) {
    val settings by settingsManager.settings.collectAsState()
    val historyItems by historyRepo.history.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var currentMode by remember { mutableStateOf(CalculatorMode.STANDARD) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    // Intercept back button: if drawer is open, close it; if in subscreen, return to standard calculator
    BackHandler(enabled = drawerState.isOpen || currentMode != CalculatorMode.STANDARD) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            currentMode = CalculatorMode.STANDARD
        }
    }

    // Force RTL layout direction across all UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    currentMode = currentMode,
                    onSelectMode = { mode ->
                        currentMode = mode
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "الحاسبة المتكاملة",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentMode.titleAr,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "القائمة")
                            }
                        },
                        actions = {
                            // Quick toggle Arabic / Western numerals
                            IconButton(
                                onClick = {
                                    val nextFormat = if (settings.numberFormat == NumberFormatStyle.ARABIC) {
                                        NumberFormatStyle.WESTERN
                                    } else {
                                        NumberFormatStyle.ARABIC
                                    }
                                    settingsManager.updateNumberFormat(nextFormat)
                                }
                            ) {
                                Text(
                                    text = if (settings.numberFormat == NumberFormatStyle.ARABIC) "١٢٣" else "123",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // History button
                            IconButton(onClick = { showHistorySheet = true }) {
                                Icon(Icons.Default.History, contentDescription = "السجل")
                            }

                            // Settings button
                            IconButton(onClick = { showSettingsSheet = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "الإعدادات")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                bottomBar = {
                    AppBottomNavBar(
                        currentMode = currentMode,
                        onSelectMode = { currentMode = it },
                        onOpenAllModes = { coroutineScope.launch { drawerState.open() } }
                    )
                },
                modifier = modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentMode) {
                        CalculatorMode.STANDARD -> StandardCalculatorScreen(
                            settings = settings,
                            historyRepo = historyRepo,
                            soundFeedback = soundFeedback,
                            onOpenHistory = { showHistorySheet = true }
                        )
                        CalculatorMode.PROGRAMMER -> ProgrammerCalculatorScreen(
                            settings = settings,
                            soundFeedback = soundFeedback
                        )
                        CalculatorMode.GRAPHING -> GraphingScreen()
                        CalculatorMode.UNITS -> UnitConverterScreen(
                            settings = settings
                        )
                        CalculatorMode.CURRENCY -> CurrencyConverterScreen(
                            currencyRepo = currencyRepo,
                            settings = settings
                        )
                        CalculatorMode.FINANCIAL -> FinancialCalculatorsScreen(
                            settings = settings
                        )
                        CalculatorMode.OTHER -> OtherCalculatorsScreen(
                            settings = settings
                        )
                    }
                }

                // History Sheet
                if (showHistorySheet) {
                    HistoryBottomSheet(
                        historyList = historyItems,
                        onSelectResult = { selectedResult ->
                            showHistorySheet = false
                        },
                        onDeleteItem = { historyRepo.deleteItem(it) },
                        onClearAll = { historyRepo.clearAll() },
                        onDismiss = { showHistorySheet = false }
                    )
                }

                // Settings Sheet
                if (showSettingsSheet) {
                    SettingsBottomSheet(
                        currentSettings = settings,
                        onThemeChange = { settingsManager.updateTheme(it) },
                        onNumberFormatChange = { settingsManager.updateNumberFormat(it) },
                        onGroupingChange = { settingsManager.toggleGrouping(it) },
                        onHapticChange = { settingsManager.toggleHaptic(it) },
                        onSoundChange = { settingsManager.toggleSound(it) },
                        onPrecisionChange = { settingsManager.updateDecimalPrecision(it) },
                        onDismiss = { showSettingsSheet = false }
                    )
                }
            }
        }
    }
}
