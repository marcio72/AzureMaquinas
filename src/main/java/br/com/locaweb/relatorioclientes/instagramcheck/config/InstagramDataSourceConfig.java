package br.com.locaweb.relatorioclientes.instagramcheck.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Banco de dados SEPARADO, exclusivo do módulo "Checagem de Perfis Instagram".
 * Não interfere no datasource principal (controledb) usado pelo resto do sistema:
 * este módulo tem seu próprio DataSource, EntityManagerFactory e TransactionManager.
 *
 * Credenciais em application.properties, prefixo "app.datasource.instagram.*".
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "br.com.locaweb.relatorioclientes.instagramcheck.repository",
        entityManagerFactoryRef = "instagramEntityManagerFactory",
        transactionManagerRef = "instagramTransactionManager"
)
public class InstagramDataSourceConfig {

    @Bean
    @ConfigurationProperties("app.datasource.instagram")
    public DataSourceProperties instagramDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource instagramDataSource() {
        return instagramDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean instagramEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", true);
        return builder
                .dataSource(instagramDataSource())
                .packages("br.com.locaweb.relatorioclientes.instagramcheck.model")
                .persistenceUnit("instagram")
                .properties(props)
                .build();
    }

    @Bean
    public PlatformTransactionManager instagramTransactionManager(
            @Qualifier("instagramEntityManagerFactory") LocalContainerEntityManagerFactoryBean instagramEntityManagerFactory) {
        return new JpaTransactionManager(instagramEntityManagerFactory.getObject());
    }
}
