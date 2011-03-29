package com.google.code.infusion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;
import com.google.code.infusion.util.CsvParser;

public class OldFusionTableService implements DatastoreService {

	String authToken;
	
	TreeMap<String,Table> tables = new TreeMap<String,Table>();
	
	
	public OldFusionTableService(String authToken) {
		this.authToken = authToken;
	}

	public static OldFusionTableService create(String user, String password) {
		try {
			URL url = new URL("https://www.google.com/accounts/ClientLogin");
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setDoOutput(true);
			connection.setDoInput(true);
			
			OutputStreamWriter w;
			w = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
			String formdata = "accountType=GOOGLE&Email=" + URLEncoder.encode(user, "utf-8") + 
				"&Passwd=" + URLEncoder.encode(password, "utf-8") + 
				"&service=fusiontables&source=StefanHaustein-Infusion-0.1\r\n";
			w.write(formdata);
			w.close();
			
			BufferedReader reader;
			try {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			} catch(IOException e) {
				throw new RuntimeException("Authentication failure. Server message: "+ connection.getResponseMessage(), e);
			}

			String authToken = null;
			while(true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				if (line.startsWith("Auth=")) {
					authToken = line.substring(5);
				}
			}	
			return new OldFusionTableService(authToken);
	
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public Collection<Transaction> getActiveTransactions() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Transaction getCurrentTransaction() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Transaction getCurrentTransaction(Transaction returnedIfNoTxn) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PreparedQuery prepare(Query query) {
		return prepare(null, query);
	}

	@Override
	public PreparedQuery prepare(Transaction txn, Query query) {
		if (txn != null) {
			throw new UnsupportedOperationException();
		}
		return new PreparedFusionTableQuery(query);
	}

	@Override
	public KeyRangeState allocateIdRange(KeyRange range) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyRange allocateIds(String kind, long num) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyRange allocateIds(Key parent, String kind, long num) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Transaction beginTransaction() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Key... keys) {
		delete(keys);
	}

	@Override
	public void delete(Iterable<Key> keys) {
		delete(null, keys);
	}

	@Override
	public void delete(Transaction txn, Key... keys) {
		delete(txn, keys);
	}

	@Override
	public void delete(Transaction txn, Iterable<Key> keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity get(Key key) throws EntityNotFoundException {
		return get(null, key);
	}

	@Override
	public Map<Key, Entity> get(Iterable<Key> keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity get(Transaction txn, Key key) throws EntityNotFoundException {
		if (txn != null) {
			throw new UnsupportedOperationException();
		}
		Query query = new Query(key.getKind());
		query.addFilter("ROWID", Query.FilterOperator.EQUAL, key.getName());
		return prepare(query).asSingleEntity();
	}

	@Override
	public Map<Key, Entity> get(Transaction txn, Iterable<Key> keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DatastoreAttributes getDatastoreAttributes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Key put(Entity entity) {
		return put(null, entity);
	}

	@Override
	public List<Key> put(Iterable<Entity> entities) {
		return put(null, entities);
	}

	@Override
	public Key put(Transaction txn, Entity entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Key> put(Transaction txn, Iterable<Entity> entities) {
		throw new UnsupportedOperationException();
	}

	
	class Table {
		String id;
		Map<String,String> fields = new TreeMap<String,String>();
		
		Table(String id) {
			this.id = id;
		}
	}

	
	class PreparedFusionTableQuery implements PreparedQuery {
		Query query;
		
		PreparedFusionTableQuery(Query query) {
			this.query = query;
		}

		@Override
		public Iterable<Entity> asIterable() {
			return asIterable(FetchOptions.Builder.withDefaults());
		}

		@Override
		public Iterable<Entity> asIterable(FetchOptions fetchOptions) {
			return asList(fetchOptions);
		}

		@Override
		public Iterator<Entity> asIterator() {
			return asIterator(FetchOptions.Builder.withDefaults());
		}

		@Override
		public Iterator<Entity> asIterator(FetchOptions fetchOptions) {
			return asList(fetchOptions).iterator();
		}
		
		private BufferedReader execSql(String command) throws IOException {
			URL url = new URL("https://www.google.com/fusiontables/api/query?sql=" + URLEncoder.encode(command, "utf-8") + 
					"&Auth=" + URLEncoder.encode(authToken, "utf-8") );
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			System.out.println("SQL command: " + command);
			System.out.println("Auth token: " + authToken);
			
			connection.setRequestProperty("Authorization", "GoogleLogin auth="+authToken);
			return new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
		}
		
		
		private Table getTable(String id) {
			Table table = tables.get(id);
			if (table == null) {
				try {
					BufferedReader reader = execSql("DESCRIBE "+id);
					table = new Table(id);
					String line = reader.readLine();
					System.out.println(line);
					while(true) {
						line = reader.readLine();
						if (line == null) {
							break;
						}
						System.out.println(line);
						String[] parts = CsvParser.parse(line);
						if (parts.length >= 3) {
							table.fields.put(parts[1], parts[2]);
						}
					}
					tables.put(id, table);
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			}
			return table;
		}
		

		@Override
		public List<Entity> asList(FetchOptions fetchOptions) {
			Table table = getTable(query.getKind());
			
			try {
				ArrayList<Entity> result = new ArrayList<Entity>();
				
				StringBuilder fields = new StringBuilder("ROWID");
				for(String name: table.fields.keySet()) {
					fields.append(",'");
					fields.append(name);
					fields.append("'");
				}
				
				BufferedReader reader = execSql("SELECT "+fields+" FROM " + query.getKind());
				
				String line = reader.readLine();
				System.out.println(line);
				String[] names = CsvParser.parse(line);
				
				while (true) {
					line = reader.readLine();
					if (line == null) {
						break;
					}
					System.out.println(line);
					String[] parts = CsvParser.parse(line);
					Entity entity = new Entity(KeyFactory.createKey(query.getKind(), parts[0]));
					for (int i = 1; i < parts.length; i++) {
						entity.setProperty(names[i], parts[i]);
					}
					result.add(entity);
				}
				return result;
				
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public QueryResultIterable<Entity> asQueryResultIterable() {
			throw new UnsupportedOperationException();
		}

		@Override
		public QueryResultIterable<Entity> asQueryResultIterable(
				FetchOptions fetchOptions) {
			throw new UnsupportedOperationException();
		}

		@Override
		public QueryResultIterator<Entity> asQueryResultIterator() {
			throw new UnsupportedOperationException();
		}

		@Override
		public QueryResultIterator<Entity> asQueryResultIterator(
				FetchOptions fetchOptions) {
			throw new UnsupportedOperationException();
		}

		@Override
		public QueryResultList<Entity> asQueryResultList(
				FetchOptions fetchOptions) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Entity asSingleEntity() throws TooManyResultsException {
			throw new UnsupportedOperationException();
		}

		@Override
		public int countEntities() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int countEntities(FetchOptions fetchOptions) {
			throw new UnsupportedOperationException();
		}
	}
}
