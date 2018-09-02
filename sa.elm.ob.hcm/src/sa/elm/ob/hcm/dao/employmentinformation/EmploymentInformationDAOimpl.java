package sa.elm.ob.hcm.dao.employmentinformation;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EhcmEmpAsset;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmpreviouservice;
import sa.elm.ob.hcm.ehcmqualification;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dto.employment.CertificationsDTO;
import sa.elm.ob.hcm.dto.employment.QualificationsDTO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.Utility;

@Repository
public class EmploymentInformationDAOimpl implements EmploymentInformationDAO {
  private static final Logger log = LoggerFactory.getLogger(EmploymentInformationDAOimpl.class);

  // private static final String OPEN_BRAVO_DATE_FORMAT = "dd-MM-yyyy";
  private static final String OPEN_BRAVO_GREG_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

  @Autowired
  EmployeeProfileDAO employeeProfileDAO;

  @Override
  public List<ehcmpreviouservice> getAllPreviousEmploymentInformationByUsername(String username)
      throws SystemException {

    List<ehcmpreviouservice> previousEmploymentList = new ArrayList<>();
    try {
      OBContext.setAdminMode();

      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
      if (employeeOB != null && employeeOB.getEhcmPreviouServiceList().size() > 0) {
        previousEmploymentList = employeeOB.getEhcmPreviouServiceList();
      }
      return previousEmploymentList;
    } catch (Exception e) {
      log.debug("error while fetching employement details for emmployee-->" + username);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public List<ehcmqualification> getAllQualificationByUserName(String username) {

    List<ehcmqualification> qualificationList = new ArrayList<>();
    try {
      OBContext.setAdminMode();

      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
      if (employeeOB != null && employeeOB.getEhcmQualificationList().size() > 0) {
        qualificationList = employeeOB.getEhcmQualificationList();
      }
      return qualificationList;
    } catch (Exception e) {
      log.debug("error while fetching qualification details for emmployee-->" + username);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public void submitQualification(String username, QualificationsDTO qualificationsDTO)
      throws SystemException, BusinessException {
    final String userLang = SecurityUtils.getUserLanguage();
    try {
      OBContext.setAdminMode();

      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);

      ehcmqualification qualificationOB = OBProvider.getInstance().get(ehcmqualification.class);
      qualificationOB.setClient(employeeOB.getClient());
      qualificationOB.setOrganization(employeeOB.getOrganization());
      qualificationOB.setCreatedBy(employeeOB.getCreatedBy());
      qualificationOB.setUpdated(new Date());
      qualificationOB.setUpdatedBy(employeeOB.getUpdatedBy());
      qualificationOB.setCreationDate(new Date());
      qualificationOB.setEhcmEmpPerinfo(employeeOB);
      qualificationOB.setEdulevel(qualificationsDTO.getEducationLevel());
      qualificationOB.setDegree(qualificationsDTO.getDegree());
      qualificationOB.setLocation(qualificationsDTO.getLocation());
      qualificationOB.setLicensesub(qualificationsDTO.getMajor());

      if (null != qualificationsDTO.getStartDate()) {
        try {
          qualificationOB.setStartDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
              Utility.convertToGregorian(qualificationsDTO.getStartDate())));
        } catch (ParseException e) {

          e.printStackTrace();
          throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
        }
      }

      if (null != qualificationsDTO.getEndDate()) {
        try {
          qualificationOB.setEndDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
              Utility.convertToGregorian(qualificationsDTO.getEndDate())));
        } catch (ParseException e) {

          e.printStackTrace();
          throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
        }
      }

      if (null != qualificationsDTO.getExpiryDate()) {
        try {
          qualificationOB.setExpirydate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
              Utility.convertToGregorian(qualificationsDTO.getExpiryDate())));
        } catch (ParseException e) {
          e.printStackTrace();
          throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
        }
      }

      OBDal.getInstance().save(qualificationOB);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log.debug("error while submitting qualification details for emmployee-->" + username);
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public void submitCertification(String username, CertificationsDTO certificationsDTO)
      throws SystemException, BusinessException {
    final String userLang = SecurityUtils.getUserLanguage();
    try {
      OBContext.setAdminMode();

      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);

      ehcmqualification qualificationOB = OBProvider.getInstance().get(ehcmqualification.class);
      qualificationOB.setClient(employeeOB.getClient());
      qualificationOB.setOrganization(employeeOB.getOrganization());
      qualificationOB.setCreatedBy(employeeOB.getCreatedBy());
      qualificationOB.setUpdated(new Date());
      qualificationOB.setUpdatedBy(employeeOB.getUpdatedBy());
      qualificationOB.setCreationDate(new Date());
      qualificationOB.setEhcmEmpPerinfo(employeeOB);
      qualificationOB.setEdulevel("C");// based on education level --identify as certification
      qualificationOB.setDegree(certificationsDTO.getSubject());
      qualificationOB.setLocation(certificationsDTO.getLocation());
      qualificationOB.setLicensesub(certificationsDTO.getLicense());

      if (null != certificationsDTO.getStartDate()) {
        try {
          qualificationOB.setStartDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
              Utility.convertToGregorian(certificationsDTO.getStartDate())));
        } catch (ParseException e) {
          e.printStackTrace();
          Resource.getProperty(MessageKeys.ERROR, userLang);
        }
      }

      if (null != certificationsDTO.getEndDate()) {
        try {
          qualificationOB.setEndDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
              Utility.convertToGregorian(certificationsDTO.getEndDate())));
        } catch (ParseException e) {
          throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
        }
      }

      if (null != certificationsDTO.getExpiryDate()) {
        try {
          qualificationOB.setExpirydate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
              Utility.convertToGregorian(certificationsDTO.getExpiryDate())));
        } catch (ParseException e) {
          e.printStackTrace();
          throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
        }
      }

      OBDal.getInstance().save(qualificationOB);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.debug("error while submitting certification details for emmployee-->" + username);
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public List<EhcmEmpAsset> getAllCustodies(String username) {

    List<EhcmEmpAsset> custodyList = new ArrayList<>();
    try {
      OBContext.setAdminMode();

      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
      if (employeeOB != null && employeeOB.getEhcmEmpAssetList().size() > 0) {
        custodyList = employeeOB.getEhcmEmpAssetList();
      }
      return custodyList;
    } catch (Exception e) {
      log.debug("error while fetching custody details for emmployee-->" + username);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public List<EmploymentInfo> getCurrentEmploymentInfoByUserName(String userName)
      throws BusinessException, SystemException {
    final String userLang = SecurityUtils.getUserLanguage();
    List<EmploymentInfo> employmentList = new ArrayList<EmploymentInfo>();
    try {
      OBContext.setAdminMode();

      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);
      if (employeeOB != null && employeeOB.getEhcmEmploymentInfoList().size() > 0) {
        employmentList = employeeOB.getEhcmEmploymentInfoList();
      } else {
        throw new BusinessException(
            Resource.getProperty(MessageKeys.EMPLOYMENT_INFORMATION_NOT_AVAILABLE, userLang));
      }

    } catch (Exception e) {
      log.debug("error while fetching employment information for employee ==>" + userName);
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));

    } finally {
      OBContext.restorePreviousMode();
    }
    return employmentList;
  }

  @Override
  public ehcmqualification getQualificationById(String reecordId) {

    try {
      OBContext.setAdminMode();

      ehcmqualification qualification = OBDal.getInstance().get(ehcmqualification.class, reecordId);
      return qualification;
    } catch (Exception e) {
      log.debug(
          "error while fetching qualification details for emmployee record id-->" + reecordId);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
