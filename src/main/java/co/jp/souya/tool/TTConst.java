package co.jp.souya.tool;



public class TTConst {

	//デフォルト値と考えること
	//運用時値はDIで上書き設定する -> 設定：servlet-context.xml
	public static String PATH_GENERATESRC_OUTPUT="C:\\Temp\\";
	public static String ACTION_CLICK="click";
	public static String ACTION_SENDKEYS="sendKeys";
	public static String URL_API_BASE="http://localhost:8080/souya";
	public static String URL_UPDATE_TEST_RESULT="/api/updateTestResult";
	public static String URL_UPDATE_RESULT="/api/updateResult";
	public static String URL_RESET_TESTCASE="/api/resetTestCase";
	public static String URL_GENERATE_TESTCASE="/api/generateTestCase";
	public static String TEST_RESULT_OK="OK";
	public static String TEST_RESULT_NG="NG";
	public static String JOB_STATUS_START="登録済み";
	public static String JOB_STATUS_EXEC="実行中";
	public static String JOB_STATUS_FINISH="完了";


	//↓↓↓↓↓↓↓自動生成したセッター INJECTに必要↓↓↓↓↓↓↓↓

	public static void setPATH_GENERATESRC_OUTPUT(String pATH_GENERATESRC_OUTPUT) {
		PATH_GENERATESRC_OUTPUT = pATH_GENERATESRC_OUTPUT;
	}
	public static void setACTION_CLICK(String aCTION_CLICK) {
		ACTION_CLICK = aCTION_CLICK;
	}
	public static void setACTION_SENDKEYS(String aCTION_SENDKEYS) {
		ACTION_SENDKEYS = aCTION_SENDKEYS;
	}
	public static void setURL_API_BASE(String uRL_API_BASE) {
		URL_API_BASE = uRL_API_BASE;
	}
	public static void setURL_UPDATE_TEST_RESULT(String uRL_UPDATE_TEST_RESULT) {
		URL_UPDATE_TEST_RESULT = uRL_UPDATE_TEST_RESULT;
	}
	public static void setURL_UPDATE_RESULT(String uRL_UPDATE_RESULT) {
		URL_UPDATE_RESULT = uRL_UPDATE_RESULT;
	}
	public static void setURL_RESET_TESTCASE(String uRL_RESET_TESTCASE) {
		URL_RESET_TESTCASE = uRL_RESET_TESTCASE;
	}
	public static void setURL_GENERATE_TESTCASE(String uRL_GENERATE_TESTCASE) {
		URL_GENERATE_TESTCASE = uRL_GENERATE_TESTCASE;
	}
	public static void setTEST_RESULT_OK(String tEST_RESULT_OK) {
		TEST_RESULT_OK = tEST_RESULT_OK;
	}
	public static void setTEST_RESULT_NG(String tEST_RESULT_NG) {
		TEST_RESULT_NG = tEST_RESULT_NG;
	}
	public static void setJOB_STATUS_START(String jOB_STATUS_START) {
		JOB_STATUS_START = jOB_STATUS_START;
	}
	public static void setJOB_STATUS_EXEC(String jOB_STATUS_EXEC) {
		JOB_STATUS_EXEC = jOB_STATUS_EXEC;
	}
	public static void setJOB_STATUS_FINISH(String jOB_STATUS_FINISH) {
		JOB_STATUS_FINISH = jOB_STATUS_FINISH;
	}



}