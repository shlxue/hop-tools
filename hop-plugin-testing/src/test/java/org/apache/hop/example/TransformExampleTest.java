package org.apache.hop.example;

import org.apache.hop.testing.HopAssert;
import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(withH2 = true)
class TransformExampleTest {

  @TestTemplate
  void previewPluginUi(TransformExampleDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void testAsyncUi(TransformExampleMeta before, TransformExampleMeta after) {
    HopAssert.assertEqual(before, after);
  }
}
