package com.example.appengine.quarkus;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class FirestoreDatastoreImpl implements Datastore {

    private final String collection;

    private final Firestore firestore;

    public FirestoreDatastoreImpl(String projectId, String collection) throws IOException {

        if (projectId == null) {
            throw new IllegalArgumentException("ProjectId is missing");
        }

        if (collection == null) {
            throw new IllegalArgumentException("Collection is missing");
        }

        this.collection = collection;

        FirestoreOptions firestoreOptions =
                FirestoreOptions.getDefaultInstance().toBuilder()
                        .setProjectId(projectId)
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();

        this.firestore = firestoreOptions.getService();
    }

    @Override
    public <T> T get(String id, Class<T> objectClass) {
        try {
            return firestore.collection(collection).document(id).get().get().toObject(objectClass);
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void put(String id, Object object) {
        try {
            firestore.collection(collection).document(id).set(object).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String put(Object object) {
        try {
            return firestore.collection(collection).add(object).get().getId();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            firestore.collection(collection).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
