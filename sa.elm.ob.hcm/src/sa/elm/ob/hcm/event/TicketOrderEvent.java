package sa.elm.ob.hcm.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMticketordertransaction;

/**
 * 
 * @author Gokul 26/07/18
 *
 */
public class TicketOrderEvent extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMticketordertransaction.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      EHCMticketordertransaction ticketOrder = (EHCMticketordertransaction) event
          .getTargetInstance();

      // Check whether start date is greater than end date
      if (ticketOrder.getTravelstartdate().compareTo(ticketOrder.getTravelenddate()) > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Ticketorder_datevalidation"));
      }

      // Check whether original decision number is available or not
      if (!(ticketOrder.getDecisionType().equals("CR"))
          && (ticketOrder.getOriginalDecisionNo() == null)) {
        if ((ticketOrder.getDecisionType().equals("TP")) && (!(ticketOrder.getReason().equals("SC"))
            && !(ticketOrder.getReason().equals("BM")))) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_ticket_orgdecno"));
        }
      }
      if (ticketOrder.getReason().equals("BM") && ticketOrder.getBusinessMission() == null) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Ticketorder_BMValidation"));
      }
      if (ticketOrder.getReason().equals("SC") && ticketOrder.getEhcmEmpScholarship() == null) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Ticketorder_SCValidation"));
      }
      List<String> dependentList = new ArrayList<String>();
      if (ticketOrder.getDependent1() != null)
        dependentList.add(ticketOrder.getDependent1().getId());
      if (ticketOrder.getDependent2() != null)
        dependentList.add(ticketOrder.getDependent2().getId());
      if (ticketOrder.getDependent3() != null)
        dependentList.add(ticketOrder.getDependent3().getId());
      if (ticketOrder.getDependent4() != null)
        dependentList.add(ticketOrder.getDependent4().getId());
      if (ticketOrder.getDependent5() != null)
        dependentList.add(ticketOrder.getDependent5().getId());
      if (ticketOrder.getDependent6() != null)
        dependentList.add(ticketOrder.getDependent6().getId());
      // Convert list to set to remove duplicate values
      Set<String> set = new HashSet<String>(dependentList);
      // comparing size of list and set
      int listSize = dependentList.size();
      int setSize = set.size();

      if (listSize > setSize) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Dependent_value"));
      }

      if (ticketOrder.getReason().equals("BM")) {
        // Allow only ticket payment if ticket provided is checked
        if (ticketOrder.getBusinessMission().isTicketsProvided() == true) {
          if (ticketOrder.getDecisionType().equals("TP")) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_tktorder_bmpayment"));
          }
        } else {
          if (!ticketOrder.getDecisionType().equals("TP")) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_tktorder_bmnotpayment"));
          }
        }
        // Allow one create per Business Mission
        if (ticketOrder.getDecisionType().equals("CR")
            || ticketOrder.getDecisionType().equals("TP")) {
          OBQuery<EHCMticketordertransaction> ticketCount = OBDal.getInstance().createQuery(
              EHCMticketordertransaction.class,
              "as e where e.businessMission.id =:businessMissionId and e.issueDecision='Y' and e.id!=:tktOrderId "
                  + " and e.businessMission.id in (select e.businessMission.id from EHCM_ticketordertransaction "
                  + "where e.processed = 'Y' and e.decisionType !='CA')");
          ticketCount.setNamedParameter("businessMissionId",
              ticketOrder.getBusinessMission().getId());
          ticketCount.setNamedParameter("tktOrderId", ticketOrder.getId());
          if (ticketCount.list().size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_Ticketorder_BMUniqueValidation"));
          }
        }
      } else if (ticketOrder.getReason().equals("SC")) {
        // Allow only ticket payment if ticket provided is checked
        if (ticketOrder.getEhcmEmpScholarship().isTicketprovided() == true) {
          if (ticketOrder.getDecisionType().equals("TP")) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_tktorder_scpayment"));
          }
        } else {
          if (!ticketOrder.getDecisionType().equals("TP")) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_tktorder_scnotpayment"));
          }
        }
        // Allow one create per Scholarship
        if (ticketOrder.getDecisionType().equals("CR")
            || ticketOrder.getDecisionType().equals("TP")) {
          OBQuery<EHCMticketordertransaction> ticketCount = OBDal.getInstance().createQuery(
              EHCMticketordertransaction.class,
              "as e where e.ehcmEmpScholarship.id =:scholarshipId and e.issueDecision='Y' and e.id!=:tktOrderId "
                  + " and e.ehcmEmpScholarship.id in (select e.ehcmEmpScholarship.id from EHCM_ticketordertransaction "
                  + "where e.processed = 'Y' and e.decisionType !='CA')");
          ticketCount.setNamedParameter("scholarshipId",
              ticketOrder.getEhcmEmpScholarship().getId());
          ticketCount.setNamedParameter("tktOrderId", ticketOrder.getId());
          if (ticketCount.list().size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_Ticketorder_SCUniqueValidation"));
          }
        }
      }

    } catch (OBException e) {
      log.error(" Exception while Ticket Order event   " + e);
      e.printStackTrace();
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
      EHCMticketordertransaction ticketOrder = (EHCMticketordertransaction) event
          .getTargetInstance();
      if (ticketOrder.getTravelstartdate().compareTo(ticketOrder.getTravelenddate()) > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Ticketorder_datevalidation"));
      }
      if (ticketOrder.getReason().equals("BM") && ticketOrder.getBusinessMission() == null) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Ticketorder_BMValidation"));
      }
      if (ticketOrder.getReason().equals("SC") && ticketOrder.getEhcmEmpScholarship() == null) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Ticketorder_SCValidation"));
      }
      if (!(ticketOrder.getDecisionType().equals("CR"))
          && (ticketOrder.getOriginalDecisionNo() == null)) {
        if ((ticketOrder.getDecisionType().equals("TP")) && (!(ticketOrder.getReason().equals("SC"))
            && !(ticketOrder.getReason().equals("BM")))) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_ticket_orgdecno"));
        }
      }
      List<String> dependentList = new ArrayList<String>();
      if (ticketOrder.getDependent1() != null)
        dependentList.add(ticketOrder.getDependent1().getId());
      if (ticketOrder.getDependent2() != null)
        dependentList.add(ticketOrder.getDependent2().getId());
      if (ticketOrder.getDependent3() != null)
        dependentList.add(ticketOrder.getDependent3().getId());
      if (ticketOrder.getDependent4() != null)
        dependentList.add(ticketOrder.getDependent4().getId());
      if (ticketOrder.getDependent5() != null)
        dependentList.add(ticketOrder.getDependent5().getId());
      if (ticketOrder.getDependent6() != null)
        dependentList.add(ticketOrder.getDependent6().getId());
      // Convert list to set to remove duplicate values
      Set<String> set = new HashSet<String>(dependentList);
      // comparing size of list and set
      int listSize = dependentList.size();
      int setSize = set.size();

      if (listSize > setSize) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Dependent_value"));
      }

      if (ticketOrder.getReason().equals("BM")) {
        // Allow only ticket payment if ticket provided is checked
        if (ticketOrder.getBusinessMission().isTicketsProvided() == true) {
          if (ticketOrder.getDecisionType().equals("TP")) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_tktorder_bmpayment"));
          }
        } else {
          if (!ticketOrder.getDecisionType().equals("TP")) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_tktorder_bmnotpayment"));
          }
        }
        // Allow one create per Business Mission
        if (ticketOrder.getDecisionType().equals("CR")
            || ticketOrder.getDecisionType().equals("TP")) {
          OBQuery<EHCMticketordertransaction> ticketCount = OBDal.getInstance().createQuery(
              EHCMticketordertransaction.class,
              "as e where e.businessMission.id =:businessMissionId and e.issueDecision='Y' and e.id!=:tktOrderId "
                  + " and e.businessMission.id in (select e.businessMission.id from EHCM_ticketordertransaction "
                  + "where e.processed = 'Y' and e.decisionType !='CA')");
          ticketCount.setNamedParameter("businessMissionId",
              ticketOrder.getBusinessMission().getId());
          ticketCount.setNamedParameter("tktOrderId", ticketOrder.getId());
          if (ticketCount.list().size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_Ticketorder_BMUniqueValidation"));
          }
        }
      } else if (ticketOrder.getReason().equals("SC")) {
        // Allow only ticket payment if ticket provided is checked
        if (ticketOrder.getEhcmEmpScholarship().isTicketprovided() == true) {
          if (ticketOrder.getDecisionType().equals("TP")) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_tktorder_scpayment"));
          }
        } else {
          if (!ticketOrder.getDecisionType().equals("TP")) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_tktorder_scnotpayment"));
          }
        }
        // Allow one create per Scholarship
        if (ticketOrder.getDecisionType().equals("CR")
            || ticketOrder.getDecisionType().equals("TP")) {
          OBQuery<EHCMticketordertransaction> ticketCount = OBDal.getInstance().createQuery(
              EHCMticketordertransaction.class,
              "as e where e.ehcmEmpScholarship.id =:scholarshipId and e.issueDecision='Y' and e.id!=:tktOrderId "
                  + " and e.ehcmEmpScholarship.id in (select e.ehcmEmpScholarship.id from EHCM_ticketordertransaction "
                  + "where e.processed = 'Y' and e.decisionType !='CA')");
          ticketCount.setNamedParameter("scholarshipId",
              ticketOrder.getEhcmEmpScholarship().getId());
          ticketCount.setNamedParameter("tktOrderId", ticketOrder.getId());
          if (ticketCount.list().size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_Ticketorder_SCUniqueValidation"));
          }
        }
      }

    } catch (OBException e) {
      log.error(" Exception while Ticket Order event   " + e);
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
      EHCMticketordertransaction ticketOrder = (EHCMticketordertransaction) event
          .getTargetInstance();
      if (ticketOrder.getDecisionStatus().equals("PR")) {
        throw new OBException(OBMessageUtils.messageBD("ehcm_process_delete"));
      }

    } catch (OBException e) {
      log.error(" Exception while Ticket Order event   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
