package sa.elm.ob.hcm.dao.profile;

import sa.elm.ob.hcm.EhcmDependents;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.dto.profile.AddressInformationDTO;
import sa.elm.ob.hcm.dto.profile.DependentInformationDTO;
import sa.elm.ob.hcm.dto.profile.EmployeeAdditionalInformationDTO;
import sa.elm.ob.hcm.dto.profile.PersonalInformationDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

public interface EmployeeProfileUpdateDAO {
  /**
   * Update the employee personal profile by User Details
   * 
   * @param userId
   * @return
   */
  void updateEmployeeProfileByUser(EhcmEmpPerInfo employeeOB,
      PersonalInformationDTO personalInformationDTO) throws SystemException;

  /**
   * update the employee dependents details
   * 
   * @param employeeOB
   * @param dependentInformationDTO
   * @throws SystemException
   */
  void updateEmployeeDependent(EhcmEmpPerInfo employeeOB, DependentInformationDTO ehcmDependentList)
      throws SystemException;

  /**
   * update employee address details
   * 
   * @param employeeOB
   * @param addressInformationDTO
   * @throws SystemException
   */
  void updateEmployeeAddress(EhcmEmpPerInfo employeeOB, AddressInformationDTO addressInformationDTO)
      throws SystemException, BusinessException;

  /**
   * 
   * @param userName
   * @param dependentInformationDTO
   * @throws SystemException
   */
  void addDependent(String userName, DependentInformationDTO dependentInformationDTO)
      throws SystemException;

  /**
   * 
   * @param userName
   * @param dependentId
   * @throws SystemException
   */
  void removeDependent(String userName, String dependentId)
      throws SystemException, BusinessException;

  /**
   * 
   * @param userName
   * @param additionalInformationDTO
   * @throws SystemException
   * @throws BusinessException
   */
  void updateContactInformation(String userName,
      EmployeeAdditionalInformationDTO additionalInformationDTO)
      throws SystemException, BusinessException;

  /**
   * 
   * @param userName
   * @param NationalID
   * @return
   * @throws SystemException
   * @throws BusinessException
   */
  EhcmDependents findDependentByNationalId(String userName, String NationalID)
      throws SystemException, BusinessException;

  /**
   * 
   * @param userName
   * @param PhotoBytes
   * @throws SystemException
   * @throws BusinessException
   */
  void updateProfilePhoto(String userName, String PhotoBytes)
      throws SystemException, BusinessException;

}
