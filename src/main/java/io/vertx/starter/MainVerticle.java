package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.web.Router;
import io.vertx.redis.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MainVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(MainVerticle.class);

  private RedisConnection client = null;
  private HttpServer server;
  @Override
  public void start(Promise<Void> startPromise) {
    String logFactory = System.getProperty("org.vertx.logger-delegate-factory-class-name");
    if (logFactory == null) {
      System.setProperty("org.vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
    }

    setUpClient(ready -> {
      server = vertx.createHttpServer();

      Router router = Router.router(vertx);

      router.route().handler(routingContext -> {

        // This handler will be called for every request
        HttpServerResponse response = routingContext.response();
        log.info("request1: {}", routingContext);
        client.send(Request.cmd(Command.SET).arg("timestamp").arg(String.valueOf(System.currentTimeMillis())).arg("GET"), result -> {
          log.info("request2: {}", routingContext);
          response.putHeader("content-type", "text/plain");

          // Write to the response and end it
          response.end(Optional.ofNullable(result.result()).toString());
        });
      });

      server.requestHandler(router).listen(8080, res -> {
        if (res.succeeded()) {
          startPromise.complete();
        } else {
          startPromise.fail(res.cause());
        }
      });
    });
  }

  private void setUpClient(Handler<Void> done) {
    Redis.createClient(vertx, new RedisOptions())
      .connect(onConnect -> {
        if (onConnect.succeeded()) {
          client = onConnect.result();
          done.handle(null);
        } else {
          throw new RuntimeException(onConnect.cause());
        }
      });
  }
}
