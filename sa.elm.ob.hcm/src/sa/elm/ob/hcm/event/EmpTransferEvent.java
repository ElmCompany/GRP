package sa.elm.ob.hcm.event;

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
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmpTransfer;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;

public class EmpTransferEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEmpTransfer.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      OBContext.setAdminMode();
      String empInfoId = "";
      EmploymentInfo employinfo = null;
      EHCMEmpTransfer transfer = (EHCMEmpTransfer) event.getTargetInstance();
      Boolean chkPositionAvailableOrNot = false;
      OBQuery<EmploymentInfo> emplyinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId()
              + "' and e.enabled='Y'");
      if (emplyinfo.list().size() > 0) {
        employinfo = emplyinfo.list().get(0);
        empInfoId = employinfo.getId();
      }
      final Property transferType = entities[0].getProperty(EHCMEmpTransfer.PROPERTY_TRANSFERTYPE);
      final Property decisionType = entities[0].getProperty(EHCMEmpTransfer.PROPERTY_DECISIONTYPE);
      final Property startdate = entities[0].getProperty(EHCMEmpTransfer.PROPERTY_STARTDATE);
      final Property enddate = entities[0].getProperty(EHCMEmpTransfer.PROPERTY_ENDDATE);
      final Property person = entities[0].getProperty(EHCMEmpTransfer.PROPERTY_EHCMEMPPERINFO);
      final Property positionObj = entities[0]
          .getProperty(EHCMEmpTransfer.PROPERTY_NEWEHCMPOSITION);
      if (!event.getPreviousState(person).equals(event.getCurrentState(person))) {
        if (transfer.getEhcmEmpPerinfo() != null) {
          if ((employinfo.getChangereason().equals("SEC")
              || employinfo.getChangereason().equals("EXSEC")) && employinfo.isEnabled()) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_DontTransfer"));
          }
        }
      }

      if (!event.getPreviousState(transferType).equals(event.getCurrentState(transferType))) {
        if (transfer.getTransferType().equals("OD")) {
          if (transfer.getNewDepartmentCode() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransferDeptMand"));
        }
      }
      if (!event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (transfer.getDecisionType().equals("CA")) {
          if (transfer.getEndDate() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransferEndDate"));
        }
        log.debug("reason:" + employinfo.getChangereason());
        if (transfer.getDecisionType().equals("UP") || transfer.getDecisionType().equals("CA")) {
          if (employinfo.getChangereason().equals("H") && employinfo.isEnabled()) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_CantUpdate"));
          }
        }
        if (transfer.getDecisionType().equals("CR")) {

          if (transfer.getDecisionNo() == null) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Emptran_Decno"));
          }
        }
        if (transfer.getDecisionType().equals("CA") || transfer.getDecisionType().equals("UP")) {
          if (transfer.getOriginalDecisionsNo() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
        }
        if (transfer.getDecisionType().equals("CR") || transfer.getDecisionType().equals("UP")) {
          if (transfer.getNEWEhcmPosition() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_NewPos"));
        }
      }
      if (transfer.getEndDate() != null) {
        log.debug("getEndDate:" + transfer.getEndDate());
        if (event.getPreviousState(enddate) != null) {
          if (!event.getPreviousState(enddate).equals(event.getCurrentState(enddate))) {
            if (transfer.getEndDate().compareTo(transfer.getStartDate()) == -1
                || transfer.getEndDate().compareTo(transfer.getStartDate()) == 0) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_StartDateGreThan_EndDate"));
            }
          }
        } else {
          log.debug("getEndDateelse:" + transfer.getEndDate());
          log.debug("compareTo:" + transfer.getEndDate().compareTo(transfer.getStartDate()));
          if (transfer.getEndDate().compareTo(transfer.getStartDate()) == -1
              || transfer.getEndDate().compareTo(transfer.getStartDate()) == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_StartDateGreThan_EndDate"));
          }
        }
      }
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (transfer.getStartDate() != null && !transfer.getDecisionType().equals("CA")) {
          OBQuery<EmploymentInfo> info = OBDal.getInstance()
              .createQuery(EmploymentInfo.class, " ehcmEmpPerinfo.id='"
                  + transfer.getEhcmEmpPerinfo().getId()
                  + "' and ( ehcmEmpTransfer.id is null or ehcmEmpTransfer.id <>:empTransferId ) order by creationDate desc ");
          if (transfer.getDecisionType().equals("CR"))
            info.setNamedParameter("empTransferId", transfer.getId());
          if (transfer.getDecisionType().equals("UP"))
            info.setNamedParameter("empTransferId", transfer.getOriginalDecisionsNo().getId());
          info.setMaxResult(1);
          EmploymentInfo empinfo = info.list().get(0);
          if (transfer.getStartDate().compareTo(empinfo.getStartDate()) == -1
              || transfer.getStartDate().compareTo(empinfo.getStartDate()) == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_StartDate"));
          }
        }
      }

      if (!event.getPreviousState(person).equals(event.getCurrentState(person))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        // cant able to cancel the transfer old position is not free
        if (transfer.getDecisionType().equals("CA")) {
          if (transfer.getEhcmEmpPerinfo() != null) {
            OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
                " ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId()
                    + "' and enabled='N' order by creationDate desc ");
            info.setMaxResult(1);
            if (info.list().size() > 0) {
              EmploymentInfo empinfo = info.list().get(0);
              if (empinfo.getPosition() != null) {
                chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO
                    .chkPositionAvailableOrNot(transfer.getEhcmEmpPerinfo(), empinfo.getPosition(),
                        transfer.getStartDate(), null, transfer.getDecisionType(), false);
                if (chkPositionAvailableOrNot) {
                  throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTrans_CantCancel"));
                }

                /*
                 * if (empinfo.getPosition().getAssignedEmployee() != null) { if
                 * (!empinfo.getPosition().getAssignedEmployee().getId()
                 * .equals(transfer.getEhcmEmpPerinfo().getId())) { throw new
                 * OBException(OBMessageUtils.messageBD("EHCM_EmpTrans_CantCancel")); } }
                 */
              }
            }
          }
          // checking delegation exists
          if (transfer.getEhcmEmpPerinfo() != null) {
            OBQuery<EmployeeDelegation> del = OBDal.getInstance()
                .createQuery(EmployeeDelegation.class, " ehcmEmploymentInfo.id='"
                    + employinfo.getId() + "' and enabled='Y'  order by creationDate desc");
            if (del.list().size() > 0) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_HaveDeleg"));
            }
          }
          // check position is in issued
          if (transfer.getEhcmEmpPerinfo() != null) {
            OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
                " ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId()
                    + "' and enabled='N' order by creationDate desc ");
            info.setMaxResult(1);
            if (info.list().size() > 0) {
              EmploymentInfo empinfo = info.list().get(0);
              if (!empinfo.getPosition().getTransactionStatus().equals("I")) {
                throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_PosNotIssue"));
              }
            }
          }
        }
      }

      if (!event.getPreviousState(person).equals(event.getCurrentState(person))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))
          || !event.getPreviousState(positionObj).equals(event.getCurrentState(positionObj))
          || !event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || (event.getPreviousState(enddate) != null
              && !event.getPreviousState(enddate).equals(event.getCurrentState(enddate)))) {
        if (transfer.getNEWEhcmPosition() != null && !transfer.getDecisionType().equals("CA")) {
          EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
              transfer.getNEWEhcmPosition().getId());
          chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO.chkPositionAvailableOrNot(
              transfer.getEhcmEmpPerinfo(), position, transfer.getStartDate(),
              transfer.getEndDate(), transfer.getDecisionType(), false);
          if (chkPositionAvailableOrNot) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_PosNotAvailable"));
          }
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Employee Transfer  " + e);
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
      String empInfoId = "";
      EmploymentInfo employinfo = null;
      EHCMEmpTransfer transfer = (EHCMEmpTransfer) event.getTargetInstance();
      Boolean chkPositionAvailableOrNot = false;

      OBQuery<EmploymentInfo> emplyinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId()
              + "' and e.enabled='Y'");
      if (emplyinfo.list().size() > 0) {
        employinfo = emplyinfo.list().get(0);
        empInfoId = employinfo.getId();
      }
      if (transfer.getTransferType().equals("OD")) {
        if (transfer.getNewDepartmentCode() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransferDeptMand"));
      }
      if (transfer.getDecisionType().equals("CA")) {
        if (transfer.getEndDate() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransferEndDate"));
      }
      if (transfer.getDecisionType().equals("CR")) {

        if (transfer.getDecisionNo() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Emptran_Decno"));
        }
      }
      log.debug("reason:" + employinfo.getChangereason());
      if (transfer.getDecisionType().equals("UP") || transfer.getDecisionType().equals("CA")) {
        if (employinfo.getChangereason().equals("H") && employinfo.isEnabled()) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_CantUpdate"));
        }
      }
      if (transfer.getDecisionType().equals("CA") || transfer.getDecisionType().equals("UP")) {
        if (transfer.getOriginalDecisionsNo() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
      }
      if (transfer.getDecisionType().equals("CR") || transfer.getDecisionType().equals("UP")) {
        if (transfer.getNEWEhcmPosition() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_NewPos"));
      }
      if (!transfer.getDecisionType().equals("CA")) {
        if (transfer.getStartDate() != null) {
          OBQuery<EmploymentInfo> info = OBDal.getInstance()
              .createQuery(EmploymentInfo.class, " ehcmEmpPerinfo.id='"
                  + transfer.getEhcmEmpPerinfo().getId()
                  + "'  and ( ehcmEmpTransfer.id is null or ehcmEmpTransfer.id <>:empTransferId ) order by creationDate desc ");
          if (transfer.getDecisionType().equals("CR"))
            info.setNamedParameter("empTransferId", transfer.getId());
          if (transfer.getDecisionType().equals("UP"))
            info.setNamedParameter("empTransferId", transfer.getOriginalDecisionsNo().getId());
          info.setMaxResult(1);
          EmploymentInfo empinfo = info.list().get(0);
          if (transfer.getStartDate().compareTo(empinfo.getStartDate()) == -1
              || transfer.getStartDate().compareTo(empinfo.getStartDate()) == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_StartDate"));
          }
          log.debug("startdate:" + employinfo.getStartDate());
        }
      }
      if (transfer.getEndDate() != null) {
        log.debug("getEndDateelsesave:" + transfer.getEndDate());
        log.debug("compareTsosave:" + transfer.getEndDate().compareTo(transfer.getStartDate()));
        if (transfer.getEndDate().compareTo(transfer.getStartDate()) == -1
            || transfer.getEndDate().compareTo(transfer.getStartDate()) == 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_StartDateGreThan_EndDate"));
        }
      }
      // }
      if (transfer.getEhcmEmpPerinfo() != null) {
        if ((employinfo.getChangereason().equals("SEC")
            || employinfo.getChangereason().equals("EXSEC")) && employinfo.isEnabled()) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_DontTransfer"));
        }
      }
      // cant able to cancel the transfer old position is not free
      if (transfer.getDecisionType().equals("CA")) {
        if (transfer.getEhcmEmpPerinfo() != null) {
          OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId()
                  + "' and enabled='N' order by creationDate desc ");
          info.setMaxResult(1);
          if (info.list().size() > 0) {
            EmploymentInfo empinfo = info.list().get(0);
            if (empinfo.getPosition() != null) {
              chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO
                  .chkPositionAvailableOrNot(transfer.getEhcmEmpPerinfo(), empinfo.getPosition(),
                      transfer.getStartDate(), null, transfer.getDecisionType(), false);
              if (chkPositionAvailableOrNot) {
                throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTrans_CantCancel"));
              }
              /*
               * if (empinfo.getPosition().getAssignedEmployee() != null) { if
               * (!empinfo.getPosition().getAssignedEmployee().getId()
               * .equals(transfer.getEhcmEmpPerinfo().getId())) { throw new
               * OBException(OBMessageUtils.messageBD("EHCM_EmpTrans_CantCancel")); } }
               */
            }
          }
        }
        // checking delegation exists

        if (transfer.getEhcmEmpPerinfo() != null) {
          OBQuery<EmployeeDelegation> del = OBDal.getInstance()
              .createQuery(EmployeeDelegation.class, " ehcmEmploymentInfo.id='" + employinfo.getId()
                  + "' and enabled='Y'  order by creationDate desc");
          if (del.list().size() > 0) {
            // throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_HaveDeleg"));
          }
        }

        // check position is in issued
        if (transfer.getEhcmEmpPerinfo() != null) {
          OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId()
                  + "' and enabled='N' order by creationDate desc ");
          info.setMaxResult(1);
          if (info.list().size() > 0) {
            EmploymentInfo empinfo = info.list().get(0);
            if (!empinfo.getPosition().getTransactionStatus().equals("I")) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_PosNotIssue"));
            }
          }
        }
      }
      if (transfer.getNEWEhcmPosition() != null && !transfer.getDecisionType().equals("CA")) {
        EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
            transfer.getNEWEhcmPosition().getId());
        chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO.chkPositionAvailableOrNot(
            transfer.getEhcmEmpPerinfo(), position, transfer.getStartDate(), transfer.getEndDate(),
            transfer.getDecisionType(), false);
        if (chkPositionAvailableOrNot) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_PosNotAvailable"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Employee transfer   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMEmpTransfer transfer = (EHCMEmpTransfer) event.getTargetInstance();
      if (transfer.getDecisionStatus().equals("I")) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Transfer_Issued"));
      }

    } catch (OBException e) {
      log.error(" Exception while Deleting Delegation : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
