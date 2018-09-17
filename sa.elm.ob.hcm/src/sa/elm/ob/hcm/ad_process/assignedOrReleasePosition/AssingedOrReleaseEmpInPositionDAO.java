package sa.elm.ob.hcm.ad_process.assignedOrReleasePosition;

import java.util.Date;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EHCMEmpPromotion;
import sa.elm.ob.hcm.EHCMEmpTransfer;
import sa.elm.ob.hcm.EHCMEmpTransferSelf;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;

/**
 * Interface for all Assigned or Release employee in Position related DB Operations
 * 
 * @author divya -30-07-2018
 *
 */
public interface AssingedOrReleaseEmpInPositionDAO {

  /**
   * insert position employee history record
   * 
   * @param empDelegation
   * @param vars
   * @throws Exception
   */
  void insertPositionEmployeeHisotry(Client client, Organization org, EhcmEmpPerInfo employee,
      EmployeeDelegation empDelegation, Date startDate, Date endDate, String decisionNo,
      Date decisionDate, EhcmPosition position, VariablesSecureApp vars,
      EHCMEmpTransfer empTransfer, EHCMEmpPromotion empPromotion,
      EHCMEmpTransferSelf empTransferSelf) throws Exception;

  /**
   * delete the position employee history record
   * 
   * @param empDelegation
   * @param vars
   * @throws Exception
   */
  void deletePositionEmployeeHisotry(EhcmEmpPerInfo employee, EhcmPosition postion)
      throws Exception;

  /**
   * Update End Date for already present record in Position Employee History
   * 
   * @param employee
   * @param postion
   * @throws Exception
   */
  void updateEndDateInPositionEmployeeHisotry(EhcmEmpPerInfo employee, EhcmPosition postion,
      Date enddate, EHCMEmpTransfer empTransfer, EHCMEmpPromotion empPromotion,
      EHCMEmpTransferSelf empTransferSelf, EmployeeDelegation empDelegation,
      EmployeeSuspension suspension, EHCMEMPTermination endofemployment,
      EmploymentInfo recentEmployeInfo) throws Exception;

  /**
   * 
   * @param employee
   * @param empTransfer
   * @param empPromotion
   * @param empTransferSelf
   * @return
   * @throws Exception
   */
  EhcmPosition revertOldValuesAndGetOldestPosition(EhcmEmpPerInfo employee,
      EHCMEmpTransfer empTransfer, EHCMEmpPromotion empPromotion,
      EHCMEmpTransferSelf empTransferSelf, boolean isreactivate) throws Exception;

  /**
   * 
   * @param employee
   * @param empPromotion
   * @param empTransfer
   * @return
   * @throws Exception
   */
  EhcmPosition getCurrentPositionOfEmployee(EhcmEmpPerInfo employee, EHCMEmpPromotion empPromotion,
      EHCMEmpTransfer empTransfer) throws Exception;

  /**
   * 
   * @param employee
   * @param empPromotion
   * @param empTransfer
   * @param empTransferSelf
   * @return
   * @throws Exception
   */
  EhcmPosition getRecentPosition(EhcmEmpPerInfo employee, EHCMEmpPromotion empPromotion,
      EHCMEmpTransfer empTransfer, EHCMEmpTransferSelf empTransferSelf) throws Exception;

  /**
   * 
   * @param employee
   * @param position
   * @param startDate
   * @param endDate
   * @param decisionType
   * @param isdelegated
   * @return
   * @throws Exception
   */
  Boolean chkPositionAvailableOrNot(EhcmEmpPerInfo employee, EhcmPosition position, Date startDate,
      Date endDate, String decisionType, Boolean isdelegated) throws Exception;

  /**
   * Update End Date for record in Position Employee History for Delegated Employee
   * 
   * @param employee
   * @param enddate
   * @throws Exception
   */
  void updateEndDateForDelegatedEmployee(EhcmEmpPerInfo employee, Date enddate,
      EmployeeSuspension suspension, EHCMEMPTermination endofemployment, String decisionType)
      throws Exception;

  /**
   * Insert record back into Position Employee History while cancel the Suspension End with enddate
   * less than Delegated start date
   * 
   * @param employee
   * @param enddate
   * @throws Exception
   */
  void updateCancelledDelegatedEmpPosition(EhcmEmpPerInfo employee, Date enddate,
      EmployeeSuspension empSuspension, EHCMEMPTermination ehcmempTermination, String decisionType)
      throws Exception;

  /**
   * check any delegated record is present for the employee with greater than start date
   * 
   * @param employee
   * @param enddate
   * @return
   * @throws Exception
   */
  boolean checkDelegatedRecordwithGreaterthanStartDate(EhcmEmpPerInfo employee, Date enddate)
      throws Exception;

  /**
   * 
   * @param employee
   * @param empSuspension
   * @param ehcmempTermination
   * @param StartDate
   * @param Enddate
   * @param decisionType
   * @param isdelegated
   * @return
   * @throws Exception
   */
  Boolean chkDelegatePositionAvailableOrNot(EhcmEmpPerInfo employee,
      EmployeeSuspension empSuspension, EHCMEMPTermination ehcmempTermination, Date StartDate,
      Date Enddate, String decisionType, Boolean isdelegated) throws Exception;

  /**
   * 
   * @param employee
   * @param promotion
   * @param empTransfer
   * @param empTransferSelf
   * @return
   * @throws Exception
   */
  EmploymentInfo getRecentEmploymentInfo(EhcmEmpPerInfo employee, EHCMEmpPromotion promotion,
      EHCMEmpTransfer empTransfer, EHCMEmpTransferSelf empTransferSelf) throws Exception;

  /**
   * Update enddate for Position employee history record for emp which are cancelling from
   * employmentinfo
   * 
   * @param employee
   * @param postion
   * @throws Exception
   */
  void updateEndDatePositionEmployeeHisotryForCancelledEmp(EhcmEmpPerInfo employee,
      EhcmPosition postion) throws Exception;

  /**
   * 
   * @param promotion
   * @param vars
   * @throws Exception
   */
  void updateEmpPositionWhileReactive(EHCMEmpPromotion promotion, EHCMEmpTransfer transfer,
      EHCMEmpTransferSelf transferself, VariablesSecureApp vars, boolean iscancel) throws Exception;

}
