package emv.benchmark.db;

import java.util.Random;

public enum FlowsState {
  EXECUTING("EXECUTING"),
  WAITING("WAITING"),
  SLEEPING("SLEEPING"),
  DEAD("DEAD");

  private final String state;

  FlowsState(String state) {
    this.state = state;
  }

  public static FlowsState getRandomState() {
    FlowsState[] values = FlowsState.values();
    return values[new Random().nextInt(values.length)];
  }

  @Override
  public String toString() {
    return state;
  }
}
