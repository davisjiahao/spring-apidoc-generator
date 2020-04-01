package top.hungrywu.helper;

import com.intellij.psi.*;
import top.hungrywu.bean.BaseInfo;
import top.hungrywu.bean.ParamDetail;
import top.hungrywu.bean.ParamInfo;
import top.hungrywu.enums.IgnoreParsingParaTypeEnum;
import top.hungrywu.enums.annotations.SpringRequestParamAnnotations;


import java.util.*;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/3/2 5:44 下午
 * @Version 1.0
 **/
public class PsiMethodResolverHelper {

    /**
     * 解析函数参数
     *
     * @param psiParameter
     * @return
     */
    public static ParamDetail parseParamOfMethod(PsiParameter psiParameter, ParamInfo paramInfoInDoc) {

        String qualifiedTypeName = psiParameter.getType().getCanonicalText();
        if (IgnoreParsingParaTypeEnum.getByQualifiedName(qualifiedTypeName) != null) {
            return null;
        }
        if (Objects.isNull(paramInfoInDoc)) {
            // todo error log
        }

        ParamDetail paramDetail = new ParamDetail();

        // 解析参数自身的信息
        parseBaseInfoOfParam(psiParameter, paramDetail, paramInfoInDoc);

        // 解析参数的注解上的信息
        parseBaseInfoVisParamAnnotation(psiParameter, paramDetail);

        // 解析JavaDoc上的参数信息
        if (!Objects.isNull(paramInfoInDoc)) {
            paramDetail.setDescription(paramInfoInDoc.getDescription());
        }

        return paramDetail;
    }

    public static void parseBaseInfoOfParam(PsiParameter psiParameter, BaseInfo paramInfo, ParamInfo paramInfoInDoc) {

        PsiType psiType = psiParameter.getType();
        if (Objects.nonNull(paramInfoInDoc) && Objects.nonNull(paramInfoInDoc.getParaType())) {
            psiType = paramInfoInDoc.getParaType();
        }

        String paraName = psiParameter.getName();
        String typeName = psiType.getPresentableText();
        paramInfo.setName(paraName);
        paramInfo.setTypeName(typeName);

        PsiTypeResolverHelper.parsePsiType(paramInfo, psiType, new Stack<>(), new HashMap<>());
    }



    public static void parseBaseInfoVisParamAnnotation(PsiParameter psiParameter, ParamDetail paramInfo) {

        // 如果是spring的REQUEST_BODY注解后的参数
        PsiAnnotation requestBodyAnnotation = psiParameter.getAnnotation(
                SpringRequestParamAnnotations.REQUEST_BODY.getQualifiedName());
        if (!Objects.isNull(requestBodyAnnotation)) {
            paramInfo.setParamRequestType(SpringRequestParamAnnotations.REQUEST_BODY.getShortName());
            paramInfo.setRequired(true);
        }

        // 如果是spring的REQUEST_PARAM注解后的参数
        PsiAnnotation requestParamAnnotation = psiParameter.getAnnotation(
                SpringRequestParamAnnotations.REQUEST_PARAM.getQualifiedName());
        if (!Objects.isNull(requestParamAnnotation)) {
            paramInfo.setParamRequestType(SpringRequestParamAnnotations.REQUEST_PARAM.getShortName());
            paramInfo.setRequired("true".equals(PsiAnnotationResolverHelper.
                    getAnnotationAttributeValue(requestParamAnnotation, "required")));
        }
    }

}
