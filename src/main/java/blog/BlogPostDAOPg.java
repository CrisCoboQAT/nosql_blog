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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlogPostDAOPg
{
	Statement st;

	public BlogPostDAOPg(final Statement st)
	{
		this.st = st;
	}

	public List<Post> findByCommentedUser(String user) throws SQLException
	{
		ResultSet rs =
				st.executeQuery("SELECT DISTINCT p.post_id, p.titulo, p.conteudo, p.data, u.nome, u.email FROM postagens p INNER JOIN usuarios u ON (u.user_id = p.user_id) INNER JOIN post_coments pc ON (p.post_id = pc.post_id) INNER JOIN comentarios c ON (pc.coment_id = c.coment_id AND c.nome like '%"
						+ user + "%')");
		List<Post> posts = new ArrayList<Post>();
		while (rs.next())
		{
			Post post =
					new Post(rs.getInt("post_id"), rs.getString("titulo"),
							rs.getString("conteudo"), rs.getDate("data"), rs.getString("nome"), rs.getString("email"));
			posts.add(post);
		}
		for (Post post : posts)
		{
			post.comments = findCommentsByPostId(post.postId);
			post.tags = findTagsByPostId(post.postId);
		}
		return posts;
	}

	public List<Post> findByBody(String body) throws SQLException
	{
		ResultSet rs =
				st.executeQuery("SELECT p.post_id, p.titulo, p.conteudo, p.data, u.nome, u.email FROM postagens p INNER JOIN usuarios u ON (u.user_id = p.user_id) WHERE p.conteudo like '%"
						+ body + "%'");
		List<Post> posts = new ArrayList<Post>();
		while (rs.next())
		{
			Post post =
					new Post(rs.getInt("post_id"), rs.getString("titulo"),
							rs.getString("conteudo"), rs.getDate("data"), rs.getString("nome"), rs.getString("email"));
			posts.add(post);
		}
		for (Post post : posts)
		{
			post.comments = findCommentsByPostId(post.postId);
			post.tags = findTagsByPostId(post.postId);
		}
		return posts;
	}

	public List<Post> findByDateDescending() throws SQLException
	{
		ResultSet rs =
				st.executeQuery("SELECT post_id, titulo, conteudo, data, nome, email FROM postagens p INNER JOIN usuarios u ON (p.user_id = u.user_id) ORDER BY data DESC");
		List<Post> posts = new ArrayList<Post>();
		while (rs.next())
		{
			Post post =
					new Post(rs.getInt("post_id"), rs.getString("titulo"),
							rs.getString("conteudo"), rs.getDate("data"), rs.getString("nome"), rs.getString("email"));
			posts.add(post);
		}
		for (Post post : posts)
		{
			post.comments = findCommentsByPostId(post.postId);
			post.tags = findTagsByPostId(post.postId);
		}
		return posts;
	}

	private List<Comment> findCommentsByPostId(Integer postId) throws SQLException
	{
		ResultSet rs =
				st.executeQuery("SELECT c.coment_id, nome, comentario, data, email FROM comentarios c INNER JOIN post_coments pc ON (post_id = "
						+ postId + " AND c.coment_id = pc.coment_id)");
		List<Comment> comments = new ArrayList<Comment>();
		while (rs.next())
		{
			Comment comment =
					new Comment(rs.getInt("coment_id"), rs.getString("nome"), rs.getString("comentario"),
							rs.getString("email"), rs.getDate("data"));
			comments.add(comment);
		}
		return comments;
	}

	private List<Tag> findTagsByPostId(Integer postId) throws SQLException
	{
		ResultSet rs =
				st.executeQuery("SELECT t.tag_id, nome FROM tags t INNER JOIN post_tags pt ON (post_id = " + postId
						+ " AND t.tag_id = pt.tag_id)");
		List<Tag> tags = new ArrayList<Tag>();
		while (rs.next())
		{
			Tag tag =
					new Tag(rs.getInt("tag_id"), rs.getString("nome"));
			tags.add(tag);
		}
		return tags;
	}

	public class Post
	{
		public Integer postId;
		public String title;
		public String body;
		public Date date;
		public String author;
		public String email;
		public List<Comment> comments;
		public List<Tag> tags;

		public Post(Integer postId, String title, String body, Date date, String author, String email)
		{
			this.postId = postId;
			this.title = title;
			this.body = body;
			this.date = date;
			this.author = author;
			this.email = email;
		}
	}

	public class Comment
	{
		public Integer commentId;
		public String name;
		public String comment;
		public String email;
		public Date date;

		public Comment(Integer commentId, String name, String comment, String email, Date date)
		{
			this.commentId = commentId;
			this.name = name;
			this.comment = comment;
			this.email = email;
			this.date = date;
		}
	}

	public class Tag
	{
		public Integer tagId;
		public String name;

		public Tag(Integer tagId, String name)
		{
			this.tagId = tagId;
			this.name = name;
		}
	}

}
