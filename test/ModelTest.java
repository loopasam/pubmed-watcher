import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class ModelTest extends UnitTest {

    @Test
    public void createAndRetrieveUser() {
    	new User("bob@gmail.com").save();
    	User bob = User.find("byEmail", "bob@gmail.com").first();
        assertNotNull(bob);
        assertEquals("bob@gmail.com", bob.email);
    }

}
