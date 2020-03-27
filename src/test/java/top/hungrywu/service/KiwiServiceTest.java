package top.hungrywu.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import top.hungrywu.bean.ApiDetail;
import top.hungrywu.bean.ApiDoc;
import top.hungrywu.config.KiwiConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KiwiServiceTest {

    private KiwiService kiwiService;

    @Before
    public void setUp() throws Exception {
        kiwiService = new KiwiService();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testLoginWiki() throws Exception {
        kiwiService.loginWiki4httpClient();
    }

    @Test
    public void testCreateNewPage() throws Exception {
        List<ApiDetail> apiDetailList = new ArrayList<>();
        apiDetailList.add(new ApiDetail().setProtocolName("http").setDescription("test"));
        String content = kiwiService.buildApiSummerKiwiPageContent(new ApiDoc()
                .setProjectName("venus")
                .setBranchName("test")
                .setCommitVersion("123345")
                .setApiDetails(apiDetailList));

        kiwiService.createNewKiwiPage(new Date().toString(), content);
    }

    @Test
    public void testQueryAllPagesUnderOnePage() throws Exception {
        kiwiService.queryAllPagesUnderOnePage(KiwiConfig.KIWI_ANCESTOR_ID);
    }

}