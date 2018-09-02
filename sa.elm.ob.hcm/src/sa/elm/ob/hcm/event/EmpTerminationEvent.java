package sa.elm.ob.hcm.event;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
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

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EhcmterminationEmpV;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.utility.util.UtilityDAO;

public class EmpTerminationEvent extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEMPTermination.ENTITY_NAME) };

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
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      String empInfoId = "";
      EmploymentInfo employinfo = null;
      EHCMEMPTermination termination = (EHCMEMPTermination) event.getTargetInstance();
      Boolean chkPositionAvailableOrNot = false;
      final Property decisionType = entities[0]
          .getProperty(EHCMEMPTermination.PROPERTY_DECISIONTYPE);
      final Property terminationreason = entities[0]
          .getProperty(EHCMEMPTermination.PROPERTY_EHCMTERMINATIONREASON);
      final Property terminationdate = entities[0]
          .getProperty(EHCMEMPTermination.PROPERTY_TERMINATIONDATE);
      final Property canceldate = entities[0].getProperty(EHCMEMPTermination.PROPERTY_CANCELDATE);
      final Property person = entities[0].getProperty(EHCMEMPTermination.PROPERTY_EHCMEMPPERINFO);
      Date terminationDateAfter = null;
      int millSec = 1 * 24 * 3600 * 1000;
      EhcmterminationEmpV terminationView = termination.getEhcmEmpPerinfo();

      OBQuery<EmploymentInfo> emplyinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
              + "' and e.enabled='Y'");
      if (emplyinfo.list().size() > 0) {
        employinfo = emplyinfo.list().get(0);
        empInfoId = employinfo.getId();
      }

      if (!event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        // cant able to create update/cancel for hiring employment
        if (termination.getDecisionType().equals("UP")
            || termination.getDecisionType().equals("CA")) {
          if (employinfo.getChangereason().equals("H") && employinfo.isEnabled()) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_CantUpdate"));
          }
        }
        if (termination.getDecisionType().equals("CR")) {
          if (employinfo.getChangereason().equals("T") && employinfo.isEnabled()) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTerm_CantCreate"));
          }
        }
        if (termination.getDecisionType().equals("CA")
            || termination.getDecisionType().equals("UP")) {
          if (termination.getOriginalDecisionsNo() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
        }
      }
      if (!event.getPreviousState(terminationdate).equals(event.getCurrentState(terminationdate))) {
        if (termination.getDecisionType().equals("CA")) {
          if (termination.getCancelDate().compareTo(termination.getTerminationDate()) == -1
              || termination.getCancelDate().compareTo(termination.getTerminationDate()) == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpProm_CancelDate"));
          }
        }
      }
      if (!event.getPreviousState(terminationdate).equals(event.getCurrentState(terminationdate))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (termination.getDecisionType().equals("CR")
            || termination.getDecisionType().equals("UP")) {
          if (termination.getTerminationDate() != null) {
            OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
                " ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
                    + "' and  ehcmEmpTermination.id is null order by creationDate desc ");
            info.setMaxResult(1);
            if (info.list().size() > 0) {
              EmploymentInfo empinfo = info.list().get(0);
              if (termination.getEhcmEmpSuspension() == null) {
                if (termination.getTerminationDate().compareTo(empinfo.getStartDate()) <= 0) {
                  throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
                }
              }
            }
          }
        }
        if (termination.getDecisionType().equals("CA")) {
          if (!event.getPreviousState(canceldate).equals(event.getCurrentState(canceldate))) {
            if (termination.getCancelDate().compareTo(termination.getTerminationDate()) == -1
                || termination.getCancelDate().compareTo(termination.getTerminationDate()) == 0) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_EmpProm_CancelDate"));
            }
          }
        }
      }

      if (!event.getPreviousState(person).equals(event.getCurrentState(person))) {
        if (termination.getEhcmTerminationReason().getSearchKey().equals("RE")
            || termination.getEhcmTerminationReason().getSearchKey().equals("ER")) {
          if (employinfo.getChangereason().equals("SUS")) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EndofEmp_Sus"));
          }
        }
      }
      if (!event.getPreviousState(person).equals(event.getCurrentState(person))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        // cant able to cancel the termination old position is not free
        if (termination.getDecisionType().equals("CA")) {
          if (termination.getEhcmEmpPerinfo() != null) {
            OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
                " ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
                    + "' and enabled='N' order by creationDate desc ");
            info.setMaxResult(1);
            if (info.list().size() > 0) {
              EmploymentInfo empinfo = info.list().get(0);
              if (empinfo.getPosition() != null) {
                terminationDateAfter = new Date(
                    termination.getTerminationDate().getTime() + millSec);
                chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO
                    .chkPositionAvailableOrNot(terminationView.getEhcmEmpPerinfo(),
                        empinfo.getPosition(), terminationDateAfter, null,
                        termination.getDecisionType(), false);
                if (chkPositionAvailableOrNot) {
                  throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTerm_CantCancel"));
                }
                /*
                 * if (empinfo.getPosition().getAssignedEmployee() != null) { if
                 * (!empinfo.getPosition().getAssignedEmployee().getId()
                 * .equals(termination.getEhcmEmpPerinfo().getId())) { throw new
                 * OBException(OBMessageUtils.messageBD("EHCM_EmpTerm_CantCancel")); } }
                 */
              }
            }
          }
        }
      }
      if (!event.getPreviousState(terminationreason)
          .equals(event.getCurrentState(terminationreason))
          || !event.getPreviousState(terminationdate)
              .equals(event.getCurrentState(terminationdate))) {
        if (termination.getEhcmTerminationReason() != null) {
          if (termination.getEhcmTerminationReason().getSearchKey().equals("AR")) {

            int age = UtilityDAO.calculateMonths(
                convertTohijriDate(
                    dateYearFormat.format(terminationView.getEhcmEmpPerinfo().getDob())),
                convertTohijriDate(dateYearFormat.format(termination.getTerminationDate())),
                termination.getClient().getId(), false);
            log.debug("age:" + age);
            if (age < 720) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTerm_AR60"));
            }
          }
        }

      }

    } catch (OBException e) {
      log.error(" Exception while updating End of Employment  " + e);
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
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      String empInfoId = "";
      EmploymentInfo employinfo = null;
      Date terminationDateAfter = null;
      int millSec = 1 * 24 * 3600 * 1000;
      EHCMEMPTermination termination = (EHCMEMPTermination) event.getTargetInstance();
      EhcmterminationEmpV terminationView = termination.getEhcmEmpPerinfo();
      Boolean chkPositionAvailableOrNot = false;
      OBQuery<EmploymentInfo> emplyinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
              + "' and e.enabled='Y'");
      if (emplyinfo.list().size() > 0) {
        employinfo = emplyinfo.list().get(0);
        empInfoId = employinfo.getId();
      }

      // cant able to create update/cancel for hiring employment
      if (termination.getDecisionType().equals("UP")
          || termination.getDecisionType().equals("CA")) {
        if (employinfo.getChangereason().equals("H") && employinfo.isEnabled()) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_CantUpdate"));
        }
        if (termination.getDecisionType().equals("CR")) {
          if (employinfo.getChangereason().equals("T") && employinfo.isEnabled()) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTerm_CantCreate"));
          }
        }

        if (termination.getOriginalDecisionsNo() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
      }
      if (termination.getDecisionType().equals("CA")) {
        if (termination.getCancelDate().compareTo(termination.getTerminationDate()) == -1
            || termination.getCancelDate().compareTo(termination.getTerminationDate()) == 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpProm_CancelDate"));
        }
      }
      if (termination.getTerminationDate() != null) {
        OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
                + "' and ehcmEmpTermination.id is null  order by creationDate desc ");
        log.debug("termination:" + termination.getId());
        info.setMaxResult(1);
        log.debug("size:" + info.list().size());
        if (info.list().size() > 0) {
          EmploymentInfo empinfo = info.list().get(0);
          log.debug("emplymnetId:" + empinfo.getId());
          log.debug("StartDate:" + empinfo.getStartDate());
          log.debug("termindationDate:" + termination.getTerminationDate());
          if (termination.getEhcmEmpSuspension() == null) {
            if (termination.getTerminationDate().compareTo(empinfo.getStartDate()) <= 0) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
            }
          }

        }
        /*
         * if (employinfo.getChangereason().equals("SUS") ||
         * employinfo.getChangereason().equals("SUE")) { throw new
         * OBException(OBMessageUtils.messageBD("EHCM_EndofEmp_Sus")); }
         */
      }
      // cant able to cancel the termination old position is not free
      if (termination.getDecisionType().equals("CA")) {

        if (termination.getEhcmEmpPerinfo() != null) {
          OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
                  + "' and enabled='N' order by creationDate desc ");
          info.setMaxResult(1);
          if (info.list().size() > 0) {
            EmploymentInfo empinfo = info.list().get(0);
            if (empinfo.getPosition() != null) {
              terminationDateAfter = new Date(termination.getTerminationDate().getTime() + millSec);
              chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO
                  .chkPositionAvailableOrNot(terminationView.getEhcmEmpPerinfo(),
                      empinfo.getPosition(), terminationDateAfter, null,
                      termination.getDecisionType(), false);
              if (chkPositionAvailableOrNot) {
                throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTerm_CantCancel"));
              }
              /*
               * if (empinfo.getPosition().getAssignedEmployee() != null) { if
               * (!empinfo.getPosition().getAssignedEmployee().getId()
               * .equals(termination.getEhcmEmpPerinfo().getId())) { throw new
               * OBException(OBMessageUtils.messageBD("EHCM_EmpTerm_CantCancel")); } }
               */
            }
          }
        }

      }
      if (termination.getEhcmTerminationReason() != null) {
        if (termination.getEhcmTerminationReason().getSearchKey().equals("AR")) {

          int age = UtilityDAO.calculateMonths(
              convertTohijriDate(
                  dateYearFormat.format(terminationView.getEhcmEmpPerinfo().getDob())),
              convertTohijriDate(dateYearFormat.format(termination.getTerminationDate())),
              termination.getClient().getId(), false);
          log.debug("age:" + age);
          if (age < 720) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTerm_AR60"));
          }
        }
      }
    } catch (OBException e) {
      log.error(" Exception while creating End of Employment   " + e);
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
      EHCMEMPTermination termination = (EHCMEMPTermination) event.getTargetInstance();
      if (termination.getDecisionStatus().equals("I")) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Termination_Issued"));
      }

    } catch (OBException e) {
      log.error(" Exception while end of employment ondelete event : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public String convertTohijriDate(String gregDate) {
    String hijriDate = "";
    try {
      SQLQuery gradeQuery = OBDal.getInstance().getSession()
          .createSQLQuery("select eut_convert_to_hijri(to_char(to_timestamp('" + gregDate
              + "','YYYY-MM-DD HH24:MI:SS'),'YYYY-MM-DD  HH24:MI:SS'))");
      if (gradeQuery.list().size() > 0) {
        Object row = (Object) gradeQuery.list().get(0);
        hijriDate = (String) row;
        log.debug("ConvertedDate:" + (String) row);
      }
    }

    catch (final Exception e) {
      log.error("Exception in convertTohijriDate() in emptermination event Method : ", e);
      return "0";
    }
    return hijriDate;
  }
}
