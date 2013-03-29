package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import controllers.Application;

import play.db.jpa.Model;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.XPath;
import utils.MapUtil;

@Entity
public class User extends Model {

	public String email;
	
	public String accessToken;

	@OneToMany(mappedBy="user", cascade=CascadeType.ALL)
	public List<RelatedArticle> relatedArticles;

	@ElementCollection
	public List<Integer> readArticlePmids;

	@OneToMany(mappedBy="user", cascade=CascadeType.ALL)
	public List<KeyArticle> keyArticles;

	public User(String email, String accessToken){
		this.accessToken = accessToken;
		this.email = email;
		this.relatedArticles = new ArrayList<RelatedArticle>();
		this.readArticlePmids = new ArrayList<Integer>();
		this.keyArticles = new ArrayList<KeyArticle>();
		this.save();
	}

	public void addRelatedArticle(int pmid, double similarity, double highestScore) {
		RelatedArticle newRelatedArticle = new RelatedArticle(this, pmid, similarity, highestScore);
		this.relatedArticles.add(newRelatedArticle);
		newRelatedArticle.save();
		this.save();
	}

	public void removeRelatedArticle(long relatedArticleId){
		RelatedArticle relatedArticleToDelete = RelatedArticle.findById(relatedArticleId);
		this.relatedArticles.remove(relatedArticleToDelete);
		relatedArticleToDelete.delete();
		this.save();
	}

	public void markAsRead(long id){
		RelatedArticle relatedArticleToDelete = RelatedArticle.findById(id);
		this.readArticlePmids.add(relatedArticleToDelete.pmid);
		this.relatedArticles.remove(relatedArticleToDelete);
		relatedArticleToDelete.delete();
		this.save();
	}

	public void unMarkAsRead(int pmid){
		for (int i = 0; i < this.readArticlePmids.size(); i++) {
			if(this.readArticlePmids.get(i) == pmid){
				this.readArticlePmids.remove(i);
			}
		}
		this.save();
	}

	public void addKeyArticle(int pmid) {
		KeyArticle newKeyArticle = new KeyArticle(this, pmid);
		this.keyArticles.add(newKeyArticle);
		newKeyArticle.save();
		this.save();
	}

	public void removeKeyArticle(long id){
		KeyArticle keyArticleToDetach = KeyArticle.findById(id);
		this.keyArticles.remove(keyArticleToDetach);
		keyArticleToDetach.delete();
		this.save();
	}

	//Updates the relates articles
	public void updateRelatedArticles() {

		List<String> newRelatedArticlesIds = new ArrayList<String>();
		HashMap<String, Double> newRelatedArticlesScores = new HashMap<String, Double>();
		//Gets the list of related articles pmid and similarity scores via PubMed API
		List<String> keyArticlesIds = getKeyArticlesIds();
		for (KeyArticle keyArticle : this.keyArticles) {
			//http://www.ncbi.nlm.nih.gov/books/NBK25499/ --> "cmd=neighbor (default)"
			HttpResponse res = WS.url("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?" +
					"dbfrom=pubmed&db=pubmed&id="+keyArticle.pmid+"&cmd=neighbor_score").get();
			int status = res.getStatus();
			//TODO If bad status then go back to home page + error message

			Document xml = res.getXml();
			//Iterates over the XML results and get pmids and scores out
			for(Node articles: XPath.selectNodes("//LinkSetDb[1]/Link", xml)) {
				String id = XPath.selectText("Id", articles);
				double similarity = Double.parseDouble(XPath.selectText("Score", articles));

				if(!keyArticlesIds.contains(id) && !this.readArticlePmids.contains(Integer.parseInt(id))){
					//Heuristic behind the articles ranking:
					//if an article is present more than one time, similarity scores
					//are added.
					if(newRelatedArticlesIds.contains(id)){
						double oldScore = newRelatedArticlesScores.get(id);
						newRelatedArticlesScores.put(id, oldScore + similarity);
					}else{
						newRelatedArticlesIds.add(id);
						newRelatedArticlesScores.put(id, similarity);
					}
				}
			}
		}

		removeOldRelatedArticles(newRelatedArticlesIds);

		if(newRelatedArticlesIds.size() > 0){
			addAndUpdateRelatedArticles(newRelatedArticlesIds, MapUtil.sortByValue(newRelatedArticlesScores));
		}

	}


	private List<String> getKeyArticlesIds() {
		List<String> keyArticlesIds = new ArrayList<String>();
		for (KeyArticle keyArticle : this.keyArticles) {
			keyArticlesIds.add(Integer.toString(keyArticle.pmid));
		}
		return keyArticlesIds;
	}

	private void addAndUpdateRelatedArticles(List<String> newRelatedArticlesIds, Map<String, Double> newRelatedArticlesScores) {

		String highestScorePmid =  newRelatedArticlesScores.keySet().iterator().next();
		double highestScore = newRelatedArticlesScores.get(highestScorePmid);

		for (String newRelatedArticleId : newRelatedArticlesIds) {

			double newRelatedArticleScore = newRelatedArticlesScores.get(newRelatedArticleId);

			//Try to get the corresponding related article previously saved
			RelatedArticle oldRelatedArticle = getOldRelatedArticle(newRelatedArticleId);
			//If the result is null, it means that the article wasn't previously known
			if(oldRelatedArticle == null){
				//The article is then added to the database
				this.addRelatedArticle(Integer.parseInt(newRelatedArticleId), newRelatedArticleScore, highestScore);
			}else{
				//The article content is updated with the freshest values
				oldRelatedArticle.update(newRelatedArticleScore, highestScore);
			}
		}
	}

	private RelatedArticle getOldRelatedArticle(String newRelatedArticleId) {
		int id = Integer.parseInt(newRelatedArticleId);
		for (RelatedArticle relatedArticle : this.relatedArticles) {
			if(relatedArticle.pmid == id){
				return relatedArticle;
			}
		}
		return null;
	}

	//Checks all the old related articles and delete them if not present in the new set anymore.
	private void removeOldRelatedArticles(List<String> newRelatedArticlesIds) {
		List<Long> toDeleteIds = new ArrayList<Long>();
		for (RelatedArticle oldRelatedArticle : this.relatedArticles) {

			if(!newRelatedArticlesIds.contains(Integer.toString(oldRelatedArticle.pmid))){
				toDeleteIds.add(oldRelatedArticle.id);
			}
		}

		for (Long relatedArticleToDeleteId : toDeleteIds) {
			removeRelatedArticle(relatedArticleToDeleteId);

		}
	}

}
