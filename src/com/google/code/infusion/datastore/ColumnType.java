package com.google.code.infusion.datastore;

import java.util.Date;

public interface ColumnType<T> {

  static final String[] MONTH_NAMES = {
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  };
  
  static final ColumnType<String> STRING = 
    new AbstractColumnType<String>("String") {
      public String parse(String s) {
        return s;
      }
    };

  static final ColumnType<Double> NUMBER = 
    new AbstractColumnType<Double>("Number") {
      public String getBaseType() {
        return "NUMBER";
      }
      public Double parse(String s) {
        return Double.parseDouble(s.trim());
      }
    };
  
  static final ColumnType<Location> LOCATION = 
    new AbstractColumnType<Location>("Location") {
    public String getBaseType() {
      return "LOCATION";
    }
    public Location parse(String s) {
      return new Location(s.trim());
    }
  };
    
  static final ColumnType<Date> DATETIME = 
    new AbstractColumnType<Date>("DateTime") {
      @SuppressWarnings("deprecation")
      public String toString(Date date) {
        return MONTH_NAMES[date.getMonth()] + " " + date.getDay() + ", " + date.getYear();
      }
      
      public String getBaseType() {
        return "DATETIME";
      }

      @SuppressWarnings("deprecation")
      public Date parse(String s) {
        s = s.trim();
        if (s.length() == 0) {
          return null;
        }
        int space = s.indexOf(' ');
        int comma = s.lastIndexOf(',');
        int year = Integer.parseInt(s.substring(comma + 1).trim());
        int day = Integer.parseInt(s.substring(space + 1, comma).trim());
        int month;
        for (month = 0; month < MONTH_NAMES.length; month++) {
          if (s.startsWith(MONTH_NAMES[month])) {
            break;
          }
        }
        return new Date(year, month, day);
      }
    };

  T parse(String s);

  String toString(T value);

  String getBaseType();

  
  static abstract class AbstractColumnType<T> implements ColumnType<T> {
    private final String name;

    protected AbstractColumnType(String name) {
      this.name = name;
    }

    public String toString() {
      return name;
    }

    public String toString(T value) {
      return value.toString();
    }
    
    public String getBaseType() {
      return "STRING";
    }
  }

}
