package EventSeatExchange.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final DataSource dataSource;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            log.info("Checking database tables for EventSeatExchange...");

            // Execute schema if needed
            executeSqlScript(conn, "schema.sql");

            // Ensure password column exists on STUDENTS
            try {
                stmt.executeUpdate("ALTER TABLE STUDENTS ADD COLUMN password VARCHAR(100) DEFAULT 'student123'");
                log.info("Verified password column in STUDENTS table.");
            } catch (Exception ignored) {
                // Column already exists
            }

            // Check if STUDENTS table has records
            boolean hasData = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM STUDENTS")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasData = true;
                }
            } catch (Exception e) {
                log.warn("Could not check STUDENTS count: {}", e.getMessage());
            }

            if (!hasData) {
                log.info("Seeding initial demo data into database...");
                executeSqlScript(conn, "data.sql");
            } else {
                log.info("Database already contains data, skipping seed script.");
            }

        } catch (Exception e) {
            log.error("Database initialization notice: {}", e.getMessage());
        }
    }

    private void executeSqlScript(Connection conn, String scriptName) {
        try {
            ClassPathResource resource = new ClassPathResource(scriptName);
            if (!resource.exists()) {
                log.warn("Script {} not found on classpath", scriptName);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                 Statement stmt = conn.createStatement()) {

                StringBuilder currentStatement = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--") || line.startsWith("//")) {
                        continue;
                    }
                    currentStatement.append(line).append(" ");
                    if (line.endsWith(";")) {
                        String sql = currentStatement.toString().trim();
                        // Strip trailing semicolon for execution
                        if (sql.endsWith(";")) {
                            sql = sql.substring(0, sql.length() - 1);
                        }
                        if (!sql.isEmpty()) {
                            try {
                                stmt.execute(sql);
                            } catch (Exception ex) {
                                // Ignore duplicate key or already exists errors
                                log.debug("SQL statement note: {} -> {}", sql, ex.getMessage());
                            }
                        }
                        currentStatement.setLength(0);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not execute {}: {}", scriptName, e.getMessage());
        }
    }
}
