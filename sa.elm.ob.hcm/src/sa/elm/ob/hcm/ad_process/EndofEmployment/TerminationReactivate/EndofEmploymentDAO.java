package sa.elm.ob.hcm.ad_process.EndofEmployment.TerminationReactivate;

import org.openbravo.base.secureApp.VariablesSecureApp;

import sa.elm.ob.hcm.EHCMEMPTermination;

/**
 * Interface for all Termination related DB Operations
 * 
 * @author poongodi -28-08-2018
 *
 */
public interface EndofEmploymentDAO {
  /**
   * 
   * @param terminationId
   */

  void removeEmploymentRecord(String terminationId);

  /**
   * 
   * @param terminationId
   * @param vars
   * @param termination
   */
  void updateEmploymentRecord(String terminationId, VariablesSecureApp vars,
      EHCMEMPTermination termination);

  /**
   * 
   * @param employeeId
   * @param terminationId
   */
  void removeEmpStatusRecord(String employeeId, String terminationId);

  /**
   * 
   * @param terminationId
   * @param vars
   * @param termination
   */
  void updateEmpRecord(String terminationId, VariablesSecureApp vars,
      EHCMEMPTermination termination);

  /**
   * 
   * @param terminationoldObj
   * @param vars
   */
  void insertRecordinEmploymentInfo(EHCMEMPTermination terminationoldObj, VariablesSecureApp vars);

  /**
   * 
   * @param terminationObj
   * @param terminationOldObj
   * @param vars
   */
  void updateTerminationRecord(EHCMEMPTermination terminationObj,
      EHCMEMPTermination terminationOldObj, VariablesSecureApp vars);
}
