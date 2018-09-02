package sa.elm.ob.hcm.ad_process;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.EhcmCancelPosition;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EhcmPositionHistory;
import sa.elm.ob.hcm.EhcmUpdatePosition;
import sa.elm.ob.hcm.PositionTreenode;

public class CorrectionPosition implements Process {
  private static final Logger log = Logger.getLogger(CorrectionPosition.class);
  private final OBError obError = new OBError();
  private static final String Transacion_Type_Hold = "HOPO";
  private static final String Transacion_Type_Freeze = "FRPO";

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("correct the position");

    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);

    boolean errorFalg = false;

    if (bundle.getParams().get("Ehcm_Position_ID") != null) {
      final String positionId = (String) bundle.getParams().get("Ehcm_Position_ID").toString();
      log.debug("positionId:" + positionId);
      EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class, positionId);

      // Correction not allowed if that position already assigned to an employee

      // Issue decision is not possible if position is already assigned to an employee
      if (position.getEHCMPosEmpHistoryList().size() > 0) {
        OBDal.getInstance().rollbackAndClose();
        OBError result = OBErrorBuilder.buildMessage(null, "error", "@EHCM_Position_Assigned_Emp@");
        bundle.setResult(result);
        return;
      }
      /*
       * if (position.getEmployee() != null) { OBDal.getInstance().rollbackAndClose(); OBError
       * result = OBErrorBuilder.buildMessage(null, "error", "@EHCM_Position_Assigned_Emp@");
       * bundle.setResult(result); return; }
       */

      try {
        OBContext.setAdminMode(true);
        position.setDecisionDate(null);
        position.setTransactionStatus("UP");
        position.setSued(false);
        OBDal.getInstance().save(position);

        OBDal.getInstance().flush();

        OBQuery<PositionTreenode> positiontree = OBDal.getInstance()
            .createQuery(PositionTreenode.class, " position.id='" + positionId + "'");
        if (positiontree.list().size() > 0) {
          for (PositionTreenode tree : positiontree.list()) {
            OBDal.getInstance().remove(tree);
            OBDal.getInstance().flush();
          }

        }
        OBDal.getInstance().commitAndClose();

        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Efin_BudgetPre_Submit"));
        bundle.setResult(obError);
        return;
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
    if (bundle.getParams().get("Ehcm_Updateposition_ID") != null) {
      final String upupdatepositionId = (String) bundle.getParams().get("Ehcm_Updateposition_ID")
          .toString();
      log.debug("upupdatepositionId:" + upupdatepositionId);
      EhcmUpdatePosition updateposition = OBDal.getInstance().get(EhcmUpdatePosition.class,
          upupdatepositionId);

      // Correction not allowed if that position already assigned to an employee
      if (updateposition.getNEWPosition() != null) {
        // if (updateposition.getNEWPosition().getAssignedEmployee() != null) {
        if (updateposition.getNEWPosition().getEHCMPosEmpHistoryList().size() > 0) {
          OBDal.getInstance().rollbackAndClose();
          OBError result = OBErrorBuilder.buildMessage(null, "error",
              "@EHCM_Position_Assigned_Emp@");
          bundle.setResult(result);
          return;
        }
      }
      if (updateposition.getTransactionType().getSearchKey().equals(Transacion_Type_Freeze)) {
        if (!updateposition.getEhcmPosition().getTransactionStatus().equals("FP")) {
          OBDal.getInstance().rollbackAndClose();
          OBError result = OBErrorBuilder.buildMessage(null, "error", "@Ehcm_Pos_Status@");
          bundle.setResult(result);
          return;
        }
      }
      if (updateposition.getTransactionType().getSearchKey().equals(Transacion_Type_Hold)) {
        if (!updateposition.getEhcmPosition().getTransactionStatus().equals("HP")) {
          OBDal.getInstance().rollbackAndClose();
          OBError result = OBErrorBuilder.buildMessage(null, "error", "@Ehcm_Pos_Status@");
          bundle.setResult(result);
          return;
        }
      }
      try {
        OBContext.setAdminMode(true);
        EhcmPosition refposition = null;
        if (updateposition != null && updateposition.getNEWPosition() != null) {
          OBQuery<EhcmPositionHistory> positionhist = OBDal.getInstance().createQuery(
              EhcmPositionHistory.class,
              "ehcmPosition.id='" + updateposition.getNEWPosition().getId() + "' ");
          positionhist.setFilterOnActive(false);
          List<EhcmPositionHistory> history = positionhist.list();

          if (history.size() > 0) {
            for (EhcmPositionHistory hist : history) {
              log.debug("srcpositionid:" + hist.getSrcpositionid());
              if (hist.getSrcpositionid() != null) {
                EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
                    hist.getSrcpositionid().getId());

                // check already same grade and jobno active or not
                OBQuery<EhcmPosition> pos = OBDal.getInstance().createQuery(EhcmPosition.class,
                    " grade.id='" + position.getGrade().getId() + "' and jOBNo='"
                        + position.getJOBNo()
                        + "' and active='Y' and ehcmPostransactiontype.searchKey not in ('RCPO','RCTRPO','RCFRPO','TRPO')");
                log.debug("list:" + pos.list().size());
                if (pos.list().size() > 0) {
                  obError.setType("Error");
                  obError.setTitle("Error");
                  obError.setMessage(OBMessageUtils.messageBD("EHCM_CantCorrectUpPos"));
                  bundle.setResult(obError);
                  errorFalg = true;
                  return;
                }
              }
            }
          }
          log.debug("errorFalg:" + errorFalg);
          if (!errorFalg) {
            // update the updatepostion transaction type as underprocessing
            updateposition.setDecisionDate(null);
            updateposition.setTransactionStatus("UP");
            updateposition.setSueDecision(false);
            OBDal.getInstance().save(updateposition);
            OBDal.getInstance().flush();

            // get newly created history record in position window and change original position
            // status
            // and remove the history recrod for newly created update position
            log.debug("new position id:" + updateposition.getNEWPosition().getId());

            updateposition.getNEWPosition().setSued(false);
            OBDal.getInstance().save(updateposition.getNEWPosition());
            OBDal.getInstance().flush();

            // OBQuery<EhcmPositionHistory> positionhist =
            // OBDal.getInstance().createQuery(EhcmPositionHistory.class, "ehcmPosition.id='" +
            // updateposition.getNEWPosition().getId() + "' ");
            // log.debug("listwhere:" + positionhist.getWhereAndOrderBy());
            // log.debug("list:" + positionhist.list().size());
            // positionhist.setFilterOnActive(false);
            if (history.size() > 0) {
              for (EhcmPositionHistory hist : history) {
                log.debug("srcpositionid:" + hist.getSrcpositionid());
                if (hist.getSrcpositionid() != null) {
                  EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
                      hist.getSrcpositionid().getId());
                  updateposition.getNEWPosition().setActive(false);
                  OBDal.getInstance().save(updateposition);
                  OBDal.getInstance().flush();
                  position.setEndDate(null);
                  position.setActive(true);
                  OBDal.getInstance().save(position);
                  OBDal.getInstance().flush();

                  OBDal.getInstance().remove(hist);
                  OBDal.getInstance().flush();
                }

                else {
                  OBDal.getInstance().remove(hist);
                  OBDal.getInstance().flush();
                }
              }
            }

            refposition = updateposition.getNEWPosition();
            updateposition.setNEWPosition(null);
            OBDal.getInstance().save(updateposition);
            OBDal.getInstance().flush();

            log.debug("refpostion:" + refposition);

            OBDal.getInstance().remove(refposition);

            OBDal.getInstance().commitAndClose();
          }
        } else {
          if (updateposition.getTransactionType().getSearchKey().equals(Transacion_Type_Hold)
              || updateposition.getTransactionType().getSearchKey()
                  .equals(Transacion_Type_Freeze)) {
            updateposition.setDecisionDate(null);
            updateposition.setTransactionStatus("UP");
            updateposition.setSueDecision(false);
            OBDal.getInstance().save(updateposition);
            OBDal.getInstance().flush();
            EhcmPosition positionObj = OBDal.getInstance().get(EhcmPosition.class,
                updateposition.getEhcmPosition().getId());
            positionObj.setTransactionStatus("I");
            OBDal.getInstance().save(positionObj);
            OBDal.getInstance().flush();
          }
        }
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
        bundle.setResult(obError);
        return;

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
    if (bundle.getParams().get("Ehcm_Cancelposition_ID") != null) {
      final String canpositionId = (String) bundle.getParams().get("Ehcm_Cancelposition_ID")
          .toString();
      log.debug("canpositionId:" + canpositionId);
      EhcmCancelPosition cancelposition = OBDal.getInstance().get(EhcmCancelPosition.class,
          canpositionId);
      try {
        EhcmPosition refposition = null;
        OBContext.setAdminMode(true);
        // update the cancelposition as underprocessing
        cancelposition.setDecisionDate(null);
        cancelposition.setTransactionStatus("UP");
        cancelposition.setSueDecision(false);
        OBDal.getInstance().save(cancelposition);
        OBDal.getInstance().flush();

        // get newly created history record in position window and change original position status
        // and remove the history recrod for newly created update position

        cancelposition.getNEWPosition().setSued(false);
        cancelposition.getNEWPosition().setActive(false);
        OBDal.getInstance().save(cancelposition.getNEWPosition());
        OBDal.getInstance().flush();

        OBQuery<EhcmPositionHistory> positionhist = OBDal.getInstance().createQuery(
            EhcmPositionHistory.class,
            "ehcmPosition.id='" + cancelposition.getNEWPosition().getId() + "'");
        log.debug("listwhere:" + positionhist.getWhereAndOrderBy());
        log.debug("list:" + positionhist.list().size());
        positionhist.setFilterOnActive(false);

        if (positionhist.list().size() > 0) {
          for (EhcmPositionHistory hist : positionhist.list()) {

            log.debug("srcpositionid:" + hist.getSrcpositionid());
            if (hist.getSrcpositionid() != null) {
              EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
                  hist.getSrcpositionid().getId());
              position.setEndDate(null);
              position.setActive(true);
              OBDal.getInstance().save(position);
              OBDal.getInstance().flush();

              OBDal.getInstance().remove(hist);
              OBDal.getInstance().flush();

            } else {
              OBDal.getInstance().remove(hist);
              OBDal.getInstance().flush();
            }
          }
        }
        // null the position reference in cancel position window

        refposition = cancelposition.getNEWPosition();
        cancelposition.setNEWPosition(null);
        OBDal.getInstance().save(cancelposition);
        OBDal.getInstance().flush();

        log.debug("refpostion:" + refposition);

        OBDal.getInstance().remove(refposition);

        OBDal.getInstance().commitAndClose();

        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
        bundle.setResult(obError);
        return;

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
}
