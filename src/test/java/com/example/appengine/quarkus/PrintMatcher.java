package com.example.appengine.quarkus;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class PrintMatcher extends BaseMatcher<String>  {
    @Override
    public boolean matches(Object o) {
        System.out.println(o);
        return true;
    }

    @Override
    public void describeTo(Description description) {

    }
}
