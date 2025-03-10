package org.apache.hop.it;

import org.apache.hop.parquet.transforms.input.ParquetInputDialog;
import org.apache.hop.parquet.transforms.output.ParquetOutputDialog;
import org.apache.hop.testing.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(ui = SpecMode.PREVIEW)
class TechTest {
    @TestTemplate
    void testParquetInputUi(ParquetInputDialog dialog) {
        Assertions.assertNull(dialog);
    }
    @TestTemplate
    void testParquetInputUi(ParquetOutputDialog dialog) {
        Assertions.assertNull(dialog);
    }
}
