<!DOCTYPE html>

<html>
  <head>
    <title>Login</title>
    <style type="text/css">
      .label {text-align: right}
      .error {color: red}
    </style>

  </head>

  <body style="background-color: thistle;">
    Precisa criar uma conta? <a href="/signup">Cadastrar</a><p>
    <div style="position: absolute; width: 80%;">
	<div style="margin-right: 10px; margin-left: 10px; position: relative;">
    <h1 style="font-size: 45px;">Entrar</h1>
    <form method="post">
      <table style="font-size: 30px;">
        <tr>
          <td class="label">
            Usuario
          </td>
          <td>
            <input type="text" name="username" value="${username}" size="21" style="height:20px; width:220px; font-size:20px">
          </td>
          <td class="error">
          </td>
        </tr>

        <tr>
          <td class="label">
            Senha
          </td>
          <td>
            <input type="password" name="password" value="" style="height:20px; width:220px; font-size:20px;">
          </td>
          <td class="error">
	    ${login_error}

          </td>
        </tr>

      </table>
	  </br>
      <input type="submit" value="Entrar" style="margin-left: 21%; margin-bottom: 10%; height: 35px; width: 100px; font-size: 20px;">
    </form>
    </div>
    </div>
  </body>

</html>
