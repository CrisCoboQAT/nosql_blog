package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Comment;
import model.Post;
import model.Tag;

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

}
