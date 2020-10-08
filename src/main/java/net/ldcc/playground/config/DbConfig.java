package net.ldcc.playground.config;

import com.zaxxer.hikari.HikariDataSource;
import net.ldcc.playground.annotation.DbType;
import net.ldcc.playground.annotation.DbType.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;

/**
 * Database 연동을 위한 data-source Bean을 설정 및 생성합니다.
 *
 * @implNote
 * DbType Annotation에서 'profile' 속성을 설정하여 data-source를 변경할 수 있습니다.<br><br>
 *
 * <b>피드백</b><br>
 * Annotation이 적용된 Controller/Service/Repository Class나 Method 실행 시에만
 * 다른 DB를 사용할 수 있도록 해볼 것.<br><br>
 *
 * <b>TODO</b><br>
 * - 다수의 data-source를 동시 연동<br>
 * - DB Transaction 전에 Interceptor나 AOP로 DbType Annotation 체크<br>
 * - ...?
 *
 * @see <a href="https://www.cubrid.org/manual/ko/9.3.0/sql/transaction.html">dirty read에 대해1</a>
 * @see <a href="https://programmer.ink/think/jdbc-transaction-isolation-level.html">dirty read에 대해2</a>
 * @see <a href="https://ilhee.tistory.com/32">dirty read에 대해3</a>
 */
@DbType(profile = Profile.DEV)
@Configuration
public class DbConfig {
    private final Logger logger = LoggerFactory.getLogger(DbConfig.class);

    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        Profile profile = Profile.PROD; //기본값 PROD

        // DbType Annotation 체크
        if (DbConfig.class.isAnnotationPresent(DbType.class)) {
            DbType dbType = DbConfig.class.getAnnotation(DbType.class);
            if (dbType != null) {
                profile = dbType.profile();
            }
        }

        // Annotation에 따라 dataSource 설정
        String url, username, password;

        if (profile == Profile.LOCAL || profile == Profile.DEV) {
            url = "jdbc:h2:~/test";
            username = "sa";
            password = "";
        } else { //PROD
            url = "jdbc:h2:~/playground";
            username = "sa";
            password = "";
        }

        // dataSource 생성 및 반환
        return DataSourceBuilder.create().type(HikariDataSource.class)
                .url(url)
                .driverClassName("org.h2.Driver")
                .username(username)
                .password(password)
                .build();
    }

}
