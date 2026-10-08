package io.cyberdise.anyway.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cyberdise.anyway.AppState
import io.cyberdise.anyway.R
import io.cyberdise.anyway.Regret
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun BeforeScreen(state: AppState) {
    var draft by rememberSaveable { mutableStateOf("") }
    val today = remember { LocalDate.now() }
    val submit = {
        state.add(draft)
        draft = ""
    }
    // Open items first, newest on top; finished ones sink to the bottom.
    val sorted = state.regrets.sortedWith(compareBy<Regret> { it.doneAt != null }.thenByDescending { it.createdAt })

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        item {
            ScreenHeader(stringResource(R.string.before_title), stringResource(R.string.before_sub))
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text(stringResource(R.string.before_hint), color = Ash) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                PrimaryButton(stringResource(R.string.before_add), enabled = draft.isNotBlank(), onClick = submit)
            }
            Spacer(Modifier.height(16.dp))
        }

        if (sorted.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.before_empty),
                    color = Ash,
                    fontSize = 16.sp,
                    lineHeight = 23.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

        items(sorted, key = { it.id }) { regret ->
            RegretRow(
                regret = regret,
                today = today,
                onToggle = { state.toggle(regret.id) },
                onRemove = { state.remove(regret.id) },
                modifier = Modifier.animateItem(),
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun RegretRow(
    regret: Regret,
    today: LocalDate,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val done = regret.doneAt != null
    val added = Instant.ofEpochMilli(regret.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
    val days = ChronoUnit.DAYS.between(added, today).toInt()

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 14.dp)) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (done) Ash else Ember, CircleShape)
                    .background(if (done) Ash else Ink)
                    .clickable(onClick = onToggle)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    regret.text,
                    color = if (done) Ash else Bone,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    when {
                        done -> stringResource(R.string.before_done)
                        days <= 0 -> stringResource(R.string.before_today)
                        else -> pluralStringResource(R.plurals.before_waiting, days, days)
                    },
                    color = if (!done && days >= 7) Ember else Ash,
                    fontSize = 13.sp,
                )
            }
            val removeLabel = stringResource(R.string.before_remove)
            TextButton(onClick = onRemove, modifier = Modifier.semantics { contentDescription = removeLabel }) {
                Text("×", color = Ash, fontSize = 22.sp)
            }
        }
        HorizontalDivider(color = Coal)
    }
}
