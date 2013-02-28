package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class RelatedArticle extends Model {

	public int pmid;

	public double standardizedSimilarity;

	@ManyToOne
	public User user;

	public RelatedArticle(User user, int pmid, double similarity, double highestScore){
		this.user = user;
		this.pmid = pmid;
		this.standardizedSimilarity = getStandardizedScore(similarity, highestScore);
	}

	private double getStandardizedScore(double similarity, double highestScore) {
		return Math.round(similarity * 100.0 / highestScore);
	}

	public void update(double newSimilarity, double highestScore) {

		boolean modified = false;

		double newStandardizedSimilarity = getStandardizedScore(newSimilarity, highestScore);

		if(newStandardizedSimilarity != this.standardizedSimilarity){
			this.standardizedSimilarity = newStandardizedSimilarity;
			modified = true;
		}

		if(modified){
			this.save();
		}
	}
}
