package sa.elm.ob.hcm.ad_process.disciplinaryAction;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EhcmDisciplineAction;

/**
 * 
 * @author Gokul 03/08/18
 *
 */

public class DisciplinaryActionReactivateProcess implements Process {
  private static final Logger log4j = LoggerFactory
      .getLogger(DisciplinaryActionReactivateProcess.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String lang = vars.getLanguage();
    final String disciplinaryActionId = bundle.getParams().get("Ehcm_Discipline_Action_ID")
        .toString();
    EhcmDisciplineAction disciplinaryObj = OBDal.getInstance().get(EhcmDisciplineAction.class,
        disciplinaryActionId);

    try {
      OBContext.setAdminMode();
      // check Issued or not
      if ((disciplinaryObj.getPayrollProcessLine() != null)) {
        if (!(disciplinaryObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
            .equals("UP")
            || disciplinaryObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
                .equals("IC")
            || disciplinaryObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
                .equals("DR"))) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_payroll_processed"));
          bundle.setResult(obError);
          return;
        }
      }

      if (disciplinaryObj.isSueDecision()) {
        // update status as UnderProcessing and set decision date for all cases
        disciplinaryObj.setSueDecision(false);
        disciplinaryObj.setDecisionDate(null);
        disciplinaryObj.setDecisionStatus("UP");
        OBDal.getInstance().save(disciplinaryObj);
        OBDal.getInstance().flush();

        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_ExtraStep_Reactivate_Process"));
        bundle.setResult(obError);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();

      }

    }

    catch (Exception e) {
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      Throwable t = DbUtility.getUnderlyingSQLException(e);
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}