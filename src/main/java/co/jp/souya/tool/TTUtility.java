package co.jp.souya.tool;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * 2014/12/19 Created
 *
 * @author hsatomi@souya ユーティリティークラス
 */
public class TTUtility {
	private static final Logger logger = LoggerFactory
			.getLogger(TTUtility.class);

	/** ファイルセパレータ */
	public static String fileSeparator = System.lineSeparator();
	// static String fileSeparator = File.separator;

	public static String lineSeparator = System.lineSeparator();

	/**
	 * オブジェクトがnull又はEmptyか判定
	 *
	 * @param obj
	 * @return true:null or Empty<br>
	 *         false:上記以外
	 */
	public static boolean isNullOrEmpty(Object obj) {

		if (obj == null) {
			return true;
		}
		if (obj instanceof String) {
			String objCls = (String) obj;
			return objCls.isEmpty();
		}

		return false;
	}

	/**
	 * オブジェクトが一致するか判定
	 *
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean isEquals(Object obj1, Object obj2) {

		if (obj1 == null && obj2 == null)
			return true;
		if (obj1 != null && obj2 == null)
			return false;
		if (obj1 == null && obj2 != null)
			return false;

		return obj1.equals(obj2);
	}

	/**
	 * mysqlDB - varchar を Stringに変換
	 *
	 * @param obj
	 * @return String値 Castエラーはnull
	 */
	public static String getDBstring(Object obj) {
		String rtnVal;

		if (isNullOrEmpty(obj)) {
			return null;
		}

		try {
			rtnVal = (String) obj;
		} catch (Exception e) {
			rtnVal = null;
		}

		return rtnVal;
	}

	/**
	 * mysqlDB - bigint を Integerに変換
	 *
	 * @param obj
	 * @return Integer値、又は変換不可の場合 null
	 */
	public static Integer getDBbigint(Object obj) {
		Integer rtnVal;

		if (isNullOrEmpty(obj)) {
			return null;
		}

		String objStr = obj.toString();
		if (isNullOrEmpty(objStr)) {
			return null;
		}

		try {
			rtnVal = Integer.parseInt(objStr);
		} catch (Exception e) {
			rtnVal = null;
		}

		return rtnVal;
	}

	/**
	 * mysqlDB - bigint を Longに変換
	 *
	 * @param obj
	 * @return
	 */
	public static Long getDBlong(Object obj) {
		Long rtnVal;

		if (isNullOrEmpty(obj)) {
			return null;
		}

		String objStr = obj.toString();
		if (isNullOrEmpty(objStr)) {
			return null;
		}

		try {
			rtnVal = Long.valueOf(objStr);
		} catch (Exception e) {
			rtnVal = null;
		}

		return rtnVal;
	}

	/**
	 * mysqlDB - datetime を Dateに変換 (UTC -> JSTへの読み替え考慮)
	 *
	 * @param obj
	 * @return Date値 Castエラーはnull
	 */
	public static Date getDBdate(Object obj) {
		Date rtnVal = null;

		if (isNullOrEmpty(obj)) {
			return null;
		}

		try {
			Date _rtnVal = (Date) obj;
			rtnVal = toJSTfromUTC(_rtnVal);

		} catch (Exception e) {
			rtnVal = null;
		}

		return rtnVal;
	}

	/**
	 * log出力用にobject(model)プロパティの値を列挙する
	 *
	 * @param obj
	 * @return
	 */
	public static String entitylog(Object obj) {
		if (obj == null)
			return "";
		String className = obj.getClass().getSimpleName();
		StringBuffer rtn = new StringBuffer();
		rtn.append(className + " ");
		try {
			Field[] fields = obj.getClass().getFields();
			for (Field field : fields) {
				Object name = field.getName();
				Object value = field.get(obj);
				rtn.append(name + "=" + value + ",");
			}
		} catch (Exception e) {
		}
		return rtn.toString();
	}

	/**
	 * log出力用にobject(model)プロパティをサブクラスまで展開して列挙する
	 *
	 * @param obj
	 * @return
	 */
	public static String entitydeeplog(Object obj) {
		if (obj == null)
			return "null";
		String className = obj.getClass().getSimpleName();
		StringBuffer rtn = new StringBuffer();
		rtn.append(className + " ");
		try {
			if (obj.getClass().equals(ArrayList.class)) {
				@SuppressWarnings("unchecked")
				ArrayList<Object> objarr = (ArrayList<Object>) obj;
				for (Object object : objarr) {
					rtn.append(object.toString());
				}

			} else {
				Field[] fields = obj.getClass().getFields();
				for (Field field : fields) {
					Object name = field.getName();
					Object value = field.get(obj);
					if (value == null) {
						rtn.append(name + "=" + value + ",");
					} else if (value.getClass().equals(String.class)
							|| value.getClass().equals(Integer.class)
							|| value.getClass().equals(Date.class)
							|| value.getClass().equals(int.class)) {
						// rtn.append(name + "=" + value + ",");
						rtn.append(entitylog(value));

					} else {
						rtn.append(entitydeeplog(value));

					}

				}
			}

		} catch (Exception e) {
			return "#parse err#";
		}
		return rtn.toString();
	}

	/**
	 * 正規表現 regex1 に一致する文字列を target から除去して返す
	 *
	 * @param regex1
	 * @param target
	 */
	public static String removePatternChar(String regex1, String target) {
		Pattern p = Pattern.compile(regex1);
		Matcher m = p.matcher(target);

		String rtnValue = new String(target);

		if (m.find()) {
			int start = m.start();
			int end = m.end();

			rtnValue = target.substring(0, start) + target.substring(end);
		}

		return rtnValue;
	}

	/**
	 * 正規表現 regex1 に一致する文字列のみを取り出して返す
	 *
	 * @param regex1
	 * @param target
	 * @return
	 */
	public static String fetchPatternChar(String regex1, String target) {
		Pattern p = Pattern.compile(regex1);
		Matcher m = p.matcher(target);

		String rtnValue = "";

		if (m.find()) {
			int start = m.start();
			int end = m.end();

			rtnValue = target.substring(start, end);
		}

		return rtnValue;
	}

	/**
	 * 正規表現 regex1 に一致する文字列を target から検索して replaceに置換する
	 *
	 * @param regex1
	 * @param target
	 * @param replace
	 */
	public static String replacePatternChar(String regex1, String target,
			String replace) {
		Pattern p = Pattern.compile(regex1);
		Matcher m = p.matcher(target);

		String rtnValue = new String(target);

		if (m.find()) {
			rtnValue = m.replaceAll(replace);
		}

		return rtnValue;
	}

	/**
	 * 文字列から日付変換
	 *
	 * @param str
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date toDate(String str, String format) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date date = sdf.parse(str);
		return date;
	}

	private static Calendar cal = Calendar.getInstance();

	/**
	 * UTC->JST時刻変換
	 *
	 * @param utcDate
	 * @return
	 */
	public synchronized static Date toJSTfromUTC(Date utcDate) {
		if (utcDate == null)
			return null;

		// UTC->JST時刻変換
		cal.setTime(utcDate);
		cal.add(Calendar.HOUR_OF_DAY, +9);
		Date newDate = cal.getTime();
		return newDate;
	}

	/**
	 * JST->UTC時刻変換
	 *
	 * @param jstDate
	 * @return
	 */
	public synchronized static Date toUTCfromJST(Date jstDate) {
		if (jstDate == null)
			return null;

		// JST->UTC時刻変換
		cal.setTime(jstDate);
		cal.add(Calendar.HOUR_OF_DAY, -9);
		Date newDate = cal.getTime();
		return newDate;
	}

	/**
	 * パラメータをまとめて JST->UTC時刻変換
	 *
	 * @param args
	 * @return
	 */
	public static Object[] toUTCfromJST(Object... args) {
		List<Object> paramsAll = new ArrayList<Object>();

		for (Object obj : args) {
			if (obj != null && Date.class.isInstance(obj)) {
				Date _obj = toUTCfromJST((Date) obj);
				paramsAll.add(_obj);
				logger.trace("Thread:" + Thread.currentThread().getName() + ","
						+ "日付変換=" + obj.toString() + "->" + _obj.toString());
			} else {
				paramsAll.add(obj);
			}
		}
		return paramsAll.toArray();
	}

	/**
	 * ファイルパス結合をする(Unix系)
	 *
	 * @param path1
	 * @param path2
	 * @return
	 */
	public static String concatPath(String path1, String path2) {
		// 行末の "/" を削除
		String lastStr1 = path1.substring(path1.length() - 1);
		if (fileSeparator.equals(lastStr1)) {
			path1 = path1.substring(0, path1.length() - 1);
		}

		// 行頭の "/" を削除
		String firstStr2 = path2.substring(0, 1);
		if (fileSeparator.equals(firstStr2)) {
			path2 = path2.substring(1, path2.length());
		}

		String path = path1 + fileSeparator + path2;
		return path;
	}

	/**
	 * 対象文字列から 改行文字 を除去する
	 *
	 * @param targetStr
	 * @return
	 */
	public static String removeLineSeparator(String targetStr) {
		String rtn = targetStr.replaceAll("\r\n", "");
		rtn = rtn.replaceAll("\r", "");
		rtn = rtn.replaceAll("\n", "");
		return rtn;
	}

	/**
	 * 対象文字列を改行でsplitして有効な文字列のみ返す
	 *
	 * @param targetStr
	 * @return
	 */
	public static List<String> splitByLineSeparator(String targetStr) {
		List<String> result = new ArrayList<String>();
		// 全て改行文字を\nに置き換える
		String rtn = targetStr.replaceAll("\r\n", "\n");
		rtn = rtn.replaceAll("\r", "\n");
		String[] strs = rtn.split("\n");

		for (String str : strs) {
			if (!isNullOrEmpty(str.trim())) {
				result.add(str.trim());
			}
		}

		return result;
	}

	/**
	 * エスケープステートメント取得
	 *
	 * @return
	 */
	public static String getEscapeStatement() {

		return " escape '@' ";

	}

	/**
	 * jdbc sql likeパラメータエスケープ
	 *
	 * @param keyword
	 * @return
	 */
	public static String escape(String keyword) {
		String newword;
		newword = keyword.replaceAll("@", "@@");
		newword = newword.replaceAll("%", "@%");
		newword = newword.replaceAll("_", "@_");
		return newword;
	}

	/**
	 * File to Base64String
	 *
	 * @param file
	 * @return
	 */
	public static String getBase64String(File file) {
		String encodeResult = "";
		try {
			int fileLen = (int) file.length();
			byte[] data = new byte[fileLen];
			FileInputStream fis;
			fis = new FileInputStream(file);
			fis.read(data);
			// encodeResult = Base64.encodeBase64URLSafeString(data);
			encodeResult = Base64.encodeBase64String(data);

			// byte[] data2 = Base64.decodeBase64(encodeResult);
			// FileOutputStream fos = new FileOutputStream("output.txt");
			// fos.write(data2);

			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return encodeResult;
	}

	/**
	 * byte[] to Base64String
	 *
	 * @param file
	 * @return
	 */
	public static String getBase64String(byte[] data) {
		String encodeResult = "";
		try {
			encodeResult = Base64.encodeBase64String(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encodeResult;
	}

	/**
	 * Base64String to File
	 *
	 * @param encodeResult
	 * @return
	 */
	public static byte[] getImageFromBase64String(String encodeResult) {
		byte[] data = null;
		try {
			data = Base64.decodeBase64(encodeResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * HTML評価
	 *
	 * @param strResultWeb
	 * @param strExpectWeb
	 * @return
	 */
	public static String validateWeb(String strResultWeb, String strExpectWeb) {
		String strDif = "";

		if (!strResultWeb.equals(strExpectWeb)) {
			strDif = getDifString2(splitByLineSeparator(strResultWeb),
					splitByLineSeparator(strExpectWeb));
		}

		return strDif;
	}

	/**
	 * 改行つき文字列の差異を出力する
	 *
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getDifString(List<String> oldLines,
			List<String> newLines) {

		StringBuffer buf = new StringBuffer();

		Patch patch = DiffUtils.diff(oldLines, newLines);
		for (Object deltaobj : patch.getDeltas()) {
			Delta delta = (Delta) deltaobj;
			// System.out.println(String.format("[変更前(%d)行目]",
			// delta.getOriginal().getPosition() + 1));
			for (Object line : delta.getOriginal().getLines()) {
				buf.append(line + lineSeparator);
			}
			buf.append("↓" + lineSeparator);

			// System.out.println(String.format("[変更後(%d)行目]",
			// delta.getRevised().getPosition() + 1));
			for (Object line : delta.getRevised().getLines()) {
				buf.append(line + lineSeparator);
			}
		}
		logger.debug(buf.toString());
		return buf.toString();
	}

	/**
	 * 改行つき文字列の差異を出力する
	 *
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getDifString2(List<String> oldLines,
			List<String> newLines) {

		StringBuffer buf = new StringBuffer();

		Patch patch = DiffUtils.diff(oldLines, newLines);
		int cnt = 1;
		for (Object deltaobj : patch.getDeltas()) {
			buf.append("-------------------------------" + cnt++
					+ "箇所目---------------------------------" + lineSeparator);

			Delta delta = (Delta) deltaobj;
			for (Object line : delta.getOriginal().getLines()) {
				buf.append(line + lineSeparator);
			}
			buf.append("↓" + lineSeparator);

			for (Object line : delta.getRevised().getLines()) {
				buf.append(line + lineSeparator);
			}

			buf.append(lineSeparator);
			buf.append(lineSeparator);

		}
		logger.debug(buf.toString());
		return buf.toString();
	}

	/**
	 * 画像差分を返却する
	 *
	 * @param fileBG
	 * @param fileFG
	 * @return Base64String
	 */
	public static String getImageDiff(byte[] fileBG, byte[] fileFG) {
		String rtnStr = "";

		try {
			BufferedImage imgBG = ImageIO
					.read(new ByteArrayInputStream(fileBG));
			BufferedImage imgFG = ImageIO
					.read(new ByteArrayInputStream(fileFG));

			int w = imgBG.getWidth(), h = imgBG.getHeight();
			int[] pixelsBG = imgBG.getRGB(0, 0, w, h, null, 0, w);
			int[] pixelsFG = imgFG.getRGB(0, 0, w, h, null, 0, w);

			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int argbBG = pixelsBG[w * y + x];
					int argbFG = pixelsFG[w * y + x];
					int alpha = argbFG >> 24 & 0xFF;
					int red = argbFG >> 16 & 0xFF;
					int green = argbFG >> 8 & 0xFF;
					int blue = argbFG & 0xFF;

					argbFG = (alpha << 24) | (red << 16) | (green << 8) | blue;
					pixelsFG[w * y + x] = argbFG;

					/*
					 * ここでいろいろ操作して 配列に戻す
					 */
					if (argbBG != argbFG) {
						// pixelsFG[w * y + x] = 0;
					} else {
						// 変更内場所を0にする
						pixelsFG[w * y + x] = 0;
					}

				}
			}

			BufferedImage imgFGw = new BufferedImage(w, h,
					BufferedImage.TYPE_INT_RGB);
			imgFGw.setRGB(0, 0, w, h, pixelsFG, 0, w);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(imgFGw, "png", bos);
			rtnStr = getBase64String(bos.toByteArray());

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return rtnStr;
	}

}
