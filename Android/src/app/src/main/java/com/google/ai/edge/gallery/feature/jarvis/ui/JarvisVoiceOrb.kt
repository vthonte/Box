package com.google.ai.edge.gallery.feature.jarvis.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.feature.jarvis.core.JarvisManager
import com.google.ai.edge.gallery.feature.jarvis.core.JarvisState

@Composable
fun JarvisVoiceOrb(jarvisManager: JarvisManager) {
    val state by jarvisManager.state.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == JarvisState.LISTENING) 1.3f else 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val orbColor = when (state) {
        JarvisState.IDLE -> Color.Gray.copy(alpha = 0.5f)
        JarvisState.LISTENING -> Color(0xFF3174F1) // Blue
        JarvisState.THINKING -> Color(0xFF85B1F8) // Light Blue
        JarvisState.SPEAKING -> Color(0xFF34A853) // Green
        JarvisState.EXECUTING -> Color(0xFFEA4335) // Red
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp * scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(orbColor, orbColor.copy(alpha = 0.3f))
                    )
                )
        )
    }
}
