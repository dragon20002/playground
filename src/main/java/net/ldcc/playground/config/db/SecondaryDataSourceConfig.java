package net.ldcc.playground.config.db;

import net.ldcc.playground.annotation.DbType;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
//    entityManagerFactoryRef = "secondaryEntityManagerFactory",
//    transactionManagerRef = "secondaryTransactionManager",
//    basePackages = {"net.ldcc.playground.repo.tempmember"})
public class SecondaryDataSourceConfig {
    private static DbType.Profile profile = DbType.Profile.SECONDARY;

    private final DefaultListableBeanFactory beanFactory;

    public SecondaryDataSourceConfig(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public static DbType.Profile getProfile() {
        return profile;
    }

    public static void setProfile(DbType.Profile profile) {
        SecondaryDataSourceConfig.profile = profile;
    }

    public DataSource getDataSource() {
        return switch (profile) {
            case PRIMARY -> beanFactory.getBean("primaryDataSource", DataSource.class);
            case SECONDARY -> beanFactory.getBean("secondaryDataSource", DataSource.class);
        };

    }

//    @Bean(name = "secondaryEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(EntityManagerFactoryBuilder builder) {
//        return builder.dataSource(getDataSource())
//                .packages("net.ldcc.playground.model")
//                .persistenceUnit(profile.name())
//                .build();
//    }
//
//    @Bean(name = "secondaryTransactionManager")
//    public PlatformTransactionManager secondaryTransactionManager(EntityManagerFactoryBuilder builder) {
//        LocalContainerEntityManagerFactoryBean emf = secondaryEntityManagerFactory(builder);
//        //noinspection ConstantConditions
//        return new JpaTransactionManager(emf.getObject());
//    }
//
//    @Bean(name = "secondaryJdbcTemplate")
//    public JdbcTemplate secondaryJdbcTemplate() {
//        return new JdbcTemplate(getDataSource());
//    }
}
