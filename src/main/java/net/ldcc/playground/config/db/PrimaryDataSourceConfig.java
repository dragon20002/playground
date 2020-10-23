package net.ldcc.playground.config.db;

import net.ldcc.playground.annotation.DbType;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(
//    entityManagerFactoryRef = "primaryEntityManagerFactory",
//    transactionManagerRef = "primaryTransactionManager",
//    basePackages = {"net.ldcc.playground.repo.member"})
public class PrimaryDataSourceConfig {
    private static DbType.Profile profile = DbType.Profile.PRIMARY;

    private final DefaultListableBeanFactory beanFactory;

    public PrimaryDataSourceConfig(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public static DbType.Profile getProfile() {
        return profile;
    }

    public static void setProfile(DbType.Profile profile) {
        PrimaryDataSourceConfig.profile = profile;
    }

    public DataSource getDataSource() {
        return switch (profile) {
            case PRIMARY -> beanFactory.getBean("primaryDataSource", DataSource.class);
            case SECONDARY -> beanFactory.getBean("secondaryDataSource", DataSource.class);
        };

    }

//    @Primary
//    @Bean(name = "primaryEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(EntityManagerFactoryBuilder builder) {
//        return builder.dataSource(getDataSource())
//                .packages("net.ldcc.playground.model")
//                .persistenceUnit(profile.name())
//                .build();
//    }
//
//    @Primary
//    @Bean(name = "primaryTransactionManager")
//    public PlatformTransactionManager primaryTransactionManager(EntityManagerFactoryBuilder builder) {
//        LocalContainerEntityManagerFactoryBean emf = primaryEntityManagerFactory(builder);
//        //noinspection ConstantConditions
//        return new JpaTransactionManager(emf.getObject());
//    }
//
//    @Primary
//    @Bean(name = "primaryJdbcTemplate")
//    public JdbcTemplate primaryJdbcTemplate() {
//        return new JdbcTemplate(getDataSource());
//    }
}
