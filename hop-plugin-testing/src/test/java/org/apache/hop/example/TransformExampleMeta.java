package org.apache.hop.example;

import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.pipeline.transform.BaseTransformMeta;

@Getter
@Setter
@Transform(id = "Demo1.Name", name = "Demo1.Description")
public class TransformExampleMeta
    extends BaseTransformMeta<TransformExample, TransformExampleData> {
  private String connection;
  private int age;
  private String content;
  private int type;
  private int customType;
}
