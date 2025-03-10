package org.apache.hop.testing;

public @interface HopSource {
  String[] resources() default {};

  String filter() default ".*\\.(hpl|hwf)";

  boolean withItProjects() default true;

}
