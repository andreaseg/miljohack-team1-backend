package com.example.appengine.quarkus.datastore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class HashMapDatastoreImpl implements Datastore {

    private final ObjectMapper mapper;

    private final Map<String, String> map;

    public HashMapDatastoreImpl() {
        this.mapper = new ObjectMapper();
        this.map = new HashMap<>();
    }

    @Override
    public <T> T get(String id, Class<T> objectClass) {
        return Optional.ofNullable(map.get(id)).map(json -> {
            try {
                System.out.println("Got " + id + ":" + json + " from datastore");
                return mapper.readValue(json, objectClass);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }).orElse(null);
    }

    @Override
    public void put(String id, Object object) {
        try {
            String json = mapper.writeValueAsString(object);
            map.put(id, json);
            System.out.println("Added " + id + ":" + json + " to datastore");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String put(Object object) {
        String id = UUID.randomUUID().toString();
        put(id, object);
        return id;
    }

    @Override
    public void delete(String id) {
        map.remove(id);
        System.out.println("Deleted " + id + " from datastore");
    }
}
