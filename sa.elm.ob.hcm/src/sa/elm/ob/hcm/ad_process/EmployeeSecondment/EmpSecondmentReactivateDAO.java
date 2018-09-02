package sa.elm.ob.hcm.ad_process.EmployeeSecondment;

import org.openbravo.base.secureApp.VariablesSecureApp;

import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EmploymentInfo;

/**
 * Interface for all Employee Secondment reactivate related DB Operations
 * 
 * @author divya 11-06-2018
 *
 */
public interface EmpSecondmentReactivateDAO {
  /**
   * 
   * @param empSecondment
   * @return
   * @throws Exception
   */
  boolean chkEmplyInfoExistAfterSecondment(EHCMEmpSecondment empSecondment) throws Exception;

  /**
   * 
   * @param empSecondment
   * @return
   * @throws Exception
   */
  boolean chkEmplyInfoExistsInCancelCase(EHCMEmpSecondment empSecondment) throws Exception;

  /**
   * 
   * @param empSecondment
   * @throws Exception
   */
  void deleteEmpInfo(EHCMEmpSecondment empSecondment) throws Exception;

  /**
   * 
   * @param empSecondment
   * @throws Exception
   */
  void updateExtendRecordInCutOffCase(EHCMEmpSecondment empSecondment) throws Exception;

  /**
   * 
   * @param empSecondment
   * @param vars
   * @throws Exception
   */
  void InsertEmpInfoInCancelCase(EHCMEmpSecondment empSecondment, VariablesSecureApp vars)
      throws Exception;

  /**
   * 
   * @param secondment
   * @throws Exception
   */
  void updateSecondmentStatus(EHCMEmpSecondment secondment) throws Exception;

  /**
   * 
   * @param empSecondment
   * @throws Exception
   */

  void updateEmpInfoInupdateCase(EHCMEmpSecondment empSecondment) throws Exception;

  /**
   * update the join work request
   * 
   * @param empSecondment
   * @throws Exception
   */
  void updateJoinWorkRequestOriginalDecisionNo(EmploymentInfo empInfo, EmploymentInfo prevEmpInfo)
      throws Exception;
}
