package sa.elm.ob.hcm.dao.businessTrip;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.geography.City;
import org.openbravo.model.common.geography.Country;
import org.openbravo.service.db.DalConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMBusMissionSummary;
import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMMisCatPeriod;
import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.EHCMMissionCategory;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.empBusinessMission.EmpBusinessMissionDAO;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dto.businessTrips.BusinessTripRequestDTO;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.hcm.util.UtilityDAO;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.Utility;

@Repository
public class BusinessTripDAOimpl implements BusinessTripDAO {

  private static final Logger log = LoggerFactory.getLogger(BusinessTripDAOimpl.class);

  private static final String OPEN_BRAVO_DATE_FORMAT = "dd-MM-yyyy";
  private static final String OPEN_BRAVO_GREG_DATE_FORMAT = "yyyy-MM-dd";

  DateFormat dateYearFormat = sa.elm.ob.utility.util.Utility.dateFormat;

  // private static final Connection conn = OBDal.getInstance().getConnection();

  @Autowired
  EmpBusinessMissionDAO businessMissionDAO;

  @Autowired
  MissionCategoryDAO missionCategoryDAO;

  @Autowired
  EmployeeProfileDAO employeeProfileDAO;

  @Override
  public EHCMEmpBusinessMission createBusinessMission(EhcmEmpPerInfo employeeOB,
      BusinessTripRequestDTO businessTripRequestDTO, String decisionType)
      throws SystemException, BusinessException {
    ConnectionProvider conn = new DalConnectionProvider();
    final String userLang = SecurityUtils.getUserLanguage();
    // find user
    User userOB = employeeProfileDAO.getUserDetailsByUserName(employeeOB.getSearchKey());
    // TODO Auto-generated method stub
    EHCMEmpBusinessMission businessMissionOB = null;
    // find employment info by employee id
    EmploymentInfo employmentInfoOB = findEmploymentInfo(employeeOB.getId());
    // find business mission category
    EHCMMissionCategory businessMissionCategoryOB = findBusinessMissionCategory(
        businessTripRequestDTO.getMissionCategory(), employeeOB.getClient().getId());
    if (businessMissionCategoryOB == null) {

      throw new BusinessException(
          Resource.getProperty(MessageKeys.BUSINESS_MISSION_CATEGORY_NOT_AVAILABLE, userLang));
    }
    // find business Mission Type type
    EHCMDeflookupsTypeLn missionTypeLookupOB = findMissionTypeLookUp(
        businessTripRequestDTO.getMissionType());
    if (missionTypeLookupOB == null) {

      throw new BusinessException(
          Resource.getProperty(MessageKeys.BUSINESS_MISSION_TYPE_NOT_AVAILABLE, userLang));
    }
    try {
      OBContext.setAdminMode(true);
      businessMissionOB = OBProvider.getInstance().get(EHCMEmpBusinessMission.class);
      businessMissionOB.setOrganization(employeeOB.getOrganization());
      businessMissionOB.setClient(employeeOB.getClient());
      businessMissionOB.setCreationDate(new java.util.Date());
      businessMissionOB.setCreatedBy(userOB);
      businessMissionOB.setUpdated(new java.util.Date());
      businessMissionOB.setUpdatedBy(userOB);
      businessMissionOB.setEmployee(employeeOB);
      businessMissionOB.setAssignedDepartment(employmentInfoOB.getSECDeptName());
      businessMissionOB.setEmployeeType(employeeOB.getEhcmActiontype().getPersonType());
      businessMissionOB.setEmployeeName(employeeOB.getArabicfullname());
      businessMissionOB.setHireDate(employeeOB.getHiredate());
      businessMissionOB.setDepartmentCode(employmentInfoOB.getPosition().getDepartment());
      businessMissionOB.setSectionCode(employmentInfoOB.getPosition().getSection());
      businessMissionOB.setPosition(employmentInfoOB.getPosition());
      businessMissionOB.setGrade(employmentInfoOB.getGrade());
      businessMissionOB.setEmploymentGrade(employmentInfoOB.getEmploymentgrade());
      businessMissionOB.setGradeClassifications(employeeOB.getGradeClass());
      businessMissionOB.setJobTitle(employmentInfoOB.getPosition().getJOBName().getJOBTitle());
      if (employeeOB.isEnabled())
        businessMissionOB.setEmployeeStatus(Constants.EMPSTATUS_ACTIVE);
      else
        businessMissionOB.setEmployeeStatus(Constants.EMPSTATUS_INACTIVE);
      businessMissionOB.setDecisionType(decisionType);

      businessMissionOB.setDecisionNo(org.openbravo.erpCommon.utility.Utility.getDocumentNo(conn,
          employeeOB.getClient().getId(), Constants.DECISION_NUMBER_SEQUENCE, true));

      try {
        businessMissionOB.setStartDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
            Utility.convertToGregorian(businessTripRequestDTO.getStartDate())));
        businessMissionOB.setEndDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
            Utility.convertToGregorian(businessTripRequestDTO.getEndDate())));
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        throw new SystemException(MessageKeys.DATE_PARSE_EXCEPTION);
      }
      businessMissionOB.setMissionCategory(businessMissionCategoryOB);
      businessMissionOB.setMissionType(missionTypeLookupOB);
      // start date and end date format--dd-MM-yyyy
      // find mission balance days
      Long missionBalanceDays = UtilityDAO.getMissionBalanceDays(
          businessMissionCategoryOB.getClient().getId(), businessMissionCategoryOB,
          DateUtils.formatDate(Utility.convertToGregorian(businessTripRequestDTO.getStartDate()),
              OPEN_BRAVO_DATE_FORMAT),
          DateUtils.formatDate(Utility.convertToGregorian(businessTripRequestDTO.getEndDate()),
              OPEN_BRAVO_DATE_FORMAT),
          employeeOB.getId());
      if (missionBalanceDays == 0) {

        throw new BusinessException(
            Resource.getProperty(MessageKeys.BUSINESS_MISSION_BALANCE_NOT_AVAILABLE, userLang));
      } else {
        businessMissionOB.setMissionBalance(missionBalanceDays);
      }
      // find from country
      Country fromCountyOB = sa.elm.ob.hcm.util.Utility
          .getCountryOB(businessTripRequestDTO.getFromCountry());
      if (fromCountyOB == null) {
        throw new BusinessException(Resource
            .getProperty(MessageKeys.BUSINESS_MISSION_FROM_COUNTRY_NOT_AVAILABLE, userLang));
      } else {
        businessMissionOB.setFromCountry(fromCountyOB);
      }
      // find to country
      Country toCountryOB = sa.elm.ob.hcm.util.Utility
          .getCountryOB(businessTripRequestDTO.getToCountry());
      if (toCountryOB == null) {
        throw new BusinessException(
            Resource.getProperty(MessageKeys.BUSINESS_MISSION_TO_COUNTRY_NOT_AVAILABLE, userLang));
      } else {
        businessMissionOB.setToCountry(toCountryOB);
      }
      // from city
      City fromCityOB = sa.elm.ob.hcm.util.Utility.getCityOB(businessTripRequestDTO.getFromCity());
      if (fromCityOB == null) {
        throw new BusinessException(
            Resource.getProperty(MessageKeys.BUSINESS_MISSION_FROM_CITY_NOT_AVAILABLE, userLang));
      } else {
        businessMissionOB.setFromCity(fromCityOB);
      }
      // to city
      City toCityOB = sa.elm.ob.hcm.util.Utility.getCityOB(businessTripRequestDTO.getToCity());
      if (toCityOB == null) {

        throw new BusinessException(
            Resource.getProperty(MessageKeys.BUSINESS_MISSION_FROM_CITY_NOT_AVAILABLE, userLang));
      } else {
        businessMissionOB.setToCity(toCityOB);
      }
      businessMissionOB.setNoofdaysAfter(new Long(businessTripRequestDTO.getNoOfDaysAfter()));
      businessMissionOB.setNoofdaysBefore(new Long(businessTripRequestDTO.getNoOfDaysBefore()));
      businessMissionOB.setFoodProvided(businessTripRequestDTO.getFoodProvided());
      businessMissionOB.setHousingProvided(businessTripRequestDTO.getHousingProvided());
      businessMissionOB.setTicketsProvided(businessTripRequestDTO.getTicketsProvided());
      businessMissionOB.setRoundTrip(businessTripRequestDTO.getRoundTrip());
      businessMissionOB.setTaskDescription(businessTripRequestDTO.getTaskDescription());
      businessMissionOB.setMissionDays(Long.valueOf(businessTripRequestDTO.getMissionDays()));
      businessMissionOB.setLetterNo(businessTripRequestDTO.getLetterNo());
      businessMissionOB.setLetterDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
          Utility.convertToGregorian(businessTripRequestDTO.getLetterDate())));
      // payment details only for business payment
      if (decisionType.equals(Constants.DECISION_TYPE_BUSINESSMISSION_PAYMENT)) {
        businessMissionOB.setPaymentAmt(
            new BigDecimal(businessTripRequestDTO.getPaymentDetails().getPaymentAmount()));
        businessMissionOB.setAdvanceAmount(
            new BigDecimal(businessTripRequestDTO.getPaymentDetails().getAdvanceAmount()));
        businessMissionOB.setAdvancePercentage(
            new BigDecimal(businessTripRequestDTO.getPaymentDetails().getAdvancePercentage()));
      }

      if (decisionType.equals(Constants.CANCEL_DECISION)
          || decisionType.equals(Constants.DECISION_TYPE_BUSINESSMISSION_PAYMENT)) {
        EHCMEmpBusinessMission orignalDecision = getBusinessTripByDecisionNo(
            businessTripRequestDTO.getOrginalDecNo()).get(0);
        businessMissionOB.setOriginalDecisionNo(orignalDecision);
      }

      OBDal.getInstance().save(businessMissionOB);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Error while Creating Business Mission ->Mission-->", e);
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
    return businessMissionOB;
  }

  /**
   * find mission type
   * 
   * @param missionType
   * @return
   */
  private EHCMDeflookupsTypeLn findMissionTypeLookUp(String missionType) {
    // TODO Auto-generated method stub
    EHCMDeflookupsTypeLn missionTypeLookUpOB = null;
    try {
      OBContext.setAdminMode(true);
      OBQuery<EHCMDeflookupsTypeLn> misssionTypeLookUpQry = OBDal.getInstance()
          .createQuery(EHCMDeflookupsTypeLn.class, " searchKey=:missionType or name=:name");
      misssionTypeLookUpQry.setNamedParameter("missionType", missionType);
      misssionTypeLookUpQry.setNamedParameter("name", missionType);
      misssionTypeLookUpQry.setFilterOnReadableClients(false);
      misssionTypeLookUpQry.setFilterOnReadableOrganization(false);
      if (misssionTypeLookUpQry.list().size() > 0) {
        missionTypeLookUpOB = misssionTypeLookUpQry.list().get(0);
      }
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return missionTypeLookUpOB;
  }

  /**
   * Find business mission category
   * 
   * @param missionCategory
   * @return
   */
  private EHCMMissionCategory findBusinessMissionCategory(String missionCategory, String clientId) {
    // TODO Auto-generated method stub
    EHCMMissionCategory businessMissionCategoryOB = null;
    try {
      OBContext.setAdminMode(true);
      OBQuery<EHCMMissionCategory> missionCategoryQuery = OBDal.getInstance().createQuery(
          EHCMMissionCategory.class, " as e where e.name=:name and e.client.id=:clientId");
      missionCategoryQuery.setNamedParameter("name", missionCategory);
      missionCategoryQuery.setNamedParameter("clientId", clientId);
      missionCategoryQuery.setFilterOnReadableClients(false);
      missionCategoryQuery.setFilterOnReadableOrganization(false);
      if (missionCategoryQuery.list().size() > 0) {
        businessMissionCategoryOB = missionCategoryQuery.list().get(0);
      }
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return businessMissionCategoryOB;
  }

  /**
   * Find employment info by employee id
   * 
   * @param employeeId
   * @return
   */
  private EmploymentInfo findEmploymentInfo(String employeeId) {

    EmploymentInfo employmentInfoOB = null;
    try {
      OBContext.setAdminMode(true);
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and enabled='Y' order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", employeeId);
      empInfo.setFilterOnReadableClients(false);
      empInfo.setFilterOnReadableOrganization(false);
      if (empInfo.list().size() > 0) {
        employmentInfoOB = empInfo.list().get(0);
      }
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return employmentInfoOB;
  }

  @Override
  public void createBusinessMissionSummary(EHCMEmpBusinessMission businessMissionOB,
      String decisionType) {
    // TODO Auto-generated method stub
    try {
      OBContext.setAdminMode();
      EHCMBusMissionSummary businessMissSummary = null;
      businessMissSummary = businessMissionDAO.getActEmpBusinessMissSummary(businessMissionOB);
      String userid = businessMissionOB.getCreatedBy().getId();
      String clientId = businessMissionOB.getClient().getId();
      EHCMMissionCategory missCategory = null;
      EHCMMisCatPeriod misCatPrd = null;
      EHCMMiscatEmployee misCatEmp = null;
      // find employee
      EhcmEmpPerInfo employeeOB = OBDal.getInstance().get(EhcmEmpPerInfo.class,
          businessMissionOB.getEmployee().getId());

      if (!decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND))
        misCatPrd = sa.elm.ob.hcm.util.UtilityDAO.getMissionPeriod(clientId,
            businessMissionOB.getMissionCategory(),
            dateYearFormat.format(businessMissionOB.getStartDate()),
            dateYearFormat.format(businessMissionOB.getEndDate()));
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
        misCatPrd = sa.elm.ob.hcm.util.UtilityDAO.getMissionPeriod(clientId, missCategory,
            dateYearFormat.format(businessMissionOB.getExtendStartdate()),
            dateYearFormat.format(businessMissionOB.getExtendEnddate()));
      }
      if (misCatPrd != null) {
        misCatEmp = missionCategoryDAO.getEmployeeinPeriod(misCatPrd, employeeOB.getId());
      }
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        // insert employee info record
        businessMissionDAO.insertBusMissionSummary(businessMissionOB, businessMissSummary, userid,
            decisionType);
        businessMissionDAO.updateMissionBalance(misCatEmp, decisionType, businessMissionOB, userid);
      }
      // update case
      else if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
          || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
          || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        // update record in business mission summary
        businessMissionDAO.updateBusMissionSummary(businessMissionOB, businessMissSummary,
            decisionType);

        businessMissionDAO.updateMissionBalance(misCatEmp, decisionType, businessMissionOB, userid);
      }
      // cancel case
      else if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {

        // remove the created business mission summary record
        businessMissionDAO.removebusinessMissionActRecord(businessMissionOB);
        businessMissionDAO.updateMissionBalance(misCatEmp, decisionType, businessMissionOB, userid);

      }
      // business & Payment case
      else if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_BUSINESSMISSION_PAYMENT)) {
        businessMissionDAO.updatePaymentFlag(businessMissionOB.getEmployee().getId(),
            businessMissionOB.getOriginalDecisionNo().getId(), false);

      }
      businessMissionDAO.updateEmpBusMissionStatus(businessMissionOB);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public List<String> getAllOriginalDecisionNoByUserName(String username) {
    // TODO Auto-generated method stub
    List<String> decisionNumberList = new ArrayList<String>();
    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
    for (EHCMEmpBusinessMission businessMissionOB : employeeOB.getEHCMEmpBusinessMissionList()) {
      decisionNumberList.add(businessMissionOB.getDecisionNo());
    }
    return decisionNumberList;
  }

  @Override
  public List<EHCMEmpBusinessMission> getBusinessTripByDecisionNo(String originalDecNo) {
    // TODO Auto-generated method stub

    List<EHCMEmpBusinessMission> objBusinessTripList = new ArrayList<EHCMEmpBusinessMission>();
    OBQuery<EHCMEmpBusinessMission> objBusinessTripQuery = null;
    try {
      OBContext.setAdminMode(true);
      String query = " as e where e.decisionNo=:decisionNumber  ";
      objBusinessTripQuery = OBDal.getInstance().createQuery(EHCMEmpBusinessMission.class, query);
      objBusinessTripQuery.setNamedParameter("decisionNumber", originalDecNo);

      objBusinessTripQuery.setFilterOnReadableClients(false);
      objBusinessTripQuery.setFilterOnReadableOrganization(false);
      objBusinessTripQuery.setMaxResult(1);
      objBusinessTripList = objBusinessTripQuery.list();
      return objBusinessTripList;
    } catch (OBException e) {
      log.error(
          "Error while fetching business trip details -> for decision number-->" + originalDecNo);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
