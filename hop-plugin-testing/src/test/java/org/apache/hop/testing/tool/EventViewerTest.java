package org.apache.hop.testing.tool;

import org.apache.hop.testing.SwtExtension;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SwtExtension.class)
class EventViewerTest {

  @TestTemplate
  void testEventViewer(EventViewer viewer) {
    assertNotNull(viewer);
  }
}
