package sa.elm.ob.hcm.ad_process.EmployeePromotion;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
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
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.hcm.util.Utility;

/**
 * @author Sowmiya on 13/08/2018
 */

public class EmployeePromotionReactivate extends DalBaseProcess {

  private static final Logger log = LoggerFactory.getLogger(EmployeePromotionReactivate.class);
  AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();

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
      EHCMEmpPromotion currectPromotion = null;
      currectPromotion = EmpPromotion;
      // Delete the record in Employee Detail window
      if ((EmpPromotion.getDecisionType()).equals("UP")) {
        decisionType = "UP";
        EmpPromotion = EmpPromotion.getOriginalDecisionsNo();

        EmploymentInfo info = Utility.getActiveEmployInfo(EmpPromotion.getEhcmEmpPerinfo().getId());
        EmployeePromotionHandlerDAO.insertEmploymentInfo(EmpPromotion, info, vars, decisionType,
            lang, null, null, true);
        assingedOrReleaseEmpInPositionDAO.updateEmpPositionWhileReactive(currectPromotion, null,
            null, vars, false);
        EmployeePromotionHandlerDAO.updateEnddateinEmpInfo(EmpPromotion, info, vars);

      } else if ((EmpPromotion.getDecisionType()).equals("CA")) {
        decisionType = "CR";
        EmpPromotion = EmpPromotion.getOriginalDecisionsNo();
        EmploymentInfo info = Utility.getActiveEmployInfo(EmpPromotion.getEhcmEmpPerinfo().getId());
        EmployeePromotionHandlerDAO.insertEmploymentInfo(EmpPromotion, info, vars, decisionType,
            lang, null, null, true);
        assingedOrReleaseEmpInPositionDAO.updateEmpPositionWhileReactive(currectPromotion, null,
            null, vars, true);
        EmployeePromotionHandlerDAO.updateEnddateinEmpInfo(EmpPromotion, info, vars);

      } else if (EmpPromotion.getDecisionType().equals("CR")) {
        EmployeePromotionHandlerDAO.CancelinPromotion(EmpPromotion, vars);
      }
      EmpPromotion = OBDal.getInstance().get(EHCMEmpPromotion.class, empPromotionId);

      EmpPromotion.setDecisionStatus("UP");
      EmpPromotion.setReactivate(false);
      EmpPromotion.setSueDecision(false);
      EmpPromotion.setDecisionDate(null);
      // ExtendServiceHandlerDAO.updateEmpRecord(EmpPromotion.getEhcmEmpPerinfo().getId(),);
      // EmpPromotion.getEhcmEmpPerinfo().setEmploymentStatus("SE");

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
