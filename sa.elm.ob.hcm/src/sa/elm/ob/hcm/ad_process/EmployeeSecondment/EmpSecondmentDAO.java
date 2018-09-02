package sa.elm.ob.hcm.ad_process.EmployeeSecondment;

import org.openbravo.base.secureApp.VariablesSecureApp;

import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EmploymentInfo;

/**
 * Interface for all Employee Secondment Decision related DB Operations
 * 
 * @author divya -26-05-2018
 *
 */
public interface EmpSecondmentDAO {

  /**
   * 
   * @param secondment
   * @throws Exception
   */
  void updateSecondmentStatus(EHCMEmpSecondment secondment) throws Exception;

  /**
   * 
   * @param secondment
   * @return
   * @throws Exception
   */
  EmploymentInfo getEmploymentInfo(EHCMEmpSecondment secondment) throws Exception;

  /**
   * 
   * @param secondment
   * @param info
   * @return
   * @throws Exception
   */
  EmploymentInfo insertEmploymentRecord(EHCMEmpSecondment secondment, EmploymentInfo info,
      boolean isJWR, boolean isSecDelay, EhcmJoiningWorkRequest jWRObj) throws Exception;

  /**
   * 
   * @param employInfo
   * @param secondment
   * @param vars
   * @throws Exception
   */
  void updateEndDateForOldRecord(EmploymentInfo employInfo, EHCMEmpSecondment secondment,
      VariablesSecureApp vars) throws Exception;

  /**
   * 
   * @param secondment
   * @param enableFlag
   * @throws Exception
   */
  void updateOldSecondmentActiveFlag(EHCMEmpSecondment secondment, boolean enableFlag)
      throws Exception;

  /**
   * 
   * @param employInfo
   * @param secondment
   * @param vars
   * @throws Exception
   */
  void updateEndDateForOldRecordInUpdate(EmploymentInfo employInfo, EHCMEmpSecondment secondment,
      VariablesSecureApp vars) throws Exception;

  /**
   * 
   * @param secondment
   * @throws Exception
   */
  void updateEmploymentStatus(EHCMEmpSecondment secondment, boolean iscancel) throws Exception;

  /**
   * 
   * @param employInfo
   * @param secondment
   * @param vars
   * @throws Exception
   */
  EmploymentInfo updateEndDateForOldRecordInCancel(EHCMEmpSecondment secondment,
      VariablesSecureApp vars) throws Exception;

  /**
   * 
   * @param empinfo
   * @param secondment
   * @throws Exception
   */
  void updateDelegation(EmploymentInfo empinfo, EHCMEmpSecondment secondment) throws Exception;

  /**
   * 
   * @param empinfo
   * @param secondment
   * @throws Exception
   */
  void remRecntEmpInfoInCancel(EmploymentInfo empinfo, EHCMEmpSecondment secondment)
      throws Exception;

  /**
   * get previous employment info -other than recent record
   * 
   * @param empId
   * @return employment info obj
   * @throws Exception
   */
  EmploymentInfo getPreviousEmployInfo(String empId) throws Exception;

}
