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
import org.openbravo.model.ad.system.Client;

import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmExtendService;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;
import sa.elm.ob.hcm.ad_callouts.dao.EmployeeExtendServiceCalloutDAO;
import sa.elm.ob.hcm.ad_callouts.dao.EmployeeExtendServiceCalloutDAOimpl;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * @author poongodi on 08/02/2018
 */
public class EmployeeExtendServiceCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpstartdate = vars.getStringParameter("inpeffectivedate");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String inpclient = vars.getStringParameter("inpadClientId");
    String effectiveDate = vars.getStringParameter("inpeffectivedate");
    String employmentInfoId = "";
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    String departmentId = null;
    Date efDate = null;
    DateFormat yearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    EmployeeExtendServiceCalloutDAO obj = new EmployeeExtendServiceCalloutDAOimpl();

    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    String inpauthorisedPerson = vars.getStringParameter("inpauthorisedPerson");
    try {
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();
      String effectiveDateConvert = UtilityDAO.convertToGregorian(effectiveDate);
      efDate = yearFormat.parse(effectiveDateConvert);
      EmploymentInfo empinfo = Utility.getActiveEmployInfo(employeeId);

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
            info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
          }
          if (empinfo.getStartDate() != null) {
            String query = " select eut_convert_to_hijri('"
                + yearFormat.format(empinfo.getStartDate()) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next()) {
              info.addResult("inpeffectivedate", rs.getString("eut_convert_to_hijri"));
              inpstartdate = rs.getString("eut_convert_to_hijri");
            }
          }

          inpdecisionType = "CR";
          info.addResult("inpdecisionType", inpdecisionType);
          info.addResult("inporiginalDecisionNo", "");

          departmentId = empinfo.getPosition().getDepartment().getId();
          JSONObject getauthoriztionInfoDetailsobj = obj.getAuthorizationInfoDetails(departmentId,
              efDate);
          if (getauthoriztionInfoDetailsobj.length() > 0) {
            info.addResult("inpauthorisedPerson",
                getauthoriztionInfoDetailsobj.getString("authorizedPerson"));
            info.addResult("inpauthorisesPersonJob",
                getauthoriztionInfoDetailsobj.getString("authorizedJobTitle"));
          } else {
            info.addResult("inpauthorisedPerson", "");
            info.addResult("inpauthorisesPersonJob", "");
          }
        } else {
          callouts.SetEmpDetailsNull(info);
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
        }
      }

      if (lastfieldChanged.equals("inpdecisionType")
          || lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (!inpdecisionType.equals("CR")) {
          OBQuery<EhcmExtendService> objEmpQuery = OBDal.getInstance().createQuery(
              EhcmExtendService.class,
              "as e where e.employee.id=:employeeId and e.enabled='Y' and issueDecision='Y' order by e.creationDate desc");
          objEmpQuery.setNamedParameter("employeeId", employeeId);
          objEmpQuery.setMaxResult(1);
          List<EhcmExtendService> extendList = objEmpQuery.list();
          if (extendList.size() > 0) {
            info.addResult("inporiginalDecisionNo", extendList.get(0).getId());
            info.addResult("inpdecisionType", inpdecisionType);
          } else {
            info.addResult("JSEXECUTE",
                "form.getFieldFromColumnName('Original_Decision_No').setValue('')");
          }

        }

      }
      if (lastfieldChanged.equals("inpauthorisedPerson")) {
        // get active employment info
        EmploymentInfo emplyinfo = Utility.getActiveEmployInfo(inpauthorisedPerson);
        if (emplyinfo != null && emplyinfo.getPosition() != null) {
          info.addResult("inpauthorisesPersonJob", emplyinfo.getPosition().getId());
        }
      }
      if (StringUtils.isNotEmpty(employeeId)) {
        departmentId = empinfo.getPosition().getDepartment().getId();
        efDate = yearFormat.parse(effectiveDateConvert);
        JSONObject getauthoriztionInfoDetailsobj = obj.getAuthorizationInfoDetails(departmentId,
            efDate);
        if (lastfieldChanged.equals("inpeffectivedate")) {
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

      } else {
        callouts.SetEmpDetailsNull(info);
      }

      // To get max extension allowed from client window.
      Client client = OBDal.getInstance().get(Client.class, inpclient);
      info.addResult("inpextendPeriod", client.getEhcmMaxextperiod());

    } catch (

    Exception e) {
      log4j.error("Exception in EmpExtendService Callout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
