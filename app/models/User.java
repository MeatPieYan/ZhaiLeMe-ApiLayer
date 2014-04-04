package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity 
public class User extends Model {

  @Id
  public Long uid;
  @Constraints.Required
  public String name;
  public String pass;
  public String mail;
  public String sweibo_uid;
  public String tweibo_uid;
  public String qq_uid;
  public String renren_uid;

  public User(){
	  
  }
  
  public static Finder<Long,User> find = new Finder<Long,User>(
		  Long.class, User.class
  ); 

}