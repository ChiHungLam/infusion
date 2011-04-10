package com.google.code.infusion.demo.gwt.client;

import java.util.List;

import com.google.code.infusion.datastore.FusionTableService;
import com.google.code.infusion.datastore.TableInfo;
import com.google.code.infusion.util.AsyncCallback;
import com.google.code.infusion.util.ClientLogin;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class InfusionGwtDemo implements EntryPoint {

  private FusionTableService service;

  DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
  SimplePanel loginPanel = new SimplePanel();
  VerticalPanel tableListPanel = new VerticalPanel();


  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    Document.get().getElementById("loading").removeFromParent();

    Grid grid = new Grid(3, 2);

    grid.setWidget(0, 0, new Label("Email Address:"));
    grid.setWidget(1, 0, new Label("Password:"));

    final TextBox email = new TextBox();
    final PasswordTextBox password = new PasswordTextBox();

    grid.setWidget(0, 1, email);
    grid.setWidget(1, 1, password);

    final Button loginButton = new Button("Login");
    loginButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        ClientLogin.requestAuthToken(ClientLogin.ACCOUNT_TYPE_GOOGLE,
            email.getValue(), password.getValue(), "fusiontables",
            "GoogleCodeProjectInfusion-InfusionDemo-0.1",
            new AsyncCallback<String>() {
              public void onSuccess(String result) {
                authenticationObtained(result);
              }

              public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
              }
            });
      }
    });
    grid.setWidget(2, 1, loginButton);

    loginPanel.add(grid);

    RootLayoutPanel.get().add(mainPanel);
    mainPanel.addWest(tableListPanel, 150);
    mainPanel.add(loginPanel);
  }


  static native String loadFile(NativeEvent event) /*-{
    var input = event.target;
    var file = input.files[0];
    return file.name;
  }-*/;


  void authenticationObtained(String authtoken) {
    this.service = new FusionTableService(authtoken);
    service.showTables(new AsyncCallback<List<TableInfo>>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.getLocalizedMessage());
      }

      public void onSuccess(List<TableInfo> result) {
        for (TableInfo table : result) {
          tableListPanel.add(new Label(table.getName()));
        }

        FileUpload upload = new FileUpload();
        tableListPanel.add(upload);
        upload.addChangeHandler(new ChangeHandler() {

          public void onChange(ChangeEvent event) {
            String content = loadFile(event.getNativeEvent());
            Window.alert(content);
          }
        });
      }
    });
  }
}
