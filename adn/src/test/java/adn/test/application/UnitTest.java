/**
 * 
 */
package adn.test.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ngoc Huy
 *
 */
public class UnitTest {

	public static final String mark;
	public static final String letter;
	public static final String ops;

	public static Pattern SAVE_PATTERN;

	static {
		mark = "\\?,\\.''\"\\s\\t\\n><\\=\\(\\)";
		letter = "\\w\\d_";
		ops = "(\\=|like|LIKE|is|IS|>|<)";
		// @formatter:off
		String regex = String.format(""
				+ "(insert|INSERT)\\s+(into|INTO)\\s+(?<templatename>[%s]+)\\s+"
				+ "\\((?<columns>[%s]+)\\)\\s+"
				+ "(values|VALUES)\\s+\\((?<values>[%s]+)\\)\\s?",
				letter,
				letter + mark,
				letter + mark);
		// @formatter:on
		SAVE_PATTERN = Pattern.compile(regex);
	}

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException, InterruptedException, ExecutionException {
//		Pattern p = Pattern
//				.compile(String.format("^[\\p{L}\\p{N}\\s\\.\\,\\_\\-\\@\"'\\*%s]{%d,%d}$", VIETNAMESE_CHARACTERS, 0, 255));
//		Matcher m = p.matcher("White LeÌ FrÃÃnt");
//		System.out.println(VIETNAMESE_CHARACTERS.contains("Ã"));
//		System.out.println(m.matches());
//		Pattern p = Pattern.compile(String.format("^[%s\\p{L}\\p{N}\s\n\\(\\)\\._\\-\"\'\\!@#$%%^&*]{0,255}$", StringHelper.VIETNAMESE_CHARACTERS));
//		Matcher m = p.matcher("aEnglish%@^.ti*ến#g_\"việt\"-('Q$&&uốc!'\n"
//				+ "Ngữ)子及");
//		
//		System.out.println(m.matches());
//		ThreadPoolTaskExecutor executorService = new ThreadPoolTaskExecutor();
//		CountDownLatch latch = new CountDownLatch(5);
//		
//		executorService.setCorePoolSize(5);
//		executorService.initialize();
//		
//		for (int i = 0; i < 5; i++) {
//			executorService.submit(new Runnable() {
//				@Override
//				public void run() {
//					System.out.println(Thread.currentThread().getName() + " start");
//					System.out.println(Thread.currentThread().getName() + " done");
//					latch.countDown();
//				}
//			});
//		}
//		
//		try {
//			latch.await();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("done....");
//		executorService.shutdown();
//		Pattern p = Pattern.compile("^[A-Z0-9-]+$");
//		Matcher m = p.matcher("20JGU-FORMA");
//		System.out.println(m.matches());
		
	}

	public static void a(LocalDate date) {
		if (date == null || date.isAfter(LocalDate.now())) {
			System.out.println("asas");
		}
	}

	public static void x2() {
		double a = 1.0000000000000001;
		double b = a + 0.0000000000000009;

		System.out.println(b);
		System.out.println(Math.ceil(b));
	}

	public static void bd() {
		BigDecimal a = new BigDecimal("1.0000000000000001");
		BigDecimal b = a.add(new BigDecimal("0.0000000000000009"));

		System.out.println(b.toString());
		System.out.println(b.setScale(2, RoundingMode.HALF_UP));
	}

	public static void testUnicodePattern() {
		Pattern p = Pattern.compile("^[_\\p{L}\\p{N}\\.]{8,}$", Pattern.UNICODE_CHARACTER_CLASS);
		Matcher m = p.matcher("孔子及其弟asdads子故事集_真.实性有争议_");

		System.out.println(m.matches());
	}

}
