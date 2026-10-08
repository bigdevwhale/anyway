package io.cyberdise.anyway.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cyberdise.anyway.AppState
import io.cyberdise.anyway.Life
import io.cyberdise.anyway.R
import java.text.NumberFormat
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
fun NowScreen(state: AppState, onSettings: () -> Unit) {
    val birth = state.birthDate ?: return
    val today = remember { LocalDate.now() }
    val left = Life.saturdaysLeft(birth, state.expectancy, today)
    val lived = Life.fractionLived(birth, state.expectancy, today)

    val resources = LocalContext.current.resources
    val question = remember(today) { Life.ofTheDay(resources.getStringArray(R.array.questions), today) }
    val quote = remember(today) { Life.ofTheDay(resources.getStringArray(R.array.quotes), today, salt = 3) }
    val nudges = remember { resources.getStringArray(R.array.nudges) }
    var nudge by remember { mutableStateOf<String?>(null) }

    val counter = remember { Animatable(0f) }
    LaunchedEffect(left) {
        counter.animateTo(left.toFloat(), tween(durationMillis = 1600, easing = FastOutSlowInEasing))
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Text(
                stringResource(R.string.wordmark),
                color = Ash,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onSettings) {
                Text(stringResource(R.string.now_settings), color = Ash, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(12.dp))
        if (left > 0) {
            Text(stringResource(R.string.now_prefix), color = Ash, fontSize = 18.sp)
            Text(
                NumberFormat.getIntegerInstance().format(counter.value.roundToLong()),
                color = Ember,
                fontSize = 92.sp,
                lineHeight = 96.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-3).sp,
            )
            Text(
                pluralStringResource(R.plurals.now_suffix, left.toInt()),
                color = Bone,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            Text(
                stringResource(R.string.now_bonus),
                color = Bone,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Black,
            )
        }

        Spacer(Modifier.height(28.dp))
        LinearProgressIndicator(
            progress = { lived },
            color = Ember,
            trackColor = Coal,
            gapSize = 0.dp,
            drawStopIndicator = {},
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.now_lived, (lived * 100).roundToInt()),
            color = Ash,
            fontSize = 13.sp,
        )

        Spacer(Modifier.height(28.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Coal)
                .padding(20.dp)
        ) {
            Text(
                stringResource(R.string.now_question_label).uppercase(),
                color = Ember,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(10.dp))
            Text(question, color = Bone, fontSize = 22.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(24.dp))
        Text(
            quote,
            color = Ash,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Serif,
        )

        Spacer(Modifier.height(20.dp))
        AnimatedContent(
            targetState = nudge,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
            label = "nudge",
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        ) { text ->
            Text(
                text ?: stringResource(R.string.anyway_hint),
                color = if (text == null) Ash else Bone,
                fontSize = if (text == null) 14.sp else 20.sp,
                lineHeight = 27.sp,
                fontWeight = if (text == null) FontWeight.Normal else FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AnywayButton(onClick = { nudge = nudges.filter { it != nudge }.random() })
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AnywayButton(onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "press")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(148.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(Ember)
            .clickable(interactionSource = interaction, indication = null) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
    ) {
        Text(
            stringResource(R.string.anyway_button),
            color = Ink,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
        )
    }
}
