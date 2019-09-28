import com.github.thorbenkuck.netcom2.logging.Logging;

public class FormatterTest {

    public static void main(String[] args) {
        Logging logging = Logging.getDefault();
        logging.debug("This is a {} Test", "simple");
        logging.warn("This is a {} Test", "simple");
    }

}
