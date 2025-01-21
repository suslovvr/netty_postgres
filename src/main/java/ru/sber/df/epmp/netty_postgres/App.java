package ru.sber.df.epmp.netty_postgres;

import ru.sber.df.epmp.netty_postgres.server.clickhouse.http.ClickhouseHttpNettyProxyServer;
import ru.sber.df.epmp.netty_postgres.server.postgres.tcp.PostgresTcpNettyProxyServer;
import ru.sber.df.epmp.netty_postgres.utils.sql.semantic.SemanticNamesConversion;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@Slf4j
@AllArgsConstructor
@EnableConfigurationProperties
@SpringBootApplication
public class App {

    private final ClickhouseHttpNettyProxyServer clickhouseHttpNettyProxyServer;
    private final PostgresTcpNettyProxyServer postgresTcpNettyProxyServer;
    private final SemanticNamesConversion semanticNamesConversion;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    /**
     * This can not be implemented with lambda, because of the spring framework limitation
     * (<a href="https://github.com/spring-projects/spring-framework/issues/18681">...</a>)
     */
    @SuppressWarnings({"Convert2Lambda", "java:S1604"})
    @Bean
    public ApplicationListener<ApplicationReadyEvent> readyEventApplicationListener() {
        return new ApplicationListener<ApplicationReadyEvent>() {
            @Override
            public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
//                clickhouseHttpNettyProxyServer.start();
                postgresTcpNettyProxyServer.start();
                log.info("Semantic conversions: {}", semanticNamesConversion.getConversions());
            }
        };
    }
}
