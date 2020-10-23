package net.ldcc.playground.dao;

import net.ldcc.playground.annotation.DbType;
import net.ldcc.playground.config.db.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public abstract class BaseDao {
    private final ApplicationContext context;
    protected final JdbcTemplate jdbcTemplate;
    private DbType.Profile profile = DbType.Profile.PRIMARY;

    public BaseDao(ApplicationContext context, JdbcTemplate jdbcTemplate) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
    }

    public DbType.Profile getProfile() {
        return profile;
    }

    public void setDataSource(DbType.Profile profile) {
        if (profile == null)
            return;

        this.profile = profile;

        DataSource dataSource = switch (profile) {
            case PRIMARY -> (DataSource) context.getBean(DataSourceProperties.PRIMARY);
            case SECONDARY -> (DataSource) context.getBean(DataSourceProperties.SECONDARY);
        };

        jdbcTemplate.setDataSource(dataSource);
    }

}
