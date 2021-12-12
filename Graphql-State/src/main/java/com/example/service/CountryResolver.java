package com.example.service;

import com.example.dto.Country;
import com.example.dto.State;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import java.util.Map;

public class CountryResolver {
  private final WebClient webClient;

  public CountryResolver(WebClient webClient) {
    this.webClient = webClient;
  }

  public void getCountry(DataFetchingEnvironment dataFetchingEnvironment, Promise<Country> countryPromise) {
    State state = dataFetchingEnvironment.getSource();
    Integer stateId = state.getId();
    webClient.postAbs("http://localhost:8082/graphql")
      .sendJsonObject(payload(stateId))
      .onComplete(event -> {
        if (event.succeeded()) {
          HttpResponse<Buffer> result = event.result();
          JsonObject entries = result.bodyAsJsonObject();
          JsonObject data = entries.getJsonObject("data");
          JsonObject countryObjectAsJson = data.getJsonObject("getCountryByStateId");
          Country country = countryObjectAsJson.mapTo(Country.class);
          countryPromise.complete(country);
        } else {
          countryPromise.fail(event.cause());
        }
      });
  }

  private JsonObject payload(final Integer stateId) {
    JsonObject payload = new JsonObject();
    payload.put("query",
      "query($stateId: Int!) {" +
      "  getCountryByStateId(stateId:$stateId) {" +
      "    id" +
      "    name" +
      "  }" +
      "}");
    payload.put("variables", Map.of("stateId", stateId));
    return payload;
  }
}
