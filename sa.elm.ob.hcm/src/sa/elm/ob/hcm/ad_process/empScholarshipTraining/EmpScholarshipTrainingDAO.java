package sa.elm.ob.hcm.ad_process.empScholarshipTraining;

import org.openbravo.base.secureApp.VariablesSecureApp;

import sa.elm.ob.hcm.EHCMEmpScholarship;
import sa.elm.ob.hcm.EHCMScholarshipSummary;

/**
 * Interface for all Employee Evaluation related DB Operations
 * 
 * @author divya -12-02-2018
 *
 */
public interface EmpScholarshipTrainingDAO {

  /**
   * update the Employee Evaluation status
   * 
   * @param empEvaluationObj
   * @param action
   * @throws Exception
   */
  void updateEmpScholarshipStatus(EHCMEmpScholarship empScholarShip) throws Exception;

  /**
   * insert employement info while create
   * 
   * @param empScholarShip
   * @param oldempInfo
   * @param vars
   * @throws Exception
   */
  void insertScholarshipSummary(EHCMEmpScholarship empScholarShip,
      EHCMScholarshipSummary scholarshipSummary, VariablesSecureApp vars, String decisionType)
      throws Exception;

  /**
   * update the employment info for decision type is "Update"
   * 
   * @param empScholarShip
   * @param employInfo
   * @param vars
   */
  void updateScholarshipSummary(EHCMEmpScholarship empScholarShip,
      EHCMScholarshipSummary scholarshipSummary, VariablesSecureApp vars, String decisionType)
      throws Exception;

  /**
   * update old employee scholarship as inactive
   * 
   * @param empScholarshipObj
   */
  void updateOldEmpScholarshipInAct(EHCMEmpScholarship empScholarshipObj);

  /**
   * remove the scholarship entry in scholarship summary while cancel the scholarship
   * 
   * @param empScholarshipObj
   */
  void removeScholarshipActRecord(EHCMEmpScholarship empScholarshipObj);

  /**
   * get Active Scholarship summary details
   * 
   * @param employeeId
   * @param originaldecId
   * @return EHCMScholarshipSummary obj
   */
  EHCMScholarshipSummary getActiveScholarshipSummary(String employeeId, String originaldecId)
      throws Exception;

  /**
   * get Active Scholarship summary details
   * 
   * @param empScholarShip
   * @return
   * @throws Exception
   */
  EHCMScholarshipSummary getActEmpScholarSummary(EHCMEmpScholarship empScholarShip)
      throws Exception;

  /**
   * update payment flag
   * 
   * @param employeeId
   * @param originaldecId
   * @param reactive
   * @return
   * @throws Exception
   */

  EHCMScholarshipSummary updatePaymentFlag(String employeeId, String originaldecId,
      boolean reactive) throws Exception;

  /**
   * 
   * @param employeeId
   * @param originaldecId
   * @throws Exception
   */
  void removeScholarshipInfo(String employeeId, String originaldecId) throws Exception;

  /**
   * 
   * @param employeeId
   * @param empScholarShip
   * @return
   * @throws Exception
   */
  EHCMScholarshipSummary updateScholarshipSummary(String employeeId,
      EHCMEmpScholarship empScholarShip) throws Exception;
}
