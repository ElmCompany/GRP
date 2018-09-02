package sa.elm.ob.hcm.ad_process.employeeEvaluation;

import sa.elm.ob.hcm.EHCMEmpEvaluation;

/**
 * Interface for all Employee Evaluation related DB Operations
 * 
 * @author divya -12-02-2018
 *
 */
public interface EmployeeEvaluationDAO {

	/**
	 * update the Employee Evaluation status
	 * 
	 * @param empEvaluationObj
	 * @param action
	 * @throws Exception
	 */
	void updateEmpEvaluationStatus(EHCMEmpEvaluation empEvaluationObj, String action) throws Exception;
}
