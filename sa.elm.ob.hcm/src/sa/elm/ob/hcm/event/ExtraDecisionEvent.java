package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmEmployeeExtraStep;
import sa.elm.ob.hcm.EmploymentInfo;

/**
 * @author poongodi on 06/02/2018
 */
public class ExtraDecisionEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmEmployeeExtraStep.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      final Property decisionType = entities[0]
          .getProperty(EhcmEmployeeExtraStep.PROPERTY_DECISIONTYPE);
      final Property startdate = entities[0].getProperty(EhcmEmployeeExtraStep.PROPERTY_STARTDATE);
      EmploymentInfo employinfo = null;
      EhcmEmployeeExtraStep extraStep = (EhcmEmployeeExtraStep) event.getTargetInstance();

      /* current active employment details */
      OBQuery<EmploymentInfo> emplyinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id='" + extraStep.getEhcmEmpPerinfo().getId()
              + "' and e.enabled='Y' order by e.creationDate desc");
      emplyinfo.setMaxResult(1);
      if (emplyinfo.list().size() > 0) {
        employinfo = emplyinfo.list().get(0);
      }

      // original decision no is mandatory
      if (!event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (extraStep.getDecisionType().equals("CA") || extraStep.getDecisionType().equals("UP")) {
          if (extraStep.getOriginalDecisionNo() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
        }
      }

      // checking startdate should not be lesser than current employment startdate
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (extraStep.getDecisionType().equals("CR") || extraStep.getDecisionType().equals("UP")) {
          if (extraStep.getStartDate().compareTo(employinfo.getStartDate()) == -1
              || extraStep.getStartDate().compareTo(employinfo.getStartDate()) == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
          }
        }

      }

    } catch (OBException e) {
      log.error(" Exception while updating Employee extrastep decision  " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();

      EhcmEmployeeExtraStep extraStep = (EhcmEmployeeExtraStep) event.getTargetInstance();
      EmploymentInfo employinfo = null;

      /* current active employment details */
      OBQuery<EmploymentInfo> emplyinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id='" + extraStep.getEhcmEmpPerinfo().getId()
              + "' and e.enabled='Y' order by e.creationDate desc");
      emplyinfo.setMaxResult(1);
      if (emplyinfo.list().size() > 0) {
        employinfo = emplyinfo.list().get(0);
      }

      // original decision no is mandatory
      if (extraStep.getDecisionType().equals("CA") || extraStep.getDecisionType().equals("UP")) {
        if (extraStep.getOriginalDecisionNo() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
      }
      // checking startdate should not be lesser than current employment startdate
      if (extraStep.getDecisionType().equals("CR") || extraStep.getDecisionType().equals("UP")) {
        if (extraStep.getStartDate().compareTo(employinfo.getStartDate()) == -1
            || extraStep.getStartDate().compareTo(employinfo.getStartDate()) == 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Employee extrastep   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
