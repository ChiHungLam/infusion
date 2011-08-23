package com.google.code.infusion.service;

import java.util.ArrayList;
import java.util.List;

import com.google.code.infusion.util.Util;

public class Query {

  public enum FilterOperator {
    EQUAL("="), GREATER_THAN(">"), GREATER_THAN_OR_EQUAL(">="), IN("in"), LESS_THAN(
    "<"), LESS_THAN_OR_EQUAL("<="), NOT_EQUAL("<>");

    private String name;

    FilterOperator(String name) {
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }

  public enum SortDirection {
    ASCENDING, DESCENDING
  }

  private final List<FilterPredicate> filterPredicates = new ArrayList<FilterPredicate>();
  private final List<SortPredicate> sortPredicates = new ArrayList<SortPredicate>();
  private final List<String> columnNames = new ArrayList<String>();

  private final String kind;

  public Query(String tableId) {
    this.kind = tableId;
  }

  public Query addFilter(java.lang.String propertyName,
      Query.FilterOperator operator, java.lang.Object value) {
    filterPredicates
    .add(new FilterPredicate(propertyName, operator, value));
    return this;
  }

  public Query addSort(String propertyName) {
    sortPredicates.add(new SortPredicate(propertyName,
        SortDirection.ASCENDING));
    return this;
  }

  public Query addSort(String propertyName, SortDirection dir) {
    sortPredicates.add(new SortPredicate(propertyName, dir));
    return this;
  }

  /**
   * If no column name is specified, all columns will be returned
   * (select * from ...)
   */
  public Query addColumn(String columnName) {
    columnNames.add(columnName);
    return this;
  }

  public List<FilterPredicate> getFilterPredicates() {
    return filterPredicates;
  }

  public List<SortPredicate> getSortPredicates() {
    return sortPredicates;
  }

  public static class SortPredicate {
    private String propertyName;
    private SortDirection direction;

    SortPredicate(String propertyName, SortDirection direction) {
      this.propertyName = propertyName;
      this.direction = direction;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public SortDirection getDirection() {
      return direction;
    }
  }

  public static class FilterPredicate {
    String propertyName;
    FilterOperator operator;
    Object value;

    public FilterPredicate(String propertyName, FilterOperator operator,
        Object value) {
      this.propertyName = propertyName;
      this.operator = operator;
      this.value = value;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public FilterOperator getOperator() {
      return operator;
    }

    public java.lang.Object getValue() {
      return value;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder(Util.singleQuote(propertyName));
      sb.append(' ');
      sb.append(operator);
      sb.append(' ');
      if (value == null) {
        sb.append("");
      } else if (value instanceof Number) {
        sb.append(value.toString());
      } else {
        sb.append(Util.quote(value.toString(), '\'', true));
      }
      return sb.toString();
    }
  }

  /**
   * Returns the query as a SQL query string.
   */
  public String toString() {
    StringBuilder sb = new StringBuilder("SELECT ");
    if (columnNames.size() == 0) {
      sb.append('*');
    } else {
      for (int i = 0; i < columnNames.size(); i++) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(Util.quote(columnNames.get(i), '\'', true));
      }
    }

    sb.append(" FROM ");
    sb.append(kind);
    for (int i = 0; i < filterPredicates.size(); i++) {
      sb.append(i == 0 ? " WHERE " : " AND ");
      sb.append(filterPredicates.get(i).toString());
    }
    return sb.toString();
  }

  public String getTableId() {
    return kind;
  }
}
