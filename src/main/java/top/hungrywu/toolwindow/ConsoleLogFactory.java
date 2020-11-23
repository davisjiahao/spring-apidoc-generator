// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package top.hungrywu.toolwindow;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.apache.commons.lang.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.MessageFormatter;

import java.util.Date;
import java.util.Objects;

/**
 * @author daviswujiahao
 */

public class ConsoleLogFactory implements ToolWindowFactory {

    private static ConsoleView consoleView;
    private static ToolWindow toolWindow;

    private static final String LOG_TIME_FORMATTER = "yyyy-MM-dd mm:ss.SSS";

    private static void initToolWindowAndConsoleView(@NotNull Project project) {
        if (Objects.isNull(ConsoleLogFactory.toolWindow)) {
            ConsoleLogFactory.toolWindow = ToolWindowManager.getInstance(project).getToolWindow("apiDocToolWindow");
        }
        if (Objects.isNull(ConsoleLogFactory.consoleView)) {
            ConsoleLogFactory.consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            Content content =  ConsoleLogFactory.toolWindow.getContentManager().getFactory().createContent(ConsoleLogFactory.consoleView.getComponent(), "", false);
            ConsoleLogFactory.toolWindow.getContentManager().addContent(content);
        }
    }

    public static boolean logConsoleIsReady() {
        return Objects.nonNull(consoleView);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        initToolWindowAndConsoleView(project);
    }

    public static void showToolWindow(@NotNull Project project) {
        initToolWindowAndConsoleView(project);
        ConsoleLogFactory.toolWindow.activate(null);
        ConsoleLogFactory.toolWindow.show(null);
    }

    public static void addInfoLog(String formatter, Object... argc) {
        consoleView.print(DateFormatUtils.format(new Date(), LOG_TIME_FORMATTER)
                        + ":INFO:" + MessageFormatter.arrayFormat(formatter, argc).getMessage() + "\n",
                ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public static void addErrorLog(String formatter, Object... argc) {
        consoleView.print(DateFormatUtils.format(new Date(), LOG_TIME_FORMATTER)
                + ":ERROR:" + MessageFormatter.arrayFormat(formatter, argc).getMessage() + "\n",
                ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public static void clearLog() {
        consoleView.clear();
    }

}
