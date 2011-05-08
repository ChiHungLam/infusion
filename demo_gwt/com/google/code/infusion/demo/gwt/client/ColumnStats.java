package com.google.code.infusion.demo.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.code.infusion.datastore.Entity;
import com.google.code.infusion.datastore.FetchOptions;
import com.google.code.infusion.datastore.FusionTableService;
import com.google.code.infusion.datastore.Query;
import com.google.code.infusion.util.AsyncCallback;
import com.google.code.infusion.util.ChainedCallback;

public class ColumnStats implements Comparable<ColumnStats>{

  final String name;
  final int rowCount;
  int maxLength;
  int totalLength;
  int valueCount;
  Set<Object> values = new HashSet<Object>();

  private ColumnStats(String name, int count) {
    this.name = name;
    this.rowCount = count;
  }
  

  public static void getStats(FusionTableService service, Query query, int sampleSize, final AsyncCallback<List<ColumnStats>> callback) {
    
    service.prepareQuery(query).asList(FetchOptions.Builder.withLimit(sampleSize), new ChainedCallback<List<Entity>>(callback) {
      @Override
      public void onSuccess(List<Entity> result) {
        HashMap<String,ColumnStats> map = new HashMap<String,ColumnStats>();
        for(Entity entity: result) {
          for (Entry<String, Object> e : entity.getProperties().entrySet()) {
            ColumnStats stats = map.get(e.getKey());
            if (stats == null) {
              stats = new ColumnStats(e.getKey(), result.size());
              map.put(e.getKey(), stats);
            }
            stats.add(e.getValue());
          }
        }
        List<ColumnStats> list = new ArrayList<ColumnStats>();
        for(ColumnStats stats: map.values()) {
          stats.calc();
          list.add(stats);
        }
        Collections.sort(list);
        callback.onSuccess(list);
      }
    });
  }


  private void calc() {
    for (Object value: values) {
      int len = value.toString().length();
      if (len > maxLength) {
        maxLength = len;
      }
      totalLength += len;
    }
    
    valueCount = values.size();
    values = null;
  }


  private void add(Object value) {
    values.add(value);
  }


  @Override
  public int compareTo(ColumnStats stats) {
    int delta = stats.valueCount - this.valueCount;
    return delta == 0 ? this.maxLength - stats.maxLength : delta;
  }


  public String getName() {
    return name;
  }
  
}
