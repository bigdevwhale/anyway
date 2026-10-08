package io.cyberdise.anyway.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cyberdise.anyway.R
import kotlinx.coroutines.delay

private val QUESTIONS = listOf(R.string.worry_q1, R.string.worry_q2, R.string.worry_q3)
private const val INPUT = -1

/**
 * Runs a worry through three time horizons. The first "yes" decides the verdict;
 * three "no"s (or a "yes" at the cosmic scale) mean it's safe to let go.
 */
@Composable
fun WorryScreen(onAddToList: (String) -> Unit) {
    var worry by rememberSaveable { mutableStateOf("") }
    var step by rememberSaveable { mutableIntStateOf(INPUT) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var mattersAt by rememberSaveable { mutableStateOf<Int?>(null) }

    fun reset() {
        worry = ""
        step = INPUT
        finished = false
        mattersAt = null
    }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        ScreenHeader(stringResource(R.string.worry_title), stringResource(R.string.worry_sub))
        Spacer(Modifier.height(32.dp))

        when {
            step == INPUT -> {
                OutlinedTextField(
                    value = worry,
                    onValueChange = { worry = it },
                    placeholder = { Text(stringResource(R.string.worry_hint), color = Ash) },
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    stringResource(R.string.worry_go),
                    enabled = worry.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { step = 0 }
            }

            !finished -> {
                WorryCard(worry)
                Spacer(Modifier.height(36.dp))
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically { it / 4 }) togetherWith fadeOut(tween(150))
                    },
                    label = "question",
                ) { s ->
                    Column {
                        Text(stringResource(R.string.worry_step, s + 1), color = Ember, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(QUESTIONS[s]),
                            color = Bone,
                            fontSize = 28.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            mattersAt = step
                            finished = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Bone),
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) { Text(stringResource(R.string.yes), fontSize = 17.sp) }
                    PrimaryButton(stringResource(R.string.no), modifier = Modifier.weight(1f)) {
                        if (step < QUESTIONS.lastIndex) step++ else finished = true
                    }
                }
            }

            else -> Verdict(
                worry = worry,
                mattersAt = mattersAt,
                onAddToList = { onAddToList(worry) },
                onAgain = ::reset,
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun Verdict(worry: String, mattersAt: Int?, onAddToList: () -> Unit, onAgain: () -> Unit) {
    // Anything that only matters at cosmic scale doesn't really matter.
    val release = mattersAt == null || mattersAt == QUESTIONS.lastIndex
    var added by remember { mutableStateOf(false) }

    if (release) {
        val fade = remember { Animatable(1f) }
        LaunchedEffect(Unit) {
            delay(300)
            fade.animateTo(0f, tween(durationMillis = 1400, easing = FastOutSlowInEasing))
        }
        WorryCard(
            worry,
            Modifier.graphicsLayer {
                scaleX = fade.value
                scaleY = fade.value
                alpha = fade.value
                translationY = -(1 - fade.value) * 240f
                rotationZ = (1 - fade.value) * -8f
            },
        )
        AnimatedVisibility(visible = fade.value < 0.4f, enter = fadeIn(tween(600))) {
            Column {
                VerdictText(stringResource(if (mattersAt == null) R.string.worry_release else R.string.worry_cosmic))
                Spacer(Modifier.height(28.dp))
                PrimaryButton(stringResource(R.string.worry_again), modifier = Modifier.fillMaxWidth(), onClick = onAgain)
            }
        }
    } else {
        WorryCard(worry)
        Spacer(Modifier.height(28.dp))
        VerdictText(stringResource(if (mattersAt == 0) R.string.worry_real else R.string.worry_legacy))
        Spacer(Modifier.height(28.dp))
        PrimaryButton(
            stringResource(if (added) R.string.worry_added else R.string.worry_to_list),
            enabled = !added,
            modifier = Modifier.fillMaxWidth(),
        ) {
            onAddToList()
            added = true
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onAgain,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Bone),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(stringResource(R.string.worry_again), fontSize = 17.sp) }
    }
}

@Composable
private fun WorryCard(text: String, modifier: Modifier = Modifier) {
    Text(
        "“${text.trim()}”",
        color = Bone,
        fontSize = 19.sp,
        lineHeight = 26.sp,
        fontStyle = FontStyle.Italic,
        fontFamily = FontFamily.Serif,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Coal)
            .padding(20.dp),
    )
}

@Composable
private fun VerdictText(text: String) {
    Text(text, color = Bone, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black)
}

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Ember,
    unfocusedBorderColor = Smoke,
    cursorColor = Ember,
    focusedTextColor = Bone,
    unfocusedTextColor = Bone,
    focusedContainerColor = Coal,
    unfocusedContainerColor = Coal,
)
