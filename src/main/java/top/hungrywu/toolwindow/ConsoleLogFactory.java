// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package top.hungrywu.toolwindow;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author daviswujiahao
 */

public class ConsoleLogFactory implements ToolWindowFactory {

    private static ConsoleView consoleView;
    private static ToolWindow toolWindow;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ConsoleLogFactory.toolWindow = toolWindow;
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public static void showToolWindow() {
        toolWindow.activate(null);
        toolWindow.show(null);
    }

    public static void addInfoLog(String formatter, Object... argc) {
        consoleView.print(MessageFormatter.arrayFormat(formatter, argc).getMessage(), ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public static void addErrorLog(String formatter, Object... argc) {
        consoleView.print(MessageFormatter.arrayFormat(formatter, argc).getMessage(), ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public static void clearLog() {
        consoleView.clear();
    }

}
