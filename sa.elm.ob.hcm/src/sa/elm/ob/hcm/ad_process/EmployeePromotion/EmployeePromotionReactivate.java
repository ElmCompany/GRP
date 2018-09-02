package sa.elm.ob.hcm.ad_process.EmployeePromotion;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEmpPromotion;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.util.Utility;

/**
 * @author Sowmiya on 13/08/2018
 */

public class EmployeePromotionReactivate extends DalBaseProcess {

  private static final Logger log = LoggerFactory.getLogger(EmployeePromotionReactivate.class);

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Entering into Employee Promotion Reactivate");

    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement ps = null;
    PreparedStatement preStmt = null;
    String lang = vars.getLanguage();
    String decisionType = "";

    try {
      OBContext.setAdminMode();
      String empPromotionId = (String) bundle.getParams().get("Ehcm_Emp_Promotion_ID");
      EHCMEmpPromotion EmpPromotion = OBDal.getInstance().get(EHCMEmpPromotion.class,
          empPromotionId);
      String empPerInfoId = EmpPromotion.getEhcmEmpPerinfo().getId();
      log.debug(empPerInfoId);

      OBQuery<EmploymentInfo> empInfoq = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id =:empPerInfoId and (e.ehcmEmpPromotion.id !=:empPromoId or e.ehcmEmpPromotion.id is null)"
              + "order by created desc");
      empInfoq.setNamedParameter("empPerInfoId", empPerInfoId);
      empInfoq.setNamedParameter("empPromoId", empPromotionId);
      log.debug(empPerInfoId);
      log.debug(empPromotionId);
      EmploymentInfo empInfo = empInfoq.list().get(0);

      log.debug((EmpPromotion.getDecisionType()));

      // Delete the record in Employee Detail window
      if ((EmpPromotion.getDecisionType()).equals("UP")) {

        decisionType = "UP";
        EmpPromotion = EmpPromotion.getOriginalDecisionsNo();
        EmploymentInfo info = Utility.getActiveEmployInfo(EmpPromotion.getEhcmEmpPerinfo().getId());
        EmployeePromotionHandlerDAO.insertEmploymentInfo(EmpPromotion, info, vars, decisionType,
            lang, null, null);
        EmployeePromotionHandlerDAO.updateEnddateinEmpInfo(EmpPromotion, info, vars);
      } else if ((EmpPromotion.getDecisionType()).equals("CA")) {

        decisionType = "CR";
        EmpPromotion = EmpPromotion.getOriginalDecisionsNo();
        EmploymentInfo info = Utility.getActiveEmployInfo(EmpPromotion.getEhcmEmpPerinfo().getId());
        EmployeePromotionHandlerDAO.insertEmploymentInfo(EmpPromotion, info, vars, decisionType,
            lang, null, null);
        EmployeePromotionHandlerDAO.updateEnddateinEmpInfo(EmpPromotion, info, vars);
      } else {

        ps = conn
            .prepareStatement("delete from ehcm_employment_info where ehcm_emp_promotion_id = ?");
        ps.setString(1, empPromotionId);
        ps.executeUpdate();

        if (empInfo.getEhcmEmpSecondment() != null) {
          empInfo.setEndDate(empInfo.getEhcmEmpSecondment().getEndDate());
        } else if (empInfo.getEhcmEmpTransfer() != null) {
          empInfo.setEndDate(empInfo.getEhcmEmpTransfer().getEndDate());
        } else if (empInfo.getEhcmEmpTransferSelf() != null) {
          empInfo.setEndDate(empInfo.getEhcmEmpTransferSelf().getEndDate());
        } else {
          empInfo.setEndDate(null);
        }
        empInfo.setEnabled(true);
        empInfo.setAlertStatus("ACT");
      }
      ps = conn.prepareStatement("delete from ehcm_posemp_history where ehcm_emp_promotion_id = ?");
      ps.setString(1, empPromotionId);
      ps.executeUpdate();

      EmpPromotion = OBDal.getInstance().get(EHCMEmpPromotion.class, empPromotionId);

      EmpPromotion.setDecisionStatus("UP");
      EmpPromotion.setReactivate(false);
      EmpPromotion.setSueDecision(false);
      EmpPromotion.setDecisionDate(null);

      OBDal.getInstance().save(EmpPromotion);
      OBError result = OBErrorBuilder.buildMessage(null, "success", "@ProcessOK@");
      bundle.setResult(result);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      e.printStackTrace();
      log.debug("Exeception in EmployeePromotionReactivate:" + e);
      OBDal.getInstance().rollbackAndClose();
      Throwable t = DbUtility.getUnderlyingSQLException(e);
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), t.getMessage());
      bundle.setResult(error);
    } finally {

      OBContext.restorePreviousMode();
    }
  }

}
