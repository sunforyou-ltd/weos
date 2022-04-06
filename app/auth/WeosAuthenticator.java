package auth;

import controllers.BaseController;
import play.cache.Cache;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security.Authenticator;

/**
 * 認証チェックコントローラー
 * @author kimura
 *
 */
public class WeosAuthenticator extends Authenticator {

	/**
	 * システムキー文字列
	 */
	public static final String WEOSAPPKEY = "WeoSAppKeyString";

    /** 未認証状態でアクセスされたときのアクション */
    @Override
    public Result onUnauthorized(Http.Context pContext)
    {
        // ログインページへジャンプします
        return redirect(BaseController.URL_PREFIX);
    }

    @Override
    /**
     * ユーザIDを取得する
     */
    public String getUsername(Http.Context pContext)
    {
        final Http.Cookie userCookie = pContext.request().cookie(WEOSAPPKEY);	// クッキーを取得する
        if (userCookie == null) return null;    								// ログインクッキーなし

        final String userToken = userCookie.value();							// クッキーよりログイン情報を取得
        final Object userInfo = Cache.get(userToken + ".userInfo");				// キャッシュよりログイン情報を取得
        if (!(userInfo instanceof String)){										// キャッシュにログイン情報が存在しない場合
        	pContext.response().discardCookie(WEOSAPPKEY); 						// すでにキャッシュにないのでクッキーも破棄
            return null;
        }

        // アクセスのたびにログイン情報登録をリフレッシュする
        registerLoginSession(pContext, userToken, userInfo);

        return (String)userInfo;
    }
    /**
     * セッション情報を登録する
     * @param context
     * @param userToken
     * @param userInfo
     */
    public static void registerLoginSession(Http.Context pContext, String userToken, Object userInfo)
    {
        // アプリケーションキャッシュの有効期限を今から30分後に
        Cache.set(userToken + ".userInfo", userInfo, 60 * 30);
        // ログインクッキーの有効期限を今から7日後に
        pContext.response().setCookie(WEOSAPPKEY, userToken, 60*60*24*7);
    }
    /**
     * セッション情報を削除する
     * @param context
     */
	public static void removeLoginSession(Http.Context pContext)
	{
	    final Http.Cookie userCookie = pContext.request().cookie(WEOSAPPKEY);
	    if (userCookie == null) return;
	    // アプリケーションキャッシュからログイン状態を削除する
	    Cache.remove(userCookie.value() + ".userInfo");
	    // ログインクッキーを削除させる
	    pContext.response().discardCookie(WEOSAPPKEY);
	}
    /**
     * セッション情報内に格納されているユーザＩＤを取得する
     * @param context
     */
	public static String getSessionId(Http.Context pContext)
	{
        final Http.Cookie userCookie = pContext.request().cookie(WEOSAPPKEY);	// クッキーを取得する
        if (userCookie == null) return null;    								// ログインクッキーなし

        final String userToken = userCookie.value();							// クッキーよりログイン情報を取得
        final Object userInfo = Cache.get(userToken + ".userInfo");				// キャッシュよりログイン情報を取得
        if (!(userInfo instanceof String)){										// キャッシュにログイン情報が存在しない場合
        	pContext.response().discardCookie(WEOSAPPKEY); 						// すでにキャッシュにないのでクッキーも破棄
            return null;
        }
        return (String)userInfo;
	}
}