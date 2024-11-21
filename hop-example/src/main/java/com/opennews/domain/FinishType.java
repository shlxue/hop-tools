package com.opennews.domain;

public enum FinishType {
  NONE('N'),
  MERGE('M'),
  INVALID('I'),
  TRANS('T'),
  CLEAR('C'),
  SKIP('S'),
  DUPLICATE('D'),
  ABORT('A'),
  UNKNOWN('U');

  private final char value;

  FinishType(char value) {
    this.value = value;
  }

  public static FinishType getValue(char value) {
    for (FinishType type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    return NONE;
  }

  public char getValue() {
    return value;
  }
}
