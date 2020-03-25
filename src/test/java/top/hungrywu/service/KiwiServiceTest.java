package top.hungrywu.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        kiwiService.createNewPage("example", "test-example");
    }
}