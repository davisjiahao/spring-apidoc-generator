package top.hungrywu.helper;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiKeywordImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.sun.istack.Nullable;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.annotations.NonNull;
import top.hungrywu.bean.DescriptionDetail;
import top.hungrywu.bean.ParamInfo;
import top.hungrywu.config.RequestConfig;

import java.util.*;
import java.util.function.Function;

/**
 * @Description javadoc 注释解析
 * @Author daviswujiahao
 * @Date 2020/3/2 12:05 下午
 * @Version 1.0
 **/
public class PsiCommentResolverHelper {

    public final static String AUTHOR_NAME_TAG_NAME_IN_JAVADOC = "author";
    public final static String AUTHOR_MAIL_TAG_NAME_IN_JAVADOC = "mail";

    public final static String DATE_NAME_TAG_NAME_IN_JAVADOC = "date";
    public final static String VERSION_NAME_TAG_NAME_IN_JAVADOC = "version";

    public final static String PARAM_NAME_TAG_NAME_IN_JAVADOC = "param";
    public final static String SEE_NAME_TAG_NAME_IN_JAVADOC = "see";
    public final static String REQUEST_WRAPPED_NAME_TAG_NAME_IN_JAVADOC = "wrapped";

    public final static String REQUEST_CONTENT_TYPE_TAG_NAME_IN_JAVADOC = "requestContentType";

    public final static String API_PARSER_IGNORE = "apiIgnore";

    /**
     * 解析javadoc注释中的描述信息
     * @param psiDocComment
     * @return 返回null，如果没有javadoc
     */
    @Nullable
    public static DescriptionDetail parseJavaDoc(PsiDocComment psiDocComment) {
        if (Objects.isNull(psiDocComment)) {
            return null;
        }

        DescriptionDetail descriptionDetail = new DescriptionDetail();

        descriptionDetail.setDescription(getDescriptionInJavaDoc(psiDocComment));

        descriptionDetail.setAuthorName(getNonParamTagValueInJavaDoc(psiDocComment, AUTHOR_NAME_TAG_NAME_IN_JAVADOC));

        descriptionDetail.setAuthMail(getNonParamTagValueInJavaDoc(psiDocComment, AUTHOR_MAIL_TAG_NAME_IN_JAVADOC));

        descriptionDetail.setCreateTime(getNonParamTagValueInJavaDoc(psiDocComment, DATE_NAME_TAG_NAME_IN_JAVADOC));

        descriptionDetail.setVersion(getNonParamTagValueInJavaDoc(psiDocComment, VERSION_NAME_TAG_NAME_IN_JAVADOC));

        descriptionDetail.setRequestContentType(getNonParamTagValueInJavaDoc(psiDocComment, REQUEST_CONTENT_TYPE_TAG_NAME_IN_JAVADOC));


        return descriptionDetail;
    }

    /**
     * 解析javadoc 注释首部的描述信息
     * @param psiDocComment
     * @return
     */
    public static String getDescriptionInJavaDoc(PsiDocComment psiDocComment) {
        if (Objects.isNull(psiDocComment)) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (PsiElement child : psiDocComment.getDescriptionElements()) {
            stringBuffer.append(child.getText().trim().replace("\n", ""));
        }

        return stringBuffer.toString();
    }


    /**
     * 解析javadoc中 @tagName 对应的值
     * @param psiDocComment
     * @param tagName
     * @return
     */
    @NonNull
    private static Map<String, ParamInfo> getTagValuesInJavaDoc(PsiDocComment psiDocComment, String tagName,
                                                     Function<PsiDocTag[], Map<String, ParamInfo>> tagsHandler) {
        if (Objects.isNull(psiDocComment)) {
            return Collections.emptyMap();
        }
        if (StringUtils.isEmpty(tagName)) {
            return Collections.emptyMap();
        }
        tagName = tagName.toLowerCase();
        PsiDocTag[] tags = psiDocComment.findTagsByName(tagName);
        if (null == tags || tags.length == 0) {
            tags = psiDocComment.findTagsByName(tagName.substring(0, 1).toLowerCase() + tagName.substring(1));
            if (null == tags || tags.length == 0) {
                return Collections.emptyMap();
            }
        }

        return tagsHandler.apply(tags);
    }

    public static boolean existedTag(PsiDocComment psiDocComment, String tagName) {
        if (Objects.isNull(psiDocComment)) {
            return false;
        }
        PsiDocTag[] tags = psiDocComment.findTagsByName(tagName);
        if (null == tags || tags.length == 0) {
            return false;
        }
        return true;
    }

    @NonNull
    public static String getNonParamTagValueInJavaDoc(PsiDocComment psiDocComment, String tagName) {
        tagName = tagName.trim();
        String finalTagName = tagName;

        if (Objects.isNull(psiDocComment)) {
            return "";
        }
        if (StringUtils.isEmpty(tagName)) {
            return "";
        }
        tagName = tagName.toLowerCase();
        PsiDocTag[] tags = psiDocComment.findTagsByName(tagName);
        if (null == tags || tags.length == 0) {
            tags = psiDocComment.findTagsByName(tagName.substring(0, 1).toLowerCase() + tagName.substring(1));
            if (null == tags || tags.length == 0) {
                return "";
            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (PsiDocTag tag : tags) {
            PsiElement[] dataElements = tag.getDataElements();
            for (int i = 0; i < dataElements.length; i++) {
                stringBuffer.append(" " + dataElements[i].getText().trim());
            }
        }
        return stringBuffer.toString();
    }

    public static Map<String, ParamInfo> getParamInfoInJavaDoc(PsiDocComment psiDocComment) {
        return getTagValuesInJavaDoc(psiDocComment, PARAM_NAME_TAG_NAME_IN_JAVADOC,
                psiDocTags -> {
                    Map<String, ParamInfo> params = new HashMap<>();
                    for (PsiDocTag tag : psiDocTags) {
                        ParamInfo paramInfo = new ParamInfo();

                        PsiDocTag requestWrappedTag = parseWrappedTagBehindOfParaTag(tag);
                        if (Objects.isNull(requestWrappedTag)) {
                            paramInfo.setRequestWrapped(RequestConfig.defaultWrapped);
                        } else {
                            String requestWrappedStr = requestWrappedTag.getValueElement().getText().trim();
                            if (StringUtils.containsIgnoreCase(requestWrappedStr, "true")) {
                                paramInfo.setRequestWrapped(true);
                            } else if (StringUtils.containsIgnoreCase(requestWrappedStr, "false")) {
                                paramInfo.setRequestWrapped(false);
                            } else {
                                paramInfo.setRequestWrapped(RequestConfig.defaultWrapped);
                            }
                        }

                        PsiDocTag seeDocTag = parseSeeTagBehindOfParaTag(tag);
                        PsiType type = null;
                        if (!Objects.isNull(seeDocTag)) {
                            for (PsiElement dataElement : seeDocTag.getDataElements()) {
                                if (!Objects.isNull(dataElement.getFirstChild())) {
                                    if (dataElement.getFirstChild() instanceof PsiJavaCodeReferenceElement) {
                                        PsiElement psiElement = ((PsiJavaCodeReferenceElement) (dataElement.getFirstChild())).resolve();
                                        type = JavaPsiFacade.getInstance(psiElement.getProject()).getElementFactory().createType((PsiClass) psiElement);
                                    } else if (dataElement.getFirstChild() instanceof PsiKeywordImpl) {
                                        JvmPrimitiveTypeKind kindByName = JvmPrimitiveTypeKind.getKindByName(((PsiKeywordImpl) dataElement.getFirstChild()).getText());
                                        if (!Objects.isNull(kindByName)) {
                                            type = new PsiPrimitiveType(kindByName, new PsiAnnotation[0]);
                                        }
                                    }
                                }
                            }
                        }
                        paramInfo.setParaType(type);

                        StringBuffer stringBuffer = new StringBuffer();
                        PsiElement[] dataElements = tag.getDataElements();
                        if (dataElements.length == 0 || StringUtils.isEmpty(dataElements[0].getText().trim())) {
                            continue;
                        }
                        String paramName = dataElements[0].getText().trim();
                        for (int i = 1; i < dataElements.length; i++) {
                            stringBuffer.append(dataElements[i].getText().trim());
                        }
                        paramInfo.setDescription(stringBuffer.toString());

                        paramInfo.setParamName(paramName);

                        params.put(paramName, paramInfo);
                    }
                    return params;
                });
    }
    
    
    public static PsiDocTag parseSeeTagBehindOfParaTag(PsiDocTag tag) {
        return parseFirstTagBehindOfParaTag(tag, SEE_NAME_TAG_NAME_IN_JAVADOC);

    }

    public static PsiDocTag parseWrappedTagBehindOfParaTag(PsiDocTag tag) {
        return parseFirstTagBehindOfParaTag(tag, REQUEST_WRAPPED_NAME_TAG_NAME_IN_JAVADOC);
    }

    private static PsiDocTag parseFirstTagBehindOfParaTag(PsiDocTag tag, String tagTypeName) {
        if (!Objects.equals(tag.getName(), PARAM_NAME_TAG_NAME_IN_JAVADOC)) {
            return null;
        }
        PsiElement nextSibling = tag.getNextSibling();
        while (!Objects.isNull(nextSibling)) {
            if (nextSibling instanceof PsiDocTag) {
                if (Objects.equals(((PsiDocTag)nextSibling).getName(), PARAM_NAME_TAG_NAME_IN_JAVADOC)) {
                    break;
                }
                if (Objects.equals(((PsiDocTag)nextSibling).getName(), tagTypeName)) {
                    return (PsiDocTag) nextSibling;
                }
            }
            nextSibling = nextSibling.getNextSibling();
        }
        return null;
    }

}
