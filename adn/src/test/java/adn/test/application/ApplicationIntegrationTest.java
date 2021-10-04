/**
 * 
 */
package adn.test.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.query.criteria.internal.path.AbstractPathImpl;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.WebConfiguration;
import adn.application.context.ContextProvider;
import adn.model.entities.Product;
import adn.model.entities.metadata._Product;
import adn.security.SecurityConfiguration;
import adn.service.resource.model.models.UserPhoto;

/**
 * @author Ngoc Huy
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = ApplicationIntegrationTest.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = { WebConfiguration.class, SecurityConfiguration.class })
@AutoConfigureMockMvc
public class ApplicationIntegrationTest {

	public static final String PREFIX = "/testunit";

	public static final String MULTITHREADING_ENDPOINT = PREFIX + "/multithreading";

	@Autowired
	private MockMvc mock;

	private final UserRequestPostProcessor ADMIN = user("ngochuy.ou").roles("ADMIN");

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	private void testGetImageByte() throws Exception {
		MockHttpServletRequestBuilder reqBuilder = MockMvcRequestBuilders.get("/file/public/image/bytes");
		reqBuilder.queryParam("filename", "IMG_20210301_162741.jpg").with(ADMIN);

		mock.perform(reqBuilder).andExpect(status().isOk()).andDo(result -> {
			logger.debug(result.getResponse().getContentAsString());
		});
	}

	@Test
	private void testGetImageBytes() throws Exception {
		File directory = new File(UserPhoto.DIRECTORY);
		MockHttpServletRequestBuilder reqBuilder = MockMvcRequestBuilders
				.get(MULTITHREADING_ENDPOINT + "/file/public/image/bytes");
		int amount = directory.listFiles().length;

		for (File f : directory.listFiles()) {
			reqBuilder.queryParam("filenames", f.getName());
		}

		reqBuilder.queryParam("amount", String.valueOf(amount));

		MockHttpServletRequest req = reqBuilder.buildRequest(mock.getDispatcherServlet().getServletContext());

		logger.debug(Arrays.asList(req.getParameterValues("filenames")).stream().collect(Collectors.joining(", ")));
		// @formatter:off
		mock.perform(reqBuilder)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(result -> {
				MockHttpServletResponse res = result.getResponse();
	
				logger.debug(res.getContentAsString());
				
				ObjectMapper mapper = ContextProvider.getApplicationContext().getBean(ObjectMapper.class);
				String[] statusSet = mapper.readValue(res.getContentAsString(), String[].class);
				
				assertThat(
					Stream
						.of(statusSet)
						.map(status -> { System.out.println(status); return status; })
						.filter(status -> status.startsWith(HttpStatus.OK.toString()))
						.count() == amount
				).isTrue();
				
				logger.trace("Successfully retrieved " + statusSet.length + " files");
			});
		// @formatter:on
	}

//	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	@Transactional
	public void test() {
		Session session = ContextProvider.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<Product> root = query.from(Product.class);

		AbstractPathImpl<?> path = (AbstractPathImpl<?>) root.get("approvalInformations").get(_Product.approvedBy);

		while (path != null) {
			System.out.println(path.getAttribute().getName());
			path = (AbstractPathImpl<?>) path.getParentPath();
		}
		
		System.out.println("out");
	}

}
