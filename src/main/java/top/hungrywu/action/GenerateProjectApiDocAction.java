package top.hungrywu.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import top.hungrywu.bean.ApiDoc;
import top.hungrywu.exception.BizException;
import top.hungrywu.exception.BizExceptionEnum;
import top.hungrywu.resolver.ApiResolver;
import top.hungrywu.service.KiwiService;
import top.hungrywu.toolwindow.ConsoleLogFactory;

import java.util.Objects;

/***
 *
 * 在项目根目录上一键生成项目中所有api文档的动作响应类
 * @author : daviswujiahao
 * @date : 2020/2/27 2:18 下午
 *
 **/
@Slf4j
public class GenerateProjectApiDocAction extends AnAction {
    /**
     * 设置选择项目目录时才会显示生成api文档的选项
     * @param event
     */
    @Override
    public void update(@NotNull AnActionEvent event) {

        Project currentProject = event.getProject();
        if (currentProject == null) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        // 获得点击时被选择的文件
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        // 设置只有在本工程的主项目目录上才显示生成api文档的选项
        String currentFilePath = file.getPath();
        if (Objects.equals(currentProject.getBasePath(), currentFilePath)) {
            event.getPresentation().setEnabledAndVisible(true);
        } else {
            event.getPresentation().setEnabledAndVisible(false);
        }

    }

    @Override
    public void actionPerformed(AnActionEvent event) {

        event.getPresentation().setEnabledAndVisible(false);
        ConsoleLogFactory.showToolWindow(event.getProject());
        ConsoleLogFactory.clearLog();

        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runReadAction(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ApiDoc apiDoc = ApiResolver.buildProjectApiDoc(event.getProject());
                            if (Objects.isNull(apiDoc)) {
                                throw new BizException(BizExceptionEnum.UNKNOWN_EXCEPTION);
                            }
                            KiwiService kiwiService = new KiwiService();
                            kiwiService.buildApiDocOnWiki(apiDoc);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            event.getPresentation().setEnabledAndVisible(true);
                        }
                    }
                });
            }
        });

    }
}
