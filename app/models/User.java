package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class User extends Model {

	public String email;

	//Get it via http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pubmed&db=pubmed&id=20210808&cmd=neighbor_score
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
		RelatedArticle newRelatedArticle = new RelatedArticle(this, pmid, similarity).save();
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
		KeyArticle newKeyArticle = new KeyArticle(this, pmid).save();
		this.keyArticles.add(newKeyArticle);
		this.save();
	}

	public void removeKeyArticle(Long id){
		KeyArticle keyArticleToDetach = KeyArticle.findById(id);
		this.keyArticles.remove(keyArticleToDetach);
		keyArticleToDetach.delete();
		this.save();
	}

}
