package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmpEvaluation_Emp;

/**
 * this process will handle the event of Employee Evaluation Employee
 * 
 * @author divya-13-02-2018
 *
 */
public class EmpEvaluationEmpEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEmpEvaluation_Emp.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(EmpEvaluationCompetencyEvent.class);

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMEmpEvaluation_Emp empEvluationEmp = (EHCMEmpEvaluation_Emp) event.getTargetInstance();
      // check employee evaluation stauts. if status is CO then dont allow to delete
      if (empEvluationEmp.getEhcmEmpEvaluation().getStatus().equals("CO")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_CantDeleteProcessed"));
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      log.error(" Exception while delete Employee Evaluation Employee: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
