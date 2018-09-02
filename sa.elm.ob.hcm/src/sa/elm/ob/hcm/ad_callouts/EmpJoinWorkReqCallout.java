package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMBusMissionSummary;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMEmpPromotion;
import sa.elm.ob.hcm.EHCMEmpTransfer;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EHCMScholarshipSummary;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.dao.EmpJoinWorkReqCalloutDAO;
import sa.elm.ob.hcm.ad_callouts.dao.EmpJoinWorkReqCalloutDAOimpl;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * @author poongodi on 14/02/2018
 */
public class EmpJoinWorkReqCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpclient = vars.getStringParameter("inpadClientId");
    String joinDate = vars.getStringParameter("inpjoindate");
    String employmentViewId = vars.getStringParameter("inporiginalDecisionNo");
    String employmentInfoId = "";
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    DateFormat yearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    List<EHCMScholarshipSummary> summaryList = new ArrayList<EHCMScholarshipSummary>();
    List<EHCMBusMissionSummary> businessList = new ArrayList<EHCMBusMissionSummary>();
    Date startDate = null;
    Date endDate = null;
    String changeReason = vars.getStringParameter("inpjoinWorkreason");
    Date dateafter = null;
    String endDateAfter = null;
    String decisionDate = null;
    int millSec = 1 * 24 * 3600 * 1000;
    Date jnDAte = null;
    String inpauthorisedPerson = vars.getStringParameter("inpauthorisedPerson");
    String departmentId = null;
    EmpJoinWorkReqCalloutDAO obj = new EmpJoinWorkReqCalloutDAOimpl();

    try {
      String jnDateCnvrt = UtilityDAO.convertToGregorian(joinDate);
      jnDAte = yearFormat.parse(jnDateCnvrt);

      EmploymentInfo empinfo = null;
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and enabled='Y' order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", employeeId);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
      }
      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {

        if (StringUtils.isNotEmpty(employeeId)) {
          /* get Employee Details by using employeeId */
          EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);

          info.addResult("inpempName", employee.getArabicfullname());
          info.addResult("inpehcmEmpPerinfoId", employeeId);

          EHCMEmployeeStatusV employeeStatus = OBDal.getInstance().get(EHCMEmployeeStatusV.class,
              employeeId);
          if (employeeStatus != null)
            info.addResult("inpempStatus", employeeStatus.getStatusvalue());
          else
            info.addResult("inpempStatus", "");

          info.addResult("inpehcmGradeclassId", employee.getGradeClass().getId());
          info.addResult("inpempType", employee.getEhcmActiontype().getPersonType());

          if (employee.getHiredate() != null) {
            String query = " select eut_convert_to_hijri_timestamp('"
                + yearFormat.format(employee.getHiredate()) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next())
              info.addResult("inphireDate", rs.getString("eut_convert_to_hijri_timestamp"));

          }
          if (empinfo != null) {
            employmentInfoId = empinfo.getId();
            departmentId = empinfo.getPosition().getDepartment().getId();

            info.addResult("inpdepartmentId", departmentId);
            if (empinfo.getPosition().getSection() != null) {
              info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
            } else {
              info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
            }
            info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
            info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
            info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
            info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
            info.addResult("inpassignedDept", empinfo.getSECDeptName());
            info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
          }
        } else {
          info.addResult("inpempName", "");
          info.addResult("inpempStatus", "");
          info.addResult("inpempType", "");
          info.addResult("inphireDate", "");
          info.addResult("inpehcmGradeclassId", null);
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Department_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Ehcm_Grade_ID').setValue('')");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Position_ID').setValue('')");
          info.addResult("inpjobTitle", "");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Employmentgrade').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Gradeclass_ID').setValue('')");
        }
        JSONObject getauthoriztionInfoDetailsobj = obj.getAuthorizationInfoDetails(departmentId,
            jnDAte);
        if (getauthoriztionInfoDetailsobj != null && getauthoriztionInfoDetailsobj.length() > 0) {
          if (getauthoriztionInfoDetailsobj.has("authorizedPerson")) {
            info.addResult("inpauthorisedPerson",
                getauthoriztionInfoDetailsobj.getString("authorizedPerson"));
          } else {
            info.addResult("inpauthorisedPerson", "");
          }
          if (getauthoriztionInfoDetailsobj.has("authorizedJobTitle")) {
            info.addResult("inpauthorisesPersonJob",
                getauthoriztionInfoDetailsobj.getString("authorizedJobTitle"));
          } else {
            info.addResult("inpauthorisesPersonJob", "");
          }
        } else {
          info.addResult("inpauthorisedPerson", "");
          info.addResult("inpauthorisesPersonJob", "");
        }
      }

      if (lastfieldChanged.equals("inporiginalDecisionNo")
          || lastfieldChanged.equals("inpjoindate")) {
        // Employee Transfer
        EHCMEmpTransfer empTransfer = OBDal.getInstance().get(EHCMEmpTransfer.class,
            employmentViewId);
        if (empTransfer != null) {
          decisionDate = UtilityDAO
              .convertTohijriDate(yearFormat.format(empTransfer.getStartDate()));
          info.addResult("inpdecisionDate", decisionDate);
        }
        // Employee promotion
        EHCMEmpPromotion promotion = OBDal.getInstance().get(EHCMEmpPromotion.class,
            employmentViewId);
        if (promotion != null) {
          decisionDate = UtilityDAO.convertTohijriDate(yearFormat.format(promotion.getStartDate()));
          info.addResult("inpdecisionDate", decisionDate);
        }
        // Employee Suspension
        EmployeeSuspension suspension = OBDal.getInstance().get(EmployeeSuspension.class,
            employmentViewId);
        if (suspension != null) {
          if (suspension.getSuspensionType().equals("SUS")) {
            decisionDate = UtilityDAO
                .convertTohijriDate(yearFormat.format(suspension.getStartDate()));
            info.addResult("inpdecisionDate", decisionDate);
          } else if (suspension.getSuspensionType().equals("SUE")) {
            decisionDate = UtilityDAO
                .convertTohijriDate(yearFormat.format(suspension.getJoinDate()));
            info.addResult("inpdecisionDate", decisionDate);
          }
        }
        // Hiring and secondment record
        else if (empTransfer == null && promotion == null && suspension == null) {
          if (!changeReason.equals("SCTR") && !changeReason.equals("BM")) {
            info.addResult("inpdecisionDate", null);
            OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(
                EmploymentInfo.class, " ehcmEmpPerinfo.id=:employeeId and id =:EmploymentId");
            empInfoObj.setNamedParameter("employeeId", employeeId);
            empInfoObj.setNamedParameter("EmploymentId", employmentViewId);
            employmentInfo = empInfoObj.list();
            if (employmentInfo.size() > 0) {
              if (changeReason.equals("H")) {// -mail
                startDate = employmentInfo.get(0).getStartDate();
              } else {// -mail
                startDate = employmentInfo.get(0).getEndDate();
                startDate = new Date(startDate.getTime() + 1 * 24 * 3600 * 1000);
              }
              decisionDate = UtilityDAO.convertTohijriDate(yearFormat.format(startDate));
              info.addResult("inpdecisionDate", decisionDate);
            }
          } else if (changeReason.equals("SCTR")) {
            OBQuery<EHCMScholarshipSummary> scholarsummaryObj = OBDal.getInstance().createQuery(
                EHCMScholarshipSummary.class, "employee.id=:employeeId and id = :scholarId");
            scholarsummaryObj.setNamedParameter("employeeId", employeeId);
            scholarsummaryObj.setNamedParameter("scholarId", employmentViewId);
            summaryList = scholarsummaryObj.list();
            if (summaryList.size() > 0) {
              endDate = summaryList.get(0).getEndDate();
              dateafter = new Date(endDate.getTime() + 1 * 24 * 3600 * 1000);
              decisionDate = UtilityDAO.convertTohijriDate(yearFormat.format(dateafter));
              info.addResult("inpdecisionDate", decisionDate);
            }
          } else if (changeReason.equals("BM")) {
            OBQuery<EHCMBusMissionSummary> businessObj = OBDal.getInstance().createQuery(
                EHCMBusMissionSummary.class, "employee.id=:employeeId and id = :scholarId");
            businessObj.setNamedParameter("employeeId", employeeId);
            businessObj.setNamedParameter("scholarId", employmentViewId);
            businessList = businessObj.list();
            if (businessList.size() > 0) {
              String missionEndDate = businessList.get(0).getEndDate().toString();
              String businessEnddate = UtilityDAO.convertTohijriDate(missionEndDate);
              EHCMEmpBusinessMission mission = businessList.get(0).getEhcmEmpBusinessmission();
              Date calculateDays = Utility.calculateDateUsingDays(inpclient,
                  mission.getNoofdaysAfter().toString(), businessEnddate);
              info.addResult("inpdecisionDate",
                  (UtilityDAO.convertTohijriDate(yearFormat.format(calculateDays))));

            }
          }
        }
        if (decisionDate != null) {
          int noofdays = sa.elm.ob.hcm.util.UtilityDAO.calculatetheDays(decisionDate, joinDate);
          info.addResult("inpnoofdays", noofdays);
        }
        jnDAte = yearFormat.parse(jnDateCnvrt);
        departmentId = empinfo.getPosition().getDepartment().getId();
        JSONObject getauthoriztionInfoDetailsobj = obj.getAuthorizationInfoDetails(departmentId,
            jnDAte);
        if (lastfieldChanged.equals("inpjoindate")) {
          departmentId = empinfo.getPosition().getDepartment().getId();
          if (getauthoriztionInfoDetailsobj.length() > 0) {
            info.addResult("inpauthorisedPerson",
                getauthoriztionInfoDetailsobj.getString("authorizedPerson"));
            info.addResult("inpauthorisesPersonJob",
                getauthoriztionInfoDetailsobj.getString("authorizedJobTitle"));
          } else {
            info.addResult("inpauthorisedPerson", "");
            info.addResult("inpauthorisesPersonJob", "");
          }

        }

      }

      if (lastfieldChanged.equals("inpauthorisedPerson")) { // get active employment info
        EmploymentInfo emplyinfo = Utility.getActiveEmployInfo(inpauthorisedPerson);
        if (emplyinfo != null && emplyinfo.getPosition() != null) {
          info.addResult("inpauthorisesPersonJob", emplyinfo.getPosition().getId());
        }
      }

    } catch (

    Exception e) {
      log4j.error("Exception in EmpJoinWorkReq Callout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
