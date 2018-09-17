package sa.elm.ob.hcm.event;

import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMBusMissionSummary;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMScholarshipSummary;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.empBusinessMission.EmpBusinessMissionDAOImpl;
import sa.elm.ob.hcm.util.Utility;

/**
 * @author divya on 28/02/2018
 */
public class EmpBusinessMissionEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEmpBusinessMission.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  final String Business_mission = "BM";

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      final Property decisionType = entities[0]
          .getProperty(EHCMEmpBusinessMission.PROPERTY_DECISIONTYPE);
      final Property startdate = entities[0].getProperty(EHCMEmpBusinessMission.PROPERTY_STARTDATE);
      final Property enddate = entities[0].getProperty(EHCMEmpBusinessMission.PROPERTY_ENDDATE);
      final Property missionDays = entities[0]
          .getProperty(EHCMEmpBusinessMission.PROPERTY_MISSIONDAYS);
      final Property canceldate = entities[0]
          .getProperty(EHCMEmpBusinessMission.PROPERTY_CANCELDATE);
      final Property orgDecisionNo = entities[0]
          .getProperty(EHCMEmpBusinessMission.PROPERTY_ORIGINALDECISIONNO);
      EHCMEmpBusinessMission empBusinessMission = (EHCMEmpBusinessMission) event
          .getTargetInstance();
      EmploymentInfo employinfo = null;
      Date startDate = null;
      String decisiontype = null;
      EHCMBusMissionSummary busMissionSumm = null;
      EHCMScholarshipSummary scholarshipSummary = null;
      /* current active employment details **/

      busMissionSumm = EmpBusinessMissionDAOImpl.getActiveBusMissionSummary(
          empBusinessMission.getEmployee().getId(),
          (empBusinessMission.getOriginalDecisionNo() != null
              ? empBusinessMission.getOriginalDecisionNo().getId()
              : null));
      if (busMissionSumm != null) {
        startDate = busMissionSumm.getEndDate();
        decisiontype = busMissionSumm.getDecisionType();
      }
      if (busMissionSumm == null || (busMissionSumm != null
          && busMissionSumm.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE))) {
        employinfo = EmpBusinessMissionDAOImpl
            .getActiveEmployInfo(empBusinessMission.getEmployee().getId());
        startDate = employinfo.getStartDate();
        decisiontype = employinfo.getChangereason();
      }
      // should not allow to create scholarship for same period
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(enddate).equals(event.getCurrentState(enddate))) {
        if (empBusinessMission.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || empBusinessMission.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
          JSONObject result = Utility.overlapWithDecisionsDate(Business_mission,
              sa.elm.ob.utility.util.Utility.formatDate(empBusinessMission.getStartDate()),
              sa.elm.ob.utility.util.Utility.formatDate(empBusinessMission.getEndDate()),
              empBusinessMission.getEmployee().getId());
          if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
            if (empBusinessMission.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
                || (empBusinessMission.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                    && result.has("businessMissionId") && !result.getString("businessMissionId")
                        .equals(empBusinessMission.getOriginalDecisionNo().getId()))) {
              if (result.has("errormsg"))
                throw new OBException(OBMessageUtils.messageBD(result.getString("errormsg")));
            }
          }
        }
      }
      // original decision no is mandatory
      if (!event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (!empBusinessMission.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          if (empBusinessMission.getOriginalDecisionNo() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
        }

        // // cant able to create cutoff for cutoff employment
        // if (empBusinessMission.getDecisionType()
        // .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        // if (decisiontype.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
        // && (employinfo.isEnabled() || (busMissionSumm != null && busMissionSumm.isEnabled())
        // || (scholarshipSummary != null && scholarshipSummary.isEnabled()))) {
        // throw new OBException(OBMessageUtils.messageBD("EHCM_EmpBussMss_CutoffDontAllow"));
        // }
        // }

      }
      // checking enddate should not be lesser than startdate
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(enddate).equals(event.getCurrentState(enddate))) {
        if (empBusinessMission.getEndDate().compareTo(empBusinessMission.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_ScholarShipEndStartDateComp"));
        }
      }
      // checking startdate should not be lesser than current employment startdate
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(enddate).equals(event.getCurrentState(enddate))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (empBusinessMission.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {

          if (empBusinessMission.getStartDate().compareTo(startDate) == -1
              || empBusinessMission.getStartDate().compareTo(startDate) == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_BusMission_StartDate"));
          }
        }
      }
      // Removed the cancel date validation
      // if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
      // || !event.getPreviousState(enddate).equals(event.getCurrentState(enddate))
      // || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
      // if (empBusinessMission.getDecisionType()
      // .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
      // /* get previous employment record */
      // // employinfo =
      // // EmpScholarshipTrainingDAOImpl.getPreviousEmploymentRecord(empBusinessMission);
      // if (empBusinessMission.getStartDate().compareTo(startDate) == -1
      // || empBusinessMission.getStartDate().compareTo(startDate) == 0) {
      // throw new OBException(OBMessageUtils.messageBD("EHCM_BusMission_StartDate"));
      // }
      // }
      // }
      // if ((event.getCurrentState(canceldate) != null
      // && !event.getCurrentState(canceldate).equals(event.getPreviousState(canceldate)))
      // || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
      // if (empBusinessMission.getDecisionType()
      // .equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
      // if (empBusinessMission.getCancelDate().compareTo(empBusinessMission.getStartDate()) == -1
      // || empBusinessMission.getCancelDate()
      // .compareTo(empBusinessMission.getStartDate()) == 0) {
      // throw new OBException(OBMessageUtils.messageBD("Ehcm_BusinessMission_CancelDate"));
      // }
      // }
      // }
      if ((event.getCurrentState(enddate) != null
          && !event.getCurrentState(enddate).equals(event.getPreviousState(enddate)))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (empBusinessMission.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          if (empBusinessMission.getOriginalDecisionNo() != null && (empBusinessMission
              .getOriginalDecisionNo().getEndDate().compareTo(empBusinessMission.getEndDate()) == -1
              || empBusinessMission.getOriginalDecisionNo().getEndDate()
                  .compareTo(empBusinessMission.getEndDate()) == 0)) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_CutOffEndDateGrtThanDecEndDate"));
          }
        }
      }

      if (!empBusinessMission.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
          && !empBusinessMission.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        if (empBusinessMission.getMissionBalance() == 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_MisBal_NotZero"));
        }
      }

      if (empBusinessMission.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
        if (empBusinessMission.getExtendEnddate() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ExtendendDate_Mandatory"));

        }
        if (empBusinessMission.getExtendMissionDay() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ExtendMissionday_Mandatory"));
        }
      }

      /*
       * if (empBusinessMission.getDecisionType()
       * .equals(DecisionTypeConstants.DECISION_TYPE_BUSINESSMISSION_PAYMENT)) { if
       * (empBusinessMission.getPaymentAmt() == null) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_BMAmount_Mandatory")); } else if
       * (empBusinessMission.getAdvancePercentage() == null) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_AdvPercentage_Mandatory")); } else if
       * (empBusinessMission.getPayrollPeriod() == null) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_payrollPeriod_Mandatory")); } else if
       * (empBusinessMission.getAdvanceAmount() == null) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_AdvAmount_Mandatory")); }
       * 
       * if (empBusinessMission.getPaymentAmt().compareTo(new BigDecimal(0)) == 0) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_Paymentamt_Zero")); } if
       * (empBusinessMission.getAdvanceAmount().compareTo(new BigDecimal(0)) == 0) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_Advamount_zero")); } if
       * (empBusinessMission.getAdvancePercentage().compareTo(new BigDecimal(0)) == 0) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_Advpercentage_Zero")); } }
       */

    } catch (OBException e) {
      log.error(" Exception while updating Employee Business Mission   " + e);
      throw new OBException(e.getMessage());
    } /*
       * catch (JSONException e) { // TODO Auto-generated catch block e.printStackTrace(); }
       */ catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @SuppressWarnings("unused")
  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EmploymentInfo employinfo = null;
      Date startDate = null;
      String decisionType = null;
      EHCMEmpBusinessMission empBusinessMission = (EHCMEmpBusinessMission) event
          .getTargetInstance();
      EHCMBusMissionSummary busMissionSumm = null;
      EHCMScholarshipSummary scholarshipSummary = null;

      /* current active employment details **/
      busMissionSumm = EmpBusinessMissionDAOImpl.getActiveBusMissionSummary(
          empBusinessMission.getEmployee().getId(),
          (empBusinessMission.getOriginalDecisionNo() != null
              ? empBusinessMission.getOriginalDecisionNo().getId()
              : null));
      if (busMissionSumm != null) {
        startDate = busMissionSumm.getEndDate();
        decisionType = busMissionSumm.getDecisionType();
      }
      if (busMissionSumm == null || (busMissionSumm != null
          && busMissionSumm.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE))) {
        employinfo = EmpBusinessMissionDAOImpl
            .getActiveEmployInfo(empBusinessMission.getEmployee().getId());
        startDate = employinfo.getStartDate();
        decisionType = employinfo.getChangereason();
      }
      // should not allow to create scholarship for same period
      if (empBusinessMission.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          || empBusinessMission.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        JSONObject result = Utility.overlapWithDecisionsDate(Business_mission,
            sa.elm.ob.utility.util.Utility.formatDate(empBusinessMission.getStartDate()),
            sa.elm.ob.utility.util.Utility.formatDate(empBusinessMission.getEndDate()),
            empBusinessMission.getEmployee().getId());
        if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
          if (empBusinessMission.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
              || (empBusinessMission.getDecisionType()
                  .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                  && result.has("businessMissionId") && !result.getString("businessMissionId")
                      .equals(empBusinessMission.getOriginalDecisionNo().getId()))) {
            if (result.has("errormsg"))
              throw new OBException(OBMessageUtils.messageBD(result.getString("errormsg")));
          }
        }
      }
      if (empBusinessMission.getEndDate().compareTo(empBusinessMission.getStartDate()) == -1) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_ScholarShipEndStartDateComp"));
      }

      // original decision no is mandatory
      if (!empBusinessMission.getDecisionType()
          .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        if (empBusinessMission.getOriginalDecisionNo() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
      }

      // checking startdate should not be lesser than current employment startdate
      if (empBusinessMission.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        if (empBusinessMission.getStartDate().compareTo(startDate) == -1
            || empBusinessMission.getStartDate().compareTo(startDate) == 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_BusMission_StartDate"));
        }
      }
      // if
      // (empBusinessMission.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
      // /* get previous employment record */
      // // EmploymentInfo employeeinfo =
      // // EmpScholarshipTrainingDAOImpl.getPreviousEmploymentRecord(empBusinessMission);
      //
      // if (empBusinessMission.getStartDate().compareTo(startDate) == -1
      // || empBusinessMission.getStartDate().compareTo(startDate) == 0) {
      // throw new OBException(OBMessageUtils.messageBD("EHCM_BusMission_StartDate"));
      // }
      // }
      // Removed the cancel date validation
      // if
      // (empBusinessMission.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
      // if (empBusinessMission.getCancelDate().compareTo(empBusinessMission.getStartDate()) == -1
      // || empBusinessMission.getCancelDate()
      // .compareTo(empBusinessMission.getStartDate()) == 0) {
      // throw new OBException(OBMessageUtils.messageBD("Ehcm_BusinessMission_CancelDate"));
      // }
      // }

      // cant able to create cutoff for cutoff employment
      if (empBusinessMission.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        // if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
        // && ((employinfo != null && employinfo.isEnabled())
        // || (busMissionSumm != null && busMissionSumm.isEnabled())
        // || (scholarshipSummary != null && scholarshipSummary.isEnabled()))) {
        // throw new OBException(OBMessageUtils.messageBD("EHCM_EmpBussMss_CutoffDontAllow"));
        // }

        if (empBusinessMission.getOriginalDecisionNo() != null && (empBusinessMission
            .getOriginalDecisionNo().getEndDate().compareTo(empBusinessMission.getEndDate()) == -1
            || empBusinessMission.getOriginalDecisionNo().getEndDate()
                .compareTo(empBusinessMission.getEndDate()) == 0)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_CutOffEndDateGrtThanDecEndDate"));
        }
      }
      if (!empBusinessMission.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
          && !empBusinessMission.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        if (empBusinessMission.getMissionBalance() == 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_MisBal_NotZero"));
        }
      }
      if (empBusinessMission.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
        if (empBusinessMission.getExtendEnddate() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ExtendendDate_Mandatory"));

        }
        if (empBusinessMission.getExtendMissionDay() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ExtendMissionday_Mandatory"));
        }
      }
      /*
       * if (empBusinessMission.getDecisionType()
       * .equals(DecisionTypeConstants.DECISION_TYPE_BUSINESSMISSION_PAYMENT)) { if
       * (empBusinessMission.getPaymentAmt() == null) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_BMAmount_Mandatory")); } else if
       * (empBusinessMission.getAdvancePercentage() == null) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_AdvPercentage_Mandatory")); } else if
       * (empBusinessMission.getPayrollPeriod() == null) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_payrollPeriod_Mandatory")); } else if
       * (empBusinessMission.getAdvanceAmount() == null) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_AdvAmount_Mandatory")); }
       * 
       * if (empBusinessMission.getPaymentAmt().compareTo(new BigDecimal(0)) == 0) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_Paymentamt_Zero")); } if
       * (empBusinessMission.getAdvanceAmount().compareTo(new BigDecimal(0)) == 0) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_Advamount_zero")); } if
       * (empBusinessMission.getAdvancePercentage().compareTo(new BigDecimal(0)) == 0) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_Advpercentage_Zero")); } }
       */
    } catch (OBException e) {
      log.error(" Exception while creating Business Mission   " + e);
      throw new OBException(e.getMessage());
    } /*
       * catch (JSONException e) { // TODO Auto-generated catch block e.printStackTrace(); }
       */ catch (Exception e) {
      e.printStackTrace();
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
      EHCMEmpBusinessMission empBusinessMission = (EHCMEmpBusinessMission) event
          .getTargetInstance();
      if (empBusinessMission.getDecisionStatus().equals("I")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_BusMiss_Cant_Del"));
      }

    } catch (OBException e) {
      log.error(" Exception while Business Mission ondelete event : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
