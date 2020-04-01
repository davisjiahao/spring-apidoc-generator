package top.hungrywu.bean;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/3/28 10:20 下午
 * @Version 1.0
 **/
@Accessors(chain = true)
@Data
public class ParamInfo {
    private String paramName;
    private PsiType paraType;
    private boolean requestWrapped;
    private String description;
}
