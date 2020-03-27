package top.hungrywu.service;

import com.alibaba.fastjson.JSON;
import com.sun.istack.Nullable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.assertj.core.annotations.NonNull;
import top.hungrywu.bean.ApiDetail;
import top.hungrywu.bean.ApiDoc;
import top.hungrywu.bean.kiwi.UpdateKiwiPageRequestData;
import top.hungrywu.bean.kiwi.WikiPageResponse;
import top.hungrywu.bean.kiwi.NewWikiPageRequestData;
import top.hungrywu.config.KiwiConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @Description 和wiki交互的业务类
 * @Author daviswujiahao
 * @Date 2020/3/25 10:50 上午
 * @Version 1.0
 **/

public class KiwiService {

    private final int HTTP_SUCCESS_CODE = 200;


    /**
     * 在kiwi上建立接口文档页面
     * @param apiDoc
     */
    public void buildApiDocOnWiki(@NonNull ApiDoc apiDoc) throws Exception {
        // 1、首先查找主页面下的所有页面信息
        WikiPageResponse wikiPageResponse = queryAllPagesUnderOnePage(KiwiConfig.KIWI_ANCESTOR_ID);
        if (Objects.isNull(wikiPageResponse)) {
            // todo error: 主页面未配置或者配置出错
            return;
        }

        // 2、根据查找出的页面标题中是否包含某接口的url判断该接口是需要更新还是新建
        List<ApiDetail> apiDetails = Objects.isNull(apiDoc.getApiDetails()) ? Collections.EMPTY_LIST : apiDoc.getApiDetails();
        List<WikiPageResponse> children;
        if (Objects.isNull(wikiPageResponse.getChildren()) || Objects.isNull(wikiPageResponse.getChildren().getPage()) || Objects.isNull(wikiPageResponse.getChildren().getPage().getResults())) {
            children = Collections.emptyList();
        } else {
            children = wikiPageResponse.getChildren().getPage().getResults();
        }
        for (ApiDetail apiDetail : apiDetails) {
            WikiPageResponse apiPage = null;
            for (WikiPageResponse child : children) {
                if (StringUtils.contains(child.getTitle(), StringUtils.join(apiDetail.getBaseUrl(), "|"))) {
                    apiPage = child;
                    break;
                }
            }
            WikiPageResponse pageResponse;
            if (Objects.isNull(apiPage)) {
                // 为该api新建页面
                pageResponse = buildApi2NewPage(KiwiConfig.KIWI_ANCESTOR_ID, apiDetail);
            } else {
                // 修改
                pageResponse = updateApi2ExistedPage(apiDetail, apiPage);
            }
            if (Objects.isNull(pageResponse)) {
                // todo log error
                continue;
            }
            apiDetail.setApiContentUrl(KiwiConfig.WIKI_VIEW_BASE_URL + pageResponse.getId());
        }

        // 3、构建api汇总信息页面
        WikiPageResponse pageResponse = buildApiSummerPageOnWiki(apiDoc, wikiPageResponse);
        if (Objects.isNull(pageResponse)) {
            // todo log error
        }

    }

    /***
     *
     * @author : daviswujiahao 
     * @date : 2020/3/27 2:05 下午
     * @param wikiPageResponse :
     * @param apiDoc :  
     * @return : top.hungrywu.bean.kiwi.WikiPageResponse
     **/
    private WikiPageResponse buildApiSummerPageOnWiki(ApiDoc apiDoc, WikiPageResponse wikiPageResponse) throws Exception {
        String content = buildApiSummerKiwiPageContent(apiDoc);
        String title = wikiPageResponse.getTitle();
        String apiSummerPageId = wikiPageResponse.getId();
        int versionNum = wikiPageResponse.getVersion().getNumber();

        return updateKiwiPage(apiSummerPageId, title, content, versionNum);
    }

    /**
     *
     * @author : daviswujiahao 
     * @date : 2020/3/27 2:05 下午
     * @param wikiPageResponse :
     * @param apiDetail :  
     * @return : top.hungrywu.bean.kiwi.WikiPageResponse
     **/
    private WikiPageResponse updateApi2ExistedPage(ApiDetail apiDetail, WikiPageResponse wikiPageResponse) throws Exception {
        String content = buildApiDetailKiwiPageContent(apiDetail);
        String title = apiDetail.getAuthor() + ":" + StringUtils.join(apiDetail.getBaseUrl(), "|");
        String pageId = wikiPageResponse.getId();
        int versionNum = wikiPageResponse.getVersion().getNumber();

        return updateKiwiPage(pageId, title, content, versionNum);

    }

    /***
     *
     * @author : daviswujiahao
     * @date : 2020/3/27 2:20 下午
     * @param apiDetail :
     * @return : java.lang.String
     **/
    private String buildApiDetailKiwiPageContent(ApiDetail apiDetail) {
        return "todo";
    }

    /**
     *
     * @author : daviswujiahao 
     * @date : 2020/3/27 2:05 下午
     * @param kiwiAncestorsId : 
     * @param apiDetail :  
     * @return : top.hungrywu.bean.kiwi.WikiPageResponse
     **/
    private WikiPageResponse buildApi2NewPage(String kiwiAncestorsId, ApiDetail apiDetail) throws Exception {
        String content = buildApiDetailKiwiPageContent(apiDetail);
        String title = apiDetail.getAuthor() + ":" + StringUtils.join(apiDetail.getBaseUrl(), "|");
        return createNewKiwiPage(title, content);
    }

    /**
     * 登录kiwi后获取httpclient，用于后续操作
     * @return null：登录失败返回null
     * @throws Exception
     */
    @Nullable
    public CloseableHttpClient loginWiki4httpClient() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        HttpPost httpPost = new HttpPost(KiwiConfig.WIKI_HOST + KiwiConfig.WIKI_LOGIN_BASE_URL);
        httpPost.setConfig(requestConfig);

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("os_username", KiwiConfig.KIWI_USER_NAME));
        formparams.add(new BasicNameValuePair("os_password", KiwiConfig.KIWI_USER_PASSWORD));
        httpPost.setEntity(new UrlEncodedFormEntity(formparams, StandardCharsets.UTF_8));

        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");

        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            String content = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
            if (response.getStatusLine().getStatusCode() == HTTP_SUCCESS_CODE) {
                return httpClient;
            } else {
                // todo error log
            }
        } finally {
            response.close();
        }
        return null;
    }

    /***
     * 更新kiwi page
     * @author : daviswujiahao
     * @date : 2020/3/27 2:03 下午
     * @param pageId :
     * @param pageTitle :
     * @param pageContent :
     * @param oldVersionNum :
     * @return : top.hungrywu.bean.kiwi.WikiPageResponse
     **/
    public WikiPageResponse updateKiwiPage(String pageId, String pageTitle, String pageContent, int oldVersionNum) throws Exception {

        HttpPut httpPut = new HttpPut(KiwiConfig.WIKI_HOST + KiwiConfig.WIKI_CONTENT_API_BASE_URL + "/" +pageId);

        UpdateKiwiPageRequestData requestData = UpdateKiwiPageRequestData.builder()
                .title(pageTitle)
                .space(UpdateKiwiPageRequestData.SpaceBean.builder()
                        .key(KiwiConfig.KIWI_SPACE_KEY)
                        .build())
                .type("page")
                .body(UpdateKiwiPageRequestData.BodyBean.builder().
                        storage(UpdateKiwiPageRequestData.BodyBean.StorageBean.builder()
                                .representation("storage")
                                .value(pageContent)
                                .build())
                        .build())
                .id(pageId)
                .version(UpdateKiwiPageRequestData.VersionBean.builder()
                        .number(oldVersionNum + 1)
                        .build())
                .build();

        httpPut.setEntity(new StringEntity(JSON.toJSONString(requestData), StandardCharsets.UTF_8));

        httpPut.setHeader("Content-type", "application/json;charset=utf8");
        httpPut.setHeader("Accept", "application/json");
        httpPut.setHeader("X-Atlassian-Token", "no-check");

        // todo 复用httpClient
        CloseableHttpClient httpClient = loginWiki4httpClient();
        if (Objects.isNull(httpClient)) {
            // todo error handler
        }
        try {

            CloseableHttpResponse response = httpClient.execute(httpPut);
            String content = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
            try {
                if (response.getStatusLine().getStatusCode() != HTTP_SUCCESS_CODE) {
                    // todo error
                }
                return JSON.parseObject(content, WikiPageResponse.class);
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }

    }

    /**
     * 在kiwi上新建page
     * @param pageContent
     * @param pageTitle
     * @throws Exception
     */
    public WikiPageResponse createNewKiwiPage(String pageTitle, String pageContent) throws Exception {


        HttpPost httpPost = new HttpPost(KiwiConfig.WIKI_HOST + KiwiConfig.WIKI_CONTENT_API_BASE_URL);

        NewWikiPageRequestData requestData = NewWikiPageRequestData.builder()
                .title(pageTitle)
                .space(NewWikiPageRequestData.SpaceBean.builder()
                        .key(KiwiConfig.KIWI_SPACE_KEY)
                        .build())
                .ancestors(Stream.of(NewWikiPageRequestData.AncestorsBean.builder()
                        .id(KiwiConfig.KIWI_ANCESTOR_ID)
                        .build())
                        .collect(toList()))
                .type("page")
                .body(NewWikiPageRequestData.BodyBean.builder().
                        storage(NewWikiPageRequestData.BodyBean.StorageBean.builder()
                                .representation("storage")
                                .value(pageContent)
                                .build())
                        .build())
                .build();

        httpPost.setEntity(new StringEntity(JSON.toJSONString(requestData), StandardCharsets.UTF_8));

        httpPost.setHeader("Content-type", "application/json;charset=utf8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("X-Atlassian-Token", "no-check");

        // todo 复用httpClient
        CloseableHttpClient httpClient = loginWiki4httpClient();
        if (Objects.isNull(httpClient)) {
            // todo error handler
        }
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String content = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
            try {
                if (response.getStatusLine().getStatusCode() != HTTP_SUCCESS_CODE) {
                    // todo error
                }
                return JSON.parseObject(content, WikiPageResponse.class);
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }


    /***
     * 构建接口汇总信息页面的内容
     * @author : daviswujiahao
     * @date : 2020/3/26 3:17 下午
     * @param apiDoc :
     * @return : java.lang.String
     **/
    public String buildApiSummerKiwiPageContent(@NonNull ApiDoc apiDoc) {

        String projectName = apiDoc.getProjectName();
        String gitBranchName = apiDoc.getBranchName();
        String gitCommitVersion = apiDoc.getCommitVersion();

        StringBuffer content = new StringBuffer();

        // 基本信息
        content.append(String.format(KiwiConfig.WIKI_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "项目名称", projectName));
        content.append(String.format(KiwiConfig.WIKI_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "git分支", gitBranchName));
        content.append(String.format(KiwiConfig.WIKI_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "git版本", gitCommitVersion));

        // api 列表
        if (CollectionUtils.isEmpty(apiDoc.getApiDetails())) {
            return content.toString();
        }

        StringBuffer rowData = new StringBuffer();
        int rowNum = 1;
        for (ApiDetail apiDetail : apiDoc.getApiDetails()) {
            String apiName = StringUtils.defaultIfEmpty(apiDetail.getDescription(), "");
            String apiBaseUrl = StringUtils.defaultIfEmpty(StringUtils.join(apiDetail.getBaseUrl(), "|"), "");
            String protocolName = StringUtils.defaultIfEmpty(apiDetail.getProtocolName(), "");
            String apiMethod = StringUtils.defaultIfEmpty(StringUtils.join(apiDetail.getMethodType(), "|"), "");
            rowData.append(String.format("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td><a href='%s'>接口详情</a></td></tr>",
                    rowNum++, apiName, apiBaseUrl, protocolName, apiMethod, apiDetail.getApiContentUrl()));
        }

        content.append(String.format(KiwiConfig.WIKI_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "接口列表", ""));
        content.append("<table><thead><tr><th>序号</th><th>接口描述</th><th>访问地址</th><th>协议类型</th><th>访问方式</th><th>接口详情</th></tr></thead>");
        content.append("<tbody>");
        content.append(rowData);
        content.append("</tbody>");
        content.append("</table>");

        return content.toString();
    }

    /***
     *
     * 获得某page直属子page信息(包括版本信息)
     * @author : daviswujiahao
     * @date : 2020/3/26 2:44 下午
     * @param parentPageId :  父page id
     * @return : top.hungrywu.bean.kiwi.WikiPageResponse
     *
     **/
    public WikiPageResponse queryAllPagesUnderOnePage(String parentPageId) throws Exception {

        URIBuilder uriBuilder = new URIBuilder(KiwiConfig.WIKI_HOST + KiwiConfig.WIKI_CONTENT_API_BASE_URL
                + "/" + parentPageId);
        uriBuilder.addParameter("expand", "version,children.page.version");
        // todo 一页最多100，暂时只最多查询100个页面
        uriBuilder.addParameter("start", "0");
        uriBuilder.addParameter("limit", "100");

        HttpGet httpGet = new HttpGet(uriBuilder.build());


        httpGet.setHeader("Content-type", "application/json;charset=utf8");
        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("X-Atlassian-Token", "no-check");

        WikiPageResponse wikiPageResponse;

        // todo 复用httpClient
        CloseableHttpClient httpClient = loginWiki4httpClient();
        if (Objects.isNull(httpClient)) {
            // todo error handler
        }

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String content = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));

            wikiPageResponse = JSON.parseObject(content, WikiPageResponse.class);

            try {
                if (response.getStatusLine().getStatusCode() != HTTP_SUCCESS_CODE) {
                    // todo error
                }
            } finally {
                response.close();
            }

        } finally {
            httpClient.close();
        }

        return wikiPageResponse;

    }



}
