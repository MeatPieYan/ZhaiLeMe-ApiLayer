package models;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.sun.corba.se.spi.ior.ObjectId;

/**
 * Created with IntelliJ IDEA.
 * User: Brian
 * Date: 17-10-13
 * Time: 18:20
 *
 * An Auth Token.
 * Auth tokens are stored in their own collection.
 * A user can have multiple auth tokens, for each time he logs in on a certain device.
 * Each token has a lifetime of 30 days, or will be deleted when the user logs out on that device.
 */
@Entity
public class AuthToken{

	@Id
    ObjectId id;

    @Indexed(unique = true, dropDups = true)
    protected String token;

    @Reference
    protected User user;
	
    private String ip;

    public AuthToken(){
    	super();
    }
    
    public AuthToken(String ip, User user) {
        this.token = UUID.randomUUID().toString();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 30);
        this.createdOn = new Date();
        this.expiresOn = cal.getTime();
        this.ip = ip;
        this.user = user;
    }

    public static Model.Finder<ObjectId, AuthToken> find(){
        return new Model.Finder<ObjectId, AuthToken>(ObjectId.class, AuthToken.class);
    }

    /**
     * Find an Auth token by the token string.
     *
     * @param authToken The token string.
     * @return The full authtoken with attached user.
     */
    public static AuthToken findByAuthToken(String authToken) {
        if (authToken == null) {
            return null;
        }

        try  {
            AuthToken tok = AuthToken.find().filter("token", authToken).get();

            if(tok != null){
                return tok;
            } else {
                return null;
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
