package com.google.code.infusion.gwt.client.demo;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.service.FusionTableService;
import com.google.code.infusion.service.Table;
import com.google.code.infusion.util.OAuthLogin;
import com.google.code.infusion.util.OAuthToken;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class InfusionGwtDemo implements EntryPoint {

  private FusionTableService service = new FusionTableService();

  FlowPanel outputPanel = new FlowPanel();
  TextBox inputBox = new TextBox();
  ScrollPanel scrollPanel = new ScrollPanel(outputPanel);
  OAuthToken token;
  
  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    Document.get().getElementById("loading").removeFromParent();

    String tokenCookie = Cookies.getCookie("accessToken");
    if (tokenCookie != null) {
      token = new OAuthToken();
      token.parse(tokenCookie);
      service.setAccessToken(token);
      println("Token loaded from cookies.");
    }
    
    String verificationCode = Window.Location.getParameter("oauth_verifier");
    if (verificationCode != null) {
      tokenCookie = Cookies.getCookie("requestToken");
      if (tokenCookie != null) {
        token = new OAuthToken();
        token.parse(tokenCookie);
      }      
      if (token == null) {
        println("Missing request token for verifcation code.");
      } else if (!token.getToken().equals(Window.Location.getParameter("oauth_token"))) {
        println("Token mismatch; expected: " + token.getToken());
      } else {
        println("Upgrading request token to access token.");
        OAuthLogin.getAccessToken(token, verificationCode, new SimpleCallback<OAuthToken>() {
          @Override
          public void onSuccess(OAuthToken result) {
            println("Access token obtained successfully.");
            Cookies.setCookie("accessToken", result.toString());
            Cookies.setCookie("requestToken", "");
            service.setAccessToken(token);
            println();
          }});
      }
    }

    println("Enter sql queries at the bottom of this page.");
    println();
    DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
    FlowPanel buttonPanel = new FlowPanel();
    Button submitButton = new Button("Submit");
    Button clearButton = new Button("Clear");
    Button authButton = new Button("Authenticate");
    buttonPanel.add(submitButton);
    buttonPanel.add(clearButton);
    buttonPanel.add(authButton);
    Anchor anchor = new Anchor("Developer Guide", "http://code.google.com/apis/fusiontables/docs/developers_guide.html");
    anchor.setTarget("_blank");
    buttonPanel.add(anchor);

    FormPanel formPanel = new FormPanel();
    mainPanel.addSouth(buttonPanel, 2);
    mainPanel.addSouth(formPanel, 2);
    
    mainPanel.add(scrollPanel);
    
    RootLayoutPanel.get().add(mainPanel);
    
    formPanel.add(inputBox);
    inputBox.getElement().getStyle().setWidth(100, Unit.PCT);
    inputBox.setText("select * from 197026");
    formPanel.addSubmitHandler(new SubmitHandler() {
      @Override
      public void onSubmit(SubmitEvent event) {
        executeCommand();
      }
    });

    submitButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
         executeCommand();
      }});
    
    clearButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        outputPanel.removeFromParent();
        outputPanel = new FlowPanel();
        scrollPanel.add(outputPanel);
      }
    });
    
    authButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        OAuthLogin.getRequestToken(FusionTableService.SCOPE,  Document.get().getURL(), new SimpleCallback<OAuthToken>() {
          @Override
            public void onSuccess(final OAuthToken requestToken) {
            Cookies.setCookie("requestToken", requestToken.toString());
            Window.Location.assign(OAuthLogin.getAuthorizationUrl(requestToken));
          }
        });
      }
    });
    
  }

  
  private void println() {
    println("\u00a0");
  }
  
  private void println(String s) {
    Label label = new Label(s);
    label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
    outputPanel.add(label);
    scrollPanel.scrollToBottom();
  }
  
  
  private void executeCommand() {
    String command = inputBox.getValue();
    println(command);
    service.query(command, new SimpleCallback<Table>() {
      
      @Override
      public void onSuccess(Table result) {
        
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<table>");
        sb.appendHtmlConstant("<tr>");
        for (int i = 0; i < result.getCols().length(); i++) {
          sb.appendHtmlConstant("<td>");
          sb.appendHtmlConstant("<b>");
          sb.appendEscaped(result.getCols().getString(i));
          sb.appendHtmlConstant("</b>");
          sb.appendHtmlConstant("</td>");
        }
        sb.appendHtmlConstant("</tr>");
        for (int i = 0; i < result.getRows().length(); i++) {
          sb.appendHtmlConstant("<tr>");
          JsonArray row = result.getRows().getArray(i);
          for (int j = 0; j < row.length(); j++) {
            sb.appendHtmlConstant("<td>");
            sb.appendEscaped(row.getString(j));
            sb.appendHtmlConstant("</td>");
          }
          sb.appendHtmlConstant("</tr>");
        }
        sb.appendHtmlConstant("</table>");
        
        Label label = new Label();
        label.getElement().setInnerHTML(sb.toSafeHtml().asString());
        
        outputPanel.add(label);
        println();
      }
    });

  }

  abstract class SimpleCallback<T> implements AsyncCallback<T>{
    public void onFailure(Throwable error) {
      println(error.toString());
    }
  }

  
}
