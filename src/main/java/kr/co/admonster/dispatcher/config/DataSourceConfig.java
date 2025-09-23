package kr.co.admonster.dispatcher.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig extends DataSourceProperty {
	
	@Bean(DATA_SOURCE_PROPERTIES) // dataSourceProperties
	@ConfigurationProperties(prefix = DATA_SOURCE_PROPERTIES_PREFIX) // spring.datasource
	@Primary
	DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties();
	}
	
	@Bean(value = DATA_SOURCE, destroyMethod = "close") // dataSource
	@ConfigurationProperties(prefix = DATA_SOURCE_PREFIX) // spring.datasource.hikari
	@Primary
	DataSource dataSource(
			@Qualifier(DATA_SOURCE_PROPERTIES) DataSourceProperties props) { // dataSourceProperties
		return props.initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
	}
	
	@Bean(JDBC_TEMPLATE) // jdbcTemplate
	@Primary
	JdbcTemplate jdbcTemplate(
			@Qualifier(DATA_SOURCE) DataSource ds) { // dataSource
		return new JdbcTemplate(ds);
	}
	
	@Bean(NAMED_PARAMETER_JDBC_TEMPLATE) // namedParameterJdbcTemplate
	@Primary
	NamedParameterJdbcTemplate namedParameterJdbcTemplate(
			@Qualifier(DATA_SOURCE) DataSource ds) { // dataSource
		return new NamedParameterJdbcTemplate(ds);
	}
	
	@Bean(TRANSACTION_MANAGER) // transactionManager
	@Primary
	PlatformTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
	
}
