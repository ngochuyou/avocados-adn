/**
 * 
 */
package adn.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

	protected class ConsumeAndReduce<T> implements Runnable {

		private final T arg;

		private final BiConsumer<ResponseEntity<?>, T> reducer;

		private final BiConsumer<T, BiConsumer<ResponseEntity<?>, T>> consumer;

		private final CountDownLatch signal;

		private Consumer<Exception> exceptionConsumer;

		/**
		 * 
		 */
		public ConsumeAndReduce(T arg, BiConsumer<ResponseEntity<?>, T> reducer,
				BiConsumer<T, BiConsumer<ResponseEntity<?>, T>> consumer, Consumer<Exception> exceptionConsumer,
				CountDownLatch signal) {
			// TODO Auto-generated constructor stub
			this.arg = arg;
			this.consumer = consumer;
			this.reducer = reducer;
			this.exceptionConsumer = exceptionConsumer;
			this.signal = signal;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				consumer.accept(arg, reducer);
				signal.countDown();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				if (exceptionConsumer != null) {
					exceptionConsumer.accept(e);

					return;
				}

				e.printStackTrace();
			}
		}

	}

	// @formatter:off
	@GetMapping("/multithreading/file/public/image/bytes")
	public @ResponseBody ResponseEntity<?> testGetImageBytes(
			@RequestParam(name = "filenames", required = true) String[] filenames,
			@RequestParam(name = "amount", required = true) int amount) throws JsonMappingException, JsonProcessingException, InterruptedException, ExecutionException {
		ThreadPoolTaskExecutor executorService = new ThreadPoolTaskExecutor();
		CountDownLatch doneSignal = new CountDownLatch(amount);
		
		executorService.setCorePoolSize(amount);
		executorService.initialize();

		Collection<String> messageHolder = new HashSet<>();

		for (int i = 0; i < amount; i++) {
			try {
				executorService.submit(new ConsumeAndReduce<String>(
					filenames[i],
					(res, passedFilename) -> {
						messageHolder.add(String.format("%s: %s", res.getStatusCode(), passedFilename));
					},
					(passedFilename, reducer) -> {
						logger.trace(
							String.format("\n\tReading image bytes, filename: %s, thread: %s",
								passedFilename,
								currentThreadName()
							)
						);
						
						ResponseEntity<?> res = fileController.getImageBytes(passedFilename);
						
						logger.debug(String.format(
							"\n\tRequest in thread %s was fulfilled"
								+ "\n\t\t-status code: %s",
							currentThreadName(),
							res.getStatusCode().toString()
						));
						reducer.accept(res, passedFilename);
					},
					(ex) -> {
						logger.error("\n\tA thread has thrown an Exception: " + Thread.currentThread()
										+ "\n\t-Error type: %s"
										+ "\n\t-Error message: %s",
							ex.getClass(), ex.getMessage()
						);
					}, doneSignal
				)).get(5, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException ex) {
				// TODO Auto-generated catch block
				messageHolder.add(String.format("%s: %s", HttpStatus.REQUEST_TIMEOUT, filenames[i]));
			}
		}
		// @formatter:on
		logger.info("Waiting for done signal...");
		doneSignal.await();
		logger.info("Done signal received");

		return ResponseEntity.ok(messageHolder.toArray());
	}

	private String currentThreadName() {

		return Thread.currentThread().getName();
	}

	protected class CountDownLatchPair {

		final CountDownLatch start;

		final CountDownLatch done;

		/**
		 * 
		 */
		public CountDownLatchPair(CountDownLatch start, CountDownLatch done) {
			// TODO Auto-generated constructor stub
			this.start = start;
			this.done = done;
		}

	}

}
