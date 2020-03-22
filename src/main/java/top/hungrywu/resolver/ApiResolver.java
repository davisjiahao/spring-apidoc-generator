package top.hungrywu.resolver;

import com.intellij.openapi.project.Project;
import lombok.extern.log4j.Log4j;
import top.hungrywu.bean.ApiDetail;
import top.hungrywu.bean.ApiDoc;
import top.hungrywu.helper.PsiTypeResolverHelper;

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
    /**
     * 为指定工程构建api文档
     * @param project
     * @return
     */
    public static ApiDoc buildApiDoc(Project project) {
        ApiDoc apiDoc = new ApiDoc().
                setProjectName(project.getName()).
                setApiDeployHost("127.0.0.1").
                setApiDeployPort(8080);

        List<ApiDetail> apiDetails = resolveAllApi(project);

        apiDoc.setApiDetails(apiDetails);

        log.info(apiDetails);

        PsiTypeResolverHelper.clearResolvedInfoCache();

        return apiDoc;
    }

    /**
     * 获取指定工程下的所有api描述信息列表
     * @param project
     * @return
     */
    private static List<ApiDetail> resolveAllApi(Project project) {
        if (Objects.isNull(project)) {
            return Collections.emptyList();
        }
        BaseResolver baseResolver = new SpringControllerResolver();
        return baseResolver.getAllApiInProject(project);
    }
}
