package controllers;

import java.util.UUID;

import models.MstUser;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import views.html.login;
import auth.WeosAuthenticator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * ログインコントローラー
 * @author kimura
 *
 */
public class LoginController extends BaseController {

	/**
	 * ログイン画面遷移
	 * @return ログイン画面レンダー
	 */
    public static Result move() {
        return ok(login.render());
    }
    /**
     * ログイン認証
     * @return
     */
    public static Result login() {

        ObjectNode resultJson = Json.newObject();

    	//------------------------------------------------------------------------------------
    	//- パラメータの取得
    	//------------------------------------------------------------------------------------
        JsonNode 	inputParameter 	= request().body().asJson();
        String userid = inputParameter.get("userid").asText();
        String password = inputParameter.get("password").asText();

    	//------------------------------------------------------------------------------------
    	//- ユーザＩＤの認証
    	//------------------------------------------------------------------------------------
        MstUser user = MstUser.find.where().eq("user_id", userid).findUnique();

        if (user == null) {
            resultJson.put("result", "usernotfound");
            Logger.warn("USER ID NOT EXISTS. ID={}",userid);
        }
        else {

        	if (!user.password.equals(password)) {
                resultJson.put("result", "passworderror");
                Logger.warn("USER PASSWORD UNMATCH. ID={}",userid);
        	}
        	else {

        		//ログインセッション情報の作成
        		final String userToken = UUID.randomUUID().toString(); // ランダムトークン作成
        		WeosAuthenticator.registerLoginSession(ctx(), userToken, user.userId);

            Logger.info("USER ACCEPTED LOGIN. ID={} ",userid);
            resultJson.put("result", "success");
        	}
        }
        return ok(resultJson);
    }
    /**
     * ログアウト
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result logout() {

        ObjectNode resultJson = Json.newObject();

        getSessionUser();
        if (user != null) {
            Logger.info("USER LOGOUT. ID={} ",user.userId);
        }
        //ログインセッションの破棄
        WeosAuthenticator.removeLoginSession(ctx());

        resultJson.put("result", "success");

        return ok(resultJson);
    }
}
