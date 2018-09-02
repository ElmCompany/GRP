package sa.elm.ob.hcm.ad_process.EmpExtraStep;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DbUtility;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmEmployeeExtraStep;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.ad_process.EmpExtraStep.DAO.ExtraStepHandlerDAO;
import sa.elm.ob.scm.EscmProposalAttribute;

/**
 * @author poongodi on 03/02/2018
 */
public class ExtraStepDecisionProcess implements Process {
  private static final Logger log = Logger.getLogger(ExtraStepDecisionProcess.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the EmpDecisionProcess");

    final String extraStepId = bundle.getParams().get("Ehcm_Emp_Extrastep_ID")
        .toString();
    EhcmEmployeeExtraStep extraStepProcess = OBDal.getInstance().get(EhcmEmployeeExtraStep.class, extraStepId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    int count = 0;
    try {
    
      OBContext.setAdminMode(true);
      EhcmEmployeeExtraStep decisionProcess = OBDal.getInstance().get(EhcmEmployeeExtraStep.class, extraStepId);
      //If the employee already reached the grade step,then throw the error.
      if(decisionProcess.getDecisionType().equals("CR"))
        {
        if(decisionProcess.getEhcmPayscaleline().getId() == decisionProcess.getNewgradepoint().getId())
        {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_Empgrade_Exist"));
          bundle.setResult(obError);
          return;
        }
        }
      // check Issued or not
      if (!extraStepProcess.isSueDecision()) {
        // update status as Issued and set decision date for all cases
        extraStepProcess.setSueDecision(true);
        extraStepProcess.setDecisionDate(new Date());
        extraStepProcess.setDecisionStatus("I");
        OBDal.getInstance().save(extraStepProcess);
        OBDal.getInstance().flush();
        
        count = ExtraStepHandlerDAO.insertLineinEmploymentInfo(extraStepProcess,vars);

        if(count == 1)
        {
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_ExtraStep_Process"));
        bundle.setResult(obError);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
        }
      }
    } catch (Exception e) {
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
