package org.apache.hop.testing.junit;

import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.database.DatabasePluginType;
import org.apache.hop.core.logging.HopLogStore;
import org.apache.hop.core.plugins.ActionPluginType;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.plugins.TransformPluginType;
import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopEnv.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HopJunitTest {

  @Test
  void testCommonPlugins() {
    try (HopJunit hopJunit = new HopJunit(Type.HOP_LOCAL, false)) {
      hopJunit.waitUntilLoaded();
      Assertions.assertTrue(HopLogStore.isInitialized());
      Assertions.assertTrue(HopClientEnvironment.isInitialized());
    }
    Assertions.assertTrue(HopLogStore.isInitialized());
    Assertions.assertFalse(HopClientEnvironment.isInitialized());
  }

  @Test
  void testHopEnv() {
    assertHopEnv(Type.MOCK, 14, 2, 3, 3);
    assertHopEnv(Type.HOP_LOCAL, 18, 2, 3, 3);
    assertHopEnv(Type.BEAM_DIRECT, 19, 2, 3, 3);
  }

  private void assertHopEnv(HopEnv.Type type, int count, int action, int database, int transform) {
    try (HopJunit hopJunit = new HopJunit(type, false)) {
      hopJunit.waitUntilLoaded();
      assertHopPlugins(count, action, database, transform);
    }
  }

  private void assertHopPlugins(int types, int action, int database, int transform) {
    PluginRegistry registry = PluginRegistry.getInstance();
    Assertions.assertArrayEquals(
        new int[] {types, action, database, transform},
        new int[] {
          registry.getPluginTypes().size(),
          registry.getPlugins(ActionPluginType.class).size(),
          registry.getPlugins(DatabasePluginType.class).size(),
          registry.getPlugins(TransformPluginType.class).size(),
        });
  }
}
