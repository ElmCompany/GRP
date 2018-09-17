package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
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
import org.springframework.util.StringUtils;

import sa.elm.ob.hcm.EHCMEmpScholarship;
import sa.elm.ob.hcm.EHCMScholarshipSummary;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.empBusinessMission.EmpBusinessMissionDAOImpl;
import sa.elm.ob.hcm.ad_process.empScholarshipTraining.EmpScholarshipTrainingDAO;
import sa.elm.ob.hcm.ad_process.empScholarshipTraining.EmpScholarshipTrainingDAOImpl;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.hcm.util.UtilityDAO;

/**
 * @author divya on 17/02/2018
 */
public class EmpScholarshipTrainingEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEmpScholarship.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  final String SCHOLARSHIP_TRAINNING = "SCTR";
  EmpScholarshipTrainingDAO empScholarshipTrainingDAOImpl = new EmpScholarshipTrainingDAOImpl();

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      final Property decisionType = entities[0]
          .getProperty(EHCMEmpScholarship.PROPERTY_DECISIONTYPE);
      final Property startdate = entities[0].getProperty(EHCMEmpScholarship.PROPERTY_STARTDATE);
      final Property enddate = entities[0].getProperty(EHCMEmpScholarship.PROPERTY_ENDDATE);
      final Property canceldate = entities[0].getProperty(EHCMEmpScholarship.PROPERTY_CANCELDATE);
      final Property courseStartdate = entities[0]
          .getProperty(EHCMEmpScholarship.PROPERTY_COURSESTARTDATE);
      final Property courseEnddate = entities[0]
          .getProperty(EHCMEmpScholarship.PROPERTY_COURSEENDDATE);
      final Property orgDecisionNo = entities[0]
          .getProperty(EHCMEmpScholarship.PROPERTY_ORIGINALDECISIONNO);
      EHCMEmpScholarship empScholarship = (EHCMEmpScholarship) event.getTargetInstance();
      EmploymentInfo employinfo = null;
      Date startDate = null;
      String decisiontype = null;

      /* current active employment details **/
      EHCMScholarshipSummary scholarshipSummary = empScholarshipTrainingDAOImpl
          .getActiveScholarshipSummary(empScholarship.getEmployee().getId(),
              (empScholarship.getOriginalDecisionNo() != null
                  ? empScholarship.getOriginalDecisionNo().getId()
                  : null));
      if (scholarshipSummary != null) {
        startDate = scholarshipSummary.getStartDate();
        decisiontype = scholarshipSummary.getDecisionType();
      }
      if (scholarshipSummary == null || (scholarshipSummary != null && scholarshipSummary
          .getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE))) {
        employinfo = EmpBusinessMissionDAOImpl
            .getActiveEmployInfo(empScholarship.getEmployee().getId());
        startDate = employinfo.getStartDate();
        decisiontype = employinfo.getChangereason();
      }

      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(enddate).equals(event.getCurrentState(enddate))) {
        // should not allow to create scholarship for same period
        if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || empScholarship.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
          JSONObject result = Utility.overlapWithDecisionsDate(SCHOLARSHIP_TRAINNING,
              sa.elm.ob.utility.util.Utility.formatDate(empScholarship.getStartDate()),
              sa.elm.ob.utility.util.Utility.formatDate(empScholarship.getEndDate()),
              empScholarship.getEmployee().getId());
          if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
            if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
                || (empScholarship.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                    && result.has("scholarShipId") && !result.getString("scholarShipId")
                        .equals(empScholarship.getOriginalDecisionNo().getId()))) {
              if (result.has("errormsg"))
                throw new OBException(OBMessageUtils.messageBD(result.getString("errormsg")));
            }
          }
        }
      }

      // original decision no is mandatory
      if (!event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (!empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          if (empScholarship.getOriginalDecisionNo() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
        }

        // // cant able to create update/extend/cancel for cutoff /hiring employment
        // if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
        // || empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
        // || empScholarship.getDecisionType()
        // .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
        // if ((decisiontype.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF))
        // && (employinfo.isEnabled() || scholarshipSummary.isEnabled())) {//
        // decisionType.equals("H")
        // // ||
        // throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSecondment_CantUpdate"));
        // }
        // }

        // cant able to create cutoff for cutoff employment
        if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          if (decisiontype.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
              && (employinfo.isEnabled() || scholarshipSummary.isEnabled())) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpScholarship_CutoffDontAllow"));
          }
        }

        // cant able to create create secondment for extend employment
        if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          if (decisiontype.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
              && (employinfo.isEnabled() || scholarshipSummary.isEnabled())) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpScholarship_CantCreate"));
          }
        }
      }
      // checking enddate should not be lesser than startdate
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(enddate).equals(event.getCurrentState(enddate))) {
        if (empScholarship.getEndDate().compareTo(empScholarship.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_ScholarShipEndStartDateComp"));
        }
      }

      // checking enddate should not be lesser than startdate
      if (event.getCurrentState(courseStartdate) != null
          && event.getCurrentState(courseEnddate) != null
          && (!event.getCurrentState(courseStartdate)
              .equals(event.getPreviousState(courseStartdate))
              || !event.getCurrentState(courseEnddate)
                  .equals(event.getPreviousState(courseEnddate)))) {
        if (empScholarship.getCourseEnddate()
            .compareTo(empScholarship.getCourseStartdate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_ScholarCoursepEndStartDateComp"));
        }
      }

      // checking startdate should not be lesser than current employment startdate
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {

          if (empScholarship.getStartDate().compareTo(startDate) == -1
              || empScholarship.getStartDate().compareTo(startDate) == 0) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Scholarship_StartDate"));
          }
        }
      }

      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
          /* get previous employment record */
          // employinfo = EmpScholarshipTrainingDAOImpl.getPreviousEmploymentRecord(empScholarship);
          if (empScholarship.getStartDate().compareTo(startDate) == -1
              || empScholarship.getStartDate().compareTo(startDate) == 0) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Scholarship_StartDate"));
          }
        }
      }
      // if ((event.getCurrentState(canceldate) != null
      // && !event.getPreviousState(canceldate).equals(event.getCurrentState(canceldate)))
      // || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
      // if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
      // if (empScholarship.getCancelDate().compareTo(empScholarship.getStartDate()) == -1
      // || empScholarship.getCancelDate().compareTo(empScholarship.getStartDate()) == 0) {
      // throw new OBException(OBMessageUtils.messageBD("Ehcm_Scholarship_CancelDate"));
      // }
      // }
      // }
      if ((event.getCurrentState(enddate) != null
          && !event.getPreviousState(enddate).equals(event.getCurrentState(enddate)))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          if (empScholarship.getOriginalDecisionNo() != null && (empScholarship
              .getOriginalDecisionNo().getEndDate().compareTo(empScholarship.getEndDate()) == -1
              || empScholarship.getOriginalDecisionNo().getEndDate()
                  .compareTo(empScholarship.getEndDate()) == 0)) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_CutOffEndDateGrtThanDecEndDate"));
          }
        }
      }

      if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
        if (empScholarship.getExtendEnddate() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ExtendendDate_Mandatory"));

        }
        if (empScholarship.getExtendMissionDays() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ExtendMissionday_Mandatory"));
        }
      }

      if (event.getCurrentState(orgDecisionNo) != null
          && !event.getPreviousState(orgDecisionNo).equals(event.getCurrentState(orgDecisionNo))) {
        if (empScholarship.getOriginalDecisionNo() != null) {
          if (UtilityDAO.chkDecisionNoUsedInJWR(empScholarship.getOriginalDecisionNo())) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_OriginDecNo_UsedJWR"));
          }
        }
      }
      if (empScholarship.getDecisionType()
          .equals(DecisionTypeConstants.DECISION_TYPE_SCHOLARSHIP_PAYMENT)) {
        if (empScholarship.getPayrollPeriod() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_payrollPeriod_Mandatory"));
        }

        if (empScholarship.getPaymentAmount() == null) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_PaymentAmt_Mandatory"));
        } else if (empScholarship.getPayrollPeriod() == null) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_payrollPeriod_Mandatory"));
        } else if (empScholarship.getAdvanceAmount() == null) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_AdvAmount_Mandatory"));
        }

        if (!StringUtils.isEmpty(empScholarship.getPaymentAmount())
            && empScholarship.getPaymentAmount().compareTo(new BigDecimal(0)) == 0) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_Paymentamt_Zero"));
        }
        if (!StringUtils.isEmpty(empScholarship.getAdvanceAmount())
            && empScholarship.getAdvanceAmount().compareTo(new BigDecimal(0)) == 0) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_Advamount_zero"));
        }

        if (!StringUtils.isEmpty(empScholarship.getAdvancePercentage())
            && empScholarship.getAdvancePercentage() != null
            && empScholarship.getAdvancePercentage().compareTo(new BigDecimal(0)) == 0) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_Advpercentage_Zero"));
        }

      }
    } catch (OBException e) {
      log.error(" Exception while updating Employee Scholarship   " + e);
      throw new OBException(e.getMessage());
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
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
      EHCMEmpScholarship empScholarship = (EHCMEmpScholarship) event.getTargetInstance();
      /* current active employment details */
      EHCMScholarshipSummary scholarshipSummary = empScholarshipTrainingDAOImpl
          .getActiveScholarshipSummary(empScholarship.getEmployee().getId(),
              (empScholarship.getOriginalDecisionNo() != null
                  ? empScholarship.getOriginalDecisionNo().getId()
                  : null));
      if (scholarshipSummary != null) {
        startDate = scholarshipSummary.getStartDate();
        decisionType = scholarshipSummary.getDecisionType();
      }
      if (scholarshipSummary == null || (scholarshipSummary != null && scholarshipSummary
          .getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE))) {
        employinfo = EmpBusinessMissionDAOImpl
            .getActiveEmployInfo(empScholarship.getEmployee().getId());
        startDate = employinfo.getStartDate();
        decisionType = employinfo.getChangereason();
      }
      // should not allow to create scholarship for same period
      if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          || empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        JSONObject result = Utility.overlapWithDecisionsDate(SCHOLARSHIP_TRAINNING,
            sa.elm.ob.utility.util.Utility.formatDate(empScholarship.getStartDate()),
            sa.elm.ob.utility.util.Utility.formatDate(empScholarship.getEndDate()),
            empScholarship.getEmployee().getId());
        if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
          if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
              || (empScholarship.getDecisionType()
                  .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE) && result.has("scholarShipId")
                  && !result.getString("scholarShipId")
                      .equals(empScholarship.getOriginalDecisionNo().getId()))) {
            if (result.has("errormsg"))
              throw new OBException(OBMessageUtils.messageBD(result.getString("errormsg")));
          }
        }
      }

      if (empScholarship.getEndDate().compareTo(empScholarship.getStartDate()) == -1) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_ScholarShipEndStartDateComp"));
      }
      if (empScholarship.getCourseEnddate() != null && empScholarship.getCourseStartdate() != null
          && empScholarship.getCourseEnddate()
              .compareTo(empScholarship.getCourseStartdate()) == -1) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_ScholarCoursepEndStartDateComp"));
      }

      // original decision no is mandatory
      if (!empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        if (empScholarship.getOriginalDecisionNo() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
      }

      // // cant able to create update/extend/cancel for cutoff /hiring employment
      // if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
      // || empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
      // || empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
      //
      // // if ((decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF))
      // // && ((employinfo != null && employinfo.isEnabled()) || scholarshipSummary.isEnabled())) {
      // // // decisionType.equals("H")
      // // // ||
      // // throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSecondment_CantUpdate"));
      // // }
      // }
      // checking startdate should not be lesser than current employment startdate
      if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        if (empScholarship.getStartDate().compareTo(startDate) == -1
            || empScholarship.getStartDate().compareTo(startDate) == 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Scholarship_StartDate"));
        }
      }
      if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        /* get previous employment record */
        // EmploymentInfo employeeinfo =
        // EmpScholarshipTrainingDAOImpl.getPreviousEmploymentRecord(empScholarship);

        if (empScholarship.getStartDate().compareTo(startDate) == -1
            || empScholarship.getStartDate().compareTo(startDate) == 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Scholarship_StartDate"));
        }
      }

      // if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
      // if (empScholarship.getCancelDate().compareTo(empScholarship.getStartDate()) == -1
      // || empScholarship.getCancelDate().compareTo(empScholarship.getStartDate()) == 0) {
      // throw new OBException(OBMessageUtils.messageBD("Ehcm_Scholarship_CancelDate"));
      // }
      // }

      // cant able to create cutoff for cutoff employment
      if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
            && ((employinfo != null && employinfo.isEnabled()) || scholarshipSummary.isEnabled())) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpScholarship_CutoffDontAllow"));
        }

        if (empScholarship.getOriginalDecisionNo() != null && (empScholarship
            .getOriginalDecisionNo().getEndDate().compareTo(empScholarship.getEndDate()) == -1
            || empScholarship.getOriginalDecisionNo().getEndDate()
                .compareTo(empScholarship.getEndDate()) == 0)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_CutOffEndDateGrtThanDecEndDate"));
        }
      }

      // cant able to create create secondment for extend employment
      if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
            && ((employinfo != null && employinfo.isEnabled()) || scholarshipSummary.isEnabled())) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpScholarship_CantCreate"));
        }
      }
      if (empScholarship.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
        if (empScholarship.getExtendEnddate() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ExtendendDate_Mandatory"));

        }
        if (empScholarship.getExtendMissionDays() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ExtendMissionday_Mandatory"));
        }
      }

      if (empScholarship.getOriginalDecisionNo() != null) {
        if (UtilityDAO.chkDecisionNoUsedInJWR(empScholarship.getOriginalDecisionNo())) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_OriginDecNo_UsedJWR"));
        }
      }
      if (empScholarship.getDecisionType()
          .equals(DecisionTypeConstants.DECISION_TYPE_SCHOLARSHIP_PAYMENT)) {
        if (empScholarship.getPayrollPeriod() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_payrollPeriod_Mandatory"));
        }

        if (empScholarship.getPaymentAmount() == null) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_PaymentAmt_Mandatory"));
        } else if (empScholarship.getPayrollPeriod() == null) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_payrollPeriod_Mandatory"));
        } else if (empScholarship.getAdvanceAmount() == null) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_AdvAmount_Mandatory"));
        }

        if (!StringUtils.isEmpty(empScholarship.getPaymentAmount())
            && empScholarship.getPaymentAmount().compareTo(new BigDecimal(0)) == 0) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_Paymentamt_Zero"));
        }
        if (!StringUtils.isEmpty(empScholarship.getAdvanceAmount())
            && empScholarship.getAdvanceAmount().compareTo(new BigDecimal(0)) == 0) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_Advamount_zero"));
        }
        if (!StringUtils.isEmpty(empScholarship.getAdvancePercentage())
            && empScholarship.getAdvancePercentage() != null
            && empScholarship.getAdvancePercentage().compareTo(new BigDecimal(0)) == 0) {
          // throw new OBException(OBMessageUtils.messageBD("Ehcm_Advpercentage_Zero"));
        }

      }

    } catch (OBException e) {
      log.error(" Exception while creating Employee Scholarship   " + e);
      throw new OBException(e.getMessage());
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
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
      EHCMEmpScholarship empScholarship = (EHCMEmpScholarship) event.getTargetInstance();
      if (empScholarship.getDecisionStatus().equals("I")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_ScholarshipCant_Del"));
      }

    } catch (OBException e) {
      log.error(" Exception while scholarship ondelete event : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
