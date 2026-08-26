package com.jegly.frequency.utils

import android.content.Context
import android.util.Log
import com.jegly.frequency.audio.ToneGenerator
import com.jegly.frequency.model.CustomPreset
import com.jegly.frequency.model.ToneSessionParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

object CustomPresetStorageManager {

    private const val TAG = "CustomPresetStorage"
    private const val CUSTOM_PRESETS_FILE = "CustomTonePresets.json"

    suspend fun getCustomPresets(context: Context): List<CustomPreset> = withContext(Dispatchers.IO) {
        loadAllSync(context)
    }

    suspend fun saveCustomPreset(context: Context, preset: CustomPreset) = withContext(Dispatchers.IO) {
        synchronized(CUSTOM_PRESETS_FILE) {
            val current = loadAllSync(context).toMutableList()
            current.add(preset)
            writeAll(context, current)
        }
    }

    suspend fun deleteCustomPreset(context: Context, id: String) = withContext(Dispatchers.IO) {
        synchronized(CUSTOM_PRESETS_FILE) {
            val current = loadAllSync(context).filterNot { it.id == id }
            writeAll(context, current)
        }
    }

    private fun loadAllSync(context: Context): List<CustomPreset> {
        val f = File(context.filesDir, CUSTOM_PRESETS_FILE)
        if (!f.exists()) return emptyList()
        val list = mutableListOf<CustomPreset>()
        try {
            val json = readFile(f) ?: return emptyList()
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                list.add(presetFromJson(arr.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadAllSync failed", e)
        }
        return list
    }

    private fun writeAll(context: Context, list: List<CustomPreset>) {
        val f = File(context.filesDir, CUSTOM_PRESETS_FILE)
        val arr = JSONArray()
        for (p in list) {
            try {
                arr.put(presetToJson(p))
            } catch (e: JSONException) {
                Log.e(TAG, "serialise error", e)
            }
        }
        try {
            writeJson(f, arr.toString())
        } catch (e: IOException) {
            Log.e(TAG, "write error", e)
        }
    }

    private fun presetToJson(p: CustomPreset): JSONObject {
        val params = p.params
        val o = JSONObject()
        o.put("id", p.id)
        o.put("name", p.name)
        o.put("frequency", params.frequency)
        o.put("waveform", params.waveform.name)
        o.put("mode", params.mode.name)
        o.put("beatFreq", params.beatFreq)
        o.put("mixFreq", params.mixFreq)
        o.put("sweepStart", params.sweepStart)
        o.put("sweepEnd", params.sweepEnd)
        o.put("sweepSpeed", params.sweepSpeed)
        o.put("sweepRandom", params.sweepRandom)
        o.put("endLfoOn", params.endLfoOn)
        o.put("endLfoSpeed", params.endLfoSpeed)
        o.put("endLfoDepth", params.endLfoDepth)
        o.put("volume", params.volume)
        o.put("whiteNoiseOn", params.whiteNoiseOn)
        o.put("pinkNoiseOn", params.pinkNoiseOn)
        o.put("brownNoiseOn", params.brownNoiseOn)
        o.put("isochronicOn", params.isochronicOn)
        o.put("isochronicRate", params.isochronicRate)
        o.put("isochronicSmooth", params.isochronicSmooth)
        o.put("toneEnabled", params.toneEnabled)
        o.put("filterOn", params.filterOn)
        o.put("filterCutoff", params.filterCutoff)
        o.put("filterResonance", params.filterResonance)
        o.put("adsrOn", params.adsrOn)
        o.put("adsrAttack", params.adsrAttack)
        o.put("adsrDecay", params.adsrDecay)
        o.put("adsrSustain", params.adsrSustain)
        o.put("adsrRelease", params.adsrRelease)
        return o
    }

    private fun presetFromJson(o: JSONObject): CustomPreset {
        val params = ToneSessionParams(
            frequency = o.getDouble("frequency"),
            waveform = ToneGenerator.Waveform.valueOf(o.optString("waveform", ToneGenerator.Waveform.SINE.name)),
            mode = ToneGenerator.PlayMode.valueOf(o.optString("mode", ToneGenerator.PlayMode.NORMAL.name)),
            beatFreq = o.optDouble("beatFreq", 4.0),
            mixFreq = o.optDouble("mixFreq", 528.0),
            sweepStart = o.optDouble("sweepStart", 200.0),
            sweepEnd = o.optDouble("sweepEnd", 1000.0),
            sweepSpeed = o.optDouble("sweepSpeed", 0.1),
            sweepRandom = o.optDouble("sweepRandom", 0.3),
            endLfoOn = o.optBoolean("endLfoOn", false),
            endLfoSpeed = o.optDouble("endLfoSpeed", 0.2),
            endLfoDepth = o.optDouble("endLfoDepth", 0.3),
            volume = o.optDouble("volume", 1.0).toFloat(),
            whiteNoiseOn = o.optBoolean("whiteNoiseOn", false),
            pinkNoiseOn = o.optBoolean("pinkNoiseOn", false),
            brownNoiseOn = o.optBoolean("brownNoiseOn", false),
            isochronicOn = o.optBoolean("isochronicOn", false),
            isochronicRate = o.optDouble("isochronicRate", 7.83),
            isochronicSmooth = o.optBoolean("isochronicSmooth", false),
            toneEnabled = o.optBoolean("toneEnabled", true),
            filterOn = o.optBoolean("filterOn", false),
            filterCutoff = o.optDouble("filterCutoff", 2000.0),
            filterResonance = o.optDouble("filterResonance", 0.3),
            adsrOn = o.optBoolean("adsrOn", false),
            adsrAttack = o.optDouble("adsrAttack", 0.05),
            adsrDecay = o.optDouble("adsrDecay", 0.20),
            adsrSustain = o.optDouble("adsrSustain", 0.80),
            adsrRelease = o.optDouble("adsrRelease", 0.30)
        )
        return CustomPreset(
            id = o.getString("id"),
            name = o.optString("name", "Preset"),
            params = params
        )
    }

    private fun writeJson(file: File, json: String) {
        BufferedWriter(FileWriter(file, false)).use { it.write(json) }
    }

    private fun readFile(file: File): String? {
        val sb = StringBuilder()
        BufferedReader(FileReader(file)).use { r ->
            var line: String?
            while (r.readLine().also { line = it } != null) sb.append(line)
        }
        return if (sb.isNotEmpty()) sb.toString() else null
    }
}
