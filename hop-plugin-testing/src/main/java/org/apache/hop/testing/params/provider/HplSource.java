package org.apache.hop.testing.params.provider;

public @interface HplSource {
  String[] files() default {};

  boolean search() default false;

  boolean withUi() default false;
}
