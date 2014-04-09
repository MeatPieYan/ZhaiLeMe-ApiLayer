package controllers;

import static play.mvc.Controller.request;
import static play.mvc.Controller.response;
import models.AuthToken;
import models.User;
import models.WrongLogin;
import play.data.Form;
import play.data.validation.Constraints;
import play.libs.F;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.SimpleResult;
import play.mvc.With;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The security controller is used for (preserving) user authentication.
 */
public class SecurityController extends Action.Simple {

    // The names for our auth tokens in the header.
    public final static String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
    public static final String AUTH_TOKEN = "authToken";

    /**
     * Every authenticated call will pass through this function to make sure the auth token exists.
     * @param ctx The http context.
     * @return The output of the original call, or unauthorized if authentication fails.
     * @throws Throwable
     */
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {

        String[] authTokenHeaderValues = ctx.request().headers().get(AUTH_TOKEN_HEADER);

        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {

            AuthToken token = AuthToken.findByAuthToken(authTokenHeaderValues[0]);
            if(token != null){
                User user = User.find.byId(token.userId);
                if (user != null) {
                    ctx.args.put("user", user);
                    return delegate.call(ctx);
                }
            }
        }
		return null;
//        return unauthorized("unauthorized");
    }

    /**
     * Get the logged in user from the http context.
     * @return The logged in User.
     */
    public static User getUser() {
        return (User)Http.Context.current().args.get("user");
    }

    /**
     * Log in the user and output the auth token to be used by the JS client.
     * @return Json with the auth token.
     */
    public static Result login() {
        Form<Login> loginForm = Form.form(Login.class).bindFromRequest();

        if (loginForm.hasErrors()) {
            return badRequest(loginForm.errorsAsJson());
        }

        Login login = loginForm.get();

        String clientIp = request().remoteAddress();

        // Check if the IP hasn't had 20 failed attempts, otherwise this user can wait.
        WrongLogin alreadyWrong = WrongLogin.findByIp(clientIp);
        if(alreadyWrong != null) {
            if(alreadyWrong.attempts > 20) {
                return forbidden("Too many failed login attempts. This is being logged :P");
            }
        }

        User user = User.findByEmailAddressAndPassword(login.emailAddress, login.password);

        if (user == null) {
            // Record the failed login
            if(alreadyWrong != null){
                alreadyWrong.attempts++;
                Ebean.save(alreadyWrong);
            } else {
                alreadyWrong = new WrongLogin(clientIp, "login");
                Ebean.save(alreadyWrong);
            }

            return unauthorized();
        }
        else {
            // set the last login date
            user.setLastLogin();

            // Log the login
//            Log log = new Log(user, "user_login");
//            log.insert();

            // create a new token
            AuthToken token = new AuthToken(clientIp, user.uid);
            Ebean.save(token);

            // Set the cookie, jsonify and return
            ObjectNode authTokenJson = Json.newObject();
            authTokenJson.put(AUTH_TOKEN, token.token);
            response().setCookie(AUTH_TOKEN, token.token);
            return ok(authTokenJson);
        }
    }

    /**
     * Log out the current user and remove that token.
     * @return A redirect to the root of the domain.
     */
    @With(SecurityController.class)
    public static Result logout() {
        response().discardCookie(AUTH_TOKEN);
        AuthToken token = AuthToken.findByAuthToken(response().getHeaders().get(AUTH_TOKEN_HEADER));
        Ebean.delete(token);
        return redirect("/");
    }

    /**
     * Needed for parsing the login form.
     */
    public static class Login {

        @Constraints.Required
        @Constraints.Email
        public String emailAddress;

        @Constraints.Required
        public String password;
    }

}
