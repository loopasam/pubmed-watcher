package models;

import java.util.Date;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.jpa.Model;

@Entity
public class KeyArticle extends Model {
	
	public int pmid;
	
	public String title;
	
	public Date date;
	
	public String journal;
	
	@ElementCollection
	public List<String> authors;
	
}
