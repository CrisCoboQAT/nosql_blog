<!doctype HTML>
<html>
<head>
    <title>Create a new post</title>
</head>
<body style="background-color: thistle;">
<#if username??>
    Bem vinda ${username} <a href="/logout">Sair</a> | <a href="/">Visualizar Blog</a>

    <p>
</#if>
<div style="position: absolute; width: 80%; margin-left: 10%;">
<div style="border-style: solid; border-width: 1px;">
<div style="margin-right: 10px; margin-left: 10px; position: relative;">
<form action="/newpost" method="POST">
    ${errors!""}
    <h2>Titulo</h2>
    <input type="text" name="subject" size="120" value="${subject!""}" ><br>

    <h2>Texto
        <h2>
            <textarea name="body" cols="120" rows="20">${body!""}</textarea><br>

            <h2>Tags</h2>
            Separadas por virgula, por favor<br>
            <input type="text" name="tags" size="120" value="${tags!""}"><br>

            <p>
                <input type="submit" value="Salvar" style="margin-left: 50%;">
</form>
</div>
</div>
</div>
</body>
</html>

