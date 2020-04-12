package top.hungrywu.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import top.hungrywu.toolwindow.ConsoleLogFactory;

import java.util.Objects;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/4/11 9:45 下午
 * @Version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper=true)
public class BizException extends RuntimeException {

    private static final long serialVersionUID = -6979901566637669960L;

    private String code;

    private String message;


    public BizException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BizException(BizExceptionEnum exceptionEnum) {
        super(exceptionEnum.message);
        this.code = exceptionEnum.code;
        this.message = exceptionEnum.message;
    }

    public BizException(String code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
        this.message = message;
    }

    public BizException(BizExceptionEnum exceptionEnum, Throwable throwable) {
        super(exceptionEnum.message, throwable);
        this.code = exceptionEnum.code;
        this.message = exceptionEnum.message;
    }


    /**
     * @return void
     * @description 重写Throwable中printStackTrace方法，打印异常信息
     * @date 2019/8/21 下午7:57
     * @author flyingkid
     */
    @Override
    public void printStackTrace() {
        if (ConsoleLogFactory.logConsoleIsReady()) {
            ConsoleLogFactory.addErrorLog("错误代码: {}, 错误信息: {}", code, message);
        } else {
            super.printStackTrace();
        }
    }

}