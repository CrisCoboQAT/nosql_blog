package dao;

import java.util.List;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class BlogPostDAO
{
	DBCollection postsCollection;

	public BlogPostDAO(final DB blogDatabase)
	{
		postsCollection = blogDatabase.getCollection("posts");
	}

	public DBObject findById(String id)
	{
		DBObject post = postsCollection.findOne(new BasicDBObject("_id", new ObjectId(id)));

		// fix up if a post has no likes
		if (post != null)
		{
			List<DBObject> comments = (List<DBObject>)post.get("comments");
			for (DBObject comment : comments)
			{
				if (!comment.containsField("num_likes"))
				{
					comment.put("num_likes", 0);
				}
			}
		}
		return post;
	}

	public List<DBObject> findByDateDescending(int limit)
	{
		DBCursor cursor = postsCollection.find().sort(new BasicDBObject().append("date", -1)).limit(limit);

		return convertResult(cursor);
	}

	public List<DBObject> findByDateDescending()
	{
		DBCursor cursor = postsCollection.find().sort(new BasicDBObject("date", -1));

		return convertResult(cursor);
	}

	public List<DBObject> findByBody(String term)
	{
		final Pattern regex = Pattern.compile(term, Pattern.CASE_INSENSITIVE);

		BasicDBObject regexExpr = new BasicDBObject("$regex", regex);

		DBCursor cursor = postsCollection.find(new BasicDBObject("body", regexExpr));

		return convertResult(cursor);
	}

	public List<DBObject> findByCommentedUser(String user)
	{
		BasicDBObject regexExpr = new BasicDBObject("$regex", user);

		DBCursor cursor = postsCollection.find(new BasicDBObject("comments.author", regexExpr));

		return convertResult(cursor);
	}

	public List<DBObject> findByTagDateDescending(final String tag)
	{
		BasicDBObject query = new BasicDBObject("tags", tag);

		DBCursor cursor = postsCollection.find(query).sort(new BasicDBObject().append("date", -1)).limit(10);

		return convertResult(cursor);
	}

	public String addPost(String title, String body, List tags, String username)
	{

		System.out.println("inserting blog entry " + title + " " + body);

		BasicDBObject post = new BasicDBObject("title", title);
		post.append("author", username);
		post.append("body", body);
		post.append("tags", tags);
		post.append("comments", new java.util.ArrayList());
		post.append("date", new java.util.Date());

		try
		{
			WriteResult result = postsCollection.insert(post);
			System.out.println("Inserting blog post");
		}
		catch (Exception e)
		{
			System.out.println("Error inserting post");
			return null;
		}

		return post.get("_id").toString();
	}

	public void addPostComment(final String name, final String email, final String body, final String id)
	{
		BasicDBObject comment = new BasicDBObject("author", name).append("body", body);
		if (email != null && !email.equals(""))
		{
			comment.append("email", email);
		}

		postsCollection.update(new BasicDBObject("_id", new ObjectId(id)),
				new BasicDBObject("$push", new BasicDBObject("comments", comment)), false, false);
	}

	public void likePost(final String id, final int ordinal)
	{
		postsCollection.update(new BasicDBObject("_id", new ObjectId(id)),
				new BasicDBObject("$inc", new BasicDBObject("comments." + ordinal + ".num_likes", 1)));
	}

	private List<DBObject> convertResult(DBCursor cursor)
	{
		List<DBObject> posts;
		try
		{
			posts = cursor.toArray();
		}
		finally
		{
			cursor.close();
		}

		return posts;
	}
}
