package models;

import java.util.ArrayList;
import java.util.List;

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

@Entity
public class User extends Model {

	public String email;

	@OneToMany(mappedBy="user", cascade=CascadeType.ALL)
	public List<RelatedArticle> relatedArticles;

	@ElementCollection
	public List<Integer> readArticlePmids;

	@OneToMany(mappedBy="user", cascade=CascadeType.ALL)
	public List<KeyArticle> keyArticles;

	public User(String email){
		this.email = email;
		this.relatedArticles = new ArrayList<RelatedArticle>();
		this.readArticlePmids = new ArrayList<Integer>();
		this.keyArticles = new ArrayList<KeyArticle>();
	}

	public void addRelatedArticle(int pmid, int similarity, double highestScore) {
		RelatedArticle newRelatedArticle = new RelatedArticle(this, pmid, similarity, highestScore);
		this.relatedArticles.add(newRelatedArticle);
		this.save();
	}

	public void removeRelatedArticle(RelatedArticle relatedArticle){
		this.relatedArticles.remove(relatedArticle);
		relatedArticle.delete();
		this.save();
	}

	public void markAsRead(Long id){
		RelatedArticle relatedArticleToDelete = RelatedArticle.findById(id);
		this.readArticlePmids.add(relatedArticleToDelete.pmid);
		this.relatedArticles.remove(relatedArticleToDelete);
		relatedArticleToDelete.delete();
		this.save();
	}

	public void addKeyArticle(int pmid) {
		KeyArticle newKeyArticle = new KeyArticle(this, pmid);
		this.keyArticles.add(newKeyArticle);
		this.save();
	}

	public void removeKeyArticle(Long id){
		KeyArticle keyArticleToDetach = KeyArticle.findById(id);
		this.keyArticles.remove(keyArticleToDetach);
		keyArticleToDetach.delete();
		this.save();
	}

	public void updateRelatedArticles() {

		for (KeyArticle keyArticle : this.keyArticles) {
			System.out.println("Getting related articles: " + keyArticle.pmid);
			HttpResponse res = WS.url("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?" +
					"dbfrom=pubmed&db=pubmed&id="+keyArticle.pmid+"&cmd=neighbor_score").get();
			int status = res.getStatus();
			//TODO If bad status then go back to home page + error message

			System.out.println("status: " + status);
			Document xml = res.getXml();
			List<String> newRelatedArticlesIds = new ArrayList<String>();
			List<String> newRelatedArticlesScores = new ArrayList<String>();

			for(Node articles: XPath.selectNodes("//LinkSetDb[1]/Link", xml)) {
				String id = XPath.selectText("Id", articles);
				
				//TODO do the pooling here
				newRelatedArticlesIds.add(id);
				String similarity = XPath.selectText("Score", articles);
				//TODO do the pooling here
				newRelatedArticlesScores.add(similarity);
			}

			removeOldRelatedArticles(newRelatedArticlesIds);
			addAndUpdateRelatedArticles(newRelatedArticlesIds, newRelatedArticlesScores);
		}

	}

	private void addAndUpdateRelatedArticles(List<String> newRelatedArticlesIds, List<String> newRelatedArticlesScores) {
		
		//TODO more complicated than that. The pooling - sorting has to happen before.
		double	highestScore = Integer.parseInt(newRelatedArticlesScores.get(0));

		for (int i = 0; i < newRelatedArticlesIds.size(); i++) {
			
			String newRelatedArticleId = newRelatedArticlesIds.get(i);
			String newRelatedArticleScore = newRelatedArticlesScores.get(i);
			
			RelatedArticle oldRelatedArticle = getOldRelatedArticle(newRelatedArticleId);
			if(oldRelatedArticle == null){
				this.addRelatedArticle(Integer.parseInt(newRelatedArticleId), Integer.parseInt(newRelatedArticleScore), highestScore);
			}else{
				oldRelatedArticle.update(Integer.parseInt(newRelatedArticleScore), highestScore);
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

	private void removeOldRelatedArticles(List<String> newRelatedArticlesIds) {
		for (RelatedArticle oldRelatedArticle : this.relatedArticles) {
			if(!newRelatedArticlesIds.contains(Integer.toString(oldRelatedArticle.pmid))){
				this.removeRelatedArticle(oldRelatedArticle);
			}
		}
	}

}
