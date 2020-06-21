package top.hungrywu.config;

import top.hungrywu.bean.ParamDetail;

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

    public static String WRAPPED_REQUEST_CLASS_NAME = "com.xiaoju.manhattan.venus.request.BusinessRequest";
    public static String WRAPPED_REQUEST_CONTENT_FILE_NAME = "bizContent";
}
