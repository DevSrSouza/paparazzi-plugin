package com.madrapps.paparazzi

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH
import com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED
import com.intellij.uiDesigner.core.GridLayoutManager
import com.madrapps.paparazzi.actions.*
import com.madrapps.paparazzi.service.service
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import java.awt.*
import javax.swing.*
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
import javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED

class PaparazziWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        MyPanel(toolWindow, project)
    }
}

class MyPanel(toolWindow: ToolWindow, project: Project) : SimpleToolWindowPanel(true, true), PaparazziWindowPanel {

    override val list: JPanel = JPanel()

    init {
        val content = ContentFactory.SERVICE.getInstance().createContent(this, "", false)
        toolWindow.contentManager.addContent(content)

        val boxLayout = BoxLayout(list, BoxLayout.Y_AXIS)
        list.layout = boxLayout
        list.border = BorderFactory.createEmptyBorder(32, 32, 32, 32)

        setContent(getContentPanel(project))
        project.service.panel = this
    }

    private fun getContentPanel(project: Project): JPanel {

        val panel = JBPanel<SimpleToolWindowPanel>(
            GridLayoutManager(
                2, 1, Insets(0, 0, 0, 0), 0, 0
            )
        )

        val toolbar = JPanel(BorderLayout())
        initToolbar(toolbar)

        panel.add(toolbar, GridConstraints().apply {
            row = 0
            fill = FILL_BOTH
            vSizePolicy = SIZEPOLICY_FIXED
        })

        val modules = project.allModules()
        val sourceRoots = modules[0].rootManager.sourceRoots

        val test = modules[90].rootManager.contentRoots.find {
            it.path.endsWith("src/test")
        }

        val model = DefaultListModel<Item>()
        val jbList = object : JBList<Item>(model) {
            override fun getScrollableUnitIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int): Int {
                return 30
            }
        }

        jbList.cellRenderer = Renderer(project)


        if (test != null) {
            val children = test.children[1].children[0].children
            children.take(25).forEach { child ->
                model.addElement(Item(child))
            }
        }

        val jbScrollPane = JBScrollPane(
            jbList, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        panel.add(jbScrollPane, GridConstraints().apply {
            row = 1
            fill = FILL_BOTH
        })

        return panel
    }

    private fun initToolbar(toolbar: JPanel) {
        val manager = ActionManager.getInstance()
        val refreshAction = manager.getAction(RefreshAction.ID)
        val zoomInAction = manager.getAction(ZoomInAction.ID)
        val zoomOutAction = manager.getAction(ZoomOutAction.ID)
        val actualSizeAction = manager.getAction(ActualSizeAction.ID)
        val fitZoomToWindowAction = manager.getAction(FitZoomToWindowAction.ID)

        val toolbarActionGroup = DefaultActionGroup().apply {
            add(refreshAction)
            addSeparator()
            add(zoomInAction)
            add(zoomOutAction)
            add(actualSizeAction)
            add(fitZoomToWindowAction)
        }

        val actionToolbar = manager.createActionToolbar(ActionPlaces.TOOLWINDOW_TITLE, toolbarActionGroup, true)
        actionToolbar.targetComponent = toolbar
        toolbar.add(actionToolbar.component)
    }
}

interface PaparazziWindowPanel {

    val list: JPanel
}

data class Item(val file: VirtualFile)

class Renderer(private val project: Project) : ListCellRenderer<Item> {
    override fun getListCellRendererComponent(
        list: JList<out Item>?,
        value: Item,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val image = project.service.image(value)
        val jLabel = JLabel(ImageIcon(image))
        jLabel.border = BorderFactory.createEmptyBorder(32, 32, 32, 32)
        jLabel.isEnabled = false
        return jLabel
    }
}
