package org.apache.hop.testing.junit;

import org.apache.hop.core.logging.HopLoggingEvent;
import org.apache.hop.core.logging.IHopLoggingEventListener;
import org.apache.hop.core.logging.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HopMvnLogger implements IHopLoggingEventListener {
  private final Logger log = LoggerFactory.getLogger(HopMvnLogger.class);

  @Override
  public void eventAdded(HopLoggingEvent event) {
    String text = null;
    if (event.getMessage() instanceof LogMessage message) {
      text = message.getMessage();
    } else if (event.getMessage() != null) {
      text = event.getMessage().toString();
    }
    if (text != null) {
      switch (event.getLevel()) {
        case BASIC -> log.info("{}", text);
        case ERROR -> log.error("{}", text);
        case MINIMAL -> log.warn("{}", text);
        case DEBUG, DETAILED -> log.debug("{}", text);
        case ROWLEVEL -> log.trace("{}", text);
      }
    }
  }
}
