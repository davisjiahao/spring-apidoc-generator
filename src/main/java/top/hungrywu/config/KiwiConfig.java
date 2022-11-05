package top.hungrywu.config;

import org.apache.commons.lang3.StringUtils;
import top.hungrywu.toolwindow.ConsoleLogFactory;

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
    public static String WIKI_VIEW_BASE_URL = "/pages/viewpage.action?pageId=";


    public static String KIWI_USER_NAME = "";
    public static String KIWI_USER_PASSWORD = "";

    public static String KIWI_SPACE_KEY = "";
    public static String KIWI_ANCESTOR_ID = "";

    public static String WIKI_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE = "<p><strong>%s</strong>:%s</p>";

    public static boolean validConfig() {
        return true;
    }

}
