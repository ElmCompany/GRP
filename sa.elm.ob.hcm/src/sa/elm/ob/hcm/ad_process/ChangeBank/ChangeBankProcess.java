package sa.elm.ob.hcm.ad_process.ChangeBank;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

import sa.elm.ob.hcm.EhcmChangeBank;
import sa.elm.ob.hcm.EhcmChangeBankV;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;

/**
 * change bank process to process and reactivate the record.
 * 
 * @author Gokul 12/07/18
 *
 */
public class ChangeBankProcess implements Process {
  private static final Logger LOG = LoggerFactory.getLogger(ChangeBankProcessDAO.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    final String processid = (String) bundle.getParams().get("Ehcm_Change_Bank_ID").toString();
    EhcmChangeBank changebank = OBDal.getInstance().get(EhcmChangeBank.class, processid);
    EhcmChangeBankV changebankview = changebank.getExistingBank();
    OBError obError = new OBError();
    try {
      OBContext.setAdminMode(true);
      // check whether the employee is suspended or not
      if (changebank.getEmployee().getEmploymentStatus()
          .equals(DecisionTypeConstants.EMPLOYMENTSTATUS_SUSPENDED)) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_emplo_suspend"));
        bundle.setResult(obError);
        return;
      }

      if (!changebank.isBankclearancedone()) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_change_bank_isbank"));
        bundle.setResult(obError);
      } else {
        if (changebank.getBankProcessStatus().equals("DR")) {
          if (changebank.getStartDate().compareTo(changebank.getEffectiveDate()) >= 0) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_change_bank_error"));
            bundle.setResult(obError);
          } else {
            // ChangeBankProcessDAO.changeProcess(changebank);
            ChangeBankProcessDAO.insertEmployeeBankDetail(changebank, changebankview);
            ChangeBankProcessDAO.updateEffectivedate(changebank, changebankview);
            obError.setType("Success");
            obError.setTitle("Success");
            obError.setMessage(OBMessageUtils.messageBD("Ehcm_Change_Bank_process"));
            bundle.setResult(obError);
          }
        } else if (changebank.getBankProcessStatus().equals("PR")) {

          int payrollprocesscheck = 0;
          payrollprocesscheck = ChangeBankProcessDAO.chkBankDetailUseinPayprocess(changebank);
          if (payrollprocesscheck > 0) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("Ehcm_payroll_processed"));
            bundle.setResult(obError);
          } else {
            ChangeBankProcessDAO.reactivatedate(changebank, changebankview);
            obError.setType("Success");
            obError.setTitle("Success");
            obError.setMessage(OBMessageUtils.messageBD("Ehcm_Change_Bank_Reactivate"));
            bundle.setResult(obError);
          }
        }
      }
      OBDal.getInstance().save(changebank);
      OBDal.getInstance().flush();
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      LOG.error(" Exception in Change Bank Process : " + e);
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBDal.getInstance().commitAndClose();
      OBContext.restorePreviousMode();
    }
  }

}
