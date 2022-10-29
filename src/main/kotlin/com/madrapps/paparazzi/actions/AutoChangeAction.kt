package com.madrapps.paparazzi.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.madrapps.paparazzi.service.service

class AutoChangeAction : ToggleAction() {

    companion object {
        const val ID = "com.madrapps.paparazzi.actions.AutoChangeAction"
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        return project.service.settings.isAutoChangeEnabled
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project
        if (project != null) {
            project.service.settings.isAutoChangeEnabled = state
        }
    }
}