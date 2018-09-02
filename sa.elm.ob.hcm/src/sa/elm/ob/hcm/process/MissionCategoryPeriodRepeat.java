package sa.elm.ob.hcm.process;

import java.util.Date;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;

import sa.elm.ob.hcm.event.dao.MissionCategoryDAOImpl;

public class MissionCategoryPeriodRepeat extends DalBaseProcess {
	private ProcessLogger logger;

	private static final Logger log = Logger.getLogger(MissionCategoryPeriodRepeat.class);

	@Override
	protected void doExecute(ProcessBundle bundle) throws Exception {
		// TODO Auto-generated method stub
		OBError result = new OBError();
		logger = bundle.getLogger();
		MissionCategoryDAOImpl missionCategoryDAOImpl= new MissionCategoryDAOImpl();
		try {
			OBContext.setAdminMode();
			final String clientId = (String) bundle.getContext().getClient();
			final String userId = (String) bundle.getContext().getUser();
			User user = OBDal.getInstance().get(User.class, userId);
			
			addLog(OBMessageUtils.messageBD("EHCM_MisCatPeriodStart"));

			missionCategoryDAOImpl.repeatMissionCatPeriod( clientId, user);

			addLog(OBMessageUtils.messageBD("EHCM_MisCatPeriodEnd"));

			bundle.setResult(result);

		}
		catch (Exception e) {
			OBDal.getInstance().rollbackAndClose();
			result = OBMessageUtils.translateError(bundle.getConnection(), bundle.getContext().toVars(), OBContext.getOBContext().getLanguage().getLanguage(), e.getMessage());
			log.error(result.getMessage(), e);
			addLog(result.getMessage());
			bundle.setResult(result);
			return;
		}
		finally {
			OBContext.restorePreviousMode();
			addLog("Ending background process.");
		}
	}

	/**
	 * Adds a message to the log.
	 * 
	 * @param msg
	 *            to add to the log
	 */
	private void addLog(String msg) {
		addLog(msg, false);
	}

	/**
	 * Add a message to the log.
	 * 
	 * @param msg
	 * @param generalLog
	 */
	private void addLog(String msg, boolean generalLog) {
		logger.log(msg + "\n");
	}

	public void kill(ProcessBundle processBundle) throws Exception {
		addLog("Process Killed");
	}
}
