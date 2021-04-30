package org.acme.vertx;

import static io.restassured.RestAssured.given;

import java.util.Arrays;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class TheOneResourceTest {

    @Inject
    Flyway flyway;

    private static final ObjectMapper MAPPER = configureObjectMapper(new ObjectMapper());

    static ObjectMapper configureObjectMapper(ObjectMapper mapper) {
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        return mapper;
    }

    @BeforeEach
    public void init() {
        flyway.clean();
        flyway.migrate();

    }

    @Test
    public void test() throws Exception {

        System.out.println("====================================== SETUP");

        Template template = new Template();
        template.setSubject("subject");
        template.setPlain("PLAIN");
        template.setName("oi");
        template.setHtml("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<title>Page Title</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<h1>My First Heading</h1>\n" +
                "<p>My first paragraph.</p>\n" +
                "\n" +
                "</body>\n" +
                "</html>");

        System.out.println("====================================== FIRST INSERT");

        var response1 = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(template)
                .when().put("stuff")
                .then().extract().response();
//        response1.prettyPrint();

        System.out.println("====================================== GET LAST");

        for (int i = 0; i < 3; i++) {
            var response2 = given()
                    .when().get("/stuff")
                    .then()
                    .statusCode(200).extract().response();
//            response2.prettyPrint();
        }

        System.out.println("====================================== SECOND INSERT");

        template.setPlain("PLAIN 2");
        var response3 = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(template)
                .when().put("stuff")
                .then().extract().response();
//        response3.prettyPrint();
        if (!isCorrect(template, response3)) {
            Assertions.fail();
        }

        System.out.println("====================================== GET LAST");

        boolean failed = false;
        for (int i = 0; i < 10; i++) {
            var response4 = given()
                    .when().get("/stuff")
                    .then()
                    .statusCode(200).extract().response();

//            response4.prettyPrint();
            boolean correct = isCorrect(template, response4);
            if (!correct) {
                failed = true;
            }
        }
        if (failed) {
            Assertions.fail("Not all fetched the latest insert");
        }
    }

    private boolean isCorrect(Template template, io.restassured.response.Response response) throws java.io.IOException {
        var templ = MAPPER.readValue(response.asByteArray(), Template.class);
        boolean correct = template.getPlain().equals(templ.getPlain());
        System.out.printf("source->plain=%s ; response->plain=%s ; correct=%s%n", template.getPlain(), templ.getPlain(), correct);
        return correct;
    }

}