package com.opennews.domain;

public enum Op {
  NONE('N'),
  INSERT('I'),
  UPDATE('U'),
  DELETE('D');

  private final char value;

  Op(char value) {
    this.value = value;
  }

  public static Op getValue(char value) {
    for (Op val : Op.values()) {
      if (val.value == value) {
        return val;
      }
    }
    return NONE;
  }

  public char getValue() {
    return value;
  }
}
