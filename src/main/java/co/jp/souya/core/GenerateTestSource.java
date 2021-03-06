package co.jp.souya.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.jp.souya.jpa.DisplayAdmin;
import co.jp.souya.jpa.InputPattern;
import co.jp.souya.jpa.MovePatternAdmin;
import co.jp.souya.jpa.MovePatternDetail;
import co.jp.souya.jpa.ParametaValue;
import co.jp.souya.jpa.ProjectAdmin;
import co.jp.souya.jpa.TestCaseAdmin;
import co.jp.souya.service.DaoSvc;
import co.jp.souya.tool.TTConst;

@Service
public class GenerateTestSource {
	private static final Logger logger = LoggerFactory
			.getLogger(GenerateTestSource.class);

	private String sep = System.lineSeparator();

	public GenerateTestSource() {
		logger.info(this.getClass().getName());
	}

	@Autowired
	private DaoSvc daoSvc;


	/**
	 * Git Pushをする
	 * @return
	 */
	public boolean gitpush() {
		boolean result = false;

		try {
			Runtime r = Runtime.getRuntime();
			Process process = r.exec(TTConst.PATH_GITPUSHSCRIPT);

			InputStream is = process.getInputStream();	//標準出力
			printInputStream(is);
			InputStream es = process.getErrorStream();	//標準エラー
			printInputStream(es);
			process.waitFor();
			int ret = process.exitValue();
			logger.info("戻り値：" + ret);

			result = true;

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return result;
	}

	private void printInputStream(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			for (;;) {
				String line = br.readLine();
				if (line == null) break;
//				System.out.println(line);
				logger.info(line);
			}
		} finally {
			br.close();
		}
	}

	/**
	 * 指定された設定でユニットテストコードを自動生成する
	 * @param id
	 * @param input_ids
	 * @return
	 */
	public boolean generate(Integer id, List<Integer> input_ids) {
		boolean result = false;

		try{
			// ----------------必要情報取得----------------
			// TODO: EntityRelationから自動取得を実装できればよい
			TestCaseAdmin daoテストケース管理
			= daoSvc.getTestCaseAdmin(id);
			MovePatternAdmin dao遷移パターン管理
			= daoSvc.getMovePatternAdmin(daoテストケース管理.get遷移パターン管理id());
			DisplayAdmin dao画面管理
			= daoSvc.getDisplayAdmin(daoテストケース管理.get画面管理id());
			ProjectAdmin daoプロジェクト管理
			= daoSvc.getProjectAdmin(dao画面管理.getプロジェクトid());
			//TODO:順に取得していることを確認
			List<MovePatternDetail> dao遷移パターン明細リスト
			= daoSvc.getMovePatternDetailList(dao遷移パターン管理.getId());
			List<InputPattern> dao入力パターンリスト = new ArrayList<InputPattern>();
			for (Integer id_input : input_ids) {
				InputPattern dao = daoSvc.getInputPattern(id_input);
				dao入力パターンリスト.add(dao);
			}


			// ----------------クラス構築----------------
			String strGenerateCls = getHinagata();
			String strプロジェクト名;
			{
				//プロジェクト名生成
				strプロジェクト名 = "project" + daoプロジェクト管理.getId();
				strGenerateCls = strGenerateCls.replace(
						"co.jp.souya.prototype",
						"co.jp.souya." + strプロジェクト名
						);
			}
			String strクラス名;
			{
				//クラス名生成
				long nケース管理番号 = 10000*daoプロジェクト管理.getId()
						+1*daoテストケース管理.getId();
				strクラス名 = "Case" + nケース管理番号;
				strGenerateCls = strGenerateCls.replace(
						"SeleniumHinagata",
						strクラス名
						);
			}
			{
				//初期化 -> 置換
				//TODO:実装
			}
			{
				//DB初期化 -> 置換
				//TODO:実装
			}
			{
				//画面遷移 -> 置換
				StringBuffer strReplace = new StringBuffer();
				strReplace.append("//画面遷移");
				strReplace.append(sep);
				@SuppressWarnings("unused")
				boolean doInitialURL = false;
				for (MovePatternDetail movePatternDetail : dao遷移パターン明細リスト) {
					String url = movePatternDetail.getUrl();
					strReplace.append("		//" + movePatternDetail.get画面タイトル());
					strReplace.append(sep);
//					if(url!=null && !"".equals(url) && !doInitialURL){
					if(url!=null && !"".equals(url)){
						strReplace.append("		webdriver.get(\"" + url + "\");");
						strReplace.append(sep);
						doInitialURL = true;
					}
					//TODO:実行順に取得していることを確認
					List<ParametaValue> daoパラメタ値リスト=
					daoSvc.getParametaValueList(movePatternDetail.get入力パターンid());
					for (ParametaValue parametaValue : daoパラメタ値リスト) {
						strReplace.append(stackParameters(parametaValue));
					}
				}
				strGenerateCls = strGenerateCls.replace(
						"//画面遷移",
						strReplace.toString()
						);
			}
			{
				//テストケース -> 置換
				StringBuffer strReplace = new StringBuffer();
				strReplace.append("	//テストケース開始");
				strReplace.append(sep);
				for (InputPattern inputPattern : dao入力パターンリスト) {
					strReplace.append("	@Test");
					strReplace.append(sep);
//					strReplace.append("	public void Test" + inputPattern.getNo() + "() throws Exception{");
					strReplace.append("	public void Test" + inputPattern.getId() + "() throws Exception{");
					strReplace.append(sep);
					//---------実行---------
					{
						strReplace.append(sep);
						strReplace.append("		//実行");
						strReplace.append(sep);
						strReplace.append("		hndlsNow = webdriver.getWindowHandles();");
						strReplace.append(sep);
						strReplace.append("		boolean bTestResult = true;");
						strReplace.append(sep);
						//TODO:実行順に取得していることを確認
						List<ParametaValue> daoパラメタ値リスト=
						daoSvc.getParametaValueList(inputPattern.getId());
						for (ParametaValue parametaValue : daoパラメタ値リスト) {
							strReplace.append(stackParameters(parametaValue));
						}
					}
					//---------実行直後がアラートダイアログ状態かどうかチェック---------
					{
						strReplace.append(sep);
						strReplace.append("		// 実行後アラートダイアログチェック");
						strReplace.append(sep);
						strReplace.append("		Alert alert = null;");
						strReplace.append(sep);
						strReplace.append("		try{");
						strReplace.append(sep);
						strReplace.append("			alert = webdriver.switchTo().alert();");
						strReplace.append(sep);
						strReplace.append("		}catch(Exception e){");
						strReplace.append(sep);
						strReplace.append("			System.out.println(\"no alert\");");
						strReplace.append(sep);
						strReplace.append("		}");
						strReplace.append(sep);
						strReplace.append("");
						strReplace.append(sep);
					}
					//---------実行後スナップショット取得---------
					{
						strReplace.append(sep);
						strReplace.append("		// 実行後スナップショット取得");
						strReplace.append(sep);
						strReplace.append("		String strSnapshot = \"\";");
						strReplace.append(sep);
						strReplace.append("		if (alert == null) {");
						strReplace.append(sep);
						strReplace.append("			strSnapshot = tryGetPicture();");
						strReplace.append(sep);
						strReplace.append("		}");
						strReplace.append(sep);
						strReplace.append("");
						strReplace.append(sep);
					}
					//---------WEB状態取得・比較---------
					{
						strReplace.append(sep);
						strReplace.append("		//web状態取得・比較");
						strReplace.append(sep);
						strReplace.append("		String strResultWeb = \"\";");
						strReplace.append(sep);
						strReplace.append("		if(alert==null){");
						strReplace.append(sep);
						strReplace.append("			strResultWeb = webdriver.getPageSource();");
						strReplace.append(sep);
						strReplace.append("		}else{");
						strReplace.append(sep);
						strReplace.append("			strResultWeb = alert.getText();");
						strReplace.append(sep);
						strReplace.append("		}");
						strReplace.append(sep);
						strReplace.append("");
						strReplace.append(sep);
					}
					//---------DB状態取得・比較---------
					{
						strReplace.append(sep);
						strReplace.append("		//DB状態取得・比較");
						strReplace.append(sep);
						strReplace.append("		String strResultDB = \"\";");
						strReplace.append(sep);
					}
					//---------JOB状況更新---------
					{
						strReplace.append(sep);
						strReplace.append("		// 結果更新");
						strReplace.append(sep);
						strReplace.append("		try {");
						strReplace.append(sep);
						strReplace.append("			URI url = new URI(\"" + TTConst.URL_API_BASE + TTConst.URL_UPDATE_TEST_RESULT + "\");");
						strReplace.append(sep);
						strReplace.append("			JSONObject request = new JSONObject();");
						strReplace.append(sep);
						strReplace.append("			request.put(\"id\", " + inputPattern.getId() + ");");
						strReplace.append(sep);
						strReplace.append("			request.put(\"html\", URLEncoder.encode(strResultWeb, \"UTF-8\"));");
						strReplace.append(sep);
						strReplace.append("			request.put(\"db\", URLEncoder.encode(strResultDB, \"UTF-8\"));");
						strReplace.append(sep);
						strReplace.append("			request.put(\"snapshot\", strSnapshot);");
						strReplace.append(sep);
						strReplace.append("			request.put(\"errList\", URLEncoder.encode(errList.toString(), \"UTF-8\"));");
						strReplace.append(sep);
						strReplace.append("");
						strReplace.append(sep);
						strReplace.append("			HttpEntity<String> entity = new HttpEntity<String>(");
						strReplace.append(sep);
						strReplace.append("					request.toString(), headers);");
						strReplace.append(sep);
						strReplace.append("");
						strReplace.append(sep);
						strReplace.append("			System.out.println(\"URL: \" + url);");
						strReplace.append(sep);
						strReplace.append("			String response = restTemplate.postForObject(url, entity,");
						strReplace.append(sep);
						strReplace.append("					String.class);");
						strReplace.append(sep);
						strReplace.append("			System.out.println(\"Response: \" + response);");
						strReplace.append(sep);
						strReplace.append("");
						strReplace.append(sep);
						strReplace.append("		} catch (Exception e) {");
						strReplace.append(sep);
						strReplace.append("			e.printStackTrace();");
						strReplace.append(sep);
						strReplace.append("			assertTrue(false);");
						strReplace.append(sep);
						strReplace.append("		}");
						strReplace.append(sep);
						strReplace.append("");
						strReplace.append(sep);
						strReplace.append("");
						strReplace.append(sep);
						strReplace.append("");
						strReplace.append(sep);
					}
					strReplace.append("	}");
					strReplace.append(sep);
				}
				strReplace.append("	//テストケース終了");
				strReplace.append(sep);
				strGenerateCls = strGenerateCls.replace(
						"//テストケース",
						strReplace.toString()
						);
			}

			logger.debug(strGenerateCls);

			try {
				//クラスファイル出力
				FileWriter fw = new FileWriter(TTConst.PATH_GENERATESRC_OUTPUT + strプロジェクト名 + File.separator + strクラス名 +  ".java");
				fw.write(strGenerateCls);
				fw.close();

				result = true;
			} catch (IOException ioe) {
				logger.error(ioe.getMessage(),ioe);
			}

		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}

		return result;
	}

	/**
	 * ユニットテストコードを削除する(物理削除)
	 * @param id
	 * @return
	 */
	public boolean ungenerate(Integer id) {
		boolean result = false;

		try{
			// ----------------必要情報取得----------------
			// TODO: EntityRelationから自動取得を実装できればよい
			TestCaseAdmin daoテストケース管理
			= daoSvc.getTestCaseAdmin(id);
			DisplayAdmin dao画面管理
			= daoSvc.getDisplayAdmin(daoテストケース管理.get画面管理id());
			ProjectAdmin daoプロジェクト管理
			= daoSvc.getProjectAdmin(dao画面管理.getプロジェクトid());

			String strプロジェクト名;
			{
				//プロジェクト名生成
				strプロジェクト名 = "project" + daoプロジェクト管理.getId();
			}
			String strクラス名;
			{
				//クラス名生成
				long nケース管理番号 = 10000*daoプロジェクト管理.getId()
						+1*daoテストケース管理.getId();
				strクラス名 = "Case" + nケース管理番号;
			}

			//クラスファイル出力
			String fileName = TTConst.PATH_GENERATESRC_OUTPUT + strプロジェクト名 + File.separator + strクラス名 +  ".java";
			File file = new File(fileName);
			if(file.delete()){
				logger.info("ファイル削除しました file:" + fileName);
				result = true;
			}else{
				logger.info("ファイル削除できませんでした file:" + fileName);
				result = false;
			}

		}catch(Exception e){
			logger.error(e.getMessage(),e);
			result = false;
		}

		return result;
	}

	private StringBuffer stackParameters(ParametaValue parametaValue){
		//TODO:全体的に javaコードエスケープを処理する必要がある 例えば C:\Temp -> C:\\Temp としないとならない
		StringBuffer strReplace = new StringBuffer();
		if("別ウィンドウへ移動".equals(parametaValue.getエレメント型())){
			//固有処理
			strReplace.append("		{");
			strReplace.append(sep);
			strReplace.append("			move_anotherWindow();");
			strReplace.append(sep);
			strReplace.append("		}");
			strReplace.append(sep);
		}else if("クリックアラートOK".equals(parametaValue.getエレメント型())){
			//固有処理
			strReplace.append("		{");
			strReplace.append(sep);
			strReplace.append("			click_alertOK();");
			strReplace.append(sep);
			strReplace.append("			move_activeWindow();");
			strReplace.append(sep);
			strReplace.append("		}");
			strReplace.append(sep);
		}else if("クリックアラートNG".equals(parametaValue.getエレメント型())){
			//固有処理
			strReplace.append("		{");
			strReplace.append(sep);
			strReplace.append("			click_alertNG();");
			strReplace.append(sep);
			strReplace.append("			move_activeWindow();");
			strReplace.append(sep);
			strReplace.append("		}");
			strReplace.append(sep);
		}else{
			strReplace.append("		try{");
			strReplace.append(sep);
			strReplace.append("			//" + parametaValue.get項目名());
			strReplace.append(sep);

			strReplace.append("			strIdentifyName = \""
			+ "項目名:"+ parametaValue.get項目名() + ",実行順:" + parametaValue.get実行順() + ",エレメント名:" + parametaValue.getエレメント名() + ",アクション:" + parametaValue.getアクション()
			+"\";");
			strReplace.append(sep);


			strReplace
			.append("			List<WebElement> elements = webdriver.findElements("
					+ parametaValue.getエレメント型()
					+ "(\""
					+ esc(parametaValue.getエレメント名()) + "\"));");
			strReplace.append(sep);
			strReplace
			.append("			WebElement element = webdriver.findElement("
					+ parametaValue.getエレメント型()
					+ "(\""
					+ esc(parametaValue.getエレメント名()) + "\"));");
			strReplace.append(sep);
			if(TTConst.ACTION_SENDKEYS.equals(parametaValue.getアクション())){
				strReplace.append("			element.clear();");
				strReplace.append(sep);
				strReplace.append("			element.sendKeys(\"" + esc(parametaValue.get値()) + "\");");
				strReplace.append(sep);
			}else
			if(TTConst.ACTION_CLICK.equals(parametaValue.getアクション())){
				strReplace.append("			element.click();");
				strReplace.append(sep);
			}else
			if(TTConst.ACTION_SELECTBYINDEX.equals(parametaValue.getアクション())){
				strReplace.append("			Select select=new Select(element);");
				strReplace.append(sep);
				strReplace.append("			select.selectByIndex(" + esc(parametaValue.get値()) + ");");
				strReplace.append(sep);
			}else
			if(TTConst.ACTION_SELECTBYVALUE.equals(parametaValue.getアクション())){
				strReplace.append("			Select select=new Select(element);");
				strReplace.append(sep);
				strReplace.append("			select.selectByValue(\"" + esc(parametaValue.get値()) + "\");");
				strReplace.append(sep);
			}else
			if(TTConst.ACTION_SELECTBYVISIBLETEXT.equals(parametaValue.getアクション())){
				strReplace.append("			Select select=new Select(element);");
				strReplace.append(sep);
				strReplace.append("			select.selectByVisibleText(\"" + esc(parametaValue.get値()) + "\");");
				strReplace.append(sep);
			}else
			if(TTConst.ACTION_CLICK_RADIOBYINDEX.equals(parametaValue.getアクション())){
				strReplace.append("			elements.get(" + esc(parametaValue.get値()) + ").click();");
				strReplace.append(sep);
			}else
			if(TTConst.ACTION_CLICK_BYATTRVALUE.equals(parametaValue.getアクション())){
				strReplace.append("			for (WebElement webElement : elements) {");
				strReplace.append(sep);
				strReplace.append("				if(\"" + esc(parametaValue.get値()) + "\".equals(webElement.getAttribute(\"value\"))){");
				strReplace.append(sep);
				strReplace.append("					webElement.click();");
				strReplace.append(sep);
				strReplace.append("					break;");
				strReplace.append(sep);
				strReplace.append("				}");
				strReplace.append(sep);
				strReplace.append("			}");
				strReplace.append(sep);
			}
			strReplace.append("			Thread.sleep(300);");
			strReplace.append(sep);
			strReplace.append("		}catch(Exception e){errList.add(strIdentifyName+e.getMessage());}");
			strReplace.append(sep);
		}
		return strReplace;
	}

	private String esc(String str){
		//エスケープ for java
		String rtnStr = str;
		rtnStr = rtnStr.replace("\\", "\\\\");
		rtnStr = rtnStr.replace("\"", "\\\"");
		return rtnStr;
	}

	private String getHinagata(){
		StringBuffer strbuf = new StringBuffer();
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("package co.jp.souya.prototype;");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("import static org.junit.Assert.*;");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("import java.io.File;");
		strbuf.append(sep);
		strbuf.append("import java.io.UnsupportedEncodingException;");
		strbuf.append(sep);
		strbuf.append("import java.net.URI;");
		strbuf.append(sep);
		strbuf.append("import java.net.URLDecoder;");
		strbuf.append(sep);
		strbuf.append("import java.net.URLEncoder;");
		strbuf.append(sep);
		strbuf.append("import java.util.ArrayList;");
		strbuf.append(sep);
		strbuf.append("import java.util.List;");
		strbuf.append(sep);
		strbuf.append("import java.util.Set;");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("import org.json.JSONObject;");
		strbuf.append(sep);
		strbuf.append("import org.junit.After;");
		strbuf.append(sep);
		strbuf.append("import org.junit.AfterClass;");
		strbuf.append(sep);
		strbuf.append("import org.junit.Before;");
		strbuf.append(sep);
		strbuf.append("import org.junit.BeforeClass;");
		strbuf.append(sep);
		strbuf.append("import org.junit.Test;");
		strbuf.append(sep);
		strbuf.append("import org.openqa.selenium.Alert;");
		strbuf.append(sep);
		strbuf.append("import org.openqa.selenium.By;");
		strbuf.append(sep);
		strbuf.append("import org.openqa.selenium.OutputType;");
		strbuf.append(sep);
		strbuf.append("import org.openqa.selenium.TakesScreenshot;");
		strbuf.append(sep);
		strbuf.append("import org.openqa.selenium.WebDriver;");
		strbuf.append(sep);
		strbuf.append("import org.openqa.selenium.WebElement;");
		strbuf.append(sep);
		strbuf.append("import org.openqa.selenium.firefox.FirefoxDriver;");
		strbuf.append(sep);
		strbuf.append("import org.openqa.selenium.support.ui.Select;");
		strbuf.append(sep);
		strbuf.append("import org.springframework.http.HttpEntity;");
		strbuf.append(sep);
		strbuf.append("import org.springframework.http.HttpHeaders;");
		strbuf.append(sep);
		strbuf.append("import org.springframework.http.MediaType;");
		strbuf.append(sep);
		strbuf.append("import org.springframework.web.client.RestTemplate;");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("import co.jp.souya.tool.TTConst;");
		strbuf.append(sep);
		strbuf.append("import co.jp.souya.tool.TTUtility;");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("public class SeleniumHinagata {");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	public SeleniumHinagata() {");
		strbuf.append(sep);
		strbuf.append("	}");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	private static WebDriver webdriver;");
		strbuf.append(sep);
		strbuf.append("	private static HttpHeaders headers;");
		strbuf.append(sep);
		strbuf.append("	private static RestTemplate restTemplate;");
		strbuf.append(sep);
		strbuf.append("	private static Set<String> hndlsNow;");
		strbuf.append(sep);
		strbuf.append("	private static List<String> errList;");
		strbuf.append(sep);
		strbuf.append("	private static String strIdentifyName;");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	@BeforeClass");
		strbuf.append(sep);
		strbuf.append("	public static  void doBeforeCls(){");
		strbuf.append(sep);
		strbuf.append("		//初期化");
		strbuf.append(sep);
		strbuf.append("		headers = new HttpHeaders();");
		strbuf.append(sep);
		strbuf.append("		headers.setContentType(MediaType.APPLICATION_JSON);");
		strbuf.append(sep);
		strbuf.append("		restTemplate = new RestTemplate();");
		strbuf.append(sep);
		strbuf.append("		errList = new ArrayList<String>();");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("		//永続化マネージャの生成等");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	}");
		strbuf.append(sep);
		strbuf.append("	@Before");
		strbuf.append(sep);
		strbuf.append("	public void doBefore() throws Exception{");
		strbuf.append(sep);
		strbuf.append("		//DB初期化");
		strbuf.append(sep);
		strbuf.append("		//import用スクリプトをキックする、など要検討");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("    	webdriver = new FirefoxDriver();");
		strbuf.append(sep);
		strbuf.append("		//画面遷移");
		strbuf.append(sep);
		strbuf.append("	}");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	//テストケース");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	@After");
		strbuf.append(sep);
		strbuf.append("	public void doAfter(){");
		strbuf.append(sep);
		strbuf.append("		//エラーリストをクリア");
		strbuf.append(sep);
		strbuf.append("		errList.clear();");
		strbuf.append(sep);
		strbuf.append("		//WebDriver終了");
		strbuf.append(sep);
		strbuf.append("		webdriver.quit();");
		strbuf.append(sep);
		strbuf.append("	}");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	@AfterClass");
		strbuf.append(sep);
		strbuf.append("	public static void aftCls(){");
		strbuf.append(sep);
		strbuf.append("		//インスタンス破棄等");
		strbuf.append(sep);
		strbuf.append("	}");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	//他画面へ遷移する");
		strbuf.append(sep);
		strbuf.append("	private void move_anotherWindow() throws Exception{");
		strbuf.append(sep);
		strbuf.append("		Thread.sleep(500);");
		strbuf.append(sep);
		strbuf.append("		String hndlMain = webdriver.getWindowHandle();");
		strbuf.append(sep);
		strbuf.append("		Set<String> windowList = webdriver.getWindowHandles();");
		strbuf.append(sep);
		strbuf.append("		for (String hndlWnd : windowList) {");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("			if(!hndlMain.equals(hndlWnd)){");
		strbuf.append(sep);
		strbuf.append("				webdriver.switchTo().window(hndlWnd);");
		strbuf.append(sep);
		strbuf.append("				break;");
		strbuf.append(sep);
		strbuf.append("			}");
		strbuf.append(sep);
		strbuf.append("		}");
		strbuf.append(sep);
		strbuf.append("		Thread.sleep(500);");
		strbuf.append(sep);
		strbuf.append("	}");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	//アラートをOKクリックする");
		strbuf.append(sep);
		strbuf.append("	private void click_alertOK() throws Exception{");
		strbuf.append(sep);
		strbuf.append("		Thread.sleep(500);");
		strbuf.append(sep);
		strbuf.append("		Alert alert = null;");
		strbuf.append(sep);
		strbuf.append("		try{");
		strbuf.append(sep);
		strbuf.append("			alert = webdriver.switchTo().alert();");
		strbuf.append(sep);
		strbuf.append("			alert.accept();");
		strbuf.append(sep);
		strbuf.append("		}catch(Exception e){");
		strbuf.append(sep);
		strbuf.append("			System.out.println(\"no alert\");");
		strbuf.append(sep);
		strbuf.append("		}");
		strbuf.append(sep);
		strbuf.append("		Thread.sleep(500);");
		strbuf.append(sep);
		strbuf.append("	}");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	//アラートをキャンセルクリックする");
		strbuf.append(sep);
		strbuf.append("	private void click_alertNG() throws Exception{");
		strbuf.append(sep);
		strbuf.append("		Thread.sleep(500);");
		strbuf.append(sep);
		strbuf.append("		Alert alert = null;");
		strbuf.append(sep);
		strbuf.append("		try{");
		strbuf.append(sep);
		strbuf.append("			alert = webdriver.switchTo().alert();");
		strbuf.append(sep);
		strbuf.append("			alert.dismiss();");
		strbuf.append(sep);
		strbuf.append("		}catch(Exception e){");
		strbuf.append(sep);
		strbuf.append("			System.out.println(\"no alert\");");
		strbuf.append(sep);
		strbuf.append("		}");
		strbuf.append(sep);
		strbuf.append("		Thread.sleep(500);");
		strbuf.append(sep);
		strbuf.append("	}");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	//アラート確認");
		strbuf.append(sep);
		strbuf.append("	private boolean isAlertPresent() {");
		strbuf.append(sep);
		strbuf.append("		try {");
		strbuf.append(sep);
		strbuf.append("			webdriver.switchTo().alert();");
		strbuf.append(sep);
		strbuf.append("			return true;");
		strbuf.append(sep);
		strbuf.append("		} catch (Exception e) {");
		strbuf.append(sep);
		strbuf.append("			return false;");
		strbuf.append(sep);
		strbuf.append("		}");
		strbuf.append(sep);
		strbuf.append("	}");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	//画像を取得");
		strbuf.append(sep);
		strbuf.append("	private String tryGetPicture() {");
		strbuf.append(sep);
		strbuf.append("		String strSnapshot = \"\";");
		strbuf.append(sep);
		strbuf.append("		try {");
		strbuf.append(sep);
		strbuf.append("			File file = ((TakesScreenshot) webdriver)");
		strbuf.append(sep);
		strbuf.append("					.getScreenshotAs(OutputType.FILE);");
		strbuf.append(sep);
		strbuf.append("			strSnapshot = TTUtility.getBase64String(file);");
		strbuf.append(sep);
		strbuf.append("		} catch (Exception e) {");
		strbuf.append(sep);
		strbuf.append("			e.printStackTrace();");
		strbuf.append(sep);
		strbuf.append("		}");
		strbuf.append(sep);
		strbuf.append("		return strSnapshot;");
		strbuf.append(sep);
		strbuf.append("	}");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("	// 活性Windowを検索してフォーカスを移動する");
		strbuf.append(sep);
		strbuf.append("	private boolean move_activeWindow() throws Exception {");
		strbuf.append(sep);
		strbuf.append("		Thread.sleep(500);");
		strbuf.append(sep);
		strbuf.append("		try{");
		strbuf.append(sep);
		strbuf.append("			webdriver.getWindowHandle();");
		strbuf.append(sep);
		strbuf.append("			return true;");
		strbuf.append(sep);
		strbuf.append("		}catch(Exception e){");
		strbuf.append(sep);
		strbuf.append("		}");
		strbuf.append(sep);
		strbuf.append("		Thread.sleep(500);");
		strbuf.append(sep);
		strbuf.append("");
		strbuf.append(sep);
		strbuf.append("		boolean bStatus = false;");
		strbuf.append(sep);
		strbuf.append("		for (String hndlWin : hndlsNow) {");
		strbuf.append(sep);
		strbuf.append("			try {");
		strbuf.append(sep);
		strbuf.append("				webdriver.switchTo().window(hndlWin);");
		strbuf.append(sep);
		strbuf.append("				bStatus = true;");
		strbuf.append(sep);
		strbuf.append("			} catch (Exception e) {");
		strbuf.append(sep);
		strbuf.append("				System.out.println(e.getMessage());");
		strbuf.append(sep);
		strbuf.append("			}");
		strbuf.append(sep);
		strbuf.append("			if (bStatus)");
		strbuf.append(sep);
		strbuf.append("				break;");
		strbuf.append(sep);
		strbuf.append("		}");
		strbuf.append(sep);
		strbuf.append("		return bStatus;");
		strbuf.append(sep);
		strbuf.append("	}");
		strbuf.append(sep);
		strbuf.append("}");

		return strbuf.toString();
	}



}
