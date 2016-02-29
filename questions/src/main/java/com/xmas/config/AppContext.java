package com.xmas.config;

import com.xmas.entity.Answer;
import com.xmas.entity.EntityHelper;
import com.xmas.entity.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.ValidationMode;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;


@SpringBootApplication
@Configuration
@ComponentScan(basePackages = "com.xmas")
@EnableJpaRepositories(basePackages = "com.xmas.dao")
@EnableAutoConfiguration()
@Import({MVCConfiguration.class})
public class AppContext {

    @Value("${jdbc.driver}")
    private String driver;
    @Value("${jdbc.host}")
    private String host;
    @Value("${jdbc.user}")
    private String dbUser;
    @Value("${jdbc.password}")
    private String dbPassword;

    @Value("${hibernate.show_sql}")
    private String showSql;
    @Value("${hibernate.dialect}")
    private String dialect;
    @Value("${hibernate.hbm2ddl.auto}")
    private String hbm2ddl;

    @Value("${mail.host}")
    private String mailServerHost;
    @Value("${mail.port}")
    private Integer mailServerPort;
    @Value("${mail.user}")
    private String mailServerUser;
    @Value("${mail.password}")
    private String mailServerPassword;

    @Autowired
    ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(AppContext.class, args);
    }

    private Properties hibernateProperties(){
        Properties properties = new Properties();
        properties.put("hibernate.show_sql",showSql);
        properties.put("hibernate.dialect",dialect);
        properties.put("hibernate.hbm2ddl.auto",hbm2ddl);
        return properties;
    }

    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(host);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(){
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan("com/xmas/entity", "com/xmas/dao");
        entityManagerFactoryBean.setPersistenceProviderClass(org.hibernate.jpa.HibernatePersistenceProvider.class);
        entityManagerFactoryBean.setJpaProperties(hibernateProperties());
        entityManagerFactoryBean.setValidationMode(ValidationMode.NONE);
        return entityManagerFactoryBean;
    }

    @Bean
    public JpaTransactionManager transactionManager(){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    @Bean
    public static PropertyPlaceholderConfigurer propertyConfigurer() throws IOException {
        PropertyPlaceholderConfigurer props = new PropertyPlaceholderConfigurer();
        props.setLocations(new FileSystemResource(System.getProperty("user.home")+ "/.pushmessages/properties/app.properties"),
                           new FileSystemResource(System.getProperty("user.home")+ "/.pushmessages/properties/jdbc.properties"),
                           new FileSystemResource(System.getProperty("user.home")+ "/.pushmessages/properties/hibernate.properties"));
        return props;
    }

    @Bean
    public EntityHelper<Answer, Question> answerHelper(){
        return new EntityHelper<>(Answer.class, applicationContext);
    }

}
