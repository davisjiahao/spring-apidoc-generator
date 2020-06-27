package top.hungrywu.config;

import org.apache.commons.lang.StringUtils;
import top.hungrywu.toolwindow.ConsoleLogFactory;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/3/27 3:17 下午
 * @Version 1.0
 **/
public class ResponseConfig {
    public static String DEFAULT_RESPONSE_CONTENT_TYPE = "application/json";
    public static boolean validConfig() {
        if (StringUtils.isEmpty(DEFAULT_RESPONSE_CONTENT_TYPE)) {
            ConsoleLogFactory.addErrorLog("config error: default response content type can not be empty");
            return false;
        }
        return true;
    }
}
