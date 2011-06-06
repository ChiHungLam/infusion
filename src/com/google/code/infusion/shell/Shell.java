package com.google.code.infusion.shell;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;

import com.google.code.infusion.server.OAuthLogin;
import com.google.code.infusion.service.FusionTableService;
import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.service.Table;
import com.google.code.infusion.util.OAuth.Token;
import com.google.code.infusion.util.Util;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Shell {
  static final String TOKEN_FILE = ".token";
  
  BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  FusionTableService service = new FusionTableService();
  
  public static void main(String[] args) throws IOException {
    new Shell().run();
  }

  private void init() {
    try {
      BufferedReader tokenReader = new BufferedReader(new FileReader (TOKEN_FILE));
      Token token = new Token();
      token.setToken(tokenReader.readLine());
      token.setTokenSecret(tokenReader.readLine());
      service.setRequestToken(token);
      tokenReader.close();
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
        } else if (lcmd.equals("auth")) {
          auth();
    //    } else if (cmd.startsWith("import ")) {
     //     importFile(cmd.substring(7));
        } else {
          service.sendQuery(cmd, new SimpleCallback<Table>() {
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
    OAuthLogin.getRequestToken(FusionTableService.SCOPE, new SimpleCallback<Token>() {
      @Override
      public void onSuccess(Token requestToken) {
        URI uri;
        try {
          uri = new URI("https://www.google.com/accounts/OAuthAuthorizeToken?hd=default&oauth_token=" + Util.urlEncode(requestToken.getToken()));
          Desktop.getDesktop().browse(uri);
          
          System.out.println("");
          System.out.print("Verification code: ");
          String verificationCode = reader.readLine();
        
          OAuthLogin.getAccessToken(requestToken, verificationCode, new SimpleCallback<Token>() {

            @Override
            public void onSuccess(Token result) {
              service.setRequestToken(result);
              
              try {
                PrintWriter writer = new PrintWriter(new FileWriter(TOKEN_FILE));
                writer.println(result.getToken());
                writer.println(result.getTokenSecret());
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

/*
  private void importFile(String fileName) throws IOException {
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
    String data = sb.toString();
    
    Iterator<Map<String,String>> parser;
    
    if (fileName.endsWith(".bib")) {
      parser = new BibtexParser(data);
    } else {
      parser = new CsvParser(data, true);
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
    
    HashSet<String> fields = new HashSet<String>();
    final ArrayList<Map<String,String>> entries = new ArrayList<Map<String,String>>();
    while (parser.hasNext()) {
      Map<String,String> entry = parser.next();
      fields.addAll(entry.keySet());
      entries.add(entry);
    }
    
    ArrayList<ColumnInfo> columns = new ArrayList<ColumnInfo>();
    for (String field: fields) {
      columns.add(new ColumnInfo(field, ColumnType.STRING));
    }
    
    service.createTable(name, columns, new SimpleCallback<String>() {
      public void onSuccess(String tableId) {
        ArrayList<Entity> entities = new ArrayList<Entity>();
        for (Map<String,String> map: entries) {
          Entity entity = new Entity(tableId);
          for (Map.Entry<String, String> e : map.entrySet()) {
            entity.setProperty(e.getKey(), e.getValue());
          }
          entities.add(entity);
        }
        service.put(entities, new SimpleCallback<List<Key>>() {
          @Override
          public void onSuccess(List<Key> result) {
            System.out.println("" + result.size() + " entities updated / inserted.");
            showPrompt();
          }
        });
      }
    });
  }
  */

  private void showHelp() {
    System.out.println();
    System.out.println("Shell Commands");
    System.out.println("  auth:                  Authenticate client");
    System.out.println("  exit:                  Quit FT demo");
    System.out.println("  help:                  Show this help screen");
    System.out.println();
    System.out.println("Fusion Table Commands");
    System.out.println("  describe <table id>:   Show table structure");
    System.out.println("  drop table <table id>: Drop (delete) the table");
    System.out.println("  show tables:           List available tables");
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
    caught.printStackTrace(System.out);
    showPrompt();
  }
  
  abstract class SimpleCallback<T> implements AsyncCallback<T> {
    @Override
    public void onFailure(Throwable error) {
      showError(error);
    }
  }
}
