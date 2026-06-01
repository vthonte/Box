package com.box.gallery

import android.content.Context
import android.os.Debug
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ProcessDeathTracker {
  private const val PREFS = "box_process_tracker"
  private const val KEY_RUNNING = "running"
  private const val KEY_LAST_START = "last_start_ms"
  private const val KEY_LAST_REPORT = "last_report"
  private const val REPORT_FILE = "last_process_report.txt"

  private fun nowStr(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

  fun onAppCreate(context: Context): String? {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val wasRunning = prefs.getBoolean(KEY_RUNNING, false)
    val lastStart = prefs.getLong(KEY_LAST_START, 0L)
    val existingReport = prefs.getString(KEY_LAST_REPORT, null)

    val report =
      if (wasRunning) {
        buildString {
          append("Previous run ended unexpectedly.\n")
          append("Detected at: ").append(nowStr()).append('\n')
          if (lastStart > 0L) append("Previous start (ms): ").append(lastStart).append('\n')
          append("Java heap (MB): ")
            .append((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024))
            .append('\n')
          append("Native heap allocated (MB): ")
            .append(Debug.getNativeHeapAllocatedSize() / (1024 * 1024))
            .append('\n')
          append("Hint: this can be caused by system force-kill under memory/GPU pressure.")
        }
      } else {
        existingReport
      }

    if (!report.isNullOrBlank()) {
      prefs.edit().putString(KEY_LAST_REPORT, report).apply()
      writeReportFile(context, report)
    }

    prefs.edit().putBoolean(KEY_RUNNING, true).putLong(KEY_LAST_START, System.currentTimeMillis()).apply()
    return report
  }

  fun onAppStoppedNormally(context: Context) {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_RUNNING, false).apply()
  }

  fun consumeReport(context: Context): String? {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val report = prefs.getString(KEY_LAST_REPORT, null)
    if (!report.isNullOrBlank()) {
      prefs.edit().remove(KEY_LAST_REPORT).apply()
    }
    return report
  }

  private fun writeReportFile(context: Context, report: String) {
    runCatching {
      File(context.filesDir, REPORT_FILE).writeText(report)
    }
  }
}

