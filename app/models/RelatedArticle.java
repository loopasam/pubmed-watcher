package models;

import java.util.Date;

import javax.persistence.Id;

import play.db.jpa.Model;

public class RelatedArticle extends Model {
	
	@Id
	public int pmid;
	
	public int similarity;
	
	public int standardizedSimilarity;
	
}
