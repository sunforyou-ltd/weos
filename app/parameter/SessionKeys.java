package parameter;


/**
 * 見積検索条件パラメータ
 * @author kimura
 *
 */
public class SessionKeys {

	/**
	 * 見積検索モード
	 * @author kimura
	 *
	 */
	public class Mode {
		/**
		 * 見積検索
		 */
		public static final String Retrieval = "Retrieval";
		/**
		 * 施工完了明細入力
		 */
		public static final String SerialNumber = "SerialNumber";
		/**
		 * 施工完了明細入力
		 */
		public static final String MakeDenpyo = "MakeDenpyo";
	}

    /**
     * 見積番号
     */
    public String mtmno = "";
    /**
     * 枝番
     */
    public long edaban = 0;
    /**
     * 見積検索モード
     */
    public String mode = "";
}