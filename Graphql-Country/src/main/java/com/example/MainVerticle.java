package com.example;

import com.example.dto.Country;
import graphql.GraphQL;
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
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;

import java.util.HashMap;
import java.util.Map;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static io.vertx.ext.web.handler.graphql.schema.VertxDataFetcher.create;

public class MainVerticle extends AbstractVerticle {

  private Map<Integer, Country> countryMappedByStateId = new HashMap<>();

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    initMap();
    GraphQL graphQL = setupGraphQL();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route("/graphql").handler(GraphQLHandler.create(graphQL));

    vertx.createHttpServer().requestHandler(router).listen(8082, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8082");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void initMap() {
    Country india = new Country(1, "India");
    Country nepal = new Country(2, "Nepal");
    countryMappedByStateId.put(1, india);
    countryMappedByStateId.put(2, india);
    countryMappedByStateId.put(3, nepal);
    countryMappedByStateId.put(4, nepal);
  }

  private GraphQL setupGraphQL() {
    /* Read the schema file from the file system. */
    String schema = vertx.fileSystem().readFileBlocking("country.graphqls").toString();
    /* (1) Parse  schema and create a TypeDefinitionRegistry */
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    /* (2) RuntimeWiring linking our schema/TypeDefinitionRegistry to our services */
    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type(newTypeWiring("Query")
        .dataFetcher("getCountryByStateId", create(this::getCountryByStateId)))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    return GraphQL.newGraphQL(graphQLSchema).build();
  }

  private void getCountryByStateId(final DataFetchingEnvironment dataFetchingEnvironment,
                                   final Promise<Country> countryPromise) {
    Integer stateId = dataFetchingEnvironment.getArgument("stateId");
    boolean containsStateId = countryMappedByStateId.containsKey(stateId);
    if (containsStateId) {
      Country country = countryMappedByStateId.get(stateId);
      countryPromise.complete(country);
    } else {
      countryPromise.fail("No country found with Given State ID");
    }
  }
}
