package top.hungrywu.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiMethodImpl;
import org.jetbrains.annotations.NotNull;
import top.hungrywu.bean.ApiDetail;
import top.hungrywu.resolver.ApiResolver;
import top.hungrywu.service.KiwiService;
import top.hungrywu.service.MarkdownService;
import top.hungrywu.toolwindow.ConsoleLogFactory;
import top.hungrywu.util.GitUtils;

import java.util.Objects;

/**
 * @author daivswujiahao
 */
public class GenerateApiDoc2MD extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project currentProject = event.getProject();
        if (currentProject == null) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }
        PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (!(psiElement instanceof PsiMethodImpl)) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        PsiClass containingClass = ((PsiMethodImpl) psiElement).getContainingClass();
        if (!ApiResolver.isClassContainApi(containingClass)) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        if (!ApiResolver.isMethodApi((PsiMethod) psiElement)) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (!(psiElement instanceof PsiMethodImpl)) {
            return;
        }

        PsiClass containingClass = ((PsiMethodImpl) psiElement).getContainingClass();
        if (!ApiResolver.isClassContainApi(containingClass)) {
            return;
        }

        if (!ApiResolver.isMethodApi((PsiMethod) psiElement)) {
            return;
        }


        event.getPresentation().setEnabledAndVisible(false);
        Project project = event.getProject();
        ConsoleLogFactory.showToolWindow(project);
        ConsoleLogFactory.clearLog();

        GitUtils.VersionInfoByGit versionInfoByGit = GitUtils.getVersionInfoByGit(project);
        String projectBasePath = project.getBasePath();


        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runReadAction(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ApiDetail apiDetail = ApiResolver.buildMethodApi((PsiMethod) psiElement);
                            if (Objects.isNull(apiDetail)) {
                                return;
                            }
                            MarkdownService markdownService = new MarkdownService();
                            markdownService.buildOneApiDetail(apiDetail, versionInfoByGit, projectBasePath);
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
