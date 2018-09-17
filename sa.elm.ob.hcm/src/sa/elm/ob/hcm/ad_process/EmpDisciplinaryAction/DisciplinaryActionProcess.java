package sa.elm.ob.hcm.ad_process.EmpDisciplinaryAction;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
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
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;

/**
 * 
 * @author poongodi on 04-05-2018
 *
 */

public class DisciplinaryActionProcess implements Process {
  private static final Logger log4j = LoggerFactory.getLogger(DisciplinaryActionProcess.class);
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
      // check whether the employee is suspended or not
      if (disciplinaryObj.getEmployee().getEmploymentStatus()
          .equals(DecisionTypeConstants.EMPLOYMENTSTATUS_SUSPENDED)) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_emplo_suspend"));
        bundle.setResult(obError);
        return;
      }

      // checking decision overlap
      if (disciplinaryObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          || (disciplinaryObj.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))) {
        JSONObject result = Utility.chkDecisionOverlap(Constants.DISCLIPLINE_OVERLAP,
            sa.elm.ob.utility.util.Utility.formatDate(disciplinaryObj.getStartDate()),
            sa.elm.ob.utility.util.Utility.formatDate(disciplinaryObj.getEndDate()),
            disciplinaryObj.getEmployee().getId(), disciplinaryObj.getDisciplineReason().getId(),
            disciplinaryObj.getId());

        if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
          if (disciplinaryObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
              || (disciplinaryObj.getDecisionType()
                  .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                  && (result.has("discliplineId") && !result.getString("discliplineId")
                      .equals(disciplinaryObj.getOriginalDecisionNo().getId()))
                  || !result.has("discliplineId"))) {
            if (result.has("errormsg")) {
              obError.setType("Error");
              obError.setTitle("Error");
              obError.setMessage(OBMessageUtils.messageBD(result.getString("errormsg")));
              bundle.setResult(obError);
              return;
            }
          }
        }
      }

      // check Issued or not
      if (!disciplinaryObj.isSueDecision()) {
        // update status as Issued and set decision date for all cases
        disciplinaryObj.setSueDecision(true);
        disciplinaryObj.setDecisionDate(new Date());
        disciplinaryObj.setDecisionStatus("I");
        OBDal.getInstance().save(disciplinaryObj);
        OBDal.getInstance().flush();

        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_ExtraStep_Process"));
        bundle.setResult(obError);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();

      }

    }

    catch (

    Exception e) {
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