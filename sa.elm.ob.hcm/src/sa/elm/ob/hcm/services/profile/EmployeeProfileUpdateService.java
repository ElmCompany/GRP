package sa.elm.ob.hcm.services.profile;
/**
 * Employee Profile Service Interface
 *
 */

import java.util.List;

import sa.elm.ob.hcm.EhcmDependents;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.dto.profile.AddressInformationDTO;
import sa.elm.ob.hcm.dto.profile.DependentInformationDTO;
import sa.elm.ob.hcm.dto.profile.EmployeeAdditionalInformationDTO;
import sa.elm.ob.hcm.dto.profile.PersonalInformationDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

public interface EmployeeProfileUpdateService {
  /**
   * Update Employee Profile by given Employee Details
   * 
   * @param userName
   * @return
   */
  Boolean updateEmployeeProfile(String userName, PersonalInformationDTO personalInformationDTO,
      AddressInformationDTO addressInformationDTO, List<DependentInformationDTO> ehcmDependentList)
      throws BusinessException, SystemException;

  /**
   * 
   * @param userName
   * @param personalInformationDTO
   * @param addressInformationDTO
   * @param ehcmDependentList
   * @param isApprove
   * @param startWorkflow
   * @return
   * @throws BusinessException
   * @throws SystemException
   */
  Boolean updateEmployeeProfileWithWorkflow(String userName,
      PersonalInformationDTO personalInformationDTO, AddressInformationDTO addressInformationDTO,
      DependentInformationDTO dependentInformationDTO) throws BusinessException, SystemException;

  /**
   * update all the dependents of the user
   * 
   * @param userName
   * @param dependentList
   * @return
   */
  Boolean updateEmployeeDependent(String userName, DependentInformationDTO ehcmDependentList)
      throws BusinessException, SystemException;

  /**
   * update all the dependents of the user by activiti flow
   * 
   * @param userName
   * @param dependentList
   * @return
   */
  Boolean updateEmployeeDependentWithWorkFlow(String userName,
      DependentInformationDTO ehcmDependentList) throws BusinessException, SystemException;

  /**
   * Update employee Address
   * 
   * @param userName
   * @return
   */
  Boolean updateEmployeeAddress(String userName, AddressInformationDTO addressInformationDTO)
      throws BusinessException, SystemException;

  /**
   * Update employee Address with Activiti workflow
   * 
   * @param userName
   * @return
   */
  Boolean updateEmployeeAddressWithWorkFlow(String userName,
      AddressInformationDTO addressInformationDTO) throws BusinessException, SystemException;

  /**
   * update personalInformation
   * 
   * @param userName
   * @return the employee personal information
   */
  Boolean updatePersonalInformation(String userName, PersonalInformationDTO personalInformationDTO)
      throws BusinessException, SystemException;

  /**
   * update personalInformation with activiti workflow
   * 
   * @param userName
   * @return the employee personal information
   */
  Boolean updatePersonalInformationWithWorkFlow(String userName,
      PersonalInformationDTO personalInformationDTO) throws BusinessException, SystemException;

  /**
   * verify user is exists or not
   * 
   * @param userName
   * @return
   */
  EhcmEmpPerInfo checkEmployee(String userName) throws BusinessException;

  /**
   * 
   * @param userName
   * @param dependentInformationDTO
   * @return
   * @throws BusinessException
   * @throws SystemException
   */
  Boolean addDependent(String userName, DependentInformationDTO dependentInformationDTO)
      throws BusinessException, SystemException;

  /**
   * 
   * @param userName
   * @param dependentId
   * @return true or false
   */
  Boolean removeDependent(String userName, String dependentId)
      throws BusinessException, SystemException;

  /**
   * 
   * @param userName
   * @param additionalInformationDTO
   * @return
   * @throws BusinessException
   * @throws SystemException
   */
  Boolean updateContactInformation(String userName,
      EmployeeAdditionalInformationDTO additionalInformationDTO)
      throws BusinessException, SystemException;

  /**
   * 
   * @param userName
   * @param NationalID
   * @return EhcmDependents
   * @throws SystemException
   * @throws BusinessException
   */
  EhcmDependents getDependentByNationalId(String userName, String NationalID)
      throws SystemException, BusinessException;

  /**
   * 
   * @param userName
   * @param photoBytes
   * @return
   * @throws SystemException
   * @throws BusinessException
   */
  Boolean updateProfilePhoto(String userName, String photoBytes)
      throws SystemException, BusinessException;

  // /**
  // * Approve the request applied by employee
  // *
  // * @param requesterUsername
  // */
  // void approveTask(String requesterUsername, String taskId);
  //
  // /**
  // * * Reject the request applied by employee
  // *
  // * @param requesterUsername
  // */
  // void rejectTask(String requesterUsername, String taskId);

}
