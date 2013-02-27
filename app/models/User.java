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

	public void addRelatedArticle(int pmid, int similarity) {
		RelatedArticle newRelatedArticle = new RelatedArticle(this, pmid, similarity);
		this.relatedArticles.add(newRelatedArticle);
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

		this.addKeyArticle(23273493);

		for (KeyArticle keyArticle : this.keyArticles) {
			System.out.println("Getting related articles: " + keyArticle.pmid);
			HttpResponse res = WS.url("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?" +
					"dbfrom=pubmed&db=pubmed&id="+keyArticle.pmid+"&cmd=neighbor_score").get();
			int status = res.getStatus();
			//TODO faire les XPATH ici
			System.out.println("status: " + status);
			Document xml = res.getXml();
			for(Node event: XPath.selectNodes("LinkSet//LinkSetDb[0]/Link", xml)) {
				String name = XPath.selectText("name", event);
				String data = XPath.selectText("@date", event);

			}
		}

	}

}
