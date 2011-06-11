package com.google.code.infusion.shell;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import com.google.code.infusion.service.FusionTableService;
import com.google.code.infusion.importer.BibtexParser;
import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.service.Table;
import com.google.code.infusion.util.OAuthLogin;
import com.google.code.infusion.util.OAuthToken;
import com.google.code.infusion.util.Util;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Simple desktop Java usage example for FusionTableService.
 * 
 * @author Stefan Haustein
 */
public class Shell {
  static final String TOKEN_FILE = ".token";
  
  BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  FusionTableService service = new FusionTableService();
  
  public static void main(String[] args) throws IOException {
    new Shell().run();
  }

  private void init() {
    try {
      String tokenString = readFile(TOKEN_FILE);
      OAuthToken token = new OAuthToken();
      token.parse(tokenString);
      service.setRequestToken(token);
      System.out.println();
      if (token.getToken() == null || token.getTokenSecret() == null) {
        throw new NullPointerException("Token or token secret is empty");
      }
      System.out.println("Using existing authentication token");
      showPrompt();
    } catch (Exception e) {
      showError("Readin authentication token failed", e);
    }
  }
  
  
  private void run() throws IOException {
    showHelp();
    init();
    
    while (true) {
      try {
        String cmd = reader.readLine();
        String lcmd = cmd.toLowerCase();
        if ("exit".equals(lcmd) || "quit".equals(lcmd)) {
          break;
        } else if ("?".equals(lcmd) || "help".equals(lcmd)) {
          showHelp();
          showPrompt();
        } else if (lcmd.equals("auth")) {
          auth();
        } else if (cmd.startsWith("import ")) {
          importFile(cmd.substring(7));
        } else {
          service.query(cmd, new SimpleCallback<Table>() {
            @Override
            public void onSuccess(Table result) {
              System.out.println(result.getCols().serialize());
              for (JsonArray row: result.getRowsAsIterable()) {
                System.out.println(row.serialize());
              }
              showPrompt();
            }});
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    System.out.println("Good bye!");
  }

  private void auth() {
    OAuthLogin.getRequestToken(FusionTableService.SCOPE, null, new SimpleCallback<OAuthToken>() {
      @Override
      public void onSuccess(OAuthToken requestToken) {
        URI uri;
        try {
          uri = new URI(OAuthLogin.getAuthorizationUrl(requestToken));
          Desktop.getDesktop().browse(uri);
          System.out.println("");
          System.out.print("Verification code: ");
          String verificationCode = reader.readLine();
        
          OAuthLogin.getAccessToken(requestToken, verificationCode, new SimpleCallback<OAuthToken>() {
            @Override
            public void onSuccess(OAuthToken result) {
              service.setRequestToken(result);
              
              try {
                Writer writer = new FileWriter(TOKEN_FILE);
                writer.write(result.getToken().toString());
                writer.close();
                showPrompt();
              } catch(Exception e) {
                showError("Saving Request Token failed:", e);
              }
            }
          });
          
        } catch (Exception e) {
          showError(e);
        }
      }
    });
  }

  private static String readFile(String fileName) throws IOException {
    Reader reader = new InputStreamReader(new FileInputStream(fileName), "utf-8");
    char[] buf = new char[32768];
    StringBuilder sb = new StringBuilder();
    while (true) {
      int count = reader.read(buf);
      if (count <= 0) {
        break;
      }
      sb.append(buf, 0, count);
    }
    return sb.toString();
  }
  
  
  private void importFile(String fileName) throws IOException {
    String data = readFile(fileName);
    final Table table;
    if (fileName.endsWith(".bib")) {
      table = BibtexParser.parse(data);
    } else {
      throw new RuntimeException("");
      //parser = new CsvParser(data, true);
    }
    
    String name = fileName;
    int cut = name.lastIndexOf('/');
    if (cut != -1) {
      name = name.substring(cut + 1);
    }
    cut = name.lastIndexOf('.');
    if (cut != -1) {
      name = name.substring(0, cut);
    }
    
    StringBuilder sb = new StringBuilder("CREATE TABLE ");
    sb.append(Util.singleQuote(name));
    sb.append(" (");
    for (int i = 0; i < table.getCols().length(); i++) {
      if (i != 0) {
        sb.append(',');
      }
      sb.append(Util.singleQuote(table.getCols().getString(i)));
      sb.append(":STRING");
    }
    sb.append(')');
    service.query(sb.toString(), new SimpleCallback<Table>() {
      public void onSuccess(Table result) {
        String tableId = result.getRows().getArray(0).getString(0);
        service.insert(tableId, table, new SimpleCallback<Table>() {
          @Override
          public void onSuccess(Table result) {
            System.out.println("" + result.getRows().length() + " entities updated / inserted.");
            showPrompt();
          }
        });
      }
    });
  }

  private void showHelp() {
    System.out.println();
    System.out.println("Shell Commands");
    System.out.println("  auth:               Authenticate client");
    System.out.println("  exit:               Quit FT demo");
    System.out.println("  help:               Show this help screen");
    System.out.println("  import <filename>   Import a bibtex or CSV file");
    System.out.println();
    System.out.println("Fusion Table query examples");
    System.out.println("  select * from 197026   Display contents of table 197026");
    System.out.println("  show tables            List available tables*");
    System.out.println("");
    System.out.println("*) Requies authentication");
  }


  private void showPrompt() {
    System.out.print("\nCommand? ");
    System.out.flush();
  }

  private void showError(Throwable caught) {
    showError(null, caught);
  }

  private void showError(String message, Throwable caught) {
    System.out.println();
    if (message != null) {
      System.out.println(message);
    }
    if (caught != null) {
      caught.printStackTrace(System.out);
    }
    showPrompt();
  }
  
  abstract class SimpleCallback<T> implements AsyncCallback<T> {
    @Override
    public void onFailure(Throwable error) {
      showError(error.getMessage(), null);
    }
  }
}
