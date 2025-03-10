package org.apache.hop.transforms.cdc;

import org.apache.hop.testing.HopExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(HopExtension.class)
class SubOpEventTest {
  @TestTemplate
  void testSubOpEventUi(SubOpEventDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

}
