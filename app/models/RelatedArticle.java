package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class RelatedArticle extends Model {

	public int pmid;

	public int similarity;

	public int standardizedSimilarity;
	
	@ManyToOne
	public User user;

	public RelatedArticle(User user, int pmid, int similarity){
		this.user = user;
		this.pmid = pmid;
		this.similarity = similarity;
	}
}
