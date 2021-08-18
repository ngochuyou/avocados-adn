/**
 * 
 */
package adn.application;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

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
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import adn.application.context.internal.ContextBuilder;
import adn.service.internal.Role;
import adn.service.services.GenericCRUDService;

/**
 * @author Ngoc Huy
 *
 */
@ComponentScan(basePackages = { Constants.ROOT_PACKAGE })
@Configuration
@EnableWebMvc
@EnableTransactionManagement
@EnableSpringDataWebSupport
@EnableAsync
public class WebConfiguration implements WebMvcConfigurer {

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

	@Bean(name = GenericCRUDService.EXECUTOR_NAME)
	public Executor crudServiceExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(3);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("crud-batch-execution-");
		executor.initialize();

		return executor;
	}

	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();

		jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);

		return jsonConverter;
	}

	@Bean
	public TransactionManager transactionManager(SessionFactory sessionFactory) {
		return new HibernateTransactionManager(sessionFactory);
	}

	@Bean(name = "dataSource")
	public DataSource getDataSource() throws IOException, NoSuchFieldException {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		File file = ResourceUtils.getFile(
				ContextBuilder.CONFIG_PATH + "13450a773a68d2a21a88ca081962a2f71a59730fee3a6cab5647c5674626e5fe.txt");
		List<String> $ = Files.readAllLines(file.toPath());

		if ($.size() < 1) {
			throw new IllegalStateException();
		}

		String $$ = $.get(0);
		DataSourceProperties $$$ = new DataSourceProperties();

		for (int i = 1; i < $.size(); i++) {
			String[] __ = $.get(i).split(Pattern.quote($$));

			if (__.length != 2) {
				continue;
			}

			String ___ = __[0];
			String ____ = __[1];

			try {
				$$$.getClass().getDeclaredField(___).set($$$, ____);
			} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
				e.printStackTrace();
				continue;
			}

		}

		dataSource.setDriverClassName($$$.getDriverClassName());
		dataSource.setUrl($$$.getUrl());
		dataSource.setUsername($$$.getUsername());
		dataSource.setPassword($$$.getPassword());

		return dataSource;
	}

	private class DataSourceProperties {

		private String driverClassName;
		private String url;
		private String username;
		private String password;

		public String getDriverClassName() {
			return driverClassName;
		}

		public String getUrl() {
			return url;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

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
