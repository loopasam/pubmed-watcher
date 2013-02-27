package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

@With(Security.class)
public class Application extends Controller {

	static User connected() {
		String email = session.get("email");
		if(email != null) {
			return User.find("byEmail", email).first();
		}
		return null; 
	}

	public static void index() {
		User user = connected();
		String updated = session.get("updated");
		if(updated == null || updated == "false"){
			System.out.println("not updated yet...");
			user.updateRelatedArticles();
			session.put("updated", "true");
		}else{
			System.out.println("already updated");
		}

		render(user);
	}

}