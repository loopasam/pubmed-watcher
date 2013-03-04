package controllers;

import java.util.Date;

import models.User;

import play.Play;
import play.libs.Crypto;
import play.libs.Time;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;

import com.google.gson.JsonObject;

public class Security extends Controller {

	@Before(unless={"login", "auth", "logout"})
	static void checkAccess() throws Throwable {
		if(!session.contains("email")) {
			login();
		}
	}

	public static void login() throws Throwable {
		Http.Cookie remember = request.cookies.get("pubmedwatcher");
		if(remember != null) {
			int firstIndex = remember.value.indexOf("-");
			int lastIndex = remember.value.lastIndexOf("-");
			if (lastIndex > firstIndex) {
				String sign = remember.value.substring(0, firstIndex);
				String restOfCookie = remember.value.substring(firstIndex + 1);
				String email = remember.value.substring(firstIndex + 1, lastIndex);
				//TODO delete this block - leave the browser doing the expiration work
//				String time = remember.value.substring(lastIndex + 1);
//				Date expirationDate = new Date(Long.parseLong(time));
//				Date now = new Date();
//				if (expirationDate == null || expirationDate.before(now)) {
//					System.out.println("coockie is expired: " + expirationDate.toGMTString());
//					logout();
//				}
				if(Crypto.sign(restOfCookie).equals(sign)) {
					System.out.println("email is put in the session...");
					session.put("email", email);
					Application.index();
				}
			}
		}

		//TODO do something nicer (link, logo, etc...) for the authorisation OAUth via google
		
		//The user is supposed to let access to it's information
		String urlGoogleOAuth = "https://accounts.google.com/o/oauth2/auth?" +
				"scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+" +
				"&redirect_uri=http%3A%2F%2F" +
				"localhost:9000%2Fauth" +
				"&response_type=code" +
				"&client_id=65272228633.apps.googleusercontent.com";
		//The response is redirected on the auth() method.
		redirect(urlGoogleOAuth);
				
	}


	public static void auth(String code) {

		//Depending on what the user entered, could be an error
		if(code == null){
			Presentation.index();
		}

		//call for access token
		HttpResponse res = WS.url("https://accounts.google.com/o/oauth2/token").
				setParameter("code", code).setParameter("client_id", "65272228633.apps.googleusercontent.com").
				setParameter("client_secret", "59-9NRRPEmjTut1392HWg3cY").setParameter("redirect_uri", "http://localhost:9000/auth").
				setParameter("grant_type", "authorization_code").post();

		JsonObject json = res.getJson().getAsJsonObject();

		if(json.get("access_token") != null){
			String accessToken = json.get("access_token").getAsString();
			HttpResponse resToken = WS.url("https://www.googleapis.com/oauth2/v1/userinfo?access_token="+accessToken).get();

			JsonObject profile = resToken.getJson().getAsJsonObject();
			if(profile.get("email") != null){
				String email = profile.get("email").getAsString().replaceAll("\\\"", "");
				session.put("email", email);
				Date expiration = new Date();
				String duration = "10d";
				expiration.setTime(expiration.getTime() + Time.parseDuration(duration));
				//TODO remove the time duration bit
				response.setCookie("pubmedwatcher", Crypto.sign(email + "-" + expiration.getTime()) + "-" + email + "-" + expiration.getTime(), duration);

				User user = User.find("byEmail", email).first();
				if(user == null){
					new User(email).save();
				}

				Application.index();
			}
		}
		Presentation.index();
	}

	public static void logout() throws Throwable {
		session.clear();
		response.removeCookie("pubmedwatcher");
		Presentation.index();
	}

}
