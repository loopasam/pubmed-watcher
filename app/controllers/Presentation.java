package controllers;

import java.util.Date;

import play.libs.Crypto;
import play.mvc.Controller;
import play.mvc.Http;

public class Presentation extends Controller {

	public static void index() {
		Http.Cookie remember = request.cookies.get("pubmedwatcher");
		if(remember != null) {
			//The user is known --> login
			try {
				Security.login();
			} catch (Throwable exception) {
				render();
			}
		}

		render();
	}

	public static void about() {
		String email = null;
		if(Application.connected() != null){
			email = Application.connected().email;
		}
		render(email);
	}


}
