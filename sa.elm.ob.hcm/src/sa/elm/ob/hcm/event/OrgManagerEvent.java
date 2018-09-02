package sa.elm.ob.hcm.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DalConnectionProvider;

import sa.elm.ob.hcm.EhcmOrgManager;
import sa.elm.ob.utility.util.Utility;

public class OrgManagerEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmOrgManager.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  PreparedStatement ps = null, ps1 = null;
  ResultSet rs = null, rs1 = null;
  private Logger log = Logger.getLogger(this.getClass());
  String result = "";

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      EhcmOrgManager currentOrgManager = (EhcmOrgManager) event.getTargetInstance();
      final Property enddate = entities[0].getProperty(EhcmOrgManager.PROPERTY_EHCMTODATE);
      final Property startdate = entities[0].getProperty(EhcmOrgManager.PROPERTY_EHCMFROMDATE);

      ConnectionProvider conn = new DalConnectionProvider(false);
      // to date should be greatr than from date.
      if (currentOrgManager.getEhcmTodate() != null) {
        if (currentOrgManager.getEhcmTodate()
            .compareTo(currentOrgManager.getEhcmFromdate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_todate"));
        }
      }
      String tdate = "";
      String fdate = Utility.formatDate(currentOrgManager.getEhcmFromdate());
      if (currentOrgManager.getEhcmTodate() == null) {
        tdate = "21-06-2058";
      } else {
        tdate = Utility.formatDate(currentOrgManager.getEhcmTodate());
      }
      // check whether any other manager is present on same period except current record.
      ps1 = conn
          .getPreparedStatement("select ehcm_fromdate from ehcm_org_manager where ad_org_id ='"
              + currentOrgManager.getOrganization().getId() + "' and ad_client_id='"
              + currentOrgManager.getClient().getId() + "' and ehcm_org_manager_id!='"
              + currentOrgManager.getId()
              + "' and ((to_date(to_char(ehcm_fromdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
              + fdate
              + "') and to_date(to_char(coalesce (ehcm_todate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + tdate
              + "','dd-MM-yyyy')) or (to_date(to_char( coalesce (ehcm_todate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
              + fdate
              + "') and to_date(to_char(ehcm_fromdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + tdate + "','dd-MM-yyyy'))) ");
      rs1 = ps1.executeQuery();
      if (rs1.next()) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_period_conflicts"));
      }

    } catch (OBException e) {
      log.error(" Exception while adding Organizaion Manager: " + e);
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
      EhcmOrgManager currentOrgManager = (EhcmOrgManager) event.getTargetInstance();

      ConnectionProvider conn = new DalConnectionProvider(false);
      // to date should be greatr than from date.
      if (currentOrgManager.getEhcmTodate() != null) {
        if (currentOrgManager.getEhcmTodate()
            .compareTo(currentOrgManager.getEhcmFromdate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_todate"));
        }
      }
      String tdate = "";
      String fdate = Utility.formatDate(currentOrgManager.getEhcmFromdate());
      if (currentOrgManager.getEhcmTodate() == null) {
        tdate = "21-06-2058";
      } else {
        tdate = Utility.formatDate(currentOrgManager.getEhcmTodate());
      }
      // check whether any other manager is present on same period.
      ps1 = conn
          .getPreparedStatement("select ehcm_fromdate from ehcm_org_manager where ad_org_id ='"
              + currentOrgManager.getOrganization().getId() + "' and ad_client_id='"
              + currentOrgManager.getClient().getId()
              + "' and ((to_date(to_char(ehcm_fromdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
              + fdate
              + "') and to_date(to_char(coalesce (ehcm_todate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + tdate
              + "','dd-MM-yyyy')) or (to_date(to_char( coalesce (ehcm_todate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
              + fdate
              + "') and to_date(to_char(ehcm_fromdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + tdate + "','dd-MM-yyyy'))) ");
      rs1 = ps1.executeQuery();
      if (rs1.next()) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_period_conflicts"));
      }

    } catch (OBException e) {
      log.error(" Exception while adding Organizaion Manager: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
