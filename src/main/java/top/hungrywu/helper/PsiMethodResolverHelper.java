package top.hungrywu.helper;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import top.hungrywu.bean.ApiDetail;
import top.hungrywu.bean.BaseInfo;
import top.hungrywu.bean.ParamDetail;
import top.hungrywu.bean.ParamInfo;
import top.hungrywu.config.RequestConfig;
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


    public static void parseParam(ApiDetail apiDetailRet, PsiMethod psiMethod, PsiDocComment commentOnMethod) {
        // 4、解析函数参数
        Map<String, ParamInfo> paramJavaDocInfos = PsiCommentResolverHelper.getParamInfoInJavaDoc(commentOnMethod);

        PsiParameterList parameterList = psiMethod.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        List<ParamDetail> wrappedParamDetails = new ArrayList<>();
        List<ParamDetail> nonWrappedParamDetails = new ArrayList<>();
        for (PsiParameter psiParameter : parameters) {

            ParamDetail paramDetail;
            boolean wrapped = RequestConfig.DEFAULT_WRAPPED;

            ParamInfo paramInfo = paramJavaDocInfos.get(psiParameter.getName());
            if (Objects.equals(psiParameter.getType().getCanonicalText(), RequestConfig.WRAPPED_REQUEST_CLASS_NAME)
                    && Objects.isNull(paramInfo.getParaType())) {
                paramDetail = new ParamDetail();
                paramDetail.setTypeName("{}");
                wrapped = true;
            } else {
                paramDetail = PsiMethodResolverHelper.parseParamOfMethod(psiParameter, paramInfo);
                if (Objects.isNull(paramDetail)) {
                    continue;
                }
            }
            if (Objects.nonNull(paramInfo)) {
                wrapped = paramInfo.isRequestWrapped();
            }
            if (wrapped) {
                wrappedParamDetails.add(paramDetail);
            } else {
                nonWrappedParamDetails.add(paramDetail);
            }
        }

        boolean wrapped = StringUtils.containsIgnoreCase(
                PsiCommentResolverHelper.getNonParamTagValueInJavaDoc(psiMethod.getDocComment(),
                        PsiCommentResolverHelper.REQUEST_WRAPPED_NAME_TAG_NAME_IN_JAVADOC), "true");
        if (CollectionUtils.isEmpty(wrappedParamDetails) && !wrapped) {
            apiDetailRet.setParams(nonWrappedParamDetails);
        } else {
            List<ParamDetail> paramDetails = new ArrayList<>();
            paramDetails.addAll(nonWrappedParamDetails);

            // todo 获取wrappedRequestClass 不需要每次解析一个param就进行一次
            PsiClass wrappedRequestClass = JavaPsiFacade.getInstance(psiMethod.getProject())
                    .findClass(RequestConfig.WRAPPED_REQUEST_CLASS_NAME,
                            GlobalSearchScope.projectScope(psiMethod.getProject()));
            if(Objects.isNull(wrappedRequestClass)) {
                // todo error log: RequestConfig.wrappedRequestClassName 配置错误
                return;
            }
            PsiField[] fields = wrappedRequestClass.getAllFields();
            for (PsiField field : fields) {
                ParamDetail requestParam = new ParamDetail();
                String description = PsiCommentResolverHelper.getDescriptionInJavaDoc(field.getDocComment());
                requestParam.setDescription(description);
                String fieldName = field.getName();
                requestParam.setName(fieldName);
                String typeName = field.getType().getPresentableText();
                requestParam.setTypeName(typeName);
                requestParam.setTypeName4TableTitle(typeName);
                if (Objects.equals(fieldName, RequestConfig.WRAPPED_REQUEST_CONTENT_FILE_NAME)) {
                    requestParam.setSubTypeInfos(new ArrayList<>());
                    if (wrappedParamDetails.size() == 1) {
                        requestParam.setTypeName(wrappedParamDetails.get(0).getTypeName());
                        requestParam.setTypeName4TableTitle(wrappedParamDetails.get(0).getTypeName4TableTitle());
                        requestParam.setSubTypeInfos(wrappedParamDetails.get(0).getSubTypeInfos());
                    } else if (wrappedParamDetails.size() == 0) {
                        requestParam.setTypeName("{}");
                    } else {
                        for (ParamDetail wrappedParamDetail : wrappedParamDetails) {
                            requestParam.getSubTypeInfos().add(wrappedParamDetail);
                        }
                    }
                }
                paramDetails.add(requestParam);
            }
            apiDetailRet.setParams(paramDetails);
        }
    }


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
            return null;
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
