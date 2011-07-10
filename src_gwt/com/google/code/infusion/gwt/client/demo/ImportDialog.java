package com.google.code.infusion.gwt.client.demo;

import com.google.code.infusion.gwt.client.demo.InfusionGwtDemo.SimpleCallback;
import com.google.code.infusion.gwt.client.file.File;
import com.google.code.infusion.gwt.client.file.FileReaderBuilder;
import com.google.code.infusion.gwt.client.file.FileUtil;
import com.google.code.infusion.importer.Importer;
import com.google.code.infusion.importer.ImporterBuilder;
import com.google.code.infusion.importer.ImporterCallback;
import com.google.code.infusion.service.FusionTableService;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class ImportDialog {
  private final FusionTableService service;
  private final AsyncCallback<String> callback;
  final FileUpload upload = new FileUpload();
  private DialogBox dialogBox = new DialogBox();
  private FlexTable grid = new FlexTable();
  private TextBox tableIdBox = new TextBox();
  private TextBox delimiterBox = new TextBox();
  private TextBox offsetBox = new TextBox();
  private TextArea statusBox = new TextArea();
  private Button cancelButton = new Button("Cancel");
  private Button importButton = new Button("Import");

  private File file;
  private String data;
  
  
  public ImportDialog(FusionTableService service, AsyncCallback<String> callback) {
    this.service = service;
    this.callback = callback;
    dialogBox.setText("File Import");
    grid.setWidget(0, 0, new Label("File"));
    grid.setWidget(0, 1, upload);
    grid.setWidget(1, 0, new Label("Table Id"));
    grid.setWidget(1, 1, tableIdBox);
    tableIdBox.setTitle("Leave table Id empty to automatically create a table.");
    grid.setWidget(2, 0, new Label("Delimited"));
    grid.setWidget(2, 1, delimiterBox);
    grid.setWidget(3, 0, new Label("offsetBox"));
    grid.setWidget(3, 1, offsetBox);
    grid.setWidget(4, 0, new Label("Status"));
    grid.setWidget(4, 1, statusBox);
    grid.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
    statusBox.setReadOnly(true);

    reset();
   
    FlowPanel buttonPanel = new FlowPanel();
    buttonPanel.add(importButton);
    buttonPanel.add(cancelButton);
    
    grid.setWidget(5, 1, buttonPanel);
    dialogBox.setWidget(grid);
    
    upload.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        file = FileUtil.getFile(upload.getElement(), 0);
        new FileReaderBuilder().readAsText(file, "utf-8", new AsyncCallback<String>() {
          @Override
          public void onSuccess(String result) {
            data = result;
            statusBox.setText("File ready for import.");
            importButton.setEnabled(true);
          }

          @Override
          public void onFailure(Throwable error) {
            statusBox.setText("File load error: " + error.getMessage());
          }
        });
      }
    });

    
    
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }});
    
    importButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        startImport();
      }});
  }
  
  public void reset() {
    tableIdBox.setText("");
    delimiterBox.setText(",");
    offsetBox.setText("0");
    importButton.setEnabled(data != null);
  }
  
  public void show() {
    setEnabled(true);
    dialogBox.center();
  }
  
  private void setEnabled(boolean b) {
    tableIdBox.setEnabled(b);
    offsetBox.setEnabled(b);
    upload.setEnabled(b);
    importButton.setEnabled(b && data != null);
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
        statusBox.setText("" + importer.getCount());
        offsetBox.setText("" + (importer.getCount() + importer.getOffset()));
        tableIdBox.setText(importer.getTableId());
      }

      @Override
      public void onSuccess(Importer importer) {
        onProgress(importer);
        callback.onSuccess("" + importer.getCount() + 
            " rows successfully imported from " + file.getName() + 
            " to table " + importer.getTableId() + 
            " offset " + importer.getOffset());
        dialogBox.hide();
      }

      @Override
      public void onFailure(Importer importer, Throwable error) {
        onProgress(importer);
        statusBox.setText("Failded after " + importer.getCount() + " rows:\n" + error.getMessage());
        importButton.setEnabled(true);
      }});
  } 
  
  
}
