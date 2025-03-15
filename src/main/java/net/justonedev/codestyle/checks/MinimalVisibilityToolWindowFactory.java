package net.justonedev.codestyle.checks;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class MinimalVisibilityToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Create the main panel for our tool window
        MinimalVisibilityToolWindowPanel panel = new MinimalVisibilityToolWindowPanel(project);

        // Wrap it in IntelliJ’s content system
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel.getMainComponent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
