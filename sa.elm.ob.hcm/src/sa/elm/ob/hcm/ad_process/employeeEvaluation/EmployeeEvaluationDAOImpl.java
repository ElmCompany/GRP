package sa.elm.ob.hcm.ad_process.employeeEvaluation;

import java.sql.Connection;

import org.openbravo.dal.service.OBDal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEmpEvaluation;

/**
 * This process class used for Employee  EvaluationDAO Implementation
 * 
 * @author divya 12-02-2018
 *
 */

public class EmployeeEvaluationDAOImpl implements EmployeeEvaluationDAO {

	private Connection connection = null;
	private static final Logger log = LoggerFactory.getLogger(EmployeeEvaluationDAOImpl.class);
	private static final String ACTION_CO = "CO";
	private static final String ACTION_RE = "RE";
	private static final String STATUS_DR = "DR";
	private static final String STATUS_CO = "CO";

	public EmployeeEvaluationDAOImpl() {
		connection = getDbConnection();
	}

	/**
	 * Get the database connection
	 * 
	 * @return
	 */
	private Connection getDbConnection() {
		return OBDal.getInstance().getConnection();
	}

	@Override
	public void updateEmpEvaluationStatus(EHCMEmpEvaluation empEvaluationObj, String action) throws Exception {
		// TODO Auto-generated method stub
		try {
			if(action.equals(ACTION_CO)) {
				empEvaluationObj.setUpdated(new java.util.Date());
				empEvaluationObj.setAction(ACTION_RE);
				empEvaluationObj.setStatus(STATUS_CO);
				OBDal.getInstance().save(empEvaluationObj);
			}
			else if(action.equals(ACTION_RE)) {
				empEvaluationObj.setUpdated(new java.util.Date());
				empEvaluationObj.setAction(ACTION_CO);
				empEvaluationObj.setStatus(STATUS_DR);
				OBDal.getInstance().save(empEvaluationObj);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			log.error("Exception in updateEmpEvaluationStatus in EmployeeEvaluationDAOImpl: ", e);
		}
	}
}
