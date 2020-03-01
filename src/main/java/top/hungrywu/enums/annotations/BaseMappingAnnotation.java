package top.hungrywu.enums.annotations;

public interface BaseMappingAnnotation {
    /**
     * 获得注解的完整类型名称
     * @return
     */
    public String getQualifiedName() ;

    /**
     * 获得注解的完成类型名称
     * @return
     */
    public String getShortName();
}
