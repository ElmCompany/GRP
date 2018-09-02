package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EHCMHoldUnHoldSalary;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.dao.EndofEmploymentCalloutDAO;
import sa.elm.ob.hcm.ad_callouts.dao.EndofEmploymentCalloutDAOImpl;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class HoldUnholdSalaryCallout extends SimpleCallout {

  private static final long serialVersionUID = -9141215828493029089L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;

    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inporiginalDecisionNo = vars.getStringParameter("inporiginalDecisionNo");
    String departmentId = null;
    Date strtDate = null;
    DateFormat yearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    Connection conn = OBDal.getInstance().getConnection();
    DateFormat dateFormat = sa.elm.ob.utility.util.Utility.YearFormat;

    PreparedStatement st = null;
    ResultSet rs = null;
    EndofEmploymentCalloutDAO endofemploymentobj = new EndofEmploymentCalloutDAOImpl();
    try {
      EmploymentInfo empinfo = null;
      empinfo = Utility.getActiveEmployInfo(employeeId);

      log4j.debug("lastfieldChanged:" + lastfieldChanged);

      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (StringUtils.isNotEmpty(employeeId)) {
          log4j.debug("entered  person");
          /* get Employee Details by using employeeId */
          EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          info.addResult("inpempName", employee.getArabicfullname());

          EHCMEmployeeStatusV employeeStatus = OBDal.getInstance().get(EHCMEmployeeStatusV.class,
              employeeId);
          if (employeeStatus != null)
            info.addResult("inpempStatus", employeeStatus.getStatusvalue());
          else
            info.addResult("inpempStatus", "");

          info.addResult("inpehcmGradeclassId", employee.getGradeClass().getId());
          info.addResult("inpempType", employee.getEhcmActiontype().getPersonType());
          if (employee.getHiredate() != null) {
            info.addResult("inphireDate",
                (UtilityDAO.convertTohijriDate(yearFormat.format(employee.getHiredate()))));
          }
          if (empinfo != null) {
            empinfo.getId();
            info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
            if (empinfo.getPosition() != null && empinfo.getPosition().getSection() != null) {
              info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
            } else {
              info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
            }
            info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
            info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
            log4j.debug("inpehcmPositionId:" + empinfo.getPosition().getJOBNo());
            info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
            info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
            info.addResult("inpassignedDept", empinfo.getSECDeptName());
            info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
            info.addResult("inpehcmGradestepsId",
                empinfo.getEhcmPayscale().getEhcmGradesteps().getId());

            String query = " select eut_convert_to_hijri_timestamp('"
                + yearFormat.format(new Date()) + "')";
            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next())
              info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));
            info.addResult("inpdecisionType", "CR");
            info.addResult("inporiginalDecisionNo", "");

            // Authorized Person Details
            departmentId = empinfo.getPosition().getDepartment().getId();
            strtDate = yearFormat.parse(yearFormat.format(new Date()));
            JSONObject authorizationInfoObj = endofemploymentobj
                .getAuthorizationInfoDetails(departmentId, strtDate);
            if ((authorizationInfoObj != null) && (authorizationInfoObj.length() > 0)) {
              info.addResult("inpehcmAuthorizePersonId",
                  authorizationInfoObj.getString("authorizedPerson"));
              info.addResult("inpauthorizePersonTitle",
                  authorizationInfoObj.getString("authorizedJobTitle"));
            } else {
              info.addResult("inpehcmAuthorizePersonId", "");
              info.addResult("inpauthorizePersonTitle", "");
            }
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
      }

      if (lastfieldChanged.equals("inpdecisionType")
          || (lastfieldChanged.equals("inporiginalDecisionNo"))) {
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          EHCMHoldUnHoldSalary holdunhold = OBDal.getInstance().get(EHCMHoldUnHoldSalary.class,
              inporiginalDecisionNo);
          if (holdunhold != null) {
            if (holdunhold.getLetterNo() != null)
              info.addResult("inpletterNo", holdunhold.getLetterNo());
            else
              info.addResult("inpletterNo", null);
            if (holdunhold.getLetterDate() != null)
              info.addResult("inpletterDate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(holdunhold.getLetterDate())));
            else
              info.addResult("inpletterDate", null);

            if (holdunhold.getStartDate() != null)
              info.addResult("inpstartdate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(holdunhold.getStartDate())));
            info.addResult("inpreason", holdunhold.getReason());
            info.addResult("inprequestType", holdunhold.getRequestType());

          } else {
            info.addResult("inpletterNo", null);
            info.addResult("inpletterDate", null);
            info.addResult("inpdecisionNo", null);
            info.addResult("inpdecisionDate", null);
            info.addResult("inpReason", null);
          }
        } else if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          info.addResult("inpletterNo", null);
          info.addResult("inpletterDate", null);
          info.addResult("inpdecisionNo", null);
          info.addResult("inpdecisionDate", null);
          info.addResult("inpReason", null);
        }
      }

    } catch (Exception e) {
      log4j.error("Exception in HoldUnholdSalaryCallout ", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}