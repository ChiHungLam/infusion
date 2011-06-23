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
import com.google.code.infusion.importer.CsvParser;
import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.service.SimpleTable;
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
  boolean authenticating;
  OAuthToken requestToken;
  
  public static void main(String[] args) throws IOException {
    new Shell().run();
  }

  private void init() {
    try {
      String tokenString = readFile(TOKEN_FILE);
      OAuthToken token = new OAuthToken();
      if (!token.parse(tokenString)) {
        throw new RuntimeException("Empty token");
      }
      service.setAccessToken(token);
      System.out.println("Using existing access token");
      showPrompt();
    } catch (Exception e) {
      showError("Reading access token failed", e);
    }
  }
  
  
  private void run() throws IOException {
    showHelp();
    init();
    
    while (true) {
      try {
        String cmd = reader.readLine();
        if (requestToken != null) {
          getAccessToken(requestToken, cmd);
          requestToken = null;
          continue;
        } 
        String lcmd = cmd.toLowerCase();
        if ("exit".equals(lcmd) || "quit".equals(lcmd)) {
          break;
        } else if ("?".equals(lcmd) || "help".equals(lcmd)) {
          showHelp();
          showPrompt();
        } else if (lcmd.equals("auth")) {
          auth();
        } else if (cmd.startsWith("import ")) {
          importFile(cmd.substring(7).split(" "));
        } else {
          service.query(cmd, new SimpleCallback<SimpleTable>() {
            @Override
            public void onSuccess(SimpleTable result) {
              System.out.println(result.getCols().serialize());
              for (JsonArray row: result.getRows()) {
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
      public void onSuccess(OAuthToken token) {
        try {
          URI uri = new URI(OAuthLogin.getAuthorizationUrl(token));
          Desktop.getDesktop().browse(uri);
          requestToken = token;
          System.out.println("");
          System.out.print("Verification code: ");
        } catch (Exception e) {
          showError("Opening Authentication page failed", e);
        }
      }
    });
  }
  
  private void getAccessToken(OAuthToken requestToken, String verificationCode) {
    OAuthLogin.getAccessToken(requestToken, verificationCode, new AsyncCallback<OAuthToken>() {
      @Override
      public void onSuccess(OAuthToken accessToken) {
        System.out.println("Token authenticated successfully.");
        service.setAccessToken(accessToken);
        
        try {
          Writer writer = new FileWriter(TOKEN_FILE);
          writer.write(accessToken.toString());
          writer.close();
          System.out.println("Token saved successfully.");
          showPrompt();
        } catch(Exception e) {
          showError("Saving Request Token failed:", e);
        }
      }

      @Override
      public void onFailure(Throwable error) {
        showError("Obtaining access token failed", error);
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
  
  
  private void importFile(String[] param) throws IOException {
    String tableId = null;
    String fileName = null;
    char delimiter = ',';
    for(int i = 0; i < param.length; i += 2) {
      String name = param[i];
      String value = param[i + 1];
      if (name.equals("file")) {
        fileName = value;
      } else if (name.equals("into")) {
        tableId = value;
      } else if (name.equals("delimiter")) {
        if (value.length() != 1) {
          showError("Delimiter must be a single character", null);
          return;
        }
        delimiter = value.charAt(0);
      }
    }
    
    String data = readFile(fileName);
    final SimpleTable table;
    if (fileName.endsWith(".bib")) {
      table = BibtexParser.parse(data);
    } else {
      table = CsvParser.parse(data, delimiter);
    }
    
    System.out.println("cols: " + table.getCols().serialize());
    
    if (tableId != null) {
      service.insert(tableId, table, new SimpleCallback<SimpleTable>() {
        @Override
        public void onSuccess(SimpleTable result) {
          System.out.println("" + result.getRowArray().length() + " rows inserted.");
          showPrompt();
        }
      });
      
    } else {
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
      service.query(sb.toString(), new SimpleCallback<SimpleTable>() {
        public void onSuccess(SimpleTable result) {
          String tableId = result.getRowArray().getArray(0).getString(0);
          service.insert(tableId, table, new SimpleCallback<SimpleTable>() {
            @Override
            public void onSuccess(SimpleTable result) {
              System.out.println("" + result.getRowArray().length() + " rows inserted.");
              showPrompt();
            }
          });
        }
      });
    }
  }

  private void showHelp() {
    System.out.println();
    System.out.println("Shell Commands");
    System.out.println("  auth         Authenticate client");
    System.out.println("  exit         Quit FT demo");
    System.out.println("  help         Show this help screen");
    System.out.println("  import file <filename> [into <tableId>] [delimiter <delimiter>]");
    System.out.println("               Import a bibtex or CSV file.");
    System.out.println();
    System.out.println("Fusion Table query examples");
    System.out.println("  select * from 197026");
    System.out.println("               Display contents of table 197026");
    System.out.println("  show tables  List available tables*");
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
