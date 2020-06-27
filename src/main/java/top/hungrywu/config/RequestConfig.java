package top.hungrywu.config;

import org.apache.commons.lang.StringUtils;
import top.hungrywu.bean.ParamDetail;
import top.hungrywu.toolwindow.ConsoleLogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/3/27 3:17 下午
 * @Version 1.0
 **/
public class RequestConfig {
    public static String DEFAULT_REQUEST_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static String DEFAULT_PROTOCOL_TYPE = "HTTP";
    public static boolean DEFAULT_WRAPPED = true;

    public static String WRAPPED_REQUEST_CLASS_NAME = "";
    public static String WRAPPED_REQUEST_CONTENT_FILE_NAME = "bizContent";

    public static boolean validConfig() {
        if (StringUtils.isEmpty(DEFAULT_REQUEST_CONTENT_TYPE)) {
            ConsoleLogFactory.addErrorLog("config error: default request content type can not be empty");
            return false;
        }
        if (StringUtils.isEmpty(DEFAULT_PROTOCOL_TYPE)) {
            ConsoleLogFactory.addErrorLog("config error: default protocol type can not be empty");
            return false;
        }
        if (DEFAULT_WRAPPED) {
            if (StringUtils.isEmpty(WRAPPED_REQUEST_CLASS_NAME)) {
                ConsoleLogFactory.addErrorLog("config error: wrapped request class name can not be empty when default wrapped is selected");
                return false;
            }

            if (StringUtils.isEmpty(WRAPPED_REQUEST_CLASS_NAME)) {
                ConsoleLogFactory.addErrorLog("config error:wrapped request content file name can not be empty when default wrapped is selected");
                return false;
            }
        }

        return true;
    }
}
