package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.EhcmPosTransactionType;
import sa.elm.ob.hcm.EhcmPosition;

@SuppressWarnings("serial")
public class UpdatePositionCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String inpdepartment = vars.getStringParameter("inpnewDepartmentId");
    String inpsection = vars.getStringParameter("inpnewSectionId");
    String inpjobPositon = vars.getStringParameter("inpehcmPositionId");
    String inpjobsId = vars.getStringParameter("inpehcmJobsId");
    String inpnewjobsId = vars.getStringParameter("inpnewEhcmJobsId");
    String inpnewJobName = vars.getStringParameter("inpnewJobName");
    String inpehcmGradeId = vars.getStringParameter("inpehcmGradeId");
    String inpjobNo = vars.getStringParameter("inpjobNo");
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpehcmPostransactiontypeId = vars.getStringParameter("inpehcmPostransactiontypeId");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    log4j.debug("lastfield:" + inpLastFieldChanged);
    try {
      if (inpLastFieldChanged.equals("inpnewDepartmentId")) {
        if (inpdepartment.isEmpty()) {
          info.addResult("inpnewDeptname", "");
        } else {
          Organization dept = OBDal.getInstance().get(Organization.class, inpdepartment);
          info.addResult("inpnewDeptname", dept.getName());
        }
      }
      if (inpLastFieldChanged.equals("inpnewSectionId")) {
        if (inpsection.isEmpty()) {
          info.addResult("inpnewSectionname", "");
        } else {
          Organization sect = OBDal.getInstance().get(Organization.class, inpsection);
          info.addResult("inpnewSectionname", sect.getName());
        }
      }

      if (inpLastFieldChanged.equals("inpjobNo")) {
        OBQuery<EhcmPosition> position = OBDal.getInstance().createQuery(EhcmPosition.class,
            " id='" + inpjobNo + "' and grade.id='" + inpehcmGradeId + "'");
        log4j.debug("posi:" + position.getWhereAndOrderBy());
        log4j.debug("list:" + position.list().size());
        if (position.list().size() > 0) {
          EhcmPosition pos = position.list().get(0);
          log4j.debug("getid:" + pos.getId());
          info.addResult("inpehcmPositionId", pos.getId());

          info.addResult("inpdepartmentId", pos.getDepartment().getId());
          if (pos.getSection() != null) {
            info.addResult("inpsectionId", pos.getSection().getId());
            info.addResult("inpsectionname", pos.getSectionname());
          } else {
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
            info.addResult("inpsectionname", null);
          }
          info.addResult("inpdeptname", pos.getDeptname());
          // info.addResult("inpsectionname", pos.getSectionname());
          info.addResult("inpehcmJobsId", pos.getEhcmJobs().getId());
          info.addResult("inpjobName", pos.getEhcmJobs().getId());
        } else {
          info.addResult("inpehcmPositionId", null);
          info.addResult("inpdepartmentId", null);
          info.addResult("inpsectionId", null);
          info.addResult("inpdeptname", null);
          info.addResult("inpsectionname", null);
          info.addResult("inpehcmJobsId", null);
          info.addResult("inpjobName", null);
        }

      }
      if (inpLastFieldChanged.equals("inpehcmGradeId")) {
        OBQuery<EhcmPosition> position = OBDal.getInstance().createQuery(EhcmPosition.class,
            " id='" + inpjobNo + "' and grade.id='" + inpehcmGradeId + "'");
        log4j.debug("posi:" + position.getWhereAndOrderBy());
        log4j.info("list:" + position.list().size());
        if (position.list().size() > 0) {
          EhcmPosition pos = position.list().get(0);
          log4j.debug("getid:" + pos.getId());
          info.addResult("inpehcmPositionId", pos.getId());

          info.addResult("inpdepartmentId", pos.getDepartment().getId());
          if (pos.getSection() != null) {
            info.addResult("inpsectionId", pos.getSection().getId());
            info.addResult("inpsectionname", pos.getSectionname());
          }
          info.addResult("inpdeptname", pos.getDeptname());
          // info.addResult("inpsectionname", pos.getSectionname());
          info.addResult("inpehcmJobsId", pos.getEhcmJobs().getId());
          info.addResult("inpjobName", pos.getEhcmJobs().getId());
          log4j.info("inpjobName>>" + pos.getEhcmJobs().getId());
        } else {
          info.addResult("inpehcmPositionId", null);
          info.addResult("inpdepartmentId", null);
          info.addResult("inpsectionId", null);
          info.addResult("inpdeptname", null);
          info.addResult("inpsectionname", null);
          info.addResult("inpehcmJobsId", null);
          info.addResult("inpjobName", null);
        }

      }
      if (inpLastFieldChanged.equals("inpnewEhcmJobsId")) {

        if (inpnewjobsId.isEmpty()) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').setValue('')");
        } else {
          info.addResult("inpnewJobName", inpnewjobsId);
        }
      }
      if (inpLastFieldChanged.equals("inpnewJobName")) {
        if (inpnewJobName.isEmpty()) {
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').setValue('')");
        } else {
          info.addResult("inpnewEhcmJobsId", inpnewJobName);
        }

      }
      if (inpLastFieldChanged.equals("inpehcmPostransactiontypeId")) {
        EhcmPosTransactionType posTranType = OBDal.getInstance().get(EhcmPosTransactionType.class,
            inpehcmPostransactiontypeId);
        info.addResult("JSEXECUTE",
            "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').setValue('')");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').setValue('')");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').setValue('')");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').setValue('')");
        info.addResult("JSEXECUTE",
            "form.getFieldFromColumnName('NEW_Department_ID').setValue('')");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').setValue('')");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').setValue('')");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').setValue('')");

        if (posTranType.getSearchKey().equals("UGPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').enable()");

          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').disable()");

        } else if (posTranType.getSearchKey().equals("UGREPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').enable()");

          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').disable()");

        } else if (posTranType.getSearchKey().equals("UGFRPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').enable()");

          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').disable()");

        } else if (posTranType.getSearchKey().equals("DGPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').enable()");

          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').disable()");

        } else if (posTranType.getSearchKey().equals("DGREPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').enable()");

          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').disable()");

        } else if (posTranType.getSearchKey().equals("DGFRPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').enable()");

          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').disable()");

        } else if (posTranType.getSearchKey().equals("TRPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').enable()");

          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').disable()");

        } else if (posTranType.getSearchKey().equals("RCPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').enable()");

          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').disable()");

        } else if (posTranType.getSearchKey().equals("RCTRPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').enable()");

          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').disable()");

        } else if (posTranType.getSearchKey().equals("RCFRPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').enable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').enable()");

          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').disable()");

        } else if (posTranType.getSearchKey().equals("FRPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').disable()");

        } else if (posTranType.getSearchKey().equals("HOPO")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Grade_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_No').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Ehcm_Jobs_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Job_Name').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Department_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Deptname').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').disable()");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Sectionname').disable()");

        }
      }

    } catch (Exception e) {
      log4j.error("Exception in UpdatePositionCallout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
