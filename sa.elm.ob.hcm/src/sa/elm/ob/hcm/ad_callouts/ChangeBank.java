package sa.elm.ob.hcm.ad_callouts;

import java.text.DateFormat;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.finance.EfinBank;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmChangeBankV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.hcm.util.UtilityDAO;

/**
 * 
 * @author Gokul 09/07/18
 *
 */
public class ChangeBank extends SimpleCallout {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub

    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String bankId = vars.getStringParameter("inpexistingBank");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String bankCode = vars.getStringParameter("inpbankCode");
    DateFormat dateFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    log4j.debug("lastfiled:" + lastfieldChanged);
    try {
      EmploymentInfo empinfo = null;
      empinfo = Utility.getActiveEmployInfo(employeeId);
      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
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
        }
      }
      if (lastfieldChanged.equals("inpexistingBank")) {
        EhcmChangeBankV bank = OBDal.getInstance().get(EhcmChangeBankV.class, bankId);
        if (bank != null) {
          info.addResult("inpstartDate", bank.getStartDate());
          info.addResult("inppercentage", bank.getPercentage());
        }
      }
      if (lastfieldChanged.equals("inpbankCode")) {
        EfinBank bankcode = OBDal.getInstance().get(EfinBank.class, bankCode);
        info.addResult("inpbankName", bankcode.getBankname());
      }

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in ChangeBankProcessCallout ", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }

}
