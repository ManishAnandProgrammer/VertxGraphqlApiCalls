package com.example.resolver;

import com.example.dto.State;
import com.example.dto.User;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import java.util.Map;

public class StateResolver {
  private final WebClient webClient;

  public StateResolver(WebClient webClient) {
    this.webClient = webClient;
  }

  public void getStateOfUser(DataFetchingEnvironment dataFetchingEnvironment, Promise<State> userPromise) {
    User source = dataFetchingEnvironment.getSource();
    Integer userId = source.getId();
    webClient.postAbs("http://localhost:8081/graphql")
      .sendJsonObject(payload(userId))
      .onComplete(httpResponseAsyncResult -> {
        if (httpResponseAsyncResult.succeeded()) {
            HttpResponse<Buffer> result = httpResponseAsyncResult.result();
            JsonObject entries = result.bodyAsJsonObject();
            JsonObject data = entries.getJsonObject("data");
            JsonObject getStateByUserId = data.getJsonObject("getStateByUserId");
            State location = getStateByUserId.mapTo(State.class);
            userPromise.complete(location);
        } else {
          userPromise.fail(httpResponseAsyncResult.cause());
        }
      });
  }

  private JsonObject payload(final Integer userId) {
    return new JsonObject().put(
      "query",
        "query($userId:Int!)" +
        "{" +
          "getStateByUserId(userId:$userId) {" +
            "id " +
            "name " +
            "country {" +
              "id " +
              "name " +
            "}" +
          "}" +
        "}"
      )
      .put("variables", variables(userId));
  }

  private Map<String, Object> variables(final Integer userId) {
    return Map.of("userId", userId);
  }

}
