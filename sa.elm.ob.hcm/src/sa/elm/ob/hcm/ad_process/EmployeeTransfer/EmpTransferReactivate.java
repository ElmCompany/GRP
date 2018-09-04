package sa.elm.ob.hcm.ad_process.EmployeeTransfer;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
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

import sa.elm.ob.hcm.EHCMEmpTransfer;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.hcm.util.Utility;

public class EmpTransferReactivate implements Process {
  private static final Logger log = Logger.getLogger(EmpTransferReactivate.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    final String transferId = (String) bundle.getParams().get("Ehcm_Emp_Transfer_ID").toString();
    EHCMEmpTransfer transfer = OBDal.getInstance().get(EHCMEmpTransfer.class, transferId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    EmploymentInfo info = null;
    String decisionType = transfer.getDecisionType();
    String lang = vars.getLanguage();
    Date dateBefore = null;
    int milliSecond = 1 * 24 * 3600 * 1000;

    try {
      OBContext.setAdminMode(true);

      if (transfer.getDecisionType().equals("CR") && !transfer.isJoinworkreq()) {
        EmpTransferreactivateDAO.deleteEmpInfo(transfer, decisionType);
        transfer.setDecisionStatus("UP");
        transfer.setDecisionDate(null);
        transfer.setSueDecision(false);
      }

      if (transfer.getDecisionType().equals("UP") && !transfer.isJoinworkreq()) {
        List<EmploymentInfo> empInfoList = null;
        List<EmploymentInfo> prevEmpInfoList = null;
        EmploymentInfo prevEmpinfo = null;
        info = Utility.getActiveEmployInfo(transfer.getEhcmEmpPerinfo().getId());
        EHCMEmpTransfer prevtransfer = transfer.getOriginalDecisionsNo();
        OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
            "  ehcmEmpPerinfo.id = :employeeId and (ehcmEmpTransfer.id <>:currentId or ehcmEmpTransfer.id is null) order by creationDate desc ");
        empInfoObj.setNamedParameter("employeeId", transfer.getEhcmEmpPerinfo().getId());
        empInfoObj.setNamedParameter("currentId", transfer.getId());

        empInfoObj.setMaxResult(1);
        empInfoList = empInfoObj.list();

        if (empInfoList.size() > 0) {
          prevEmpinfo = empInfoList.get(0);
        }

        EmpTransferreactivateDAO.updateEmpInfo(prevtransfer, prevEmpinfo, info, vars, "UP", lang,
            null, null);
        AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
        EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
            transfer.getOriginalDecisionsNo().getNEWEhcmPosition().getId());

        assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(transfer.getClient(),
            transfer.getOrganization(), transfer.getEhcmEmpPerinfo(), null,
            transfer.getOriginalDecisionsNo().getStartDate(),
            transfer.getOriginalDecisionsNo().getEndDate(),
            transfer.getOriginalDecisionsNo().getOriginalDecisionNo(),
            transfer.getOriginalDecisionsNo().getDecisionDate(), pos, vars,
            transfer.getOriginalDecisionsNo(), null, null);

        EhcmPosition uppos = OBDal.getInstance().get(EhcmPosition.class,
            transfer.getNEWEhcmPosition().getId());

        EmploymentInfo recentEmployeInfo = assingedOrReleaseEmpInPositionDAO
            .getRecentEmploymentInfo(transfer.getOriginalDecisionsNo().getEhcmEmpPerinfo(), null,
                transfer.getOriginalDecisionsNo(), null);

        assingedOrReleaseEmpInPositionDAO.deletePositionEmployeeHisotry(
            transfer.getOriginalDecisionsNo().getEhcmEmpPerinfo(), uppos);

        dateBefore = new Date(
            transfer.getOriginalDecisionsNo().getStartDate().getTime() - milliSecond);
        assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
            transfer.getOriginalDecisionsNo().getEhcmEmpPerinfo(), recentEmployeInfo.getPosition(),
            dateBefore, transfer.getOriginalDecisionsNo(), null, null, null, null, null,
            recentEmployeInfo);
        transfer.setEnabled(true);
        transfer.setDecisionStatus("UP");
        transfer.setSueDecision(false);
        transfer.setDecisionType("UP");

      }
      if (transfer.getDecisionType().equals("CA") && !transfer.isJoinworkreq()) {
        info = Utility.getActiveEmployInfo(transfer.getEhcmEmpPerinfo().getId());
        EmpTransferreactivateDAO.insertEmploymentInfo(transfer.getOriginalDecisionsNo(), info, vars,
            "CR", lang, null, null);
        AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
        EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
            transfer.getOriginalDecisionsNo().getNEWEhcmPosition().getId());

        assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(transfer.getClient(),
            transfer.getOrganization(), transfer.getEhcmEmpPerinfo(), null,
            transfer.getOriginalDecisionsNo().getStartDate(),
            transfer.getOriginalDecisionsNo().getEndDate(),
            transfer.getOriginalDecisionsNo().getOriginalDecisionNo(),
            transfer.getOriginalDecisionsNo().getDecisionDate(), pos, vars,
            transfer.getOriginalDecisionsNo(), null, null);

        EmploymentInfo recentEmployeInfo = assingedOrReleaseEmpInPositionDAO
            .getRecentEmploymentInfo(transfer.getOriginalDecisionsNo().getEhcmEmpPerinfo(), null,
                transfer.getOriginalDecisionsNo(), null);

        dateBefore = new Date(
            transfer.getOriginalDecisionsNo().getStartDate().getTime() - milliSecond);
        assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
            transfer.getOriginalDecisionsNo().getEhcmEmpPerinfo(), recentEmployeInfo.getPosition(),
            dateBefore, null, null, null, null, null, null, recentEmployeInfo);
        transfer.setDecisionStatus("UP");
        transfer.setSueDecision(false);
        transfer.setDecisionDate(null);
      }

      obError.setType("Success");
      obError.setTitle("Success");
      obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
      bundle.setResult(obError);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    }

    catch (Exception e) {
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
