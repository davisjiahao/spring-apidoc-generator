package top.hungrywu.util;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.annotations.NonNull;

import java.util.List;

/**
 * @Description git 工具类
 * @Author daviswujiahao
 * @Date 2020/3/26 3:12 下午
 * @Version 1.0
 **/
public class GitUtils {

    @Data
    @Builder
    public static class VersionInfoByGit {
        private String branchName;
        private String commitId;
    }

    /***
     * 获取项目的git版本信息
     * @author : daviswujiahao
     * @date : 2020/3/26 4:07 下午
     * @param project :
     * @return : top.hungrywu.util.GitUtils.VersionInfoByGit 当当前项目不是git仓库时，返回null
     **/
    public static VersionInfoByGit getVersionInfoByGit(@NonNull Project project) {
        List<GitRepository> repositories = GitRepositoryManager.getInstance(project).getRepositories();
        if (CollectionUtils.isEmpty(repositories)) {
            // 不是git仓库
            // todo warning log
            return null;
        }
        GitRepository gitRepository = repositories.get(0);
        String branchName = gitRepository.getCurrentBranchName();
        String commitId = gitRepository.getCurrentRevision();

        return VersionInfoByGit.builder().
                branchName(branchName).
                commitId(commitId).
                build();

    }


}
