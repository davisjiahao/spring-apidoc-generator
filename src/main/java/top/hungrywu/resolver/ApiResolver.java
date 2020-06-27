package top.hungrywu.resolver;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import lombok.extern.log4j.Log4j;
import top.hungrywu.bean.ApiDetail;
import top.hungrywu.bean.ApiDoc;
import top.hungrywu.config.KiwiConfig;
import top.hungrywu.config.RequestConfig;
import top.hungrywu.config.ResponseConfig;
import top.hungrywu.helper.PsiTypeResolverHelper;
import top.hungrywu.util.GitUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * @Description 解析api接口的服务类
 * @Author daviswujiahao
 * @Date 2020/2/27 6:55 下午
 * @Version 1.0
 **/
@Log4j
public class ApiResolver {

    private final static BaseResolver baseResolver =  new SpringControllerResolver();

    /**
     * 为指定工程构建api文档
     * @param project
     * @return
     */
    public static ApiDoc buildProjectApiDoc(Project project) {
        if (!validConfig()) {
            return null;
        }
        ApiDoc apiDoc = new ApiDoc().
                setProjectName(project.getName()).
                setApiDeployHost("127.0.0.1").
                setApiDeployPort(8080);

        GitUtils.VersionInfoByGit versionInfoByGit = GitUtils.getVersionInfoByGit(project);
        if (!Objects.isNull(versionInfoByGit)) {
            apiDoc.setBranchName(versionInfoByGit.getBranchName())
                    .setCommitVersion(versionInfoByGit.getCommitId());
        }

        List<ApiDetail> apiDetails = resolveAllApiOfProject(project);

        apiDoc.setApiDetails(apiDetails);

        PsiTypeResolverHelper.clearResolvedInfoCache();

        return apiDoc;
    }

    /**
     * 获取单个方法的接口文档描述信息
     * @param psiMethod
     * @return
     */
    public static ApiDetail buildMethodApi(PsiMethod psiMethod) {
        if (!validConfig()) {
            return null;
        }
        if (Objects.isNull(psiMethod)) {
            return null;
        }
        return baseResolver.getApiOfMethod(psiMethod);
    }

    /**
     * 获取单个class文件下的接口文档描述信息
     * @author : daviswujiahao 
     * @date : 2020/6/27 11:10 上午
     * @param psiClass :  
     * @return : java.util.List<top.hungrywu.bean.ApiDetail>
     **/
    public static List<ApiDetail> buildApiInClass(PsiClass psiClass) {
        if (!validConfig()) {
            return null;
        }
        if (Objects.isNull(psiClass)) {
            return Collections.emptyList();
        }
        return baseResolver.getAllApiInClass(psiClass);
    }

    private static boolean validConfig() {
        if (!KiwiConfig.validConfig()) {
            return false;
        }
        if (!RequestConfig.validConfig()) {
            return false;
        }
        if (!ResponseConfig.validConfig()) {
            return false;
        }
        return true;
    }

    /**
     * 获取指定工程下的所有api描述信息列表
     * @param project
     * @return
     */
    private static List<ApiDetail> resolveAllApiOfProject(Project project) {
        if (Objects.isNull(project)) {
            return Collections.emptyList();
        }
        return baseResolver.getAllApiInProject(project);
    }


    public static boolean isClassContainApi(PsiClass psiClass) {
        if (Objects.isNull(psiClass)) {
            return false;
        }
        return baseResolver.isClassContainApi(psiClass);
    }

    public static boolean isMethodApi(PsiMethod psiMethod) {
        if (Objects.isNull(psiMethod)) {
            return false;
        }
        return baseResolver.isMethodApi(psiMethod);
    }
}
