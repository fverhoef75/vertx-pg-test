package org.acme.vertx;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

@Path("/stuff")
public class TheOneResource {

    @Inject
    PgPool pool;

    public static final String NAME = "oi"; //static name for simplification's sake


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Template put(Template template) throws Exception {
        var result = new CompletableFuture<Template>();
        UUID id = UUID.randomUUID();
        pool.begin(res -> {
            if (res.succeeded()) {
                var transaction = res.result();

                transaction
                        .preparedQuery("INSERT INTO test.template (id, name, last_change, subject, plain, html) VALUES ($1, $2, $3, $4, $5, $6)")
                        .execute(Tuple.of(id, template.getName(),
                                LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                                template.getSubject(), template.getPlain(), template.getHtml()), ar1 -> {
                            if (ar1.succeeded()) {
                                getTemplate(transaction,
                                        t -> {
                                            result.complete(t);
                                            transaction.commit();
                                        },
                                        ex -> transaction.rollback());
                            } else {
                                transaction.rollback();
                            }
                        });
            }
        });
        return result.get();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Template get() throws Exception {
        var result = new CompletableFuture<Template>();
        getTemplate(pool, template -> result.complete(template), ex -> result.completeExceptionally(ex));

        return result.get();
    }

    private void getTemplate(SqlClient sqlClient,
            Consumer<Template> templateConsumer, Consumer<Exception> exceptionConsumer) {
        sqlClient.preparedQuery("" +
                "SELECT id, name, last_change, subject, plain, html  " +
                "FROM  test.template " +
                "WHERE name = $1 ORDER BY last_change DESC")
                .execute(Tuple.of(NAME), res -> {
                    if (res.succeeded() && res.result().rowCount() > 0) {
                        var template = Template.toTemplate(res.result().iterator().next());
                        templateConsumer.accept(template);
                    } else {
                        exceptionConsumer.accept(new NullPointerException("oops"));
                    }
                });
    }
}