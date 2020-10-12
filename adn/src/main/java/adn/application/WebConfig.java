/**
 * 
 */
package adn.application;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.catalina.Context;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.hibernate.SessionFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.fasterxml.jackson.databind.ObjectMapper;

import adn.utilities.Role;

/**
 * @author Ngoc Huy
 *
 */
@Configuration
@EnableWebMvc
@EnableTransactionManagement
public class WebConfig implements WebMvcConfigurer {

	@Bean
	public InternalResourceViewResolver viewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();

		viewResolver.setViewClass(JstlView.class);
		viewResolver.setPrefix("/WEB-INF/view/");
		viewResolver.setSuffix(".jsp");

		return viewResolver;
	}

	@Bean
	public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
		LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();

		sessionFactory.setDataSource(dataSource);
		sessionFactory.setPackagesToScan(new String[] { Constants.entityPackage });

		Properties properties = new Properties();

		properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
		properties.put("hibernate.show_sql", true);
		properties.put("hibernate.format_sql", true);
		properties.put("hibernate.id.new_generator_mappings", "false");
		properties.put("hibernate.hbm2ddl.auto", "update");
		properties.put("hibernate.flush_mode", "MANUAL");
		sessionFactory.setHibernateProperties(properties);

		return sessionFactory;
	}

	@Bean
	public HibernateTransactionManager transactionManager(SessionFactory s) {
		HibernateTransactionManager txManager = new HibernateTransactionManager();

		txManager.setSessionFactory(s);

		return txManager;
	}

	@Bean(name = "dataSource")
	public DataSource getDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();

		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUrl(
				"jdbc:mysql://localhost:3306/adn?serverTimezone=UTC&useUnicode=yes&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&jdbcCompliantTruncation=false");
		dataSource.setUsername("root");
		dataSource.setPassword("root");

		return dataSource;
	}

	@Bean
	ObjectMapper objectMapper() {

		return new ObjectMapper();
	}

	@Bean
	WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
		return new WebServerFactoryCustomizer<TomcatServletWebServerFactory>() {

			@Override
			public void customize(TomcatServletWebServerFactory tomcatServletWebServerFactory) {
				tomcatServletWebServerFactory.addContextCustomizers(new TomcatContextCustomizer() {
					@Override
					public void customize(Context context) {
						context.setCookieProcessor(new LegacyCookieProcessor());
					}
				});
			}
		};
	}

	@Bean
	Converter<String, Role> roleStringConverter() {
		return new Converter<String, Role>() {

			@Override
			public Role convert(String source) {
				// TODO Auto-generated method stub
				try {
					return Role.valueOf(source);
				} catch (Exception e) {
					throw new EnumConstantNotPresentException(Role.class, source);
				}
			}
			
		};
	}

}
