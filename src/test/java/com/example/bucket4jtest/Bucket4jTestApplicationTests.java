package com.example.bucket4jtest;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.mssql.Bucket4jMSSQL;
import io.github.bucket4j.mssql.MSSQLSelectForUpdateBasedProxyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.time.Duration.ofSeconds;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class Bucket4jTestApplicationTests {

    private static final String INIT_TABLE_SCRIPT = "CREATE TABLE bucket (id BIGINT NOT NULL PRIMARY KEY, state BINARY(256), expires_at BIGINT)";

    @Autowired
    DataSource dataSource;

    @BeforeEach
    public void init() {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(INIT_TABLE_SCRIPT);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void contextLoads() throws Exception {
        MSSQLSelectForUpdateBasedProxyManager<Long> proxyManager = Bucket4jMSSQL
                .selectForUpdateBasedBuilder(dataSource)
                .build();


        BucketConfiguration bucketConfiguration = BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(10).refillGreedy(10, ofSeconds(1)))
                .build();


        new Thread(() -> {
            for (int i = 1; i < 100000; i++) {
                BucketProxy bucket = proxyManager.getProxy(i * 100000L, () -> bucketConfiguration);
                System.out.println(bucket.getAvailableTokens());
            }
        }).start();


        new Thread(() -> {
            try {
                BucketProxy bucket = proxyManager.getProxy(0L, () -> bucketConfiguration);
                while (true) {
                    bucket.tryConsume(1);
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(10_000_000);
    }

}
