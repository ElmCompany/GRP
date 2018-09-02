package sa.elm.ob.hcm.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EHCMPayrollPeriodTypes;
import sa.elm.ob.utility.EUT_HijriDates;
import sa.elm.ob.utility.util.UtilityDAO;

public class PayrolldefinitionEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMPayrollDefinition.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  PreparedStatement st = null;
  ResultSet rs = null;
  NumberFormat Numformatter = new DecimalFormat("00");
  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    int MM = 0, YYYY = 0;
    Date endDate = new Date();
    try {
      OBContext.setAdminMode();
      EHCMPayrollDefinition payrolldef = (EHCMPayrollDefinition) event.getTargetInstance();
      final Property isdefualt = entities[0]
          .getProperty(EHCMPayrollDefinition.PROPERTY_EHCMDEFAULT);

      /*
       * check whether period end date is last date of month.
       */
      EHCMPayrollPeriodTypes periodType = OBDal.getInstance().get(EHCMPayrollPeriodTypes.class,
          payrolldef.getEhcmPayrollPeriodTypes().getId());
      if (periodType.getPeriodsperyear() == 2 || periodType.getPeriodsperyear() == 4
          || periodType.getPeriodsperyear() == 6 || periodType.getPeriodsperyear() == 12) {
        String PeriodEnddate = format.format(payrolldef.getPeriodEnddate());
        String hijiridate = UtilityDAO.eventConvertTohijriDate(PeriodEnddate);
        MM = Integer.valueOf(hijiridate.split("-")[1]);
        YYYY = Integer.valueOf(hijiridate.split("-")[2]);

        String fstGerDate = UtilityDAO
            .eventConvertToGregorian(hijiridate.split("-")[2] + hijiridate.split("-")[1] + "01");

        Date startDate = format.parse(fstGerDate);

        MM = MM + 1;
        if (MM > 12) {
          MM = MM - 12;
          YYYY = YYYY + 1;
        }

        hijiridate = "01-" + Numformatter.format(MM) + "-" + YYYY;
        fstGerDate = UtilityDAO.eventConvertToGregorian(YYYY + Numformatter.format(MM) + "01");
        Date nextstartDate = format.parse(fstGerDate);
        OBCriteria<EUT_HijriDates> cr = OBDal.getInstance().createCriteria(EUT_HijriDates.class);

        cr.add(Restrictions.ge(EUT_HijriDates.PROPERTY_GREGORIANDATE, startDate));
        cr.add(Restrictions.lt(EUT_HijriDates.PROPERTY_GREGORIANDATE, nextstartDate));
        cr.addOrder(Order.desc(EUT_HijriDates.PROPERTY_GREGORIANDATE));
        cr.setMaxResults(1);
        if (cr.list().size() > 0) {
          endDate = cr.list().get(0).getGregorianDate();
        }
        if (payrolldef.getPeriodEnddate().compareTo(endDate) != 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate_EOM"));
        }
      }
      /*
       * check whether values in payroll run and direct deposit is between 1 - 30.
       */
      final Property payrollRun = entities[0]
          .getProperty(EHCMPayrollDefinition.PROPERTY_PAYROLLRUN);
      final Property directDeposite = entities[0]
          .getProperty(EHCMPayrollDefinition.PROPERTY_DIRECTDEPOSITE);
      if (!event.getPreviousState(payrollRun).equals(payrolldef.getPayrollRun())
          || !event.getPreviousState(directDeposite).equals(payrolldef.getDirectDeposite())) {
        if (payrolldef.getPayrollRun() < 1 || payrolldef.getPayrollRun() > 30
            || payrolldef.getDirectDeposite() < 1 || payrolldef.getDirectDeposite() > 30) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Payroll_Date"));
        }
      }
      /*
       * NO.of year should be > 0
       */
      if (payrolldef.getEhcmYear() < 1) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Period_Year"));
      }
      /*
       * More than one default should not be allowed.
       */
      if (payrolldef.isEhcmDefault()
          && (event.getCurrentState(isdefualt) != event.getPreviousState(isdefualt))) {
        OBQuery<EHCMPayrollDefinition> payDef = OBDal.getInstance()
            .createQuery(EHCMPayrollDefinition.class, "ehcmDefault='Y'");
        if (payDef != null && payDef.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Morethanone_Default"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while updating payroll definition  " + e);
      throw new OBException(e.getMessage());
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
    int MM = 0, YYYY = 0;
    Date endDate = new Date();
    try {
      OBContext.setAdminMode();
      EHCMPayrollDefinition payrolldef = (EHCMPayrollDefinition) event.getTargetInstance();

      /*
       * check whether period end date is last date of month.
       */
      EHCMPayrollPeriodTypes periodType = OBDal.getInstance().get(EHCMPayrollPeriodTypes.class,
          payrolldef.getEhcmPayrollPeriodTypes().getId());
      if (periodType.getPeriodsperyear() == 2 || periodType.getPeriodsperyear() == 4
          || periodType.getPeriodsperyear() == 6 || periodType.getPeriodsperyear() == 12) {
        String PeriodEnddate = format.format(payrolldef.getPeriodEnddate());
        String hijiridate = UtilityDAO.eventConvertTohijriDate(PeriodEnddate);
        MM = Integer.valueOf(hijiridate.split("-")[1]);
        YYYY = Integer.valueOf(hijiridate.split("-")[2]);

        String fstGerDate = UtilityDAO
            .eventConvertToGregorian(hijiridate.split("-")[2] + hijiridate.split("-")[1] + "01");

        Date startDate = format.parse(fstGerDate);

        MM = MM + 1;
        if (MM > 12) {
          MM = MM - 12;
          YYYY = YYYY + 1;
        }

        hijiridate = "01-" + Numformatter.format(MM) + "-" + YYYY;
        fstGerDate = UtilityDAO.eventConvertToGregorian(YYYY + Numformatter.format(MM) + "01");
        Date nextstartDate = format.parse(fstGerDate);
        OBCriteria<EUT_HijriDates> cr = OBDal.getInstance().createCriteria(EUT_HijriDates.class);

        cr.add(Restrictions.ge(EUT_HijriDates.PROPERTY_GREGORIANDATE, startDate));
        cr.add(Restrictions.lt(EUT_HijriDates.PROPERTY_GREGORIANDATE, nextstartDate));
        cr.addOrder(Order.desc(EUT_HijriDates.PROPERTY_GREGORIANDATE));
        cr.setMaxResults(1);
        if (cr.list().size() > 0) {
          endDate = cr.list().get(0).getGregorianDate();
        }
        if (payrolldef.getPeriodEnddate().compareTo(endDate) != 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate_EOM"));
        }
      }
      /*
       * check whether values in payroll run and direct deposit is between 1 - 30.
       */
      if (payrolldef.getPayrollRun() < 1 || payrolldef.getPayrollRun() > 30
          || payrolldef.getDirectDeposite() < 1 || payrolldef.getDirectDeposite() > 30) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Payroll_Date"));
      }

      /*
       * NO.of year should be > 0
       */
      if (payrolldef.getEhcmYear() < 1) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Period_Year"));
      }
      /*
       * More than one default should not be allowed.
       */
      if (payrolldef.isEhcmDefault()) {
        OBQuery<EHCMPayrollDefinition> payDef = OBDal.getInstance()
            .createQuery(EHCMPayrollDefinition.class, "ehcmDefault='Y'");
        if (payDef != null && payDef.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Morethanone_Default"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while creating payroll definition  " + e);
      throw new OBException(e.getMessage());
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
