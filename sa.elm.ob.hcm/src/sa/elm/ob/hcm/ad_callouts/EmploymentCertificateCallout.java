package sa.elm.ob.hcm.ad_callouts;

import java.text.DateFormat;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author Gokul 2/07/18
 *
 */
public class EmploymentCertificateCallout extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String requestDate = vars.getStringParameter("inprequestDate");
    String requestDatepar = UtilityDAO.convertToGregorian(requestDate);
    DateFormat dateFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    log4j.debug("lastfiled:" + lastfieldChanged);
    try {
      EmploymentInfo empinfo = null;
      EmploymentInfo empreqinfo = null;

      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
        empinfo = Utility.getActiveEmployInfo(employeeId);
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
              (UtilityDAO.convertTohijriDate(dateFormat.format(employee.getHiredate()))));
        }
        if (empinfo != null) {
          info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
          if (empinfo.getPosition() != null && empinfo.getPosition().getSection() != null) {
            info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
          }
          info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
          info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
          info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
          info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
          info.addResult("inpassignedDept", empinfo.getSECDeptName());
          info.addResult("inpehcmGradestepsId",
              empinfo.getEhcmPayscale().getEhcmGradesteps().getId());
          info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
          info.addResult("inporiginalDecisionsNo", "");
        }
      }
      if (lastfieldChanged.equals("inprequestDate")) {
        empreqinfo = Utility.getRequestedEmployeeInfo(employeeId, requestDatepar);
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
              (UtilityDAO.convertTohijriDate(dateFormat.format(employee.getHiredate()))));
        }
        if (empreqinfo != null) {
          info.addResult("inpdepartmentId", empreqinfo.getPosition().getDepartment().getId());
          if (empreqinfo.getPosition() != null && empreqinfo.getPosition().getSection() != null) {
            info.addResult("inpsectionId", empreqinfo.getPosition().getSection().getId());
          }
          info.addResult("inpehcmGradeId", empreqinfo.getGrade().getId());
          info.addResult("inpehcmPositionId", empreqinfo.getPosition().getId());
          info.addResult("inpjobTitle", empreqinfo.getPosition().getJOBName().getJOBTitle());
          info.addResult("inpemploymentgrade", empreqinfo.getEmploymentgrade().getId());
          info.addResult("inpassignedDept", empreqinfo.getSECDeptName());
          info.addResult("inpehcmGradestepsId",
              empreqinfo.getEhcmPayscale().getEhcmGradesteps().getId());
          info.addResult("inpehcmPayscalelineId", empreqinfo.getEhcmPayscaleline().getId());
          info.addResult("inporiginalDecisionsNo", "");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in EmployeeCertificateCallout Callout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }

}
