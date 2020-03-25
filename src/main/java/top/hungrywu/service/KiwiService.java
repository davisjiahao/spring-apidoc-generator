package top.hungrywu.service;

import com.alibaba.fastjson.JSON;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import top.hungrywu.bean.WikiRequestData;
import top.hungrywu.config.KiwiConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    public void createNewPage(String pageContent, String pageTitle) throws Exception {


        HttpPost httpPost = new HttpPost(KiwiConfig.WIKI_HOST + KiwiConfig.WIKI_CONTENT_API_BASE_URL);

        CloseableHttpClient httpClient = loginWiki4httpClient();
        if (Objects.isNull(httpClient)) {
            // todo error handler
        }

        WikiRequestData requestData = WikiRequestData.builder()
                .title(pageTitle)
                .space(WikiRequestData.SpaceBean.builder()
                        .key(KiwiConfig.KIWI_SPACE_KEY)
                        .build())
                .ancestors(Stream.of(WikiRequestData.AncestorsBean.builder()
                        .id(KiwiConfig.KIWI_ANCESTORS_ID)
                        .build())
                        .collect(toList()))
                .type("page")
                .body(WikiRequestData.BodyBean.builder().
                        storage(WikiRequestData.BodyBean.StorageBean.builder()
                                .representation("storage")
                                .value(pageContent)
                                .build())
                        .build())
                .build();

        httpPost.setEntity(new StringEntity(JSON.toJSONString(requestData), StandardCharsets.UTF_8));

        httpPost.setHeader("Content-type", "application/json;charset=utf8");
        httpPost.setHeader("X-Atlassian-Token", "no-check");

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String content = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
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
    }

}
