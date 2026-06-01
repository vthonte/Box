package com.google.ai.edge.gallery.runtime

import android.os.Process
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ThreadPriorityHints"

private fun isAppInForegroundNow(): Boolean {
  return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
}

private fun targetPriorityForCurrentAppState(): Int {
  return if (isAppInForegroundNow()) {
    Process.THREAD_PRIORITY_DEFAULT
  } else {
    Process.THREAD_PRIORITY_BACKGROUND
  }
}

suspend fun <T> runWithInferencePriorityHints(block: suspend () -> T): T {
  return withContext(Dispatchers.Default) {
    val tid = Process.myTid()
    val oldPriority =
      try {
        Process.getThreadPriority(tid)
      } catch (_: Exception) {
        Process.THREAD_PRIORITY_DEFAULT
      }
    val newPriority = targetPriorityForCurrentAppState()
    try {
      if (oldPriority != newPriority) {
        Process.setThreadPriority(tid, newPriority)
      }
      block()
    } catch (e: Exception) {
      throw e
    } finally {
      try {
        if (oldPriority != newPriority) {
          Process.setThreadPriority(tid, oldPriority)
        }
      } catch (e: Exception) {
        Log.w(TAG, "Failed to restore thread priority", e)
      }
    }
  }
}

