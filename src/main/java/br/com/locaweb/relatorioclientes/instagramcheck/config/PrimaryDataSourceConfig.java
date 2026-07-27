package br.com.locaweb.relatorioclientes.instagramcheck.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Ao existir um segundo DataSource (o do módulo "instagramcheck"), o Spring Boot
 * para de criar automaticamente o DataSource/EntityManagerFactory/TransactionManager
 * padrão do sistema (regra @ConditionalOnMissingBean). Esta classe recria, de forma
 * explícita, os MESMOS beans principais que já existiam — mesmas propriedades já
 * presentes no application.properties (spring.datasource.*, spring.jpa.*), sem
 * nenhum valor novo — só que marcados como @Primary e com os nomes que o resto do
 * sistema já espera ("dataSource", "entityManagerFactory", "transactionManager").
 * Resultado: o banco principal (controledb) e todas as entidades/repositories
 * originais continuam funcionando exatamente como antes; nada muda para o
 * restante do sistema.
 */
@Configuration
public class PrimaryDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dataSource") DataSource dataSource) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        props.put("hibernate.jdbc.time_zone", "UTC");
        return builder
                .dataSource(dataSource)
                .packages("br.com.locaweb.relatorioclientes.model")
                .persistenceUnit("primary")
                .properties(props)
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}
