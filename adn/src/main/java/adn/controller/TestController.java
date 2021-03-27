/**
 * 
 */
package adn.controller;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import adn.security.SecurityConfiguration;

/**
 * @author Ngoc Huy
 *
 */
@Controller
@RequestMapping(SecurityConfiguration.TESTUNIT_PREFIX)
public class TestController extends BaseController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FileController fileController;

	private class TaskWithSingleArgumentAndReducer<T> implements Runnable {

		private final T arg;

		private final Consumer<ResponseEntity<?>> reducer;

		private final BiConsumer<T, Consumer<ResponseEntity<?>>> consumer;

		/**
		 * 
		 */
		public TaskWithSingleArgumentAndReducer(T arg, Consumer<ResponseEntity<?>> reducer,
				BiConsumer<T, Consumer<ResponseEntity<?>>> consumer) {
			// TODO Auto-generated constructor stub
			this.arg = arg;
			this.consumer = consumer;
			this.reducer = reducer;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			consumer.accept(arg, reducer);
		}

	}

	// @formatter:off
	@GetMapping("/multithreading/file/public/image/bytes")
	public @ResponseBody ResponseEntity<?> testGetImageBytes(
			@RequestParam(name = "filenames", required = true) String[] filenames,
			@RequestParam(name = "amount", required = true) int amount) throws JsonMappingException, JsonProcessingException {
		ThreadPoolTaskExecutor executorService = new ThreadPoolTaskExecutor();
		
		executorService.setCorePoolSize(amount);
		executorService.initialize();
		
		Set<String> messages = new HashSet<>();

		for (int i = 0; i < amount; i++) {
			executorService.submit(new TaskWithSingleArgumentAndReducer<String>(
				filenames[i],
				(res) -> {
					messages.add(isOk(res) ? "SUCCESS: " + res.getStatusCodeValue() : "FAILED: " + res.getStatusCodeValue());
				},
				(passedFilename, consumer) -> {
					logger.trace("Executing task testGetImageBytes(), thread: " + Thread.currentThread().getName());
	
					ResponseEntity<?> res = fileController.getImageBytes(passedFilename);

					consumer.accept(res);
			}));
		}
		// @formatter:on
		return ResponseEntity.ok(messages);
	}

	private boolean isOk(ResponseEntity<?> res) {

		return res != null && res.getStatusCode() == HttpStatus.OK;
	}

}
