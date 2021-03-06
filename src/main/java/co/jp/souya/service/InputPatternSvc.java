package co.jp.souya.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.jp.souya.dto.InputPatternDTO;
import co.jp.souya.jpa.InputPattern;
import co.jp.souya.jpa.ParametaValue;

@Service
public class InputPatternSvc extends BaseSvc {
	private static final Logger logger = LoggerFactory
			.getLogger(InputPatternSvc.class);

	public InputPatternSvc() {
		logger.info(this.getClass().getName());
	}

	/**
	 * 単純取得
	 *
	 * @param id
	 * @return
	 */
	public InputPattern get(Integer id) {
		InputPattern dao = null;
		try {
			init();

			dao = em.find(InputPattern.class, id);

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		} finally {
			destroy();
		}

		return dao;
	}

	/**
	 * 単純更新
	 *
	 * @param dao
	 * @return
	 */
	public InputPattern update(InputPattern dao) {

		InputPattern newdao = null;
		try {
			init();
			tx.begin();

			newdao = (dao.getId() == null) ? new InputPattern() : em.find(
					InputPattern.class, dao.getId());

			newdao.setテストケース管理id(dao.getテストケース管理id());
			newdao.setNo(dao.getNo());
			newdao.set入力パターン名(dao.get入力パターン名());
			newdao.set備考(dao.get備考());
			em.persist(newdao);

			tx.commit();

		} catch (Throwable e) {
			tx.rollback();
			logger.error(e.getMessage(), e);
		} finally {
			destroy();
		}

		return newdao;
	}

	/**
	 * 削除
	 *
	 * @param id
	 * @return
	 */
	public boolean delete(int id) {

		try {
			init();
			tx.begin();

			InputPattern dao = em.find(InputPattern.class, id);
			em.remove(dao);

			tx.commit();

		} catch (Throwable e) {
			tx.rollback();
			logger.error(e.getMessage(), e);
			return false;
		} finally {
			destroy();
		}

		return true;
	}

	/**
	 * 入力パターン画面　表示情報を取得する
	 *
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public InputPatternDTO getDTO(int id) {

		InputPatternDTO dto = new InputPatternDTO();

		try {
			init();

			InputPattern w入力パターン = em.find(InputPattern.class, id);
			List<ParametaValue> wパラメタ値リスト = em
					.createNamedQuery("ParametaValue.findListById")
					.setParameter("入力パターンid", w入力パターン.getId()).getResultList();
			dto.set入力パターン(w入力パターン);
			dto.setパラメタ値リスト(wパラメタ値リスト);

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		} finally {
			destroy();
		}

		return dto;
	}

	/**
	 * テスト結果をアップデートする
	 *
	 * @param id
	 * @param w判定結果
	 * @param wJob状況
	 * @param wスナップショットBase64
	 * @param wHtml
	 * @param wDb
	 * @param wHtmlDif
	 * @param wDbDif
	 * @param wErr_list
	 * @return
	 */
	public InputPattern updateTestResult(int id, String w判定結果, String wJob状況,
			String wスナップショットBase64, String wHtml, String wDb, String wHtmlDif,
			String wDbDif, String wErr_list) {

		InputPattern dao = null;
		try {
			init();
			tx.begin();

			dao = em.find(InputPattern.class, id);
			dao.set判定結果(w判定結果);
			dao.setJob状況(wJob状況);
			// dao.set遷移結果(wスナップショットBase64);
			dao.set画面(wスナップショットBase64);
			int n実行回数 = dao.get実行回数() == null ? 0 : dao.get実行回数();
			dao.set実行回数(n実行回数 + 1);
			dao.setDb(wDb);
			dao.setHtml(wHtml);
			dao.setDb差異(wDbDif);
			dao.setHtml差異(wHtmlDif);
			dao.set遷移結果(wErr_list);
			em.persist(dao);

			tx.commit();

		} catch (Throwable e) {
			tx.rollback();
			logger.error(e.getMessage(), e);
		} finally {
			destroy();
		}

		return dao;
	}

	/**
	 * テストケースを[指定]状態にする
	 *
	 * @param input_ids
	 * @param status
	 * @return
	 */
	public boolean updateTestStatus(List<Integer> input_ids, String status) {

		if (input_ids == null)
			return true;

		try {
			init();
			tx.begin();

			for (Integer id : input_ids) {
				InputPattern dao = em.find(InputPattern.class, id);
				dao.setJob状況(status);
				em.persist(dao);
			}

			tx.commit();

		} catch (Throwable e) {
			tx.rollback();
			logger.error(e.getMessage(), e);
			return false;
		} finally {
			destroy();
		}

		return true;
	}

	/**
	 * テスト結果の正解値（HTMLとDB）をアップデートする
	 *
	 * @param id
	 * @return
	 */
	public InputPattern updateResult(int id, String wHTML, String wDB,
			String wSnapshot) {
		InputPattern dao = null;
		try {
			init();
			tx.begin();

			dao = em.find(InputPattern.class, id);
			dao.setDb正解(wDB);
			dao.setHtml正解(wHTML);
			dao.set画面正解(wSnapshot);
			dao.set実行回数(0);
			em.persist(dao);

			tx.commit();

		} catch (Throwable e) {
			tx.rollback();
			logger.error(e.getMessage(), e);
		} finally {
			destroy();
		}

		return dao;
	}

	/**
	 * テストケースをリセットする
	 *
	 * @param id
	 * @return
	 */
	public boolean reset(int id) {

		try {
			init();
			tx.begin();

			InputPattern dao = em.find(InputPattern.class, id);
			dao.set実行回数(0);
			dao.setJob状況("");
			dao.set判定結果("");
			dao.set遷移結果("");
			dao.setHtml正解("");
			dao.setHtml("");
			dao.setHtml差異("");
			dao.setDb("");
			dao.setDb正解("");
			dao.setDb差異("");
			dao.set画面("");
			dao.set画面正解("");
			dao.set画面差異("");
			em.persist(dao);

			tx.commit();

		} catch (Throwable e) {
			tx.rollback();
			logger.error(e.getMessage(), e);
			return false;
		} finally {
			destroy();
		}

		return true;
	}
}
