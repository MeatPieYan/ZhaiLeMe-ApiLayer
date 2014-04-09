package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.db.ebean.Model.Finder;

import com.avaje.ebean.Ebean;

/**
* Used to detect brute force attacks on login.
*/
@Entity
@Table(name="wrong_login")
public class WrongLogin {
	@Id
	public Long id;
    public String ip;
    public Date created;
    public long attempts;
    //To indicate if it's a wrong login or password reset.
    public String type;
    
    public WrongLogin(){
        // Needed to load WrongLogins
    }

    public WrongLogin(String ip) {
        this.ip = ip;
        created = new Date();
        attempts = 1;
    }
    
    public WrongLogin(String ip, String type) {
        this.ip = ip;
        created = new Date();
        attempts = 1;
        this.type = type;
    }

    public static Finder<Long, WrongLogin> find = new Finder<Long, WrongLogin>(
	    Long.class, WrongLogin.class
	); 
    
    /**
     * Get a WrongLogin by IP.
     * @param ip The IP address.
     * @return The WrongLogin object, if found.
     */
    public static WrongLogin findByIp(String ip){
        return Ebean.find(WrongLogin.class)
        		.where().eq("ip", ip)
        		.findUnique();
    }
}
