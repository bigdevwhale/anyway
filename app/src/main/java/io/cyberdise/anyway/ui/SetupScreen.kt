package io.cyberdise.anyway.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cyberdise.anyway.AppState
import io.cyberdise.anyway.Locales
import io.cyberdise.anyway.Nudges
import io.cyberdise.anyway.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(state: AppState, isFirstRun: Boolean, onDone: () -> Unit) {
    val context = LocalContext.current
    // Saveable: switching language recreates the activity mid-setup.
    var birth by rememberSaveable { mutableStateOf(state.birthDate) }
    var years by rememberSaveable { mutableFloatStateOf(state.expectancy.toFloat()) }
    var nudges by rememberSaveable { mutableStateOf(state.nudgesEnabled) }
    var picking by remember { mutableStateOf(false) }

    BackHandler(enabled = !isFirstRun, onBack = onDone)

    val finish = {
        Nudges.schedule(context)
        onDone()
    }
    val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        finish()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Ink)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        LanguagePicker()
        Spacer(Modifier.height(40.dp))
        Text(
            stringResource(R.string.onb_title),
            color = Bone,
            fontSize = 44.sp,
            lineHeight = 48.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.onb_sub), color = Ash, fontSize = 18.sp, lineHeight = 25.sp)

        Spacer(Modifier.height(48.dp))
        Label(stringResource(R.string.onb_birth))
        OutlinedButton(
            onClick = { picking = true },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Bone),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text(
                birth?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(LocalConfiguration.current.locales[0]))
                    ?: stringResource(R.string.onb_pick),
                fontSize = 17.sp,
            )
        }

        Spacer(Modifier.height(32.dp))
        Label(pluralStringResource(R.plurals.onb_expectancy, years.roundToInt(), years.roundToInt()))
        Slider(
            value = years,
            onValueChange = { years = it },
            valueRange = 50f..110f,
            steps = 59,
            colors = SliderDefaults.colors(
                thumbColor = Ember,
                activeTrackColor = Ember,
                inactiveTrackColor = Coal,
                activeTickColor = Ember,
                inactiveTickColor = Coal,
            ),
        )

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.onb_nudges),
                color = Bone,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f).padding(end = 16.dp),
            )
            Switch(
                checked = nudges,
                onCheckedChange = { nudges = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Ink,
                    checkedTrackColor = Ember,
                    uncheckedThumbColor = Ash,
                    uncheckedTrackColor = Coal,
                    uncheckedBorderColor = Smoke,
                ),
            )
        }

        Spacer(Modifier.height(48.dp))
        PrimaryButton(
            text = stringResource(if (isFirstRun) R.string.onb_go else R.string.onb_save),
            enabled = birth != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            state.saveSetup(birth!!, years.roundToInt(), nudges)
            val needsPermission = nudges && Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            if (needsPermission) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS) else finish()
        }
    }

    if (picking) {
        val today = LocalDate.now()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (birth ?: today.minusYears(30))
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            yearRange = 1900..today.year,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= System.currentTimeMillis()
            },
        )
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        birth = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    picking = false
                }) { Text(stringResource(android.R.string.ok), color = Ember) }
            },
            dismissButton = {
                TextButton(onClick = { picking = false }) {
                    Text(stringResource(android.R.string.cancel), color = Ash)
                }
            },
        ) {
            DatePicker(pickerState)
        }
    }
}

@Composable
private fun LanguagePicker() {
    val activity = LocalActivity.current ?: return
    var current by remember { mutableStateOf(Locales.current(activity)) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Locales.supported.forEach { tag ->
            val selected = tag == current
            Text(
                when (tag) {
                    Locales.SYSTEM -> stringResource(R.string.lang_system)
                    "ru" -> "Русский"
                    else -> "English"
                },
                color = if (selected) Ink else Bone,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) Ember else Coal)
                    .clickable {
                        current = tag
                        Locales.set(activity, tag)
                    }
                    .padding(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text.uppercase(),
        color = Ash,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}
