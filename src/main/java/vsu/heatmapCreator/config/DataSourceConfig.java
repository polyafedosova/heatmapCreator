package vsu.heatmapCreator.config;


import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.postgresql")
    public DataSourceProperties postgresqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.postgresql.configuration")
    public HikariDataSource postgresqlDataSource(DataSourceProperties postgresqlDataSourceProperties) {
        return postgresqlDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.clickhouse")
    public DataSourceProperties clickhouseDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.clickhouse.configuration")
    public BasicDataSource clickhouseDataSource(@Qualifier("clickhouseDataSourceProperties") DataSourceProperties clickhouseDataSourceProperties) {
        return clickhouseDataSourceProperties.initializeDataSourceBuilder().type(BasicDataSource.class).build();
    }

}


