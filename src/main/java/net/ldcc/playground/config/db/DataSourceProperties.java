package net.ldcc.playground.config.db;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties
public class DataSourceProperties {
    public static final String PRIMARY = "primaryDataSource";
    public static final String SECONDARY = "secondaryDataSource";

    @Bean(name = PRIMARY)
    @Qualifier(PRIMARY)
    @Primary //주 DataSource로 사용
    @ConfigurationProperties(prefix = "spring.datasource.hikari.primary") //properties를 가져옴
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = SECONDARY)
    @Qualifier(SECONDARY)
    @ConfigurationProperties(prefix = "spring.datasource.hikari.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

}
