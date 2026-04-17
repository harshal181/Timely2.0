package com.om.timely.faculty.timetable_generation.presentation.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.om.timely.faculty.timetable_generation.domain.model.TimetableSlot
import kotlin.math.roundToInt

// ── State ─────────────────────────────────────────────────────────────────────

class DragDropState {
    var isDragging        by mutableStateOf(false)
    var itemWindowTopLeft by mutableStateOf(Offset.Zero)
    var dragDelta         by mutableStateOf(Offset.Zero)
    var fingerOffset      by mutableStateOf(Offset.Zero)

    var draggedSlot       by mutableStateOf<TimetableSlot?>(null)
    var draggedDayId      by mutableStateOf(-1)
    var draggedSlotIndex  by mutableStateOf(-1)
    var draggedItemSize   by mutableStateOf(IntSize.Zero)

    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var providerWindowTopLeft by mutableStateOf(Offset.Zero)

    // Drop targets: targetId -> info
    val dropTargets = mutableMapOf<String, DropTargetInfo>()
    var currentDropTargetId by mutableStateOf<String?>(null)

    // Pending drop callback — fired just before state clear
    var pendingDrop: (() -> Unit)? = null

    fun startDrop() {
        // Execute the pending drop callback if we have one
        pendingDrop?.invoke()
        pendingDrop = null
        isDragging = false
        draggedSlot = null
        draggableComposable = null
        currentDropTargetId = null
        dragDelta = Offset.Zero
        draggedDayId = -1
        draggedSlotIndex = -1
    }

    fun cancelDrag() {
        pendingDrop = null
        isDragging = false
        draggedSlot = null
        draggableComposable = null
        currentDropTargetId = null
        dragDelta = Offset.Zero
        draggedDayId = -1
        draggedSlotIndex = -1
    }
}

data class DropTargetInfo(
    val dayId: Int,
    val slotIndex: Int,
    val bounds: Rect,
    val onDropped: (sourceDayId: Int, sourceSlotIndex: Int) -> Unit
)

val LocalDragDropState = compositionLocalOf { DragDropState() }

// ── Provider ──────────────────────────────────────────────────────────────────

@Composable
fun DragDropProvider(content: @Composable BoxScope.() -> Unit) {
    val state = remember { DragDropState() }

    CompositionLocalProvider(LocalDragDropState provides state) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    state.providerWindowTopLeft = it.localToWindow(Offset.Zero)
                }
        ) {
            content()

            // Floating drag overlay
            if (state.isDragging && state.draggableComposable != null) {
                val density = LocalDensity.current
                val overlayX = state.itemWindowTopLeft.x + state.dragDelta.x - state.providerWindowTopLeft.x
                val overlayY = state.itemWindowTopLeft.y + state.dragDelta.y - state.providerWindowTopLeft.y

                with(density) {
                    Box(
                        modifier = Modifier
                            .size(
                                width  = state.draggedItemSize.width.toDp(),
                                height = state.draggedItemSize.height.toDp()
                            )
                            .offset { IntOffset(overlayX.roundToInt(), overlayY.roundToInt()) }
                    ) {
                        state.draggableComposable!!()
                    }
                }
            }
        }
    }
}

// ── dragTarget ────────────────────────────────────────────────────────────────

fun Modifier.dragTarget(
    slot: TimetableSlot,
    dayId: Int,
    slotIndex: Int,
    composable: @Composable () -> Unit
): Modifier = composed {
    val state = LocalDragDropState.current
    var itemTopLeft by remember { mutableStateOf(Offset.Zero) }
    var itemSize    by remember { mutableStateOf(IntSize.Zero) }

    this
        .onGloballyPositioned { coords ->
            itemTopLeft = coords.localToWindow(Offset.Zero)
            itemSize    = coords.size
        }
        .pointerInput(slot.id) {
            detectDragGestures(
                onDragStart = { localOffset ->
                    state.isDragging          = true
                    state.itemWindowTopLeft   = itemTopLeft
                    state.dragDelta           = Offset.Zero
                    state.fingerOffset        = localOffset
                    state.draggedSlot         = slot
                    state.draggedDayId        = dayId
                    state.draggedSlotIndex    = slotIndex
                    state.draggedItemSize     = itemSize
                    state.draggableComposable = composable
                    state.currentDropTargetId = null
                    state.pendingDrop         = null
                },
                onDrag = { change, delta ->
                    change.consume()
                    state.dragDelta += delta

                    val fingerWindow = state.itemWindowTopLeft + state.fingerOffset + state.dragDelta

                    // Find which target the finger overlaps
                    val hit = state.dropTargets.entries
                        .firstOrNull { (_, info) -> info.bounds.contains(fingerWindow) }

                    state.currentDropTargetId = hit?.key

                    // Pre-register the drop callback for when finger lifts
                    if (hit != null) {
                        val srcDay  = dayId
                        val srcSlot = slotIndex
                        state.pendingDrop = {
                            hit.value.onDropped(srcDay, srcSlot)
                        }
                    } else {
                        state.pendingDrop = null
                    }
                },
                onDragEnd   = { state.startDrop() },
                onDragCancel = { state.cancelDrag() }
            )
        }
}

// ── dropTarget ────────────────────────────────────────────────────────────────

fun Modifier.dropTarget(
    dayId: Int,
    slotIndex: Int,
    onDropped: (sourceDayId: Int, sourceSlotIndex: Int) -> Unit
): Modifier = composed {
    val state    = LocalDragDropState.current
    val targetId = "target_${dayId}_${slotIndex}"

    val modifier = this.onGloballyPositioned { coords ->
        state.dropTargets[targetId] = DropTargetInfo(
            dayId     = dayId,
            slotIndex = slotIndex,
            bounds    = coords.boundsInWindow(),
            onDropped = onDropped
        )
    }

    DisposableEffect(targetId) {
        onDispose { state.dropTargets.remove(targetId) }
    }

    modifier
}
