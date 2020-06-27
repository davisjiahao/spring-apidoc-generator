package top.hungrywu.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import top.hungrywu.bean.ApiDetail;
import top.hungrywu.bean.ApiDoc;
import top.hungrywu.exception.BizException;
import top.hungrywu.exception.BizExceptionEnum;
import top.hungrywu.resolver.ApiResolver;
import top.hungrywu.service.KiwiService;
import top.hungrywu.toolwindow.ConsoleLogFactory;

import java.util.List;
import java.util.Objects;

/***
 *
 * 在项目根目录上一键生成项目中所有api文档的动作响应类
 * @author : daviswujiahao
 * @date : 2020/2/27 2:18 下午
 *
 **/
@Slf4j
public class GenerateClassApiDescsAction extends AnAction {
    /**
     * 设置选择class文件时才会显示生成api文档的选项
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
        PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement == null || !(psiElement instanceof PsiClass)) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        if (!ApiResolver.isClassContainApi((PsiClass) psiElement)) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }


    }

    @Override
    public void actionPerformed(AnActionEvent event) {

        event.getPresentation().setEnabledAndVisible(false);
        ConsoleLogFactory.showToolWindow(event.getProject());
        ConsoleLogFactory.clearLog();

        // 获得点击时被选择的文件
        PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement == null || !(psiElement instanceof PsiClass)) {
            return;
        }

        if (!ApiResolver.isClassContainApi((PsiClass) psiElement)) {
            return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> ApplicationManager.getApplication().runReadAction(() -> {
            try {
                List<ApiDetail> apiDetails = ApiResolver.buildApiInClass((PsiClass) psiElement);
                if (Objects.isNull(apiDetails)) {
                    return;
                }
                KiwiService kiwiService = new KiwiService();
                kiwiService.buildApiDescsOnWiki(apiDetails);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                event.getPresentation().setEnabledAndVisible(true);
            }
        }));

    }
}
