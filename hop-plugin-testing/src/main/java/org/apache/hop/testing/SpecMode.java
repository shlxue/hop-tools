package org.apache.hop.testing;

import org.apache.hop.testing.junit.StatusUtil;

public enum SpecMode {
  NONE(false, false, false, false, false, 0),
  STRICT(true, true, false, true, true, StatusUtil.delayTime(0)),
  HEADLESS(true, true, false, true, false, StatusUtil.delayTime(1)),
  NORMAL(true, false, false, false, false, StatusUtil.delayTime(10)),
  DEBUG(true, false, true, true, true, StatusUtil.delayTime(100)),
  PREVIEW(false, false, true, false, false, StatusUtil.delayTime(20000));

  private final boolean test;
  private final boolean headless;
  private final boolean colorized;
  private final boolean staticEvent;
  private final boolean dynamicEvent;
  private final long waitTimeMs;

  SpecMode(
      boolean test,
      boolean headless,
      boolean colorized,
      boolean staticEvent,
      boolean dynamicEvent,
      long waitTimeMs) {
    this.test = test;
    this.headless = headless;
    this.colorized = colorized;
    this.staticEvent = staticEvent;
    this.dynamicEvent = dynamicEvent;
    this.waitTimeMs = waitTimeMs;
  }

  public boolean isTest() {
    return test;
  }

  public boolean isHeadless() {
    return headless;
  }

  public boolean isColorized() {
    return colorized;
  }

  public boolean isStaticEvent() {
    return staticEvent;
  }

  public boolean isDynamicEvent() {
    return dynamicEvent;
  }

  public long getWaitTimeMs() {
    return waitTimeMs;
  }
}
