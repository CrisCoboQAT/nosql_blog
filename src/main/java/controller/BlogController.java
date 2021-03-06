package controller;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringEscapeUtils;

import spark.Request;
import spark.Response;
import spark.Route;
import util.Timer;

import com.mongodb.DBObject;

import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;

/**
 * This class encapsulates the controllers for the blog web application.
 */
public class BlogController extends BaseController
{
	public static void main(String[] args) throws IOException, SQLException
	{
		if (args.length == 0)
		{
			new BlogController("mongodb://localhost", "jdbc:postgresql://localhost/postgres");
		}
		else
		{
			new BlogController(args[0], args[1]);
		}
	}

	public BlogController(String mongoURIString, String pgURIString) throws IOException, SQLException
	{
		super(mongoURIString, pgURIString);
		initializeRoutes();
	}

	private void initializeRoutes() throws IOException
	{
		// this is the blog home page
		get(new FreemarkerBasedRoute("/", "blog_template.ftl")
		{
			@Override
			public void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{
				String username = sessionDAO.findUserNameBySessionId(getSessionCookie(request));

				List<DBObject> posts = blogPostDAO.findByDateDescending(10);
				SimpleHash root = new SimpleHash();

				root.put("myposts", posts);
				if (username != null)
				{
					root.put("username", username);
				}

				template.process(root, writer);
			}
		});

		// used to display actual blog post detail page
		get(new FreemarkerBasedRoute("/post/:id", "entry_template.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{
				String id = request.params(":id");

				System.out.println("/post: get " + id);

				DBObject post = blogPostDAO.findById(id);
				if (post == null)
				{
					response.redirect("/post_not_found");
				}
				else
				{
					// empty comment to hold new comment in form at bottom of blog entry detail page
					SimpleHash newComment = new SimpleHash();
					newComment.put("name", "");
					newComment.put("email", "");
					newComment.put("body", "");

					SimpleHash root = new SimpleHash();

					root.put("post", post);
					root.put("comment", newComment);

					template.process(root, writer);
				}
			}
		});

		// handle the signup post
		post(new FreemarkerBasedRoute("/signup", "signup.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{
				String email = request.queryParams("email");
				String username = request.queryParams("username");
				String password = request.queryParams("password");
				String verify = request.queryParams("verify");

				HashMap<String, String> root = new HashMap<String, String>();
				root.put("username", StringEscapeUtils.escapeHtml4(username));
				root.put("email", StringEscapeUtils.escapeHtml4(email));

				if (validateSignup(username, password, verify, email, root))
				{
					// good user
					System.out.println("Signup: Creating user with: " + username + " " + password);
					if (!userDAO.addUser(username, password, email))
					{
						// duplicate user
						root.put("username_error", "Username already in use, Please choose another");
						template.process(root, writer);
					}
					else
					{
						// good user, let's start a session
						String sessionID = sessionDAO.startSession(username);
						System.out.println("Session ID is" + sessionID);

						response.raw().addCookie(new Cookie("session", sessionID));
						response.redirect("/welcome");
					}
				}
				else
				{
					// bad signup
					System.out.println("User Registration did not validate");
					template.process(root, writer);
				}
			}
		});

		// present signup form for blog
		get(new FreemarkerBasedRoute("/signup", "signup.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer)
					throws IOException, TemplateException
			{

				SimpleHash root = new SimpleHash();

				// initialize values for the form.
				root.put("username", "");
				root.put("password", "");
				root.put("email", "");
				root.put("password_error", "");
				root.put("username_error", "");
				root.put("email_error", "");
				root.put("verify_error", "");

				template.process(root, writer);
			}
		});

		// will present the form used to process new blog posts
		get(new FreemarkerBasedRoute("/newpost", "newpost_template.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{

				// get cookie
				String username = sessionDAO.findUserNameBySessionId(getSessionCookie(request));

				if (username == null)
				{
					// looks like a bad request. user is not logged in
					response.redirect("/login");
				}
				else
				{
					SimpleHash root = new SimpleHash();
					root.put("username", username);

					template.process(root, writer);
				}
			}
		});

		// handle the new post submission
		post(new FreemarkerBasedRoute("/newpost", "newpost_template.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer)
					throws IOException, TemplateException
			{

				String title = StringEscapeUtils.escapeHtml4(request.queryParams("subject"));
				String post = StringEscapeUtils.escapeHtml4(request.queryParams("body"));
				String tags = StringEscapeUtils.escapeHtml4(request.queryParams("tags"));

				String username = sessionDAO.findUserNameBySessionId(getSessionCookie(request));

				if (username == null)
				{
					response.redirect("/login"); // only logged in users can post to blog
				}
				else if (title.equals("") || post.equals(""))
				{
					// redisplay page with errors
					HashMap<String, String> root = new HashMap<String, String>();
					root.put("errors", "post must contain a title and blog entry.");
					root.put("subject", title);
					root.put("username", username);
					root.put("tags", tags);
					root.put("body", post);
					template.process(root, writer);
				}
				else
				{
					// extract tags
					ArrayList<String> tagsArray = extractTags(tags);

					// substitute some <p> for the paragraph breaks
					post = post.replaceAll("\\r?\\n", "<p>");

					String id = blogPostDAO.addPost(title, post, tagsArray, username);

					// now redirect to the blog id
					response.redirect("/post/" + id);
				}
			}
		});

		get(new FreemarkerBasedRoute("/welcome", "welcome.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{

				String cookie = getSessionCookie(request);
				String username = sessionDAO.findUserNameBySessionId(cookie);

				if (username == null)
				{
					System.out.println("welcome() can't identify the user, redirecting to signup");
					response.redirect("/signup");

				}
				else
				{
					SimpleHash root = new SimpleHash();

					root.put("username", username);

					template.process(root, writer);
				}
			}
		});

		// process a new comment
		post(new FreemarkerBasedRoute("/newcomment", "entry_template.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer)
					throws IOException, TemplateException
			{
				String name = StringEscapeUtils.escapeHtml4(request.queryParams("commentName"));
				String email = StringEscapeUtils.escapeHtml4(request.queryParams("commentEmail"));
				String body = StringEscapeUtils.escapeHtml4(request.queryParams("commentBody"));
				String id = request.queryParams("id");

				DBObject post = blogPostDAO.findById(id);
				if (post == null)
				{
					response.redirect("/post_not_found");
				}
				// check that comment is good
				else if (name.equals("") || body.equals(""))
				{
					// bounce this back to the user for correction
					SimpleHash root = new SimpleHash();
					SimpleHash comment = new SimpleHash();

					comment.put("name", name);
					comment.put("email", email);
					comment.put("body", body);
					root.put("comment", comment);
					root.put("post", post);
					root.put("errors", "Post must contain your name and an actual comment");

					template.process(root, writer);
				}
				else
				{
					blogPostDAO.addPostComment(name, email, body, id);
					response.redirect("/post/" + id);
				}
			}
		});

		// present the login page
		get(new FreemarkerBasedRoute("/login", "login.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{
				SimpleHash root = new SimpleHash();

				root.put("username", "");
				root.put("login_error", "");

				template.process(root, writer);
			}
		});

		// process output coming from login form. On success redirect folks to the welcome page
		// on failure, just return an error and let them try again.
		post(new FreemarkerBasedRoute("/login", "login.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{

				String username = request.queryParams("username");
				String password = request.queryParams("password");

				System.out.println("Login: User submitted: " + username + "  " + password);

				DBObject user = userDAO.validateLogin(username, password);

				if (user != null)
				{

					// valid user, let's log them in
					String sessionID = sessionDAO.startSession(user.get("_id").toString());

					if (sessionID == null)
					{
						response.redirect("/internal_error");
					}
					else
					{
						// set the cookie for the user's browser
						response.raw().addCookie(new Cookie("session", sessionID));

						response.redirect("/welcome");
					}
				}
				else
				{
					SimpleHash root = new SimpleHash();

					root.put("username", StringEscapeUtils.escapeHtml4(username));
					root.put("password", "");
					root.put("login_error", "Invalid Login");
					template.process(root, writer);
				}
			}
		});

		// Show the posts filed under a certain tag
		get(new FreemarkerBasedRoute("/tag/:thetag", "blog_template.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer)
					throws IOException, TemplateException
			{

				String username = sessionDAO.findUserNameBySessionId(getSessionCookie(request));
				SimpleHash root = new SimpleHash();

				String tag = StringEscapeUtils.escapeHtml4(request.params(":thetag"));
				List<DBObject> posts = blogPostDAO.findByTagDateDescending(tag);

				root.put("myposts", posts);
				if (username != null)
				{
					root.put("username", username);
				}

				template.process(root, writer);
			}
		});

		// will allow a user to click Like on a post
		post(new FreemarkerBasedRoute("/like", "entry_template.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{

				String id = request.queryParams("id");
				String commentOrdinalStr = request.queryParams("comment_ordinal");

				// look up the post in question

				int ordinal = Integer.parseInt(commentOrdinalStr);

				// TODO: check return or have checkSession throw
				String username = sessionDAO.findUserNameBySessionId(getSessionCookie(request));
				DBObject post = blogPostDAO.findById(id);

				// if post not found, redirect to post not found error
				if (post == null)
				{
					response.redirect("/post_not_found");
				}
				else
				{
					blogPostDAO.likePost(id, ordinal);

					response.redirect("/post/" + id);
				}
			}
		});

		// tells the user that the URL is dead
		get(new FreemarkerBasedRoute("/post_not_found", "post_not_found.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{
				SimpleHash root = new SimpleHash();
				template.process(root, writer);
			}
		});

		// allows the user to logout of the blog
		get(new FreemarkerBasedRoute("/logout", "signup.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{

				String sessionID = getSessionCookie(request);

				if (sessionID == null)
				{
					// no session to end
					response.redirect("/login");
				}
				else
				{
					// deletes from session table
					sessionDAO.endSession(sessionID);

					// this should delete the cookie
					Cookie c = getSessionCookieActual(request);
					c.setMaxAge(0);

					response.raw().addCookie(c);

					response.redirect("/login");
				}
			}
		});

		// used to process internal errors
		get(new FreemarkerBasedRoute("/internal_error", "error_template.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{
				SimpleHash root = new SimpleHash();

				root.put("error", "System has encountered an error.");
				template.process(root, writer);
			}
		});

		// present the analysis
		get(new FreemarkerBasedRoute("/analysis", "analysis.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException, SQLException
			{

				// get cookie
				String username = sessionDAO.findUserNameBySessionId(getSessionCookie(request));

				if (username == null)
				{
					// looks like a bad request. user is not logged in
					response.redirect("/login");
				}
				else
				{
					SimpleHash root = new SimpleHash();

					root.put("username", username);

					template.process(root, writer);
				}
			}

		});

		// used to process internal errors
		get(new FreemarkerBasedRoute("/internal_error", "error_template.ftl")
		{
			@Override
			protected void doHandle(Request request, Response response, Writer writer) throws IOException,
					TemplateException
			{
				SimpleHash root = new SimpleHash();

				root.put("error", "System has encountered an error.");
				template.process(root, writer);
			}
		});

		// return the execution time to find by date on MongoDB
		post(new Route("/byDateMongo")
		{
			@Override
			public Object handle(Request arg0, Response arg1)
			{
				Timer timer = new Timer();

				blogPostDAO.findByDateDescending();

				return timer.toString(true);
			}
		});

		// return the execution time to find by body on MongoDB
		post(new Route("/byBodyMongo")
		{
			@Override
			public Object handle(Request arg0, Response arg1)
			{
				Timer timer = new Timer();

				blogPostDAO.findByBody("postagem");

				return timer.toString(true);
			}
		});

		// return the execution time to find by commented user on MongoDB
		post(new Route("/byCommentedUserMongo")
		{
			@Override
			public Object handle(Request arg0, Response arg1)
			{
				Timer timer = new Timer();

				blogPostDAO.findByCommentedUser("autor comentario 1");

				return timer.toString(true);
			}
		});

		// return the execution time to find by date on PostgreSQL
		post(new Route("/byDatePg")
		{
			@Override
			public Object handle(Request arg0, Response arg1)
			{
				Timer timer = new Timer();

				try
				{
					blogPostDAOPg.findByDateDescending();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}

				return timer.toString(true);
			}
		});

		// return the execution time to find by body on PostgreSQL
		post(new Route("/byBodyPg")
		{
			@Override
			public Object handle(Request arg0, Response arg1)
			{
				Timer timer = new Timer();

				try
				{
					blogPostDAOPg.findByBody("postagem");
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}

				return timer.toString(true);
			}
		});

		// return the execution time to find by commented user on PostgreSQL
		post(new Route("/byCommentedUserPg")
		{
			@Override
			public Object handle(Request arg0, Response arg1)
			{
				Timer timer = new Timer();

				try
				{
					blogPostDAOPg.findByCommentedUser("autor comentario 1");
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}

				return timer.toString(true);
			}
		});

	}
}
