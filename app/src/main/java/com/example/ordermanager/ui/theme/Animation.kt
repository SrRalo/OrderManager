package com.example.ordermanager.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object AnimSpecs {
    val default: FiniteAnimationSpec<Float> = tween(300)
    val slow: FiniteAnimationSpec<Float> = tween(600, easing = FastOutSlowInEasing)
    val springSpec: FiniteAnimationSpec<Float> = spring(dampingRatio = 0.7f, stiffness = 300f)
    val easing: Easing = FastOutSlowInEasing
}
