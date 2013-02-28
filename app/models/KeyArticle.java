package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class KeyArticle extends Model {

	public int pmid;

	public String title;

	public String date;

	public String journal;

	@ElementCollection
	public List<String> authors;

	@ManyToOne
	public User user;

	public KeyArticle(User user, int pmid) {
		this.user = user;
		this.pmid = pmid;
	}

}
