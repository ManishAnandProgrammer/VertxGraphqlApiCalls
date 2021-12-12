package com.example.service;

import com.example.dto.User;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import io.vertx.core.Promise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
  private Map<Integer, User> userMap;

  public UserService() {
    userMap = new HashMap<>();
    initUserMap();
  }

  private void initUserMap() {
    userMap.put(1, new User(1, "Manish"));
    userMap.put(2, new User(2, "Rajat"));
    userMap.put(3, new User(3, "Swati"));
    userMap.put(4, new User(4, "Naveen"));
  }

  public void getUser(final DataFetchingEnvironment fetchingEnvironment,
                      final Promise<User> promise) {
    Integer id = fetchingEnvironment.getArgument("id");
    User user = userMap.get(id);
    if (user == null)
      promise.fail("User Not Found");

    promise.complete(user);
  }
}
