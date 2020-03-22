package top.hungrywu.resolver;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.util.text.DateFormatUtil;
import org.assertj.core.annotations.NonNull;
import org.jetbrains.annotations.Nullable;
import top.hungrywu.bean.BaseInfo;

import java.math.BigDecimal;
import java.util.*;

import static com.intellij.psi.CommonClassNames.*;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/3/14 7:40 下午
 * @Version 1.0
 **/
public class PsiTypeResolverHelper {

    private static Map<String, List<BaseInfo>> hadResolvedInfo = new HashMap<>();

    public static void clearResolvedInfoCache() {
        hadResolvedInfo.clear();
    }

    public static void parsePsiType(BaseInfo baseInfo, PsiType psiType, Stack<String> types) {

        baseInfo.setTypeName(psiType.getPresentableText());

        // 获取list、set、array、map里的实际类型
        psiType = handleCollectionType(psiType);

        // 如果该类型已经解析过了
        if (hadResolvedInfo.containsKey(psiType.getCanonicalText())) {
            baseInfo.setSubTypeInfos(hadResolvedInfo.get(psiType.getCanonicalText()));
            return;
        }


        // 如果是基本类型
        Object defaultValue = getJavaBaseTypeDefaultValue(psiType.getPresentableText());
        if (defaultValue != null) {
            baseInfo.setSubTypeInfos(new ArrayList<>(0));
            return;
        }
        if (!(psiType instanceof PsiClassReferenceType)) {
            // todo impossible: 不是基本类型，则一定会是 PsiClassReferenceType
            return;
        }

        // 如果类包含自身对象成员
        for (String type : types) {
            if (type.equals(psiType.getCanonicalText())) {
                return;
            }
        }
        types.push(psiType.getCanonicalText());

        // 如果是自定义类型
        List<BaseInfo> baseInfoList = new ArrayList<>();
        PsiSubstitutor psiSubstitutor;
        PsiClass psiClass;
        do {
            psiSubstitutor = ((PsiClassReferenceType) psiType).resolveGenerics().getSubstitutor();
            psiClass = ((PsiClassReferenceType) psiType).resolve();

            PsiField[] allFields = psiClass.getFields();
            for (PsiField subPsiField : allFields) {

                BaseInfo subBaseInfo = new BaseInfo();
                PsiType subTruePsiType = psiSubstitutor.substitute(subPsiField.getType());

                // 0、解析基本信息
                subBaseInfo.setName(subPsiField.getName());

                // 1、解析psiField 的javaDoc注释
                PsiDocComment psiDocCommentOnField = subPsiField.getDocComment();
                subBaseInfo.setDescription(CommentResolverServiceHelper.getDescriptionInJavaDoc(psiDocCommentOnField));

                // 2、解析psiField 的注解信息 todo


                parsePsiType(subBaseInfo, subTruePsiType, types);

                baseInfoList.add(subBaseInfo);
            }

            psiType = (PsiType) psiClass.getSuperClassType();
            if (Objects.isNull(psiType) || psiType.getCanonicalText().equals(JAVA_LANG_OBJECT)) {
                break;
            }

        } while (true);

        baseInfo.setSubTypeInfos(baseInfoList);
        hadResolvedInfo.put(psiType.getCanonicalText(), baseInfoList);
        types.pop();
    }

    /**
     * 判断是否是基本类型
     *
     * @param paramType
     * @return
     */
    @Nullable
    public static Object getJavaBaseTypeDefaultValue(String paramType) {
        Object paramValue = null;
        switch (paramType.toLowerCase()) {
            case "byte":
                paramValue = Byte.valueOf("1");
                break;
            case "char":
                paramValue = Character.valueOf('Z');
                break;
            case "character":
                paramValue = Character.valueOf('Z');
                break;
            case "boolean":
                paramValue = Boolean.TRUE;
                break;
            case "int":
                paramValue = Integer.valueOf(1);
                break;
            case "integer":
                paramValue = Integer.valueOf(1);
                break;
            case "double":
                paramValue = Double.valueOf(1);
                break;
            case "float":
                paramValue = Float.valueOf(1.0F);
                break;
            case "long":
                paramValue = Long.valueOf(1L);
                break;
            case "short":
                paramValue = Short.valueOf("1");
                break;
            case "bigdecimal":
                return BigDecimal.ONE;
            case "string":
                paramValue = "demoData";
                break;
            case "date":
                paramValue = DateFormatUtil.formatDateTime(new Date());
                break;
        }
        return paramValue;
    }

    /**
     *
     * @param psiType
     * @return
     */
    public static PsiType handleCollectionType(PsiType psiType) {
        if (!(psiType instanceof PsiClassReferenceType)) {
            return psiType;
        }
        PsiType trueType = psiType;
        if (isArray(psiType)) {
            trueType =  handleArrayType(psiType);
        } else if (isList((PsiClassReferenceType) psiType) || isSet((PsiClassReferenceType) psiType)) {
            trueType = handleListOrSetType(psiType);
        } else if (isMap((PsiClassReferenceType) psiType)) {
            trueType = handleMapValueType(psiType);
        } else { // 如果trueType不是array、set、list、map，则返回
            return trueType;
        }
        // 递归解析真实类型
        return handleCollectionType(trueType);
    }

    /**
     * 解析map类型，key只能为基本类型
     * @param psiTypeMap
     * @return
     */
    public static PsiType handleMapValueType(PsiType psiTypeMap) {
        if (!isMap((PsiClassReferenceType) psiTypeMap)) {
            return psiTypeMap;
        }
        return ((PsiClassReferenceType) psiTypeMap).getParameters()[1];
    }

    /**
     * 解析普通数据类的真实TYPE
     * @param psiType
     * @return
     */
    @NonNull
    public static PsiType handleArrayType(PsiType psiType) {
        if (!(psiType instanceof PsiArrayType)) {
            return psiType;
        }
//        PsiType trueType = psiType;
//        if (trueType instanceof PsiArrayType) {
//            // 如果是数组，需要提取真实的类型
//            while (trueType instanceof PsiArrayType) {
//                trueType = ((PsiArrayType) trueType).getComponentType();
//            }
//        }
//        return handleArrayType(((PsiArrayType) psiType).getComponentType());
        return ((PsiArrayType) psiType).getComponentType();
    }

    /**
     * 获取set、List的实际类型
     * @param psiType
     * @return
     */
    @NonNull
    public static PsiType handleListOrSetType(PsiType psiType) {
        if (!isList((PsiClassReferenceType) psiType) && !isSet((PsiClassReferenceType) psiType)) {
            return psiType;
        }
        return ((PsiClassReferenceType) psiType).getParameters()[0];
    }

    /**
     * 是否是数组
     * @param psiType
     * @return
     */
    public static boolean isArray(@NonNull PsiType psiType) {
        return (psiType instanceof PsiArrayType);
    }

    /**
     * 如果某个类实现了 JAVA_UTIL_LIST, 则该类就是list
     * @param psiType
     * @return
     */
    public static boolean isList(@NonNull PsiClassReferenceType psiType) {
        return isImplementedOneClass(psiType, JAVA_UTIL_LIST);
    }

    /**
     * 判断某个类是否是map类型
     * @param psiType
     * @return
     */
    public static boolean isMap(@NonNull PsiClassReferenceType psiType) {
        return isImplementedOneClass(psiType, JAVA_UTIL_MAP);
    }

    /**
     * 判断某个类是否是set类型
     * @param psiType
     * @return
     */
    public static boolean isSet(@NonNull PsiClassReferenceType psiType) {
        return isImplementedOneClass(psiType, JAVA_UTIL_SET);
    }

    /**
     * 某类型是否实现了某个类
     * @param psiType
     * @param qualifiedNameOfInterface
     * @return
     */
    public static boolean isImplementedOneClass(@NonNull PsiClassReferenceType psiType, String qualifiedNameOfInterface) {
        if (qualifiedNameOfInterface.equals(psiType.resolve().getQualifiedName())) {
            return true;
        }
        PsiClassType[] implementsListTypes = psiType.resolve().getImplementsListTypes();
        for (PsiClassType implementsListType : implementsListTypes) {
            if (implementsListType.resolve().getQualifiedName().equals(qualifiedNameOfInterface)) {
                return true;
            }
        }
        return false;
    }
}