package com.google.code.infusion.shell;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;

import com.google.code.infusion.service.FusionTableService;
import com.google.code.infusion.service.Table;
import com.google.code.infusion.importer.Importer;
import com.google.code.infusion.importer.ImporterBuilder;
import com.google.code.infusion.importer.ImporterCallback;
import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.util.OAuthLogin;
import com.google.code.infusion.util.OAuthToken;
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
  Throwable trace;
  
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
        } else if (cmd.equals("trace")) {
          if (trace == null) {
            System.out.println("No stacktrace available.");
          } else {
            trace.printStackTrace(System.out);
          }
          showPrompt();
        } else {
          service.query(cmd, new SimpleCallback<Table>() {
            @Override
            public void onSuccess(Table result) {
              System.out.println(Arrays.toString(result.getCols()));
              for (JsonArray row: result) {
                System.out.println(row.serialize());
              }
              showPrompt();
            }});
        }
      } catch (IOException e) {
        showError(null, e);
      }
    }
    println("Good bye!");
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
    ImporterBuilder builder = new ImporterBuilder(service);
    String fileName = null;
    for(int i = 0; i < param.length; i += 2) {
      String name = param[i];
      String value = param[i + 1];
      if (name.equals("file")) {
        fileName = value;
        builder.setFileName(value);
      } else if (name.equals("into")) {
        builder.setTableId(value);
      } else if (name.equals("delimiter")) {
        if (value.length() != 1) {
          showError("Delimiter must be a single character", null);
          return;
        }
        builder.setDelimiter(value.charAt(0));
      } else if (name.equals("offset")) {
        builder.setOffset(Integer.parseInt(value));
      }
    }
    builder.setData(readFile(fileName));
    builder.importData(new ImporterCallback() {
      @Override
      public void onProgress(Importer importer) {
        System.out.println("Import progress: " + importer.getCount() + " rows.");
      }

      @Override
      public void onSuccess(Importer importer) {
        System.out.println("Import successfull; imported " + importer.getCount() + " rows.");
        showPrompt();
      }

      @Override
      public void onFailure(Importer importer, Throwable error) {
        showError("Import failed. " + importer.getCount() + 
            " rows were imported. Re-try with offset " + 
            (importer.getOffset() + importer.getCount()), error);
      }
      
    });
  }
    
  public void println(String s) {
    System.out.println(s);
  }

  private void showHelp() {
    System.out.println();
    System.out.println("Shell Commands");
    System.out.println("  auth         Authenticate client");
    System.out.println("  exit         Quit FT demo");
    System.out.println("  help         Show this help screen");
    System.out.println("  trace        Show stack trace for last error");
    System.out.println("  import file <filename> [into <tableId>] [delimiter <delimiter>]");
    System.out.println("               [offset <offset>]");
    System.out.println("               Import a bibtex or CSV file.");
    System.out.println();
    System.out.println("Fusion Table query examples");
    System.out.println("  select * from 197026");
    System.out.println("               Display contents of table 197026");
    System.out.println("  show tables  List available tables*");
    System.out.println("");
    System.out.println("*) Requies authentication");
  }


  public void showPrompt() {
    System.out.print("\nCommand? ");
    System.out.flush();
  }

  public void showError(String message, Throwable caught) {
    System.out.println();
    if (message != null) {
      System.out.println(message);
    } else if (caught != null) {
      System.out.println("" + caught.getMessage());
    }
    trace = caught;
    showPrompt();
  }
  
  abstract class SimpleCallback<T> implements AsyncCallback<T> {
    @Override
    public void onFailure(Throwable error) {
      showError(error.getMessage(), error);
    }
  }

}
