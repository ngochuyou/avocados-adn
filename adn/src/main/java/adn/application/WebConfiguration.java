/**
 * 
 */
package adn.application;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.catalina.Context;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@ComponentScan(basePackages = { Constants.ROOT_PACKAGE })
@Configuration
@EnableWebMvc
@EnableTransactionManagement
@EnableSpringDataWebSupport
public class WebConfiguration implements WebMvcConfigurer {

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		Hibernate5Module h5module = new Hibernate5Module();
		h5module.disable(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION);

		for (HttpMessageConverter<?> mc : converters) {
			if (mc instanceof MappingJackson2HttpMessageConverter
					|| mc instanceof MappingJackson2XmlHttpMessageConverter) {
				((AbstractJackson2HttpMessageConverter) mc).getObjectMapper().registerModule(h5module);
			}
		}
	}

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
		sessionFactory.setPackagesToScan(new String[] { Constants.ENTITY_PACKAGE });

		Properties properties = new Properties();

		properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
		properties.put("hibernate.show_sql", true);
		properties.put("hibernate.format_sql", true);
		properties.put("hibernate.id.new_generator_mappings", "false");
//		properties.put("hibernate.hbm2ddl.auto", "create-drop");
		properties.put("hibernate.hbm2ddl.auto", "update");
		properties.put("hibernate.flush_mode", "MANUAL");
		properties.put("hibernate.jdbc.batch_size", 500);
		sessionFactory.setHibernateProperties(properties);

		return sessionFactory;
	}

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public TransactionManager transactionManager(SessionFactory sessionFactory) {
		return new HibernateTransactionManager(sessionFactory);
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
	public Converter<String, Role> roleStringConverter() {
		return new Converter<String, Role>() {

			@Override
			public Role convert(String source) {
				try {
					return Role.valueOf(source);
				} catch (Exception e) {
					throw new EnumConstantNotPresentException(Role.class, source);
				}
			}

		};
	}

	@Bean
	public BeanFactoryPostProcessor beanFactoryPostProcessor() {
		return beanFactory -> beanFactory.registerScope("thread", new SimpleThreadScope());
	}

}
