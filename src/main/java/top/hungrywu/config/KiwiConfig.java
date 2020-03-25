package top.hungrywu.config;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/3/25 5:25 下午
 * @Version 1.0
 **/
public class KiwiConfig {
    public static String WIKI_HOST = "http://wiki.intra.xiaojukeji.com";
    public static String WIKI_CONTENT_API_BASE_URL = "/rest/api/content";
    public static String WIKI_LOGIN_BASE_URL = "/dologin.action";
    public static String KIWI_USER_NAME = "daviswujiahao";
    public static String KIWI_USER_PASSWORD = "TYRoqr*969";

    public static String KIWI_SPACE_KEY = "~daviswujiahao";
    public static String KIWI_ANCESTORS_ID = "339095512";
    public static String CREATE_PAGE_REQUEST_BODY_TEMPLATE = "{\"type\":\"page\",\"ancestors\":[{\"id\":%s}],\"title\":\"%s\",\"space\":{\"key\":\"~daviswujiahao\"},\"body\":{\"storage\":{\"value\":\"%s\",\"representation\":\"storage\"}}}";
}
