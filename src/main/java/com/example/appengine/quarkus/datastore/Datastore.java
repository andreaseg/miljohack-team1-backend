package com.example.appengine.quarkus.datastore;

public interface Datastore {
    <T> T get(String id, Class<T> objectClass);
    void put(String id, Object object);
    String put(Object object);
    void delete(String id);
}
