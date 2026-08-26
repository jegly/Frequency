package com.jegly.frequency.utils

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.jegly.frequency.audio.ToneExporter
import com.jegly.frequency.model.ToneSessionParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ToneExportManager {

    private const val TAG = "ToneExportManager"

    sealed class ExportResult {
        data object Success : ExportResult()
        data object Cancelled : ExportResult()
        data class Failure(val message: String) : ExportResult()
    }

    suspend fun exportSession(
        context: Context,
        params: ToneSessionParams,
        durationSeconds: Int,
        fileName: String,
        onProgress: (Float) -> Unit,
        isCancelled: () -> Boolean
    ): ExportResult = withContext(Dispatchers.Default) {
        val safeName = fileName.trim().ifEmpty { "Frequency Session" }
            .replace("[\\\\/:*?\"<>|]".toRegex(), "_")
        val tempFile = File.createTempFile("tone_export", ".wav", context.cacheDir)

        try {
            val completed = ToneExporter.renderToWav(params, durationSeconds, tempFile, onProgress, isCancelled)
            if (!completed) {
                tempFile.delete()
                return@withContext ExportResult.Cancelled
            }

            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "$safeName.wav")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/Frequency")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext ExportResult.Failure("Could not create output file")

            resolver.openOutputStream(uri)?.use { out ->
                tempFile.inputStream().use { it.copyTo(out) }
            } ?: return@withContext ExportResult.Failure("Could not open output stream")

            values.clear()
            values.put(MediaStore.Audio.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            ExportResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "exportSession failed", e)
            ExportResult.Failure(e.message ?: "Export failed")
        } finally {
            tempFile.delete()
        }
    }
}
