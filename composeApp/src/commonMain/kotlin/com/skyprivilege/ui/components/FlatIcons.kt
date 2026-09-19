package com.skyprivilege.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skyprivilege.ui.AppTab

enum class FlatIconType {
    HOME,
    TRANSACTION,
    SCAN,
    ABSEN,
    PROFILE,
    CAMERA_SCAN,
    GPS_PIN,
    EDIT_NOTE,
    LOCK_SHIFT,
    CHECKLIST,
    VOUCHER_TICKET,
    HISTORY_CLOCK,
    BOOK_SOP
}

fun getTabIcon(tab: AppTab): FlatIconType {
    return when (tab) {
        AppTab.HOME -> FlatIconType.HOME
        AppTab.HISTORY -> FlatIconType.TRANSACTION
        AppTab.SCAN -> FlatIconType.SCAN
        AppTab.ABSEN -> FlatIconType.ABSEN
        AppTab.AKUN_SAYA -> FlatIconType.PROFILE
    }
}

/**
 * Modern flat minimalist Home icon.
 * Features a sharp gable roof, solid rectangular house body, and centered arched door cutout.
 */
@Composable
fun FlatHomeIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF005BAC),
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // House body & roof path
        val housePath = Path().apply {
            // Peak of roof
            moveTo(w * 0.50f, h * 0.12f)
            // Right eave
            lineTo(w * 0.90f, h * 0.44f)
            // Right wall top
            lineTo(w * 0.80f, h * 0.44f)
            // Right wall bottom
            lineTo(w * 0.80f, h * 0.88f)
            // Right of door
            lineTo(w * 0.60f, h * 0.88f)
            // Door right edge up
            lineTo(w * 0.60f, h * 0.58f)
            // Door arch to left
            cubicTo(
                w * 0.60f, h * 0.50f,
                w * 0.40f, h * 0.50f,
                w * 0.40f, h * 0.58f
            )
            // Door left edge down
            lineTo(w * 0.40f, h * 0.88f)
            // Left wall bottom
            lineTo(w * 0.20f, h * 0.88f)
            // Left wall top
            lineTo(w * 0.20f, h * 0.44f)
            // Left eave
            lineTo(w * 0.10f, h * 0.44f)
            close()
        }
        drawPath(housePath, color = tint, style = Fill)
    }
}

/**
 * Modern flat Transaction / Credit Card icon.
 * Features a clean card silhouette with magnetic stripe cutout and signature chip block.
 */
@Composable
fun FlatTransactionIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF64748B),
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.08f

        // Card body outline
        val cardRect = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = w * 0.10f,
                    top = h * 0.22f,
                    right = w * 0.90f,
                    bottom = h * 0.78f,
                    radiusX = w * 0.10f,
                    radiusY = w * 0.10f
                )
            )
        }
        drawPath(cardRect, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Magnetic line
        drawLine(
            color = tint,
            start = Offset(w * 0.10f, h * 0.40f),
            end = Offset(w * 0.90f, h * 0.40f),
            strokeWidth = stroke
        )

        // Chip / card embossed line
        drawLine(
            color = tint,
            start = Offset(w * 0.24f, h * 0.60f),
            end = Offset(w * 0.48f, h * 0.60f),
            strokeWidth = stroke * 0.8f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Modern flat Scan icon.
 * 4 crisp viewfinder corner brackets with a centered camera aperture / optical dot.
 */
@Composable
fun FlatScanIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.09f
        val arm = w * 0.22f

        // Top-Left corner
        drawLine(color = tint, start = Offset(w * 0.14f, h * 0.14f + arm), end = Offset(w * 0.14f, h * 0.14f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.14f, h * 0.14f), end = Offset(w * 0.14f + arm, h * 0.14f), strokeWidth = stroke, cap = StrokeCap.Round)

        // Top-Right corner
        drawLine(color = tint, start = Offset(w * 0.86f - arm, h * 0.14f), end = Offset(w * 0.86f, h * 0.14f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.86f, h * 0.14f), end = Offset(w * 0.86f, h * 0.14f + arm), strokeWidth = stroke, cap = StrokeCap.Round)

        // Bottom-Left corner
        drawLine(color = tint, start = Offset(w * 0.14f, h * 0.86f - arm), end = Offset(w * 0.14f, h * 0.86f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.14f, h * 0.86f), end = Offset(w * 0.14f + arm, h * 0.86f), strokeWidth = stroke, cap = StrokeCap.Round)

        // Bottom-Right corner
        drawLine(color = tint, start = Offset(w * 0.86f - arm, h * 0.86f), end = Offset(w * 0.86f, h * 0.86f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.86f, h * 0.86f), end = Offset(w * 0.86f, h * 0.86f - arm), strokeWidth = stroke, cap = StrokeCap.Round)

        // Center Laser or Aperture: Modern clean horizontal scanner bar + center dot
        drawLine(
            color = tint,
            start = Offset(w * 0.28f, h * 0.50f),
            end = Offset(w * 0.72f, h * 0.50f),
            strokeWidth = stroke * 0.8f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = tint,
            radius = w * 0.08f,
            center = Offset(w * 0.50f, h * 0.50f)
        )
    }
}

/**
 * Modern flat Absen / Stopwatch clock icon.
 * Circular watch face with top stem and clock hands pointing at work shift hours.
 */
@Composable
fun FlatAbsenIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF64748B),
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.08f
        val center = Offset(w * 0.50f, h * 0.56f)
        val radius = w * 0.36f

        // Top button stem
        drawLine(
            color = tint,
            start = Offset(w * 0.50f, h * 0.08f),
            end = Offset(w * 0.50f, h * 0.18f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Top crown bar
        drawLine(
            color = tint,
            start = Offset(w * 0.40f, h * 0.08f),
            end = Offset(w * 0.60f, h * 0.08f),
            strokeWidth = stroke * 0.9f,
            cap = StrokeCap.Round
        )

        // Watch face circle
        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = stroke)
        )

        // Center pivot dot
        drawCircle(
            color = tint,
            radius = stroke * 0.7f,
            center = center
        )

        // Hour hand (pointing up)
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x, center.y - radius * 0.60f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        // Minute hand (pointing 2 o'clock)
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x + radius * 0.48f, center.y - radius * 0.25f),
            strokeWidth = stroke * 0.85f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Modern flat Profile / User icon.
 * Clean silhouette with circular head and smooth dome shoulder torso.
 */
@Composable
fun FlatProfileIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF64748B),
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Head circle
        drawCircle(
            color = tint,
            radius = w * 0.20f,
            center = Offset(w * 0.50f, h * 0.30f)
        )

        // Torso path
        val torsoPath = Path().apply {
            moveTo(w * 0.16f, h * 0.88f)
            cubicTo(
                w * 0.16f, h * 0.62f,
                w * 0.32f, h * 0.56f,
                w * 0.50f, h * 0.56f
            )
            cubicTo(
                w * 0.68f, h * 0.56f,
                w * 0.84f, h * 0.62f,
                w * 0.84f, h * 0.88f
            )
            close()
        }
        drawPath(torsoPath, color = tint, style = Fill)
    }
}

/**
 * Flat Camera Scanner icon for ticket verification.
 */
@Composable
fun FlatCameraScanIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.08f

        // Camera body outline
        val cameraPath = Path().apply {
            moveTo(w * 0.35f, h * 0.24f)
            lineTo(w * 0.65f, h * 0.24f)
            lineTo(w * 0.72f, h * 0.34f)
            lineTo(w * 0.86f, h * 0.34f)
            cubicTo(w * 0.90f, h * 0.34f, w * 0.92f, h * 0.36f, w * 0.92f, h * 0.40f)
            lineTo(w * 0.92f, h * 0.80f)
            cubicTo(w * 0.92f, h * 0.84f, w * 0.90f, h * 0.86f, w * 0.86f, h * 0.86f)
            lineTo(w * 0.14f, h * 0.86f)
            cubicTo(w * 0.10f, h * 0.86f, w * 0.08f, h * 0.84f, w * 0.08f, h * 0.80f)
            lineTo(w * 0.08f, h * 0.40f)
            cubicTo(w * 0.08f, h * 0.36f, w * 0.10f, h * 0.34f, w * 0.14f, h * 0.34f)
            lineTo(w * 0.28f, h * 0.34f)
            close()
        }
        drawPath(cameraPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Center lens circle
        drawCircle(
            color = tint,
            radius = w * 0.16f,
            center = Offset(w * 0.50f, h * 0.58f),
            style = Stroke(width = stroke)
        )
    }
}

/**
 * Flat GPS Pin / Map Marker icon.
 */
@Composable
fun FlatGpsPinIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    cutoutColor: Color = Color(0xFF072146),
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val pinPath = Path().apply {
            moveTo(w * 0.50f, h * 0.90f)
            cubicTo(
                w * 0.28f, h * 0.60f,
                w * 0.20f, h * 0.42f,
                w * 0.20f, h * 0.32f
            )
            cubicTo(
                w * 0.20f, h * 0.16f,
                w * 0.33f, h * 0.10f,
                w * 0.50f, h * 0.10f
            )
            cubicTo(
                w * 0.67f, h * 0.10f,
                w * 0.80f, h * 0.16f,
                w * 0.80f, h * 0.32f
            )
            cubicTo(
                w * 0.80f, h * 0.42f,
                w * 0.72f, h * 0.60f,
                w * 0.50f, h * 0.90f
            )
            close()
        }
        drawPath(pinPath, color = tint, style = Fill)

        // Center cutout
        drawCircle(
            color = cutoutColor,
            radius = w * 0.12f,
            center = Offset(w * 0.50f, h * 0.32f)
        )
    }
}

/**
 * Flat Edit Note / Document with Pencil icon.
 */
@Composable
fun FlatEditNoteIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.08f

        // Document rectangle
        val docPath = Path().apply {
            moveTo(w * 0.20f, h * 0.15f)
            lineTo(w * 0.60f, h * 0.15f)
            lineTo(w * 0.80f, h * 0.35f)
            lineTo(w * 0.80f, h * 0.85f)
            lineTo(w * 0.20f, h * 0.85f)
            close()
        }
        drawPath(docPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Document lines
        drawLine(color = tint, start = Offset(w * 0.32f, h * 0.45f), end = Offset(w * 0.68f, h * 0.45f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.32f, h * 0.60f), end = Offset(w * 0.60f, h * 0.60f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.32f, h * 0.75f), end = Offset(w * 0.48f, h * 0.75f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

/**
 * Flat Lock / Unlock Shift icon.
 */
@Composable
fun FlatLockShiftIcon(
    isLocked: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.08f

        // Lock body (rounded rectangle)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.22f, h * 0.45f),
            size = Size(w * 0.56f, h * 0.45f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
            style = Fill
        )

        // Shackle arc
        val shacklePath = Path().apply {
            if (isLocked) {
                moveTo(w * 0.34f, h * 0.45f)
                lineTo(w * 0.34f, h * 0.28f)
                cubicTo(w * 0.34f, h * 0.12f, w * 0.66f, h * 0.12f, w * 0.66f, h * 0.28f)
                lineTo(w * 0.66f, h * 0.45f)
            } else {
                // Open shackle
                moveTo(w * 0.34f, h * 0.45f)
                lineTo(w * 0.34f, h * 0.25f)
                cubicTo(w * 0.34f, h * 0.10f, w * 0.66f, h * 0.10f, w * 0.66f, h * 0.25f)
                lineTo(w * 0.66f, h * 0.35f)
            }
        }
        drawPath(shacklePath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Keyhole cutout
        drawCircle(
            color = Color(0xFF0072CE),
            radius = w * 0.06f,
            center = Offset(w * 0.50f, h * 0.64f)
        )
    }
}

/**
 * Flat Checklist Clipboard icon.
 */
@Composable
fun FlatChecklistIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.08f

        // Board outline
        val boardRect = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = w * 0.18f,
                    top = h * 0.22f,
                    right = w * 0.82f,
                    bottom = h * 0.90f,
                    radiusX = w * 0.08f,
                    radiusY = w * 0.08f
                )
            )
        }
        drawPath(boardRect, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Clip at top
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.36f, h * 0.12f),
            size = Size(w * 0.28f, h * 0.14f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        // Checkmark inside
        val checkPath = Path().apply {
            moveTo(w * 0.32f, h * 0.54f)
            lineTo(w * 0.44f, h * 0.66f)
            lineTo(w * 0.68f, h * 0.42f)
        }
        drawPath(checkPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/**
 * Flat Voucher / Ticket icon with notched sides.
 */
@Composable
fun FlatVoucherTicketIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.08f

        val ticketPath = Path().apply {
            moveTo(w * 0.10f, h * 0.25f)
            lineTo(w * 0.90f, h * 0.25f)
            lineTo(w * 0.90f, h * 0.44f)
            cubicTo(w * 0.80f, h * 0.44f, w * 0.80f, h * 0.56f, w * 0.90f, h * 0.56f)
            lineTo(w * 0.90f, h * 0.75f)
            lineTo(w * 0.10f, h * 0.75f)
            lineTo(w * 0.10f, h * 0.56f)
            cubicTo(w * 0.20f, h * 0.56f, w * 0.20f, h * 0.44f, w * 0.10f, h * 0.44f)
            close()
        }
        drawPath(ticketPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Center dashed or dotted line
        drawLine(
            color = tint,
            start = Offset(w * 0.50f, h * 0.32f),
            end = Offset(w * 0.50f, h * 0.68f),
            strokeWidth = stroke * 0.8f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Flat History Clock / Rewind icon.
 */
@Composable
fun FlatHistoryClockIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.08f
        val center = Offset(w * 0.50f, h * 0.50f)
        val radius = w * 0.34f

        // Clock circle
        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = stroke)
        )

        // Hands
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x, center.y - radius * 0.58f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x + radius * 0.45f, center.y),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Flat Book SOP icon.
 */
@Composable
fun FlatBookSopIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.08f

        // Open book path
        val bookPath = Path().apply {
            // Left page top
            moveTo(w * 0.50f, h * 0.30f)
            cubicTo(w * 0.40f, h * 0.22f, w * 0.25f, h * 0.22f, w * 0.12f, h * 0.28f)
            // Left edge
            lineTo(w * 0.12f, h * 0.74f)
            // Left page bottom
            cubicTo(w * 0.25f, h * 0.68f, w * 0.40f, h * 0.68f, w * 0.50f, h * 0.76f)
            // Right page bottom
            cubicTo(w * 0.60f, h * 0.68f, w * 0.75f, h * 0.68f, w * 0.88f, h * 0.74f)
            // Right edge
            lineTo(w * 0.88f, h * 0.28f)
            // Right page top
            cubicTo(w * 0.75f, h * 0.22f, w * 0.60f, h * 0.22f, w * 0.50f, h * 0.30f)
            close()
        }
        drawPath(bookPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Center spine
        drawLine(
            color = tint,
            start = Offset(w * 0.50f, h * 0.30f),
            end = Offset(w * 0.50f, h * 0.76f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Flat Pencil Edit icon for credential / profile settings.
 */
@Composable
fun FlatEditIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF005BAC),
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.09f

        val pencilPath = Path().apply {
            moveTo(w * 0.75f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.25f)
            lineTo(w * 0.35f, h * 0.75f)
            lineTo(w * 0.15f, h * 0.85f)
            lineTo(w * 0.25f, h * 0.65f)
            close()
        }
        drawPath(pencilPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        drawLine(
            color = tint,
            start = Offset(w * 0.65f, h * 0.25f),
            end = Offset(w * 0.75f, h * 0.35f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Flat Logout / Door exit icon.
 */
@Composable
fun FlatLogoutIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFE11D48),
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.09f

        // Door frame
        val doorPath = Path().apply {
            moveTo(w * 0.55f, h * 0.15f)
            lineTo(w * 0.20f, h * 0.15f)
            lineTo(w * 0.20f, h * 0.85f)
            lineTo(w * 0.55f, h * 0.85f)
        }
        drawPath(doorPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Arrow pointing out
        drawLine(
            color = tint,
            start = Offset(w * 0.40f, h * 0.50f),
            end = Offset(w * 0.85f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrow head top
        drawLine(
            color = tint,
            start = Offset(w * 0.68f, h * 0.33f),
            end = Offset(w * 0.85f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrow head bottom
        drawLine(
            color = tint,
            start = Offset(w * 0.68f, h * 0.67f),
            end = Offset(w * 0.85f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Flat Airplane silhouette icon for Promo banner.
 */
@Composable
fun FlatAirplaneIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val planePath = Path().apply {
            moveTo(w * 0.50f, h * 0.08f)
            lineTo(w * 0.58f, h * 0.36f)
            lineTo(w * 0.95f, h * 0.54f)
            lineTo(w * 0.95f, h * 0.62f)
            lineTo(w * 0.58f, h * 0.54f)
            lineTo(w * 0.58f, h * 0.78f)
            lineTo(w * 0.72f, h * 0.88f)
            lineTo(w * 0.72f, h * 0.94f)
            lineTo(w * 0.50f, h * 0.88f)
            lineTo(w * 0.28f, h * 0.94f)
            lineTo(w * 0.28f, h * 0.88f)
            lineTo(w * 0.42f, h * 0.78f)
            lineTo(w * 0.42f, h * 0.54f)
            lineTo(w * 0.05f, h * 0.62f)
            lineTo(w * 0.05f, h * 0.54f)
            lineTo(w * 0.42f, h * 0.36f)
            close()
        }
        drawPath(planePath, color = tint, style = Fill)
    }
}
