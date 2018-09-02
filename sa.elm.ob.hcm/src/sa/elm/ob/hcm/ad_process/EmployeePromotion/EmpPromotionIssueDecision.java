package sa.elm.ob.hcm.ad_process.EmployeePromotion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DbUtility;

import sa.elm.ob.hcm.DecisionBalance;
import sa.elm.ob.hcm.EHCMEmpPromotion;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class EmpPromotionIssueDecision implements Process {
  private static final Logger log = Logger.getLogger(EmpPromotionIssueDecision.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the position");

    final String promotionId = (String) bundle.getParams().get("Ehcm_Emp_Promotion_ID").toString();
    EHCMEmpPromotion promotion = OBDal.getInstance().get(EHCMEmpPromotion.class, promotionId);
    Connection conn = OBDal.getInstance().getConnection();
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    EmploymentInfo info = null;
    String promotionType = "", decisionType = "", employmentInfoId = "";
    PreparedStatement st = null;
    ResultSet rs = null;
    log.debug("promotionId:" + promotionId);
    Long seqno = 0L;
    String lang = vars.getLanguage();
    Boolean chkPositionAvailableOrNot = false;
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    OBQuery<EmploymentInfo> employmentInfo = null;
    DateFormat yearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    String lastPromotionDate = null;
    try {
      OBContext.setAdminMode(true);
      log.debug("isSueDecision:" + promotion.isSueDecision());
      log.debug("getDecisionType:" + promotion.getDecisionType());

      // checking position is available or not
      if (!promotion.getDecisionType().equals("CA")) {
        EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
            promotion.getNewPosition().getId());
        chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO.chkPositionAvailableOrNot(
            promotion.getEhcmEmpPerinfo(), position, promotion.getStartDate(), null,
            promotion.getDecisionType(), false);
        if (chkPositionAvailableOrNot) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_PosNotAvailable"));
          bundle.setResult(obError);
          return;
        }
      } else {/*
               * EhcmPosition currentPos = assingedOrReleaseEmpInPositionDAO.getRecentPosition(
               * promotion.getEhcmEmpPerinfo(), promotion.getOriginalDecisionsNo(), null, null);
               */

        EmploymentInfo recentEmployeInfo = assingedOrReleaseEmpInPositionDAO
            .getRecentEmploymentInfo(promotion.getEhcmEmpPerinfo(),
                promotion.getOriginalDecisionsNo(), null, null);

        chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO.chkPositionAvailableOrNot(
            promotion.getEhcmEmpPerinfo(), recentEmployeInfo.getPosition(),
            promotion.getStartDate(), null, promotion.getDecisionType(), false);
        if (chkPositionAvailableOrNot) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_PosNotAvailable"));
          bundle.setResult(obError);
          return;
        }
      }

      if (promotion.getDecisionType().equals("CA")) {
        // dont allow to cancel the promotion while delegate
        // get employment Information for getting the values Location,payroll,payscale
        OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " as e where ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
                + "'  and e.enabled='N' order by e.creationDate desc");
        empInfo.setMaxResult(1);
        if (empInfo.list().size() > 0) {
          EmploymentInfo eminfo = empInfo.list().get(0);
          st = conn.prepareStatement(
              " select seqno from ehcm_grade where seqno > ( select seqno from ehcm_grade where ehcm_grade_id=? and ad_client_id = ? )  order by seqno asc limit 2 ");
          st.setString(1, eminfo.getGrade().getId());
          st.setString(2, eminfo.getClient().getId());

          rs = st.executeQuery();
          while (rs.next()) {
            seqno = rs.getLong("seqno");
          }

          OBQuery<EmployeeDelegation> dele = OBDal.getInstance().createQuery(
              EmployeeDelegation.class,
              " as e where ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
                  + "' and e.enabled='Y' order by e.creationDate desc");
          dele.setMaxResult(1);
          if (dele.list().size() > 0) {
            EmployeeDelegation delegate = dele.list().get(0);
            if ((delegate.getNewPosition().getGrade().getSequenceNumber().compareTo(seqno)) > 1) {
              obError.setType("Error");
              obError.setTitle("Error");
              obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpPromCantCancel"));
              bundle.setResult(obError);
              return;
            }
          }
        }
      }

      // check 4 year validation
      if (promotion.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          || promotion.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {

        if (promotion.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          employmentInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " as e where e.ehcmEmpPerinfo.id=:employeeId and e.employmentgrade.id=:gradeId  order by startdate asc ");
          employmentInfo.setNamedParameter("employeeId", promotion.getEhcmEmpPerinfo().getId());
          employmentInfo.setNamedParameter("gradeId", promotion.getEmploymentGrade().getId());
          employmentInfo.setMaxResult(1);
        } else if (promotion.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {

          OBQuery<ehcmgrade> empgrade = OBDal.getInstance().createQuery(ehcmgrade.class,
              " sequenceNumber <:gradeSeq order by sequenceNumber desc");
          empgrade.setNamedParameter("gradeSeq",
              promotion.getEmploymentGrade().getSequenceNumber());
          empgrade.setMaxResult(1);
          if (empgrade.list().size() > 0) {
            ehcmgrade emplygrade = empgrade.list().get(0);
            employmentInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
                " ehcmEmpPerinfo.id=:employeeId and ehcmEmpPromotion.id is null "
                    + " and employmentgrade.id=:gradeId  order by startdate asc ");
            employmentInfo.setNamedParameter("employeeId", promotion.getEhcmEmpPerinfo().getId());
            employmentInfo.setNamedParameter("gradeId", emplygrade.getId());
            employmentInfo.setMaxResult(1);
          }
        }
        List<EmploymentInfo> employmentInfoList = employmentInfo.list();
        if (employmentInfoList.size() > 0) {
          EmploymentInfo empinfo = employmentInfoList.get(0);
          if (empinfo.getStartDate() != null) {
            String empstartdate = UtilityDAO
                .convertTohijriDate(yearFormat.format(empinfo.getStartDate()));
            String promotiondate = UtilityDAO
                .convertTohijriDate(yearFormat.format(promotion.getStartDate()));

            if (empinfo.getChangereason().equals("H")) {
              DecisionBalance promotionInitialBalance = sa.elm.ob.hcm.util.UtilityDAO
                  .getInitialBaanceObjforEmployee(promotion.getEhcmEmpPerinfo().getId(),
                      Constants.PROMOTIONBALANCE);
              if (promotionInitialBalance != null) {
                lastPromotionDate = UtilityDAO.convertTohijriDate(
                    yearFormat.format(promotionInitialBalance.getBlockStartdate()));
              }
            }
            boolean validation = UtilityDAO.periodyearValidation(
                lastPromotionDate == null ? empstartdate : lastPromotionDate, promotiondate, 4);
            if (!validation) {
              obError.setType("Error");
              obError.setTitle("Error");
              obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpPromCantAllow"));
              bundle.setResult(obError);
              return;
            }
          }
        }
      }
      // check Issued or not
      if (!promotion.isSueDecision()) {
        // update status as Issued and set decision date for all cases
        promotion.setSueDecision(true);
        promotion.setDecisionDate(new Date());
        promotion.setDecisionStatus("I");
        OBDal.getInstance().save(promotion);
        OBDal.getInstance().flush();

        info = Utility.getActiveEmployInfo(promotion.getEhcmEmpPerinfo().getId());
        decisionType = promotion.getDecisionType();
        // Create & update Cases
        if (promotion.getDecisionType().equals("CR") && !promotion.isJoinWorkRequest()) {

          EmployeePromotionHandlerDAO.insertEmploymentInfo(promotion, info, vars, decisionType,
              lang, null, null);
        }

        if (promotion.getDecisionType().equals("UP") && promotion.getOriginalDecisionsNo() != null
            && !promotion.getOriginalDecisionsNo().isJoinWorkRequest()) {
          log.debug(promotion.getOriginalDecisionsNo());
          EmployeePromotionHandlerDAO.insertEmploymentInfo(promotion, info, vars, decisionType,
              lang, null, null);
          EmployeePromotionHandlerDAO.updateEnddateinEmpInfo(promotion, info, vars);
        }
        // cancel case
        else if (promotion.getDecisionType().equals("CA")
            && promotion.getOriginalDecisionsNo() != null
            && !promotion.getOriginalDecisionsNo().isJoinWorkRequest()) {
          EmployeePromotionHandlerDAO.CancelinPromotion(promotion, vars);
        }

        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
        bundle.setResult(obError);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
      }
    } catch (Exception e) {
      e.printStackTrace();
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
