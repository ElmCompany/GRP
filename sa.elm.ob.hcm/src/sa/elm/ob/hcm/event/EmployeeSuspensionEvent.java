package sa.elm.ob.hcm.event;

import java.util.ArrayList;
import java.util.List;

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

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmSuspensionEmpV;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;

/**
 * 
 * @author gopalakrishnan on 15/12/2016
 * 
 */

public class EmployeeSuspensionEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EmployeeSuspension.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      EmployeeSuspension suspension = (EmployeeSuspension) event.getTargetInstance();
      final Property suspensionType = entities[0]
          .getProperty(EmployeeSuspension.PROPERTY_SUSPENSIONTYPE);
      final Property decisionType = entities[0]
          .getProperty(EmployeeDelegation.PROPERTY_DECISIONTYPE);
      String strSuspesionType = (String) event.getCurrentState(suspensionType);
      final Property employee = entities[0].getProperty(EmployeeDelegation.PROPERTY_EHCMEMPPERINFO);
      final Property startDate = entities[0].getProperty(EmployeeDelegation.PROPERTY_STARTDATE);
      List<EhcmEmpPerInfo> empList = new ArrayList<EhcmEmpPerInfo>();
      boolean isExist = false;
      AssingedOrReleaseEmpInPositionDAO positionEmpHist = new AssingedOrReleaseEmpInPositionDAOImpl();
      EhcmSuspensionEmpV suspensionView = suspension.getEhcmEmpPerinfo();

      // check employee is terminated or not
      if (!event.getCurrentState(decisionType).equals(event.getPreviousState(decisionType))) {
        if (event.getCurrentState(decisionType).toString().equals("CR")) {
          OBQuery<EhcmEmpPerInfo> terQuery = OBDal.getInstance().createQuery(EhcmEmpPerInfo.class,
              "as e where e.cancelEmployee.id=:empId and e.employmentStatus='TE'");
          terQuery.setNamedParameter("empId", suspension.getEhcmEmpPerinfo().getId());
          empList = terQuery.list();
          if (empList.size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Employee_Terminated"));
          }
        }
      }

      // check original decision number
      if (!event.getCurrentState(decisionType).equals(event.getPreviousState(decisionType))) {
        if (suspension.getDecisionType().equals("CA")
            || suspension.getDecisionType().equals("UP")) {
          if (suspension.getOriginalDecisionNo() == null) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Old_Suspension_Blank"));
          }
        }
      }
      // check end Date and join date
      if (suspension.getEndDate() != null && suspension.getJoinDate() != null) {
        if (suspension.getEndDate().compareTo(suspension.getJoinDate()) > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_EndDate_GrrThan_JoinDate"));
        }
      }
      // check start date and end date
      if (suspension.getStartDate() != null && suspension.getEndDate() != null) {
        if (suspension.getStartDate().compareTo(suspension.getEndDate()) > 0) {
          throw new OBException(
              OBMessageUtils.messageBD("Ehcm_EndDate_GrrThan_StartDate(Suspend)"));
        }
      }
      // check Start Date and Expected End Date
      if (suspension.getStartDate() != null && suspension.getExpectedEndDate() != null) {
        if (suspension.getStartDate().compareTo(suspension.getExpectedEndDate()) > 0) {
          throw new OBException(
              OBMessageUtils.messageBD("Ehcm_ExpEndDate_GrrThan_StartDate(Suspend)"));
        }
      }
      // check start date with employee start date
      if (!event.getPreviousState(startDate).equals(event.getCurrentState(startDate))) {
        if (event.getCurrentState(startDate) != null) {
          if (suspension.getStartDate()
              .compareTo(suspensionView.getEmployee().getStartDate()) <= 0) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_EmpSuspension_StartDate"));
          }
        }
      }
      // check start date with current active employment startdate
      if (!event.getPreviousState(startDate).equals(event.getCurrentState(startDate))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (strSuspesionType.equals("SUS") && suspension.getDecisionType().equals("CR")) {
          OBQuery<EmploymentInfo> empInfoQuery = OBDal.getInstance().createQuery(
              EmploymentInfo.class,
              " ehcmEmpPerinfo.id=:empId and enabled='Y' and (alertStatus='ACT') order by created desc ");
          empInfoQuery.setNamedParameter("empId", suspension.getEhcmEmpPerinfo().getId());
          EmploymentInfo empInfo = empInfoQuery.list().get(0);
          if (empInfoQuery.list().size() > 0) {
            if (suspension.getStartDate().compareTo(empInfo.getStartDate()) <= 0) {
              throw new OBException(OBMessageUtils.messageBD("Ehcm_EmpSuspension_StartDate"));
            }
          }

        }
      }
      if (!event.getCurrentState(employee).equals(event.getPreviousState(employee))) {
        if (suspension.getDecisionType().equals("CR")
            && suspension.getDecisionType().equals("SUS")) {
          OBQuery<EmployeeSuspension> suspensionQuery = OBDal.getInstance().createQuery(
              EmployeeSuspension.class,
              "as e where e.ehcmEmpPerinfo.id=:empId and e.enabled='Y' and e.suspensionType='SUS' and e.decisionStatus<>'UP'");
          suspensionQuery.setNamedParameter("empId", suspension.getEhcmEmpPerinfo().getId());
          if (suspensionQuery.list().size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Suspension_Not_Possible"));
          }
        }
      }

      if (strSuspesionType.equals("SUS")) {
        if (suspension.getSuspensionReason() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Suspension_Reason(Empty)"));
        }
      }
      if (strSuspesionType.equals("SUE")) {
        if (suspension.getSuspensionEndReason() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Suspension_EndReason(Empty)"));
        }
        if (suspension.getEndDate() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Suspension_EndDate(Empty)"));
        }
        if (suspension.getOriginalDecisionNo() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Suspension_decisionno(Empty)"));
        }
      }

      // check original decision number
      if (suspension.getDecisionType().equals("CA")) {
        if (suspension.getCancelDate() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Delegation_CancelDate"));
        }
      }
      /*
       * // check termination reason defined if (suspension.getSuspensionEndReason().equals("T") ||
       * suspension.getSuspensionEndReason().equals("TRD")) { OBQuery<EHCMTerminationReason>
       * objTerminationReason = OBDal.getInstance() .createQuery( EHCMTerminationReason.class,
       * "as e where e.searchKey='LDS' and e.client.id='" + suspension.getClient().getId() + "'");
       * if (objTerminationReason.list().size() == 0) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_TerminationReasonNotDefined(LDR)")); } }
       */
      if (suspension.getSuspensionType().equals("SUE")
          && suspension.getSuspensionEndReason() != null
          && (suspension.getSuspensionEndReason().getSearchKey().equals("T")
              || suspension.getSuspensionEndReason().getSearchKey().equals("TRD"))) {
        isExist = positionEmpHist.checkDelegatedRecordwithGreaterthanStartDate(
            suspensionView.getEmployee(), suspension.getEndDate());
        if (isExist) {
          // throw new OBException(OBMessageUtils.messageBD("EHCM_CancelDelegation"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while updating Employeee Suspension  " + e);
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
      EmployeeSuspension suspension = (EmployeeSuspension) event.getTargetInstance();
      final Property suspensionType = entities[0]
          .getProperty(EmployeeSuspension.PROPERTY_SUSPENSIONTYPE);
      String strSuspesionType = (String) event.getCurrentState(suspensionType);
      boolean isExist = false;
      AssingedOrReleaseEmpInPositionDAO positionEmpHist = new AssingedOrReleaseEmpInPositionDAOImpl();
      EhcmSuspensionEmpV suspensionView = suspension.getEhcmEmpPerinfo();
      // check delegation already associated
      if (suspension.getDecisionType().equals("CR")
          && suspension.getSuspensionType().equals("SUS")) {
        OBQuery<EmployeeSuspension> suspensionQuery = OBDal.getInstance().createQuery(
            EmployeeSuspension.class,
            "as e where e.ehcmEmpPerinfo.id=:empId and e.enabled='Y' and e.suspensionType='SUS' and e.decisionStatus<>'UP'");
        suspensionQuery.setNamedParameter("empId", suspension.getEhcmEmpPerinfo().getId());
        if (suspensionQuery.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Suspension_Not_Possible"));
        }
      }
      // check employee is terminated or not
      if (suspension.getDecisionType().equals("CR")) {
        OBQuery<EhcmEmpPerInfo> terQuery = OBDal.getInstance().createQuery(EhcmEmpPerInfo.class,
            "as e where e.cancelEmployee.id=:empId and e.employmentStatus='TE'");
        terQuery.setNamedParameter("empId", suspension.getEhcmEmpPerinfo().getId());
        if (terQuery.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Employee_Terminated"));
        }
      }
      // check end Date and join date
      if (suspension.getEndDate() != null && suspension.getJoinDate() != null) {
        if (suspension.getEndDate().compareTo(suspension.getJoinDate()) > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_EndDate_GrrThan_JoinDate"));
        }
      }
      // check start date and end date
      if (suspension.getStartDate() != null && suspension.getEndDate() != null) {
        if (suspension.getStartDate().compareTo(suspension.getEndDate()) > 0) {
          throw new OBException(
              OBMessageUtils.messageBD("Ehcm_EndDate_GrrThan_StartDate(Suspend)"));
        }
      }
      // check Start Date and Expected End Date
      if (suspension.getStartDate() != null && suspension.getExpectedEndDate() != null) {
        if (suspension.getStartDate().compareTo(suspension.getExpectedEndDate()) > 0) {
          throw new OBException(
              OBMessageUtils.messageBD("Ehcm_ExpEndDate_GrrThan_StartDate(Suspend)"));
        }
      }
      // check start date with employee start date
      if (suspension.getStartDate() != null) {
        if (suspension.getStartDate().compareTo(suspensionView.getEmployee().getStartDate()) <= 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_EmpSuspension_StartDate"));
        }
      }
      // check start date with current active employment startdate
      if (strSuspesionType.equals("SUS") && suspension.getDecisionType().equals("CR")) {
        OBQuery<EmploymentInfo> empInfoQuery = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id=:empId and enabled='Y'  and (alertStatus='ACT') order by created desc ");
        empInfoQuery.setNamedParameter("empId", suspension.getEhcmEmpPerinfo().getId());
        EmploymentInfo empInfo = empInfoQuery.list().get(0);
        if (empInfoQuery.list().size() > 0) {
          if (suspension.getStartDate().compareTo(empInfo.getStartDate()) <= 0) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_EmpSuspension_StartDate"));
          }
        }

      }
      if (strSuspesionType.equals("SUS")) {
        if (suspension.getSuspensionReason() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Suspension_Reason(Empty)"));
        }
      }
      if (strSuspesionType.equals("SUE")) {
        if (suspension.getSuspensionEndReason() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Suspension_EndReason(Empty)"));
        }
        if (suspension.getEndDate() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Suspension_EndDate(Empty)"));
        }
        if (suspension.getOriginalDecisionNo() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Suspension_decisionno(Empty)"));
        }
      }
      // check original decision number
      if (suspension.getDecisionType().equals("CA") || suspension.getDecisionType().equals("UP")) {
        if (suspension.getOriginalDecisionNo() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Old_Suspension_Blank"));
        }
      }

      // check original decision number
      if (suspension.getDecisionType().equals("CA")) {
        if (suspension.getCancelDate() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Delegation_CancelDate"));
        }
      }
      // check termination reason defined
      /*
       * if (suspension.getSuspensionEndReason().equals("T") ||
       * suspension.getSuspensionEndReason().equals("TRD")) { OBQuery<EHCMTerminationReason>
       * objTerminationReason = OBDal.getInstance() .createQuery( EHCMTerminationReason.class,
       * "as e where e.searchKey='LDS' and e.client.id='" + suspension.getClient().getId() + "'");
       * if (objTerminationReason.list().size() == 0) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_TerminationReasonNotDefined(LDR)")); } }
       */

      if (suspension.getSuspensionType().equals("SUE")
          && suspension.getSuspensionEndReason() != null
          && (suspension.getSuspensionEndReason().getSearchKey().equals("T")
              || suspension.getSuspensionEndReason().getSearchKey().equals("TRD"))) {
        isExist = positionEmpHist.checkDelegatedRecordwithGreaterthanStartDate(
            suspensionView.getEmployee(), suspension.getEndDate());
        if (isExist) {
          // throw new OBException(OBMessageUtils.messageBD("EHCM_CancelDelegation"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Employee Suspension   " + e);
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
      EmployeeSuspension suspension = (EmployeeSuspension) event.getTargetInstance();
      if (suspension.getDecisionStatus().equals("I")) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Suspension_Issued"));
      }

    } catch (OBException e) {
      log.error(" Exception while Deleting Employee Suspension : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
