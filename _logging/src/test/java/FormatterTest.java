import com.github.thorbenkuck.netcom2.logging.Logging;

import java.util.concurrent.TimeUnit;

public class FormatterTest {

    public static void main(String[] args) {
        Logging logging = Logging.getDefault();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            logging.warn("This is a {} Test", "simple");
        }
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("It took " + elapsed + "ms");
        System.out.println("It took " + TimeUnit.MILLISECONDS.toSeconds(elapsed) + "s");
    }

}
