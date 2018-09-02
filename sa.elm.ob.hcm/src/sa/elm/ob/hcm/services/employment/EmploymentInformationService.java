package sa.elm.ob.hcm.services.employment;

import java.util.List;

import sa.elm.ob.hcm.dto.employment.CertificationsDTO;
import sa.elm.ob.hcm.dto.employment.CustodiesDTO;
import sa.elm.ob.hcm.dto.employment.QualificationsDTO;
import sa.elm.ob.hcm.dto.employment.ViewEmplInfoDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

public interface EmploymentInformationService {

  /**
   * Retrieve All previous employment information of employee
   * 
   * @param username
   * @return
   */
  List<ViewEmplInfoDTO> getAllPreviousEmploymentInfo(String username) throws SystemException;

  /**
   * Retrieve All Employment Qualifications
   * 
   * @param username
   * @return
   */
  List<QualificationsDTO> getQualifications(String username);

  /**
   * Add/Update Employment Qualifications
   * 
   * @param username
   * @param qualificationsDTO
   * @return
   */
  Boolean submitQualification(String username, QualificationsDTO qualificationsDTO)
      throws SystemException, BusinessException;

  /**
   * Add/Update Employment Qualifications with approval flow
   * 
   * @param username
   * @param qualificationsDTO
   * @return
   * @throws SystemException
   * @throws BusinessException
   */
  Boolean submitQualificationWithApproval(String username, QualificationsDTO qualificationsDTO)
      throws SystemException, BusinessException;

  /**
   * Retrieve All Employment Skills/Certifications
   * 
   * @param username
   * @return
   */
  List<CertificationsDTO> getCertifications(String username);

  /**
   * Retrieve All Certification By Employee Record Id
   * 
   * @param username
   * @return
   */
  CertificationsDTO getCertificationsById(String employeeRecordId);

  /**
   * Retrieve All Qualification By Employee Record Id
   * 
   * @param employeeRecordId
   * @return
   */
  QualificationsDTO getQualificationById(String employeeRecordId);

  /**
   * Add/Update Employment Skills/Certifications
   * 
   * @param username
   * @param certificationsDTO
   * @return
   */
  Boolean submitCertificate(String username, CertificationsDTO certificationsDTO)
      throws SystemException, BusinessException;

  /**
   * Add/Update Employment Skills/Certifications with approval flow
   * 
   * @param username
   * @param certificationsDTO
   * @return
   * @throws SystemException
   */
  Boolean submitCertificateWithApproval(String username, CertificationsDTO certificationsDTO)
      throws SystemException;

  /**
   * @param username
   * @return
   */
  List<CustodiesDTO> getAllCustodies(String username);

  /**
   * Retrieve current employment information of employee
   * 
   * @param userName
   * @return
   */
  List<ViewEmplInfoDTO> getCurrentEmploymentInfoByUserName(String userName)
      throws BusinessException, SystemException;

  // /**
  // *Approve the request applied by employee
  // *
  // * @param requesterUsername
  // */
  // void approveTask(String requesterUsername, String decisionType);
  //
  // /**
  // * * Reject the request applied by employee
  // *
  // * @param requesterUsername
  // */
  // void rejectTask(String requesterUsername);
}
