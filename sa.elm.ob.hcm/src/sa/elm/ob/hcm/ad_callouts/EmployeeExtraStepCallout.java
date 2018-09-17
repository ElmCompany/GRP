package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmEmployeeExtraStep;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;

/**
 * @author poongodi on 05/02/2018
 */
public class EmployeeExtraStepCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpstartdate = vars.getStringParameter("inpstartdate");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String inpclient = vars.getStringParameter("inpadClientId");
    String originaldecNo = vars.getStringParameter("inporiginalDecisionNo");
    String employmentInfoId = "";
    String payscaleId = "";
    String nextGradePoint = "";
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    log4j.debug("lastfieldChanged:" + lastfieldChanged);
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    try {
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();
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
                + dateFormat.format(employee.getHiredate()) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next())
              info.addResult("inphireDate", rs.getString("eut_convert_to_hijri_timestamp"));

          }
          if (empinfo != null) {
            employmentInfoId = empinfo.getId();
            info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
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
            log4j.debug("id:" + empinfo.getEhcmPayscaleline().getId());
            info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
          }
          if (empinfo.getStartDate() != null) {
            String query = " select eut_convert_to_hijri('"
                + dateFormat.format(empinfo.getStartDate()) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next()) {
              info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri"));
              inpstartdate = rs.getString("eut_convert_to_hijri");
              log4j.debug("inpstartdate:" + rs.getString("eut_convert_to_hijri"));
            }
          }

          inpdecisionType = "CR";
          info.addResult("inpdecisionType", inpdecisionType);
          info.addResult("inporiginalDecisionNo", "");

        } else {
          callouts.SetEmpDetailsNull(info);
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
          /*
           * info.addResult("inpempName", ""); info.addResult("inpempStatus", "");
           * info.addResult("inpempType", ""); info.addResult("inphireDate", "");
           * info.addResult("inpehcmGradeclassId", null); info.addResult("JSEXECUTE",
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

      if (inpdecisionType.equals("CR")) {
        // To find next grade step
        if (StringUtils.isNotEmpty(employeeId)) {
          OBQuery<ehcmpayscaleline> seqOrder = OBDal.getInstance()
              .createQuery(ehcmpayscaleline.class, "  as e where e.ehcmPayscale.id='"
                  + empinfo.getEhcmPayscaleline().getEhcmPayscale().getId() + "' and e.id not in ('"
                  + empinfo.getEhcmPayscaleline().getId() + "')" + " and e.lineNo >"
                  + empinfo.getEhcmPayscaleline().getLineNo() + " order by e.lineNo asc");
          seqOrder.setMaxResult(1);
          if (seqOrder.list().size() > 0) {
            nextGradePoint = seqOrder.list().get(0).getId();
            info.addResult("inpnewgradepoint", nextGradePoint);
          } else {
            info.addResult("inpnewgradepoint", empinfo.getEhcmPayscaleline().getId());
          }
        } else {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Newgradepoint').setValue('')");
        }
      } else {
        if (StringUtils.isNotEmpty(employeeId)) {
          info.addResult("inpnewgradepoint", empinfo.getEhcmPayscaleline().getId());
        } else {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Newgradepoint').setValue('')");
        }
      }

      if (lastfieldChanged.equals("inpdecisionType")) {
        if (StringUtils.isNotEmpty(employeeId)) {
          if (!inpdecisionType.equals("CR")) {
            info.addResult("inpnewgradepoint", empinfo.getEhcmPayscaleline().getId());
            OBQuery<EhcmEmployeeExtraStep> objEmpQuery = OBDal.getInstance().createQuery(
                EhcmEmployeeExtraStep.class,
                "as e where e.ehcmEmpPerinfo.id=:employeeId and e.enabled='Y' and e.issueDecision='Y'  order by e.creationDate desc");
            objEmpQuery.setNamedParameter("employeeId", employeeId);
            objEmpQuery.setMaxResult(1);
            List<EhcmEmployeeExtraStep> extrastepList = objEmpQuery.list();
            if (extrastepList.size() > 0) {
              info.addResult("inporiginalDecisionNo", extrastepList.get(0).getId());
              info.addResult("inpdecisionType", inpdecisionType);
            }

          }

        } else {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Newgradepoint').setValue('')");
        }

      }
    } catch (

    Exception e) {
      log4j.error("Exception in EmpExtrastep Callout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
