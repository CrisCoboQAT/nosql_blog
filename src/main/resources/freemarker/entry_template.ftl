<!doctype HTML>
<html
<head>
    <title>
        Blog Post
    </title>
</head>
<body style="background-color: thistle;">
<#if username??>
    Bem vinda ${username} <a href="/logout">Sair</a> | <a href="/newpost">Nova Postagem</a>

    <p>
</#if>

<a href="/">Meu Blog</a><br><br>
<div style="position: absolute; width: 80%; margin-left: 10%;">
<div style="border-style: solid; border-width: 1px;">
<div style="margin-right: 10px; margin-left: 10px; position: relative;">
<h2>${post["title"]}</h2>
Postado ${post["date"]?datetime}<i> Por ${post["author"]}</i><br>
<hr>
${post["body"]}
<p>
    <em>Tags</em>:
    <#if post["tags"]??>
        <#list post["tags"] as tag>
            <a href="/tag/${tag}">${tag}</a>
        </#list>
    </#if>
<p>
    Comentarios:
<ul>
    <#if post["comments"]??>
        <#assign numComments = post["comments"]?size>
            <#else>
                <#assign numComments = 0>
    </#if>
    <#if (numComments > 0)>
        <#list 0 .. (numComments -1) as i>
            <form action="/like" method="POST">
                <input type="hidden" name="id", value="${post["_id"]}">
                <input type="hidden" name="comment_ordinal", value="${i}">
                Author: ${post["comments"][i]["author"]}<br>
                Likes: ${post["comments"][i]["num_likes"]} <input type="submit" value="Like"></form>
            <br>
            ${post["comments"][i]["body"]}<br>
            <hr>
        </#list>
    </#if>
    <h3>Adicionar um comentario</h3>

    <form action="/newcomment" method="POST">
        <input type="hidden" name="id", value="${post["_id"]}">
        ${errors!""}<br>
        <b>Nome</b> (requerido)<br>
        <input type="text" name="commentName" size="60" value="${comment["name"]}"><br>
        <b>Email</b> (opcional)<br>
        <input type="text" name="commentEmail" size="60" value="${comment["email"]}"><br>
        <b>Comentario</b><br>
        <textarea name="commentBody" cols="60" rows="10">${comment["body"]}</textarea><br>
        <input type="submit" value="Salvar" style="margin-left: 25%;">
    </form>
</ul>
</div>
</div>
</div>
</body>
</html>


