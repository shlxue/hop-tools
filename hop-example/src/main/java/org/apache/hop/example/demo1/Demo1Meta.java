package org.apache.hop.example.demo1;

import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.pipeline.transform.BaseTransformMeta;

@Getter
@Setter
@Transform(id = "Demo1.Name", name = "Demo1.Description")
public class Demo1Meta extends BaseTransformMeta<Demo1, Demo1Data> {
  private String connection;
  private int age;
}
