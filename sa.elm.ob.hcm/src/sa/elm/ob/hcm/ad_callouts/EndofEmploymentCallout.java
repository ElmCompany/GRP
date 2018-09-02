package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EHCMTerminationReason;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.dao.EndofEmploymentCalloutDAO;
import sa.elm.ob.hcm.ad_callouts.dao.EndofEmploymentCalloutDAOImpl;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.utility.util.UtilityDAO;

public class EndofEmploymentCallout extends SimpleCallout {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String inpterminationdate = vars.getStringParameter("inpterminationDate");
    String inpehcmAuthorizePersonId = vars.getStringParameter("inpehcmAuthorizePersonId");
    String departmentCode = vars.getStringParameter("inpdepartmentId");
    String departmentId = null;
    Date terminationDate = null;
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    log4j.debug("lastfieldChanged:" + lastfieldChanged);
    String employmentInfoId = "";
    String cancelDate = null;

    EndofEmploymentCalloutDAO endofemploymentobj = new EndofEmploymentCalloutDAOImpl();
    try { /*
           * get Latest active EmploymentInfo by using EmployeeId and set the value based on
           * Employment Info
           */
      EmploymentInfo empinfo = null;
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + employeeId + "' and enabled='Y'  order by creationDate desc ");
      log4j.debug("employeeId:" + employeeId);
      log4j.debug("positiontype:" + empInfo.list().size());
      if (empInfo.list().size() > 0) {
        empinfo = empInfo.list().get(0);
        log4j.debug("getChangereason:" + empinfo.getChangereason());
        log4j.debug("getChangereasoninfo:" + empinfo.getChangereasoninfo());
      }
      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {

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
        log4j.debug("info:" + employee.getEhcmActiontype().getPersonType());
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
          log4j.debug("employmentInfoId:" + employmentInfoId);
          info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
          if (empinfo.getPosition().getSection() != null) {
            info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
          } else {
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
          }
          info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
          info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
          log4j.debug("inpehcmPositionId:" + empinfo.getPosition().getJOBNo());
          info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
          /*
           * if (empinfo.getChangereason().equals("T")) info.addResult("inporiginalDecisionNo",
           * empinfo.getDecisionNo()); else info.addResult("inporiginalDecisionNo", "");
           */
          info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
          info.addResult("inpassignedDept", empinfo.getSECDeptName());
          info.addResult("inpehcmGradestepsId",
              empinfo.getEhcmPayscale().getEhcmGradesteps().getId());
          info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
          log4j.debug("inpehcmPayscalelineId:" + empinfo.getEhcmPayscaleline().getId());
          if (empinfo.getStartDate() != null) {
            String query = " select eut_convert_to_hijri('"
                + dateFormat.format(empinfo.getStartDate()) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next()) {
              inpterminationdate = rs.getString("eut_convert_to_hijri");
              log4j.debug("inpterminationdate:" + inpterminationdate);
              info.addResult("inpterminationDate", rs.getString("eut_convert_to_hijri"));
            }
          }
          departmentId = empinfo.getPosition().getDepartment().getId();
          terminationDate = empinfo.getStartDate();

          JSONObject authorizationInfoObj = endofemploymentobj
              .getAuthorizationInfoDetails(departmentId, terminationDate);
          if (authorizationInfoObj != null && authorizationInfoObj.length() > 0) {
            info.addResult("inpehcmAuthorizePersonId",
                authorizationInfoObj.getString("authorizedPerson"));
            info.addResult("inpauthorizePersonTitle",
                authorizationInfoObj.getString("authorizedJobTitle"));
          } else {
            info.addResult("inpehcmAuthorizePersonId", "");
            info.addResult("inpauthorizePersonTitle", "");
          }
        }
        inpdecisionType = DecisionTypeConstants.DECISION_TYPE_CREATE;
        info.addResult("inpdecisionType", inpdecisionType);
        info.addResult("inporiginalDecisionsNo", null);
        info.addResult("inpcanceldate", null);

      }
      if (lastfieldChanged.equals("inpdecisionType")) {
        if (!inpdecisionType.equals("CR")) {
          if (empinfo.getStartDate() != null) {
            String query = " select eut_convert_to_hijri('"
                + dateFormat.format(empinfo.getStartDate()) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next()) {
              inpterminationdate = rs.getString("eut_convert_to_hijri");
              log4j.debug("inpterminationdate:" + inpterminationdate);
              info.addResult("inpterminationDate", rs.getString("eut_convert_to_hijri"));
            }
          }
        }
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          cancelDate = UtilityDAO.convertTohijriDate(dateFormat.format(new Date()));
          info.addResult("inpcanceldate", cancelDate);
        }
        if (inpdecisionType.equals("CA") || inpdecisionType.equals("UP")) {
          OBQuery<EHCMTerminationReason> termreason = OBDal.getInstance().createQuery(
              EHCMTerminationReason.class, " searchKey='" + empinfo.getChangereasoninfo() + "'");
          if (termreason.list().size() > 0) {
            EHCMTerminationReason termination = termreason.list().get(0);
            log4j.debug(" getId:" + termination.getId());
            info.addResult("inpehcmTerminationReasonId", termination.getId());

          }
          if (empinfo.getChangereason().equals("T")) {
            OBQuery<EHCMEMPTermination> objEmpQuery = OBDal.getInstance()
                .createQuery(EHCMEMPTermination.class, "as e where e.ehcmEmpPerinfo.id='"
                    + employeeId + "' and e.enabled='Y'  order by e.creationDate desc");
            objEmpQuery.setMaxResult(1);
            if (objEmpQuery.list().size() > 0) {
              EHCMEMPTermination termination = objEmpQuery.list().get(0);
              log4j.debug("getDecisionNo():" + termination.getId());
              info.addResult("inporiginalDecisionsNo", termination.getId());
            }
          }

          OBQuery<EHCMEMPTermination> termination = OBDal.getInstance()
              .createQuery(EHCMEMPTermination.class, " ehcmEmpPerinfo.id='" + employeeId
                  + "' and enabled='Y' order by creationDate desc ");
          termination.setMaxResult(1);
          if (termination.list().size() > 0) {
            EHCMEMPTermination term = termination.list().get(0);
            // info.addResult("inpehcmAuthorizePersonId", term.getEhcmAuthorizePerson());
            info.addResult("inppaymentPeriod", term.getPaymentPeriod());
            // info.addResult("inpauthorizePersonTitle", term.getAuthorizePersonTitle());
          }
        }
      }

      /*
       * if (lastfieldChanged.equals("inpehcmAuthorizePersonId")) { // get active employment info
       * EmploymentInfo emplyinfo = Utility.getActiveEmployInfo(inpehcmAuthorizePersonId); if
       * (emplyinfo != null && emplyinfo.getPosition() != null) {
       * info.addResult("inpauthorizePersonTitle", emplyinfo.getPosition().getId()); } }
       */
      // termination_date change
      if (lastfieldChanged.equals("inpterminationDate")) {
        String strGregterminateDate = UtilityDAO.convertToGregorian(inpterminationdate);
        Date termntnDate = dateFormat.parse(strGregterminateDate);
        log4j.debug("Date after conversion:" + termntnDate);
        JSONObject authorizationInfoObj1 = endofemploymentobj
            .getAuthorizationInfoDetails(departmentCode, termntnDate);
        if (authorizationInfoObj1 != null && authorizationInfoObj1.length() > 0) {
          info.addResult("inpehcmAuthorizePersonId",
              authorizationInfoObj1.getString("authorizedPerson"));
          info.addResult("inpauthorizePersonTitle",
              authorizationInfoObj1.getString("authorizedJobTitle"));
        } else {
          info.addResult("inpehcmAuthorizePersonId", "");
          info.addResult("inpauthorizePersonTitle", "");
        }

      }

    } catch (Exception e) {
      log4j.error("Exception in EmpSecondmentCallout Callout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
