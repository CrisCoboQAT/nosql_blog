<!DOCTYPE html>
<html>
<head>
    <title>Meu Blog</title>
</head>
<body style="background-color: thistle;">

<#if username??>
    Bem vinda ${username} <a href="/logout">Sair</a> | <a href="/newpost">Nova Postagem</a>

    <p>
</#if>
<div style="position: absolute; width: 80%; margin-left: 10%;">
<h1>Meu Blog</h1>
<div style="border-style: solid; border-width: 1px;">
<div style="margin-right: 10px; margin-left: 10px; position: relative;">
<#list myposts as post>
    <h2><a href="/post/${post["_id"]}">${post["title"]}</a></h2>
    Postado ${post["date"]?datetime} <i>Por ${post["author"]}</i><br>
    Comentarios:
    <#if post["comments"]??>
        <#assign numComments = post["comments"]?size>
            <#else>
                <#assign numComments = 0>
    </#if>

    <a href="/post/${post["_id"]}">${numComments}</a>
    <hr>
    ${post["body"]!""}
    <p>

    <p>
        <em>Tags</em>:
        <#if post["tags"]??>
            <#list post["tags"] as tag>
                <a href="/tag/${tag}">${tag}</a>
            </#list>
        </#if>

    <p>
    <hr>
</#list>
</div>
</div>
</div>
</body>
</html>

