package model;

import java.util.Date;
import java.util.List;

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

	public Post()
	{
	}

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
