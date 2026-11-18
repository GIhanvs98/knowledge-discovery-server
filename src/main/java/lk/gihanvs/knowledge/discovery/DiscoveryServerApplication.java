package lk.gihanvs.knowledge.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Service Discovery Server for the AI-Powered Enterprise Knowledge Assistant.
 * Provides service registration and discovery capabilities for all microservices in the ecosystem.
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryServerApplication.class);

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }

    /**
     * Loads key-value pairs from a local .env file into System properties if not already set.
     * Searches current working directory and parent directories.
     */
    private static void loadDotEnv() {
        Path[] candidatePaths = new Path[]{
                Path.of(".env"),
                Path.of("../.env")
        };

        for (Path path : candidatePaths) {
            if (Files.isRegularFile(path)) {
                log.info("Loading environment configuration from: {}", path.toAbsolutePath());
                try (Stream<String> lines = Files.lines(path)) {
                    lines.map(String::trim)
                            .filter(line -> !line.isEmpty() && !line.startsWith("#") && line.contains("="))
                            .forEach(line -> {
                                int delimiterIndex = line.indexOf('=');
                                String key = line.substring(0, delimiterIndex).trim();
                                String value = line.substring(delimiterIndex + 1).trim();

                                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                        (value.startsWith("'") && value.endsWith("'"))) {
                                    value = value.substring(1, value.length() - 1);
                                }

                                if (System.getProperty(key) == null && System.getenv(key) == null) {
                                    System.setProperty(key, value);
                                }
                            });
                    break;
                } catch (IOException e) {
                    log.warn("Failed to read environment file at {}: {}", path, e.getMessage());
                }
            }
        }
    }
}
