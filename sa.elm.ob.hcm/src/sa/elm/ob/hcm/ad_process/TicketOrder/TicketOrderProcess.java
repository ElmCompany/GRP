package sa.elm.ob.hcm.ad_process.TicketOrder;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

import sa.elm.ob.hcm.EHCMticketordertransaction;

/**
 * 
 * @author Gokul 26/07/2018
 *
 */
public class TicketOrderProcess implements Process {
  private static final Logger LOG = LoggerFactory.getLogger(TicketOrderProcess.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    final String processid = (String) bundle.getParams().get("Ehcm_Ticketordertransaction_ID")
        .toString();
    EHCMticketordertransaction ticketorder = OBDal.getInstance()
        .get(EHCMticketordertransaction.class, processid);
    OBError obError = new OBError();
    try {
      if ((ticketorder.getPayrollProcessLine() != null)
          && ticketorder.getDecisionType().equals("TP")) {
        if (!(ticketorder.getPayrollProcessLine().getPayrollProcessHeader().getStatus().equals("UP")
            || ticketorder.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
                .equals("IC")
            || ticketorder.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
                .equals("DR"))) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_payroll_processed"));
          bundle.setResult(obError);
          return;
        }
      }

      if (ticketorder.getDecisionType().equals("CA") && ticketorder.isSueDecision().equals(false)) {
        ticketorder.setCancel(false);
        ticketorder.getOriginalDecisionNo().setCancel(false);
      } else {
        ticketorder.setCancel(true);
      }

      if (ticketorder.getDecisionStatus().equals("UP")) {
        ticketorder.setDecisionDate(new java.util.Date());
        ticketorder.setDecisionStatus("PR");
        ticketorder.setSueDecision(true);
        ticketorder.setReactivate(false);
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_ticket_order"));
        bundle.setResult(obError);
      } else {
        ticketorder.setDecisionDate(null);
        ticketorder.setDecisionStatus("UP");
        ticketorder.setSueDecision(false);
        ticketorder.setReactivate(true);
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_ticket_order_reactivate"));
        bundle.setResult(obError);
      }
      OBDal.getInstance().save(ticketorder);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      LOG.error(" Exception in Ticket Order Process : " + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
