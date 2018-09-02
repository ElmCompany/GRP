package sa.elm.ob.hcm.ad_process;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.EhcmCancelPosition;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EhcmPositionHistory;

public class CancelPosIssueDecision implements Process {
  private static final Logger log = Logger.getLogger(CancelPosIssueDecision.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the cancel position");

    final String canpositionId = (String) bundle.getParams().get("Ehcm_Cancelposition_ID")
        .toString();
    EhcmCancelPosition cancelposition = OBDal.getInstance().get(EhcmCancelPosition.class,
        canpositionId);

    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    log.debug("position:" + canpositionId);
    try {
      OBContext.setAdminMode(true);
      // Issue decision is not possible if position is already assigned to an employee
      // if (cancelposition.getJobNo().getAssignedEmployee() != null) {
      if (cancelposition.getJobNo().getEHCMPosEmpHistoryList().size() > 0) {
        OBDal.getInstance().rollbackAndClose();
        OBError result = OBErrorBuilder.buildMessage(null, "error",
            "@EHCM_Position_Assigned_Emp_cannot_Update@");
        bundle.setResult(result);
        return;
      }

      if (cancelposition.getEhcmPosition().getStartDate().after(cancelposition.getCanceldate())
          || (cancelposition.getEhcmPosition().getStartDate()
              .compareTo(cancelposition.getCanceldate()) == 0)) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_CanPosStartDate"));
        bundle.setResult(obError);
        return;
      }

      if (!cancelposition.isSueDecision()) {
        cancelposition.setSueDecision(true);
        cancelposition.setDecisionDate(new Date());
        cancelposition.setTransactionStatus("I");
        OBDal.getInstance().save(cancelposition);

        EhcmPosition positionenddate = OBDal.getInstance().get(EhcmPosition.class,
            cancelposition.getEhcmPosition().getId());
        positionenddate.setActive(false);
        Date startdate = cancelposition.getEhcmPosition().getStartDate();
        Date dateBefore = new Date(cancelposition.getCanceldate().getTime() - 1 * 24 * 3600 * 1000);
        log.debug("stat:" + startdate);
        log.debug("updateposition.getStartDate():" + cancelposition.getCanceldate());
        log.debug(
            "updateposition.compareTo():" + startdate.compareTo(cancelposition.getCanceldate()));
        log.debug("updateposition.dateBefore():" + dateBefore);
        if (startdate.compareTo(cancelposition.getCanceldate()) == 0)
          positionenddate.setEndDate(cancelposition.getCanceldate());
        else
          positionenddate.setEndDate(dateBefore);

        // positionenddate.setEndDate(new java.util.Date());
        // positionenddate.setEhcmCancelposition(cancelposition);
        OBDal.getInstance().save(positionenddate);
        OBDal.getInstance().flush();
        log.debug("isActive:" + positionenddate.isActive());
        log.debug("getJOBNo:" + positionenddate.getJOBNo());
        log.debug("getGrade:" + positionenddate.getGrade());
        log.debug("positionenddate:" + positionenddate);

        EhcmPosition position = OBProvider.getInstance().get(EhcmPosition.class);
        position.setClient(cancelposition.getClient());
        position.setOrganization(cancelposition.getOrganization());
        position.setActive(cancelposition.isActive());
        position.setCreationDate(new java.util.Date());
        position.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        position.setUpdated(new java.util.Date());
        position.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        position.setDepartment(cancelposition.getDepartmentCode());
        position.setDeptname(cancelposition.getDepartmentName());
        if (cancelposition.getSectionCode() != null) {
          position.setSection(cancelposition.getSectionCode());
          position.setSectionname(cancelposition.getSectionName());
        }
        position.setStartDate(cancelposition.getCanceldate());
        position.setEndDate(null);

        if (cancelposition.getNEWMcsLetterNo() != null)
          position.setMCSLetterNo(cancelposition.getNEWMcsLetterNo());
        else
          position.setMCSLetterNo(positionenddate.getMCSLetterNo());

        if (cancelposition.getNEWMcsLetterDate() != null)
          position.setMCSLetterDate(cancelposition.getNEWMcsLetterDate());
        else
          position.setMCSLetterDate(positionenddate.getMCSLetterDate());
        if (cancelposition.getDecisionNo() != null)
          position.setDecisionNo(cancelposition.getDecisionNo());

        position.setDecisionDate(cancelposition.getDecisionDate());
        position.setEhcmPostransactiontype(cancelposition.getTransactionType());
        position.setTransactionStatus(cancelposition.getTransactionStatus());
        position.setTransactionDesc(cancelposition.getReason());

        position.setGrade(cancelposition.getGrade());
        position.setEhcmJobs(cancelposition.getEhcmJobs());
        position.setJOBNo(cancelposition.getJobNo().getJOBNo());
        position.setJOBName(cancelposition.getJobTitle());

        position.setMainGroupCode(positionenddate.getMainGroupCode());
        position.setMainGroupName(positionenddate.getMainGroupName());
        position.setGroupSeqCode(positionenddate.getGroupSeqCode());
        position.setGroupSeqName(positionenddate.getGroupSeqName());
        position.setSUBGroupCode(positionenddate.getSUBGroupCode());
        position.setSUBGroupName(positionenddate.getSUBGroupName());
        position.setYear(positionenddate.getYear());
        position.setBudgetDate(positionenddate.getBudgetDate());
        position.setMOFDecisionDate(positionenddate.getMOFDecisionDate());
        position.setMOFDecisionNo(positionenddate.getMOFDecisionNo());
        position.setSued(cancelposition.isSueDecision());
        position.setEhcmCancelposition(cancelposition);

        if (cancelposition.getMCSLetterNo() != null)
          position.setLetterNo(cancelposition.getMCSLetterNo());
        if (cancelposition.getMCSLetterDate() != null)
          position.setLetterDate(cancelposition.getMCSLetterDate());

        OBDal.getInstance().save(position);
        OBDal.getInstance().flush();

        // update the new positionid for reference
        cancelposition.setNEWPosition(position);
        OBDal.getInstance().save(cancelposition);
        OBDal.getInstance().flush();

        log.debug("position:" + position.getId());
        log.debug("isActive1212:" + position.isActive());
        log.debug("getJOBNo12:" + position.getJOBNo());
        log.debug("getGrade12:" + position.getGrade());

        /*
         * insert a new record in position history window EhcmPositionHistory posHistory =
         * OBProvider.getInstance().get(EhcmPositionHistory.class);
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
         * posHistory.setMCSLetterNo(position.getMCSLetterNo());
         * posHistory.setMCSLetterDate(position.getMCSLetterDate());
         * posHistory.setDecisionNo(position.getDecisionNo());
         * posHistory.setDecisionDate(position.getDecisionDate());
         * posHistory.setTransactionType(position.getEhcmPostransactiontype());
         * posHistory.setTransactionStatus(position.getTransactionStatus());
         * posHistory.setTransactionDescription(position.getTransactionDesc());
         * posHistory.setGrade(position.getGrade()); posHistory.setJobCode(position.getEhcmJobs());
         * posHistory.setJobNo(position.getJOBNo()); posHistory.setJobTitle(position.getJOBName());
         * posHistory.setMainGroupCode(position.getMainGroupCode());
         * posHistory.setMainGroupName(position.getMainGroupName());
         * posHistory.setGroupSequenceCode(position.getGroupSeqCode());
         * posHistory.setGroupSequenceName(position.getGroupSeqName());
         * posHistory.setSubGroupCode(position.getSUBGroupCode());
         * posHistory.setSubGroupName(position.getSUBGroupName());
         * posHistory.setYear(position.getYear());
         * posHistory.setBudgetDate(position.getBudgetDate());
         * posHistory.setMOFDecisionDate(position.getMOFDecisionDate());
         * posHistory.setMOFDecisionNo(position.getMOFDecisionNo());
         * posHistory.setSueDecision(position.isSued()); posHistory.setEhcmPosition(position);
         * OBDal.getInstance().save(posHistory);
         */

        /* insert a new record in position history window for enddate update record */

        OBQuery<EhcmPositionHistory> hislist = OBDal.getInstance().createQuery(
            EhcmPositionHistory.class, "ehcmPosition.id='" + positionenddate.getId() + "'  ");
        log.debug("hislist:" + hislist.getWhereAndOrderBy());
        log.debug("hislist:" + hislist.list().size());
        hislist.setFilterOnActive(false);
        Long lineno = 10L;
        if (hislist.list().size() == 0) {
          EhcmPositionHistory posenddateHistory = OBProvider.getInstance()
              .get(EhcmPositionHistory.class);
          posenddateHistory.setClient(positionenddate.getClient());
          posenddateHistory.setOrganization(positionenddate.getOrganization());
          posenddateHistory.setActive(positionenddate.isActive());
          posenddateHistory.setCreationDate(positionenddate.getCreationDate());
          posenddateHistory.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          posenddateHistory.setUpdated(positionenddate.getUpdated());
          posenddateHistory.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          posenddateHistory.setDepartmentCode(positionenddate.getDepartment());
          posenddateHistory.setDepartmentName(positionenddate.getDeptname());
          if (positionenddate.getSection() != null) {
            posenddateHistory.setSectionCode(positionenddate.getSection());
            posenddateHistory.setSectionName(positionenddate.getSectionname());
          }
          posenddateHistory.setStartDate(positionenddate.getStartDate());
          posenddateHistory.setEndDate(positionenddate.getEndDate());
          posenddateHistory.setMCSLetterNo(positionenddate.getMCSLetterNo());
          posenddateHistory.setMCSLetterDate(positionenddate.getMCSLetterDate());
          posenddateHistory.setDecisionNo(positionenddate.getDecisionNo());
          posenddateHistory.setDecisionDate(positionenddate.getDecisionDate());
          posenddateHistory.setTransactionType(positionenddate.getEhcmPostransactiontype());
          posenddateHistory.setTransactionStatus(positionenddate.getTransactionStatus());
          posenddateHistory.setTransactionDescription(positionenddate.getTransactionDesc());
          posenddateHistory.setGrade(positionenddate.getGrade());
          posenddateHistory.setJobCode(positionenddate.getEhcmJobs());
          posenddateHistory.setJobNo(positionenddate.getJOBNo());
          posenddateHistory.setJobTitle(positionenddate.getJOBName());
          posenddateHistory.setMainGroupCode(positionenddate.getMainGroupCode());
          posenddateHistory.setMainGroupName(positionenddate.getMainGroupName());
          posenddateHistory.setGroupSequenceCode(positionenddate.getGroupSeqCode());
          posenddateHistory.setGroupSequenceName(positionenddate.getGroupSeqName());
          posenddateHistory.setSubGroupCode(positionenddate.getSUBGroupCode());
          posenddateHistory.setSubGroupName(positionenddate.getSUBGroupName());
          posenddateHistory.setYear(positionenddate.getYear());
          posenddateHistory.setBudgetDate(positionenddate.getBudgetDate());
          posenddateHistory.setBudgetDate(positionenddate.getBudgetDate());
          posenddateHistory.setMOFDecisionDate(positionenddate.getMOFDecisionDate());
          posenddateHistory.setMOFDecisionNo(positionenddate.getMOFDecisionNo());
          posenddateHistory.setSueDecision(positionenddate.isSued());
          posenddateHistory.setSourrec(true);
          posenddateHistory.setEhcmPosition(position);
          posenddateHistory.setSrcpositionid(positionenddate);
          posenddateHistory.setSequenceNumber(lineno);
          OBDal.getInstance().save(posenddateHistory);
          OBDal.getInstance().flush();
        } else {

          EhcmPositionHistory posenddateHistory = OBProvider.getInstance()
              .get(EhcmPositionHistory.class);
          posenddateHistory.setClient(positionenddate.getClient());
          posenddateHistory.setOrganization(positionenddate.getOrganization());
          posenddateHistory.setActive(positionenddate.isActive());
          posenddateHistory.setCreationDate(positionenddate.getCreationDate());
          posenddateHistory.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          posenddateHistory.setUpdated(positionenddate.getUpdated());
          posenddateHistory.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          posenddateHistory.setDepartmentCode(positionenddate.getDepartment());
          posenddateHistory.setDepartmentName(positionenddate.getDeptname());
          if (positionenddate.getSection() != null) {
            posenddateHistory.setSectionCode(positionenddate.getSection());
            posenddateHistory.setSectionName(positionenddate.getSectionname());
          }
          posenddateHistory.setStartDate(positionenddate.getStartDate());
          posenddateHistory.setEndDate(positionenddate.getEndDate());
          posenddateHistory.setMCSLetterNo(positionenddate.getMCSLetterNo());
          posenddateHistory.setMCSLetterDate(positionenddate.getMCSLetterDate());
          posenddateHistory.setDecisionNo(positionenddate.getDecisionNo());
          posenddateHistory.setDecisionDate(positionenddate.getDecisionDate());
          posenddateHistory.setTransactionType(positionenddate.getEhcmPostransactiontype());
          posenddateHistory.setTransactionStatus(positionenddate.getTransactionStatus());
          posenddateHistory.setTransactionDescription(positionenddate.getTransactionDesc());
          posenddateHistory.setGrade(positionenddate.getGrade());
          posenddateHistory.setJobCode(positionenddate.getEhcmJobs());
          posenddateHistory.setJobNo(positionenddate.getJOBNo());
          posenddateHistory.setJobTitle(positionenddate.getJOBName());
          posenddateHistory.setMainGroupCode(positionenddate.getMainGroupCode());
          posenddateHistory.setMainGroupName(positionenddate.getMainGroupName());
          posenddateHistory.setGroupSequenceCode(positionenddate.getGroupSeqCode());
          posenddateHistory.setGroupSequenceName(positionenddate.getGroupSeqName());
          posenddateHistory.setSubGroupCode(positionenddate.getSUBGroupCode());
          posenddateHistory.setSubGroupName(positionenddate.getSUBGroupName());
          posenddateHistory.setYear(positionenddate.getYear());
          posenddateHistory.setBudgetDate(positionenddate.getBudgetDate());
          posenddateHistory.setBudgetDate(positionenddate.getBudgetDate());
          posenddateHistory.setMOFDecisionDate(positionenddate.getMOFDecisionDate());
          posenddateHistory.setMOFDecisionNo(positionenddate.getMOFDecisionNo());
          posenddateHistory.setSueDecision(positionenddate.isSued());
          posenddateHistory.setSourrec(true);
          posenddateHistory.setEhcmPosition(position);
          posenddateHistory.setSrcpositionid(positionenddate);
          posenddateHistory.setSequenceNumber(lineno);
          OBDal.getInstance().save(posenddateHistory);
          OBDal.getInstance().flush();

          for (EhcmPositionHistory hist : hislist.list()) {
            lineno++;
            EhcmPositionHistory posenddate = OBProvider.getInstance()
                .get(EhcmPositionHistory.class);
            posenddate.setClient(hist.getClient());
            posenddate.setOrganization(hist.getOrganization());
            posenddate.setActive(hist.isActive());
            posenddate.setCreationDate(hist.getCreationDate());
            posenddate.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
            posenddate.setUpdated(hist.getUpdated());
            posenddate.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
            posenddate.setDepartmentCode(hist.getDepartmentCode());
            posenddate.setDepartmentName(hist.getDepartmentName());
            if (hist.getSectionCode() != null) {
              posenddate.setSectionCode(hist.getSectionCode());
              posenddate.setSectionName(hist.getSectionName());
            }
            posenddate.setStartDate(hist.getStartDate());
            posenddate.setEndDate(hist.getEndDate());
            posenddate.setMCSLetterNo(hist.getMCSLetterNo());
            posenddate.setMCSLetterDate(hist.getMCSLetterDate());
            posenddate.setDecisionNo(hist.getDecisionNo());
            posenddate.setDecisionDate(hist.getDecisionDate());
            posenddate.setTransactionType(hist.getTransactionType());
            posenddate.setTransactionStatus(hist.getTransactionStatus());
            posenddate.setTransactionDescription(hist.getTransactionDescription());
            posenddate.setGrade(hist.getGrade());
            posenddate.setJobCode(hist.getJobCode());
            posenddate.setJobNo(hist.getJobNo());
            posenddate.setJobTitle(hist.getJobTitle());
            posenddate.setMainGroupCode(hist.getMainGroupCode());
            posenddate.setMainGroupName(hist.getMainGroupName());
            posenddate.setGroupSequenceCode(hist.getGroupSequenceCode());
            posenddate.setGroupSequenceName(hist.getGroupSequenceName());
            posenddate.setSubGroupCode(hist.getSubGroupCode());
            posenddate.setSubGroupName(hist.getSubGroupName());
            posenddate.setYear(hist.getYear());
            posenddate.setBudgetDate(hist.getBudgetDate());
            posenddate.setBudgetDate(hist.getBudgetDate());
            posenddate.setMOFDecisionDate(hist.getMOFDecisionDate());
            posenddate.setMOFDecisionNo(hist.getMOFDecisionNo());
            posenddate.setSueDecision(hist.isSueDecision());
            posenddate.setEhcmPosition(position);
            posenddateHistory.setSequenceNumber(lineno);
            OBDal.getInstance().save(posenddate);
            OBDal.getInstance().flush();
          }
        }

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
         * posHistory.setTransactionType(cancelposition.getTransactionType());
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
