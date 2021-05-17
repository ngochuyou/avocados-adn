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

import javax.transaction.Transactional;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.internal.EntityState;
import org.hibernate.internal.SessionImpl;
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

import adn.application.Constants;
import adn.application.WebConfiguration;
import adn.application.context.ContextProvider;
import adn.application.context.DatabaseInitializer;
import adn.model.ModelManager;
import adn.model.entities.Account;
import adn.model.entities.Admin;
import adn.security.SecurityConfiguration;

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
		File directory = new File(Constants.IMAGE_FILE_DIRECTORY);
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

	@Autowired
	private ModelManager modelManager;

	@Test
	private void testInstantiateEntity() {
		String id = "ngochuy.ou";
		Admin instance = modelManager.instantiate(Admin.class, id);

		assertThat(instance != null).isTrue();
		assertThat(instance.getId()).isEqualTo(id);
	}

	@Autowired
	private SessionFactory factory;

	@Autowired
	private DatabaseInitializer dbInit;

	@Test
	@Transactional
	private void entityEntryTest() {
		SessionImpl session = (SessionImpl) factory.getCurrentSession();

		session.setHibernateFlushMode(FlushMode.MANUAL);

//		Account account = session.find(Account.class, "adn.personnel.manager.0");

		Account account = dbInit.getAdmin();
		session.persist(account);

		EntityEntry entry = session.getPersistenceContext().getEntry(account);
		EntityState state = EntityState.getEntityState(account, entry.getEntityName(), entry, session, null);

		assertThat(state == EntityState.PERSISTENT);
		logger.debug("Status: " + entry.getStatus().toString());
		logger.debug("State: " + state.toString());
		inspectState(entry);
		logger.debug("Version: " + entry.getVersion());
		logger.debug("Exists: " + entry.isExistsInDatabase());
		session.delete(account);
		state = EntityState.getEntityState(account, entry.getEntityName(), entry, session, null);

		assertThat(entry.getStatus() == Status.DELETED && state == EntityState.DELETED);
		logger.debug("Status: " + entry.getStatus().toString());
		logger.debug("State: " + state.toString());
//		inspectState(entry);
		logger.debug("Version: " + entry.getVersion());
		logger.debug("Exists: " + entry.isExistsInDatabase());

		session.flush();

		assertThat(entry.getStatus() == Status.GONE);
		assertThat(state == EntityState.TRANSIENT);
		logger.debug("Status: " + entry.getStatus().toString());
		logger.debug("State: " + state.toString());
//		inspectState(entry);
		logger.debug("Version: " + entry.getVersion());
		logger.debug("Exists: " + entry.isExistsInDatabase());
	}

	private void inspectState(EntityEntry entry) {
		if (entry.getLoadedState() != null) {
			logger.debug("Loaded state");

			for (Object val : entry.getLoadedState()) {
				if (val == null) {
					logger.debug("NULL");
					continue;
				}

				logger.debug(val.toString());
			}
		}

		if (entry.getDeletedState() != null) {
			logger.debug("Deleted state");

			for (Object val : entry.getDeletedState()) {
				if (val == null) {
					logger.debug("NULL");
					continue;
				}

				logger.debug(val.toString());
			}
		}
	}

	@Test
	public void sessionLoadTest() throws Exception {
		MockHttpServletRequestBuilder reqBuilder = MockMvcRequestBuilders
				.get(PREFIX + "/file/public/image/session-load");

		mock.perform(reqBuilder).andExpect(status().isOk()).andDo(result -> {
			logger.debug(result.getResponse().getContentAsString());
		});
	}

}
