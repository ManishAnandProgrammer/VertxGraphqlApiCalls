package com.example.service;

import com.example.dto.State;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Promise;

import java.util.HashMap;
import java.util.Map;

public class StateService {
  private Map<Integer, State> stateMap;

  public StateService() {
    stateMap = new HashMap<>();
    initMap();
  }

  private void initMap() {
    stateMap.put(1, new State(1, "Haryana"));
    stateMap.put(2, new State(2, "Punjab"));
    stateMap.put(3, new State(3, "Himachal"));
    stateMap.put(4, new State(4, "Utter Pardesh"));
  }

  public void getStateByUserId(DataFetchingEnvironment dataFetchingEnvironment,
                               Promise<State> promise) {
    Integer userId = dataFetchingEnvironment.getArgument("userId");
    State state = stateMap.get(userId);
    if (state == null)
      promise.fail("No State Found with Given User Id");

    promise.complete(state);
  }
}
