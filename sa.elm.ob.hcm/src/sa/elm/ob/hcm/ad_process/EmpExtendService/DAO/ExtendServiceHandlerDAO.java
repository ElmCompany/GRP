package sa.elm.ob.hcm.ad_process.EmpExtendService.DAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.jfree.util.Log;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EHCMEmpTransfer;
import sa.elm.ob.hcm.EHCMEmpTransferSelf;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmExtendService;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author Poongodi 09/02/2018
 *
 */

public class ExtendServiceHandlerDAO {
  private static final Logger LOG = LoggerFactory.getLogger(ExtendServiceHandlerDAO.class);

  /**
   * 
   * @param extendServiceProcess
   * @return
   */

  public static int getExtendCountFromEmploymentInfo(EhcmExtendService extendServiceProcess) {
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    int empCount = 0;
    try {
      OBQuery<EmploymentInfo> chkEmployeeCount = OBDal.getInstance().createQuery(
          EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId  and  ehcmExtendService.id is not null ");
      chkEmployeeCount.setNamedParameter("employeeId", extendServiceProcess.getEmployee().getId());
      empInfoList = chkEmployeeCount.list();
      empCount = empInfoList.size();
    } catch (OBException e) {
      // TODO Auto-generated catch block
      LOG.error("Exception in getExtendCountFromEmploymentInfo " + e.getMessage());
      e.printStackTrace();
    }
    return empCount;
  }

  /**
   * 
   * @param extendServiceProcess
   * @return
   */

  public static boolean checkPeriodExists(EhcmExtendService extendServiceProcess) {
    boolean checkPeriodExists = false;
    PreparedStatement st = null;
    ResultSet rs1 = null;
    try {
      String sql = "";
      String toDate = "";
      String fromDate = Utility.formatDate(extendServiceProcess.getEffectivedate());
      toDate = "21-06-2058";

      sql = "select startdate from ehcm_employment_info where ehcm_emp_perinfo_id ='"
          + extendServiceProcess.getEmployee().getId() + "' and ad_client_id='"
          + extendServiceProcess.getClient().getId()
          + "'  and ehcm_extend_service_id is not null and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
          + fromDate
          + "') and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
          + toDate
          + "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
          + fromDate + "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
          + toDate + "','dd-MM-yyyy'))) ";
      st = OBDal.getInstance().getConnection().prepareStatement(sql);
      rs1 = st.executeQuery();
      if (rs1.next()) {
        checkPeriodExists = true;
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      try {
        st.close();
        rs1.close();
      } catch (final SQLException e) {
        LOG.error("Exception in checkPeriodExists", e);
      }
    }
    return checkPeriodExists;
  }

  /**
   * 
   * @param extendServiceProcess
   * @param vars
   * @return
   */
  @SuppressWarnings("rawtypes")
  public static int insertLineinEmploymentInfo(EhcmExtendService extendServiceProcess,
      VariablesSecureApp vars) {
    EmploymentInfo info = null;
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    int count = 0;
    EHCMEmpSupervisor supervisorId = null;
    Boolean isExtraStep = false;
    try {
      OBContext.setAdminMode();
      String enddate = "", startyear = "", startDate = "";
      int endyear = 0;
      Date endDate = null;
      // get employment Information by passing the corresponding employee id.
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where ehcmEmpPerinfo.id=:employeeId and e.enabled='Y' and e.ehcmExtendService.id is not null order by e.creationDate desc");
      empInfo.setNamedParameter("employeeId", extendServiceProcess.getEmployee().getId());
      empInfo.setMaxResult(1);
      empInfoList = empInfo.list();
      if (empInfoList.size() > 0) {
        info = empInfoList.get(0);
      }
      // on create case
      if (extendServiceProcess.getDecisionType()
          .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " as e where ehcmEmpPerinfo.id=:employeeId and e.enabled='Y' and (e.ehcmExtendService.id is null) order by e.creationDate desc");
        empInfo.setNamedParameter("employeeId", extendServiceProcess.getEmployee().getId());
        empInfo.setMaxResult(1);
        empInfoList = empInfo.list();
        if (empInfoList.size() > 0) {
          info = empInfoList.get(0);
        }
      }

      if (extendServiceProcess.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          || extendServiceProcess.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        EmploymentInfo employInfo = null;
        if (extendServiceProcess.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
        } else {
          employInfo = info;
        }

        if (extendServiceProcess.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))
          employInfo.setChangereason(DecisionTypeConstants.EMPLOYMENTSTATUS_EXTENDSERVICE);
        else
          employInfo.setChangereason(DecisionTypeConstants.EMPLOYMENTSTATUS_EXTENDSERVICE);
        // inserting current active employment info details
        sa.elm.ob.hcm.util.UtilityDAO.insertActEmplymntInfoDetailsInIssueDecision(
            extendServiceProcess.getEmployee(), employInfo, isExtraStep, false);

        employInfo.setStartDate(extendServiceProcess.getEffectivedate());
        employInfo.setEhcmExtendService(extendServiceProcess);
        employInfo.setDecisionNo(extendServiceProcess.getDecisionNo());
        employInfo.setDecisionDate(extendServiceProcess.getDecisionDate());
        String inpperiod = extendServiceProcess.getExtendPeriod().toString();
        if (!inpperiod.equals("0")) {
          String inpstartdate = UtilityDAO
              .convertTohijriDate(extendServiceProcess.getEffectivedate().toString());
          startyear = inpstartdate.split("-")[2];
          startDate = inpstartdate.split("-")[0];
          endyear = Integer.valueOf(startyear) + Integer.valueOf(inpperiod);
          enddate = endyear + inpstartdate.split("-")[1] + inpstartdate.split("-")[0];
          Date Enddate = getOneDayMinusHijiriDate(enddate,
              extendServiceProcess.getClient().getId());
          employInfo.setEndDate(Enddate);
        } else {
          employInfo.setEndDate(null);
        }
        LOG.debug("info:" + employInfo.getEndDate());

        // Update the enddate for old hiring record.
        if (extendServiceProcess.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          Date dateBefore = null;
          OBQuery<EmploymentInfo> empInfoold = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " as e where ehcmEmpPerinfo.id=:employeeId  and e.enabled='Y' order by e.creationDate desc");
          empInfoold.setNamedParameter("employeeId", extendServiceProcess.getEmployee().getId());
          empInfoold.setMaxResult(1);
          EmploymentInfo empinfo = empInfoold.list().get(0);
          empinfo.setUpdated(new java.util.Date());
          empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));

          Date startdate = empinfo.getStartDate();
          dateBefore = new Date(extendServiceProcess.getEffectivedate().getTime()
              - DecisionTypeConstants.ONE_DAY_IN_MILISEC);

          if (startdate.compareTo(extendServiceProcess.getEffectivedate()) == 0)
            empinfo.setEndDate(empinfo.getStartDate());
          else
            empinfo.setEndDate(dateBefore);

          empinfo.setAlertStatus(DecisionTypeConstants.Status_Inactive);
          empinfo.setEnabled(false);

          OBDal.getInstance().save(empinfo);
          OBDal.getInstance().flush();

        }
        OBDal.getInstance().save(employInfo);
        OBDal.getInstance().flush();

        // Update Case
        if (extendServiceProcess.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
          // update the endate and active flag for old hiring record.
          OBQuery<EmploymentInfo> empInfoold = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id=:employeeId  and id not in ('" + employInfo.getId()
                  + "')  order by creationDate desc ");
          empInfoold.setNamedParameter("employeeId", extendServiceProcess.getEmployee().getId());
          empInfoold.setMaxResult(1);
          empInfoList = empInfoold.list();
          if (empInfoList.size() > 0) {
            EmploymentInfo empinfo = empInfoList.get(0);
            empinfo.setUpdated(new java.util.Date());
            empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
            Date startdate = empinfo.getStartDate();
            Date dateBefore = new Date(extendServiceProcess.getEffectivedate().getTime()
                - DecisionTypeConstants.ONE_DAY_IN_MILISEC);
            if (startdate.compareTo(extendServiceProcess.getEffectivedate()) == 0)
              empinfo.setEndDate(empinfo.getStartDate());
            else
              empinfo.setEndDate(dateBefore);

          }
          // update old extrastep as inactive
          EhcmExtendService oldExtraStep = extendServiceProcess.getOriginalDecisionNo();
          oldExtraStep.setEnabled(false);
          OBDal.getInstance().save(oldExtraStep);
          OBDal.getInstance().flush();
        }

        EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
            extendServiceProcess.getEmployee().getId());
        if (extendServiceProcess.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          person.setEmploymentStatus(DecisionTypeConstants.EMPLOYMENTSTATUS_ACTIVE);
        } else {
          person.setEmploymentStatus(DecisionTypeConstants.EMPLOYMENTSTATUS_EXTENDSERVICE);
        }
        OBDal.getInstance().save(person);
        OBDal.getInstance().flush();

      }
      // cancel case
      else if (extendServiceProcess.getDecisionType()
          .equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        EmploymentInfo empInfor = null;
        // update the acive flag='Y' and enddate is null for recently update record
        OBQuery<EmploymentInfo> originalemp = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id=:employeeId  and (ehcmExtendService.id not in ('"
                + extendServiceProcess.getOriginalDecisionNo().getId()
                + "') or ehcmExtendService.id is null) order by creationDate desc ");
        originalemp.setNamedParameter("employeeId", extendServiceProcess.getEmployee().getId());
        originalemp.setMaxResult(1);
        LOG.debug(originalemp.getWhereAndOrderBy());
        empInfoList = originalemp.list();
        if (empInfoList.size() > 0) {
          EmploymentInfo empinfo = empInfoList.get(0);
          // remove the recent record
          OBQuery<EmploymentInfo> employInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id=:employeeId  and enabled='Y'  and ehcmExtendService.id ='"
                  + extendServiceProcess.getOriginalDecisionNo().getId()
                  + "' order by creationDate desc");
          employInfo.setNamedParameter("employeeId", extendServiceProcess.getEmployee().getId());
          employInfo.setMaxResult(1);
          empInfoList = employInfo.list();
          if (empInfoList.size() > 0) {
            empInfor = empInfoList.get(0);
            OBDal.getInstance().remove(empInfor);
            // OBDal.getInstance().flush();
          }
          empinfo.setUpdated(new java.util.Date());
          empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          endDate = updateEndDateInEmploymentInfo(extendServiceProcess.getEmployee().getId(),
              extendServiceProcess.getClient().getId(), empInfor.getId());
          empinfo.setEndDate(endDate);
          empinfo.setEnabled(true);
          empinfo.setAlertStatus(DecisionTypeConstants.Status_active);
          OBDal.getInstance().save(empinfo);
          // OBDal.getInstance().flush();

          if (empinfo.getEhcmExtendService() != null) {
            EhcmExtendService oldExtendService = empinfo.getEhcmExtendService();
            oldExtendService.setEnabled(true);
            OBDal.getInstance().save(oldExtendService);
            // OBDal.getInstance().flush();
          }
        }
        EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
            extendServiceProcess.getEmployee().getId());

        person.setEmploymentStatus(DecisionTypeConstants.EMPLOYMENTSTATUS_ACTIVE);

        OBDal.getInstance().save(person);
        // OBDal.getInstance().flush();

        // update old extrastep as inactive
        EhcmExtendService oldExtend = extendServiceProcess.getOriginalDecisionNo();
        oldExtend.setEnabled(false);
        OBDal.getInstance().save(oldExtend);
        // OBDal.getInstance().flush();

        extendServiceProcess.setEnabled(false);
        OBDal.getInstance().save(extendServiceProcess);
        // OBDal.getInstance().flush();
      }

      count = 1;

    }

    catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Exception while inserting lines in employment tab using extendservice : ", e, e);
      }
      OBDal.getInstance().rollbackAndClose();

    } finally {
      OBContext.restorePreviousMode();
    }
    return count;

  }

  public static Date getOneDayMinusHijiriDate(String gregoriandate, String clientId) {
    Query query = null;
    String strQuery = "";
    Date startdate = null;
    try {

      strQuery = "  select  gregorian_date from eut_hijri_dates  where hijri_date < '"
          + gregoriandate + "' order by hijri_date desc limit 1 ";
      query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
      Log.debug(strQuery.toString());
      if (query != null && query.list().size() > 0) {
        Object row = query.list().get(0);
        startdate = (Date) row;
      }
    } catch (Exception e) {
      LOG.error("Exception in getOneDayMinusHijiriDate", e);
      e.printStackTrace();
    }
    return startdate;
  }

  public static String convertTohijriDate(String gregDate) {
    String hijriDate = "";
    try {
      SQLQuery greQuery = OBDal.getInstance().getSession()
          .createSQLQuery(" select eut_cnvrttohjr_yyyymmdd('" + gregDate + "')");

      if (greQuery.list().size() > 0) {
        Object row = (Object) greQuery.list().get(0);
        hijriDate = (String) row;
      }
    }

    catch (final Exception e) {
      LOG.error("Exception in convertTohijriDate()  Method : ", e);
      return "0";
    }
    return hijriDate;
  }

  public static boolean chkEmpExtendOrNot(String dob, String effectiveDate, int maxAge) {
    boolean chkEmpExtendOrNot = false;
    String sql = "";
    PreparedStatement st = null;
    ResultSet rs1 = null;
    int age = 0;
    try {
      sql = " select extract (year from (age(to_date('" + effectiveDate
          + "','dd-MM-yyyy') ,to_date('" + dob + "','dd-mm-yyyy') ))) ";
      st = OBDal.getInstance().getConnection().prepareStatement(sql);
      rs1 = st.executeQuery();
      if (rs1.next()) {
        age = rs1.getInt("age");
        if (age < maxAge) {
          chkEmpExtendOrNot = true;
        }
      }

    } catch (Exception e) {
      LOG.error("Exception in chkEmpExtendOrNot", e);
      e.printStackTrace();
    } finally {
      try {
        st.close();
        rs1.close();
      } catch (final SQLException e) {
        LOG.error("Exception in chkEmpExtendOrNot", e);
      }
    }
    return chkEmpExtendOrNot;

  }

  /**
   * Revert the changes after reactivate the Extend of Service
   * 
   * @param extendservice
   * @param vars
   * @param clientId
   */
  public static void revertTheChangesInEmploymentInfo(EhcmExtendService extendservice,
      VariablesSecureApp vars, String clientId) {

    try {
      List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
      String hql = "";
      String effectiveDate = "", enddate = "", startyear = "", endYear = "", startDate = "";
      int endyear = 0;
      Date dateBefore = null;
      Date endDate = null;
      EmploymentInfo empInfor = null;

      OBQuery<EmploymentInfo> originalemp = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and (ehcmExtendService.id not in ('"
              + extendservice.getId()
              + "') or ehcmExtendService.id is null) and client.id =:clientId order by creationDate desc ");
      originalemp.setNamedParameter("employeeId", extendservice.getEmployee().getId());
      originalemp.setNamedParameter("clientId", clientId);
      originalemp.setMaxResult(1);
      empInfoList = originalemp.list();
      if (empInfoList.size() > 0) {
        EmploymentInfo empinfo = empInfoList.get(0);
        // remove the recent record (Extended Record)
        OBQuery<EmploymentInfo> employInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id=:employeeId  and enabled='Y'  and ehcmExtendService.id =:extendOriginalDecisionNoId and client.id =:clientId order by creationDate desc");
        employInfo.setNamedParameter("employeeId", extendservice.getEmployee().getId());
        employInfo.setNamedParameter("extendOriginalDecisionNoId", extendservice.getId());
        employInfo.setNamedParameter("clientId", clientId);
        employInfo.setMaxResult(1);
        empInfoList = employInfo.list();
        if (empInfoList.size() > 0) {
          empInfor = empInfoList.get(0);
          if (extendservice.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
            OBDal.getInstance().remove(empInfor);

          }
          if (extendservice.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
            empInfor.setStartDate(extendservice.getOriginalDecisionNo().getEffectivedate());
            empInfor.setDecisionNo(extendservice.getOriginalDecisionNo().getDecisionNo());
            empInfor.setDecisionDate(extendservice.getOriginalDecisionNo().getDecisionDate());
            empInfor.setEhcmExtendService(extendservice.getOriginalDecisionNo());
            String inpperiod = extendservice.getExtendPeriod().toString();
            if (!inpperiod.equals("0")) {
              String inpstartdate = UtilityDAO.convertTohijriDate(
                  extendservice.getOriginalDecisionNo().getEffectivedate().toString());
              startyear = inpstartdate.split("-")[2];
              startDate = inpstartdate.split("-")[0];
              endyear = Integer.valueOf(startyear) + Integer.valueOf(inpperiod);
              enddate = endyear + inpstartdate.split("-")[1] + inpstartdate.split("-")[0];
              Date Enddate = getOneDayMinusHijiriDate(enddate, extendservice.getClient().getId());
              empInfor.setEndDate(Enddate);
            } else {
              empInfor.setEndDate(null);
            }
            OBDal.getInstance().save(empInfor);

          }
        }
        // update enddate for old record
        if (extendservice.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          empinfo.setUpdated(new java.util.Date());
          empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          endDate = updateEndDateInEmploymentInfo(extendservice.getEmployee().getId(),
              extendservice.getClient().getId(), empInfor.getId());
          empinfo.setEndDate(endDate);
          empinfo.setEnabled(true);
          empinfo.setAlertStatus(DecisionTypeConstants.Status_active);
          OBDal.getInstance().save(empinfo);
          // OBDal.getInstance().flush();
        }
        if (extendservice.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
          dateBefore = new Date(extendservice.getOriginalDecisionNo().getEffectivedate().getTime()
              - DecisionTypeConstants.ONE_DAY_IN_MILISEC);
          empinfo.setEndDate(dateBefore);
          OBDal.getInstance().save(empinfo);
          // OBDal.getInstance().flush();
        }

      }
      // set the employment status in employee
      updateEmpRecord(extendservice.getEmployee().getId(), empInfor.getId());
      /*
       * EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
       * extendservice.getEmployee().getId()); person.setEmploymentStatus("AC");
       * OBDal.getInstance().save(person); OBDal.getInstance().flush();
       */

    } catch (Exception e) {
      LOG.error("Exception in revertTheChangesforOldRecordInEmploymentInfo", e);
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param extendservice
   */
  public static void updateExtendofServiceStatus(EhcmExtendService extendservice) {
    try {
      // set the status in Extend of Service
      EhcmExtendService extendOfService = OBDal.getInstance().get(EhcmExtendService.class,
          extendservice.getId());
      if (extendOfService.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        extendOfService.setDecisionStatus(DecisionTypeConstants.DECISION_TYPE_UPDATE);
        extendOfService.setSueDecision(false);
        OBDal.getInstance().save(extendOfService);
        OBDal.getInstance().flush();
      }
      if (extendOfService.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        extendOfService.getOriginalDecisionNo().setEnabled(true);
        extendOfService.setDecisionStatus(DecisionTypeConstants.DECISION_TYPE_UPDATE);
        extendOfService.setSueDecision(false);
        OBDal.getInstance().save(extendOfService);
        OBDal.getInstance().flush();

      }

    } catch (Exception e) {
      LOG.error("Exception in updateExtendofServiceStatus", e);
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param extendservice
   * @param clientId
   * @return
   */
  public static boolean checkOriginalDecisionNoIssued(EhcmExtendService extendservice,
      String clientId) {
    try {
      List<EhcmExtendService> extendofservicelist = new ArrayList<EhcmExtendService>();
      OBQuery<EhcmExtendService> extendOfService = OBDal.getInstance().createQuery(
          EhcmExtendService.class,
          " id =:originalDecisionNoId and client.id =:clientId and issueDecision = 'N' ");
      extendOfService.setNamedParameter("originalDecisionNoId",
          extendservice.getOriginalDecisionNo().getId());
      extendOfService.setNamedParameter("clientId", clientId);
      extendOfService.setMaxResult(1);
      extendofservicelist = extendOfService.list();
      if (extendofservicelist.size() > 0) {
        return true;
      }

    } catch (Exception e) {
      LOG.error("Exception in updateExtendofServiceStatus", e);
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 
   * @param extendservice
   * @param clientId
   */
  public static void revertChangesAfterCancelReactivate(EhcmExtendService extendservice,
      String clientId) {
    try {
      String effectiveDate = "", enddate = "", startyear = "", endYear = "", startDate = "";
      int endyear = 0;
      EmploymentInfo info = null;
      Date dateBefore = null;
      EHCMEmpSupervisor supervisorId = null;
      List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
      // update end date and set active as 'Y' for the recent record
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where ehcmEmpPerinfo.id=:employeeId and e.enabled='Y' and (e.ehcmExtendService.id is null) order by e.creationDate desc");
      empInfo.setNamedParameter("employeeId", extendservice.getEmployee().getId());
      empInfo.setMaxResult(1);
      empInfoList = empInfo.list();
      if (empInfoList.size() > 0) {
        info = empInfoList.get(0);
        dateBefore = new Date(extendservice.getOriginalDecisionNo().getEffectivedate().getTime()
            - DecisionTypeConstants.ONE_DAY_IN_MILISEC);
        info.setEndDate(dateBefore);
        info.setEnabled(false);
        info.setAlertStatus(DecisionTypeConstants.Status_Inactive);
        OBDal.getInstance().save(info);
        OBDal.getInstance().flush();
      }
      // insert record in Employment Info
      EmploymentInfo employInfo = null;
      employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
      employInfo.setChangereason(DecisionTypeConstants.EMPLOYMENTSTATUS_EXTENDSERVICE);
      employInfo
          .setDepartmentName(extendservice.getOriginalDecisionNo().getDepartmentCode().getName());
      employInfo.setDeptcode(extendservice.getOriginalDecisionNo().getDepartmentCode());
      employInfo.setGrade(extendservice.getOriginalDecisionNo().getGrade());
      ehcmpayscaleline line = OBDal.getInstance().get(ehcmpayscaleline.class,
          extendservice.getOriginalDecisionNo().getEhcmPayscaleline().getId());
      employInfo.setEhcmPayscale(line.getEhcmPayscale());
      employInfo
          .setEmpcategory(extendservice.getOriginalDecisionNo().getGradeClassifications().getId());
      employInfo.setEmployeeno(extendservice.getOriginalDecisionNo().getEmployee().getSearchKey());
      employInfo.setEhcmPayscaleline(line);
      employInfo.setEmploymentgrade(extendservice.getOriginalDecisionNo().getEmploymentGrade());
      employInfo.setJobcode(extendservice.getOriginalDecisionNo().getPosition().getEhcmJobs());
      employInfo.setPosition(extendservice.getOriginalDecisionNo().getPosition());
      employInfo.setJobtitle(
          extendservice.getOriginalDecisionNo().getPosition().getJOBName().getJOBTitle());
      employInfo.setLocation(info.getLocation());
      if (info.getEhcmPayrollDefinition() != null)
        employInfo.setEhcmPayrollDefinition(info.getEhcmPayrollDefinition());
      if (extendservice.getOriginalDecisionNo().getSectionCode() != null)
        employInfo.setSectionName(extendservice.getOriginalDecisionNo().getSectionCode().getName());
      employInfo.setSectioncode(extendservice.getOriginalDecisionNo().getSectionCode());
      employInfo.setEhcmEmpPerinfo(extendservice.getOriginalDecisionNo().getEmployee());
      employInfo.setStartDate(extendservice.getOriginalDecisionNo().getEffectivedate());
      employInfo.setAlertStatus(DecisionTypeConstants.Status_active);
      employInfo.setEhcmExtendService(extendservice.getOriginalDecisionNo());
      employInfo.setDecisionNo(extendservice.getOriginalDecisionNo().getDecisionNo());
      employInfo.setDecisionDate(extendservice.getOriginalDecisionNo().getDecisionDate());
      OBQuery<EHCMEmpSupervisorNode> supervisior = OBDal.getInstance().createQuery(
          EHCMEmpSupervisorNode.class,
          "  as e where e.ehcmEmpPerinfo.id=:employeeId and e.client.id =:client");
      supervisior.setNamedParameter("employeeId",
          extendservice.getOriginalDecisionNo().getEmployee().getId());
      supervisior.setNamedParameter("client",
          extendservice.getOriginalDecisionNo().getClient().getId());
      List<EHCMEmpSupervisorNode> node = supervisior.list();
      if (node.size() > 0) {
        supervisorId = node.get(0).getEhcmEmpSupervisor();
        employInfo.setEhcmEmpSupervisor(supervisorId);
      }

      String inpperiod = extendservice.getOriginalDecisionNo().getExtendPeriod().toString();
      if (!inpperiod.equals("0")) {
        String inpstartdate = UtilityDAO.convertTohijriDate(
            extendservice.getOriginalDecisionNo().getEffectivedate().toString());
        startyear = inpstartdate.split("-")[2];
        startDate = inpstartdate.split("-")[0];
        endyear = Integer.valueOf(startyear) + Integer.valueOf(inpperiod);
        enddate = endyear + inpstartdate.split("-")[1] + inpstartdate.split("-")[0];
        Date Enddate = getOneDayMinusHijiriDate(enddate,
            extendservice.getOriginalDecisionNo().getClient().getId());
        employInfo.setEndDate(Enddate);
      } else {
        employInfo.setEndDate(null);
      }
      /* secondary */
      LOG.debug("info:" + employInfo.getEndDate());
      employInfo.setSecpositionGrade(info.getSecpositionGrade());
      employInfo.setSecpositionGrade(info.getSecpositionGrade());
      employInfo.setSecjobno(info.getSecjobno());
      employInfo.setSecjobcode(info.getSecjobcode());
      employInfo.setSecjobtitle(info.getSecjobtitle());
      employInfo.setSECDeptCode(info.getSECDeptCode());
      employInfo.setSECDeptName(info.getSECDeptName());
      employInfo.setSECSectionCode(info.getSECSectionCode());
      employInfo.setSECSectionName(info.getSECSectionName());
      employInfo.setSECLocation(info.getSECLocation());
      employInfo.setSECStartdate(info.getSECStartdate());
      employInfo.setSECEnddate(info.getSECEnddate());
      employInfo.setSECDecisionNo(info.getSECDecisionNo());
      employInfo.setSECDecisionDate(info.getSECDecisionDate());
      employInfo.setSECChangeReason(info.getSECChangeReason());
      employInfo.setSECEmploymentNumber(info.getSECEmploymentNumber());
      OBDal.getInstance().save(employInfo);
      OBDal.getInstance().flush();

      // update employment status for the employee
      EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
          extendservice.getOriginalDecisionNo().getEmployee().getId());
      person.setEmploymentStatus(DecisionTypeConstants.EMPLOYMENTSTATUS_EXTENDSERVICE);
      OBDal.getInstance().save(person);
      OBDal.getInstance().flush();

      // update old extend of service as active
      EhcmExtendService oldExtend = extendservice.getOriginalDecisionNo();
      oldExtend.setEnabled(true);
      OBDal.getInstance().save(oldExtend);
      OBDal.getInstance().flush();

      extendservice.setSueDecision(false);
      extendservice.setEnabled(true);
      extendservice.setDecisionStatus(DecisionTypeConstants.DECISION_TYPE_UPDATE);
      OBDal.getInstance().save(extendservice);
      OBDal.getInstance().flush();

    } catch (Exception e) {
      LOG.error("Exception in updateExtendofServiceStatus", e);
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param extendofservice
   */
  public static void updateEmpRecord(String employeeId, String recentEmpInfoId) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    String employmentStatus = null;
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "   ehcmEmpPerinfo.id=:employeeId and id <>:recentEmpInfoId order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", employeeId);
      empInfo.setNamedParameter("recentEmpInfoId", recentEmpInfoId);
      empInfo.setMaxResult(1);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
        EmploymentInfo empinfoObj = empinfo;
        if (empinfoObj.getEhcmEmpSecondment() != null) {
          employmentStatus = DecisionTypeConstants.EMPLOYMENTSTATUS_SECONDMENT;
        } else if (empinfoObj.getEhcmExtendService() != null) {
          employmentStatus = DecisionTypeConstants.EMPLOYMENTSTATUS_EXTENDSERVICE;
        } else if (empinfoObj.getEhcmEmpExtrastep() != null) {
          employmentStatus = DecisionTypeConstants.EMPLOYMENTSTATUS_EXTRASTEP;
        } else {
          employmentStatus = DecisionTypeConstants.EMPLOYMENTSTATUS_ACTIVE;
        }
        EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
        employee.setEmploymentStatus(employmentStatus);
        OBDal.getInstance().save(employee);
        OBDal.getInstance().flush();

      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      LOG.error("Exception in updateEmpRecord " + e.getMessage());
      e.printStackTrace();
    }

  }

  /**
   * 
   * @param extendofservice
   * @param clientId
   * @return
   */
  public static Date updateEndDateInEmploymentInfo(String employeeId, String clientId,
      String recentEmpInfoId) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    Date endDate = null;
    String enddate = "", startyear = "", startDate = "";
    int endyear = 0;

    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "   ehcmEmpPerinfo.id=:employeeId and client.id=:clientId  and id <>:recentEmpInfoId order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", employeeId);
      empInfo.setNamedParameter("clientId", clientId);
      empInfo.setNamedParameter("recentEmpInfoId", recentEmpInfoId);
      empInfo.setMaxResult(1);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
        EmploymentInfo empinfoObj = empinfo;
        if (empinfoObj.getEhcmEmpSecondment() != null) {
          EHCMEmpSecondment secondment = empinfoObj.getEhcmEmpSecondment();
          if (secondment.getEndDate() != null) {
            endDate = secondment.getEndDate();
          }
        } else if (empinfoObj.getEhcmEmpTransfer() != null) {
          EHCMEmpTransfer transfer = empinfoObj.getEhcmEmpTransfer();
          if (transfer.getEndDate() != null) {
            endDate = transfer.getEndDate();
          }
        } else if (empinfoObj.getEhcmEmpTransferSelf() != null) {
          EHCMEmpTransferSelf transferself = empinfoObj.getEhcmEmpTransferSelf();
          if (transferself.getEndDate() != null) {
            endDate = transferself.getEndDate();
          }
        } else if (empinfoObj.getEhcmEmpSuspension() != null) {
          EmployeeSuspension suspension = empinfoObj.getEhcmEmpSuspension();
          if (suspension.getSuspensionType().equals("SUE") && suspension.getEndDate() != null) {
            endDate = suspension.getEndDate();
          }
        } else if (empinfoObj.getEhcmExtendService() != null) {
          EhcmExtendService eos = empinfoObj.getEhcmExtendService();
          if (eos.getEffectivedate() != null && eos.getExtendPeriod() != null) {
            String inpperiod = eos.getExtendPeriod().toString();
            if (!inpperiod.equals("0")) {
              String inpstartdate = UtilityDAO
                  .convertTohijriDate(eos.getEffectivedate().toString());
              startyear = inpstartdate.split("-")[2];
              startDate = inpstartdate.split("-")[0];
              endyear = Integer.valueOf(startyear) + Integer.valueOf(inpperiod);
              enddate = endyear + inpstartdate.split("-")[1] + inpstartdate.split("-")[0];
              Date Enddate = getOneDayMinusHijiriDate(enddate, eos.getClient().getId());
              endDate = Enddate;
            } else {
              endDate = null;
            }
          }
        }

      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      LOG.error("Exception in updateEndDateInEmploymentInfo " + e.getMessage());
      e.printStackTrace();
    }
    return endDate;

  }

  public static boolean checkRecentRecordIsEOSInEmpInfo(String employeeId, String clientId) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "   ehcmEmpPerinfo.id=:employeeId and client.id=:clientId order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", employeeId);
      empInfo.setNamedParameter("clientId", clientId);
      empInfo.setMaxResult(1);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
        if (empinfo.getEhcmExtendService() == null) {
          return true;
        }

      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      LOG.error("Exception in checkRecentRecordIsEOS " + e.getMessage());
      e.printStackTrace();
    }
    return false;

  }

  public static boolean chkOriginalDecisionUsed(String employeeId, String clientId,
      String originalDecisionId, String ExtendId) {
    OBQuery<EhcmExtendService> extendService = null;
    List<EhcmExtendService> EhcmExtendServiceList = new ArrayList<EhcmExtendService>();
    boolean chkOriginalDecisionUsed = false;
    try {
      extendService = OBDal.getInstance().createQuery(EhcmExtendService.class,
          " as e where e.originalDecisionNo.id in (select originalDecisionNo.id from ehcm_extend_service where employee.id = :employeeId and decisionStatus = 'I' and originalDecisionNo.id = :OriginalDecisionNo) and e.id not in ('"
              + ExtendId + "')");
      extendService.setNamedParameter("employeeId", employeeId);
      extendService.setNamedParameter("OriginalDecisionNo", originalDecisionId);
      EhcmExtendServiceList = extendService.list();
      if (EhcmExtendServiceList.size() > 0) {
        chkOriginalDecisionUsed = true;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      LOG.error("Exception in chkOriginalDecisionUsed ", e.getMessage());
      e.printStackTrace();
    }
    return chkOriginalDecisionUsed;

  }
}