package top.hungrywu.bean;

import lombok.Data;

import java.util.Date;

/**
 * @Description java doc注释信息
 * @Author daviswujiahao
 * @Date 2020/2/28 3:23 下午
 * @Version 1.0
 **/
@Data
public class DescriptionDetail {
    /**
     * 描述信息
     */
    private String description;

    /**
     * 作者姓名
     */
    private String authorName;

    /**
     * 作者联系邮箱
     */
    private String authMail;

    /**
     * 创建日期
     */
    private String createTime;

    /**
     * 版本信息
     */
    private String version;
}
