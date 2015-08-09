/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package blog;

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
		List<DBObject> posts;
		DBCursor cursor = postsCollection.find().sort(new BasicDBObject().append("date", -1)).limit(limit);
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

	public DBCursor findByDateDescending()
	{
		DBCursor cursor = postsCollection.find().sort(new BasicDBObject("date", -1));

		return cursor;
	}

	public DBCursor findByBody(String term)
	{
		final Pattern regex = Pattern.compile(term, Pattern.CASE_INSENSITIVE);

		BasicDBObject regexExpr = new BasicDBObject("$regex", regex);

		DBCursor cursor = postsCollection.find(new BasicDBObject("body", regexExpr));

		return cursor;
	}

	public DBCursor findByCommentedUser(String user)
	{
		BasicDBObject regexExpr = new BasicDBObject("$regex", user);

		DBCursor cursor = postsCollection.find(new BasicDBObject("comments.author", regexExpr));

		return cursor;
	}

	public List<DBObject> findByTagDateDescending(final String tag)
	{
		List<DBObject> posts;
		BasicDBObject query = new BasicDBObject("tags", tag);
		System.out.println("/tag query: " + query.toString());
		DBCursor cursor = postsCollection.find(query).sort(new BasicDBObject().append("date", -1)).limit(10);
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

		System.out.println("************" + post.get("_id"));

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
}
