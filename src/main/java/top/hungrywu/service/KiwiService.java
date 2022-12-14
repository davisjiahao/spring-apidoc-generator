package top.hungrywu.service;

import com.alibaba.fastjson.JSON;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
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
import top.hungrywu.bean.BaseInfo;
import top.hungrywu.bean.kiwi.UpdateKiwiPageRequestData;
import top.hungrywu.bean.kiwi.WikiPageResponse;
import top.hungrywu.bean.kiwi.NewWikiPageRequestData;
import top.hungrywu.config.KiwiConfig;
import top.hungrywu.exception.BizException;
import top.hungrywu.exception.BizExceptionEnum;
import top.hungrywu.toolwindow.ConsoleLogFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
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

    private CloseableHttpClient httpClient;

    public void buildOneApiDescOnWiki(@NotNull ApiDetail apiDetail) throws Exception {

        ConsoleLogFactory.addInfoLog("start build api={} on wiki", apiDetail);
        // 0、登录wiki获取http连接
        ConsoleLogFactory.addInfoLog("user={} start login wiki", KiwiConfig.KIWI_USER_NAME);
        this.httpClient = loginWiki4httpClient();
        if (this.httpClient == null) {
            ConsoleLogFactory.addErrorLog("login wiki failed，please check your network and username or password");
            return;
        }

        try {
            // 1、首先查找主页面下的所有页面信息
            ConsoleLogFactory.addInfoLog("found all sub pages under page={}", KiwiConfig.KIWI_ANCESTOR_ID);
            WikiPageResponse wikiPageResponse = queryAllPagesUnderOnePage(KiwiConfig.KIWI_ANCESTOR_ID);
            if (Objects.isNull(wikiPageResponse)) {
                ConsoleLogFactory.addErrorLog("page={} can not be found on wiki", KiwiConfig.KIWI_ANCESTOR_ID);
                return;
            }

            // 2、根据查找出的页面标题中是否包含某接口的url判断该接口是需要更新还是新建
            List<WikiPageResponse> children;
            if (Objects.isNull(wikiPageResponse.getChildren()) || Objects.isNull(wikiPageResponse.getChildren().getPage()) || Objects.isNull(wikiPageResponse.getChildren().getPage().getResults())) {
                children = Collections.emptyList();
            } else {
                children = wikiPageResponse.getChildren().getPage().getResults();
            }
            WikiPageResponse apiPage = null;
            for (WikiPageResponse child : children) {
                if (StringUtils.contains(child.getTitle(), StringUtils.join(apiDetail.getBaseUrl(), "|"))) {
                    apiPage = child;
                    break;
                }
            }
            WikiPageResponse pageResponse;
            String apiUrl = StringUtils.join(apiDetail.getBaseUrl(), "|");
            if (Objects.isNull(apiPage)) {
                // 为该api新建页面
                ConsoleLogFactory.addInfoLog("start create new doc for api={} on wiki", apiUrl);
                pageResponse = buildApi2NewPage(KiwiConfig.KIWI_ANCESTOR_ID, apiDetail);
            } else {
                // 修改
                ConsoleLogFactory.addInfoLog("start update doc for api={} on wiki page={}", apiUrl, apiPage.getId());
                pageResponse = updateApi2ExistedPage(apiDetail, apiPage);
            }
            if (Objects.isNull(pageResponse)) {
                ConsoleLogFactory.addErrorLog("failed build doc for api={} on wiki", apiUrl);
                return;
            }

            ConsoleLogFactory.addInfoLog("finished build doc for api={} on wiki page={}", apiUrl, pageResponse.getId());
            apiDetail.setApiContentUrl(KiwiConfig.WIKI_VIEW_BASE_URL + pageResponse.getId());

        } finally {
            if (this.httpClient != null) {
                this.httpClient.close();
            }
        }

    }

    /***
     *
     * @author : daviswujiahao 
     * @date : 2020/6/27 12:24 下午
     * @param apiDetails :
     * @return : void
     **/
    public void buildApiDescsOnWiki(@NotNull List<ApiDetail> apiDetails) throws Exception {

        ConsoleLogFactory.addInfoLog("start build apis={} on wiki", apiDetails);
        // 0、登录wiki获取http连接
        ConsoleLogFactory.addInfoLog("user={} start login wiki", KiwiConfig.KIWI_USER_NAME);
        this.httpClient = loginWiki4httpClient();
        if (this.httpClient == null) {
            ConsoleLogFactory.addErrorLog("login wiki failed，please check your network and username or password");
            return;
        }

        try {
            // 1、首先查找主页面下的所有页面信息
            ConsoleLogFactory.addInfoLog("found all sub pages under page={}", KiwiConfig.KIWI_ANCESTOR_ID);
            WikiPageResponse wikiPageResponse = queryAllPagesUnderOnePage(KiwiConfig.KIWI_ANCESTOR_ID);
            if (Objects.isNull(wikiPageResponse)) {
                ConsoleLogFactory.addErrorLog("page={} can not be found on wiki", KiwiConfig.KIWI_ANCESTOR_ID);
                return;
            }

            // 2、根据查找出的页面标题中是否包含某接口的url判断该接口是需要更新还是新建
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
                String apiUrl = StringUtils.join(apiDetail.getBaseUrl(), "|");
                if (Objects.isNull(apiPage)) {
                    // 为该api新建页面
                    ConsoleLogFactory.addInfoLog("start create new doc for api={} on wiki", apiUrl);
                    pageResponse = buildApi2NewPage(KiwiConfig.KIWI_ANCESTOR_ID, apiDetail);
                } else {
                    // 修改
                    ConsoleLogFactory.addInfoLog("start update doc for api={} on wiki page={}", apiUrl, apiPage.getId());
                    pageResponse = updateApi2ExistedPage(apiDetail, apiPage);
                }
                if (Objects.isNull(pageResponse)) {
                    ConsoleLogFactory.addErrorLog("failed build doc for api={} on wiki", apiUrl);
                    continue;
                }

                ConsoleLogFactory.addInfoLog("finished build doc for api={} on wiki page={}", apiUrl, pageResponse.getId());
                apiDetail.setApiContentUrl(KiwiConfig.WIKI_VIEW_BASE_URL + pageResponse.getId());
            }

        } finally {
            if (this.httpClient != null) {
                this.httpClient.close();
            }
        }

    }

    /**
     * 在kiwi上建立接口文档页面
     *
     * @param apiDoc
     */
    public void buildApiDocOnWiki(@NonNull ApiDoc apiDoc) throws Exception {

        ConsoleLogFactory.addInfoLog("start build api doc={} on wiki", apiDoc);

        // 0、登录wiki获取http连接
        ConsoleLogFactory.addInfoLog("user={} start login wiki", KiwiConfig.KIWI_USER_NAME);
        this.httpClient = loginWiki4httpClient();
        if (this.httpClient == null) {
            ConsoleLogFactory.addErrorLog("login wiki failed，please check your network and username or password");
            return;
        }

        try {
            // 1、首先查找主页面下的所有页面信息
            ConsoleLogFactory.addInfoLog("found all sub pages under page={}", KiwiConfig.KIWI_ANCESTOR_ID);
            WikiPageResponse wikiPageResponse = queryAllPagesUnderOnePage(KiwiConfig.KIWI_ANCESTOR_ID);
            if (Objects.isNull(wikiPageResponse)) {
                ConsoleLogFactory.addErrorLog("page={} can not be found on wiki", KiwiConfig.KIWI_ANCESTOR_ID);
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
                String apiUrl = StringUtils.join(apiDetail.getBaseUrl(), "|");
                if (Objects.isNull(apiPage)) {
                    // 为该api新建页面
                    ConsoleLogFactory.addInfoLog("start create new doc for api={} on wiki", apiUrl);
                    pageResponse = buildApi2NewPage(KiwiConfig.KIWI_ANCESTOR_ID, apiDetail);
                } else {
                    // 修改
                    ConsoleLogFactory.addInfoLog("start update doc for api={} on wiki page={}", apiUrl, apiPage.getId());
                    pageResponse = updateApi2ExistedPage(apiDetail, apiPage);
                }
                if (Objects.isNull(pageResponse)) {
                    ConsoleLogFactory.addErrorLog("failed build doc for api={} on wiki", apiUrl);
                    continue;
                }

                ConsoleLogFactory.addInfoLog("finished build doc for api={} on wiki page={}", apiUrl, pageResponse.getId());
                apiDetail.setApiContentUrl(KiwiConfig.WIKI_VIEW_BASE_URL + pageResponse.getId());
            }

            // 3、构建api汇总信息页面
           /* ConsoleLogFactory.addInfoLog("start update summer doc for all on wiki page={}", KiwiConfig.KIWI_ANCESTOR_ID);
            WikiPageResponse pageResponse = buildApiSummerPageOnWiki(apiDoc, wikiPageResponse);
            if (Objects.isNull(pageResponse)) {
                // todo log error
            }*/

        } finally {
            if (this.httpClient != null) {
                this.httpClient.close();
            }
        }

        ConsoleLogFactory.addInfoLog("finished build all api doc on wiki");
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
     * @param wikiPageResponse :
     * @param apiDetail        :
     * @return : top.hungrywu.bean.kiwi.WikiPageResponse
     * @author : daviswujiahao
     * @date : 2020/3/27 2:05 下午
     **/
    private WikiPageResponse updateApi2ExistedPage(ApiDetail apiDetail, WikiPageResponse wikiPageResponse) throws Exception {
        String content = buildApiDetailKiwiPageContent(apiDetail);
        String title = apiDetail.getDescription() + ":" + StringUtils.join(apiDetail.getBaseUrl(), "|");
        String pageId = wikiPageResponse.getId();
        int versionNum = wikiPageResponse.getVersion().getNumber();

        return updateKiwiPage(pageId, title, content, versionNum);

    }


    /**
     * @param kiwiAncestorsId :
     * @param apiDetail       :
     * @return : top.hungrywu.bean.kiwi.WikiPageResponse
     * @author : daviswujiahao
     * @date : 2020/3/27 2:05 下午
     **/
    private WikiPageResponse buildApi2NewPage(String kiwiAncestorsId, ApiDetail apiDetail) throws Exception {
        String content = buildApiDetailKiwiPageContent(apiDetail);
        String title = apiDetail.getDescription() + ":" + StringUtils.join(apiDetail.getBaseUrl(), "|");
        ConsoleLogFactory.addInfoLog(title);
        ConsoleLogFactory.addInfoLog(content);
        return createNewKiwiPage(title, content);
    }

    /**
     * 登录kiwi后获取httpclient，用于后续操作
     *
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
        formparams.add(new BasicNameValuePair("os_cookie", "true"));
        httpPost.setEntity(new UrlEncodedFormEntity(formparams, StandardCharsets.UTF_8));

        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            String content = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
            if (response.getStatusLine().getStatusCode() == HTTP_SUCCESS_CODE) {
                return httpClient;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (Objects.nonNull(response)) {
                response.close();
            }
        }
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

        HttpPut httpPut = new HttpPut(KiwiConfig.WIKI_HOST + KiwiConfig.WIKI_CONTENT_API_BASE_URL + "/" + pageId);

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

        if (Objects.isNull(httpClient)) {
            return null;
        }

        CloseableHttpResponse response = httpClient.execute(httpPut);
        String content = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                .lines().collect(Collectors.joining(System.lineSeparator()));
        try {
            if (response.getStatusLine().getStatusCode() != HTTP_SUCCESS_CODE) {
                return null;
            }
            return JSON.parseObject(content, WikiPageResponse.class);
        } finally {
            response.close();
        }
    }

    /**
     * 在kiwi上新建page
     *
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


        if (Objects.isNull(httpClient)) {
            return null;
        }
        CloseableHttpResponse response = httpClient.execute(httpPost);
        String content = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                .lines().collect(Collectors.joining(System.lineSeparator()));
        try {
            if (response.getStatusLine().getStatusCode() != HTTP_SUCCESS_CODE) {
                return null;
            }
            return JSON.parseObject(content, WikiPageResponse.class);
        } finally {
            response.close();
        }
    }

    /***
     *
     * @author : daviswujiahao
     * @date : 2020/3/27 2:20 下午
     * @param apiDetail :
     * @return : java.lang.String
     **/
    private String buildApiDetailKiwiPageContent(@NonNull ApiDetail apiDetail) {

        String apiName = StringUtils.defaultIfEmpty(apiDetail.getDescription(), "");
        String apiBaseUrl = StringUtils.defaultIfEmpty(StringUtils.join(apiDetail.getBaseUrl(), "|"), "");
        String protocolName = StringUtils.defaultIfEmpty(apiDetail.getProtocolName(), "");
        String apiMethod = StringUtils.defaultIfEmpty(StringUtils.join(apiDetail.getMethodType(), "|"), "");
        String contentType = StringUtils.defaultIfEmpty(apiDetail.getContentType(), "");

        StringBuffer content = new StringBuffer();

        // 基本信息
        content.append(String.format(KiwiConfig.WIKI_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "接口描述", apiName));
        content.append(String.format(KiwiConfig.WIKI_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "访问地址", apiBaseUrl));
        content.append(String.format(KiwiConfig.WIKI_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "协议类型", protocolName));
        content.append(String.format(KiwiConfig.WIKI_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "访问方式", apiMethod));
        content.append(String.format(KiwiConfig.WIKI_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "Content-type", contentType));

        if (CollectionUtils.isEmpty(apiDetail.getParams())) {
            return content.toString();
        }

        BaseInfo firstParam = new BaseInfo();
        firstParam.setTypeName("参数列表");
        firstParam.setTypeName4TableTitle("参数列表");
        if (apiDetail.getParams().size() == 1) {
            firstParam.setSubTypeInfos(apiDetail.getParams().get(0).getSubTypeInfos());
        } else {
            firstParam.setSubTypeInfos(new ArrayList<>(apiDetail.getParams()));
        }

        content.append(buildKiwiContentForFieldType(firstParam));

        apiDetail.getResult().setTypeName("返回值列表");
        apiDetail.getResult().setTypeName4TableTitle("返回值列表");
        content.append(buildKiwiContentForFieldType(apiDetail.getResult()));

        return content.toString();
    }

    private String buildKiwiContentForFieldType(BaseInfo fieldInfo) {

        StringBuffer content = new StringBuffer();

        Set<String> hadBuildTypes = new HashSet<>();
        Queue<BaseInfo> listQueue = new ArrayDeque<>();
        listQueue.add(fieldInfo);
        while (!listQueue.isEmpty()) {

            BaseInfo nowTypeInfos = listQueue.poll();
            if (CollectionUtils.isEmpty(nowTypeInfos.getSubTypeInfos())) {
                continue;
            }

            StringBuffer rowData = new StringBuffer();
            for (BaseInfo nowTypeInfo : nowTypeInfos.getSubTypeInfos()) {

                String fieldName = StringUtils.defaultIfEmpty(nowTypeInfo.getName(), "");
                String fieldType = StringUtils.defaultIfEmpty(nowTypeInfo.getTypeName(), "");
                String fieldDescription = StringUtils.defaultIfEmpty(nowTypeInfo.getDescription(), "");
                boolean fieldRequired = nowTypeInfo.isRequired();

                rowData.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                        fieldName, StringEscapeUtils.escapeHtml4(fieldType), fieldRequired, fieldDescription));

                if (!hadBuildTypes.contains(nowTypeInfo.getTypeName())) {
                    listQueue.add(nowTypeInfo);
                }
            }

            content.append(String.format(KiwiConfig.WIKI_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE,
                    StringEscapeUtils.escapeHtml4(nowTypeInfos.getTypeName4TableTitle()), ""));
            content.append("<table><thead><tr><th>属性名称</th><th>属性类型</th><th>是否必填</th><th>属性描述</th></tr></thead>");
            content.append("<tbody>");
            content.append(rowData);
            content.append("</tbody>");
            content.append("</table>");

            hadBuildTypes.add(nowTypeInfos.getTypeName());
        }

        return content.toString();
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
        uriBuilder.addParameter("expand", "children.page.version");
        // todo 一页最多100，暂时只最多查询100个页面
        uriBuilder.addParameter("start", "0");
        uriBuilder.addParameter("limit", "100");

        HttpGet httpGet = new HttpGet(uriBuilder.build());


        httpGet.setHeader("Content-type", "application/json;charset=utf8");
        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("X-Atlassian-Token", "no-check");

        WikiPageResponse wikiPageResponse;

        // todo 复用httpClient
        if (Objects.isNull(httpClient)) {
            // todo error handler
        }

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

        return wikiPageResponse;

    }


}
