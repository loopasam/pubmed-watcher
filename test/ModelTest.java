import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class ModelTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void createAndRetrieveUser() {
		new User("bob@gmail.com").save();
		User bob = User.find("byEmail", "bob@gmail.com").first();
		assertNotNull(bob);
		assertEquals("bob@gmail.com", bob.email);
	}

	@Test
	public void createRelatedArticle() {
		User bob = new User("bob@gmail.com").save();
		new RelatedArticle(bob, 20210808, 1009).save();
		assertEquals(1, RelatedArticle.count());
		List<RelatedArticle> bobRelatedArticles = RelatedArticle.find("byUser", bob).fetch();

		assertEquals(1, bobRelatedArticles.size());
		RelatedArticle relatedArticle = bobRelatedArticles.get(0);
		assertNotNull(relatedArticle);
		assertEquals(bob, relatedArticle.user);
		assertEquals(20210808, relatedArticle.pmid);
		assertEquals(1009, relatedArticle.similarity);
	}

	@Test
	public void useRelatedArticleRelation() {
		User bob = new User("bob@gmail.com").save();
		RelatedArticle relatedArticle = new RelatedArticle(bob, 1234, 90).save();
		bob.addRelatedArticle(12345, 80);
		
		assertEquals(1, User.count());
		assertEquals(2, RelatedArticle.count());
		
		//TODO stuck here

		relatedArticles = RelatedArticle.find("byUser", bob);
		assertNotNull(bobPost);

		// Navigate to comments
		assertEquals(2, bobPost.comments.size());
		assertEquals("Jeff", bobPost.comments.get(0).author);

		// Delete the post
		bobPost.delete();

		// Check that all comments have been deleted
		assertEquals(1, User.count());
		assertEquals(0, Post.count());
		assertEquals(0, Comment.count());
	}

}
