package com.google.code.infusion.service;


public class FetchOptions {
  static public class Builder {
    public static FetchOptions withDefaults() {
      return new FetchOptions();
    }

    public static FetchOptions withLimit(int limit) {
      return new FetchOptions().limit(limit);
    }

    public static FetchOptions withOffset(int offset) {
      return new FetchOptions().offset(offset);
    }
  }

  int offset;
  int limit = Integer.MAX_VALUE;

  public FetchOptions limit(int limit) {
    this.limit = limit;
    return this;
  }

  public FetchOptions offset(int offset) {
    this.offset = offset;
    return this;
  }

  public int getOffset() {
    return offset;
  }

  public int getLimit() {
    return limit;
  }
}
