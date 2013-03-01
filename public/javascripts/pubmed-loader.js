function loadDocuments(pmids){
	$.ajax({
		url: "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=" + pmids,
		type: "GET",
		success: function(xml, textStatus, jqXHR){
			$(xml).find('DocSum').each(function(){
				var pmid = $(this).find('Id').text();
				var date = $(this).find('Item[Name="PubDate"]').text();
				var journal = $(this).find('Item[Name="Source"]').text();
				var title = $(this).find('Item[Name="Title"]').text();

				var authorList = "";
				var maxAuthors = 5;
				var i = 0;
				var authors = $(this).find('Item[Name="AuthorList"] Item[Name="Author"]').each(function(){
					var author = $(this).text();
					if(i == 0){
						authorList += author;
					}else if(i < maxAuthors){
						authorList += ", " + author;
					}else if(i == maxAuthors){
						authorList += " et al.";
					}
					i++;
				});

				appendArticle(pmid, date, journal, title, authorList);
			});
		},
		// callback handler that will be called on error
		error: function(jqXHR, textStatus, errorThrown){
			//TODO handle the error
			console.log("The following error occured: "+ textStatus, errorThrown);
		}
	});
}

function appendArticle(pmid, date, journal, title, authorList) {
	var article = $('<div></div>');
	article.append('<div>' + pmid + '</div>');
	article.append('<div>' + title + '</div>');
	article.append('<div>' + authorList + '</div>');
	article.append('<div>' + date + '</div>');
	article.append('<div>' + journal + '</div>');
	var score = $('#' + pmid).attr('data-score');
	if(score != NaN){
		article.append('<div class="progress"><div class="bar" style="width: ' + score + '%;"></div></div>');
	}
	$('#' + pmid).append(article);
	$('#' + pmid).show();
}

//TODO correct the road for call
$(document).ready(function() {

	$('#showMoreRelatedArticles').click(function(){
		var that = this;
		var pagination = $(this).attr('data-pagination');
		$.ajax({
			url: "/moreRelatedArticles/" + pagination,
			type: "GET",
			success: function(json, textStatus, jqXHR){
				var newPagination = parseInt(pagination) + 10;
				var pmids = "";
				var isFirst = true;
				$(that).attr('data-pagination', newPagination);
				var verif = 0;
				$.each(json, function() {
					verif++;
					if(isFirst){
						pmids += this.pmid;
						isFirst = false;
					}else{
						pmids += "," + this.pmid;
					}
					$('#related-articles').append('<div style="display: none;" id="'+ this.pmid +'" data-score="' + this.score + '">'+
							'<a href="/markAsRead/'+ this.id +'" class="btn">mark as read</a></div><hr>');
				});
				if(verif < 10){
					$('#showMoreRelatedArticles').hide();
				}
				loadDocuments(pmids);
			},
			// callback handler that will be called on error
			error: function(jqXHR, textStatus, errorThrown){
				//TODO handle the error
				console.log("The following error occured: "+ textStatus, errorThrown);
			}
		});
	});


	$('#showMoreReadArticles').click(function(){
		var that = this;
		var pagination = $(this).attr('data-pagination');
		$.ajax({
			url: "/moreReadArticles/" + pagination,
			type: "GET",
			success: function(json, textStatus, jqXHR){
				var newPagination = parseInt(pagination) + 10;
				var pmids = "";
				var isFirst = true;
				$(that).attr('data-pagination', newPagination);
				var verif = 0;
				$.each(json, function() {
					verif++;
					if(isFirst){
						pmids += this.pmid;
						isFirst = false;
					}else{
						pmids += "," + this.pmid;
					}
					$('#read-articles').append('<div style="display: none;" id="'+ this.pmid +'">'+
							'<a href="/unMarkAsRead/' + this.pmid + '" class="btn">unmark as read</a></div><hr>');
				});
				if(verif < 10){
					$('#showMoreReadArticles').hide();
				}
				loadDocuments(pmids);
			},
			// callback handler that will be called on error
			error: function(jqXHR, textStatus, errorThrown){
				//TODO handle the error
				console.log("The following error occured: "+ textStatus, errorThrown);
			}
		});
	});

});
