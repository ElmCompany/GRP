package sa.elm.ob.hcm.ad_forms.MedicalInsurance.dao;

import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;

import sa.elm.ob.hcm.ad_forms.MedicalInsurance.vo.MedicalInsuranceVO;

/**
 * Interface for all Medical Insurance related DB Operations
 * 
 * @author Priyanka Ranjan 17-03-2018
 *
 */
public interface MedicalInsuranceDAO {

  /**
   * 
   * @param clientId
   * @param searchTerm
   * @param pagelimit
   * @param page
   * @return
   * @throws Exception
   */
  JSONObject getInsuranceSchema(String clientId, String searchTerm, int pagelimit, int page)
      throws Exception;

  /**
   * get dependents from dependent table of medical insurance
   * 
   * @param clientId
   * @param employeeId
   * @return
   * @throws Exception
   */
  JSONObject getDependents(String clientId, String employeeId, String searchTerm, int pagelimit,
      int page) throws Exception;

  /**
   * get Insurance category Reference from Reference of medical insurance
   * 
   * @param clientId
   * @return
   * @throws Exception
   */
  List<MedicalInsuranceVO> getInsuCategoryReference() throws Exception;

  /**
   * Insert Records in Medical Insurance table
   * 
   * @param clientId
   * @param userId
   * @param vo
   * @param vars
   * @return
   * @throws Exception
   */
  String addMedicalInsurance(String clientId, String userId, MedicalInsuranceVO vo,
      VariablesSecureApp vars) throws Exception;

  /**
   * Update records in Medical Insurance table
   * 
   * @param clientId
   * @param userId
   * @param vo
   * @param employeeId
   * @param vars
   * @return
   * @throws Exception
   */
  String updateMedicalInsurance(String clientId, String userId, MedicalInsuranceVO vo,
      String employeeId, VariablesSecureApp vars) throws Exception;

  /**
   * Get Medical Insurance records from Medical Insurance table
   * 
   * @param clientId
   * @param employeeId
   * @param searchAttr
   * @param medicalInsuranceId
   * @return
   * @throws Exception
   */
  JSONObject getMedicalInsuranceList(String clientId, String employeeId, JSONObject searchAttr,
      String medicalInsuranceId) throws Exception;

  /**
   * Get Medical Insurance records for editing from Medical Insurance table
   * 
   * @param clientId
   * @param employeeId
   * @param searchAttr
   * @param medicalInsuranceId
   * @return
   * @throws Exception
   */
  JSONObject getMedicalInsEditList(String clientId, String employeeId, JSONObject searchAttr,
      String medicalInsuranceId) throws Exception;

  /**
   * Delete Medical Insurance
   * 
   * @param medicalInsuranceId
   * @return
   * @throws Exception
   */
  boolean deleteMedicalInsurance(String medicalInsuranceId) throws Exception;

  /**
   * Get Relationship of Dependent aand Employee
   * 
   * @param searchKey
   * @return
   * @throws Exception
   */
  String getRelationshipName(String searchKey) throws Exception;

  /**
   * Check Already Insurance is Exist for Dependent with memebershipNo for the particular employee
   * 
   * @param dependent
   * @param membershipno
   * @param employeeId
   * @param clientId
   * @return
   * @throws Exception
   */
  boolean checkInsuranceAlreadyExistsForDependent(String dependent, String membershipno,
      String employeeId, String medicalinsuranceId, String clientId) throws Exception;

}
