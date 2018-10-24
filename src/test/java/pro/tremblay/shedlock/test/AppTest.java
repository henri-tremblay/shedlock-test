package pro.tremblay.shedlock.test;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.SimpleLock;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.support.StorageBasedLockProvider;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

import java.time.Instant;
import java.util.Optional;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.*;

public class AppTest {

    @Test
    public void test() {
        HikariDataSource datasource = new HikariDataSource();
        datasource.setJdbcUrl("jdbc:h2:mem:hardbacon;DB_CLOSE_DELAY=-1");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
        jdbcTemplate.execute("CREATE TABLE shedlock(name VARCHAR(64), lock_until TIMESTAMP, locked_at TIMESTAMP, locked_by  VARCHAR(255), PRIMARY KEY (name))");

        StorageBasedLockProvider provider = new JdbcTemplateLockProvider(jdbcTemplate);

        LockConfiguration configuration = new LockConfiguration("test", Instant.MAX);
        Optional<SimpleLock> lock = provider.lock(configuration);
        assertThat(lock).isPresent();

        provider.clearCache();

        try {
            Optional<SimpleLock> lockAgain = provider.lock(configuration);
            assertThat(lockAgain).isNotPresent();
        }
        finally {
            lock.get().unlock();
        }

        jdbcTemplate.execute("DROP TABLE shedlock");
        datasource.close();
    }
}
