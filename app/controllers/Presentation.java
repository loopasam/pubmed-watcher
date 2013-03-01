package controllers;

import java.util.Date;

import play.libs.Crypto;
import play.mvc.Controller;
import play.mvc.Http;

public class Presentation extends Controller {

	public static void index() {

		Http.Cookie remember = request.cookies.get("pubmedwatcher");
		if(remember != null) {
			System.out.println("coockie is not null on home page");
			//The user is known --> login
			try {
				Security.login();
			} catch (Throwable exception) {
				render();
			}
		}

		render();
	}

}
