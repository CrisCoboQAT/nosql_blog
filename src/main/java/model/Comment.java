package model;

import java.util.Date;

public class Comment
{
	public Integer commentId;
	public String name;
	public String comment;
	public String email;
	public Date date;

	public Comment()
	{
	}

	public Comment(Integer commentId, String name, String comment, String email, Date date)
	{
		this.commentId = commentId;
		this.name = name;
		this.comment = comment;
		this.email = email;
		this.date = date;
	}
}
