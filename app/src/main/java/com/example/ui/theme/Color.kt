package com.example.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Termavigator Visual Tokens (PRD Completo v1.0 & PRD Design v0.1)
val TermavigatorWhite = Color(0xFFFFFFFF)
val TermavigatorSurfaceLight = Color(0xFFF4F8FD)
val TermavigatorBlueLight = Color(0xFF5BB8F5)
val TermavigatorBlueDark = Color(0xFF0A2A5E)
val TermavigatorYellow = Color(0xFFFFC93C)
val TermavigatorOrange = Color(0xFFFF8A1F)
val TermavigatorBlack = Color(0xFF0B0B0F)
val TermavigatorSurfaceDark = Color(0xFF131D31)
val TermavigatorCardBorderLight = Color(0xFFE2EDF9)
val TermavigatorCardBorderDark = Color(0xFF1B2C4E)
val TermavigatorTextSecondaryLight = Color(0xFF6B7E99)
val TermavigatorTextSecondaryDark = Color(0xFF8F9FB5)

// Gradients
val BrandGradient = Brush.linearGradient(
    colors = listOf(TermavigatorBlueLight, TermavigatorBlueDark),
    start = Offset.Zero,
    end = Offset.Infinite
)

val ActionGradient = Brush.horizontalGradient(
    colors = listOf(TermavigatorYellow, TermavigatorOrange)
)

val NightGradient = Brush.verticalGradient(
    colors = listOf(TermavigatorBlack, TermavigatorBlueDark)
)
