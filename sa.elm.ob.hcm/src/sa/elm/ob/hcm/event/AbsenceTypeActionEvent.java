package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMAbsenceTypeAction;
import sa.elm.ob.hcm.ad_process.AbsenceDecision.AbsenceIssueDecisionDAOImpl;

public class AbsenceTypeActionEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMAbsenceTypeAction.ENTITY_NAME) };
  private Logger log = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      AbsenceIssueDecisionDAOImpl absenceIssueDecisionDAOImpl = new AbsenceIssueDecisionDAOImpl();
      EHCMAbsenceTypeAction absTypeAction = (EHCMAbsenceTypeAction) event.getTargetInstance();
      if (absenceIssueDecisionDAOImpl.chkAlreadyDepAssorNot(absTypeAction.getAbsenceType())) {
        throw new OBException(OBMessageUtils.messageBD("@EHCM_DepDefineForAbsenceType@"));
      }
    } catch (OBException e) {
      log.error("Exception while creating absence type Action" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
