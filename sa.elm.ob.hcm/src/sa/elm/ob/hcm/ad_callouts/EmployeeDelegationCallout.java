package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;

/**
 * 
 * @author gopalakrishnan on 16/11/2016
 * 
 */

@SuppressWarnings("serial")
public class EmployeeDelegationCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String inpnewEhcmPositionId = vars.getStringParameter("inpnewEhcmPositionId");
    String inpehcmGradeId = vars.getStringParameter("inpehcmGradeId");
    String departmentId = vars.getStringParameter("inpdepartmentId");
    String inpehcmPositionId = vars.getStringParameter("inpehcmPositionId");
    String delegationType = vars.getStringParameter("inpdelegationType");
    String newdepartmentId = vars.getStringParameter("inpnewDepartmentId");
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String employmentInfoId = "";
    Connection conn = OBDal.getInstance().getConnection();
    OBQuery<EmploymentInfo> empInfo = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    log4j.debug("lastfield:" + inpLastFieldChanged);
    try {
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();
      EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class, " ehcmEmpPerinfo.id='"
          + employeeId + "' and enabled='Y'  and alertStatus='ACT' order by created desc ");
      empInfo.setMaxResult(1);
      if (!inpLastFieldChanged.equals("inpnewEhcmPositionId")) {
        if (empInfo.list().size() > 0) {
          for (EmploymentInfo empinfo : empInfo.list()) {
            employmentInfoId = empinfo.getId();
            info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
            info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
            if (empinfo.getPosition().getSection() != null) {
              info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
            } else {
              info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
            }
            info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
            if (empinfo.getAssignedDepartment() != null) {
              info.addResult("inpassignedDepartmentId", empinfo.getAssignedDepartment().getId());
            }
            info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
            info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
            info.addResult("inpnewDepartmentId", empinfo.getPosition().getDepartment().getId());
          }
        }

      }
      if (inpLastFieldChanged.equals("inpnewDepartmentId")) {
        log4j.debug("Changing newDepartMent");
        // info.addResult("inpnewEhcmPositionId", "");
        info.addResult("JSEXECUTE",
            "form.getFieldFromColumnName('NEW_Ehcm_Position_ID').setValue('')");
        info.addResult("inpnewSectionId", "");
        info.addResult("inpnewJobCode", "");
        info.addResult("inpnewJobTitle", "");
        info.addResult("inpnewJobNo", "");
        info.addResult("inpnewPositionCode", "");

      }
      if (inpLastFieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (StringUtils.isNotEmpty(employeeId)) {
          info.addResult("inpemployeeName", employee.getArabicfullname());
          if (employee.getHiredate() != null) {
            String query = " select eut_convert_to_hijri_timestamp('"
                + dateFormat.format(employee.getHiredate()) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next())
              info.addResult("inphireDate", rs.getString("eut_convert_to_hijri_timestamp"));

          }
          info.addResult("inpemployeeType", employee.getEhcmActiontype().getPersonType());

          EHCMEmployeeStatusV employeeStatus = OBDal.getInstance().get(EHCMEmployeeStatusV.class,
              employeeId);
          if (employeeStatus != null)
            info.addResult("inpempStatus", employeeStatus.getStatusvalue());
          else
            info.addResult("inpempStatus", "");

        } else {
          callouts.SetEmpDetailsNull(info);
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Assigned_Department_ID').setValue('')");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('NEW_Ehcm_Position_ID').setValue('')");
          info.addResult("inpnewSectionId", "");
          info.addResult("inpnewJobCode", "");
          info.addResult("inpnewJobTitle", "");
          info.addResult("inpnewJobNo", "");
          info.addResult("inpnewPositionCode", "");
          info.addResult("inporiginalDecisionNo", null);
          info.addResult("inpdelegationType", "");
          info.addResult("inpdecisionType", "CR");

        }
      }

      if (inpLastFieldChanged.equals("inpdelegationType")) {
        // info.addResult("inpnewEhcmPositionId", "");
        info.addResult("JSEXECUTE",
            "form.getFieldFromColumnName('NEW_Ehcm_Position_ID').setValue('')");
        info.addResult("inpnewSectionId", "");
        info.addResult("inpnewJobCode", "");
        info.addResult("inpnewJobTitle", "");
        info.addResult("inpnewJobNo", "");
        info.addResult("inpnewPositionCode", "");
        if (delegationType.equals("DIN"))
          info.addResult("inpnewDepartmentId", departmentId);
      }

      if (inpLastFieldChanged.equals("inpdecisionType")) {
        info.addResult("inpnewDepartmentId", newdepartmentId);
        if (inpdecisionType.equals("UP") || inpdecisionType.equals("CA")) {
          OBQuery<EmployeeDelegation> objEmpQuery = OBDal.getInstance().createQuery(
              EmployeeDelegation.class,
              "as e where e.ehcmEmploymentInfo.id='" + employmentInfoId
                  + "' and e.ehcmEmploymentInfo.enabled='Y' and e.enabled='Y' and e.decisionStatus != 'UP'"
                  + "  and e.ehcmEmploymentInfo.alertStatus='ACT' order by e.creationDate desc");
          objEmpQuery.setMaxResult(1);
          if (objEmpQuery.list().size() > 0) {
            EmployeeDelegation empDelegation = objEmpQuery.list().get(0);
            info.addResult("inporiginalDecisionNo", empDelegation.getId());
            info.addResult("inpdelegationType", empDelegation.getDelegationType());
            if (empDelegation.getNewDepartment() != null) {
              // info.addResult("inpnewDepartmentId", empDelegation.getNewDepartment().getId());
              info.addResult("inpnewDepartmentId", empDelegation.getNewDepartment().getId());

            }
            if (empDelegation.getNewPosition() != null) {
              info.addResult("inpnewEhcmPositionId", empDelegation.getNewPosition().getId());
            }
            if (empDelegation.getNewSection() != null) {
              info.addResult("inpnewSectionId", empDelegation.getNewSection().getId());
            }
            info.addResult("inpnewJobCode", empDelegation.getNewJobCode());
            info.addResult("inpnewJobTitle", empDelegation.getNewJobTitle());
            info.addResult("inpnewJobNo", empDelegation.getNewJobNo());
            if (empDelegation.getNewPosition() != null
                && empDelegation.getNewPosition().getGrade() != null) {
              info.addResult("inpnewPositionCode",
                  empDelegation.getNewPosition().getGrade().getSearchKey() + "-"
                      + empDelegation.getNewJobNo());
            } else
              info.addResult("inpnewPositionCode", "");
            if (empDelegation.getStartDate() != null) {
              String query = " select eut_convert_to_hijri_timestamp('"
                  + dateFormat.format(empDelegation.getStartDate()) + "')";

              st = conn.prepareStatement(query);
              rs = st.executeQuery();
              if (rs.next())
                info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));

            }
            if (empDelegation.getEndDate() != null) {
              String query = " select eut_convert_to_hijri_timestamp('"
                  + dateFormat.format(empDelegation.getEndDate()) + "')";

              st = conn.prepareStatement(query);
              rs = st.executeQuery();
              if (rs.next())
                info.addResult("inpenddate", rs.getString("eut_convert_to_hijri_timestamp"));

            }
          }
        }
      }
      if (inpLastFieldChanged.equals("inpnewEhcmPositionId") && !inpnewEhcmPositionId.equals("")) {
        EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class, inpnewEhcmPositionId);
        if (pos.getSection() != null) {
          info.addResult("inpnewSectionId", pos.getSection().getId());
        } else {
          info.addResult("inpnewSectionId", null);
        }

        if (pos.getEhcmJobs() != null) {
          Jobs job = OBDal.getInstance().get(Jobs.class, pos.getEhcmJobs().getId());
          info.addResult("inpnewJobCode", job.getJobCode());
          info.addResult("inpnewJobTitle", job.getJOBTitle());
        } else {
          info.addResult("inpnewJobCode", "");
          info.addResult("inpnewJobTitle", "");
        }
        info.addResult("inpnewJobNo", pos.getJOBNo());
        if (pos.getGrade() != null) {
          info.addResult("inpnewPositionCode",
              pos.getGrade().getSearchKey() + "-" + pos.getJOBNo());
        } else
          info.addResult("inpnewPositionCode", "");
      }

      if (inpLastFieldChanged.equals("inpehcmPositionId")) {
        empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class, " ehcmEmpPerinfo.id='"
            + employeeId + "' and  position.id='" + inpehcmPositionId + "' and enabled='Y' ");
        if (empInfo.list().size() > 0) {
          for (EmploymentInfo empinfo : empInfo.list()) {
            info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
            info.addResult("inpjobTitle", empinfo.getJobtitle());
          }
        }
      }
      if (inpLastFieldChanged.equals("inpehcmGradeId")) {
        empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class, " ehcmEmpPerinfo.id='"
            + employeeId + "' and  grade.id='" + inpehcmGradeId + "' and enabled='Y'");
        if (empInfo.list().size() > 0) {
          for (EmploymentInfo empinfo : empInfo.list()) {
            info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
            info.addResult("inpjobTitle", empinfo.getJobtitle());
          }
        }

      }

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in EmployeeDelegation Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
