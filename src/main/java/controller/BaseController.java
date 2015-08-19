package controller;

import static spark.Spark.setPort;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.Cookie;

import spark.Request;
import spark.Response;
import spark.Route;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import dao.BlogPostDAO;
import dao.BlogPostDAOPg;
import dao.SessionDAO;
import dao.UserDAO;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * This class delegates all interaction with MongoDB
 * to three Data Access Objects (DAOs).
 * <p/>
 * It is also the entry point into the web application.
 */
public class BaseController
{
	protected final Configuration cfg;
	protected final BlogPostDAO blogPostDAO;
	protected final UserDAO userDAO;
	protected final SessionDAO sessionDAO;
	protected BlogPostDAOPg blogPostDAOPg;

	public BaseController(String mongoURIString, String pgURIString) throws IOException, SQLException
	{
		// Init mongodb
		final MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoURIString));
		final DB blogDatabase = mongoClient.getDB("tcc");

		blogPostDAO = new BlogPostDAO(blogDatabase);
		userDAO = new UserDAO(blogDatabase);
		sessionDAO = new SessionDAO(blogDatabase);

		// Init postgres
		Connection con = DriverManager.getConnection(pgURIString, "postgres", "postgres");
		Statement st = con.createStatement();
		blogPostDAOPg = new BlogPostDAOPg(st);

		// Init freemarker
		cfg = createFreemarkerConfiguration();
		setPort(8082);
	}

	abstract class FreemarkerBasedRoute extends Route
	{
		final Template template;

		/**
		 * Constructor
		 * 
		 * @param path The route path which is used for matching. (e.g. /hello, users/:name)
		 */
		protected FreemarkerBasedRoute(final String path, final String templateName) throws IOException
		{
			super(path);
			template = cfg.getTemplate(templateName);
		}

		@Override
		public Object handle(Request request, Response response)
		{
			StringWriter writer = new StringWriter();
			try
			{
				doHandle(request, response, writer);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				response.redirect("/internal_error");
			}
			return writer;
		}

		protected abstract void doHandle(final Request request, final Response response, final Writer writer)
				throws IOException, TemplateException, SQLException;

	}

	// helper function to get session cookie as string
	protected String getSessionCookie(final Request request)
	{
		if (request.raw().getCookies() == null)
		{
			return null;
		}
		for (Cookie cookie : request.raw().getCookies())
		{
			if (cookie.getName().equals("session"))
			{
				return cookie.getValue();
			}
		}
		return null;
	}

	// helper function to get session cookie as string
	protected Cookie getSessionCookieActual(final Request request)
	{
		if (request.raw().getCookies() == null)
		{
			return null;
		}
		for (Cookie cookie : request.raw().getCookies())
		{
			if (cookie.getName().equals("session"))
			{
				return cookie;
			}
		}
		return null;
	}

	// tags the tags string and put it into an array
	protected ArrayList<String> extractTags(String tags)
	{

		// probably more efficent ways to do this.
		//
		// whitespace = re.compile('\s')

		tags = tags.replaceAll("\\s", "");
		String tagArray[] = tags.split(",");

		// let's clean it up, removing the empty string and removing dups
		ArrayList<String> cleaned = new ArrayList<String>();
		for (String tag : tagArray)
		{
			if (!tag.equals("") && !cleaned.contains(tag))
			{
				cleaned.add(tag);
			}
		}

		return cleaned;
	}

	// validates that the registration form has been filled out right and username conforms
	public boolean validateSignup(String username, String password, String verify, String email,
			HashMap<String, String> errors)
	{
		String USER_RE = "^[a-zA-Z0-9_-]{3,20}$";
		String PASS_RE = "^.{3,20}$";
		String EMAIL_RE = "^[\\S]+@[\\S]+\\.[\\S]+$";

		errors.put("username_error", "");
		errors.put("password_error", "");
		errors.put("verify_error", "");
		errors.put("email_error", "");

		if (!username.matches(USER_RE))
		{
			errors.put("username_error", "invalid username. try just letters and numbers");
			return false;
		}

		if (!password.matches(PASS_RE))
		{
			errors.put("password_error", "invalid password.");
			return false;
		}

		if (!password.equals(verify))
		{
			errors.put("verify_error", "password must match");
			return false;
		}

		if (!email.equals(""))
		{
			if (!email.matches(EMAIL_RE))
			{
				errors.put("email_error", "Invalid Email Address");
				return false;
			}
		}

		return true;
	}

	private Configuration createFreemarkerConfiguration()
	{
		Configuration retVal = new Configuration();
		retVal.setClassForTemplateLoading(BaseController.class, "/freemarker");
		return retVal;
	}
}