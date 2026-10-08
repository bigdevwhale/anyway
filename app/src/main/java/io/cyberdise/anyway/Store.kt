package io.cyberdise.anyway

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Something to do before it's too late. */
data class Regret(val id: Long, val text: String, val createdAt: Long, val doneAt: Long?)

/** Thin wrapper over SharedPreferences — the app keeps very little. */
class Store(context: Context) {
    private val prefs = context.getSharedPreferences("anyway", Context.MODE_PRIVATE)

    var birthDate: LocalDate?
        get() = prefs.getString(KEY_BIRTH, null)?.let(LocalDate::parse)
        set(value) = prefs.edit().putString(KEY_BIRTH, value?.toString()).apply()

    var expectancy: Int
        get() = prefs.getInt(KEY_EXPECTANCY, 80)
        set(value) = prefs.edit().putInt(KEY_EXPECTANCY, value).apply()

    var nudgesEnabled: Boolean
        get() = prefs.getBoolean(KEY_NUDGES, true)
        set(value) = prefs.edit().putBoolean(KEY_NUDGES, value).apply()

    var lastNudge: LocalDate?
        get() = prefs.getString(KEY_LAST_NUDGE, null)?.let(LocalDate::parse)
        set(value) = prefs.edit().putString(KEY_LAST_NUDGE, value?.toString()).apply()

    var nextNudgeAt: Long
        get() = prefs.getLong(KEY_NEXT_NUDGE, 0)
        set(value) = prefs.edit().putLong(KEY_NEXT_NUDGE, value).apply()

    var regrets: List<Regret>
        get() {
            val json = JSONArray(prefs.getString(KEY_REGRETS, "[]"))
            return List(json.length()) { i ->
                val o = json.getJSONObject(i)
                Regret(
                    id = o.getLong("id"),
                    text = o.getString("text"),
                    createdAt = o.getLong("createdAt"),
                    doneAt = if (o.isNull("doneAt")) null else o.getLong("doneAt"),
                )
            }
        }
        set(value) {
            val json = JSONArray()
            value.forEach { r ->
                json.put(
                    JSONObject()
                        .put("id", r.id)
                        .put("text", r.text)
                        .put("createdAt", r.createdAt)
                        .put("doneAt", r.doneAt ?: JSONObject.NULL)
                )
            }
            prefs.edit().putString(KEY_REGRETS, json.toString()).apply()
        }

    private companion object {
        const val KEY_BIRTH = "birth"
        const val KEY_EXPECTANCY = "expectancy"
        const val KEY_NUDGES = "nudges"
        const val KEY_LAST_NUDGE = "last_nudge"
        const val KEY_NEXT_NUDGE = "next_nudge"
        const val KEY_REGRETS = "regrets"
    }
}

/** Observable app state for Compose; writes through to [Store]. */
class AppState(private val store: Store) {
    var birthDate by mutableStateOf(store.birthDate)
        private set
    var expectancy by mutableIntStateOf(store.expectancy)
        private set
    var nudgesEnabled by mutableStateOf(store.nudgesEnabled)
        private set
    var regrets by mutableStateOf(store.regrets)
        private set

    fun saveSetup(birth: LocalDate, years: Int, nudges: Boolean) {
        store.birthDate = birth
        store.expectancy = years
        store.nudgesEnabled = nudges
        birthDate = birth
        expectancy = years
        nudgesEnabled = nudges
    }

    fun add(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val id = (regrets.maxOfOrNull { it.id } ?: 0) + 1
        update(regrets + Regret(id, trimmed, System.currentTimeMillis(), null))
    }

    fun toggle(id: Long) = update(
        regrets.map {
            if (it.id != id) it
            else it.copy(doneAt = if (it.doneAt == null) System.currentTimeMillis() else null)
        }
    )

    fun remove(id: Long) = update(regrets.filterNot { it.id == id })

    private fun update(value: List<Regret>) {
        regrets = value
        store.regrets = value
    }
}
