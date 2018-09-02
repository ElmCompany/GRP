package sa.elm.ob.hcm.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DalConnectionProvider;

import sa.elm.ob.finance.EfinBankBranch;
import sa.elm.ob.hcm.EhcmChangeBank;
import sa.elm.ob.hcm.event.dao.ChangeBankEventDAO;

public class ChangeBankEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmChangeBank.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  PreparedStatement ps = null, ps1 = null;
  ResultSet rs = null, rs1 = null;
  ConnectionProvider conn = new DalConnectionProvider(false);
  private Logger log = Logger.getLogger(this.getClass());
  ChangeBankEventDAO dao = new ChangeBankEventDAO();

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {

      OBContext.setAdminMode();
      EhcmChangeBank changebank = (EhcmChangeBank) event.getTargetInstance();
      final Property paymentType = entities[0].getProperty(EhcmChangeBank.PROPERTY_PAYMENTTYPE);
      final Property accountNumber = entities[0].getProperty(EhcmChangeBank.PROPERTY_ACCOUNTNUMBER);
      final Property bankId = entities[0].getProperty(EhcmChangeBank.PROPERTY_BANKCODE);
      final Property branchId = entities[0].getProperty(EhcmChangeBank.PROPERTY_BANKBRANCH);
      final Property employee = entities[0].getProperty(EhcmChangeBank.PROPERTY_EMPLOYEE);
      String paymenttype = changebank.getPaymentType().getId();
      String employeedetail = changebank.getEmployee().getId();
      String bank = changebank.getBankCode().getId();
      String accountnumber = changebank.getAccountNumber();
      Boolean isExist = true;
      String result = null;
      EfinBankBranch bankbranch = changebank.getBankBranch();
      result = dao.checkIban(accountnumber);
      log.debug("result" + result);
      if (changebank.getStartDate().compareTo(changebank.getEffectiveDate()) >= 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_change_bank_error"));
      }
      if (result.equals("failed")) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Iban_check"));
      }
      if (!event.getCurrentState(paymentType).equals(event.getPreviousState(paymentType))
          || !event.getCurrentState(accountNumber).equals(event.getPreviousState(accountNumber))
          || !event.getCurrentState(bankId).equals(event.getPreviousState(bankId))
          || ((event.getPreviousState(branchId)) != null)
              && !event.getPreviousState(branchId).equals(event.getCurrentState(branchId))
          || !event.getCurrentState(employee).equals(event.getPreviousState(employee))) {
        if (!changebank.getBankProcessStatus().equals("PR")) {
          isExist = dao.checkUnique(paymenttype, employeedetail, changebank.getBankCode(),
              accountnumber, bankbranch);
        }
      }
      if (!isExist) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_unique bank"));
      }

    } catch (OBException e) {
      log.error(" Exception while Change Bank event   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

  @SuppressWarnings("unused")
  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EhcmChangeBank changebank = (EhcmChangeBank) event.getTargetInstance();
      String paymenttype = changebank.getPaymentType().getId();
      String employeedetail = changebank.getEmployee().getId();
      String bank = changebank.getBankCode().getId();
      String accountnumber = changebank.getAccountNumber();
      EfinBankBranch bankbranch = changebank.getBankBranch();
      Boolean isExist = true;
      String result = null;
      if (changebank.getStartDate().compareTo(changebank.getEffectiveDate()) >= 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_change_bank_error"));
      }
      result = dao.checkIban(accountnumber);
      log.debug("result" + result);
      if (result.equals("failed")) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Iban_check"));
      }
      isExist = dao.checkUnique(paymenttype, employeedetail, changebank.getBankCode(),
          accountnumber, bankbranch);
      if (!isExist) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_unique bank"));
      }
    } catch (OBException e) {
      log.error(" Exception while Change Bank event   " + e);
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
    try {
      EhcmChangeBank changebank = (EhcmChangeBank) event.getTargetInstance();
      if (changebank.getBankProcessStatus().equals("PR")) {
        throw new OBException(OBMessageUtils.messageBD("ehcm_process_delete"));
      }
    }

    catch (OBException e) {
      log.error(" Exception while Change Bank event   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
