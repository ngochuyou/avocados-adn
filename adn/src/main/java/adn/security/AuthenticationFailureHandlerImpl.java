package adn.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.Common;

@Component
public class AuthenticationFailureHandlerImpl implements AuthenticationEntryPoint,
		org.springframework.security.web.authentication.AuthenticationFailureHandler {

	/**
	 * 
	 */
	private static final String INVALID_CREDENTIALS = "Invalid credentials";

	private final ObjectMapper mapper;

	public AuthenticationFailureHandlerImpl(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		// we hide UsernameNotFoundException so the following
		// is not necessary
//		if (e instanceof UsernameNotFoundException) {
//			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//			response.getWriter().write(USERNAME_NOT_FOUND);
//			return;
//		}

		response.getWriter().write(getMessage(request, response));
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		return;
	}

	private String getMessage(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
		String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);

		if (acceptHeader.equals(MediaType.APPLICATION_JSON_VALUE)) {
			return mapper.writeValueAsString(Common.message(INVALID_CREDENTIALS));
		}

		return INVALID_CREDENTIALS;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		commence(request, response, exception);
	}

}
