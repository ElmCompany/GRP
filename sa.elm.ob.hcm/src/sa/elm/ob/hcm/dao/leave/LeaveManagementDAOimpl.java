package sa.elm.ob.hcm.dao.leave;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.axis.utils.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.service.db.DalConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceReason;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EHCMAbsenceTypeRules;
import sa.elm.ob.hcm.EHCMEmpLeave;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.AbsenceDecision.AbsenceIssueDecisionDAO;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dto.leave.LeaveRequestDTO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.Utility;

@Repository
public class LeaveManagementDAOimpl implements LeaveManagementDAO {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(LeaveManagementDAOimpl.class);
  private static final String OPEN_BRAVO_GREG_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
  @Autowired
  AbsenceIssueDecisionDAO absenceIssueDecisionDAO;
  @Autowired
  EmployeeProfileDAO employeeProfileDAO;

  @Override
  public EHCMAbsenceType findAbsenceType(String absenceType) {

    EHCMAbsenceType objAbsenceType = null;
    OBQuery<EHCMAbsenceType> objAbsenceQry = null;
    List<EHCMAbsenceType> objAbsenceTypeList = new ArrayList<EHCMAbsenceType>();
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.searchKey=:value  ";
      objAbsenceQry = OBDal.getInstance().createQuery(EHCMAbsenceType.class, query);
      objAbsenceQry.setNamedParameter("value", absenceType);
      objAbsenceQry.setFilterOnReadableClients(false);
      objAbsenceQry.setFilterOnReadableOrganization(false);
      objAbsenceQry.setMaxResult(1);
      objAbsenceTypeList = objAbsenceQry.list();
      if (objAbsenceTypeList.size() > 0) {
        objAbsenceType = objAbsenceTypeList.get(0);
      }
    } catch (OBException e) {
      log.error("Error while fetching absence type->absence type value-->" + absenceType);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return objAbsenceType;
  }

  @Override
  public String verifyAvailableLeave(EHCMAbsenceType absenceTypeOB, EhcmEmpPerInfo objEmployeeeOB,
      LeaveRequestDTO leaveRequestDTO, String strIssueDecisionType) {

    return null;

  }

  @Override
  public EHCMAbsenceAttendance createAbsenceAttendanceForEmployee(EhcmEmpPerInfo employeeOB,
      LeaveRequestDTO leaveRequestDTO, String decisionType, EHCMAbsenceType absenceTypeOB) {
    ConnectionProvider conn = new DalConnectionProvider();
    // find user
    User userOB = employeeProfileDAO.getUserDetailsByUserName(employeeOB.getSearchKey());

    EHCMAbsenceAttendance originalAttendance = null;
    // find employment info by employee id
    EmploymentInfo employmentInfoOB = findEmploymentInfo(employeeOB.getId());
    try {
      OBContext.setAdminMode();
      // find leave request for original decision number
      if (!StringUtils.isEmpty(leaveRequestDTO.getDecisionNumber())) {
        List<EHCMAbsenceAttendance> originalAttendanceList = getLeaveRequestByDecisionNoOrByUsername(
            leaveRequestDTO.getDecisionNumber(), "");
        originalAttendance = originalAttendanceList.size() > 0 ? originalAttendanceList.get(0)
            : null;
      }

      EHCMAbsenceAttendance absenceAttendanceOB = OBProvider.getInstance()
          .get(EHCMAbsenceAttendance.class);
      if (originalAttendance != null)
        absenceAttendanceOB.setOriginalDecisionNo(originalAttendance);
      absenceAttendanceOB.setOrganization(employeeOB.getOrganization());
      absenceAttendanceOB.setClient(employeeOB.getClient());
      absenceAttendanceOB.setCreationDate(new java.util.Date());
      absenceAttendanceOB.setCreatedBy(userOB);
      absenceAttendanceOB.setUpdated(new java.util.Date());
      absenceAttendanceOB.setUpdatedBy(userOB);
      absenceAttendanceOB.setAssignedDepartment(employmentInfoOB.getSECDeptName());
      absenceAttendanceOB.setEmployeeType(employeeOB.getEhcmActiontype().getPersonType());
      absenceAttendanceOB.setEmployeeName(employeeOB.getArabicfullname());
      absenceAttendanceOB.setHireDate(employeeOB.getHiredate());
      absenceAttendanceOB.setDepartmentCode(employmentInfoOB.getPosition().getDepartment());
      absenceAttendanceOB.setSectionCode(employmentInfoOB.getPosition().getSection());
      absenceAttendanceOB.setPosition(employmentInfoOB.getPosition());
      absenceAttendanceOB.setGrade(employmentInfoOB.getGrade());
      absenceAttendanceOB.setEmploymentGrade(employmentInfoOB.getEmploymentgrade());
      absenceAttendanceOB.setGradeClassifications(employeeOB.getGradeClass());
      absenceAttendanceOB.setEhcmAbsenceType(absenceTypeOB);
      absenceAttendanceOB.setEhcmEmpPerinfo(employeeOB);
      if (employeeOB.isEnabled())
        absenceAttendanceOB.setEmployeeStatus(Constants.EMPSTATUS_ACTIVE);
      else
        absenceAttendanceOB.setEmployeeStatus(Constants.EMPSTATUS_INACTIVE);
      absenceAttendanceOB.setDecisionType(decisionType);
      absenceAttendanceOB.setDecisionNo(org.openbravo.erpCommon.utility.Utility.getDocumentNo(conn,
          employeeOB.getClient().getId(), Constants.DECISION_NUMBER_SEQUENCE, true));

      if (StringUtils.isEmpty(leaveRequestDTO.getAbsenceReason()))
        absenceAttendanceOB.setEhcmAbsenceReason(originalAttendance.getEhcmAbsenceReason());
      else
        absenceAttendanceOB
            .setEhcmAbsenceReason(findAbsenceReason(leaveRequestDTO.getAbsenceReason()));

      if (decisionType.equals(Constants.CANCEL_DECISION)
          || decisionType.equals(Constants.EXTEND_DECISION)) {
        if (decisionType.equals(Constants.CANCEL_DECISION)) {
          absenceAttendanceOB.setCancelDate(new Date());
          absenceAttendanceOB.setEndDate(originalAttendance.getEndDate());
        }
        absenceAttendanceOB.setAbsenceDays(originalAttendance.getAbsenceDays());
        absenceAttendanceOB.setStartDate(originalAttendance.getStartDate());
        if (decisionType.equals(Constants.EXTEND_DECISION)) {
          try {
            absenceAttendanceOB.setExtendLeaveDay(new Long(leaveRequestDTO.getAbsenceDays()));
            absenceAttendanceOB.setAbsenceDays(originalAttendance.getAbsenceDays()
                .add(new BigDecimal(leaveRequestDTO.getAbsenceDays())));
            absenceAttendanceOB
                .setExtendStartdate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
                    Utility.convertToGregorian(leaveRequestDTO.getStartDate())));
            absenceAttendanceOB
                .setExtendEnddate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
                    Utility.convertToGregorian(leaveRequestDTO.getEndDate())));
            absenceAttendanceOB
                .setEndDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
                    Utility.convertToGregorian(leaveRequestDTO.getEndDate())));
          } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      } else {
        absenceAttendanceOB.setAbsenceDays(new BigDecimal(leaveRequestDTO.getAbsenceDays()));
        try {
          absenceAttendanceOB
              .setStartDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
                  Utility.convertToGregorian(leaveRequestDTO.getStartDate())));
          absenceAttendanceOB.setEndDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
              Utility.convertToGregorian(leaveRequestDTO.getEndDate())));
        } catch (ParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      OBDal.getInstance().save(absenceAttendanceOB);
      OBDal.getInstance().flush();
      return absenceAttendanceOB;
    } catch (Exception e) {
      log.error("Error while creating absence decision for employee > employe -->", e);
      e.printStackTrace();
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * find employment info by employee id
   * 
   * @param employeeId
   * @return
   */
  private EmploymentInfo findEmploymentInfo(String employeeId) {

    EmploymentInfo employmentInfoOB = null;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + employeeId + "' and enabled='Y' order by creationDate desc ");
      empInfo.setFilterOnReadableClients(false);
      empInfo.setFilterOnReadableOrganization(false);
      if (empInfo.list().size() > 0) {
        employmentInfoOB = empInfo.list().get(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.debug("Erro while fetchin employment info for employee--->" + employeeId);
    } finally {
      OBContext.restorePreviousMode();
    }

    return employmentInfoOB;
  }

  @Override
  public EHCMAbsenceReason findAbsenceReason(String absenceReason) {

    EHCMAbsenceReason objAbsenceReason = null;
    OBQuery<EHCMAbsenceReason> objAbsenceReasonQry = null;
    List<EHCMAbsenceReason> objAbsenceReasonList = new ArrayList<EHCMAbsenceReason>();
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.eUTDeflookupsTypeln.code=:reason  ";
      objAbsenceReasonQry = OBDal.getInstance().createQuery(EHCMAbsenceReason.class, query);
      objAbsenceReasonQry.setNamedParameter("reason", absenceReason);
      objAbsenceReasonQry.setFilterOnReadableClients(false);
      objAbsenceReasonQry.setFilterOnReadableOrganization(false);
      objAbsenceReasonQry.setMaxResult(1);
      objAbsenceReasonList = objAbsenceReasonQry.list();
      if (objAbsenceReasonList.size() > 0) {
        objAbsenceReason = objAbsenceReasonList.get(0);
      }
    } catch (OBException e) {
      log.error("Error while fetching absence type->absence type value-->" + absenceReason);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return objAbsenceReason;

  }

  @Override
  public EHCMAbsenceTypeAccruals getAccural(EHCMAbsenceAttendance absenceAttendance)
      throws BusinessException, Exception {

    EHCMAbsenceTypeAccruals definedAccrual = null;
    final String userLang = SecurityUtils.getUserLanguage();

    if (absenceAttendance.getEhcmAbsenceType() != null
        && absenceAttendance.getEhcmAbsenceType().isAccrual()) {
      List<EHCMAbsenceTypeAccruals> accrualAllList = absenceAttendance.getEhcmAbsenceType()
          .getEHCMAbsenceTypeAccrualsList();
      if (accrualAllList.size() == 0) {

        throw new BusinessException(
            Resource.getProperty(MessageKeys.ABSENCETYPE_ACCURAL_NOT_DEFINED, userLang));

      } else {
        List<EHCMAbsenceTypeAccruals> accrualList;
        accrualList = absenceIssueDecisionDAO.getAbsenceAccrual(absenceAttendance, Boolean.FALSE);

        if (accrualList.size() == 0) {
          throw new BusinessException(
              Resource.getProperty(MessageKeys.ACCURAL_NOT_DEFINED_FOR_YEAR, userLang));
        } else {
          for (EHCMAbsenceTypeAccruals defAccrual : accrualList) {
            if (defAccrual.getGradeClassifications() != null
                && absenceAttendance.getGradeClassifications().getId()
                    .equals(defAccrual.getGradeClassifications().getId())) {
              definedAccrual = defAccrual;
              break;
            } else {
              definedAccrual = defAccrual;
            }
          }
        }
      }
    }

    return definedAccrual;

  }

  @Override
  public void createLeave(EHCMAbsenceAttendance absenceAttendanceOB, EHCMAbsenceType absenceTypeOB,
      EHCMAbsenceTypeAccruals absenceAccuralOB) throws BusinessException {

    Connection conn = OBDal.getInstance().getConnection();
    String message = "";

    try {
      EHCMEmpLeave leave = absenceIssueDecisionDAO.insertLeaveOccuranceEmpLeave(absenceAttendanceOB,
          absenceTypeOB, absenceAccuralOB, absenceAttendanceOB.getStartDate(),
          absenceAttendanceOB.getEndDate());
      // Create or Extend Case
      if (!absenceAttendanceOB.isSueDecision()) {
        if (absenceAttendanceOB.getDecisionType().equals(Constants.CREATE_DECISION)
            || absenceAttendanceOB.getDecisionType().equals(Constants.EXTEND_DECISION)) {
          // Need to create a block in empleaveblock table
          // while taking exception leaave
          // (Leave occurance)
          if (absenceAttendanceOB.getEhcmAbsenceType().getAccrualResetDate()
              .equals(Constants.ACCRUALRESETDATE_LEAVEOCCUR)) {
            message = absenceIssueDecisionDAO.chkexceptionleaveval(conn, absenceAttendanceOB,
                absenceAccuralOB.getId());
            if (!message.equals("Success")) {
              OBDal.getInstance().rollbackAndClose();
              throw new BusinessException(message);
            }
          }
          // check absence leave type is deducted leave or not
          if (absenceAttendanceOB.getEhcmAbsenceType().isDeducted()) {
            absenceIssueDecisionDAO.deductedLeave(absenceTypeOB, conn, absenceAttendanceOB, leave,
                absenceAccuralOB, null);
          }
          if (!absenceAttendanceOB.getEhcmAbsenceType().isDeducted()) {
            /* insert a new record in emp leave type table */
            absenceIssueDecisionDAO.insertEmpLeaveLine(leave, absenceTypeOB, absenceAttendanceOB,
                absenceAttendanceOB.getAbsenceDays(), absenceAttendanceOB.getStartDate(),
                absenceAttendanceOB.getEndDate());
          }

          absenceIssueDecisionDAO.updateAbsenceDecision(absenceAttendanceOB);

        } else if (absenceAttendanceOB.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || absenceAttendanceOB.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          if (absenceAttendanceOB.getOriginalDecisionNo() != null) {

            if (absenceAttendanceOB.getEhcmAbsenceType().getAccrualResetDate().equals("LO")) {
              // update the emp leave block
              message = absenceIssueDecisionDAO.chkexceptionleaveval(conn, absenceAttendanceOB,
                  absenceAccuralOB.getId());
              if (!message.equals("Success")) {
                OBDal.getInstance().rollbackAndClose();
                throw new BusinessException(message);
              }
            }
            if (absenceAttendanceOB.getEhcmAbsenceType().isDeducted()) {
              absenceIssueDecisionDAO.deductedLeave(absenceTypeOB, conn, absenceAttendanceOB, leave,
                  absenceAccuralOB, null);
            } else {
              absenceIssueDecisionDAO.updateEmpLeave(absenceAttendanceOB);
            }

            // update old absence as inactive
            absenceIssueDecisionDAO
                .updateAbsenceEnableFlag(absenceAttendanceOB.getOriginalDecisionNo(), false);
            absenceIssueDecisionDAO.updateAbsenceDecision(absenceAttendanceOB);

          }

        } else if (absenceAttendanceOB.getDecisionType().equals("CA")) {

          absenceIssueDecisionDAO.cancelEmpLeave(absenceAttendanceOB, absenceTypeOB, conn,
              absenceAccuralOB, leave);
          // update old absence as inactive
          absenceIssueDecisionDAO
              .updateAbsenceEnableFlag(absenceAttendanceOB.getOriginalDecisionNo(), false);

          if (absenceAttendanceOB.getOriginalDecisionNo().getOriginalDecisionNo() != null) {
            absenceIssueDecisionDAO.updateAbsenceEnableFlag(
                absenceAttendanceOB.getOriginalDecisionNo().getOriginalDecisionNo(), true);
          }

          absenceAttendanceOB.setEnabled(false);
          OBDal.getInstance().save(absenceAttendanceOB);
          OBDal.getInstance().flush();
          absenceIssueDecisionDAO.updateAbsenceDecision(absenceAttendanceOB);

          if (absenceAttendanceOB.getEhcmAbsenceType().getAccrualResetDate().equals("LO")) {
            // delete the block
            // update the emp leave block
            message = absenceIssueDecisionDAO.chkexceptionleaveval(conn, absenceAttendanceOB,
                absenceAccuralOB.getId());
          }
        }
        // update the related & depenedent absence days
        if (!absenceAttendanceOB.getDecisionType().equals("CA")) {
          absenceIssueDecisionDAO.updateDependentRelatedAbsDays(absenceAttendanceOB);
        }

      }
    } catch (Exception e) {
      log.debug("Error while creating leave block by employee decision", e);
    } finally {
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    }
  }

  @Override
  public List<String> getAllOriginalDecisionNoByUserName(String username) {

    List<String> decisionNumberList = new ArrayList<String>();
    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
    for (EHCMAbsenceAttendance absenceAttedance : employeeOB.getEHCMAbsenceAttendanceList()) {
      decisionNumberList.add(absenceAttedance.getDecisionNo());
    }
    return decisionNumberList;
  }

  @Override
  public List<EHCMAbsenceAttendance> getLeaveRequestByDecisionNoOrByUsername(String orginalDecNo,
      String userName) {

    List<EHCMAbsenceAttendance> objAbsenceDecisionList = new ArrayList<EHCMAbsenceAttendance>();
    OBQuery<EHCMAbsenceAttendance> objAbsenceListQry = null;
    try {
      OBContext.setAdminMode(true);
      String query = "";
      if (StringUtils.isEmpty(userName)) {
        query = " as e where e.decisionNo=:decisionNumber  ";
      } else if (StringUtils.isEmpty(orginalDecNo)) {
        query = " as e where e.ehcmEmpPerinfo.id=:userId  ";
      }

      objAbsenceListQry = OBDal.getInstance().createQuery(EHCMAbsenceAttendance.class, query);
      if (StringUtils.isEmpty(userName)) {
        objAbsenceListQry.setNamedParameter("decisionNumber", orginalDecNo);
      } else if (StringUtils.isEmpty(orginalDecNo)) {
        EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);
        objAbsenceListQry.setNamedParameter("userId", employeeOB.getId());
      }

      objAbsenceListQry.setFilterOnReadableClients(false);
      objAbsenceListQry.setFilterOnReadableOrganization(false);
      objAbsenceListQry.setMaxResult(1);
      objAbsenceDecisionList = objAbsenceListQry.list();
      return objAbsenceDecisionList;
    } catch (Exception e) {
      e.printStackTrace();
      log.error(
          "Error while fetching absence decision list -> for decision number-->" + orginalDecNo);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public EHCMAbsenceAttendance getAbsenceAttendanceById(String attendanceId) {

    EHCMAbsenceAttendance objAbsenceAttendance = null;
    try {
      OBContext.setAdminMode(true);
      objAbsenceAttendance = OBDal.getInstance().get(EHCMAbsenceAttendance.class, attendanceId);
    } catch (OBException e) {
      log.error("Error while fetching absence Attendance");
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return objAbsenceAttendance;
  }

  @Override
  public String getLeaveRequestDaysValidation(EHCMAbsenceAttendance absenceAttendance,
      String approvalMessage) {

    String localapprovalMessage = "";
    try {
      OBContext.setAdminMode();
      OBQuery<EHCMAbsenceTypeRules> ruleQry = OBDal.getInstance().createQuery(
          EHCMAbsenceTypeRules.class,
          " as e where e.code=:code and e.absenceType.id=:absenceTypeId ");
      ruleQry.setNamedParameter("code", "LLTF");
      ruleQry.setNamedParameter("absenceTypeId", absenceAttendance.getEhcmAbsenceType().getId());

      ruleQry.setFilterOnReadableClients(false);
      ruleQry.setFilterOnReadableOrganization(false);
      ruleQry.setMaxResult(1);
      List<EHCMAbsenceTypeRules> absenceRuleList = ruleQry.list();
      if (absenceRuleList.size() > 0) {
        EHCMAbsenceTypeRules absrule = absenceRuleList.get(0);
        String input = absrule.getCondition().split("<=")[1].toString();
        localapprovalMessage = OBMessageUtils.messageBD(approvalMessage);
        localapprovalMessage = localapprovalMessage.replace("%", input);
      }
    } catch (Exception e) {

    } finally {
      OBContext.restorePreviousMode();
    }
    return localapprovalMessage;
  }

}
