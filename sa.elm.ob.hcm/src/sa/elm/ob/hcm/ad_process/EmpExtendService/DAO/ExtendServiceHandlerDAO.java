package sa.elm.ob.hcm.ad_process.EmpExtendService.DAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
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

import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmExtendService;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmpayscaleline;
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
    try {
      String sql = "";
      String toDate = "";
      String fromDate = Utility.formatDate(extendServiceProcess.getEffectivedate());
      toDate = "21-06-2058";
      PreparedStatement st = null;
      ResultSet rs1 = null;
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
    Date dateafter = null;
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    int count = 0;
    EHCMEmpSupervisor supervisorId = null;
    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      OBContext.setAdminMode();
      String effectiveDate = "", enddate = "", startyear = "", endYear = "", startDate = "";
      int endyear = 0, endmonth = 0, endday = 0;
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
      if (extendServiceProcess.getDecisionType().equals("CR")) {
        empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " as e where ehcmEmpPerinfo.id=:employeeId and e.enabled='Y' and (e.ehcmExtendService.id is null) order by e.creationDate desc");
        empInfo.setNamedParameter("employeeId", extendServiceProcess.getEmployee().getId());
        empInfo.setMaxResult(1);
        empInfoList = empInfo.list();
        if (empInfoList.size() > 0) {
          info = empInfoList.get(0);
        }
      }

      if (extendServiceProcess.getDecisionType().equals("CR")
          || extendServiceProcess.getDecisionType().equals("UP")) {
        EmploymentInfo employInfo = null;
        if (extendServiceProcess.getDecisionType().equals("CR")) {
          employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
        } else {
          employInfo = info;
        }

        if (extendServiceProcess.getDecisionType().equals("UP"))
          employInfo.setChangereason("EOS");
        else
          employInfo.setChangereason("EOS");
        employInfo.setDepartmentName(extendServiceProcess.getDepartmentCode().getName());
        employInfo.setDeptcode(extendServiceProcess.getDepartmentCode());
        employInfo.setGrade(extendServiceProcess.getGrade());
        ehcmpayscaleline line = OBDal.getInstance().get(ehcmpayscaleline.class,
            extendServiceProcess.getEhcmPayscaleline().getId());
        employInfo.setEhcmPayscale(line.getEhcmPayscale());
        employInfo.setEmpcategory(extendServiceProcess.getGradeClassifications().getId());
        employInfo.setEmployeeno(extendServiceProcess.getEmployee().getSearchKey());
        employInfo.setEhcmPayscaleline(line);
        employInfo.setEmploymentgrade(extendServiceProcess.getEmploymentGrade());
        employInfo.setJobcode(extendServiceProcess.getPosition().getEhcmJobs());
        employInfo.setPosition(extendServiceProcess.getPosition());
        employInfo.setJobtitle(extendServiceProcess.getPosition().getJOBName().getJOBTitle());
        employInfo.setLocation(info.getLocation());
        if (info.getEhcmPayrollDefinition() != null)
          employInfo.setEhcmPayrollDefinition(info.getEhcmPayrollDefinition());
        if (extendServiceProcess.getSectionCode() != null)
          employInfo.setSectionName(extendServiceProcess.getSectionCode().getName());
        employInfo.setSectioncode(extendServiceProcess.getSectionCode());
        employInfo.setEhcmEmpPerinfo(extendServiceProcess.getEmployee());
        employInfo.setStartDate(extendServiceProcess.getEffectivedate());
        employInfo.setAlertStatus("ACT");
        employInfo.setEhcmExtendService(extendServiceProcess);
        employInfo.setDecisionNo(extendServiceProcess.getDecisionNo());
        employInfo.setDecisionDate(extendServiceProcess.getDecisionDate());
        OBQuery<EHCMEmpSupervisorNode> supervisior = OBDal.getInstance().createQuery(
            EHCMEmpSupervisorNode.class,
            "  as e where e.ehcmEmpPerinfo.id=:employeeId and e.client.id =:client");
        supervisior.setNamedParameter("employeeId", extendServiceProcess.getEmployee().getId());
        supervisior.setNamedParameter("client", extendServiceProcess.getClient().getId());
        List<EHCMEmpSupervisorNode> node = supervisior.list();
        if (node.size() > 0) {
          supervisorId = node.get(0).getEhcmEmpSupervisor();
          employInfo.setEhcmEmpSupervisor(supervisorId);
        }

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

        // Update the enddate for old hiring record.
        if (extendServiceProcess.getDecisionType().equals("CR")) {
          Date dateBefore = null;
          OBQuery<EmploymentInfo> empInfoold = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " as e where ehcmEmpPerinfo.id=:employeeId  and e.enabled='Y' order by e.creationDate desc");
          empInfoold.setNamedParameter("employeeId", extendServiceProcess.getEmployee().getId());
          empInfoold.setMaxResult(1);
          EmploymentInfo empinfo = empInfoold.list().get(0);
          empinfo.setUpdated(new java.util.Date());
          empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));

          Date startdate = empinfo.getStartDate();
          dateBefore = new Date(
              extendServiceProcess.getEffectivedate().getTime() - 1 * 24 * 3600 * 1000);

          if (startdate.compareTo(extendServiceProcess.getEffectivedate()) == 0)
            empinfo.setEndDate(empinfo.getStartDate());
          else
            empinfo.setEndDate(dateBefore);

          empinfo.setAlertStatus("INACT");
          empinfo.setEnabled(false);

          OBDal.getInstance().save(empinfo);
          OBDal.getInstance().flush();

        }
        OBDal.getInstance().save(employInfo);
        OBDal.getInstance().flush();

        // Update Case
        if (extendServiceProcess.getDecisionType().equals("UP")) {
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
            Date dateBefore = new Date(
                extendServiceProcess.getEffectivedate().getTime() - 1 * 24 * 3600 * 1000);
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
        if (extendServiceProcess.getDecisionType().equals("CO")) {
          person.setEmploymentStatus("AC");
        } else {
          person.setEmploymentStatus("EOS");
        }
        OBDal.getInstance().save(person);
        OBDal.getInstance().flush();

      }
      // cancel case
      else if (extendServiceProcess.getDecisionType().equals("CA")) {
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
          empinfo.setUpdated(new java.util.Date());
          empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          empinfo.setEndDate(null);
          empinfo.setEnabled(true);
          empinfo.setAlertStatus("ACT");
          empinfo.setEhcmEmpTransfer(null);
          OBDal.getInstance().save(empinfo);
          OBDal.getInstance().flush();

          if (empinfo.getEhcmExtendService() != null) {
            EhcmExtendService oldExtendService = empinfo.getEhcmExtendService();
            oldExtendService.setEnabled(true);
            OBDal.getInstance().save(oldExtendService);
            OBDal.getInstance().flush();
          }

          // remove the recent record
          OBQuery<EmploymentInfo> employInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id=:employeeId  and enabled='Y'  and ehcmExtendService.id ='"
                  + extendServiceProcess.getOriginalDecisionNo().getId()
                  + "' order by creationDate desc");
          employInfo.setNamedParameter("employeeId", extendServiceProcess.getEmployee().getId());
          employInfo.setMaxResult(1);
          empInfoList = employInfo.list();
          if (empInfoList.size() > 0) {
            EmploymentInfo empInfor = empInfoList.get(0);
            OBDal.getInstance().remove(empInfor);
            OBDal.getInstance().flush();
          }
        }
        EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
            extendServiceProcess.getEmployee().getId());

        person.setEmploymentStatus("AC");

        OBDal.getInstance().save(person);
        OBDal.getInstance().flush();

        // update old extrastep as inactive
        EhcmExtendService oldExtend = extendServiceProcess.getOriginalDecisionNo();
        oldExtend.setEnabled(false);
        OBDal.getInstance().save(oldExtend);
        OBDal.getInstance().flush();

        extendServiceProcess.setEnabled(false);
        OBDal.getInstance().save(extendServiceProcess);
        OBDal.getInstance().flush();
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

}