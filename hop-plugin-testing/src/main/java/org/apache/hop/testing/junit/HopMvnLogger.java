package org.apache.hop.testing.junit;

import org.apache.hop.core.logging.HopLoggingEvent;
import org.apache.hop.core.logging.IHopLoggingEventListener;
import org.apache.hop.core.logging.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class HopMvnLogger implements IHopLoggingEventListener {
  private final Logger logger = LoggerFactory.getLogger(HopMvnLogger.class);
  private final List<String> ids = new ArrayList<>();

  @Override
  public void eventAdded(HopLoggingEvent event) {
    AtomicBoolean skipped = new AtomicBoolean(false);
    String message = toMessage(event, skipped);
    if (skipped.get() && event.getLevel().getLevel() > 2) {
      return;
    }
//    switch (event.getLevel()) {
//      case BASIC -> logger.info(message);
//      case ERROR -> logger.error(message, getThrowable(event));
//      case MINIMAL -> logger.warn(message, getThrowable(event));
//      case DEBUG, DETAILED -> logger.debug(message);
//      case ROWLEVEL -> logger.trace(message);
//    }
  }

  private Throwable getThrowable(HopLoggingEvent event) {
    if (event.getMessage() instanceof LogMessage message) {
      Object[] args = message.getArguments();
      if (args != null && args.length > 0 && args[args.length - 1] instanceof Throwable error) {
        return error;
      }
    }
    return null;
  }

  private String toMessage(HopLoggingEvent event, AtomicBoolean skipped) {
    if (event.getMessage() == null) {
      return event.toString();
    }
    if (event.getMessage() instanceof LogMessage message) {
      skipped.set(ids.contains(message.getLogChannelId()));
      return message.getMessage();
    }
    return event.getMessage().toString();
  }
}
