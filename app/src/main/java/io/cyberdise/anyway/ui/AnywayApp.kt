package io.cyberdise.anyway.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cyberdise.anyway.AppState
import io.cyberdise.anyway.R

enum class Tab(@param:StringRes val label: Int) {
    Now(R.string.tab_now),
    Worry(R.string.tab_worry),
    Before(R.string.tab_before),
}

@Composable
fun AnywayApp(state: AppState) {
    var editing by rememberSaveable { mutableStateOf(false) }
    if (state.birthDate == null || editing) {
        SetupScreen(state, isFirstRun = state.birthDate == null, onDone = { editing = false })
        return
    }

    var tab by rememberSaveable { mutableStateOf(Tab.Now) }
    Scaffold(
        containerColor = Ink,
        bottomBar = { TabBar(tab, onSelect = { tab = it }) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                Tab.Now -> NowScreen(state, onSettings = { editing = true })
                Tab.Worry -> WorryScreen(onAddToList = state::add)
                Tab.Before -> BeforeScreen(state)
            }
        }
    }
}

@Composable
private fun TabBar(current: Tab, onSelect: (Tab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Ink)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Tab.entries.forEach { tab ->
            val selected = tab == current
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    stringResource(tab.label).uppercase(),
                    color = if (selected) Bone else Ash,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .size(width = 20.dp, height = 2.dp)
                        .background(if (selected) Ember else Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String) {
    Column {
        Spacer(Modifier.height(24.dp))
        Text(title, color = Bone, fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = Ash, fontSize = 16.sp, lineHeight = 22.sp)
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Ember,
            contentColor = Ink,
            disabledContainerColor = Coal,
            disabledContentColor = Ash,
        ),
        modifier = modifier.height(56.dp),
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}
