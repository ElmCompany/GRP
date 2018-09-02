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

import sa.elm.ob.hcm.EHCMPaydefPaymethod;
import sa.elm.ob.utility.util.Utility;

public class PayrolldefPaymentmethodEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMPaydefPaymethod.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  PreparedStatement ps = null, ps1 = null;
  ResultSet rs = null, rs1 = null;
  private Logger log = Logger.getLogger(this.getClass());

  /*
   * check whether end date is greater than start date.
   */
  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      final Property startDate = entities[0].getProperty(EHCMPaydefPaymethod.PROPERTY_STARTDATE);
      final Property endDate = entities[0].getProperty(EHCMPaydefPaymethod.PROPERTY_ENDDATE);
      final Property isdefualt = entities[0].getProperty(EHCMPaydefPaymethod.PROPERTY_DEFAULT);

      EHCMPaydefPaymethod payrolldef = (EHCMPaydefPaymethod) event.getTargetInstance();
      if (payrolldef.getEndDate() != null) {
        if (event.getCurrentState(startDate) != event.getPreviousState(startDate)
            || event.getCurrentState(endDate) != event.getPreviousState(endDate)) {
          if (payrolldef.getEndDate().compareTo(payrolldef.getStartDate()) < 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_StartDateGreThan_EndDate"));
          }
        }
      }

      ConnectionProvider conn = new DalConnectionProvider(false);
      String tdate = "";
      String fdate = Utility.formatDate(payrolldef.getStartDate());
      if (payrolldef.getEndDate() == null) {
        tdate = "21-06-2058";
      } else {
        tdate = Utility.formatDate(payrolldef.getEndDate());
      }
      // check whether any other same payment methods is present on same period.
      ps1 = conn.getPreparedStatement(
          "select start_date from ehcm_paydef_paymethod  where ad_org_id = ? and ad_client_id = ? and ehcm_payroll_payment_method_id = ? and EHCM_Paydef_Paymethod_id != ?"
              + "and ((to_date(to_char(start_date,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fdate
              + "') "
              + "and to_date(to_char(coalesce (end_date,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + tdate + "','dd-MM-yyyy')) "
              + "or (to_date(to_char( coalesce (end_date,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
              + fdate + "') "
              + "and to_date(to_char(start_date,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('" + tdate
              + "','dd-MM-yyyy'))) ");
      ps1.setString(1, payrolldef.getOrganization().getId());
      ps1.setString(2, payrolldef.getClient().getId());
      ps1.setString(3, payrolldef.getEhcmPayrollPaymentMethod().getId());
      ps1.setString(4, payrolldef.getId());
      rs1 = ps1.executeQuery();
      if (rs1.next()) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Payment_Period_Conflicts"));
      }
      /*
       * More than one default should not be allowed.
       */
      if (payrolldef.isDefault()
          && (event.getCurrentState(isdefualt) != event.getPreviousState(isdefualt))) {
        OBQuery<EHCMPaydefPaymethod> payDefmethod = OBDal.getInstance()
            .createQuery(EHCMPaydefPaymethod.class, "default='Y'");
        if (payDefmethod != null && payDefmethod.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Morethanone_Default"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while updating payroll definition payment method " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /*
   * check whether end date is greater than start date.
   */
  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMPaydefPaymethod payrolldef = (EHCMPaydefPaymethod) event.getTargetInstance();
      if (payrolldef.getEndDate() != null) {
        if (payrolldef.getEndDate().compareTo(payrolldef.getStartDate()) < 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_StartDateGreThan_EndDate"));
        }
      }
      ConnectionProvider conn = new DalConnectionProvider(false);
      String tdate = "";
      String fdate = Utility.formatDate(payrolldef.getStartDate());
      if (payrolldef.getEndDate() == null) {
        tdate = "21-06-2058";
      } else {
        tdate = Utility.formatDate(payrolldef.getEndDate());
      }
      // check whether any other same payment methods is present on same period.
      ps1 = conn.getPreparedStatement(
          "select start_date from ehcm_paydef_paymethod  where ad_org_id = ? and ad_client_id = ? and ehcm_payroll_payment_method_id = ? "
              + "and ((to_date(to_char(start_date,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fdate
              + "') "
              + "and to_date(to_char(coalesce (end_date,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + tdate + "','dd-MM-yyyy')) "
              + "or (to_date(to_char( coalesce (end_date,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
              + fdate + "') "
              + "and to_date(to_char(start_date,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('" + tdate
              + "','dd-MM-yyyy'))) ");
      ps1.setString(1, payrolldef.getOrganization().getId());
      ps1.setString(2, payrolldef.getClient().getId());
      ps1.setString(3, payrolldef.getEhcmPayrollPaymentMethod().getId());
      rs1 = ps1.executeQuery();
      if (rs1.next()) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Payment_Period_Conflicts"));
      }

      /*
       * More than one default should not be allowed.
       */
      if (payrolldef.isDefault()) {
        OBQuery<EHCMPaydefPaymethod> payDefmethod = OBDal.getInstance()
            .createQuery(EHCMPaydefPaymethod.class, "default='Y'");
        if (payDefmethod != null && payDefmethod.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Morethanone_Default"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating payroll definition payment method " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
