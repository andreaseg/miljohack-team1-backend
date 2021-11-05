/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class HouseResourceTest {


    private class TestHouse {
        public String data = UUID.randomUUID().toString();

    }

    @BeforeEach
    public void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        System.setProperty("store.impl", "HashMap");
    }


    @Test
    public void postNew() {

        given()
                .when()
                .body(new TestHouse())
                .contentType(ContentType.JSON)
                .accept(ContentType.ANY)
                .post("/houses/")
                .then()
                .statusCode(200)
                .body(is(new UUIDMatcher()));

    }

    @Test
    public void retrieve() {

        var uuid = UUID.randomUUID();

        var body = new TestHouse();

        given()
                .when()
                .body(body)
                .contentType(ContentType.JSON)
                .post("/houses/" + uuid)
                .then()
                .statusCode(204);

        given()
                .when()
                .accept(ContentType.JSON)
                .get("/houses/" + uuid)
                .then()
                .statusCode(200)
                .body(containsString(body.data));
    }

    @Test
    public void preventInvalidId() {
        var id = "invalid";

        var body = new TestHouse();

        given()
                .when()
                .body(body)
                .contentType(ContentType.JSON)
                .post("/houses/" + id)
                .then()
                .statusCode(400);
    }

    @Test
    public void notFoundResponse() {
        var id = "invalid";

        given()
                .when()
                .accept(ContentType.JSON)
                .get("/houses/" + id)
                .then()
                .statusCode(404);
    }
}
