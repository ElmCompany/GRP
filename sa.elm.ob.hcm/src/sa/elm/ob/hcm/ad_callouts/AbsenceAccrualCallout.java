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

import sa.elm.ob.hcm.EHCMAbsenceAccrual;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.utility.util.UtilityDAO;

public class AbsenceAccrualCallout extends SimpleCallout {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String absenceAccuralId = vars.getStringParameter("inpehcmAbsenceAccrualId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String employmentInfoId = "";
    String startDate = "";

    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    try {
      /* get Absence Acccrual information */
      EHCMAbsenceAccrual accrual = OBDal.getInstance().get(EHCMAbsenceAccrual.class,
          absenceAccuralId);
      /*
       * get Latest active EmploymentInfo by using EmployeeId and set the value based on Employment
       * Info
       */
      EmploymentInfo empinfo = null;
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + employeeId + "' and enabled='Y' order by creationDate desc ");
      log4j.debug("employeeId:" + employeeId);
      log4j.debug("positiontype:" + empInfo.list().size());
      if (empInfo.list().size() > 0) {
        empinfo = empInfo.list().get(0);
      }
      if (lastfieldChanged.equals("inpehcmEmpPerinfoId") && StringUtils.isNotEmpty(employeeId)) {

        /* get Employee Details by using employeeId */
        EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
        info.addResult("inpempName", employee.getArabicfullname());

        if (employee.isEnabled())
          info.addResult("inpempStatus", "ACT");
        else
          info.addResult("inpempStatus", "INACT");

        info.addResult("inpehcmGradeclassId", employee.getGradeClass().getId());
        info.addResult("inpempType", employee.getEhcmActiontype().getPersonType());
        if (employee.getHiredate() != null) {
          info.addResult("inphireDate",
              (UtilityDAO.convertTohijriDate(dateFormat.format(employee.getHiredate()))));
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
          info.addResult("inpehcmGradestepsId",
              empinfo.getEhcmPayscale().getEhcmGradesteps().getId());
          info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());

          if (empinfo.getStartDate() != null) {
            startDate = UtilityDAO.convertTohijriDate(dateFormat.format(empinfo.getStartDate()));
            info.addResult("inpstartdate", startDate);
          }
        }

      } else {
        info.addResult("inpempName", "");
        info.addResult("inpempStatus", "");
        info.addResult("inpempType", "");
        info.addResult("inphireDate", "");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Department_ID').setValue('')");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Ehcm_Grade_ID').setValue('')");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Ehcm_Position_ID').setValue('')");
        info.addResult("inpjobTitle", "");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Employmentgrade').setValue('')");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
        info.addResult("JSEXECUTE",
            "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
        info.addResult("JSEXECUTE",
            "form.getFieldFromColumnName('Ehcm_Gradeclass_ID').setValue('')");
      }
    } catch (Exception e) {
      log4j.error("Exception in AbsenceAccrualCallout Callout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
