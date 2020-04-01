package top.hungrywu.resolver;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.annotations.NonNull;
import top.hungrywu.bean.*;
import top.hungrywu.config.RequestConfig;
import top.hungrywu.enums.annotations.SpringControllerAnnotation;
import top.hungrywu.enums.annotations.SpringRequestMethodAnnotation;
import top.hungrywu.helper.PsiCommentResolverHelper;
import top.hungrywu.helper.PsiAnnotationResolverHelper;
import top.hungrywu.helper.PsiMethodResolverHelper;
import top.hungrywu.helper.PsiTypeResolverHelper;

import java.util.*;

/**
 * @Description Spring Controller 接口解析
 * @Author daviswujiahao
 * @Date 2020/2/27 7:07 下午
 * @Version 1.0
 **/
@Slf4j
public class SpringControllerResolver extends BaseResolver {

    private final static String METHOD_ATTR_NAME = "method";
    private final static String VALUE_ATTR_NAME = "value";
    private final static String PATH_ATTR_NAME = "path";

    public SpringControllerResolver() {
        super(SpringControllerAnnotation.values());
    }

    @NonNull
    @Override
    protected List<ApiDetail> getAllApiInClass(@NonNull PsiClass psiClass) {

        // 1、解析myClass上的RequestPath注解，获取class 级别的urlBasePath 和 requestMethodType
        PsiAnnotation[] classAnnotations = psiClass.getAnnotations();
        if (classAnnotations == null || classAnnotations.length == 0) {
            return Collections.emptyList();
        }
        // 获取class级别的RequestPath注解上的method属性的值
        List<String> methodTypeListOnClass = new ArrayList<>();
        // 获取class级别的RequestPath注解上的value属性的值
        List<String> pathListOnClass = new ArrayList<>();
        resolveRequestPathOnClass(classAnnotations, methodTypeListOnClass, pathListOnClass);


        // 2、解析myClass上的javaDoc注释，获取默认的author信息
        PsiDocComment comment = psiClass.getDocComment();
        DescriptionDetail classDescriptionDetail = PsiCommentResolverHelper.parseJavaDoc(comment);
        if (Objects.isNull(classDescriptionDetail)) {
            classDescriptionDetail = new DescriptionDetail();
        }


        // 3、获取class中所有的函数对象
        PsiMethod[] psiMethods = psiClass.getMethods();
        if (psiMethods.length == 0) {
            return new ArrayList<>();
        }
        // 3.1、过滤出函数对象有含有spring request 注解的函数对象，并进行接口信息解析
        List<ApiDetail> apiDetails = new ArrayList<>();
        for (PsiMethod method : psiMethods) {
            ApiDetail apiDetail = resolveMethod2Api(method);
            if (apiDetail == null) {
                continue;
            }
            if (StringUtils.isEmpty(apiDetail.getAuthor()) && !StringUtils.isEmpty(classDescriptionDetail.getAuthorName())) {
                apiDetail.setAuthor(classDescriptionDetail.getAuthorName());
            }

            if (CollectionUtils.isEmpty(apiDetail.getBaseUrl())) {
                // todo error
                continue;
            }
            if (CollectionUtils.isNotEmpty(pathListOnClass)) {
                List<String> fullBaseUrls = new ArrayList<>();
                // 拼接url
                for (String pathOnClass : pathListOnClass) {
                    for (String pathOnMethod : apiDetail.getBaseUrl()) {
                        fullBaseUrls.add(pathOnClass + pathOnMethod);
                    }
                }
                apiDetail.setBaseUrl(fullBaseUrls);
            }

            if (CollectionUtils.isEmpty(apiDetail.getMethodType()) && !CollectionUtils.isEmpty(methodTypeListOnClass)) {
                apiDetail.setMethodType(methodTypeListOnClass);
            }
            if (CollectionUtils.isEmpty(apiDetail.getMethodType())) {
                // todo 如果进行到这里 methodType还是为空，需要指定默认的methodType
            }
            for (int i = 0; i < apiDetail.getMethodType().size(); i++) {
                // 去掉methodTypeqian前缀
                if (apiDetail.getMethodType().get(i).contains(".")) {
                    apiDetail.getMethodType().set(i, apiDetail.getMethodType().get(i).substring(apiDetail.getMethodType().get(i).lastIndexOf('.') + 1));
                }
            }
            apiDetails.add(apiDetail);
        }

        return apiDetails;
    }

    private void resolveRequestPathOnClass(PsiAnnotation[] classAnnotations,
                                           @NonNull List<String> methodTypeListOnClass,
                                           @NonNull List<String> pathListOnClass) {
        PsiAnnotation requestMappingAnnotation = null;
        for (PsiAnnotation classPsiAnnotation : classAnnotations) {
            if (Objects.equals(classPsiAnnotation.getQualifiedName(), SpringRequestMethodAnnotation.REQUEST_MAPPING.getQualifiedName())) {
                requestMappingAnnotation = classPsiAnnotation;
                break;
            }
        }

        if (Objects.isNull(requestMappingAnnotation)) {
           return;
        } else {
            methodTypeListOnClass.addAll(PsiAnnotationResolverHelper.getAnnotationAttributeValues(requestMappingAnnotation, METHOD_ATTR_NAME));
            // 获取class级别的RequestPath注解上的value属性的值
            pathListOnClass.addAll(PsiAnnotationResolverHelper.getAnnotationAttributeValues(requestMappingAnnotation, VALUE_ATTR_NAME));
            if (pathListOnClass.size() == 0) {
                pathListOnClass.addAll(PsiAnnotationResolverHelper.getAnnotationAttributeValues(requestMappingAnnotation, PATH_ATTR_NAME));
            }
        }
    }


    private ApiDetail resolveMethod2Api(PsiMethod psiMethod) {

        // 1、获得psiMethod上的spring注解
        PsiAnnotation springAnnotationOnMethod = getSpringAnnotationOnMethod(psiMethod);
        if (springAnnotationOnMethod == null) {
            return null;
        }

        // 1.1 如果有apiIgnore标识，则忽略
        if (PsiCommentResolverHelper.existedTag(psiMethod.getDocComment(), PsiCommentResolverHelper.API_PARSER_IGNORE)) {
            return null;
        }

        ApiDetail apiDetailRet = new ApiDetail();

        // 2、解析函数级别上的spring注解
        List<String> methodTypeListOnMethod;
        List<String> pathListOnMethod;
        SpringRequestMethodAnnotation requestMethodAnnotation = SpringRequestMethodAnnotation.getByQualifiedName(springAnnotationOnMethod.getQualifiedName());
        if (requestMethodAnnotation.getQualifiedName().equals(SpringRequestMethodAnnotation.REQUEST_MAPPING.getQualifiedName())) {
            // 获取method级别的RequestPath注解上的method属性的值
            methodTypeListOnMethod = PsiAnnotationResolverHelper.getAnnotationAttributeValues(springAnnotationOnMethod, METHOD_ATTR_NAME);
        } else {
            methodTypeListOnMethod = new ArrayList<>();
            methodTypeListOnMethod.add(requestMethodAnnotation.methodName());
        }
        // 获取method级别的RequestPath注解上的value属性的值,即baseurlpath
        pathListOnMethod = PsiAnnotationResolverHelper.getAnnotationAttributeValues(springAnnotationOnMethod, VALUE_ATTR_NAME);
        if (pathListOnMethod.size() == 0) {
            pathListOnMethod = PsiAnnotationResolverHelper.getAnnotationAttributeValues(springAnnotationOnMethod, PATH_ATTR_NAME);
        }
        apiDetailRet.setBaseUrl(pathListOnMethod);
        apiDetailRet.setMethodType(methodTypeListOnMethod);

        // 3、解析函数级别上的javadoc信息
        PsiDocComment commentOnMethod = psiMethod.getDocComment();
        DescriptionDetail methodDescriptionDetail = PsiCommentResolverHelper.parseJavaDoc(commentOnMethod);
        if (Objects.isNull(methodDescriptionDetail)) {
            methodDescriptionDetail = new DescriptionDetail();
        }
        if (StringUtils.isNotEmpty(methodDescriptionDetail.getAuthorName())) {
            apiDetailRet.setAuthor(methodDescriptionDetail.getAuthorName());
        }
        if (StringUtils.isNotEmpty(methodDescriptionDetail.getDescription())) {
            apiDetailRet.setDescription(methodDescriptionDetail.getDescription());
        }

        // todo 解析content-type
        apiDetailRet.setContentType(RequestConfig.DEFAULT_REQUEST_CONTENT_TYPE);
        // todo 设置协议名称
        apiDetailRet.setProtocolName(RequestConfig.DEFAULT_PROTOCOL_TYPE);


        // 4、解析函数参数
        PsiMethodResolverHelper.parseParam(apiDetailRet, psiMethod, commentOnMethod);


        // 5、解析函数返回值
        PsiTypeElement returnType = psiMethod.getReturnTypeElement();
        PsiType type = psiMethod.getReturnType();
        ReturnDetail returnDetail = new ReturnDetail();
        returnDetail.setTypeName(type.getPresentableText());
        apiDetailRet.setResult(returnDetail);
        PsiTypeResolverHelper.parsePsiType(returnDetail, type, new Stack<>(), new HashMap<>());

        return apiDetailRet;
    }

    /**
     * 获得
     * @param psiMethod
     * @return 如果该函数未声明spring注解，则返回null
     */
    private PsiAnnotation getSpringAnnotationOnMethod(PsiMethod psiMethod) {
        PsiAnnotation[] annotationsOnMethod = psiMethod.getAnnotations();
        for (PsiAnnotation annotationOnMethod : annotationsOnMethod) {
            if (SpringRequestMethodAnnotation.getByQualifiedName(annotationOnMethod.getQualifiedName()) != null) {
                return annotationOnMethod;
            }
        }
        return null;
    }

}
