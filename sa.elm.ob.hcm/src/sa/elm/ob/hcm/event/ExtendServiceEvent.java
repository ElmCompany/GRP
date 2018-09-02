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

import sa.elm.ob.hcm.EhcmExtendService;
import sa.elm.ob.hcm.EmploymentInfo;

/**
 * @author poongodi on 12/02/2018
 */
public class ExtendServiceEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmExtendService.ENTITY_NAME) };

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
          .getProperty(EhcmExtendService.PROPERTY_DECISIONTYPE);
      final Property startdate = entities[0].getProperty(EhcmExtendService.PROPERTY_EFFECTIVEDATE);
      EmploymentInfo employinfo = null;
      EhcmExtendService extendService = (EhcmExtendService) event.getTargetInstance();

      /* current active employment details */
      OBQuery<EmploymentInfo> emplyinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id='" + extendService.getEmployee().getId()
              + "' and e.enabled='Y' order by e.creationDate desc");
      emplyinfo.setMaxResult(1);
      if (emplyinfo.list().size() > 0) {
        employinfo = emplyinfo.list().get(0);
      }

      // original decision no is mandatory
      if (!event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (extendService.getDecisionType().equals("CA")
            || extendService.getDecisionType().equals("UP")) {
          if (extendService.getOriginalDecisionNo() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
        }
      }

      // checking effectivedate should not be lesser than current employment startdate
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (extendService.getDecisionType().equals("CR")) {
          if (extendService.getEffectivedate().compareTo(employinfo.getStartDate()) == -1
              || extendService.getEffectivedate().compareTo(employinfo.getStartDate()) == 0) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Extend_EffectiveDate"));
          }
        }

        if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
            || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
          if (extendService.getDecisionType().equals("UP")) {
            // Get the previous employment record
            OBQuery<EmploymentInfo> emply = OBDal.getInstance().createQuery(EmploymentInfo.class,
                "as e where e.ehcmEmpPerinfo.id='" + extendService.getEmployee().getId()
                    + "' and e.ehcmExtendService is null order by e.creationDate desc");
            emply.setMaxResult(1);
            if (emply.list().size() > 0) {
              employinfo = emply.list().get(0);
            }
            if (extendService.getEffectivedate().compareTo(employinfo.getStartDate()) == -1
                || extendService.getEffectivedate().compareTo(employinfo.getStartDate()) == 0) {
              throw new OBException(OBMessageUtils.messageBD("Ehcm_Extend_EffectiveDate"));
            }
          }
        }
      }

    } catch (OBException e) {
      log.error(" Exception while updating Employee extendService   " + e);
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

      EhcmExtendService extendService = (EhcmExtendService) event.getTargetInstance();
      EmploymentInfo employinfo = null;

      /* current active employment details */
      OBQuery<EmploymentInfo> emplyinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id='" + extendService.getEmployee().getId()
              + "' and e.enabled='Y' order by e.creationDate desc");
      emplyinfo.setMaxResult(1);
      if (emplyinfo.list().size() > 0) {
        employinfo = emplyinfo.list().get(0);
      }

      // original decision no is mandatory
      if (extendService.getDecisionType().equals("CA")
          || extendService.getDecisionType().equals("UP")) {
        if (extendService.getOriginalDecisionNo() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
      }
      // checking effectivedate should not be lesser than current employment startdate
      if (extendService.getDecisionType().equals("CR")) {
        if (extendService.getEffectivedate().compareTo(employinfo.getStartDate()) == -1
            || extendService.getEffectivedate().compareTo(employinfo.getStartDate()) == 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Extend_EffectiveDate"));
        }
      }
      if (extendService.getDecisionType().equals("UP")) {
        /* get previous employment record */
        OBQuery<EmploymentInfo> emply = OBDal.getInstance().createQuery(EmploymentInfo.class,
            "as e where e.ehcmEmpPerinfo.id='" + extendService.getEmployee().getId()
                + "' and e.ehcmExtendService is null order by e.creationDate desc");
        emply.setMaxResult(1);
        if (emply.list().size() > 0) {
          employinfo = emply.list().get(0);
        }

        if (extendService.getEffectivedate().compareTo(employinfo.getStartDate()) == -1
            || extendService.getEffectivedate().compareTo(employinfo.getStartDate()) == 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Extend_EffectiveDate"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Employee extendService   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
