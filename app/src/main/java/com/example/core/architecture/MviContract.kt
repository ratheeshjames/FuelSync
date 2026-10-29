package com.example.core.architecture

/**
 * Immutable State interface representing the visual model rendered by Jetpack Compose.
 */
interface UiState

/**
 * Action / Intent interface representing user or system interactions sent to ViewModel.
 */
interface UiAction

/**
 * One-off event interface for transient UI interactions (Snackbars, Haptic triggers, Nav pops).
 */
interface UiEvent
