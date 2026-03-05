package com.om.timely.splash.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.R
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel(),
    onNavigate: (String) -> Unit
) {
    val navigateToNext by viewModel.navigateToNext.collectAsState()
    var animationFinished by remember { mutableStateOf(false) }

    // Start 2.4s timer for animation
    LaunchedEffect(Unit) {
        delay(2400)
        animationFinished = true
    }

    // Effect to navigate 
    LaunchedEffect(animationFinished, navigateToNext) {
        if (animationFinished && navigateToNext != null) {
            onNavigate(navigateToNext!!)
        }
    }

    // Animations Setup
    val scale = remember { Animatable(0f) }
    val opacity = remember { Animatable(0f) }
    val titleY = remember { Animatable(20f) }
    val titleOpacity = remember { Animatable(0f) }
    val descY = remember { Animatable(10f) }
    val descOpacity = remember { Animatable(0f) }
    
    val dotsShowed = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Logo entrance
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
            )
        )
    }

    LaunchedEffect(Unit) {
        opacity.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    LaunchedEffect(Unit) {
        delay(300)
        titleY.animateTo(0f, tween(500))
    }
    LaunchedEffect(Unit) {
        delay(300)
        titleOpacity.animateTo(1f, tween(500))
    }

    LaunchedEffect(Unit) {
        delay(500)
        descY.animateTo(0f, tween(500))
    }
    LaunchedEffect(Unit) {
        delay(500)
        descOpacity.animateTo(1f, tween(500))
    }

    LaunchedEffect(Unit) {
        delay(800)
        dotsShowed.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4C1D95),
                        Color(0xFF7C3AED),
                        Color(0xFF6366F1)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Top right background circle
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = 100.dp, y = (-120).dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .align(Alignment.TopEnd)
        )

        // Bottom left background circle
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-80).dp, y = 80.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .align(Alignment.BottomStart)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(scale.value)
                .alpha(opacity.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Logo Box
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(32.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.2f))
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.timely_ic),
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            // Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Timely",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.03).sp,
                    lineHeight = 40.sp,
                    modifier = Modifier
                        .offset(y = titleY.value.dp)
                        .alpha(titleOpacity.value)
                )
                Text(
                    text = "Smart College Scheduling",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.05.sp,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .offset(y = descY.value.dp)
                        .alpha(descOpacity.value)
                )
            }
        }

        // Loading Dots
        if (dotsShowed.value) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0..2) {
                    val dotOpacity = remember { Animatable(0.3f) }
                    LaunchedEffect(Unit) {
                        delay((i * 200).toLong())
                        dotOpacity.animateTo(
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .alpha(dotOpacity.value)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }
    }
}
