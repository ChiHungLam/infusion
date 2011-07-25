package com.google.code.infusion.gwt.client.demo;

import com.google.code.infusion.gwt.client.file.File;
import com.google.code.infusion.gwt.client.file.FileReaderBuilder;
import com.google.code.infusion.gwt.client.file.FileUtil;
import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.service.FusionTableService;
import com.google.code.infusion.service.Table;
import com.google.code.infusion.util.OAuthLogin;
import com.google.code.infusion.util.OAuthToken;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.Widget;


/**
 * Fusion table GWT client demo. Displays a command line and
 * sends commands to the FT API.
 */
public class InfusionGwtDemo implements EntryPoint {

  private static final String REQUEST_TOKEN_COOKIE = "requestToken";
  private static final String ACCESS_TOKEN_COOKIE = "accessToken";

  private FusionTableService service = new FusionTableService();
  
  private FlowPanel outputPanel = new FlowPanel();
  private TextBox inputBox = new TextBox();
  private ScrollPanel scrollPanel = new ScrollPanel(outputPanel);
  private ImportDialog importDialog;
  
  
  Label createButton(String label) {
    Label button = new Label(label);
    button.addStyleName("button");
    return button;
  }
  
  
  
  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    Element loading = Document.get().getElementById("loading");
    if (loading != null) {
      loading.removeFromParent();
    }

    { // Keep accessToken local here
      OAuthToken accessToken = new OAuthToken();
      if (accessToken.parse(Cookies.getCookie(ACCESS_TOKEN_COOKIE))) {
        println("Access token loaded from cookies.");
        service.setAccessToken(accessToken);
      } else {
        println("Access token cookie not found or empty.");
        accessToken = null;
      }
    }
    
    String verificationCode = Window.Location.getParameter("oauth_verifier");
    if (verificationCode != null) {
      OAuthToken requestToken = new OAuthToken();
      if (!requestToken.parse(Cookies.getCookie(REQUEST_TOKEN_COOKIE))) {
        println("Missing request token for verifcation code.");
      } else if (!requestToken.getToken().equals(Window.Location.getParameter("oauth_token"))) {
        println("Token mismatch; expected: " + requestToken.getToken());
      } else {
        println("Upgrading request token to access token.");
        OAuthLogin.getAccessToken(requestToken, verificationCode, new AsyncCallback<OAuthToken>() {
          @Override
          public void onSuccess(OAuthToken accessToken) {
            println("Access token obtained successfully, storing in cookie.");
            Cookies.setCookie(ACCESS_TOKEN_COOKIE, accessToken.toString());
            Cookies.setCookie(REQUEST_TOKEN_COOKIE, "");
            service.setAccessToken(accessToken);
            println();
          }
          public void onFailure(Throwable e) {
            println("Obtaining access token failed: " + e.toString());
            println();
          }
        });
      }
    }

    println("Enter sql queries at the bottom of this page.");
    println();
    DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
    FlowPanel buttonPanel = new FlowPanel();
    buttonPanel.addStyleName("topbar");
    FormPanel formPanel = new FormPanel();
    formPanel.add(inputBox);
    inputBox.addStyleName("query");
    buttonPanel.add(formPanel);
    Label submitButton = createButton("Submit");
    submitButton.addStyleName("submit");
    Label clearButton = createButton("Clear");
    Label authButton = createButton("Authenticate");
    Label importButton = createButton("Import");
    buttonPanel.add(submitButton);
    buttonPanel.add(clearButton);
    buttonPanel.add(authButton);
    buttonPanel.add(importButton);
    
    SimplePanel sp = new SimplePanel();
    buttonPanel.add(sp);
    Element element = sp.getElement();
    sp.addStyleName("link-panel");
    element.setInnerHTML(
        "<div><a href='http://code.google.com/apis/fusiontables/docs/developers_guide.html'" +
        " target='blank_'>API Guide</a></div><div><a href='http://code.google.com/p/infusion' " +
        "target='blank_'>Infusion Homepage</a></div>");

    mainPanel.addNorth(buttonPanel, 101);
    scrollPanel.addStyleName("console");
    mainPanel.add(scrollPanel);
    
    RootLayoutPanel.get().add(mainPanel);
    
  //  inputBox.getElement().getStyle().setWidth(100, Unit.PCT);
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
            Cookies.setCookie(REQUEST_TOKEN_COOKIE, requestToken.toString());
            Window.Location.assign(OAuthLogin.getAuthorizationUrl(requestToken));
          }
        });
      }
    });
    
    importButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (importDialog == null) {
          importDialog = new ImportDialog(service, 
              new SimpleCallback<String>() {
                @Override
                public void onSuccess(String result) {
                  println(result);
                }} );
        }
        importDialog.show();
      }});
    
  }

  
  private void println() {
    println("\u00a0");
  }
  
  public void println(String s) {
    Label label = new Label(s);
    label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
    outputPanel.add(label);
    scrollPanel.scrollToBottom();
  }
  
  private void executeCommand() {
    String command = inputBox.getValue();
    String lcmd = command.trim().toLowerCase();
    if (lcmd.startsWith("select") && lcmd.indexOf("limit") == -1) {
      command += " limit 100";
    }
    
    println(command);
    service.query(command, new SimpleCallback<Table>() {
      @Override
      public void onSuccess(Table result) {
        
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<table>");
        sb.appendHtmlConstant("<tr>");
        for (String col: result.getCols()) {
          sb.appendHtmlConstant("<td>");
          sb.appendHtmlConstant("<b>");
          sb.appendEscaped(col);
          sb.appendHtmlConstant("</b>");
          sb.appendHtmlConstant("</td>");
        }
        sb.appendHtmlConstant("</tr>");
        for (JsonArray row:  result) {
          sb.appendHtmlConstant("<tr>");
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


  public void showError(String message, Throwable exception) {
    if (message != null) {
      println(message);
    }
    if (exception != null) {
      println(exception.toString());
    }
  }
}
