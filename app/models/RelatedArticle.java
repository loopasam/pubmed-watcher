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

	public double standardizedSimilarity;

	@ManyToOne
	public User user;

	public RelatedArticle(User user, int pmid, int similarity, double highestScore){
		this.user = user;
		this.pmid = pmid;
		this.similarity = similarity;
		this.standardizedSimilarity = similarity * 100.0 / highestScore;
	}

	public void update(int newSimilarity, double highestScore) {
		
		//TODO actually maybe no needs to check the score + round score

		boolean modified = false;
		
		double newStandardizedSimilarity = newSimilarity * 100.0 / highestScore;

		if(newSimilarity != this.similarity){
			this.similarity = newSimilarity;
			modified = true;
		}

		if(newStandardizedSimilarity != this.standardizedSimilarity){
			this.standardizedSimilarity = newStandardizedSimilarity;
			modified = true;
		}

		if(modified){
			this.save();
		}
	}
}
