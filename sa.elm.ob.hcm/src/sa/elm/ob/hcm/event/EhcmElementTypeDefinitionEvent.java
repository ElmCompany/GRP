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

import sa.elm.ob.hcm.EHCMElmttypeDef;

/**
 * @author Priyanka Ranjan on 20/01/2017
 */

public class EhcmElementTypeDefinitionEvent extends EntityPersistenceEventObserver {

  /**
   * This class is responsible for process of Element Type Definition
   * 
   * 
   */

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMElmttypeDef.ENTITY_NAME) };

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
      EHCMElmttypeDef Checkrecord = (EHCMElmttypeDef) event.getTargetInstance();
      final Property code = entities[0].getProperty(EHCMElmttypeDef.PROPERTY_CODE);
      final Property name = entities[0].getProperty(EHCMElmttypeDef.PROPERTY_NAME);
      final Property reportname = entities[0].getProperty(EHCMElmttypeDef.PROPERTY_REPORTINGNAME);
      final Property graderate = entities[0].getProperty(EHCMElmttypeDef.PROPERTY_GRADERATE);
      final Property extendprocess = entities[0]
          .getProperty(EHCMElmttypeDef.PROPERTY_EXTENDPROCESS);
      String temp = null;
      // Code should be unique
      if (!event.getPreviousState(code).equals(event.getCurrentState(code))) {
        OBQuery<EHCMElmttypeDef> uniquecode = OBDal.getInstance().createQuery(EHCMElmttypeDef.class,
            "as e where e.code='" + Checkrecord.getCode() + "' and e.client.id='"
                + Checkrecord.getClient().getId() + "'");
        if (uniquecode.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtTypeDef_UniCode"));
        }
      }
      // Name should be unique
      if (!event.getPreviousState(name).equals(event.getCurrentState(name))) {
        OBQuery<EHCMElmttypeDef> uniquename = OBDal.getInstance().createQuery(EHCMElmttypeDef.class,
            "as e where e.name='" + Checkrecord.getName() + "' and e.client.id='"
                + Checkrecord.getClient().getId() + "'");
        if (uniquename.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtTypeDef_UniName"));
        }
      }
      // Reporting Name should be unique
      if (event.getPreviousState(reportname) != null
          && !event.getPreviousState(reportname).equals(event.getCurrentState(reportname))) {
        OBQuery<EHCMElmttypeDef> uniquereportname = OBDal.getInstance().createQuery(
            EHCMElmttypeDef.class, "as e where e.reportingName='" + Checkrecord.getReportingName()
                + "' and e.client.id='" + Checkrecord.getClient().getId() + "'");
        if (uniquereportname.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtTypeDef_UniReportingName"));
        }
      }
      // if Type is Nonrecurring then End Date is Mandatory
      /*
       * if (Checkrecord.getType() != null && Checkrecord.getType().equals("NREC") &&
       * Checkrecord.getEndDate() == null) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_ElmtTypeDef_EndDate_Mandatory")); }
       */
      // End Date should be greater than or equal to Start Date
      if (Checkrecord.getEndDate() != null) {
        if (Checkrecord.getEndDate().compareTo(Checkrecord.getStartDate()) < 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDate_greaterorequal_StartDate"));
        }
      }
      // Grade Rate and Extend process field is mandatory for Graderate in ElementSource
      if (Checkrecord.getElementSource().equals("GR")) {
        if (Checkrecord.getGradeRate() == null || Checkrecord.getGradeRate().equals("")) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_GradeRate_Mandatory"));
        }
        if (Checkrecord.getBaseProcess().equals("E")) {
          if (Checkrecord.getExtendProcess() == null || Checkrecord.getExtendProcess().equals("")) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_ExtendProcess_Mandatory"));
          }
        }
      }

      // Empty grade rate and Extend process field for except Grade rate in ElementSource if
      if (!Checkrecord.getElementSource().equals("GR")) {
        event.setCurrentState(graderate, temp);
        event.setCurrentState(extendprocess, temp);
      }

    } catch (OBException e) {
      log.error(" Exception while Updating Element Type Definitio: " + e);
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
      final Property graderate = entities[0].getProperty(EHCMElmttypeDef.PROPERTY_GRADERATE);
      final Property extendprocess = entities[0]
          .getProperty(EHCMElmttypeDef.PROPERTY_EXTENDPROCESS);
      String temp = null;
      OBContext.setAdminMode();
      EHCMElmttypeDef Checkrecord = (EHCMElmttypeDef) event.getTargetInstance();
      // Code should be unique
      OBQuery<EHCMElmttypeDef> uniquecode = OBDal.getInstance().createQuery(EHCMElmttypeDef.class,
          "as e where e.code='" + Checkrecord.getCode() + "' and e.client.id='"
              + Checkrecord.getClient().getId() + "'");
      if (uniquecode.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtTypeDef_UniCode"));
      }
      // Name should be unique
      OBQuery<EHCMElmttypeDef> uniquename = OBDal.getInstance().createQuery(EHCMElmttypeDef.class,
          "as e where e.name='" + Checkrecord.getName() + "' and e.client.id='"
              + Checkrecord.getClient().getId() + "'");
      if (uniquename.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtTypeDef_UniName"));
      }
      // Reporting Name should be unique
      OBQuery<EHCMElmttypeDef> uniquereportname = OBDal.getInstance().createQuery(
          EHCMElmttypeDef.class, "as e where e.reportingName='" + Checkrecord.getReportingName()
              + "' and e.client.id='" + Checkrecord.getClient().getId() + "'");
      if (uniquereportname.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtTypeDef_UniReportingName"));
      }
      // if Type is Nonrecurring then End Date is Mandatory
      /*
       * if (Checkrecord.getType() != null && Checkrecord.getType().equals("NREC") &&
       * Checkrecord.getEndDate() == null) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_ElmtTypeDef_EndDate_Mandatory")); }
       */
      // End Date should be greater than or equal to Start Date
      if (Checkrecord.getEndDate() != null) {
        if (Checkrecord.getEndDate().compareTo(Checkrecord.getStartDate()) < 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDate_greaterorequal_StartDate"));
        }
      }
      // Grade Rate and Extend process field is mandatory for Graderate in ElementSource
      if (Checkrecord.getElementSource().equals("GR")) {
        if (Checkrecord.getGradeRate() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_GradeRate_Mandatory"));
        }
        if (Checkrecord.getBaseProcess().equals("E")) {
          if (Checkrecord.getExtendProcess() == null) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_ExtendProcess_Mandatory"));
          }
        }
      }
      // Empty grade rate and Extend process field for except Grade rate in ElementSource if
      if (!Checkrecord.getElementSource().equals("GR")) {
        Checkrecord.setGradeRate(null);
        if (!Checkrecord.getElementSource().equals("GR")
            && !Checkrecord.getBaseProcess().equals("E")) {
          Checkrecord.setExtendProcess("");
        }
      }
      // Empty grade rate and Extend process field for except Grade rate in ElementSource if
      if (!Checkrecord.getElementSource().equals("GR")) {
        event.setCurrentState(graderate, temp);
        event.setCurrentState(extendprocess, temp);
      }

    } catch (OBException e) {
      log.error(" Exception while creating Element Type Definition: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }
}
