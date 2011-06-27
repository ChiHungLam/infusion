package com.google.code.infusion.shell;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Iterator;

import com.google.code.infusion.service.FusionTableService;
import com.google.code.infusion.service.Table;
import com.google.code.infusion.importer.BibtexParser;
import com.google.code.infusion.importer.CsvParser;
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
          service.query(cmd, new SimpleCallback<Table>() {
            @Override
            public void onSuccess(Table result) {
              System.out.println(result.getCols().serialize());
              for (JsonArray row: result) {
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
    File file = new File(fileName);
    byte[] buf = new byte[(int) file.length()];
    DataInputStream dis = new DataInputStream(new FileInputStream(fileName));
    dis.readFully(buf);
    return new String(buf, "utf-8");
  }
  
  
  private void importFile(String[] param) throws IOException {
    String tableId = null;
    String fileName = null;
    char delimiter = ',';
    int offset = 0;
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
      } else if (name.equals("offset")) {
        offset = Integer.parseInt(value);
      }
    }
    
    String data = readFile(fileName);
    final JsonArray cols;
    final Iterator<JsonArray> rows;
    if (fileName.endsWith(".bib")) {
      Table table = BibtexParser.parse(data);
      cols = table.getCols();
      rows = table.iterator();
    } else {
      rows = new CsvParser(data, delimiter);
      cols = rows.next();
    }

    System.out.println("cols: " + cols.serialize());
    if (offset != 0) {
      System.out.print("Advancing " + offset + " rows... ");
      for (int i = 0; i < offset; i++) {
        rows.next();
      }
      System.out.println("done.");
    }
    
    if (tableId != null) {
      importRows(tableId, cols, rows, offset);
      return;
    }
    final int finalOffset = offset;
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
    for (int i = 0; i < cols.length(); i++) {
      if (i != 0) {
        sb.append(',');
      }
      sb.append(Util.singleQuote(cols.getString(i)));
      sb.append(":STRING");
    }
    sb.append(')');
    service.query(sb.toString(), new SimpleCallback<Table>() {
      public void onSuccess(Table result) {
        String tableId = result.iterator().next().getString(0);
        importRows(tableId, cols, rows, finalOffset);
      }
    });
  }
  
  private void importRows(final String tableId, final JsonArray cols, final Iterator<JsonArray> rows, final int offset) {
    final Table buf = new Table(cols, JsonArray.create());
    if (!rows.hasNext()) {
      if (offset == 0) {
        System.out.println("Nothing to import.");
      } else {
        System.out.println("All rows imported successfully.");
      }
      showPrompt();
      return;
    }
    for (int i = 0; i < 250 && rows.hasNext(); i++) {
      buf.addRow(rows.next());
    }
    
    System.out.print("Importing rows " + offset + " to " + (offset + buf.getRowCount()) + "... ");
    service.insert(tableId, buf, new AsyncCallback<Table>() {
      @Override
      public void onSuccess(Table result) {
        System.out.println("Ok.");
        importRows(tableId, cols, rows, offset + buf.getRowCount());
      }

      @Override
      public void onFailure(Throwable error) {
        System.out.println(error.getMessage());
        if (offset != 0) {
          System.out.println("You may be able to import the remaining data spcecifying table " + tableId + " and offset "+ offset);
        }
        showPrompt();
      }
    });
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
