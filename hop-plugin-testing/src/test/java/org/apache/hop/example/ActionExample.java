package org.apache.hop.example;

import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.Result;
import org.apache.hop.core.annotations.Action;
import org.apache.hop.workflow.action.ActionBase;

@Getter
@Setter
@Action(id = "Demo2.Name", name = "Demo2.Description")
public class ActionExample extends ActionBase {
  private String value;

  @Override
  public Result execute(Result result, int i) {
    return new Result();
  }
}
