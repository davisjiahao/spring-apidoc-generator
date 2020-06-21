package top.hungrywu.resolver;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.annotations.NonNull;
import top.hungrywu.bean.ApiDetail;
import top.hungrywu.bean.DescriptionDetail;
import top.hungrywu.bean.ParamDetail;
import top.hungrywu.bean.ReturnDetail;
import top.hungrywu.enums.annotations.BaseMappingAnnotation;
import top.hungrywu.helper.PsiCommentResolverHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/2/27 7:07 下午
 * @Version 1.0
 **/
public abstract class BaseResolver {

    /**
     * 解析的标注信息
     */
    private BaseMappingAnnotation[] supportedAnnotations;

    /**
     * 需要指明支持搜索解析的标注信息
     * @param supportedAnnotations not null
     */
    public BaseResolver(@NonNull BaseMappingAnnotation[] supportedAnnotations) {
        this.supportedAnnotations = supportedAnnotations;
    }

    /***
     * 解析函数的返回值信息
     * @author : daviswujiahao
     * @date : 2020/2/28 2:30 下午
     * @param psiMethod : not null
     * @return : top.hungrywu.bean.ApiDetail
     **/
    protected ReturnDetail resolveApiReturnValue(@NonNull PsiMethod psiMethod) {
        return null;
    }

    /***
     * 解析函数的参数信息
     * @author : daviswujiahao
     * @date : 2020/2/28 3:16 下午
     * @param psiMethod : not null
     * @return : java.util.List<top.hungrywu.bean.ParamDetail>
     **/
    protected List<ParamDetail> resolveApiParamValue(@NonNull PsiMethod psiMethod) {
        return null;
    }


    /***
     * 解析函数的注释信息
     * @author : daviswujiahao
     * @date : 2020/2/28 3:31 下午
     * @param psiMethod :
     * @return : top.hungrywu.bean.DescriptionDetail
     **/
    protected DescriptionDetail resolveMethodDescription(@NonNull PsiMethod psiMethod) {
        return null;
    }

    /**
     * 获取指定工程下的所有api描述信息列表
     * @param myProject not null
     * @return
     */
    public List<ApiDetail> getAllApiInProject(@NonNull Project myProject) {

        if (myProject == null) {
            return new ArrayList<>(0);
        }

        // 获得全局搜索工具对象
        GlobalSearchScope globalSearchScope = GlobalSearchScope.projectScope(myProject);

        List<ApiDetail> apiDetailResult = new ArrayList<>();

        for (BaseMappingAnnotation mappingAnnotation : supportedAnnotations) {
            // 1、先找到包含指定注解的class文件对象
            Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(mappingAnnotation.getShortName(), myProject, globalSearchScope);
            for (PsiAnnotation psiAnnotation : psiAnnotations) {

                PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
                PsiElement psiElement = psiModifierList.getParent();
                if (!(psiElement instanceof PsiClass)) {
                    // (RestController) annotation 只出现在class上
                    continue;
                }

                // 2、再在找到的class文件中寻找api描述信息
                PsiClass psiClass = (PsiClass) psiElement;
                if (PsiCommentResolverHelper.existedTag(psiClass.getDocComment(), PsiCommentResolverHelper.API_PARSER_IGNORE)) {
                    continue;
                }
                List<ApiDetail> apiDetailsInClass = this.getAllApiInClass(psiClass);
                if (CollectionUtils.isEmpty(apiDetailsInClass)) {
                    continue;
                }
                apiDetailResult.addAll(apiDetailsInClass);
            }
        }

        return apiDetailResult;
    }

    /**
     * 解析class文件中的
     * @param myClass
     * @return
     */
    protected abstract List<ApiDetail> getAllApiInClass(@NonNull PsiClass myClass);
}
