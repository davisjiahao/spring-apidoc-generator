package top.hungrywu.configuration;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import top.hungrywu.config.KiwiConfig;
import top.hungrywu.config.RequestConfig;
import top.hungrywu.config.ResponseConfig;

import javax.swing.*;
import java.util.Objects;


public class ApiDocConfigurable implements SearchableConfigurable {

  private ApiDocSetting apiDocSetting;

  private ApiDocSettingForm apiDocSettingForm;

  private String separator = ";";

  private Splitter splitter = Splitter.on(separator).omitEmptyStrings().trimResults();

  private Joiner joiner = Joiner.on(separator);

  public ApiDocConfigurable() {
    apiDocSetting = ApiDocSetting.getInstance();
  }

  @Override
  public String getId() {
    return "ApiDoc";
  }

  @Override
  public Runnable enableSearch(String option) {
    return null;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return getId();
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return getId();
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    if (null == apiDocSettingForm) {
      this.apiDocSettingForm = new ApiDocSettingForm();
    }
    return apiDocSettingForm.getMainPanel();
  }

  @Override
  public boolean isModified() {
    if (apiDocSettingForm == null) {
      return false;
    }
    return !StringUtils.equals(KiwiConfig.WIKI_HOST, apiDocSettingForm.getWikiHost().getText()) ||
                    !StringUtils.equals(KiwiConfig.KIWI_USER_NAME, apiDocSettingForm.getWikiUsername().getText()) ||
                    !StringUtils.equals(KiwiConfig.KIWI_USER_PASSWORD, apiDocSettingForm.getWikiPassword().getText()) ||
                    !StringUtils.equals(KiwiConfig.KIWI_SPACE_KEY, apiDocSettingForm.getPageSpaceName().getText()) ||
                    !StringUtils.equals(KiwiConfig.KIWI_ANCESTOR_ID, apiDocSettingForm.getContentPageId().getText()) ||

                    !StringUtils.equals(RequestConfig.DEFAULT_REQUEST_CONTENT_TYPE, apiDocSettingForm.getRequestDefaultContentType().getText()) ||
                    !StringUtils.equals(RequestConfig.DEFAULT_PROTOCOL_TYPE, Objects.requireNonNull(apiDocSettingForm.getRequestProtocolType().getSelectedItem()).toString()) ||
                    RequestConfig.DEFAULT_WRAPPED != apiDocSettingForm.getDefaultWrapped().isEnabled() ||
                    !StringUtils.equals(RequestConfig.WRAPPED_REQUEST_CLASS_NAME, apiDocSettingForm.getWrappedRequestClass().getText()) ||
                    !StringUtils.equals(RequestConfig.WRAPPED_REQUEST_CONTENT_FILE_NAME, apiDocSettingForm.getRequestBizData().getText()) ||

                    !StringUtils.equals(ResponseConfig.DEFAULT_RESPONSE_CONTENT_TYPE, apiDocSettingForm.getDefaultResponseContentType().getText())
            ;
  }

  @Override
  public void apply() throws ConfigurationException {
    // wiki settings
    KiwiConfig.WIKI_HOST = apiDocSettingForm.getWikiHost().getText();
    KiwiConfig.KIWI_USER_NAME = apiDocSettingForm.getWikiUsername().getText();
    KiwiConfig.KIWI_USER_PASSWORD = new String(apiDocSettingForm.getWikiPassword().getPassword());
    KiwiConfig.KIWI_SPACE_KEY = apiDocSettingForm.getPageSpaceName().getText();
    KiwiConfig.KIWI_ANCESTOR_ID = apiDocSettingForm.getContentPageId().getText();

    // request settings
    RequestConfig.DEFAULT_REQUEST_CONTENT_TYPE =
            apiDocSettingForm.getRequestDefaultContentType().getText();
    RequestConfig.DEFAULT_PROTOCOL_TYPE = Objects.requireNonNull(apiDocSettingForm.getRequestProtocolType().getSelectedItem()).toString();
    RequestConfig.DEFAULT_WRAPPED = apiDocSettingForm.getDefaultWrapped().isEnabled();
    RequestConfig.WRAPPED_REQUEST_CLASS_NAME =
            apiDocSettingForm.getWrappedRequestClass().getText();
    RequestConfig.WRAPPED_REQUEST_CONTENT_FILE_NAME =
            apiDocSettingForm.getRequestBizData().getText();

    // response settings
    ResponseConfig.DEFAULT_RESPONSE_CONTENT_TYPE = apiDocSettingForm.getDefaultResponseContentType().getText();

  }

  @Override
  public void reset() {
    // wiki settings
    apiDocSettingForm.getWikiHost().setText(KiwiConfig.WIKI_HOST);
    apiDocSettingForm.getWikiUsername().setText(KiwiConfig.KIWI_USER_NAME);
    apiDocSettingForm.getWikiPassword().setText(KiwiConfig.KIWI_USER_PASSWORD);
    apiDocSettingForm.getPageSpaceName().setText(KiwiConfig.KIWI_SPACE_KEY);
    apiDocSettingForm.getContentPageId().setText(KiwiConfig.KIWI_ANCESTOR_ID);

    // request settings
    apiDocSettingForm.getRequestDefaultContentType().setText(RequestConfig.DEFAULT_REQUEST_CONTENT_TYPE);
    apiDocSettingForm.getRequestProtocolType().setSelectedItem(RequestConfig.DEFAULT_PROTOCOL_TYPE);
    apiDocSettingForm.getDefaultWrapped().setEnabled(RequestConfig.DEFAULT_WRAPPED);
    apiDocSettingForm.getWrappedRequestClass().setText(RequestConfig.WRAPPED_REQUEST_CLASS_NAME);
    apiDocSettingForm.getRequestBizData().setText(RequestConfig.WRAPPED_REQUEST_CONTENT_FILE_NAME);

    // response settings
    apiDocSettingForm.getDefaultResponseContentType().setText(ResponseConfig.DEFAULT_RESPONSE_CONTENT_TYPE);
  }

  @Override
  public void disposeUIResources() {
    apiDocSettingForm.setMainPanel(null);
  }

}
