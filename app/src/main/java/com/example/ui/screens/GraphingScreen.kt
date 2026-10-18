package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.FunctionPlotter
import kotlin.math.roundToInt

@Composable
fun GraphingScreen(
    modifier: Modifier = Modifier
) {
    var functionInput by remember { mutableStateOf("x^2 - 4") }
    val plotter = remember { FunctionPlotter() }

    // Graph viewport state
    var scale by remember { mutableFloatStateOf(40f) } // pixels per math unit
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Trace coordinate
    var inspectX by remember { mutableStateOf<Double?>(null) }
    var inspectY by remember { mutableStateOf<Double?>(null) }

    val presetFunctions = listOf(
        "x^2 - 4",
        "sin(x)",
        "cos(x)",
        "x^3 - 3*x",
        "1/x",
        "2^x"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Function Input Field
        OutlinedTextField(
            value = functionInput,
            onValueChange = { functionInput = it },
            label = { Text("أدخل الدالة: y = f(x)") },
            placeholder = { Text("مثال: x^2 + 2*x أو sin(x)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("function_input_field"),
            shape = RoundedCornerShape(16.dp)
        )

        // Presets chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetFunctions.take(4).forEach { preset ->
                FilterChip(
                    selected = functionInput == preset,
                    onClick = { functionInput = preset },
                    label = { Text(preset, fontSize = 12.sp) }
                )
            }
        }

        // Coordinate Inspector Badge (if active)
        if (inspectX != null && inspectY != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نقطة التتبع: X = ${"%.2f".format(inspectX)}, Y = ${"%.2f".format(inspectY)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 13.sp
                    )
                    TextButton(onClick = { inspectX = null; inspectY = null }) {
                        Text("إخفاء", fontSize = 12.sp)
                    }
                }
            }
        }

        // Canvas Area with interactive Drag & Pan
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .testTag("graph_canvas_container")
        ) {
            val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
            val curveColor = MaterialTheme.colorScheme.primary

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                panOffsetX += dragAmount.x
                                panOffsetY += dragAmount.y
                            }
                        )
                    }
            ) {
                val originX = (size.width / 2f) + panOffsetX
                val originY = (size.height / 2f) + panOffsetY

                // 1. Draw Grid Lines
                val leftMath = -(originX / scale).toDouble()
                val rightMath = ((size.width - originX) / scale).toDouble()
                val topMath = (originY / scale).toDouble()
                val bottomMath = -((size.height - originY) / scale).toDouble()

                val minGridX = leftMath.toInt() - 1
                val maxGridX = rightMath.toInt() + 1
                for (gx in minGridX..maxGridX) {
                    val px = originX + (gx * scale)
                    drawLine(
                        color = gridColor,
                        start = Offset(px, 0f),
                        end = Offset(px, size.height),
                        strokeWidth = 1f
                    )
                }

                val minGridY = bottomMath.toInt() - 1
                val maxGridY = topMath.toInt() + 1
                for (gy in minGridY..maxGridY) {
                    val py = originY - (gy * scale)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, py),
                        end = Offset(size.width, py),
                        strokeWidth = 1f
                    )
                }

                // 2. Draw Main Axes (X and Y)
                // X-axis
                if (originY in 0f..size.height) {
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, originY),
                        end = Offset(size.width, originY),
                        strokeWidth = 2.5f
                    )
                }
                // Y-axis
                if (originX in 0f..size.width) {
                    drawLine(
                        color = axisColor,
                        start = Offset(originX, 0f),
                        end = Offset(originX, size.height),
                        strokeWidth = 2.5f
                    )
                }

                // 3. Draw Function Curve
                val stepCount = (size.width / 2f).roundToInt().coerceIn(100, 600)
                val points = plotter.computePoints(
                    expression = functionInput,
                    minX = leftMath,
                    maxX = rightMath,
                    stepCount = stepCount
                )

                val path = Path()
                var isFirstValid = true

                for (pt in points) {
                    if (!pt.isValid) {
                        isFirstValid = true
                        continue
                    }
                    val screenX = originX + (pt.x * scale)
                    val screenY = originY - (pt.y * scale)

                    // Bound checks to prevent drawing outside screen bounds
                    if (screenY < -size.height * 2 || screenY > size.height * 3) {
                        isFirstValid = true
                        continue
                    }

                    if (isFirstValid) {
                        path.moveTo(screenX, screenY)
                        isFirstValid = false
                    } else {
                        path.lineTo(screenX, screenY)
                    }
                }

                drawPath(
                    path = path,
                    color = curveColor,
                    style = Stroke(width = 3.5f)
                )

                // 4. Draw Origin Marker dot
                if (originX in 0f..size.width && originY in 0f..size.height) {
                    drawCircle(
                        color = curveColor,
                        radius = 4f,
                        center = Offset(originX, originY)
                    )
                }
            }

            // Floating Controls: Zoom In, Zoom Out, Reset Origin
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { scale = (scale * 1.25f).coerceAtMost(300f) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "تكبير")
                }
                SmallFloatingActionButton(
                    onClick = { scale = (scale / 1.25f).coerceAtLeast(10f) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "تصغير")
                }
                SmallFloatingActionButton(
                    onClick = {
                        scale = 40f
                        panOffsetX = 0f
                        panOffsetY = 0f
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط المركز")
                }
            }
        }
    }
}
