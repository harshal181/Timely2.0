package com.om.timely.faculty.subjects.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun SubjectColorPicker(
    modifier: Modifier = Modifier,
    initialColor: Color = Color(0xFF7C3AED),
    onColorChanged: (Color) -> Unit
) {
    val hsv = remember { FloatArray(3).apply { android.graphics.Color.colorToHSV(initialColor.toArgb(), this) } }

    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    val currentColor = Color.hsv(hue, saturation, value)

    val density = LocalDensity.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Subject Color", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937), modifier = Modifier.padding(bottom = 8.dp))

        // --- HUE SLIDER ---
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        hue = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                        onColorChanged(Color.hsv(hue, saturation, value))
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        hue = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                        onColorChanged(Color.hsv(hue, saturation, value))
                    }
                }
        ) {
            // FIX 1: Calculate Hue knob position using 'hue' variable
            val knobSizePx = with(density) { 12.dp.toPx() }
            val knobX = ((hue / 360f) * constraints.maxWidth.toFloat()) - knobSizePx

            Box(
                modifier = Modifier
                    .offset { IntOffset(knobX.roundToInt(), 0) }
                    .size(24.dp)
                    .border(3.dp, Color.White, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SATURATION / VALUE AREA ---
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.hsv(hue, 1f, 1f))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        saturation = (offset.x / size.width).coerceIn(0f, 1f)
                        value = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                        onColorChanged(Color.hsv(hue, saturation, value))
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        saturation = (change.position.x / size.width).coerceIn(0f, 1f)
                        value = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
                        onColorChanged(Color.hsv(hue, saturation, value))
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(Brush.horizontalGradient(listOf(Color.White, Color.Transparent)))
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            }

            // FIX 2: Redefine knobX and knobY inside this scope using saturation and value
            val knobSizePx = with(density) { 12.dp.toPx() }
            val knobX = (saturation * constraints.maxWidth.toFloat()) - knobSizePx
            val knobY = ((1f - value) * constraints.maxHeight.toFloat()) - knobSizePx

            Box(
                modifier = Modifier
                    .offset { IntOffset(knobX.roundToInt(), knobY.roundToInt()) }
                    .size(24.dp)
                    .border(3.dp, Color.White, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = String.format("#%06X", (0xFFFFFF and currentColor.toArgb())),
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

private fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
