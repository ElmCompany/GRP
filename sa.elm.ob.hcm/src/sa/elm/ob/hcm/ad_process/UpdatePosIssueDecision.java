package sa.elm.ob.hcm.ad_process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EhcmPositionHistory;
import sa.elm.ob.hcm.EhcmUpdatePosition;

public class UpdatePosIssueDecision implements Process {
  private static final Logger log = Logger.getLogger(UpdatePosIssueDecision.class);
  private final OBError obError = new OBError();
  private static final String Transacion_Type_Hold = "HOPO";
  private static final String Transacion_Type_Freeze = "FRPO";
  private static final String Transaction_Type_UpgradeFreeze = "UGFRPO";
  private static final String Transaction_Type_DowngradeFreeze = "DGFRPO";
  private static final String Transaction_Type_ReclassifyFreeze = "RCFRPO";

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the updateposition");

    final String upupdatepositionId = (String) bundle.getParams().get("Ehcm_Updateposition_ID")
        .toString();
    EhcmUpdatePosition updateposition = OBDal.getInstance().get(EhcmUpdatePosition.class,
        upupdatepositionId);
    Connection conn = OBDal.getInstance().getConnection();
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    PreparedStatement st = null;
    ResultSet rs = null;

    // Issue decision is not possible if position is already assigned to an employee
    if(updateposition.getJobNo().getEHCMPosEmpHistoryList().size()>0) {
      OBDal.getInstance().rollbackAndClose();
      OBError result = OBErrorBuilder.buildMessage(null, "error",
          "@EHCM_Position_Assigned_Emp_cannot_Update@");
      bundle.setResult(result);
      return;
    }
/*    if (updateposition.getJobNo().getAssignedEmployee() != null) {
      OBDal.getInstance().rollbackAndClose();
      OBError result = OBErrorBuilder.buildMessage(null, "error",
          "@EHCM_Position_Assigned_Emp_cannot_Update@");
      bundle.setResult(result);
      return;
    }*/

    try {
      OBContext.setAdminMode(true);
      if (!updateposition.getTransactionType().getSearchKey().equals(Transacion_Type_Hold)
          && !updateposition.getTransactionType().getSearchKey().equals(Transacion_Type_Freeze)) {
        st = conn.prepareStatement(
            " select count(*)  as total from ehcm_updateposition pos where pos.ehcm_updateposition_id='"
                + updateposition.getId()
                + "' and new_department_id is null and new_deptname is null and new_section_id is null and new_sectionname is null and new_ehcm_jobs_id is null and new_ehcm_grade_id is null and new_job_name is null and new_job_no is null and c_year_id is null and budget_date is null and mof_decision_no is null and mof_decision_date is null");
        log.debug("st:" + st.toString());
        rs = st.executeQuery();
        if (rs.next()) {
          log.debug("total:" + rs.getInt("total"));
          if (rs.getInt("total") > 0) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_UpPosEnterFields"));
            bundle.setResult(obError);
            return;
          }
        }
      }
      if (updateposition.getEhcmPosition().getTransactionStatus().equals("UP")) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_Pos_Status"));
        bundle.setResult(obError);
        return;
      }
      if (updateposition.getTransactionType().getSearchKey().equals("DGRETRPO")
          || updateposition.getTransactionType().getSearchKey().equals("DGTRPO")
          || updateposition.getTransactionType().getSearchKey().equals("UGTRPO")
          || updateposition.getTransactionType().getSearchKey().equals("UGRETRPO")) {
        if (updateposition.getNEWEhcmGrade() == null || updateposition.getNEWJobNo() == null
            || updateposition.getNEWEhcmJobs() == null || updateposition.getNEWJobName() == null
            || updateposition.getNEWDepartment() == null
            || updateposition.getNEWDeptname() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_PosTransfer_Mandatory"));
          bundle.setResult(obError);
          return;
        }
      }
      if (updateposition.getTransactionType().getSearchKey().equals("RCTRPO")) {
        if (updateposition.getNEWDepartment() == null || updateposition.getNEWEhcmJobs() == null
            || updateposition.getNEWJobName() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_ReclassifyPos_Mandatory"));
          bundle.setResult(obError);
          return;
        }
      }

      if (updateposition.getNEWSection() != null) {
        if (updateposition.getNEWDepartment() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_UpPosEnterDept"));
          bundle.setResult(obError);
          return;
        }
      }
      if (updateposition.getNEWEhcmGrade() != null) {
        if (updateposition.getNEWEhcmJobs() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_UpPosEnterJobCode"));
          bundle.setResult(obError);
          return;
        }
      }
      if (updateposition.getNEWEhcmGrade() != null) {
        if (updateposition.getNEWJobNo() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_UpPosEnterJobNo"));
          bundle.setResult(obError);
          return;
        }
      }
      if (updateposition.getNEWEhcmJobs() != null) {
        if (updateposition.getNEWJobName() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_UpPosEnterJobTitle"));
          bundle.setResult(obError);
          return;
        }
      }
      if (updateposition.getNEWJobNo() != null) {
        if (updateposition.getNEWEhcmGrade() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_UpPosEnterGrade"));
          bundle.setResult(obError);
          return;
        }
      }

      if (updateposition.getEhcmPosition().getStartDate().after(updateposition.getStartDate())
          || (updateposition.getEhcmPosition().getStartDate()
              .compareTo(updateposition.getStartDate()) == 0)) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_UPPosStartDate"));
        bundle.setResult(obError);
        return;
      }

      if (updateposition.getNEWEhcmGrade() != null && updateposition.getNEWJobNo() != null) {
        OBQuery<EhcmPosition> pos = OBDal.getInstance().createQuery(EhcmPosition.class,
            " grade.id='" + updateposition.getNEWEhcmGrade().getId() + "' and jOBNo='"
                + updateposition.getNEWJobNo() + "' and active='Y' ");
        log.debug("list:" + pos.list().size());
        if (pos.list().size() > 0) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_CantIssueUPPos"));
          bundle.setResult(obError);
          return;
        }
      }

      if (!updateposition.isSueDecision()) {
        updateposition.setSueDecision(true);
        updateposition.setDecisionDate(new Date());
        updateposition.setTransactionStatus("I");
        OBDal.getInstance().save(updateposition);
        if (!updateposition.getTransactionType().getSearchKey().equals(Transacion_Type_Hold)
            && !updateposition.getTransactionType().getSearchKey().equals(Transacion_Type_Freeze)) {

          EhcmPosition positionenddate = OBDal.getInstance().get(EhcmPosition.class,
              updateposition.getEhcmPosition().getId());
          positionenddate.setActive(false);
          Date startdate = updateposition.getEhcmPosition().getStartDate();
          Date dateBefore = new Date(
              updateposition.getStartDate().getTime() - 1 * 24 * 3600 * 1000);

          log.debug("stat:" + startdate);
          log.debug("updateposition.getStartDate():" + updateposition.getStartDate());
          log.debug(
              "updateposition.compareTo():" + startdate.compareTo(updateposition.getStartDate()));
          log.debug("updateposition.dateBefore():" + dateBefore);
          if (startdate.compareTo(updateposition.getStartDate()) == 0)
            positionenddate.setEndDate(updateposition.getStartDate());
          else
            positionenddate.setEndDate(dateBefore);
          // positionenddate.setEhcmUpdateposition(updateposition);

          OBDal.getInstance().save(positionenddate);
          OBDal.getInstance().flush();

          /* insert a new record in position window */
          EhcmPosition position = OBProvider.getInstance().get(EhcmPosition.class);
          position.setClient(updateposition.getClient());
          position.setOrganization(updateposition.getOrganization());
          position.setActive(updateposition.isActive());
          position.setCreationDate(new java.util.Date());
          position.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          position.setUpdated(new java.util.Date());
          position.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          if (updateposition.getNEWDepartment() != null) {
            position.setDepartment(updateposition.getNEWDepartment());
            position.setDeptname(updateposition.getNEWDeptname());
          } else {
            position.setDepartment(updateposition.getDepartmentCode());
            position.setDeptname(updateposition.getDepartmentName());
          }
          if (updateposition.getNEWSection() != null) {
            position.setSection(updateposition.getNEWSection());
            position.setSectionname(updateposition.getNEWSectionname());
          } else {
            if (updateposition.getSectionCode() != null) {
              position.setSection(updateposition.getSectionCode());
              position.setSectionname(updateposition.getSectionName());
            }
          }
          position.setStartDate(updateposition.getStartDate());
          position.setEndDate(null);

          position.setMCSLetterNo(updateposition.getMCSLetterNo());
          position.setMCSLetterDate(updateposition.getMCSLetterDate());
          position.setDecisionNo(updateposition.getDecisionNo());
          position.setDecisionDate(updateposition.getDecisionDate());
          position.setEhcmPostransactiontype(updateposition.getTransactionType());
          if (updateposition.getTransactionType().getSearchKey()
              .equals(Transaction_Type_UpgradeFreeze)) {
            position.setTransactionStatus("FP");
          } else if (updateposition.getTransactionType().getSearchKey()
              .equals(Transaction_Type_DowngradeFreeze)) {
            position.setTransactionStatus("FP");
          } else if (updateposition.getTransactionType().getSearchKey()
              .equals(Transaction_Type_ReclassifyFreeze)) {
            position.setTransactionStatus("FP");
          } else {
            position.setTransactionStatus(updateposition.getTransactionStatus());
          }
          position.setTransactionDesc(updateposition.getReason());

          if (updateposition.getNEWEhcmGrade() != null) {
            position.setGrade(updateposition.getNEWEhcmGrade());

          } else {
            position.setGrade(updateposition.getGrade());

          }
          if (updateposition.getNEWEhcmJobs() != null) {
            position.setEhcmJobs(updateposition.getNEWEhcmJobs());
            position.setJOBName(updateposition.getNEWJobName());
          } else {
            position.setEhcmJobs(updateposition.getEhcmJobs());
            position.setJOBName(updateposition.getJobTitle());
          }
          if (updateposition.getNEWJobNo() != null) {
            position.setJOBNo(updateposition.getNEWJobNo());
          } else
            position.setJOBNo(updateposition.getJobNo().getJOBNo());
          position.setMainGroupCode(positionenddate.getMainGroupCode());
          position.setMainGroupName(positionenddate.getMainGroupName());
          position.setGroupSeqCode(positionenddate.getGroupSeqCode());
          position.setGroupSeqName(positionenddate.getGroupSeqName());
          position.setSUBGroupCode(positionenddate.getSUBGroupCode());
          position.setSUBGroupName(positionenddate.getSUBGroupName());

          if (updateposition.getYear() != null)
            position.setYear(updateposition.getYear());
          else
            position.setYear(positionenddate.getYear());

          if (updateposition.getBudgetDate() != null)
            position.setBudgetDate(updateposition.getBudgetDate());
          else
            position.setBudgetDate(positionenddate.getBudgetDate());

          if (updateposition.getMOFDecisionDate() != null)
            position.setMOFDecisionDate(updateposition.getMOFDecisionDate());
          else
            position.setMOFDecisionDate(positionenddate.getMOFDecisionDate());

          if (updateposition.getMOFDecisionNo() != null)
            position.setMOFDecisionNo(updateposition.getMOFDecisionNo());
          else
            position.setMOFDecisionNo(positionenddate.getMOFDecisionNo());

          position.setSued(updateposition.isSueDecision());
          position.setEhcmUpdateposition(updateposition);
          OBDal.getInstance().save(position);
          OBDal.getInstance().flush();

          // update the new positionid for reference
          updateposition.setNEWPosition(position);
          OBDal.getInstance().save(updateposition);
          OBDal.getInstance().flush();

          log.debug("position:" + position.getId());

          /* insert a new record in position history window for enddate update record */
          OBQuery<EhcmPositionHistory> hislist = OBDal.getInstance().createQuery(
              EhcmPositionHistory.class, "ehcmPosition.id='" + positionenddate.getId() + "'");
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
            posenddateHistory.setEhcmPosition(position);
            posenddateHistory.setSourrec(true);
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
            posenddateHistory.setEhcmPosition(position);
            posenddateHistory.setSourrec(true);
            posenddateHistory.setSequenceNumber(lineno);
            posenddateHistory.setSrcpositionid(positionenddate);
            OBDal.getInstance().save(posenddateHistory);
            OBDal.getInstance().flush();

            for (EhcmPositionHistory hist : hislist.list()) {
              lineno++;
              EhcmPositionHistory hispos = OBProvider.getInstance().get(EhcmPositionHistory.class);
              hispos.setClient(hist.getClient());
              hispos.setOrganization(hist.getOrganization());
              hispos.setActive(hist.isActive());
              hispos.setCreationDate(hist.getCreationDate());
              hispos.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
              hispos.setUpdated(hist.getUpdated());
              hispos.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
              hispos.setDepartmentCode(hist.getDepartmentCode());
              hispos.setDepartmentName(hist.getDepartmentName());
              if (hist.getSectionCode() != null) {
                hispos.setSectionCode(hist.getSectionCode());
                hispos.setSectionName(hist.getSectionName());
              }
              hispos.setStartDate(hist.getStartDate());
              hispos.setEndDate(hist.getEndDate());
              hispos.setMCSLetterNo(hist.getMCSLetterNo());
              hispos.setMCSLetterDate(hist.getMCSLetterDate());
              hispos.setDecisionNo(hist.getDecisionNo());
              hispos.setDecisionDate(hist.getDecisionDate());
              hispos.setTransactionType(hist.getTransactionType());
              hispos.setTransactionStatus(hist.getTransactionStatus());
              hispos.setTransactionDescription(hist.getTransactionDescription());
              hispos.setGrade(hist.getGrade());
              hispos.setJobCode(hist.getJobCode());
              hispos.setJobNo(hist.getJobNo());
              hispos.setJobTitle(hist.getJobTitle());
              hispos.setMainGroupCode(hist.getMainGroupCode());
              hispos.setMainGroupName(hist.getMainGroupName());
              hispos.setGroupSequenceCode(hist.getGroupSequenceCode());
              hispos.setGroupSequenceName(hist.getGroupSequenceName());
              hispos.setSubGroupCode(hist.getSubGroupCode());
              hispos.setSubGroupName(hist.getSubGroupName());
              hispos.setYear(hist.getYear());
              hispos.setBudgetDate(hist.getBudgetDate());
              hispos.setBudgetDate(hist.getBudgetDate());
              hispos.setMOFDecisionDate(hist.getMOFDecisionDate());
              hispos.setMOFDecisionNo(hist.getMOFDecisionNo());
              hispos.setSueDecision(hist.isSueDecision());
              hispos.setEhcmPosition(position);
              hispos.setSequenceNumber(lineno);
              OBDal.getInstance().save(hispos);
              OBDal.getInstance().flush();
            }
          }
        } else if (updateposition.getTransactionType().getSearchKey()
            .equals(Transacion_Type_Freeze)) {
          EhcmPosition positionObj = OBDal.getInstance().get(EhcmPosition.class,
              updateposition.getEhcmPosition().getId());
          positionObj.setTransactionStatus("FP");
          OBDal.getInstance().save(positionObj);
        } else if (updateposition.getTransactionType().getSearchKey()
            .equals(Transacion_Type_Hold)) {
          EhcmPosition positionObj = OBDal.getInstance().get(EhcmPosition.class,
              updateposition.getEhcmPosition().getId());
          positionObj.setTransactionStatus("HP");
          OBDal.getInstance().save(positionObj);
        }
      }

      obError.setType("Success");
      obError.setTitle("Success");
      obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
      bundle.setResult(obError);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    } catch (

    Exception e) {
      e.printStackTrace();
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
