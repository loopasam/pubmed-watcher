package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class User extends Model {

	public String email;
	
	//Get it via http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pubmed&db=pubmed&id=20210808&cmd=neighbor_score
	@OneToMany
	public List<RelatedArticle> relatedArticles;

	public User(String email){
		this.email = email;
		this.relatedArticles = new ArrayList<RelatedArticle>();
	}
}
