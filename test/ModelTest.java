import org.junit.*;
import java.util.*;

import play.db.jpa.JPABase;
import play.test.*;
import models.*;

public class ModelTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
	}

	@Test
	public void createAndRetrieveUser() {
		new User("bob@gmail.com").save();
		User bob = User.find("byEmail", "bob@gmail.com").first();
		assertNotNull(bob);
		assertEquals("bob@gmail.com", bob.email);
	}

	@Test
	public void useRelatedArticleRelation() {
		User bob = new User("bob@gmail.com").save();
		bob.addRelatedArticle(1234, 90, 45);
		bob.addRelatedArticle(12345, 80, 56);
		assertEquals(2, bob.relatedArticles.size());
		
		assertEquals(1, User.count());
		assertEquals(2, RelatedArticle.count());

		List<RelatedArticle> relatedArticles = RelatedArticle.find("byUser", bob).fetch();

		assertEquals(2, relatedArticles.size());
		assertEquals(2, bob.relatedArticles.size());
		
		assertEquals(1234, bob.relatedArticles.get(0).pmid);

		bob.delete();

		assertEquals(0, User.count());
		assertEquals(0, RelatedArticle.count());
	}
	
	@Test
	public void markAsRead() {
		User bob = new User("bob@gmail.com").save();
		bob.addRelatedArticle(1234, 90, 34);
		bob.addRelatedArticle(12345, 80, 56);
		assertEquals(2, bob.relatedArticles.size());
		assertEquals(0, bob.readArticlePmids.size());
		bob.markAsRead(bob.relatedArticles.get(0).id);
		assertEquals(1, bob.readArticlePmids.size());
		assertEquals(1, bob.relatedArticles.size());
		
		List<RelatedArticle> relatedArticles = RelatedArticle.findAll();
		assertEquals(1, relatedArticles.size());
	}
	
	@Test
	public void addKeyArticle(){
		User bob = new User("bob@gmail.com").save();
		bob.addKeyArticle(1234);
		assertEquals(1, bob.keyArticles.size());
		
		User joe = new User("joe@gmail.com").save();
		joe.addKeyArticle(4321);
		joe.addKeyArticle(1234);
		assertEquals(2, joe.keyArticles.size());
		
		List<RelatedArticle> keyArticles = KeyArticle.findAll();
		assertEquals(3, keyArticles.size());
	}
	
	@Test
	public void removeKeyArticle() {
		User bob = new User("bob@gmail.com").save();
		bob.addKeyArticle(1234);
		
		User joe = new User("joe@gmail.com").save();
		joe.addKeyArticle(4321);
		joe.addKeyArticle(1234);
		
		assertEquals(3, KeyArticle.count());
		
		joe.removeKeyArticle(joe.keyArticles.get(0).id);
		assertEquals(1, joe.keyArticles.size());
		
		joe.removeKeyArticle(joe.keyArticles.get(0).id);
		assertEquals(0, joe.keyArticles.size());
						
		assertEquals(1, bob.keyArticles.size());
		assertEquals(1, KeyArticle.count());
	}

}
