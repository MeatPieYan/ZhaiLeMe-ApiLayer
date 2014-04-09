package models;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import com.avaje.ebean.Ebean;

/**
 * A User in the system.
 * Contains a user's basic information.
 */
@Entity 
@Table(name="user")
public class User extends Model {

  @Id
  public Long uid;
  @Constraints.Required
  public String name;
  
  private String email;
  private byte[] password;
  
  public String sweibo_uid;
  public String tweibo_uid;
  public String qq_uid;
  public String renren_uid;
  
  public Date created;
  public Date updated;
  public Date lastLogin;
  
  public User(){
	  this.created = new Date();
  }
  
  /**
   * New User object
   * @param emailAddress The user's email address.
   * @param password The user's password.
   * @param firstName The user's first name.
   * @param lastName The user's last name.
   */
  public User(String emailAddress, String password, String name) {
      setEmail(emailAddress);
      setPassword(password);
      this.name = name;
      this.created = new Date();
  }
  
  public String getEmail() {
      return email;
  }

  public void setEmail(String emailAddress) {
      this.email = emailAddress.toLowerCase();
  }

  public String getPassword() {
      return password.toString();
  }

  public void setPassword(String password) {
      this.password = getSha512(password);
  }
  
  /**
   * Encrypt (hash) the password.
   * @param value The unencrypted password.
   * @return The hashed password.
   */
  public static byte[] getSha512(String value) {
      try {
          value = value + "sOmE&sAlT";
          return MessageDigest.getInstance("SHA-512").digest(value.getBytes("UTF-8"));
      }
      catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
          throw new RuntimeException(e);
      }
  }

  public Date setLastLogin(){
      this.lastLogin = new Date();
      update();
      return this.lastLogin;
  }

  public Date getLastLogin(){
      return this.lastLogin;
  }
  
  public static Finder<Long, User> find = new Finder<Long, User>(
	  Long.class, User.class
  ); 
  
  /**
   * Find a user by their e-mail address and password. Used for logging in.
   * @param emailAddress The e-mail address of the user.
   * @param password The unencrypted password of the user.
   * @return The user, if found.
   */
  public static User findByEmailAddressAndPassword(String emailAddress, String password) {
	  return Ebean.find(User.class)
		.where()
			.eq("email", emailAddress)
			.eq("password", getSha512(password))
		.findUnique();
  }
}