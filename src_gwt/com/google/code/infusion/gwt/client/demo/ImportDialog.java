package com.google.code.infusion.gwt.client.demo;

import com.google.code.infusion.gwt.client.file.File;
import com.google.code.infusion.importer.Importer;
import com.google.code.infusion.importer.ImporterBuilder;
import com.google.code.infusion.importer.ImporterCallback;
import com.google.code.infusion.service.FusionTableService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class ImportDialog {
  DialogBox dialogBox = new DialogBox();
  Grid grid = new Grid(5, 2);
  TextBox tableIdBox = new TextBox();
  TextBox delimiterBox = new TextBox();
  TextBox offsetBox = new TextBox();
  TextBox importedBox = new TextBox();
  Button cancelButton = new Button("Cancel");
  Button importButton = new Button("Import");
  FusionTableService service;
  File file;
  String data;
  AsyncCallback<String> callback;
  
  
  public ImportDialog(FusionTableService service, AsyncCallback<String> callback) {
    grid.setWidget(0, 0, new Label("Table Id"));
    grid.setWidget(0, 1, tableIdBox);
    grid.setWidget(1, 0, new Label("Delimited"));
    grid.setWidget(1, 1, delimiterBox);
    grid.setWidget(2, 0, new Label("offsetBox"));
    grid.setWidget(2, 1, offsetBox);
    grid.setWidget(3, 0, new Label("Imported"));
    grid.setWidget(3, 1, importedBox);
    importedBox.setEnabled(false);

    reset();
    
   
    FlowPanel buttonPanel = new FlowPanel();
    buttonPanel.add(importButton);
    buttonPanel.add(cancelButton);
    
    grid.setWidget(4, 1, buttonPanel);
    dialogBox.setWidget(grid);
    
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }});
    
    importButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        startImport();
        dialogBox.hide();
      }});
  }
  
  public void reset() {
    tableIdBox.setText("");
    delimiterBox.setText(",");
    offsetBox.setText("0");
  }
  
  public void show(File file, String data) {
    dialogBox.setTitle(file.getName());
    this.file = file;
    this.data = data;
    setEnabled(true);
    dialogBox.show();
  }
  
  private void setEnabled(boolean b) {
    tableIdBox.setEnabled(b);
    offsetBox.setEnabled(b);
    importButton.setEnabled(b);
    
  }

  private void startImport() {
    setEnabled(false);
    ImporterBuilder builder = new ImporterBuilder(service);
    String tableId = tableIdBox.getText();
    builder.setTableId(tableId != null && tableId.trim().length() > 0 ? tableId : null);
    String delimiterText = delimiterBox.getText();
    if (delimiterText == null || delimiterText.length() != 1) {
      Window.alert("invalid delimter; using ','");
    } else {
      builder.setDelimiter(delimiterText.charAt(0));
    }
    builder.setOffset(Integer.parseInt(offsetBox.getValue()));
    builder.setFileName(file.getName());
    builder.setData(data);
    builder.importData(new ImporterCallback() {
      public void onProgress(Importer importer) {
        importedBox.setText("" + importer.getCount());
        offsetBox.setText("" + (importer.getCount() + importer.getOffset()));
        tableIdBox.setText(importer.getTableId());
      }

      @Override
      public void onSuccess(Importer importer) {
        onProgress(importer);
        callback.onSuccess("" + importer.getCount() + 
            " successfully imported from " + file.getName() + 
            " to table " + importer.getTableId() + 
            " offset " + importer.getOffset());
        dialogBox.hide();
      }

      @Override
      public void onFailure(Importer importer, Throwable error) {
        onProgress(importer);
        importedBox.setText("Failded after " + importer.getCount() + " rows.");
        importButton.setEnabled(true);
      }});
  } 
  
  
}
