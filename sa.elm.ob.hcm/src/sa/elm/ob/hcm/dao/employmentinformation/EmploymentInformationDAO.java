package sa.elm.ob.hcm.dao.employmentinformation;

import java.util.List;

import sa.elm.ob.hcm.EhcmEmpAsset;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmpreviouservice;
import sa.elm.ob.hcm.ehcmqualification;
import sa.elm.ob.hcm.dto.employment.CertificationsDTO;
import sa.elm.ob.hcm.dto.employment.QualificationsDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

public interface EmploymentInformationDAO {
  /**
   * get employment information by using user name
   * 
   * @param username
   * @return
   */
  List<ehcmpreviouservice> getAllPreviousEmploymentInformationByUsername(String username)
      throws SystemException;

  /**
   * get qualification information by using user name
   * 
   * @param username
   * @return
   */
  List<ehcmqualification> getAllQualificationByUserName(String username);

  /**
   * submit qualification by using user name
   * 
   * @param username
   * @param qualificationsDTO
   */
  void submitQualification(String username, QualificationsDTO qualificationsDTO)
      throws SystemException, BusinessException;

  /**
   * submit certification by using user name
   * 
   * @param username
   * @param certificationsDTO
   * 
   */
  void submitCertification(String username, CertificationsDTO certificationsDTO)
      throws SystemException, BusinessException;

  /**
   * get custody details by using user name
   * 
   * @param username
   * @return
   */
  List<EhcmEmpAsset> getAllCustodies(String username);

  /**
   * Get Employment Information By User Name
   * 
   * @param userName
   * @return
   */
  List<EmploymentInfo> getCurrentEmploymentInfoByUserName(String userName)
      throws SystemException, BusinessException;

  /**
   * Get Qualification Details by Employee RecordId
   * 
   * @param employeeRecordId
   * @return list of qualification
   */
  ehcmqualification getQualificationById(String recordId);

}
