package com.businessempire.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.businessempire.game.domain.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.saveStore: DataStore<Preferences> by preferencesDataStore(name = "business_empire_save")

/** Stores the single JSON save blob in DataStore. */
class SaveRepository(private val context: Context) {
    private val key = stringPreferencesKey("save_json")

    suspend fun load(): GameState? = withContext(Dispatchers.IO) {
        val text = context.saveStore.data.first()[key] ?: return@withContext null
        SaveCodec.decode(text)
    }

    suspend fun save(state: GameState) {
        val text = withContext(Dispatchers.Default) { SaveCodec.encode(state) }
        withContext(Dispatchers.IO) {
            context.saveStore.edit { it[key] = text }
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        context.saveStore.edit { it.remove(key) }
    }
}
