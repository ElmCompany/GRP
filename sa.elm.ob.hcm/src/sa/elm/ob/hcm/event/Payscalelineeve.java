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
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DalConnectionProvider;

import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.utility.util.Utility;

public class Payscalelineeve extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ehcmpayscaleline.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  PreparedStatement ps = null, ps1 = null;
  ResultSet rs = null, rs1 = null;

  String result = "";
  private Logger log = Logger.getLogger(this.getClass());

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      ehcmpayscaleline payscaleline = (ehcmpayscaleline) event.getTargetInstance();
      final Property sequencetype = entities[0].getProperty(ehcmpayscaleline.PROPERTY_LINENO);
      final Property pointno = entities[0].getProperty(ehcmpayscaleline.PROPERTY_EHCMPROGRESSIONPT);
      final Property startdate = entities[0].getProperty(ehcmpayscaleline.PROPERTY_STARTDATE);
      final Property enddate = entities[0].getProperty(ehcmpayscaleline.PROPERTY_ENDDATE);
      ConnectionProvider conn = new DalConnectionProvider(false);
      if (payscaleline.getEndDate() != null) {
        if (payscaleline.getEndDate().compareTo(payscaleline.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_todate"));
        }
      }

      String tdate = "";
      String fdate = Utility.formatDate(payscaleline.getStartDate());
      if (payscaleline.getEndDate() == null) {
        tdate = "21-06-2058";
      } else {
        tdate = Utility.formatDate(payscaleline.getEndDate());
      }

      OBQuery<ehcmpayscaleline> seqtype = OBDal.getInstance().createQuery(ehcmpayscaleline.class,
          " lineNo ='" + payscaleline.getLineNo() + "' and ehcmPayscale.id = '"
              + payscaleline.getEhcmPayscale().getId() + "'  and client.id ='"
              + payscaleline.getClient().getId() + "' ");
      if (!event.getPreviousState(sequencetype).equals(event.getCurrentState(sequencetype))) {
        if (seqtype.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Lineno"));
        }
      }

      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || ((event.getPreviousState(enddate) != null && event.getCurrentState(enddate) != null)
              && !event.getPreviousState(enddate).equals(event.getCurrentState(enddate)))
          || event.getCurrentState(enddate) == null || event.getPreviousState(enddate) == null
          || !event.getPreviousState(pointno).equals(event.getCurrentState(pointno))) {
        ps1 = conn
            .getPreparedStatement("select * from ehcm_payscaleline where ehcm_progressionpt_id ='"
                + payscaleline.getEhcmProgressionpt().getId() + "' and ad_client_id='"
                + payscaleline.getClient().getId() + "' and ehcm_payscale_id='"
                + payscaleline.getEhcmPayscale().getId()
                + "' and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
                + fdate
                + "') and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
                + tdate
                + "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
                + fdate
                + "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
                + tdate + "','dd-MM-yyyy'))) and ehcm_payscaleline_id <> '" + payscaleline.getId()
                + "'");
        rs1 = ps1.executeQuery();
        if (rs1.next()) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Pointno"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating payscaleline event: " + e);
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
      ehcmpayscaleline payscaleline = (ehcmpayscaleline) event.getTargetInstance();
      ConnectionProvider conn = new DalConnectionProvider(false);
      if (payscaleline.getEndDate() != null) {
        if (payscaleline.getEndDate().compareTo(payscaleline.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_todate"));
        }
      }
      String tdate = "";
      String fdate = Utility.formatDate(payscaleline.getStartDate());
      if (payscaleline.getEndDate() == null) {
        tdate = "21-06-2058";
      } else {
        tdate = Utility.formatDate(payscaleline.getEndDate());
      }

      OBQuery<ehcmpayscaleline> seqtype = OBDal.getInstance().createQuery(ehcmpayscaleline.class,
          " lineNo ='" + payscaleline.getLineNo() + "' and ehcmPayscale.id = '"
              + payscaleline.getEhcmPayscale().getId() + "'  and client.id ='"
              + payscaleline.getClient().getId() + "' ");
      if (seqtype.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Lineno"));
      }

      ps1 = conn
          .getPreparedStatement("select * from ehcm_payscaleline where ehcm_progressionpt_id ='"
              + payscaleline.getEhcmProgressionpt().getId() + "' and ad_client_id='"
              + payscaleline.getClient().getId() + "' and ehcm_payscale_id='"
              + payscaleline.getEhcmPayscale().getId()
              + "' and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fdate
              + "') and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + tdate
              + "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
              + fdate + "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + tdate + "','dd-MM-yyyy'))) ");
      rs1 = ps1.executeQuery();
      if (rs1.next()) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Pointno"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating payscaleline event " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
