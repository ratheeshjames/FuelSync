package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.di.AppEnvironment
import com.example.domain.model.CalculatedFuelEntry
import com.example.domain.model.DateRangeFilter
import com.example.domain.model.FillType
import com.example.domain.model.FuelType
import com.example.ui.sheets.FastLogBottomSheet
import com.example.ui.sheets.ReceiptAndTaggingBottomSheet
import com.example.ui.sheets.VehicleSwitcherBottomSheet
import com.example.ui.theme.LocalExpressiveShapes
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    appEnvironment: AppEnvironment,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { LogsViewModel(appEnvironment) }
    val uiState by viewModel.uiState.collectAsState()
    val shapes = LocalExpressiveShapes.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filterScrollState = rememberScrollState()

    val fastLogSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val vehicleSwitcherSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val taggingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Event handling
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LogsUiEvent.ShowSnackbar -> {
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            duration = if (event.actionLabel != null) SnackbarDuration.Short else SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.handleIntent(LogsUiAction.UndoDelete)
                        }
                    }
                }
                is LogsUiEvent.ShareCsv -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("FuelSync CSV", event.csvContent)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (uiState.isMultiSelectMode) {
                // Contextual Batch Action Top Bar
                TopAppBar(
                    title = {
                        Text(
                            text = "${uiState.selectedEntryIds.size} Selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.handleIntent(LogsUiAction.ExitMultiSelect) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Exit Selection")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.handleIntent(LogsUiAction.SelectAll) },
                            modifier = Modifier.testTag("action_select_all")
                        ) {
                            Icon(Icons.Filled.SelectAll, contentDescription = "Select All")
                        }
                        IconButton(
                            onClick = { viewModel.handleIntent(LogsUiAction.ExportSelectedToCsv) },
                            modifier = Modifier.testTag("action_export_csv")
                        ) {
                            Icon(Icons.Filled.FileDownload, contentDescription = "Export CSV")
                        }
                        IconButton(
                            onClick = { viewModel.handleIntent(LogsUiAction.DeleteSelected) },
                            modifier = Modifier.testTag("action_bulk_delete")
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                // Standard Search & Quick Actions Top Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = shapes.pill,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { viewModel.handleIntent(LogsUiAction.ToggleVehicleSwitcherSheet(true)) },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("vehicle_switcher_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DirectionsCar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = uiState.activeVehicle?.name ?: "All Vehicles",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("▼", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Fast-Log 5s Button
                        Surface(
                            shape = shapes.pill,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = { viewModel.handleIntent(LogsUiAction.ToggleFastLogSheet(true)) },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("fast_log_trigger_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "5s Fast Log",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false
                                )
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(10.dp))

                    // Debounced Search Bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.handleIntent(LogsUiAction.Search(it)) },
                        placeholder = { Text("Search station, notes, or highway...") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.handleIntent(LogsUiAction.Search("")) }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = shapes.pill,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("logs_search_input")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dynamic Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(filterScrollState)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date Range Filters
                DateRangeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = uiState.selectedDateRange == filter,
                        onClick = { viewModel.handleIntent(LogsUiAction.SetDateRange(filter)) },
                        label = { Text(filter.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Fill Status: Full vs Partial
                FilterChip(
                    selected = uiState.selectedFillType == FillType.FULL,
                    onClick = {
                        val next = if (uiState.selectedFillType == FillType.FULL) null else FillType.FULL
                        viewModel.handleIntent(LogsUiAction.SetFillType(next))
                    },
                    label = { Text("Full Only") }
                )

                FilterChip(
                    selected = uiState.selectedFillType == FillType.PARTIAL,
                    onClick = {
                        val next = if (uiState.selectedFillType == FillType.PARTIAL) null else FillType.PARTIAL
                        viewModel.handleIntent(LogsUiAction.SetFillType(next))
                    },
                    label = { Text("Partials Only") }
                )
            }

            // Results count and gesture hint banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.filteredEntries.size} entries",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Swipe: ➡ Duplicate | ⬅ Delete",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Entries List
            if (uiState.filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.LocalGasStation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching fill-ups found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try clearing search filters or logging a new fill-up",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("logs_feed_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredEntries, key = { it.entry.id }) { calculatedEntry ->
                        val entryId = calculatedEntry.entry.id
                        val isSelected = uiState.selectedEntryIds.contains(entryId)

                        val onToggleSelect = remember(entryId) {
                            { viewModel.handleIntent(LogsUiAction.ToggleSelectEntry(entryId)) }
                        }
                        val onDuplicate = remember(entryId) {
                            { viewModel.handleIntent(LogsUiAction.DuplicateEntry(entryId)) }
                        }
                        val onDelete = remember(entryId) {
                            { viewModel.handleIntent(LogsUiAction.DeleteEntryWithUndo(entryId)) }
                        }
                        val onCardClick = remember(entryId, uiState.isMultiSelectMode) {
                            {
                                if (uiState.isMultiSelectMode) {
                                    viewModel.handleIntent(LogsUiAction.ToggleSelectEntry(entryId))
                                } else {
                                    viewModel.handleIntent(LogsUiAction.OpenTaggingSheet(calculatedEntry))
                                }
                            }
                        }

                        SwipeableFuelLogCard(
                            calculated = calculatedEntry,
                            isSelected = isSelected,
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            onToggleSelect = onToggleSelect,
                            onDuplicate = onDuplicate,
                            onDelete = onDelete,
                            onCardClick = onCardClick
                        )
                    }


                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet 1: Fast-Log
    if (uiState.isFastLogSheetVisible) {
        FastLogBottomSheet(
            sheetState = fastLogSheetState,
            vehicle = uiState.activeVehicle,
            latestOdometerKm = uiState.allCalculatedEntries.firstOrNull()?.entry?.odometerKm ?: 0.0,
            averageUnitPrice = uiState.averageUnitPrice,
            onDismiss = { viewModel.handleIntent(LogsUiAction.ToggleFastLogSheet(false)) },
            onSaveFastLog = { entry ->
                coroutineScope.launch {
                    appEnvironment.repository.insertFuelEntry(entry)
                    viewModel.handleIntent(LogsUiAction.ToggleFastLogSheet(false))
                    viewModel.handleIntent(LogsUiAction.Search(uiState.searchQuery))
                }
            }
        )
    }

    // Modal Bottom Sheet 2: Vehicle Switcher
    if (uiState.isVehicleSwitcherSheetVisible) {
        VehicleSwitcherBottomSheet(
            sheetState = vehicleSwitcherSheetState,
            vehicles = uiState.allVehicles,
            activeVehicleId = uiState.activeVehicle?.id,
            onVehicleSelected = { vehicleId ->
                viewModel.handleIntent(LogsUiAction.SelectVehicle(vehicleId))
            },
            onDismiss = { viewModel.handleIntent(LogsUiAction.ToggleVehicleSwitcherSheet(false)) }
        )
    }

    // Modal Bottom Sheet 3: Receipt & Tagging
    if (uiState.isTaggingSheetVisible && uiState.selectedEntryForTagging != null) {
        ReceiptAndTaggingBottomSheet(
            sheetState = taggingSheetState,
            calculatedEntry = uiState.selectedEntryForTagging,
            onDismiss = { viewModel.handleIntent(LogsUiAction.CloseTaggingSheet) },
            onSaveTags = { updatedEntry ->
                viewModel.handleIntent(LogsUiAction.SaveTags(updatedEntry))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SwipeableFuelLogCard(
    calculated: CalculatedFuelEntry,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onToggleSelect: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onCardClick: () -> Unit
) {
    val shapes = LocalExpressiveShapes.current
    val entry = calculated.entry

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onDuplicate()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !isMultiSelectMode,
        enableDismissFromEndToStart = !isMultiSelectMode,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val backgroundColor by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "swipe_bg_color"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shapes.telemetryCard)
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Duplicate",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Duplicate",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Delete",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    ) {
        val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }
        val formattedDate = remember(entry.timestamp) { dateFormat.format(Date(entry.timestamp)) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shapes.telemetryCard)
                .combinedClickable(
                    onClick = onCardClick,
                    onLongClick = onToggleSelect
                )
                .testTag("fuel_item_${entry.id}"),
            shape = shapes.telemetryCard,
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isMultiSelectMode) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (entry.fillType) {
                                            FillType.FULL -> MaterialTheme.colorScheme.primaryContainer
                                            FillType.PARTIAL -> MaterialTheme.colorScheme.secondaryContainer
                                            FillType.MISSED_PREVIOUS -> MaterialTheme.colorScheme.errorContainer
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (entry.fillType) {
                                        FillType.FULL -> Icons.Filled.LocalGasStation
                                        FillType.PARTIAL -> Icons.Filled.HourglassTop
                                        FillType.MISSED_PREVIOUS -> Icons.Filled.Warning
                                    },
                                    contentDescription = null,
                                    tint = when (entry.fillType) {
                                        FillType.FULL -> MaterialTheme.colorScheme.onPrimaryContainer
                                        FillType.PARTIAL -> MaterialTheme.colorScheme.onSecondaryContainer
                                        FillType.MISSED_PREVIOUS -> MaterialTheme.colorScheme.onErrorContainer
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = entry.stationName ?: "Fuel Station",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$formattedDate • ${String.format(Locale.US, "%,.0f", entry.odometerKm)} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "$${String.format(Locale.US, "%.2f", entry.totalCost)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats and Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", entry.fuelQuantityLiters)} L @ $${String.format(Locale.US, "%.2f", entry.unitPrice)}/L",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (entry.isAirConditioningOn) {
                            Surface(
                                shape = shapes.pill,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AcUnit,
                                    contentDescription = "AC on",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(14.dp)
                                )
                            }
                        }

                        if (entry.drivingEnvironment != null) {
                            Surface(
                                shape = shapes.pill,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = entry.drivingEnvironment,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        when {
                            calculated.isPendingPartial -> {
                                Surface(
                                    shape = shapes.pill,
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "Partial Queued",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            calculated.isMissedBoundary -> {
                                Surface(
                                    shape = shapes.pill,
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = "Missed Gap",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            calculated.calculatedEfficiency != null -> {
                                Surface(
                                    shape = shapes.pill,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${calculated.calculatedEfficiency} (${calculated.deltaDistanceKm?.toInt()} km)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            else -> {
                                Surface(
                                    shape = shapes.pill,
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "Baseline",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }


                if (!entry.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Memo: ${entry.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
