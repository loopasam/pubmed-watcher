package controllers;

import play.*;
import play.data.validation.Required;
import play.libs.WS;
import play.libs.XPath;
import play.libs.WS.HttpResponse;
import play.mvc.*;

import java.net.ConnectException;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import models.*;

@With(Security.class)
public class Application extends Controller {

	public static final int PAGINATION = 10;

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
			user.updateRelatedArticles();
			session.put("updated", "true");
		}
		renderArgs.put("email", user.email);
		List<KeyArticle> keyArticles = user.keyArticles;
		List<RelatedArticle> relatedArticles = RelatedArticle.find("user=? order by standardizedSimilarity desc", user).fetch(PAGINATION);

		String pmids = "";
		boolean isFirst = true;
		for (RelatedArticle relatedArticle : relatedArticles) {
			if(isFirst){
				isFirst = false;
				pmids = Integer.toString(relatedArticle.pmid);
			}else{
				pmids += "," + relatedArticle.pmid;
			}
		}
		render(relatedArticles, keyArticles, pmids);
	}

	public static void addKeyArticle() {
		String email = connected().email;
		render(email);
	}

	public static void settings(){
		String email = connected().email;
		render(email);
	}

	public static void removeKeyArticle(long id) {
		connected().removeKeyArticle(id);
		session.put("updated", "false");
		index();
	}

	public static void markAsRead(long id) {
		connected().markAsRead(id);
		index();
	}

	public static void unMarkAsRead(int pmid) {
		connected().unMarkAsRead(pmid);
		session.put("updated", "false");
		index();
	}

	public static void deleteAccount(String email, boolean confirmed) throws Throwable{

		if(email.equals(connected().email) && confirmed){
			Security.revoke();
		}else{
			validation.addError(null, "Please enter your email address and tick the box in order to delete your account.");
			validation.keep();
			settings();
		}
	}

	public static void read(){

		int pagination = 10;
		boolean someLeft = true;

		if(connected().readArticlePmids.size() <= pagination){
			pagination = connected().readArticlePmids.size();
			someLeft = false;
		}

		List<Integer> readArticlePmids = connected().readArticlePmids.subList(0, pagination);
		String pmids = "";
		boolean isFirst = true;
		for (Integer relatedArticlePmid : connected().readArticlePmids) {
			if(isFirst){
				isFirst = false;
				pmids = Integer.toString(relatedArticlePmid);
			}else{
				pmids += "," + relatedArticlePmid;
			}
		}
		String email = connected().email;
		render(readArticlePmids, pmids, email, someLeft);
	}

	public static void moreReadArticles(int pagination){

		int oldPagination = pagination;

		if(connected().readArticlePmids.size() < 10 + pagination){
			pagination = connected().readArticlePmids.size();
		}else{
			pagination = 10 + pagination;
		}

		List<Integer> readArticlePmids = connected().readArticlePmids.subList(oldPagination, pagination);

		render("Application/readArticles.json", readArticlePmids);
	}


	public static void moreRelatedArticles(int pagination){
		User user = connected();
		List<RelatedArticle> relatedArticles = 
				RelatedArticle.find("user=? order by standardizedSimilarity desc", user).from(pagination).fetch(PAGINATION);		
		render("Application/relatedArticles.json", relatedArticles);
	}

	public static void addnewKeyArticle(int pmid) {

		if(connected().keyArticles.size() > 3){
			validation.addError(null, "Already four Key Articles, you sneaky!");
			validation.keep();
			index();
		}

		//http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=11850928,11482001
		HttpResponse res = WS.url("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?" +
				"db=pubmed&id=" + pmid).get();

		Document xml = res.getXml();
		//Iterates over the XML results and get pmids and scores out

		if(XPath.selectNodes("//DocSum", xml).size() == 0){
			validation.keep();
			addKeyArticle();
		}

		//Create and save a new key article
		for(Node article: XPath.selectNodes("//DocSum", xml)) {
			KeyArticle newKeyArticle = new KeyArticle(connected(), pmid);
			newKeyArticle.date = XPath.selectText("Item[@Name='PubDate']", article);
			newKeyArticle.title = XPath.selectText("Item[@Name='Title']", article);
			newKeyArticle.journal = XPath.selectText("Item[@Name='Source']", article);
			List<Node> authors = XPath.selectNodes("//DocSum/Item[@Name='AuthorList']", article);
			newKeyArticle.authors = XPath.selectText("Item[1]", authors);
			connected().keyArticles.add(newKeyArticle);
			newKeyArticle.save();
			connected().save();
		} 

		session.put("updated", "false");
		index();
	}
}