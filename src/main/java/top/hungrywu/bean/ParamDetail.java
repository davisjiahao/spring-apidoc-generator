package top.hungrywu.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Description api 参数描述类
 * @Author daviswujiahao
 * @Date 2020/2/27 10:16 下午
 * @Version 1.0
 **/
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class ParamDetail extends BaseInfo {
    /**
     * 参数类型
     */
    private String paramType;
}
