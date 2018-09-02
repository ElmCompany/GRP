package sa.elm.ob.hcm.event;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMHoldUnHoldSalary;
import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.util.payroll.PayrollUtility;

public class HoldUnHoldSalaryEvent extends EntityPersistenceEventObserver {

  private Logger log = Logger.getLogger(this.getClass());
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMHoldUnHoldSalary.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) throws ParseException {
    if (!isValidEvent(event)) {
      return;
    }
    final EHCMHoldUnHoldSalary holdUnHoldSlry = (EHCMHoldUnHoldSalary) event.getTargetInstance();
    try {

      EhcmEmpPerInfo empPerInfo = holdUnHoldSlry.getEhcmEmpPerinfo();
      EHCMPayrolldefPeriod payrollDefPeriod = holdUnHoldSlry.getPayrollPeriod();
      EHCMPayrollDefinition payrollDef = payrollDefPeriod.getEhcmPayrollDefinition();
      String periodStartDate = sa.elm.ob.utility.util.Utility
          .formatDate(payrollDefPeriod.getStartDate());

      if (holdUnHoldSlry.getRequestType().equalsIgnoreCase("HS")) {
        if (PayrollUtility.hasActiveHoldRequest(empPerInfo, holdUnHoldSlry.getId())) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_HasActiveHoldReq"));
        }

        String holdEndDateStr = PayrollUtility.getLatestHoldEndDate(empPerInfo);
        if (holdEndDateStr != null) {
          Date holdEndDate = dateFormat.parse(holdEndDateStr);
          Date startDate = dateFormat.parse(payrollDefPeriod.getStartDate().toString());
          if (holdEndDate.compareTo(startDate) >= 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_HoldFromPeriod_Inv"));
          }
        }

      } else if (holdUnHoldSlry.getRequestType().equalsIgnoreCase("UHS")) {
        if (holdUnHoldSlry.getHoldSalaryReference() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_HoldReference_Req"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while processing Hold & UnHold Salary: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

  public void onSave(@Observes EntityNewEvent event) throws ParseException {
    if (!isValidEvent(event)) {
      return;
    }
    final EHCMHoldUnHoldSalary holdUnHoldSlry = (EHCMHoldUnHoldSalary) event.getTargetInstance();
    try {
      EhcmEmpPerInfo empPerInfo = holdUnHoldSlry.getEhcmEmpPerinfo();
      EHCMPayrolldefPeriod payrollDefPeriod = holdUnHoldSlry.getPayrollPeriod();
      EHCMPayrollDefinition payrollDef = null;
      if (payrollDefPeriod != null) {
        payrollDef = payrollDefPeriod.getEhcmPayrollDefinition();
      }
      String periodStartDate = sa.elm.ob.utility.util.Utility
          .formatDate(payrollDefPeriod.getStartDate());

      if (PayrollUtility.hasDraftHoldUnHoldRequest(empPerInfo, holdUnHoldSlry.getId())) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_HoldUnHold_HasDraft"));
      }

      if (holdUnHoldSlry.getRequestType().equalsIgnoreCase("HS")) {
        if (PayrollUtility.hasActiveHoldRequest(empPerInfo, holdUnHoldSlry.getId())) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_HasActiveHoldReq"));
        }

        String holdEndDateStr = PayrollUtility.getLatestHoldEndDate(empPerInfo);
        if (holdEndDateStr != null) {
          Date holdEndDate = dateFormat.parse(holdEndDateStr);
          Date startDate = dateFormat.parse(payrollDefPeriod.getStartDate().toString());
          if (holdEndDate.compareTo(startDate) >= 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_HoldFromPeriod_Inv"));
          }
        }

      } else if (holdUnHoldSlry.getRequestType().equalsIgnoreCase("UHS")) {
        if (holdUnHoldSlry.getHoldSalaryReference() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_HoldReference_Req"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while processing Hold & UnHold Salary: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final EHCMHoldUnHoldSalary holdUnHoldSlry = (EHCMHoldUnHoldSalary) event.getTargetInstance();
    try {

      if (holdUnHoldSlry.getProcessed().equalsIgnoreCase("Y")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_HoldReq_Processed"));
      }

    } catch (OBException e) {
      log.error(" Exception while processing Hold & UnHold Salary: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
