package top.hungrywu.helper;

import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.sun.istack.Nullable;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.annotations.NonNull;
import top.hungrywu.bean.DescriptionDetail;

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
    private static Map<String, String> getTagValuesInJavaDoc(PsiDocComment psiDocComment, String tagName,
                                                     Function<PsiDocTag[], Map<String, String>> tagsHandler) {
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

    @NonNull
    public static String getNonParamTagValueInJavaDoc(PsiDocComment psiDocComment, String tagName) {
        tagName = tagName.trim();
        String finalTagName = tagName;
        Map<String, String> res = getTagValuesInJavaDoc(psiDocComment, tagName,
                psiDocTags -> {
                    Map<String, String> value = new HashMap<>();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (PsiDocTag tag : psiDocTags) {
                        PsiElement[] dataElements = tag.getDataElements();
                        for (int i = 0; i < dataElements.length; i++) {
                            stringBuffer.append(" " + dataElements[i].getText().trim());
                        }
                    }
                    value.put(finalTagName, stringBuffer.toString());
                    return value;
                });
        if (res.isEmpty() || !res.containsKey(tagName)) {
            return "";
        }
        return res.get(tagName);
    }

    public static Map<String, String> getParamTagValuesInJavaDoc(PsiDocComment psiDocComment) {
        return getTagValuesInJavaDoc(psiDocComment, PARAM_NAME_TAG_NAME_IN_JAVADOC,
                psiDocTags -> {
                    Map<String, String> params = new HashMap<>();
                    for (PsiDocTag tag : psiDocTags) {
                        StringBuffer stringBuffer = new StringBuffer();
                        PsiElement[] dataElements = tag.getDataElements();
                        if (dataElements.length == 0 || StringUtils.isEmpty(dataElements[0].getText().trim())) {
                            continue;
                        }
                        String paramName = dataElements[0].getText().trim();
                        for (int i = 1; i < dataElements.length; i++) {
                            stringBuffer.append(dataElements[i].getText().trim());
                        }
                        params.put(paramName, stringBuffer.toString());
                    }
                    return params;
                });
    }

}
