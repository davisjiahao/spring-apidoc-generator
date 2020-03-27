package top.hungrywu.bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Description 整个项目的api 描述信息实体类
 * @Author daviswujiahao
 * @Date 2020/2/28 10:11 上午
 * @Version 1.0
 **/

@Accessors(chain = true)
@Data
public class ApiDoc {
    /**
     * 项目名称
     */
    private String projectName;

    /**
     * api 服务访问ip
     */
    private String apiDeployHost;

    /**
     * api 服务访问端口
     */
    private Integer apiDeployPort;

    /**
     * api 服务详情列表
     */
    private List<ApiDetail> apiDetails;

    private String branchName;

    private String commitVersion;
}
