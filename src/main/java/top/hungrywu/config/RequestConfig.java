package top.hungrywu.config;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
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
    public final static String DEFAULT_REQUEST_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public final static String DEFAULT_PROTOCOL_TYPE = "HTTP";
    public final static boolean defaultWrapped = true;

    public final static String wrappedRequestClassName = "com.xiaoju.manhattan.venus.request.BusinessRequest";
    public final static String wrappedRequestContentFileName = "bizContent";


    public final static List<ParamDetail> wrappedRequestParams = new ArrayList<>();

    static {

        ParamDetail paramSign = new ParamDetail();
        paramSign.setDescription("请求签名");
        paramSign.setName("sign");
        paramSign.setRequired(true);
        paramSign.setTypeName("string");
        wrappedRequestParams.add(paramSign);

        ParamDetail paramVersion = new ParamDetail();
        paramVersion.setDescription("版本信息");
        paramVersion.setName("version");
        paramVersion.setRequired(false);
        paramVersion.setTypeName("string");
        wrappedRequestParams.add(paramVersion);

        ParamDetail paramCaller = new ParamDetail();
        paramCaller.setDescription("请求端标识");
        paramCaller.setName("caller");
        paramCaller.setRequired(true);
        paramCaller.setTypeName("string");
        wrappedRequestParams.add(paramCaller);

        ParamDetail paramBizContent = new ParamDetail();
        paramBizContent.setDescription("实际请求数据");
        paramBizContent.setName("bizContent");
        paramBizContent.setRequired(true);
        paramBizContent.setTypeName("JSONObject");
        paramBizContent.setTypeName4TableTitle("JSONObject");
        paramBizContent.setSubTypeInfos(new ArrayList<>());
        wrappedRequestParams.add(paramBizContent);
    }
}
