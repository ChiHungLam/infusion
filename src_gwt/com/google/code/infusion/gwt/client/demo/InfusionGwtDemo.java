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
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Grid;
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

    String tokenCookie = Cookies.getCookie("token");
    if (tokenCookie != null) {
      token = new OAuthToken();
      token.parse(tokenCookie);
      service.setRequestToken(token);
      println("Token loaded from cookies.");
    }
    
    String verificationCode = Window.Location.getParameter("oauth_verifier");
    if (verificationCode != null) {
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
            Cookies.setCookie("token", result.toString());
            service.setRequestToken(token);
          }});
      }
    }

    println("Enter sql queries at the bottom of this page.");
    DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
    FlowPanel buttonPanel = new FlowPanel();
    Button submitButton = new Button("Submit");
    Button clearButton = new Button("Clear");
    Button authButton = new Button("Authenticate");
    buttonPanel.add(submitButton);
    buttonPanel.add(clearButton);
    buttonPanel.add(authButton);

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
            Cookies.setCookie("token", requestToken.toString());
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
    println(inputBox.getText());
    service.query(inputBox.getText(), new SimpleCallback<Table>() {
      
      @Override
      public void onSuccess(Table result) {
        inputBox.setText("");
        Grid grid = new Grid(result.getRows().length() + 1, result.getCols().length());
        for (int i = 0; i < result.getCols().length(); i++) {
          grid.setText(0, i, result.getCols().getString(i));
        }
        for (int i = 0; i < result.getRows().length(); i++) {
          JsonArray row = result.getRows().getArray(i);
          for (int j = 0; j < row.length(); j++) {
            grid.setText(i+ 1, j, row.getString(j));
          }
        }
        
        outputPanel.add(grid);
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
