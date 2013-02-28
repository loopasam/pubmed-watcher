package controllers;

import play.*;
import play.data.validation.Required;
import play.libs.WS;
import play.libs.XPath;
import play.libs.WS.HttpResponse;
import play.mvc.*;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
		if(updated == null || updated.equals("false")){
			System.out.println("not updated yet...");
			user.updateRelatedArticles();
			session.put("updated", "true");
		}else{
			System.out.println("already updated");
		}
		renderArgs.put("email", user.email);
		List<KeyArticle> keyArticles = user.keyArticles;
		List<RelatedArticle> relatedArticles = RelatedArticle.find("user=? order by standardizedSimilarity desc", user).fetch(10);
		render(relatedArticles, keyArticles);
	}

	public static void addKeyArticle() {
		render();
	}

	public static void addnewKeyArticle(int pmid) {
		//http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=11850928,11482001
		HttpResponse res = WS.url("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?" +
				"db=pubmed&id=" + pmid).get();
		int status = res.getStatus();
		//TODO If bad status then go back to home page + error message
		System.out.println("status key article: " + status);
		Document xml = res.getXml();
		//Iterates over the XML results and get pmids and scores out
		
		if(XPath.selectNodes("//DocSum", xml).size() == 0){
			//TODO handle problem in connection too and configure errors
			addKeyArticle();
		}

		for(Node article: XPath.selectNodes("//DocSum", xml)) {
			String pubDate = XPath.selectText("Item[@Name='PubDate']", article);
			System.out.println("--pubdate: " + pubDate);
			String title = XPath.selectText("Item[@Name='Title']", article);
			System.out.println("--title: " + title);
			String source = XPath.selectText("Item[@Name='Source']", article);
			System.out.println("--Source: " + source);
		}

		session.put("updated", "false");
		index();
	}
}