package dao;

import java.security.SecureRandom;

import sun.misc.BASE64Encoder;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class SessionDAO
{
	private final DBCollection sessionsCollection;

	public SessionDAO(final DB blogDatabase)
	{
		sessionsCollection = blogDatabase.getCollection("sessions");
	}

	public String findUserNameBySessionId(String sessionId)
	{
		DBObject session = getSession(sessionId);

		if (session == null)
		{
			return null;
		}
		else
		{
			return session.get("username").toString();
		}
	}

	// starts a new session in the sessions table
	public String startSession(String username)
	{

		// get 32 byte random number. that's a lot of bits.
		SecureRandom generator = new SecureRandom();
		byte randomBytes[] = new byte[32];
		generator.nextBytes(randomBytes);

		BASE64Encoder encoder = new BASE64Encoder();

		String sessionID = encoder.encode(randomBytes);

		// build the BSON object
		BasicDBObject session = new BasicDBObject("username", username);

		session.append("_id", sessionID);

		sessionsCollection.insert(session);

		return session.getString("_id");
	}

	// ends the session by deleting it from the sesisons table
	public void endSession(String sessionID)
	{
		sessionsCollection.remove(new BasicDBObject("_id", sessionID));
	}

	// retrieves the session from the sessions table
	public DBObject getSession(String sessionID)
	{
		return sessionsCollection.findOne(new BasicDBObject("_id", sessionID));
	}
}
