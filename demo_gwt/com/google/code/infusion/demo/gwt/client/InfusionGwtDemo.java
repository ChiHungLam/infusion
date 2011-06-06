package com.google.code.infusion.demo.gwt.client;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.service.FusionTableService;
import com.google.code.infusion.service.Table;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
  ScrollPanel scrollPanel = new ScrollPanel(outputPanel);
  DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
  FormPanel formPanel = new FormPanel();
  TextBox inputBox = new TextBox();
  FlowPanel buttonPanel = new FlowPanel();
  
  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    Document.get().getElementById("loading").removeFromParent();
    
    Button submitButton = new Button("Submit");
    Button clearButton = new Button("Clear");
    buttonPanel.add(submitButton);
    buttonPanel.add(clearButton);

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
  }

  private void executeCommand() {
    Label command = new Label(inputBox.getText());
    command.getElement().getStyle().setFontWeight(FontWeight.BOLD);
    outputPanel.add(command);
    service.sendQuery(inputBox.getText(), new AsyncCallback<Table>() {
      
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
        outputPanel.add(new Label("\u00A0"));
      }

      @Override
      public void onFailure(Throwable error) {
        outputPanel.add(new Label(error.toString()));
      }
      
    });

  }


  
}
