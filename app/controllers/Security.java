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
				if(Crypto.sign(restOfCookie).equals(sign)) {
					session.put("email", email);
					Application.index();
				}
			}
		}

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

		String client_secret = (String) Play.configuration.get("client_secret");
		
		//call for access token
		HttpResponse res = WS.url("https://accounts.google.com/o/oauth2/token").
				setParameter("code", code).setParameter("client_id", "65272228633.apps.googleusercontent.com").
				setParameter("client_secret", client_secret).setParameter("redirect_uri", "http://localhost:9000/auth").
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
				response.setCookie("pubmedwatcher", Crypto.sign(email + "-" + expiration.getTime()) + "-" + email + "-" + expiration.getTime(), duration);

				User user = User.find("byEmail", email).first();
				if(user == null){
					new User(email, accessToken).save();
				}else{
					user.accessToken = accessToken;
					user.save();
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

	public static void revoke() {

		WS.url("https://accounts.google.com/o/oauth2/revoke?token="+Application.connected().accessToken).get();

		Application.connected().delete();
		session.clear();
		response.removeCookie("pubmedwatcher");
		try {
			Security.logout();
		} catch (Throwable exception) {	}

	}
}