import ru.gennad.commenter.CommenterTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This file aggregates all of the Unit Tests for this component.
 *
 * @author TCSDEVELOPER
 */
public class UnitTests extends TestCase {
	/**
	 * Creates a test suite containing all Unit Tests
	 * for this component.
	 *
	 * @return A test suite containing all unit tests.
	 */
	@org.junit.Test
	public static Test suite() {
		TestSuite suite = new TestSuite();		
		suite.addTestSuite(CommenterTest.class);
		return suite;
	}
}