<!DOCTYPE html>

<html>
  <head>
    <title>Cadastrar</title>
    <style type="text/css">
      .label {text-align: right}
      .error {color: red}
    </style>

  </head>

  <body style="background-color: thistle;">
    Ja esta cadastrado? <a href="/login">Entrar</a><p>
    <div style="position: absolute; width: 80%;">
	<div style="margin-right: 10px; margin-left: 10px; position: relative;">
    <h1 style="font-size: 45px;">Cadastrar</h1>
    <form method="post">
      <table style="font-size: 30px;">
        <tr>
          <td class="label">
            Usuario
          </td>
          <td>
            <input type="text" name="username" value="${username}" style="height:20px; width:200px; font-size:20px;>
          </td>
          <td class="error">
	    ${username_error!""}

          </td>
        </tr>

        <tr>
          <td class="label">
            Senha
          </td>
          <td>
            <input type="password" name="password" value="" style="height:20px; width:200px; font-size:20px;>
          </td>
          <td class="error">
	    ${password_error!""}

          </td>
        </tr>

        <tr>
          <td class="label">
            Confirmar Senha
          </td>
          <td>
            <input type="password" name="verify" value="" style="height:20px; width:200px; font-size:20px;>
          </td>
          <td class="error">
	    ${verify_error!""}

          </td>
        </tr>

        <tr>
          <td class="label">
            Email (opcional)
          </td>
          <td>
            <input type="text" name="email" value="${email}" style="height:20px; width:300px; font-size:20px;>
          </td>
          <td class="error">
	    ${email_error!""}

          </td>
        </tr>
      </table>
	  </br>
      <input type="submit" value="Salvar" style="margin-left: 39%; margin-bottom: 10%; height: 35px; width: 100px; font-size: 20px;">
    </form>
    </div>
    </div>
  </body>

</html>