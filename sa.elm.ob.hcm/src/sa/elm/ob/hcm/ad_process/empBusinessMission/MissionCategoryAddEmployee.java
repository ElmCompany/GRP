package sa.elm.ob.hcm.ad_process.empBusinessMission;

import java.text.DateFormat;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMMisEmpCategory;
import sa.elm.ob.hcm.EHCMMissionCategory;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAOImpl;

/**
 * This Process will handle the Mission Category Add Employee
 * 
 * @author Divya-23-03-2018
 *
 */
public class MissionCategoryAddEmployee implements Process {
	private static final Logger log4j = LoggerFactory.getLogger(MissionCategoryAddEmployee.class);
	private final OBError obError = new OBError();

	@Override
	public void execute(ProcessBundle bundle) throws Exception {
		// TODO Auto-generated method stub
		HttpServletRequest request = RequestContext.get().getRequest();
		VariablesSecureApp vars = new VariablesSecureApp(request);
		String lang = vars.getLanguage();

		final String missCatId = bundle.getParams().get("Ehcm_Mission_Category_ID").toString();
		final String userId = (String) bundle.getContext().getUser();
        User user = OBDal.getInstance().get(User.class, userId);
        
		EHCMMissionCategory missCatObj = OBDal.getInstance().get(EHCMMissionCategory.class, missCatId);
		final String orgId = missCatObj.getOrganization().getId();
		final String roleId = (String) bundle.getContext().getRole();

		Boolean successFlag = false;
		 MissionCategoryDAOImpl missionCategoryDAOImpl = new MissionCategoryDAOImpl();
		try {
			OBContext.setAdminMode();
			
			successFlag=missionCategoryDAOImpl.addNewEmployeesToRecentPeriod (missCatObj , user);

			if(successFlag) {
				obError.setType("Success");
				obError.setTitle("Success");
				obError.setMessage(OBMessageUtils.messageBD("EHCM_RefEmp_Success"));
			}
			else if(successFlag) {
				obError.setType("Error");
				obError.setTitle("Error");
				obError.setMessage(OBMessageUtils.messageBD("EHCM_RefEmp_NotComp"));
			}
			bundle.setResult(obError);
			OBDal.getInstance().flush();
		}

		catch (Exception e) {
			bundle.setResult(obError);
			OBDal.getInstance().rollbackAndClose();
			if(log4j.isErrorEnabled()) {
				log4j.error("exception :", e);
			}
		}
		finally {
			OBContext.restorePreviousMode();
		}

	}

}