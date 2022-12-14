package top.hungrywu.bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/2/27 10:39 下午
 * @Version 1.0
 **/
@Data
@Accessors(chain = true)
public class BaseInfo {
    /**
     * 参数标识
     */
    private String name;

    /**
     * 参数类型名称
     */
    private String typeName;


    private String typeName4TableTitle;

    /**
     * 该参数是否可选
     */
    private boolean required = true;

    /**
     * 子类型描述信息
     */
    private List<BaseInfo> subTypeInfos;

    /**
     * 参数描述信息
     */
    private String description;
}
