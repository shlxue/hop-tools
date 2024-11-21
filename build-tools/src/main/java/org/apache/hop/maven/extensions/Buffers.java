package org.apache.hop.maven.extensions;

import org.apache.maven.shared.utils.logging.MessageBuilder;
import org.apache.maven.shared.utils.logging.MessageUtils;

class Buffers {
  static MessageBuilder builder() {
    return MessageUtils.buffer(128);
  }
}
