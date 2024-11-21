package org.apache.hop.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HopEnv {

  Type type() default Type.MOCK;

  boolean withH2() default false;

  SqlMode sqlMode() default SqlMode.H2;

  SpecMode ui() default SpecMode.HEADLESS;

  enum Type {
    MOCK(true, false),
    HOP_LOCAL(false, false),
    BEAM_DIRECT(false, true);

    private final boolean mock;
    private final boolean beam;

    Type(boolean mock, boolean beam) {
      this.mock = mock;
      this.beam = beam;
    }

    public boolean isMock() {
      return mock;
    }

    public boolean isBeam() {
      return beam;
    }
  }
}
