package sa.elm.ob.hcm.ad_process.ChangeBank;

import java.util.Date;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

import sa.elm.ob.hcm.EHCMPayrollProcessLne;
import sa.elm.ob.hcm.EHCMPpmBankdetail;
import sa.elm.ob.hcm.EhcmChangeBank;
import sa.elm.ob.hcm.EhcmChangeBankV;

/**
 * 
 * @author Gokul 12/07/18
 *
 */
public class ChangeBankProcessDAO {
  private static final Logger log4j = LoggerFactory.getLogger(ChangeBankProcessDAO.class);
  private static Date datebefore;

  /**
   * This method is to insert the ChangeBank details in Employee detail window.
   * 
   * @param changeBank
   * @param changebankview
   */
  public static void insertEmployeeBankDetail(EhcmChangeBank changeBank,
      EhcmChangeBankV changebankview) {
    try {
      OBContext.setAdminMode(true);
      EHCMPpmBankdetail exbankdetail = OBDal.getInstance().get(EHCMPpmBankdetail.class,
          changebankview.getId());
      EHCMPpmBankdetail bankdetail = OBProvider.getInstance().get(EHCMPpmBankdetail.class);
      bankdetail.setClient(exbankdetail.getClient());
      bankdetail.setOrganization(exbankdetail.getOrganization());
      bankdetail.setEhcmPersonalPaymethd(exbankdetail.getEhcmPersonalPaymethd());
      bankdetail.setEfinBank(changeBank.getBankCode());
        if (changeBank.getBankBranch() != null) {
        bankdetail.setBankBranch(changeBank.getBankBranch());
      } else {
        bankdetail.setBankBranch(null);
      }
      bankdetail.setAccountNumber(changeBank.getAccountNumber());
        if (changeBank.getAccountName() != null) {
        bankdetail.setAccountName(changeBank.getAccountName());
      } else {
        bankdetail.setAccountName(null);
      }
      bankdetail.setPercentage(changeBank.getPercentage());
      bankdetail.setStartDate(changeBank.getEffectiveDate());
      bankdetail.setEndDate(null);
      bankdetail.setDefault(false);
      if (changeBank.getBankProcessStatus().equals("DR")) {
        changeBank.setBankProcessStatus("PR");

      } else {
        changeBank.setBankProcessStatus("DR");
      }
      OBDal.getInstance().save(changeBank);
      OBDal.getInstance().save(bankdetail);
      changeBank.setBankDetails(bankdetail);

    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      log4j.error(" Exception while inserting Employee detail : " + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  /**
   * This method is used to update the effective date to the employee detail window.
   * 
   * @param changeBank
   * @param changebankview
   */
  public static void updateEffectivedate(EhcmChangeBank changeBank,
      EhcmChangeBankV changebankview) {
    try {
      OBContext.setAdminMode(true);
      EHCMPpmBankdetail exbankdetail = OBDal.getInstance().get(EHCMPpmBankdetail.class,
          changebankview.getId());
      // EHCMPpmBankdetail bankdetail = OBProvider.getInstance().get(EHCMPpmBankdetail.class);
      datebefore = null;

      datebefore = new Date(changeBank.getEffectiveDate().getTime() - 1 * 24 * 3600 * 1000);
      exbankdetail.setEndDate(datebefore);
      exbankdetail.setActive(false);
      OBDal.getInstance().save(exbankdetail);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log4j.error(" Exception while Updating : " + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  /**
   * This method is to check the record is processed for payroll or not.
   * 
   * @param changeBank
   * @return
   */

  public static int chkBankDetailUseinPayprocess(EhcmChangeBank changeBank) {
    List<EHCMPayrollProcessLne> payrolllist = null;
    try {

      OBQuery<EHCMPayrollProcessLne> processln = OBDal.getInstance()
          .createQuery(EHCMPayrollProcessLne.class, "as e where e.bankDetails.id =:bankdetailID ");
      processln.setNamedParameter("bankdetailID", changeBank.getBankDetails().getId());
      payrolllist = processln.list();
      return payrolllist.size();

    } catch (Exception e) {
      log4j.error(" Exception while Updating : " + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

  /**
   * This method reactivates the process done by the change bank.
   * 
   * @param changeBank
   * @param changebankview
   */
  public static void reactivatedate(EhcmChangeBank changeBank, EhcmChangeBankV changebankview) {
    try {

      EHCMPpmBankdetail exbankdetail = OBDal.getInstance().get(EHCMPpmBankdetail.class,
          changebankview.getId());
      exbankdetail.setEndDate(null);
      OBDal.getInstance().save(exbankdetail);
      if (changeBank.getBankDetails() != null) {
        EHCMPpmBankdetail bankDetailOB = changeBank.getBankDetails();
        changeBank.setBankProcessStatus("DR");
        changeBank.setBankDetails(null);
        // OBDal.getInstance().remove(bankDetailOB);
        OBDal.getInstance().getConnection()
            .prepareStatement("delete from EHCM_Ppm_Bankdetail where EHCM_Ppm_Bankdetail_ID='"
                + bankDetailOB.getId() + "'")
            .execute();
      }
    } catch (Exception e) {
      log4j.error(" Exception while Reactivation: " + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
