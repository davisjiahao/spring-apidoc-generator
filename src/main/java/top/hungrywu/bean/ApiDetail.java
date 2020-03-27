package top.hungrywu.bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Description api 详情
 * @Author daviswujiahao
 * @Date 2020/2/27 9:49 下午
 * @Version 1.0
 **/

@Accessors(chain = true)
@Data
public class ApiDetail {

//    public String paramTemplate = "{sign:?}"

    /**
     * 接口描述信息
     */
    private String description;

    /**
     * api 与主机无关的request url，以/开头
     */
    private List<String> baseUrl;

    /**
     * api http request method type
     */
    private List<String> methodType;

    /**
     * api 参数信息列表
     */
    private List<ParamDetail> params;

    /**
     * api return 信息
     */
    private ReturnDetail result;

    /**
     * 接口作者
     */
    private String author;

    /**
     * 接口协议名称
     */
    private String protocolName;

    /**
     * api 访问url
     */
    private String apiContentUrl;
}
