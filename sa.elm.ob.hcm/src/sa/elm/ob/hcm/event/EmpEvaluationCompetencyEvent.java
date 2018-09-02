package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmpEvalCompetency;
import sa.elm.ob.hcm.event.dao.CompetencyDAO;

/**
 * this process will handle the event of Employee Evaluation Competency
 * 
 * @author divya-12-02-2018
 *
 */
public class EmpEvaluationCompetencyEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEmpEvalCompetency.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(EmpEvaluationCompetencyEvent.class);

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMEmpEvalCompetency empEvalCompetency = (EHCMEmpEvalCompetency) event.getTargetInstance();
      // check competency type and competency is unique
      if (CompetencyDAO.chkEmpEvalCompetencyUnique(empEvalCompetency)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_CompType_Unique"));
      }
      if (empEvalCompetency.getMax().compareTo(empEvalCompetency.getValuation()) < 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpComp_MaximVal"));
      }
      if (empEvalCompetency.getMin().compareTo(empEvalCompetency.getValuation()) > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpComp_MinimumVal"));
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      log.error(" Exception while adding Employee Evaluationcompetecny: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      final Property comptencyType = entities[0]
          .getProperty(EHCMEmpEvalCompetency.PROPERTY_EHCMCOMPTYPECOMPETENCY);
      final Property rating = entities[0].getProperty(EHCMEmpEvalCompetency.PROPERTY_VALUATION);
      EHCMEmpEvalCompetency empEvalCompetency = (EHCMEmpEvalCompetency) event.getTargetInstance();
      // check competency type and competency is unique
      if (!event.getPreviousState(comptencyType).equals(event.getCurrentState(comptencyType))) {
        if (CompetencyDAO.chkEmpEvalCompetencyUnique(empEvalCompetency)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_CompType_Unique"));
        }
      }

      if (!event.getPreviousState(comptencyType).equals(event.getCurrentState(comptencyType))
          || !event.getPreviousState(rating).equals(event.getCurrentState(rating))) {
        if (empEvalCompetency.getMax().compareTo(empEvalCompetency.getValuation()) < 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpComp_MaximVal"));
        }

        if (empEvalCompetency.getMin().compareTo(empEvalCompetency.getValuation()) > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpComp_MinimumVal"));
        }
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      log.error(" Exception while update Employee Evaluationcompetecny : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMEmpEvalCompetency empEvalCompetency = (EHCMEmpEvalCompetency) event.getTargetInstance();
      // check employee evaluation stauts. if status is CO then dont allow to delete
      if (empEvalCompetency.getEhcmEmpevaluationEmp().getEhcmEmpEvaluation().getStatus()
          .equals("CO")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_CantDeleteProcessed"));
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      log.error(" Exception while delete Employee Evaluationcompetecny : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
