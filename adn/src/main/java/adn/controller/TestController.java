/**
 * 
 */
package adn.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import adn.security.SecurityConfiguration;
import adn.service.resource.ResourceManager;
import adn.service.resource.model.models.ProductImage;
import adn.service.services.GenericCRUDService;

/**
 * @author Ngoc Huy
 *
 */
@Controller
@RequestMapping(SecurityConfiguration.TESTUNIT_PREFIX)
@SuppressWarnings("all")
public class TestController extends BaseController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected class ConsumeAndReduce<T> implements Runnable {

		private final T arg;

		private final BiConsumer<ResponseEntity<?>, T> reducer;

		private final Function<T, ResponseEntity<?>> producer;

		private final CountDownLatch signal;

		private Consumer<Exception> exceptionConsumer;

		private final Timer timer;

		private volatile boolean isTimedOut = false;

		// @formatter:off
		public ConsumeAndReduce(
				T arg,
				BiConsumer<ResponseEntity<?>, T> reducer,
				Function<T, ResponseEntity<?>> consumer,
				Consumer<Exception> exceptionConsumer,
				CountDownLatch signal,
				long timeout) {
		// @formatter:on
			this.arg = arg;
			this.producer = consumer;
			this.reducer = reducer;
			this.exceptionConsumer = exceptionConsumer;
			this.signal = signal;
			this.timer = new Timer();
			this.timer.schedule(new TimerTask() {
				@Override
				public void run() {
					isTimedOut = true;
				}
			}, timeout);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				ResponseEntity<?> res = producer.apply(arg);

				if (!isTimedOut) {
					reducer.accept(res, arg);
				}

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

	public static final String MULTITHREADING_PREFIX = "/multithreading";

	// @formatter:off
	@GetMapping(MULTITHREADING_PREFIX + "/file/public/image/bytes")
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
					passedFilename -> {
						logger.trace(
							String.format("\n\tReading image bytes, filename: %s, thread: %s",
								passedFilename,
								getCurrentThreadName()
							)
						);
						
						ResponseEntity<?> res = null;
						
						logger.debug(String.format(
							"\n\tRequest in thread %s was fulfilled"
								+ "\n\t\t-status code: %s",
							getCurrentThreadName(),
							"200"
						));

						return res;
					},
					(ex) -> {
						logger.error(String.format("\n\tA thread has thrown an Exception: " + Thread.currentThread()
										+ "\n\t-Error type: %s"
										+ "\n\t-Error message: %s",
							ex.getClass(), ex.getMessage())
						);
					}, doneSignal, TimeUnit.SECONDS.toMillis(5)
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

	protected String getCurrentThreadName() {

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

	@Autowired
	private ResourceManager session;

	private String filename = "1623406220771_12d4fc19efc1899e0731cd4d7e67f66daec3c271105cc0eb0ed6757f94822615.jpg";

	@GetMapping("/file/public/image/session-load")
	public @ResponseBody ResponseEntity<?> testGetImageBytes() throws IOException, InterruptedException {
		ProductImage image1 = session.get(ProductImage.class, "1626973200_TzvUg0KR2X.jpg");
		ProductImage image2 = session.get(ProductImage.class, "1626973200_fF55Fwt693.jpg");

		session.delete(image1);
		session.delete(image2);
		session.flush();

		return image1 != null ? ResponseEntity.ok(image1.getExtension())
				: ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("File [%s] not found", filename));
	}

	@Autowired
	private A a;

	@GetMapping("/multi")
	public ResponseEntity<String> multi() throws InterruptedException {
		Future<String> getA = a.get();
		Future<String> getB = a.get();

		String a = "";
		String b = "";

		try {
			a = getA.get(3, TimeUnit.SECONDS);
			b = getB.get(3, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok(a + b);
	}

	@Component
	public class A {
		@Async(GenericCRUDService.EXECUTOR_NAME)
		public Future<String> get() {
			try {
				System.out.println("starting " + Thread.currentThread().getName());
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new AsyncResult(Thread.currentThread().getName());
		}
	}

	private byte[] getDummyBytes() throws IOException {
		return Files.readAllBytes(
				Paths.get("C:\\Users\\Ngoc Huy\\Pictures\\Saved Pictures\\alesia-kazantceva-XLm6-fPwK5Q-unsplash.jpg"));
	}

}
