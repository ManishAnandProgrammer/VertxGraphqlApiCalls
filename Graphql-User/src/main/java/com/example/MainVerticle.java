package com.example;

import com.example.resolver.StateResolver;
import com.example.service.UserService;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
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

  private WebClient webClient;
  private UserService userService;
  private StateResolver stateResolver;

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    webClient = WebClient.create(vertx);
    userService = new UserService();
    stateResolver = new StateResolver(webClient);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    GraphQL graphQL = setupGraphQL();
    router.route("/graphql").handler(GraphQLHandler.create(graphQL));

    vertx.createHttpServer().requestHandler(router).listen(8080, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8080");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private GraphQL setupGraphQL() {
    /* Read the schema file from the file system. */
    String schema = vertx.fileSystem().readFileBlocking("user.graphqls").toString();
    /* (1) Parse  schema and create a TypeDefinitionRegistry */
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    /* (2) RuntimeWiring linking our schema/TypeDefinitionRegistry to our services */
    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type(newTypeWiring("Query")
        .dataFetcher("getUser", create(userService::getUser))
        .dataFetcher("hello", create(this::hello)))
      .type(newTypeWiring("User").dataFetchers(getDataFetcherForUser()))
        .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    return GraphQL.newGraphQL(graphQLSchema).build();
  }

  private Map<String, DataFetcher> getDataFetcherForUser() {

      Map<String, DataFetcher> dataFetcherMap = new HashMap<>();
      dataFetcherMap.put("state", create(stateResolver::getStateOfUser));
      return dataFetcherMap;
  }

  private void hello(DataFetchingEnvironment dataFetchingEnvironment, Promise<String> promise) {
    promise.complete("hello manish");
  }

}
