package top.hungrywu.configuration;

import lombok.Data;

import javax.swing.*;

@Data
public class ApiDocSettingForm {

  public JPanel mainPanel;

//  private JTextField wikiHost;
//  private JTextField wikiUsername;
//  private JPasswordField wikiPassword;
//  private JTextField pageSpaceName;
//  private JTextField contentPageId;
//  private JPanel wikiSettingsPanel;
  private JPanel requestSettingsPanel;
  private JTextField requestBizData;
  private JTextField defaultResponseContentType;
  private JTextField wrappedRequestClass;
  private JTextField requestDefaultContentType;
  private JRadioButton defaultWrapped;
  private JComboBox requestProtocolType;
  private JPanel responseSettingPanel;

  private void createUIComponents() {

  }
}
