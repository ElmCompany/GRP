package sa.elm.ob.hcm.ad_process;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.EhcmPosition;

public class IssueDecision implements Process {
  private static final Logger log = Logger.getLogger(IssueDecision.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the position");

    final String positionId = (String) bundle.getParams().get("Ehcm_Position_ID").toString();
    EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class, positionId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    log.debug("position:" + positionId);
    try {
      OBContext.setAdminMode(true);
      if (!position.isSued()) {
        position.setSued(true);
        position.setDecisionDate(new Date());
        position.setTransactionStatus("I");
        OBDal.getInstance().save(position);

        /*
         * EhcmPositionHistory posHistory = OBProvider.getInstance().get(EhcmPositionHistory.class);
         * posHistory.setClient(position.getClient());
         * posHistory.setOrganization(position.getOrganization());
         * posHistory.setActive(position.isActive()); posHistory.setCreationDate(new
         * java.util.Date()); posHistory.setCreatedBy(OBDal.getInstance().get(User.class,
         * vars.getUser())); posHistory.setUpdated(new java.util.Date());
         * posHistory.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
         * posHistory.setDepartmentCode(position.getDepartment());
         * posHistory.setDepartmentName(position.getDeptname());
         * posHistory.setSectionCode(position.getSection());
         * posHistory.setSectionName(position.getSectionname());
         * posHistory.setStartDate(position.getStartDate());
         * posHistory.setEndDate(position.getEndDate());
         * posHistory.setEhcmJobClassification(position.getEhcmJobClassification());
         * posHistory.setMCSLetterDate(position.getMCSLetterDate());
         * posHistory.setDecisionNo(position.getDecisionNo());
         * posHistory.setDecisionDate(position.getDecisionDate());
         * posHistory.setTransactionType(position.getEhcmPostransactiontype());
         * posHistory.setTransactionStatus(position.getTransactionStatus());
         * posHistory.setTransactionDescription(position.getTransactionDesc());
         * posHistory.setGrade(position.getEhcmJobs());
         * posHistory.setJobCode(position.getJOBCode()); posHistory.setJobNo(position.getJOBNo());
         * posHistory.setJobTitle(position.getJOBName());
         * posHistory.setMainGroupCode(position.getMainGroupCode());
         * posHistory.setMainGroupName(position.getMainGroupName());
         * posHistory.setGroupSequenceCode(position.getGroupSeqCode());
         * posHistory.setGroupSequenceName(position.getGroupSeqName());
         * posHistory.setSubGroupCode(position.getSUBGroupCode());
         * posHistory.setSubGroupName(position.getSUBGroupName());
         * posHistory.setYear(position.getYear());
         * posHistory.setBudgetDate(position.getBudgetDate());
         * posHistory.setBudgetDate(position.getBudgetDate());
         * posHistory.setMOFDecisionDate(position.getMOFDecisionDate());
         * posHistory.setMOFDecisionNo(position.getMOFDecisionNo());
         * posHistory.setSueDecision(position.isSued()); posHistory.setEhcmPosition(position);
         * OBDal.getInstance().save(posHistory);
         */

      }
      obError.setType("Success");
      obError.setTitle("Success");
      obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
      bundle.setResult(obError);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      bundle.setResult(obError);
      log.error("exception :", e);
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
