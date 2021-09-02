/**
 * 
 */
package adn.security;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import adn.application.Constants;
import adn.helpers.StringHelper;
import adn.model.entities.metadata._Product;
import adn.security.context.OnMemoryUserContext;
import adn.security.jwt.JwtRequestFilter;
import adn.security.jwt.JwtUsernamePasswordAuthenticationFilter;
import adn.service.services.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
@ComponentScan(basePackages = { Constants.ROOT_PACKAGE })
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment env;

	@Autowired
	private AuthenticationService authService;

	@Autowired
	@Qualifier(UserDetailsServiceImpl.NAME)
	private UserDetailsService userDetailsService;

	@Autowired
	private JwtRequestFilter jwtFilter;

	@Autowired
	private SimpleJwtLogoutFilter jwtLogoutFilter;

	@Autowired
	private OnMemoryUserContext onMemUserContext;

	public static final String TESTUNIT_PREFIX = "/testunit";
	// @formatter:off
	private static final String[] PUBLIC_ENDPOINTS = {
			"/account/photo\\GET",
			"/product/image/**\\GET",
			"/rest/product/category/all\\GET",
			"/rest/product\\GET",
			String.format("/rest/product/{productId:^[A-Z0-9-]{%d}$}\\GET", _Product.ID_LENGTH)
	};
	// @formatter:on
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// TODO Auto-generated method stub
		auth.userDetailsService(userDetailsService);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// TODO Auto-generated method stub
		// @formatter:off
		http
			.csrf().disable()
			.cors();
		
		for (String endpoint: PUBLIC_ENDPOINTS) {
			String[] parts = endpoint.split("\\\\");
			
			if (parts.length == 0) {
				continue;
			}

			if (parts.length == 1) {
				http.authorizeRequests()
					.antMatchers(parts[0]).permitAll();
				continue;
			}

			String[] methods = parts[1].split('[' + StringHelper.WHITESPACE_CHARS + ']');
			
			for (String method: methods) {
				HttpMethod httpMethod = HttpMethod.resolve(method);
				
				if (httpMethod == null) {
					continue;
				}

				http.authorizeRequests()
					.antMatchers(httpMethod, parts[0]).permitAll();
			}
		}
		
		if (!env.getProperty("spring.profiles.active").equals("PROD")) {
			http.authorizeRequests().antMatchers(TESTUNIT_PREFIX + "/**").permitAll();
			logger.debug("Publishing " + TESTUNIT_PREFIX + " endpoints");
		}

		http
			.authorizeRequests()
			.antMatchers(HttpMethod.POST, "/auth/token").permitAll()
			.anyRequest().authenticated()
		.and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(jwtLogoutFilter, JwtRequestFilter.class)
			.exceptionHandling()
				.authenticationEntryPoint(new AuthenticationFailureHandler());
		// @formatter:on
	}

	@Bean
	@Override
	protected AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowCredentials(true);
		configuration.setAllowedOrigins(Arrays.asList("http://192.168.100.10:3000"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(
				Arrays.asList("authorization", "content-type", "x-auth-token", "Access-Control-Allow-Credentials"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	@Bean
	public AbstractAuthenticationProcessingFilter jwtUsernamePasswordAuthenticationFilter() throws Exception {
		AbstractAuthenticationProcessingFilter jwtAuthFilter = new JwtUsernamePasswordAuthenticationFilter(authService,
				onMemUserContext);

		jwtAuthFilter.setAuthenticationManager(authenticationManager());

		return jwtAuthFilter;
	}

}
