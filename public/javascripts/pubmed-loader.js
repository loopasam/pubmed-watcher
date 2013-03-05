function loadDocuments(pmids, toHide){
	$('.loader-articles').show();
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
				if(!toHide){
					$('.show-more').show();
				}
				
			});
			$('.show-more span').show();
			$('.show-more img').hide();
			$('.loader-articles').hide();
		},
		// callback handler that will be called on error
		error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: "+ textStatus, errorThrown);
		}
	});
}

function appendArticle(pmid, date, journal, title, authorList) {
	var article = $('<div></div>');
	article.append('<p><a href="http://www.ncbi.nlm.nih.gov/pubmed/'+pmid+'" target="_BLANK"><strong>' + title + '</strong></a></p>');
	article.append('<p>' + authorList + '</p>');
			
	article.append('<p>' + journal + ' - <em>' + date + '</em> - PMID: ' + pmid + '</p>');
	
	var readInteraction = $('#' + pmid + ' a');
	$('#' + pmid + ' a').remove();
	article.append(readInteraction);
	
	var score = $('#' + pmid).attr('data-score');
			
	if(score != undefined){
		article.append('<div class="progress progress-striped" style="margin-bottom: 0px;"><div class="bar" style="width: ' + score + '%;">'+score+'% relative relatedness</div></div>');
	}
	$('#' + pmid).append(article);
	$('#' + pmid).show();
}

//TODO correct the road for call
$(document).ready(function() {

	$('#showMoreRelatedArticles').click(function(){
		var that = this;
		$('.show-more span').hide();
		$('.show-more img').show();
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
				var toHide = false;
				$.each(json, function() {
					verif++;
					if(isFirst){
						pmids += this.pmid;
						isFirst = false;
					}else{
						pmids += "," + this.pmid;
					}
					$('#related-articles').append('<div class="well" style="display: none;" id="'+ this.pmid +'" data-score="' + Math.round(this.score) + '">'+
							'<a href="/markAsRead/'+ this.id +'" class="btn pull-right btn-mini markasread"><i class="icon-eye-open"></i> mark as read</a></a></div>');
				});
				if(verif < 10){
					toHide = true;
					$('#showMoreRelatedArticles').hide();
				}
				loadDocuments(pmids, toHide);
			},
			// callback handler that will be called on error
			error: function(jqXHR, textStatus, errorThrown){
				console.log("The following error occured: "+ textStatus, errorThrown);
			}
		});
	});


	$('#showMoreReadArticles').click(function(){
		var that = this;
		$('.show-more span').hide();
		$('.show-more img').show();
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
				var toHide = false;
				$.each(json, function() {
					verif++;
					if(isFirst){
						pmids += this.pmid;
						isFirst = false;
					}else{
						pmids += "," + this.pmid;
					}
					$('#read-articles').append('<div class="well" style="display: none;" id="'+ this.pmid +'">'+
							'<a href="/unMarkAsRead/' + this.pmid + '" class="btn"><i class="icon-eye-close"></i> mark as unread</a></div>');
				});
				if(verif < 10){
					toHide = true;
					$('#showMoreReadArticles').hide();
				}
				loadDocuments(pmids, toHide);
			},
			// callback handler that will be called on error
			error: function(jqXHR, textStatus, errorThrown){
				console.log("The following error occured: "+ textStatus, errorThrown);
			}
		});
	});

});
