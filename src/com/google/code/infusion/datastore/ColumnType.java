package com.google.code.infusion.datastore;

public interface ColumnType<T> {

	static final ColumnType<String> STRING = new AbstractColumnType<String>(
			"string") {
		public String parse(String s) {
			return s;
		}
	};

	String getName();

	T parse(String s);

	String toString(T value);

	static abstract class AbstractColumnType<T> implements ColumnType<T> {
		private final String name;

		private AbstractColumnType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String toString() {
			return name;
		}

		public String toString(T value) {
			return value.toString();
		}
	}
}
