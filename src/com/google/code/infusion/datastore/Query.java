package com.google.code.infusion.datastore;


import java.util.ArrayList;
import java.util.List;

public class Query {
	
	public enum FilterOperator {
		EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL,
		IN, LESS_THAN, LESS_THAN_OR_EQUAL, NOT_EQUAL 
	}
	
	public enum SortDirection {
		ASCENDING, DESCENDING
	}
	
	private final List<FilterPredicate> filterPredicates = new ArrayList<FilterPredicate>();
	private final List<SortPredicate> sortPredicates = new ArrayList<SortPredicate>();
	 
	private final String kind;
	
	public Query(String tableId) {
		this.kind = tableId;
	}
	
	
	public Query addFilter(java.lang.String propertyName,
            Query.FilterOperator operator,
            java.lang.Object value) {
		filterPredicates.add(new FilterPredicate(propertyName, operator, value));
		return this;
	}

	public Query addSort(String propertyName) {
		sortPredicates.add(new SortPredicate(propertyName, SortDirection.ASCENDING));
		return this;
	}
	
	public Query addSort(String propertyName, SortDirection dir) {
		sortPredicates.add(new SortPredicate(propertyName, dir));
		return this;
	}
		
	public List<FilterPredicate>getFilterPredicates() {
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
		
		public FilterPredicate(String propertyName,
				FilterOperator operator,
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
	}

	public String getKind() {
		return kind;
	}
}

