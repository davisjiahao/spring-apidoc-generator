package top.hungrywu.resolver;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import org.assertj.core.annotations.NonNull;
import top.hungrywu.bean.ApiDetail;
import top.hungrywu.enums.annotations.SpringControllerAnnotation;
import top.hungrywu.enums.annotations.SpringRequestMethodAnnotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @Description Spring Controller 接口解析
 * @Author daviswujiahao
 * @Date 2020/2/27 7:07 下午
 * @Version 1.0
 **/
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

        PsiModifierList classElements = psiClass.getModifierList();
        if (Objects.isNull(classElements)) {
            return Collections.emptyList();
        }

        // 0、解析myClass上的RequestPath注解，获取class 级别的urlBasePath 和 requestMethodType
        PsiAnnotation[] classAnnotations = classElements.getAnnotations();
        if (classAnnotations == null || classAnnotations.length == 0) {
            return Collections.emptyList();
        }
        PsiAnnotation requestMappingAnnotation = null;
        for (PsiAnnotation classPsiAnnotation : classAnnotations) {
            if (Objects.equals(classPsiAnnotation.getQualifiedName(), SpringRequestMethodAnnotation.REQUEST_MAPPING)) {
                requestMappingAnnotation = classPsiAnnotation;
                break;
            }
        }
        if (Objects.isNull(requestMappingAnnotation)) {
            return Collections.emptyList();
        }

        List<String> methodTypeListOnClass;
        methodTypeListOnClass = PsiAnnotationResolverHelper.getAnnotationAttributeValues(requestMappingAnnotation, METHOD_ATTR_NAME);

        List<String> pathListOnClass = PsiAnnotationResolverHelper.getAnnotationAttributeValues(requestMappingAnnotation, VALUE_ATTR_NAME);
        if (pathListOnClass.size() == 0) {
            pathListOnClass = PsiAnnotationResolverHelper.getAnnotationAttributeValues(requestMappingAnnotation, PATH_ATTR_NAME);
        }



        // 1、获取class中所有的函数对象
        PsiMethod[] psiMethods = psiClass.getMethods();
        if (psiMethods.length == 0) {
            return new ArrayList<>();
        }

        // 2、过滤出函数对象有含有spring request 注解的函数对象，并进行接口信息解析

        return null;
    }
}
