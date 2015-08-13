<!DOCTYPE html>

<html>
  <head>
    <title>Analise</title>
  </head>
  <body style="background-color: thistle;">
  <div style="background-color:rebeccapurple; text-align:center;">
  	<h1>Analise de Desempenho</h1>
  </div>
  <div id="mongodb" style="width:49%;float:left;border-style: solid;border-width: 2px;">
  	  <div style="margin-left:10px">
	  	  <h2>Mongo Db</h2>
		  <div id="postByDate">
		    <h3>Postagens ordenadas por data de insercao</h3>
		    <p>Tempo total de execucao: ${executionTimeMethodByDate}</p>
		  </div>
		  <div id="postByBody">
		    <h3>Postagens que contem uma determinada palavra ("postagem") em seu conteudo</h3>
		    <p>Tempo total de execucao: ${executionTimeMethodByBody}</p>
		  </div>
		  <div id="postByCommentedUser">
		    <h3>Postagens comentadas por um determinado usuario ("autor comentario 1")</h3>
		    <p>Tempo total de execucao: ${executionTimeMethodByCommentedUser}</p>
		  </div>
	 </div>
  </div>
  <div id="postgresql" style="width:49%;float:right;border-style: solid;border-width: 2px;">
  	  <div style="margin-left:10px">
	  	  <h2>Postgre SQL</h2>
		  <div id="postByDatePg">
		  	<h3>Postagens ordenadas por data de insercao</h3>
		  	<p>Tempo total de execucao: ${executionTimeMethodByDatePG}</p>
		  </div>
		  <div id="postByBodyPg">
		  	<h3>Postagens que contem uma determinada palavra ("postagem") em seu conteudo</h3>
		  	<p>Tempo total de execucao: ${executionTimeMethodByBodyPG}</p>
		  </div>
		  <div id="postByCommentedUserPg">
		  	<h3>Postagens comentadas por um determinado usuario ("autor comentario 1")</h3>
		  	<p>Tempo total de execucao: ${executionTimeMethodByCommentedUserPG}</p>
		  </div>
	</div>
  </div>
</html>