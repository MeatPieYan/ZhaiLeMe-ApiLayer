package models;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.db.ebean.Model.Finder;

import com.avaje.ebean.Ebean;

/**
 * An Auth Token.
 * Auth tokens are stored in their own collection.
 * A user can have multiple auth tokens, for each time he logs in on a certain device.
 * Each token has a lifetime of 30 days, or will be deleted when the user logs out on that device.
 */
@Entity
@Table(name="auth_token")
public class AuthToken{
	@Id
    public Long id;
	public String token;
	@Column(name="uid")
	public Long userId;
	public String ip;
	@Column(name="created")
	public Date createdOn;
	@Column(name="expires")
	public Date expiresOn;

    public AuthToken(){
    	Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        this.createdOn = new Date();
        this.expiresOn = cal.getTime();
    }
    
    public AuthToken(String ip, Long userId) {
        this.token = UUID.randomUUID().toString();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 30);
        this.createdOn = new Date();
        this.expiresOn = cal.getTime();
        this.ip = ip;
        this.userId = userId;
    }

    public static Finder<Long, AuthToken> find = new Finder<Long, AuthToken>(
    	Long.class, AuthToken.class
    ); 

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
            AuthToken tok = Ebean.find(AuthToken.class)
            		.where().eq("token", authToken)
            		.findUnique();
            
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

}
