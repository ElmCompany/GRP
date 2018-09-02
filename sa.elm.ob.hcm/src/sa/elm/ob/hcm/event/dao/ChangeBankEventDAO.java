package sa.elm.ob.hcm.event.dao;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.finance.EfinBank;
import sa.elm.ob.finance.EfinBankBranch;
import sa.elm.ob.hcm.EHCMPersonalPaymethd;
import sa.elm.ob.hcm.EHCMPpmBankdetail;

/**
 * 
 * @author Gokul
 *
 */
public class ChangeBankEventDAO {
  private static Logger LOG = Logger.getLogger(ChangeBankEventDAO.class);

  /**
   * Check the account number is Iban
   * 
   * @param accountnumber
   * @return String.
   */
  public String checkIban(String accountnumber) {
    String accnum = null;

    try {
      OBContext.setAdminMode();
      SQLQuery Query = OBDal.getInstance().getSession()
          .createSQLQuery("select ehcm_changebank_account_num(?);");

      Query.setParameter(0, accountnumber);

      if (Query.list().size() > 0) {
        Object row = (Object) Query.list().get(0);
        accnum = (String) row;
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return accnum;
  }

  /**
   * Check the given details are unique.
   * 
   * @param paymenttype
   * @param employeedetail
   * @param bank
   * @param accountnumber
   * @param bankbranch
   * @return
   */
  public boolean checkUnique(String paymenttype, String employeedetail, EfinBank bank,
      String accountnumber, EfinBankBranch bankbranch) {
    try {
      EHCMPersonalPaymethd header = null;
      OBQuery<EHCMPersonalPaymethd> paymethod = OBDal.getInstance().createQuery(
          EHCMPersonalPaymethd.class,
          "as e  where e.ehcmEmpPerinfo.id=:employeeId and code.id =:PaymentMethodId ");

      paymethod.setNamedParameter("PaymentMethodId", paymenttype);
      paymethod.setNamedParameter("employeeId", employeedetail);
      paymethod.setFilterOnReadableClients(false);
      paymethod.setFilterOnReadableOrganization(false);
      header = paymethod.list().get(0);
      for (EHCMPpmBankdetail bank1 : header.getEHCMPpmBankdetailList()) {
        if ((bank1.getEfinBank() == bank) && (bank1.getAccountNumber().equals(accountnumber))
            && (bank1.getBankBranch() == bankbranch)) {
          return false;
        }

      }

    } catch (final Exception e) {
      e.printStackTrace();
      LOG.error("Exception in Change Bank Event Method : ", e);
    }
    return true;
  }
}
