package com.aiope2.core.network

import android.content.Context
import org.json.JSONObject

/**
 * Task-based model routing. Each task can use a different provider profile.
 * Falls back to the active profile if no task-specific override is set.
 */
enum class ModelTask(val id: String, val label: String, val description: String) {
  CHAT("chat", "Chat", "General conversation and coding assistance"),
  AGENT("agent", "Agent / Tool Use", "Agentic tasks with tool calling (run_sh, read_file, etc)"),
  TITLE("title", "Title Generation", "Generate conversation titles from first message"),
  COMPACT("compact", "Compact / Summary", "Summarize conversation context"),
  VISION("vision", "Vision", "Image/video understanding"),
}

data class TaskModelConfig(
  val taskId: String,
  val profileId: String? = null, // null = use active profile
  val modelId: String? = null    // null = use profile's selected model
)

class TaskModelStore(context: Context) {
  private val prefs = context.getSharedPreferences("task_models", Context.MODE_PRIVATE)

  fun getTaskConfig(task: ModelTask): TaskModelConfig {
    val json = prefs.getString("task_${task.id}", null) ?: return TaskModelConfig(task.id)
    return try {
      val j = JSONObject(json)
      TaskModelConfig(
        taskId = task.id,
        profileId = j.optString("profileId", "").ifBlank { null },
        modelId = j.optString("modelId", "").ifBlank { null }
      )
    } catch (_: Exception) { TaskModelConfig(task.id) }
  }

  fun setTaskConfig(task: ModelTask, config: TaskModelConfig) {
    val j = JSONObject().apply {
      config.profileId?.let { put("profileId", it) }
      config.modelId?.let { put("modelId", it) }
    }
    prefs.edit().putString("task_${task.id}", j.toString()).apply()
  }

  fun clearTaskConfig(task: ModelTask) {
    prefs.edit().remove("task_${task.id}").apply()
  }

  /** Resolve which profile + model to use for a given task */
  fun resolve(task: ModelTask, providerStore: Any): Pair<String?, String?> {
    val tc = getTaskConfig(task)
    return tc.profileId to tc.modelId
  }
}
