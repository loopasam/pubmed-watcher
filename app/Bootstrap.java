import models.User;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job{

	public void doJob() {
		// Check if the database is empty
//		Fixtures.deleteDatabase();
//		Fixtures.loadModels("../test/data.yml");
	}
}
