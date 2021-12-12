package com.example;

import com.example.dto.Country;
import com.example.service.CountryResolver;
import com.example.service.StateService;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;

import java.util.HashMap;
import java.util.Map;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static io.vertx.ext.web.handler.graphql.schema.VertxDataFetcher.create;

public class MainVerticle extends AbstractVerticle {

  private StateService stateService;
  private CountryResolver countryResolver;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    stateService = new StateService();
    countryResolver = new CountryResolver(WebClient.create(vertx));

    GraphQL graphQL = setupGraphQL();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route("/graphql").handler(GraphQLHandler.create(graphQL));

    vertx.createHttpServer().requestHandler(router)
      .listen(8081, http -> {
          if (http.succeeded()) {
            startPromise.complete();
            System.out.println("HTTP server started on port 8081");
          } else {
            startPromise.fail(http.cause());
          }
    });
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }

  private GraphQL setupGraphQL() {
    /* Read the schema file from the file system. */
    String schema = vertx.fileSystem().readFileBlocking("state.graphqls").toString();
    /* (1) Parse  schema and create a TypeDefinitionRegistry */
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    /* (2) RuntimeWiring linking our schema/TypeDefinitionRegistry to our services */
    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type(newTypeWiring("Query")
        .dataFetcher("getStateByUserId", create(stateService::getStateByUserId)))
      .type(newTypeWiring("State").dataFetchers(getDataFetcherForState()))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    return GraphQL.newGraphQL(graphQLSchema).build();
  }

  private Map<String, DataFetcher> getDataFetcherForState() {
    Map<String, DataFetcher> dataFetcherMap = new HashMap<>();
    //dataFetcherMap.put("country", create(countryResolver::getCountryOfUser));
    dataFetcherMap.put("country", create(countryResolver::getCountry));
    return dataFetcherMap;
  }
}
