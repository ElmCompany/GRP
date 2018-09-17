package sa.elm.ob.hcm.dao.profile;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.City;
import org.openbravo.model.common.geography.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMEmpAddress;
import sa.elm.ob.hcm.EhcmDependents;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmTitletype;
import sa.elm.ob.hcm.dto.profile.AddressInformationDTO;
import sa.elm.ob.hcm.dto.profile.DependentInformationDTO;
import sa.elm.ob.hcm.dto.profile.EmployeeAdditionalInformationDTO;
import sa.elm.ob.hcm.dto.profile.PersonalInformationDTO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.dao.lookup.LookUpDAO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.utility.util.DateUtils;

@Repository
public class EmployeeProfileUpdateDAOImpl implements EmployeeProfileUpdateDAO {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(EmployeeProfileDAOImpl.class);

  private static final String OPEN_BRAVO_DATE_FORMAT = "dd-MM-yyyy";

  @Autowired
  private EmployeeProfileDAO employeeProfileDAO;

  @Autowired
  private LookUpDAO lookUpDAO;

  /**
   * find Title by Title Name
   * 
   * @param titleName
   * @param employeeOB
   * @return
   */
  private EhcmTitletype findTitleByTitleName(String titleName, EhcmEmpPerInfo employeeOB) {

    try {
      OBContext.setAdminMode();
      final OBCriteria<EhcmTitletype> obtitle = OBDal.getInstance()
          .createCriteria(EhcmTitletype.class);
      obtitle.add(Restrictions.eq(EhcmTitletype.PROPERTY_NAME, titleName));
      obtitle.add(Restrictions.eq(EhcmTitletype.PROPERTY_CLIENT, employeeOB.getClient().getId()));
      obtitle.setFilterOnReadableClients(false);
      obtitle.setFilterOnReadableOrganization(false);
      final EhcmTitletype titleOB = (EhcmTitletype) obtitle.uniqueResult();
      return titleOB;
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Find Dependents by nationalId and employee id
   * 
   * @param dependentInformationDTO
   * @param employeeId
   * @return
   */
  private EhcmDependents findDependentByNationalId(String nationalID, EhcmEmpPerInfo employeeOB) {

    try {
      OBContext.setAdminMode();
      final OBCriteria<EhcmDependents> obDependent = OBDal.getInstance()
          .createCriteria(EhcmDependents.class);
      obDependent.add(Restrictions.eq(EhcmDependents.PROPERTY_NATIONALIDENTIFIER, nationalID));
      obDependent.add(Restrictions.eq(EhcmDependents.PROPERTY_CLIENT, employeeOB.getClient()));
      obDependent.add(Restrictions.eq(EhcmDependents.PROPERTY_EHCMEMPPERINFO, employeeOB));
      obDependent.setFilterOnReadableClients(false);
      obDependent.setFilterOnReadableOrganization(false);
      final EhcmDependents dependentOB = (EhcmDependents) obDependent.list().get(0);
      return dependentOB;
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public void updateEmployeeProfileByUser(EhcmEmpPerInfo employeeOB,
      PersonalInformationDTO personalInformationDTO) throws SystemException {
    try {
      OBContext.setAdminMode();

      employeeOB.setName(personalInformationDTO.getFirstNameEn());
      employeeOB.setArabicname(personalInformationDTO.getFirstNameAr());
      employeeOB.setArabicfatname(personalInformationDTO.getFatherNameAr());
      employeeOB.setFathername(personalInformationDTO.getFatherNameEn());
      employeeOB.setArbgrafaname(personalInformationDTO.getGrandFatherNameAr());
      employeeOB.setGrandfathername(personalInformationDTO.getGrandFatherNameEn());
      employeeOB.setArabicfamilyname(personalInformationDTO.getFamilyNameAr());
      employeeOB.setFamilyname(personalInformationDTO.getFamilyNameEn());
      if (null != personalInformationDTO.getDob()) {
        try {
          employeeOB.setDob(DateUtils.convertStringToDate(OPEN_BRAVO_DATE_FORMAT,
              personalInformationDTO.getDob()));
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }
      employeeOB.setLookupTitle(lookUpDAO.findSubLookupByCode(personalInformationDTO.getTitle()));

      employeeOB.setNationalityIdentifier(personalInformationDTO.getNationalId());
      employeeOB.setLookupMaritalStatus(
          lookUpDAO.findSubLookupByCode(personalInformationDTO.getMaritalStatus()));
      employeeOB.setLookupGender(lookUpDAO.findSubLookupByCode(personalInformationDTO.getGender()));
      OBDal.getInstance().save(employeeOB);
      OBDal.getInstance().flush();
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public void updateEmployeeDependent(EhcmEmpPerInfo employeeOB,
      DependentInformationDTO dependentInformationDTO) throws SystemException {

    try {
      OBContext.setAdminMode();
      // find dependent
      EhcmDependents ehcmDependents = findDependentByNationalId(
          dependentInformationDTO.getNationalId(), employeeOB);

      ehcmDependents.setRelationship(dependentInformationDTO.getRelationship());
      ehcmDependents.setFirstName(dependentInformationDTO.getFirstNameEn());
      ehcmDependents.setFathername(dependentInformationDTO.getFatherNameAr());
      ehcmDependents.setGrandfather(dependentInformationDTO.getGrandFatherNameAr());
      ehcmDependents.setFamily(dependentInformationDTO.getFatherNameAr());
      if (null != dependentInformationDTO.getDob()) {
        try {
          ehcmDependents.setDob(DateUtils.convertStringToDate(OPEN_BRAVO_DATE_FORMAT,
              dependentInformationDTO.getDob()));
        } catch (ParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      ehcmDependents
          .setLookupGender(lookUpDAO.findSubLookupByCode(dependentInformationDTO.getGender()));
      // TODO: remove this line after removing not null constraint in the table.
      ehcmDependents.setGender(dependentInformationDTO.getGender());
      OBDal.getInstance().save(ehcmDependents);
      OBDal.getInstance().flush();
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public void updateEmployeeAddress(EhcmEmpPerInfo employeeOB,
      AddressInformationDTO addressInformationDTO) throws SystemException, BusinessException {

    try {
      OBContext.setAdminMode();
      EHCMEmpAddress ehcmEmpAddress = null;// Need to
      // check
      // if
      // there
      // can be
      // multiple
      // addresses
      // get country id based on country name
      if (employeeOB.getEHCMEmpAddressList().size() > 0) {
        ehcmEmpAddress = employeeOB.getEHCMEmpAddressList().get(0);
      } else {
        ehcmEmpAddress = OBProvider.getInstance().get(EHCMEmpAddress.class);
        ehcmEmpAddress.setClient(employeeOB.getClient());
        ehcmEmpAddress.setOrganization(employeeOB.getOrganization());
        ehcmEmpAddress.setCreatedBy(employeeOB.getCreatedBy());
        ehcmEmpAddress.setUpdated(new Date());
        ehcmEmpAddress.setUpdatedBy(employeeOB.getUpdatedBy());
        ehcmEmpAddress.setCreationDate(new Date());
        ehcmEmpAddress.setEhcmEmpPerinfo(employeeOB);
      }
      final OBCriteria<Country> obc = OBDal.getInstance().createCriteria(Country.class);
      obc.add(Restrictions.eq(Country.PROPERTY_NAME, addressInformationDTO.getCountry()));
      obc.setFilterOnReadableClients(false);
      obc.setFilterOnReadableOrganization(false);
      final Country countryOB = (Country) obc.uniqueResult();
      ehcmEmpAddress.setCountry(countryOB);
      // get city id based on city name
      final OBCriteria<City> obcity = OBDal.getInstance().createCriteria(City.class);
      obcity.add(Restrictions.eq(City.PROPERTY_NAME, addressInformationDTO.getCity()));
      obcity.setFilterOnReadableClients(false);
      obcity.setFilterOnReadableOrganization(false);
      final City cityOB = (City) obcity.uniqueResult();
      ehcmEmpAddress.setCity(cityOB);
      // addressInformationDTO.setRegion(ehcmEmpAddress.get); Need to confirm from
      // where to get the
      // region
      ehcmEmpAddress.setDistrict(addressInformationDTO.getCity());
      ehcmEmpAddress.setStreet(addressInformationDTO.getStreet());
      ehcmEmpAddress.setPostBox(addressInformationDTO.getPostBox());
      ehcmEmpAddress.setPostalCode(addressInformationDTO.getPostalCode());
      ehcmEmpAddress.setAddressLine1(addressInformationDTO.getAddressLine1());
      ehcmEmpAddress.setAddressLine2(addressInformationDTO.getAddressLine2());
      OBDal.getInstance().save(ehcmEmpAddress);
      OBDal.getInstance().flush();

    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public void addDependent(String userName, DependentInformationDTO dependentInformationDTO)
      throws SystemException {

    try {
      OBContext.setAdminMode();
      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);
      // find dependent
      EhcmDependents ehcmDependents = OBProvider.getInstance().get(EhcmDependents.class);

      ehcmDependents.setRelationship(dependentInformationDTO.getRelationship());
      ehcmDependents.setFirstName(dependentInformationDTO.getFirstNameAr());
      ehcmDependents.setFathername(dependentInformationDTO.getFatherNameAr());
      ehcmDependents.setGrandfather(dependentInformationDTO.getGrandFatherNameAr());
      ehcmDependents.setFamily(dependentInformationDTO.getFatherNameAr());
      ehcmDependents.setNationalidentifier(dependentInformationDTO.getNationalId());
      if (null != dependentInformationDTO.getDob()) {
        try {
          ehcmDependents.setDob(DateUtils.convertStringToDate(OPEN_BRAVO_DATE_FORMAT,
              dependentInformationDTO.getDob()));
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }
      ehcmDependents
          .setLookupGender(lookUpDAO.findSubLookupByCode(dependentInformationDTO.getGender()));
      ehcmDependents.setEhcmEmpPerinfo(employeeOB);
      ehcmDependents.setOrganization(employeeOB.getOrganization());
      ehcmDependents.setClient(employeeOB.getClient());
      ehcmDependents.setCreationDate(new java.util.Date());
      ehcmDependents.setCreatedBy(employeeOB.getCreatedBy());
      ehcmDependents.setUpdated(new java.util.Date());
      ehcmDependents.setUpdatedBy(employeeOB.getUpdatedBy());
      OBDal.getInstance().save(ehcmDependents);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public void removeDependent(String userName, String nationalId)
      throws SystemException, BusinessException {

    final String userLang = SecurityUtils.getUserLanguage();
    try {
      OBContext.setAdminMode();
      // find dependent
      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);
      EhcmDependents ehcmDependents = findDependentByNationalId(nationalId, employeeOB);
      if (ehcmDependents == null)
        throw new BusinessException(
            Resource.getProperty(MessageKeys.DEPENDENT_NOT_AVAILABLE, userLang));
      else
        OBDal.getInstance().remove(ehcmDependents);
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public void updateContactInformation(String userName,
      EmployeeAdditionalInformationDTO additionalInformationDTO)
      throws SystemException, BusinessException {

    try {
      OBContext.setAdminMode();
      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);
      employeeOB.setWorkno(additionalInformationDTO.getWorkno());
      employeeOB.setEmail(additionalInformationDTO.getEmail());
      employeeOB.setMobno(additionalInformationDTO.getMobno());
      employeeOB.setHomeno(additionalInformationDTO.getHomeno());
      OBDal.getInstance().save(employeeOB);
      OBDal.getInstance().flush();

    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public EhcmDependents findDependentByNationalId(String userName, String NationalID)
      throws SystemException, BusinessException {

    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);
    EhcmDependents ehcmDependents = findDependentByNationalId(NationalID, employeeOB);
    return ehcmDependents;
  }

  @Override
  public void updateProfilePhoto(String userName, String PhotoBytes)
      throws SystemException, BusinessException {

    try {
      // find dependent
      OBContext.setAdminMode();
      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);
      Image imgOB = OBProvider.getInstance().get(Image.class);
      imgOB.setClient(OBDal.getInstance().get(Client.class, employeeOB.getClient()));
      imgOB.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
      imgOB.setName(employeeOB.getName());
      imgOB.setBindaryData(Base64.decodeBase64(PhotoBytes));
      imgOB.setWidth(new Long(200));
      imgOB.setHeight(new Long(200));
      // imgOB.setMimetype(mimetype);
      OBDal.getInstance().save(imgOB);
      OBDal.getInstance().flush();
      employeeOB.setCIVAdImage(imgOB);
      OBDal.getInstance().save(employeeOB);
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
