package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;
import sa.elm.ob.hcm.ad_callouts.dao.EmployeeSuspensionCalloutDAO;
import sa.elm.ob.hcm.ad_callouts.dao.EmployeeSuspensionCalloutDAOImpl;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author gopalakrishnan on 14/12/2016
 * 
 */

@SuppressWarnings("serial")
public class EmployeeSuspensionCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String suspensionId = vars.getStringParameter("inpehcmEmpSuspensionId");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String inpsuspensionType = vars.getStringParameter("inpsuspensionType");
    String inpEndDate = vars.getStringParameter("inpendDate");
    String inpStartDate = vars.getStringParameter("inpstartdate");
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpauthorisedPerson = vars.getStringParameter("inpauthorisedPerson");
    String inpsuspensionEndReason = vars.getStringParameter("inpsuspensionEndReason");
    Date startDate = null;
    String departmentId = null;
    String departmentCode = vars.getStringParameter("inpdepartmentId");
    Connection conn = OBDal.getInstance().getConnection();
    OBQuery<EmploymentInfo> empInfo = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    log4j.debug("lastfield:" + inpLastFieldChanged);
    log4j.debug("inpEndDate:" + inpEndDate);
    EmployeeSuspensionCalloutDAO obj = new EmployeeSuspensionCalloutDAOImpl();
    try {
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();
      EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
      String query = " select eut_convert_to_hijri_timestamp(?)";

      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class, " ehcmEmpPerinfo.id='"
          + employeeId
          + "' and enabled='Y'  and (alertStatus='ACT' or alertStatus='TE') order by created desc ");
      empInfo.setMaxResult(1);
      log4j.debug("empInfo" + empInfo.getWhereAndOrderBy());
      // employee number change
      if (inpLastFieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (StringUtils.isNotEmpty(employeeId)) {
          if (empInfo.list().size() > 0) {
            for (EmploymentInfo empinfo : empInfo.list()) {
              info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
              info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
              if (empinfo.getPosition().getSection() != null) {
                info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
              } else {
                info.addResult("JSEXECUTE",
                    "form.getFieldFromColumnName('Section_ID').setValue('')");
              }
              info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
              if (empinfo.getAssignedDepartment() != null) {
                info.addResult("inpassignedDept", empinfo.getAssignedDepartment().getId());
              }
              info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
              info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
              info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
              if (empinfo.getStartDate() != null) {
                log4j.debug("empInfo" + empinfo.getStartDate());
                st = conn.prepareStatement(query);
                st.setString(1, dateFormat.format(empinfo.getStartDate()));
                rs = st.executeQuery();

                if (rs.next()) {
                  log4j.debug("empInfo" + rs.getString("eut_convert_to_hijri_timestamp"));
                  info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));

                }
              }

              departmentId = empinfo.getPosition().getDepartment().getId();
              startDate = empinfo.getStartDate();
            }
          }
          info.addResult("inpemployeeName", employee.getArabicfullname());
          if (employee.getHiredate() != null) {

            st = conn.prepareStatement(query);
            st.setString(1, dateFormat.format(employee.getHiredate()));
            rs = st.executeQuery();
            if (rs.next())
              info.addResult("inphireDate", rs.getString("eut_convert_to_hijri_timestamp"));

          }
          info.addResult("inpemployeeType", employee.getEhcmActiontype().getPersonType());
          EHCMEmployeeStatusV employeeStatus = OBDal.getInstance().get(EHCMEmployeeStatusV.class,
              employeeId);
          if (employeeStatus != null)
            info.addResult("inpemployeeStatus", employeeStatus.getStatusvalue());
          else
            info.addResult("inpemployeeStatus", "");
          if (employee.getGradeClass() != null)
            info.addResult("inpehcmGradeclassId", employee.getGradeClass().getId());

          if (inpdecisionType.equals("CR")) {
            if (inpsuspensionType.equals("SUE")) {
              OBQuery<EmployeeSuspension> objSusQuery = OBDal.getInstance()
                  .createQuery(EmployeeSuspension.class, "as e where e.ehcmEmpPerinfo.id='"
                      + employeeId
                      + "' and e.enabled='Y' and e.suspensionType='SUS' and e.issueDecision='Y' order by e.creationDate desc");
              objSusQuery.setFilterOnActive(false);
              objSusQuery.setMaxResult(1);
              if (objSusQuery.list().size() > 0) {
                EmployeeSuspension objSuspension = objSusQuery.list().get(0);
                info.addResult("inporiginalDecisionNo", objSuspension.getId());

                // if (objSuspension.getStartDate() != null) {
                // st = conn.prepareStatement(query);
                // st.setString(1, dateFormat.format(objSuspension.getStartDate()));
                // rs = st.executeQuery();
                // if (rs.next()) {
                // info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));
                // }
                // }

              }
            } else {
              info.addResult("inporiginalDecisionNo", null);
              info.addResult("inpendDate", "");
            }
          }
          if (inpdecisionType.equals("CA") || inpdecisionType.equals("UP")) {
            if (inpsuspensionType.equals("SUS")) {
              OBQuery<EmployeeSuspension> objSusQuery = OBDal.getInstance()
                  .createQuery(EmployeeSuspension.class, "as e where e.ehcmEmpPerinfo.id='"
                      + employeeId
                      + "' and e.enabled='Y' and e.suspensionType='SUS' and e.issueDecision='Y' order by e.creationDate desc");
              objSusQuery.setFilterOnActive(false);
              objSusQuery.setMaxResult(1);
              if (objSusQuery.list().size() > 0) {
                EmployeeSuspension objSuspension = objSusQuery.list().get(0);
                info.addResult("inporiginalDecisionNo", objSuspension.getId());
                info.addResult("inpsuspensionReason", objSuspension.getSuspensionReason().getId());
                info.addResult("inpauthorisedPerson", objSuspension.getAuthorisedPerson());
                info.addResult("inpauthorisesPersonJob",
                    objSuspension.getAuthorisedPersonJobTitle());
                if (objSuspension.getStartDate() != null) {
                  st = conn.prepareStatement(query);
                  st.setString(1, dateFormat.format(objSuspension.getStartDate()));
                  rs = st.executeQuery();
                  if (rs.next())
                    info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));

                }
                if (objSuspension.getJoinDate() != null) {
                  st = conn.prepareStatement(query);
                  st.setString(1, dateFormat.format(objSuspension.getJoinDate()));
                  rs = st.executeQuery();
                  if (rs.next())
                    info.addResult("inpjoinDate", rs.getString("eut_convert_to_hijri_timestamp"));
                }
                if (objSuspension.getExpectedEndDate() != null) {
                  st = conn.prepareStatement(query);
                  st.setString(1, dateFormat.format(objSuspension.getExpectedEndDate()));
                  rs = st.executeQuery();
                  if (rs.next())
                    info.addResult("inpexpectedEnddate",
                        rs.getString("eut_convert_to_hijri_timestamp"));
                }

              } else {
                info.addResult("inpsuspensionReason", "");
                info.addResult("inporiginalDecisionNo", null);
                info.addResult("inpauthorisedPerson", "");
                info.addResult("inpauthorisesPersonJob", "");
                info.addResult("inpstartdate", "");
                info.addResult("inpjoinDate", "");
                info.addResult("inpexpectedEnddate", "");

              }
            } else if (inpsuspensionType.equals("SUE")) {
              OBQuery<EmployeeSuspension> objSusQuery = OBDal.getInstance()
                  .createQuery(EmployeeSuspension.class, "as e where e.ehcmEmpPerinfo.id='"
                      + employeeId
                      + "' and e.enabled='Y' and e.suspensionType='SUE' and e.issueDecision='Y' and e.isJoinWorkRequestRequired = 'N' order by e.creationDate desc");
              objSusQuery.setFilterOnActive(false);
              objSusQuery.setMaxResult(1);
              if (objSusQuery.list().size() > 0) {
                EmployeeSuspension objSuspension = objSusQuery.list().get(0);
                info.addResult("inporiginalDecisionNo", objSuspension.getId());
                info.addResult("inpsuspensionEndReason",
                    objSuspension.getSuspensionEndReason().getId());
                if (objSuspension.getStartDate() != null) {
                  st = conn.prepareStatement(query);
                  st.setString(1, dateFormat.format(objSuspension.getStartDate()));
                  rs = st.executeQuery();
                  if (rs.next())
                    info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));

                }
                if (objSuspension.getEndDate() != null) {
                  st = conn.prepareStatement(query);
                  st.setString(1, dateFormat.format(objSuspension.getEndDate()));
                  rs = st.executeQuery();
                  if (rs.next())
                    info.addResult("inpendDate", rs.getString("eut_convert_to_hijri_timestamp"));
                }
                if (objSuspension.getJoinDate() != null) {
                  st = conn.prepareStatement(query);
                  st.setString(1, dateFormat.format(objSuspension.getJoinDate()));
                  rs = st.executeQuery();
                  if (rs.next())
                    info.addResult("inpjoinDate", rs.getString("eut_convert_to_hijri_timestamp"));
                }
                // info.addResult("inpauthorisedPerson", objSuspension.getAuthorisedPerson());
                // info.addResult("inpauthorisesPersonJob",
                // objSuspension.getAuthorisedPersonJobTitle());

              } else {
                info.addResult("inporiginalDecisionNo", null);
                info.addResult("inpsuspensionEndReason", null);
                info.addResult("inpstartdate", "");
                info.addResult("inpjoinDate", "");
                // info.addResult("inpauthorisedPerson", "");
                // info.addResult("inpauthorisesPersonJob", "");
              }
            }
          }
          if (departmentId != null && startDate != null) {
            JSONObject authorizationInfoObj = obj.getAuthorizationInfoDetails(departmentId,
                startDate);
            if (authorizationInfoObj != null && authorizationInfoObj.length() > 0) {
              info.addResult("inpauthorisedPerson",
                  authorizationInfoObj.getString("authorizedPerson"));
              info.addResult("inpauthorisesPersonJob",
                  authorizationInfoObj.getString("authorizedJobTitle"));
            } else {
              info.addResult("inpauthorisedPerson", "");
              info.addResult("inpauthorisesPersonJob", "");
            }
          }
        } else {
          callouts.SetEmpDetailsNull(info);
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
          /*
           * info.addResult("inpemployeeName", ""); info.addResult("inpemployeeStatus", "");
           * info.addResult("inpemployeeType", ""); info.addResult("inpempType", "");
           * info.addResult("inphireDate", ""); info.addResult("inpehcmGradeclassId", null);
           * info.addResult("JSEXECUTE",
           * "form.getFieldFromColumnName('Department_ID').setValue('')");
           * info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
           * info.addResult("JSEXECUTE",
           * "form.getFieldFromColumnName('Ehcm_Grade_ID').setValue('')");
           * info.addResult("JSEXECUTE",
           * "form.getFieldFromColumnName('Ehcm_Position_ID').setValue('')");
           * info.addResult("inpjobTitle", ""); info.addResult("JSEXECUTE",
           * "form.getFieldFromColumnName('Employmentgrade').setValue('')");
           * info.addResult("JSEXECUTE",
           * "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
           * info.addResult("JSEXECUTE",
           * "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
           * info.addResult("JSEXECUTE",
           * "form.getFieldFromColumnName('Ehcm_Gradeclass_ID').setValue('')");
           */
        }
      }
      // type change
      if (inpLastFieldChanged.equals("inpsuspensionType")
          || inpLastFieldChanged.equals("inpdecisionType")) {
        if (inpdecisionType.equals("CR")) {
          if (inpsuspensionType.equals("SUE")) {
            OBQuery<EmployeeSuspension> objSusQuery = OBDal.getInstance()
                .createQuery(EmployeeSuspension.class, "as e where e.ehcmEmpPerinfo.id='"
                    + employeeId
                    + "' and e.enabled='Y' and e.suspensionType='SUS' and e.issueDecision='Y' order by e.creationDate desc");
            objSusQuery.setFilterOnActive(false);
            objSusQuery.setMaxResult(1);
            if (objSusQuery.list().size() > 0) {
              EmployeeSuspension objSuspension = objSusQuery.list().get(0);
              info.addResult("inporiginalDecisionNo", objSuspension.getId());
              if (empInfo.list().size() > 0) {
                for (EmploymentInfo empinfo : empInfo.list()) {
                  if (empinfo.getStartDate() != null) {
                    log4j.debug("empInfo" + empinfo.getStartDate());
                    st = conn.prepareStatement(query);
                    st.setString(1, dateFormat.format(empinfo.getStartDate()));
                    rs = st.executeQuery();

                    if (rs.next())
                      log4j.debug("empInfo" + rs.getString("eut_convert_to_hijri_timestamp"));
                    info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));

                  }
                }
              }
              // if (objSuspension.getStartDate() != null) {
              // st = conn.prepareStatement(query);
              // st.setString(1, dateFormat.format(objSuspension.getStartDate()));
              // rs = st.executeQuery();
              // if (rs.next()) {
              // info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));
              // }
              // }

            }
          } else {
            info.addResult("inporiginalDecisionNo", null);
            info.addResult("inpendDate", "");
          }
        }
        if (inpdecisionType.equals("CA") || inpdecisionType.equals("UP")) {
          if (inpsuspensionType.equals("SUS")) {
            log4j.debug("changing :" + inpdecisionType);

            OBQuery<EmployeeSuspension> objSusQuery = OBDal.getInstance()
                .createQuery(EmployeeSuspension.class, "as e where e.ehcmEmpPerinfo.id='"
                    + employeeId
                    + "' and e.enabled='Y' and e.suspensionType='SUS' and e.issueDecision='Y' order by e.creationDate desc");
            objSusQuery.setFilterOnActive(false);
            objSusQuery.setMaxResult(1);
            if (objSusQuery.list().size() > 0) {
              EmployeeSuspension objSuspension = objSusQuery.list().get(0);
              info.addResult("inporiginalDecisionNo", objSuspension.getId());
              info.addResult("inpsuspensionReason", objSuspension.getSuspensionReason().getId());
              info.addResult("inpauthorisedPerson", objSuspension.getAuthorisedPerson());
              info.addResult("inpauthorisesPersonJob", objSuspension.getAuthorisedPersonJobTitle());
              if (objSuspension.getStartDate() != null) {
                log4j.debug("inside:" + objSuspension.getStartDate());
                st = conn.prepareStatement(query);
                st.setString(1, dateFormat.format(objSuspension.getStartDate()));
                rs = st.executeQuery();
                if (rs.next())
                  info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));

              }
              if (objSuspension.getJoinDate() != null) {
                st = conn.prepareStatement(query);
                st.setString(1, dateFormat.format(objSuspension.getJoinDate()));
                rs = st.executeQuery();
                if (rs.next())
                  info.addResult("inpjoinDate", rs.getString("eut_convert_to_hijri_timestamp"));
              }
              if (objSuspension.getExpectedEndDate() != null) {
                st = conn.prepareStatement(query);
                st.setString(1, dateFormat.format(objSuspension.getExpectedEndDate()));
                rs = st.executeQuery();
                if (rs.next())
                  info.addResult("inpexpectedEnddate",
                      rs.getString("eut_convert_to_hijri_timestamp"));
              }

            } else {
              info.addResult("inpsuspensionReason", null);
              info.addResult("inporiginalDecisionNo", null);
              info.addResult("inpauthorisedPerson", "");
              info.addResult("inpauthorisesPersonJob", "");
              info.addResult("inpstartdate", "");
              info.addResult("inpjoinDate", "");
              info.addResult("inpexpectedEnddate", "");

            }
          } else if (inpsuspensionType.equals("SUE")) {
            OBQuery<EmployeeSuspension> objSusQuery = OBDal.getInstance()
                .createQuery(EmployeeSuspension.class, "as e where e.ehcmEmpPerinfo.id='"
                    + employeeId
                    + "' and e.enabled='Y' and e.suspensionType='SUE' and e.issueDecision='Y' and e.isJoinWorkRequestRequired = 'N' order by e.creationDate desc");
            objSusQuery.setFilterOnActive(false);
            objSusQuery.setMaxResult(1);
            if (objSusQuery.list().size() > 0) {
              EmployeeSuspension objSuspension = objSusQuery.list().get(0);
              info.addResult("inporiginalDecisionNo", objSuspension.getId());
              info.addResult("inpsuspensionEndReason",
                  objSuspension.getSuspensionEndReason().getId());
              if (objSuspension.getStartDate() != null) {
                st = conn.prepareStatement(query);
                st.setString(1, dateFormat.format(objSuspension.getStartDate()));
                rs = st.executeQuery();
                if (rs.next())
                  info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));

              }
              if (objSuspension.getEndDate() != null) {
                st = conn.prepareStatement(query);
                st.setString(1, dateFormat.format(objSuspension.getEndDate()));
                rs = st.executeQuery();
                if (rs.next())
                  info.addResult("inpendDate", rs.getString("eut_convert_to_hijri_timestamp"));
              }
              if (objSuspension.getJoinDate() != null) {
                st = conn.prepareStatement(query);
                st.setString(1, dateFormat.format(objSuspension.getJoinDate()));
                rs = st.executeQuery();
                if (rs.next())
                  info.addResult("inpjoinDate", rs.getString("eut_convert_to_hijri_timestamp"));
              }
              info.addResult("inpauthorisedPerson", objSuspension.getAuthorisedPerson());
              info.addResult("inpauthorisesPersonJob", objSuspension.getAuthorisedPersonJobTitle());

            } else {
              info.addResult("inporiginalDecisionNo", null);
              info.addResult("inpsuspensionEndReason", null);
              info.addResult("inpstartdate", "");
              info.addResult("inpjoinDate", "");
              info.addResult("inpauthorisedPerson", "");
              info.addResult("inpauthorisesPersonJob", "");
            }
          }
        }

      }
      // end date change
      if (inpLastFieldChanged.equals("inpendDate")) {
        if (inpsuspensionType.equals("SUE")) {
          String strGregEndDate = UtilityDAO.convertToGregorian(inpEndDate);
          Date endDate = dateFormat.parse(strGregEndDate);
          log4j.debug("endDate after conversion:" + endDate);
          Date dateBefore = new Date(endDate.getTime() + 1 * 24 * 3600 * 1000);
          st = conn.prepareStatement(query);
          st.setString(1, dateFormat.format(dateBefore));
          rs = st.executeQuery();
          if (rs.next()) {
            info.addResult("inpjoinDate", rs.getString("eut_convert_to_hijri_timestamp"));
          }
        } else {
          info.addResult("inpjoinDate", "");
        }
      }

      // start_date change
      if (inpLastFieldChanged.equals("inpstartdate")) {
        String strGregStartDate = UtilityDAO.convertToGregorian(inpStartDate);
        Date strtDate = dateFormat.parse(strGregStartDate);
        log4j.debug("startDate after conversion:" + strtDate);
        JSONObject authorizationInfoObj1 = obj.getAuthorizationInfoDetails(departmentCode,
            strtDate);
        if (authorizationInfoObj1 != null && authorizationInfoObj1.length() > 0) {
          info.addResult("inpauthorisedPerson",
              authorizationInfoObj1.getString("authorizedPerson"));
          info.addResult("inpauthorisesPersonJob",
              authorizationInfoObj1.getString("authorizedJobTitle"));
        } else {
          info.addResult("inpauthorisedPerson", "");
          info.addResult("inpauthorisesPersonJob", "");
        }

      }

      // suspension end reason change
      if (inpLastFieldChanged.equals("inpsuspensionEndReason")) {
        if (inpEndDate != "") {
          PreparedStatement stmt = conn.prepareStatement(
              "select value from Ehcm_Suspension_Reason where Ehcm_Suspension_Reason_ID=?");
          stmt.setString(1, inpsuspensionEndReason);
          rs = stmt.executeQuery();
          if (rs.next()) {
            String suspEndReason = rs.getString("value");
            if ((!suspEndReason.equals("T")) && (!suspEndReason.equals("TRD"))) {
              String strGregEndDate = UtilityDAO.convertToGregorian(inpEndDate);
              Date endDate = dateFormat.parse(strGregEndDate);
              log4j.debug("endDate after conversion:" + endDate);
              Date dateBefore = new Date(endDate.getTime() + 1 * 24 * 3600 * 1000);
              st = conn.prepareStatement(query);
              st.setString(1, dateFormat.format(dateBefore));
              rs = st.executeQuery();
              if (rs.next()) {
                info.addResult("inpjoinDate", rs.getString("eut_convert_to_hijri_timestamp"));
              }
            }
          }
        }
      }

      /*
       * if (inpLastFieldChanged.equals("inpauthorisedPerson")) { // get active employment info
       * EmploymentInfo emplyinfo = Utility.getActiveEmployInfo(inpauthorisedPerson); if (emplyinfo
       * != null && emplyinfo.getPosition() != null) { info.addResult("inpauthorisesPersonJob",
       * emplyinfo.getPosition().getId()); } }
       */

    } catch (

    Exception e) {
      log4j.error("Exception in Employee Suspension Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
