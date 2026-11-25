package com.kumuluz.ee.rest.smoke;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

/**
 * Runs in Quarkus test mode (QuarkusClassLoader). Each filter goes through parboiled's generated parser.
 */
@QuarkusTest
public class ThingResourceTest {

    @Test
    public void filterExpressionWithAndOrAndParentheses() {
        given().queryParam("filter", "name:like:'%a' and (id:lt:2 or id:gt:3)")
                .when().get("/things")
                .then().statusCode(200)
                .header("X-Total-Count", "2")
                .body("$", containsInAnyOrder(1, 4));
    }

    @Test
    public void orderAndPaging() {
        given().queryParam("order", "id DESC").queryParam("limit", 2).queryParam("offset", 1)
                .when().get("/things")
                .then().statusCode(200)
                .header("X-Total-Count", "4")
                .body("$", contains(3, 2));
    }

    @Test
    public void likeOnNativeUuid() {
        given().queryParam("filter", "ref:like:'abcdef12%'")
                .when().get("/things")
                .then().statusCode(200)
                .body("$", containsInAnyOrder(1, 3));
    }

    @Test
    public void likeIgnoreCaseOnNativeUuid() {
        given().queryParam("filter", "ref:likeic:'ABCDEF12%'")
                .when().get("/things")
                .then().statusCode(200)
                .body("$", containsInAnyOrder(1, 3));
    }

    @Test
    public void nestedFieldFilter() {
        given().queryParam("filter", "parent.name:eq:'alpha'")
                .when().get("/things")
                .then().statusCode(200)
                .body("$", containsInAnyOrder(2, 4));
    }

    @Test
    public void invalidFilterIsReported() {
        given().queryParam("filter", "name:unknownop:'x'")
                .when().get("/things")
                .then().statusCode(equalTo(500));
    }
}
