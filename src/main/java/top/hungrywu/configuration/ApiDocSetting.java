package top.hungrywu.configuration;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import top.hungrywu.config.KiwiConfig;
import top.hungrywu.config.RequestConfig;
import top.hungrywu.config.ResponseConfig;


/**
 * @author yanglin
 */
@State(
    name = "ApiDocSettings",
    storages = @Storage("apiDoc.xml")
)
public class ApiDocSetting implements PersistentStateComponent<Element> {

  public static ApiDocSetting getInstance() {

    return ServiceManager.getService(ApiDocSetting.class);
  }

  @Nullable
  @Override
  public Element getState() {
    Element element = new Element("ApiDocSettings");

    // wiki settings
    element.setAttribute("WIKI_HOST", KiwiConfig.WIKI_HOST);
    element.setAttribute("KIWI_USER_NAME", KiwiConfig.KIWI_USER_NAME);
    element.setAttribute("KIWI_USER_PASSWORD", KiwiConfig.KIWI_USER_PASSWORD);
    element.setAttribute("KIWI_SPACE_KEY", KiwiConfig.KIWI_SPACE_KEY);
    element.setAttribute("KIWI_ANCESTOR_ID", KiwiConfig.KIWI_ANCESTOR_ID);

    // request settings
    element.setAttribute("DEFAULT_REQUEST_CONTENT_TYPE", RequestConfig.DEFAULT_REQUEST_CONTENT_TYPE);
    element.setAttribute("DEFAULT_PROTOCOL_TYPE", RequestConfig.DEFAULT_PROTOCOL_TYPE);
    element.setAttribute("DEFAULT_WRAPPED", String.valueOf(RequestConfig.DEFAULT_WRAPPED));
    element.setAttribute("WRAPPED_REQUEST_CLASS_NAME", RequestConfig.WRAPPED_REQUEST_CLASS_NAME);
    element.setAttribute("WRAPPED_REQUEST_CONTENT_FILE_NAME", RequestConfig.WRAPPED_REQUEST_CONTENT_FILE_NAME);

    // response settings
    element.setAttribute("DEFAULT_RESPONSE_CONTENT_TYPE", ResponseConfig.DEFAULT_RESPONSE_CONTENT_TYPE);

    return element;
  }

  @Override
  public void loadState(Element state) {

    KiwiConfig.WIKI_HOST = state.getAttributeValue("WIKI_HOST");
    KiwiConfig.KIWI_USER_NAME = state.getAttributeValue("KIWI_USER_NAME");
    KiwiConfig.KIWI_USER_PASSWORD = state.getAttributeValue("KIWI_USER_PASSWORD");
    KiwiConfig.KIWI_SPACE_KEY = state.getAttributeValue("KIWI_SPACE_KEY");
    KiwiConfig.KIWI_ANCESTOR_ID = state.getAttributeValue("KIWI_ANCESTOR_ID");

    RequestConfig.DEFAULT_REQUEST_CONTENT_TYPE = state.getAttributeValue("DEFAULT_REQUEST_CONTENT_TYPE");
    RequestConfig.DEFAULT_PROTOCOL_TYPE = state.getAttributeValue("DEFAULT_PROTOCOL_TYPE");
    RequestConfig.DEFAULT_WRAPPED = Boolean.parseBoolean(state.getAttributeValue("DEFAULT_WRAPPED"));
    RequestConfig.WRAPPED_REQUEST_CLASS_NAME = state.getAttributeValue("WRAPPED_REQUEST_CLASS_NAME");
    RequestConfig.WRAPPED_REQUEST_CONTENT_FILE_NAME = state.getAttributeValue("WRAPPED_REQUEST_CONTENT_FILE_NAME");

    ResponseConfig.DEFAULT_RESPONSE_CONTENT_TYPE = state.getAttributeValue("DEFAULT_RESPONSE_CONTENT_TYPE");

  }


}
