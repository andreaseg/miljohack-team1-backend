package com.example.appengine.quarkus;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.UUID;

public class UUIDMatcher extends BaseMatcher<String> {

    @Override
    public boolean matches(Object o) {
        if (!(o instanceof String)) {
            return false;
        }

        try {
            return UUID.fromString((String) o) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("uuid");
    }
}
