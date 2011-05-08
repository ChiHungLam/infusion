package com.google.code.infusion.demo.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.code.infusion.datastore.ColumnInfo;
import com.google.code.infusion.datastore.Entity;
import com.google.code.infusion.datastore.FusionTableService;
import com.google.code.infusion.datastore.Query;
import com.google.code.infusion.datastore.TableDescription;
import com.google.code.infusion.datastore.TableInfo;
import com.google.code.infusion.gwt.client.FusionTableDataProvider;
import com.google.code.infusion.util.AsyncCallback;
import com.google.code.infusion.util.ClientLogin;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
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

  CellTable<Entity> cellTable;
  DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
  Grid loginPanel = new Grid(3, 2);
  VerticalPanel tableListPanel = new VerticalPanel();
  SimplePanel mainPanel = new SimplePanel();
  

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    Document.get().getElementById("loading").removeFromParent();

    loginPanel.setWidget(0, 0, new Label("Email Address:"));
    loginPanel.setWidget(1, 0, new Label("Password:"));

    final TextBox email = new TextBox();
    final PasswordTextBox password = new PasswordTextBox();

    loginPanel.setWidget(0, 1, email);
    loginPanel.setWidget(1, 1, password);

    final Button loginButton = new Button("Login");
    loginButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        ClientLogin.requestAuthToken(ClientLogin.ACCOUNT_TYPE_GOOGLE,
            email.getValue(), password.getValue(), "fusiontables",
            "GoogleCodeProjectInfusion-InfusionGwtDemo-0.1",
            new AsyncCallback<String>() {
              public void onSuccess(String result) {
                authenticated(result);
              }

              public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
              }
            });
      }
    });
    loginPanel.setWidget(2, 1, loginButton);    
    loginPanel.addStyleName("loginPanel");

    tableListPanel.addStyleName("tableListPanel");
    
    mainPanel.addStyleName("mainPanel");
    mainPanel.add(loginPanel);

    RootLayoutPanel.get().add(dockPanel);
    dockPanel.addWest(tableListPanel, 150);
    dockPanel.add(mainPanel);
  }


  static native String loadFile(NativeEvent event) /*-{
    var input = event.target;
    var file = input.files[0];
    return file.name;
  }-*/;


  void authenticated(String authtoken) {
    mainPanel.remove(loginPanel);
    this.service = new FusionTableService(authtoken);
    service.showTables(new AsyncCallback<List<TableInfo>>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.getLocalizedMessage());
      }

      public void onSuccess(List<TableInfo> result) {
        for (final TableInfo table : result) {
          Anchor anchor = new Anchor(table.getName());
          tableListPanel.add(anchor);
          anchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              service.describe(table.getId(), new AsyncCallback<TableDescription>() {

                @Override
                public void onSuccess(TableDescription result) {
                  selectColumns(table, result);
                }

                @Override
                public void onFailure(Throwable error) {
                  Window.alert("" + error);
                }
              });
            }

           
          });
        }

       /* FileUpload upload = new FileUpload();
        tableListPanel.add(upload);
        upload.addChangeHandler(new ChangeHandler() {

          public void onChange(ChangeEvent event) {
            String content = loadFile(event.getNativeEvent());
            Window.alert(content);
          }
        });
        */
      }
    });
  }
  
  
  private void selectColumns(final TableInfo tableInfo, 
      final TableDescription tableDescription) {

    ColumnStats.getStats(service, new Query(tableInfo.getId()), 100, new AsyncCallback<List<ColumnStats>>() {
      @Override
      public void onSuccess(List<ColumnStats> stats) {
        ArrayList<ColumnInfo> columns = new ArrayList<ColumnInfo>();
        for (int i = 0; i < Math.min(stats.size(), 10); i++) {
          columns.add(tableDescription.get(stats.get(i).getName()));
        }
        showTable(tableInfo, columns);
      }

      @Override
      public void onFailure(Throwable error) {
        error.printStackTrace();
      }
    });
  }
  
  
  private void showTable(TableInfo table, List<ColumnInfo> columns) {
    if (cellTable != null) {
      mainPanel.remove(cellTable);
    }
    cellTable = new CellTable<Entity>();
    mainPanel.add(cellTable);
    
    SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
    SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
    pager.setDisplay(cellTable);
    
    for (final ColumnInfo column: columns) {
      cellTable.addColumn(new TextColumn<Entity>() {
        @Override
        public String getValue(Entity entity) {
          return "" + entity.getProperty(column.getName());
        }
      }, column.getName());
      
    }
    
    new FusionTableDataProvider(service, new Query(table.getId())).addDataDisplay(cellTable);
    
  }
  
}
