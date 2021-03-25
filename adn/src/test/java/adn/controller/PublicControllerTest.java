/**
 * 
 */
package adn.controller;

import java.io.UnsupportedEncodingException;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.WebConfig;
import adn.application.context.ContextProvider;
import adn.model.models.AccountModel;
import adn.security.SecurityConfiguration;

/**
 * @author Ngoc Huy
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(PublicController.class)
@ContextConfiguration(classes = { WebConfig.class, SecurityConfiguration.class })
@WebAppConfiguration
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class PublicControllerTest {

	@Autowired
	private MockMvc mock;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	@WithUserDetails(value = "ngochuy.ou", userDetailsServiceBeanName = "applicationUserDetailsService")
	public void testGreets() throws Exception {
		RequestBuilder req = MockMvcRequestBuilders.get("/rest/account");
		MvcResult res = null;

		try {
			res = mock.perform(req).andReturn();

			logger.debug("Response status " + res.getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Assert.notNull(res, "Null response");
		Assert.isTrue(res.getResponse().getStatus() == HttpStatus.OK.value(), res.getResponse().getContentAsString());

		logger.debug(res.getResponse().getContentAsString());
	}

	protected Stream<MvcResult> plurallyPerform(String... endpoints) {
		Assert.isTrue(endpoints != null && endpoints.length == 0, "Endpoints quantity must not be empty");

		return Stream.of(endpoints).map(uri -> {
			try {
				return mock.perform(MockMvcRequestBuilders.get(uri)).andReturn();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		});
	}

	@Test
	@WithUserDetails(value = "ngochuy.ou", userDetailsServiceBeanName = "applicationUserDetailsService")
	public void plurallyPerformAndGetArray() {
		String[] usernames = plurallyPerform((resArray) -> {
			return resArray.filter(res -> res != null && res.getResponse().getStatus() == HttpStatus.OK.value())
					.map(res -> {
						final ObjectMapper mapper = ContextProvider.getApplicationContext().getBean(ObjectMapper.class);

						try {
							return mapper.readValue(res.getResponse().getContentAsString(), AccountModel.class)
									.getUsername();
						} catch (JsonProcessingException | UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						}
					}).filter(username -> username != null).toArray(String[]::new);
		}, MockMvcRequestBuilders.get("/rest/account"),
				MockMvcRequestBuilders.get("/rest/account?username=ngochuy.ou"));

		Assert.isTrue(usernames.length == 2, "Failed");
	}

	protected <T> T plurallyPerform(Function<Stream<MvcResult>, T> function, RequestBuilder... reqs) {
		return function.apply(Stream.of(reqs).map(req -> {
			try {
				return mock.perform(req).andReturn();
			} catch (Exception e) {
				return null;
			}
		}));
	}

	@Test
	public void concurrentlyPerform() {
		int amount = 20;

		for (int i = 0; i < amount; i++) {
			Thread thread = new Thread() {
				RequestBuilder req = MockMvcRequestBuilders.get("/public/greet");
				MvcResult res = null;

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						res = mock.perform(req).andReturn();

						Assert.notNull(res, "Null response");
						Assert.isTrue(res.getResponse().getStatus() == HttpStatus.OK.value(),
								res.getResponse().getContentAsString());

						logger.debug(res.getResponse().getContentAsString());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error("Thread id: " + getId() + " failed");
						e.printStackTrace();
					}

				}
			};

			thread.start();
		}
	}

}
