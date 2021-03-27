/**
 * 
 */
package adn.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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
import adn.service.services.AuthenticationService;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@ComponentScan(basePackages = { Constants.rootPackage })
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthenticationService authService;

	@Autowired
	@Qualifier("applicationUserDetailsService")
	private UserDetailsService userDetailsService;

	@Autowired
	private JwtRequestFilter jwtFilter;

	@Autowired
	private SimpleJwtLogoutFilter jwtLogoutFilter;

	public static final String TESTUNIT_PREFIX = "/testunit";
	// @formatter:off
	private static final String[] PUBLIC_ENDPOINTS = {
			"/account/photo\\GET",
			"public/**",
			TESTUNIT_PREFIX + "/**"
	};
	// @formatter:on
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// TODO Auto-generated method stub
		// @formatter:off
		auth.userDetailsService(userDetailsService);
		// @formatter:on
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

			String[] methods = parts[1].split('[' + Strings.WHITESPACE_CHARS + ']');
			
			for (String method: methods) {
				HttpMethod httpMethod = HttpMethod.resolve(method);
				
				if (httpMethod == null) {
					continue;
				}

				http.authorizeRequests()
					.antMatchers(httpMethod, parts[0]).permitAll();
			}
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
		// TODO Auto-generated method stub
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
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(
				Arrays.asList("authorization", "content-type", "x-auth-token", "Access-Control-Allow-Credentials"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	@Bean
	public AbstractAuthenticationProcessingFilter jwtUsernamePasswordAuthenticationFilter() throws Exception {
		AbstractAuthenticationProcessingFilter jwtAuthFilter = new JwtUsernamePasswordAuthenticationFilter(authService);

		jwtAuthFilter.setAuthenticationManager(authenticationManager());

		return jwtAuthFilter;
	}

}
