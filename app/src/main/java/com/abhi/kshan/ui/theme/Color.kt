package com.abhi.kshan.ui.theme

import androidx.compose.ui.graphics.Color

// Strict 4-Color Design System for क्षण (Kshan)
// 1. Background (#070A12)
val KshanBackground = Color(0xFF070A12)
val SpaceBlack = KshanBackground

// 2. Surface (#121620)
val KshanSurface = Color(0xFF121620)
val SpaceDark = KshanSurface
val SpaceCard = KshanSurface
val SpaceSurface = KshanSurface
val ObsidianGlass = KshanSurface

// 3. Border (1.dp solid Color.White.copy(alpha = 0.08f))
val KshanBorder = Color.White.copy(alpha = 0.08f)
val KshanUnpinnedBorder = Color.White.copy(alpha = 0.06f)
val TitaniumHairline = KshanBorder
val SpaceCardBorder = KshanBorder

// 4. Accent: Single Interactive Cyan (#2ABFFF) + Exclusive Vision Gold (#E6C87A)
val KshanCyan = Color(0xFF2ABFFF)
val IceCyan = KshanCyan
val VisionGold = Color(0xFFE6C87A)
val WarmChampagne = VisionGold
val BrushedSilver = Color(0xFFCBD5E1)

// Category Dot Accents for Saved Kshans
val CategoryGoalDot = Color(0xFF2ABFFF)
val CategoryBirthdayDot = Color(0xFFF472B6)
val CategoryHolidayDot = Color(0xFFE6C87A)
val CategoryKshanDot = Color(0xFF10B981)

// Legacy Token Mappings -> Strict 4-Color System
val NeonCyan = KshanCyan
val NeonBlue = KshanCyan
val NeonPurple = KshanCyan
val NeonPink = VisionGold
val NeonEmerald = KshanCyan
val NeonAmber = VisionGold

// Text & Telemetry Hierarchy
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color.White.copy(alpha = 0.72f)
val TextMuted = Color.White.copy(alpha = 0.50f)
