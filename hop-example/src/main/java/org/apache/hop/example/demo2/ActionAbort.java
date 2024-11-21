package org.apache.hop.example.demo2;

import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.Result;
import org.apache.hop.core.annotations.Action;
import org.apache.hop.workflow.action.ActionBase;

@Getter
@Setter
@Action(id = "Demo2.Name", name = "Demo2.Description")
public class ActionAbort extends ActionBase {
  private String name;

  @Override
  public Result execute(Result result, int i) {
    return new Result();
  }
}
