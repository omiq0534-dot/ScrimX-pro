package com.example.widget

import android.graphics.*

object WidgetCardRenderer {

    fun generateCardBitmap(widthPx: Int = 800, heightPx: Int = 420): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cornerRadius = 48f
        val bounds = RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat())

        // 1. Main Obsidian Dark Body Surface Gradient
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, heightPx.toFloat(),
                intArrayOf(
                    Color.parseColor("#141F2B"),
                    Color.parseColor("#080D14"),
                    Color.parseColor("#030508")
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, bodyPaint)

        // 2. Bottom-Left & Bottom-Right Corner Green Light Glow
        val glowPaintLeft = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                0f, heightPx.toFloat(), widthPx * 0.55f,
                Color.parseColor("#7000E676"),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, glowPaintLeft)

        val glowPaintRight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                widthPx.toFloat(), heightPx.toFloat(), widthPx * 0.55f,
                Color.parseColor("#7000E676"),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, glowPaintRight)

        // 3. Top 3D Tilted Glass Sheen Highlight
        val sheenBounds = RectF(8f, 6f, widthPx - 8f, heightPx * 0.45f)
        val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, heightPx * 0.45f,
                intArrayOf(
                    Color.parseColor("#38FFFFFF"),
                    Color.parseColor("#0EFFFFFF"),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(sheenBounds, cornerRadius - 4, cornerRadius - 4, sheenPaint)

        // 4. Cyber Matrix Accent Lines
        val matrixLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                30f, 0f, widthPx - 30f, 0f,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.parseColor("#8000E676"),
                    Color.TRANSPARENT
                ),
                null,
                Shader.TileMode.CLAMP
            )
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        // Top matrix line
        canvas.drawLine(35f, heightPx * 0.28f, widthPx - 35f, heightPx * 0.28f, matrixLinePaint)
        // Bottom matrix line
        canvas.drawLine(35f, heightPx * 0.72f, widthPx - 35f, heightPx * 0.72f, matrixLinePaint)

        // 5. Outer 3D Bevel Cyber Stroke
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2C3E52")
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        val strokeBounds = RectF(2f, 2f, widthPx - 2f, heightPx - 2f)
        canvas.drawRoundRect(strokeBounds, cornerRadius, cornerRadius, strokePaint)

        return bitmap
    }
}
