package top.hungrywu.enums;

import top.hungrywu.enums.annotations.SpringRequestMethodAnnotation;

/**
 * @Description 函数参数类型如果为枚举中的其中一个，则跳过该参数解析
 * @Author daviswujiahao
 * @Date 2020/3/2 5:45 下午
 * @Version 1.0
 **/
public enum  IgnoreParsingParaTypeEnum {

    HTTP_SERVLET_REQUEST("HttpServletRequest", "javax.servlet.http.HttpServletRequest"),
    HTTP_SERVLET_RESPONSE("HttpServletResponse", "javax.servlet.http.HttpServletResponse");

    IgnoreParsingParaTypeEnum(String shortName, String qualifiedName) {
        this.shortName = shortName;
        this.qualifiedName = qualifiedName;
    }

    private String shortName;
    private String qualifiedName;

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getShortName() {
        return shortName;
    }

    public static IgnoreParsingParaTypeEnum getByQualifiedName(String qualifiedName) {
        for (IgnoreParsingParaTypeEnum ignoreParsingParaType : IgnoreParsingParaTypeEnum.values()) {
            if (ignoreParsingParaType.getQualifiedName().equals(qualifiedName)) {
                return ignoreParsingParaType;
            }
        }
        return null;
    }

    public static IgnoreParsingParaTypeEnum getByShortName(String requestMapping) {
        for (IgnoreParsingParaTypeEnum ignoreParsingParaType : IgnoreParsingParaTypeEnum.values()) {
            if (ignoreParsingParaType.getQualifiedName().endsWith(requestMapping)) {
                return ignoreParsingParaType;
            }
        }
        return null;
    }
}
