package top.hungrywu.exception;

import lombok.AllArgsConstructor;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/4/11 9:48 下午
 * @Version 1.0
 **/
@AllArgsConstructor
public enum BizExceptionEnum {

    UNKNOWN_EXCEPTION("unknown_exception","未知异常"),
    INTERNET_EXCEPTION("internet_exception","网络异常,请重试"),
    LOGIN_WIKI_EXCEPTION("login_wiki_exception","wiki登录失败，请检查账号密码和网络配置后重新"),
    SYSTEM_EXCEPTION("system_exception","系统异常");

    public final String code;

    public final String message;
}
