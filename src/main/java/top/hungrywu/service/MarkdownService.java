package top.hungrywu.service;

import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import top.hungrywu.bean.ApiDetail;
import top.hungrywu.bean.ApiDoc;
import top.hungrywu.bean.BaseInfo;
import top.hungrywu.config.KiwiConfig;
import top.hungrywu.toolwindow.ConsoleLogFactory;
import top.hungrywu.util.GitUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * MD服务
 *
 * @author daviswujiahao
 * @date 2020/11/23 4:54 下午
 * @since 1.0
 **/
public class MarkdownService {

    private static String MD_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE = "> **%s**\n\n>>%s\n\n";
    private static String API_DIRECTORY = "apidoc";


    public void buildOneApiDetail(@NonNull ApiDetail apiDetail,
                                  GitUtils.VersionInfoByGit gitVersionInfo,
                                  String projectPath) {
        // 1、判断api文件夹是否存在
        File folder = new File(projectPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // 2、创建文件
        File apiFile = new File(projectPath + File.separator + API_DIRECTORY + apiDetail.getBaseUrl().get(0) + ".md");
        if (!apiFile.exists()) {
            File parentFile = new File(apiFile.getParent());
            ConsoleLogFactory.addInfoLog("start create folder={}", parentFile.getAbsolutePath());
            parentFile.mkdirs();
            try {
                ConsoleLogFactory.addInfoLog("start create file={}", apiFile.getAbsolutePath());
                parentFile.createNewFile();
            } catch (IOException e) {
                ConsoleLogFactory.addErrorLog("create parentFile={} error:{}",
                        apiFile.getAbsolutePath(), e.getMessage());
                return;
            }
        }
        // 3、构造api的markdown内容，写入文件
        String content = buildApiDetail(apiDetail, gitVersionInfo.getBranchName(), gitVersionInfo.getCommitId());
        ConsoleLogFactory.addInfoLog("start write api detail={} to markdown file={}", content, apiFile.getAbsolutePath());
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(apiFile);
            outStream.write(content.getBytes());
            outStream.close();	//关闭文件输出流
        } catch (Exception e) {
            ConsoleLogFactory.addErrorLog("write file={} error:{}",
                    apiFile.getAbsolutePath(), e.getMessage());
            return;
        }
    }

    /***
     *
     * @author : daviswujiahao
     * @date : 2020/3/27 2:20 下午
     * @param apiDetail :
     * @return : java.lang.String
     **/
    private String buildApiDetail(@NonNull ApiDetail apiDetail, String gitBranchName, String gitCommitVersion) {

        String apiName = StringUtils.defaultIfEmpty(apiDetail.getDescription(), "");
        String apiBaseUrl = StringUtils.defaultIfEmpty(StringUtils.join(apiDetail.getBaseUrl(), "|"), "");
        String protocolName = StringUtils.defaultIfEmpty(apiDetail.getProtocolName(), "");
        String apiMethod = StringUtils.defaultIfEmpty(StringUtils.join(apiDetail.getMethodType(), "|"), "");
        String contentType = StringUtils.defaultIfEmpty(apiDetail.getContentType(), "");

        StringBuilder content = new StringBuilder();

        // 基本信息
        content.append(String.format(MD_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "git分支", gitBranchName));
        content.append(String.format(MD_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "git版本", gitCommitVersion));
        content.append(String.format(MD_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "接口描述", apiName));
        content.append(String.format(MD_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "访问地址", apiBaseUrl));
        content.append(String.format(MD_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "协议类型", protocolName));
        content.append(String.format(MD_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "访问方式", apiMethod));
        content.append(String.format(MD_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, "Content-type", contentType));

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

        content.append(buildContentForFieldType(firstParam));

        apiDetail.getResult().setTypeName("返回值列表");
        apiDetail.getResult().setTypeName4TableTitle("返回值列表");
        content.append(buildContentForFieldType(apiDetail.getResult()));

        return content.toString();
    }

    private String buildContentForFieldType(BaseInfo fieldInfo) {

        StringBuilder content = new StringBuilder();

        Set<String> hadBuildTypes = new HashSet<>();
        Queue<BaseInfo> listQueue = new ArrayDeque<>();
        listQueue.add(fieldInfo);
        while (!listQueue.isEmpty()) {

            BaseInfo nowTypeInfos = listQueue.poll();
            if (CollectionUtils.isEmpty(nowTypeInfos.getSubTypeInfos())) {
                continue;
            }

            StringBuilder rowData = new StringBuilder();
            for (BaseInfo nowTypeInfo : nowTypeInfos.getSubTypeInfos()) {

                String fieldName = StringUtils.defaultIfEmpty(nowTypeInfo.getName(), "");
                String fieldType = StringUtils.defaultIfEmpty(nowTypeInfo.getTypeName(), "");
                String fieldDescription = StringUtils.defaultIfEmpty(nowTypeInfo.getDescription(), "");
                boolean fieldRequired = nowTypeInfo.isRequired();

                rowData.append(String.format(">>|%s|%s|%s|%s|\n",
                        fieldName, StringEscapeUtils.escapeHtml4(fieldType), fieldRequired, fieldDescription));

                if (!hadBuildTypes.contains(nowTypeInfo.getTypeName())) {
                    listQueue.add(nowTypeInfo);
                }
            }

            content.append(String.format(MD_API_DOC_INDEX_PAGE_CONTENT_TITLE_TEMPLATE, nowTypeInfos.getTypeName4TableTitle(), ""));
            content.append(">>|属性名称|属性类型|是否必填|属性描述|\n");
            content.append(">>|:----:|:----:|:----:|:----:|\n");
            content.append(rowData);


            hadBuildTypes.add(nowTypeInfos.getTypeName());
        }

        return content.toString();
    }
}
