package org.apache.hop.testing.junit;

import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.transforms.dummy.Dummy;
import org.apache.hop.testing.params.provider.HopFilter;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class HopMetaSearcherTest {

  @TestFactory
  @HopFilter(Dummy.class)
  Stream<DynamicTest> forEachMeta(IPipelineEngine<?> engine) {
    //    List<Path> paths =
    //        new HopMetaSearcher()
    //            .search(true, this::filter);
    // Paths.get("/Users/shl/Projects/opennews/hop/integration-tests")
    //    Assertions.assertEquals(0, paths.size());
    //    new LocalPipelineEngine(pipelineMeta).execute();
    return Stream.of(DynamicTest.dynamicTest("aTest", () -> {}));
  }

  @TestFactory
  Stream<DynamicTest> forEachMeta(PipelineMeta pipelineMeta) {
    //    List<Path> paths =
    //        new HopMetaSearcher()
    //            .search(true, this::filter);
    // Paths.get("/Users/shl/Projects/opennews/hop/integration-tests")
    //    Assertions.assertEquals(0, paths.size());
    //    new LocalPipelineEngine(pipelineMeta).execute();
    return Stream.of(DynamicTest.dynamicTest("aTest", () -> {}));
  }

  private boolean filter(String id, String[] names) {
    return false;
  }
}
