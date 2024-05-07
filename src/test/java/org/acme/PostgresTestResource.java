package org.acme;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>("postgres:15.5-bullseye")
                    .withDatabaseName("mydb")
                    .withUsername("myuser")
                    .withPassword("mypassword")
                    .withNetworkAliases("mypostgre");


    @Override
    public Map<String, String> start() {
        db.setPortBindings(List.of("5432:5432"));
        db.start();
        return Collections.singletonMap(
                "quarkus.datasource.jdbc.url", "jdbc:postgresql://127.0.0.1:5432/mydb"
        );
    }


    @Override
    public void stop() {
        db.stop();
    }


    public PostgreSQLContainer<?> getContainer() {
        return db;
    }

}

