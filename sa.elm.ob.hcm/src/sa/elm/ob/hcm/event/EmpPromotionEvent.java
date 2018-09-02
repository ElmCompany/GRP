package sa.elm.ob.hcm.event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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

import sa.elm.ob.hcm.DecisionBalance;
import sa.elm.ob.hcm.EHCMEmpPromotion;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.utility.util.UtilityDAO;

public class EmpPromotionEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEmpPromotion.ENTITY_NAME) };

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
      final Property promotionType = entities[0]
          .getProperty(EHCMEmpPromotion.PROPERTY_PROMOTIONTYPE);
      final Property newDepartment = entities[0]
          .getProperty(EHCMEmpPromotion.PROPERTY_NEWDEPARTMENT);
      final Property decisionType = entities[0].getProperty(EHCMEmpPromotion.PROPERTY_DECISIONTYPE);
      final Property startdate = entities[0].getProperty(EHCMEmpPromotion.PROPERTY_STARTDATE);
      final Property canceldate = entities[0].getProperty(EHCMEmpPromotion.PROPERTY_CANCELDATE);
      final Property position = entities[0].getProperty(EHCMEmpPromotion.PROPERTY_NEWPOSITION);
      final Property grade = entities[0].getProperty(EHCMEmpPromotion.PROPERTY_NEWGRADE);
      final Property person = entities[0].getProperty(EHCMEmpPromotion.PROPERTY_EHCMEMPPERINFO);
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      String lastPromotionDate = null;
      String empInfoId = "";
      boolean chkPositionAvailableOrNot = false;
      EmploymentInfo employinfo = null;
      EHCMEmpPromotion promotion = (EHCMEmpPromotion) event.getTargetInstance();
      /* current active employment details */
      OBQuery<EmploymentInfo> emplyinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
              + "' and e.enabled='Y'");
      if (emplyinfo.list().size() > 0) {
        employinfo = emplyinfo.list().get(0);
        empInfoId = employinfo.getId();
      }

      // New Department is mandatory for promotion type- promotion and transfer
      if (!event.getCurrentState(promotionType).equals(event.getPreviousState(promotionType))
          || (event.getCurrentState(newDepartment) != null && !event.getCurrentState(newDepartment)
              .equals(event.getPreviousState(newDepartment)))) {
        if (promotion.getPromotionType().equals("PRT") && promotion.getNewDepartment() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_NewDept_Mandatory"));
        }
      }
      if (!event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (promotion.getDecisionType().equals("UP") || promotion.getDecisionType().equals("CA")) {
          if (employinfo.getChangereason().equals("H") && employinfo.isEnabled()) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_CantUpdate"));
          }
        }
        if (promotion.getDecisionType().equals("CA") || promotion.getDecisionType().equals("UP")) {
          if (promotion.getOriginalDecisionsNo() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
        }
        if (promotion.getDecisionType().equals("CR") || promotion.getDecisionType().equals("UP")) {
          if (promotion.getNewPosition() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_NewPos"));
        }
        if (employinfo.getPosition().getGrade().equals(employinfo.getEmploymentgrade())) {
          if (promotion.getDecisionType().equals("CR")) {
            if (promotion.getPromotionType().equals("PR")) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_EmpPromSameGrade"));
            }
          }
        }
      }
      if (event.getPreviousState(position) != null && event.getCurrentState(position) == null) {
        if (promotion.getNewPosition() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_NewPos"));
        if (promotion.getNEWGrade().getSequenceNumber()
            .compareTo(promotion.getNewPosition().getGrade().getSequenceNumber()) > 1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpPromGradeGreater"));
        }
      }
      if ((!event.getPreviousState(position).equals(event.getCurrentState(position))
          || (!event.getPreviousState(grade).equals(event.getCurrentState(grade))))) {
        if (promotion.getNEWGrade().getSequenceNumber()
            .compareTo(promotion.getNewPosition().getGrade().getSequenceNumber()) > 1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpPromGradeGreater"));
        }
      }

      log.debug("canstdate:" + promotion.getCancelDate().compareTo(promotion.getStartDate()));
      if (promotion.getDecisionType().equals("CA")) {
        if (!event.getPreviousState(canceldate).equals(event.getCurrentState(canceldate))) {
          if (promotion.getCancelDate().compareTo(promotion.getStartDate()) == -1
              || promotion.getCancelDate().compareTo(promotion.getStartDate()) == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpProm_CancelDate"));
          }
        }
        if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))) {
          if (promotion.getCancelDate().compareTo(promotion.getStartDate()) == -1
              || promotion.getCancelDate().compareTo(promotion.getStartDate()) == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpProm_CancelDate"));
          }
        }
      }
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (promotion.getDecisionType().equals("CR") || promotion.getDecisionType().equals("UP")) {
          OBQuery<EmploymentInfo> info = OBDal.getInstance()
              .createQuery(EmploymentInfo.class, " ehcmEmpPerinfo.id='"
                  + promotion.getEhcmEmpPerinfo().getId()
                  + "' and ehcmEmpPromotion.id is null and issecondment ='N' order by creationDate desc ");
          info.setMaxResult(1);
          EmploymentInfo empinfo = info.list().get(0);
          if (promotion.getStartDate().compareTo(empinfo.getStartDate()) == -1
              || promotion.getStartDate().compareTo(empinfo.getStartDate()) == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
          }
        }
      }

      // check 4 year complete
      if (promotion.getDecisionType().equals("CR") || promotion.getDecisionType().equals("UP")) {
        if (!event.getPreviousState(person).equals(event.getCurrentState(person))
            || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))
            || !event.getPreviousState(startdate).equals(event.getCurrentState(startdate))) {
          OBQuery<EmploymentInfo> info = null;
          if (employinfo != null) {
            if (promotion.getDecisionType().equals("CR")) {
              info = OBDal.getInstance().createQuery(EmploymentInfo.class,
                  " as e where e.ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
                      + "' and e.employmentgrade.id='" + promotion.getEmploymentGrade().getId()
                      + "'  order by startdate asc ");
              info.setMaxResult(1);
            } else if (promotion.getDecisionType().equals("UP")) {

              OBQuery<ehcmgrade> empgrade = OBDal.getInstance().createQuery(ehcmgrade.class,
                  " sequenceNumber <'" + promotion.getEmploymentGrade().getSequenceNumber()
                      + "' order by sequenceNumber desc");
              empgrade.setMaxResult(1);
              if (empgrade.list().size() > 0) {
                ehcmgrade emplygrade = empgrade.list().get(0);
                info = OBDal.getInstance().createQuery(EmploymentInfo.class,
                    " ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
                        + "' and ehcmEmpPromotion.id is null " + " and employmentgrade.id='"
                        + emplygrade.getId() + "'  order by startdate asc ");
                info.setMaxResult(1);
              }

            }
            if (info.list().size() > 0) {
              EmploymentInfo empinfo = info.list().get(0);
              if (empinfo.getStartDate() != null) {
                String empstartdate = convertTohijriDate(
                    dateYearFormat.format(empinfo.getStartDate()));
                String promotiondate = convertTohijriDate(
                    dateYearFormat.format(promotion.getStartDate()));

                if (empinfo.getChangereason().equals("H")) {
                  DecisionBalance promotionInitialBalance = sa.elm.ob.hcm.util.UtilityDAO
                      .getInitialBaanceObjforEmployee(promotion.getEhcmEmpPerinfo().getId(),
                          Constants.PROMOTIONBALANCE);
                  if (promotionInitialBalance != null) {
                    lastPromotionDate = convertTohijriDate(
                        dateYearFormat.format(promotionInitialBalance.getBlockStartdate()));
                  }
                }
                boolean validation = UtilityDAO.periodyearValidation(
                    lastPromotionDate == null ? empstartdate : lastPromotionDate, promotiondate, 4);
                if (!validation) {
                  throw new OBException(OBMessageUtils.messageBD("EHCM_EmpPromCantAllow"));
                }
              }
            }
          }
        }
      }
      if (!event.getPreviousState(person).equals(event.getCurrentState(person))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        // cant able to cancel the promotion old position is not free
        if (promotion.getDecisionType().equals("CA")) {
          if (promotion.getEhcmEmpPerinfo() != null) {
            OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
                " ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
                    + "' and enabled='N' order by creationDate desc ");
            info.setMaxResult(1);
            if (info.list().size() > 0) {
              EmploymentInfo empinfo = info.list().get(0);
              if (empinfo.getPosition() != null) {
                chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO
                    .chkPositionAvailableOrNot(promotion.getEhcmEmpPerinfo(), empinfo.getPosition(),
                        promotion.getStartDate(), null, promotion.getDecisionType(), false);
                if (chkPositionAvailableOrNot) {
                  throw new OBException(OBMessageUtils.messageBD("EHCM_EmpProm_CantCancel"));
                }
                /*
                 * if (empinfo.getPosition().getAssignedEmployee() != null) { if
                 * (!empinfo.getPosition().getAssignedEmployee().getId()
                 * .equals(promotion.getEhcmEmpPerinfo().getId())) { throw new
                 * OBException(OBMessageUtils.messageBD("EHCM_EmpProm_CantCancel")); } }
                 */
              }
            }
          }
        }
      }
    } catch (OBException e) {
      log.error(" Exception while creating Employee Promotion  " + e);
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
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      boolean chkPositionAvailableOrNot = false;
      OBContext.setAdminMode();
      EHCMEmpPromotion promotion = (EHCMEmpPromotion) event.getTargetInstance();
      String empInfoId = "";
      String lastPromotionDate = null;
      EmploymentInfo employinfo = null;
      /* current active employment details */
      OBQuery<EmploymentInfo> emplyinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
              + "' and e.enabled='Y'");
      if (emplyinfo.list().size() > 0) {
        employinfo = emplyinfo.list().get(0);
        empInfoId = employinfo.getId();
      }

      // New Department is mandatory for promotion type- promotion and transfer
      if (promotion.getPromotionType().equals("PRT") && promotion.getNewDepartment() == null) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_NewDept_Mandatory"));
      }

      if (promotion.getDecisionType().equals("UP") || promotion.getDecisionType().equals("CA")) {
        if (employinfo.getChangereason().equals("H") && employinfo.isEnabled()) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_CantUpdate"));
        }
      }
      if (promotion.getDecisionType().equals("CA") || promotion.getDecisionType().equals("UP")) {
        if (promotion.getOriginalDecisionsNo() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
      }
      if (promotion.getDecisionType().equals("CR") || promotion.getDecisionType().equals("UP")) {
        if (promotion.getNewPosition() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_NewPos"));
      }
      log.debug("cancom:" + promotion.getCancelDate().compareTo(promotion.getStartDate()));
      if (promotion.getDecisionType().equals("CA")) {
        if (promotion.getCancelDate().compareTo(promotion.getStartDate()) == -1
            || promotion.getCancelDate().compareTo(promotion.getStartDate()) == 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpProm_CancelDate"));
        }
      }
      if (promotion.getStartDate() != null) {
        if (promotion.getDecisionType().equals("CR") || promotion.getDecisionType().equals("UP")) {
          OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
                  + "' and  ehcmEmpPromotion.id is null order by creationDate desc ");
          info.setMaxResult(1);
          EmploymentInfo empinfo = info.list().get(0);
          if (promotion.getStartDate().compareTo(empinfo.getStartDate()) == -1
              || promotion.getStartDate().compareTo(empinfo.getStartDate()) == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
          }
        }
      }
      if (promotion.getDecisionType().equals("CR") || promotion.getDecisionType().equals("UP")) {
        // check 4 year complete
        OBQuery<EmploymentInfo> info = null;
        if (employinfo != null) {
          if (promotion.getDecisionType().equals("CR")) {
            info = OBDal.getInstance().createQuery(EmploymentInfo.class,
                " as e where e.ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
                    + "' and e.employmentgrade.id='" + promotion.getEmploymentGrade().getId()
                    + "'  order by startdate asc ");
            info.setMaxResult(1);

          } else if (promotion.getDecisionType().equals("UP")) {
            OBQuery<ehcmgrade> empgrade = OBDal.getInstance().createQuery(ehcmgrade.class,
                " sequenceNumber <'" + promotion.getEmploymentGrade().getSequenceNumber()
                    + "' order by sequenceNumber desc");
            empgrade.setMaxResult(1);
            if (empgrade.list().size() > 0) {
              ehcmgrade emplygrade = empgrade.list().get(0);
              info = OBDal.getInstance().createQuery(EmploymentInfo.class,
                  " ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
                      + "' and ehcmEmpPromotion.id is null " + " and employmentgrade.id='"
                      + emplygrade.getId() + "'  order by startdate asc ");
              info.setMaxResult(1);
            }
          }
          if (info.list().size() > 0) {
            EmploymentInfo empinfo = info.list().get(0);
            if (empinfo.getStartDate() != null) {
              String empstartdate = convertTohijriDate(
                  dateYearFormat.format(empinfo.getStartDate()));
              String promotiondate = convertTohijriDate(
                  dateYearFormat.format(promotion.getStartDate()));
              log.debug("promotiondate:" + promotiondate);
              log.debug("empstartdate:" + empstartdate);

              if (empinfo.getChangereason().equals("H")) {
                DecisionBalance promotionInitialBalance = sa.elm.ob.hcm.util.UtilityDAO
                    .getInitialBaanceObjforEmployee(promotion.getEhcmEmpPerinfo().getId(),
                        Constants.PROMOTIONBALANCE);
                if (promotionInitialBalance != null) {
                  lastPromotionDate = convertTohijriDate(
                      dateYearFormat.format(promotionInitialBalance.getBlockStartdate()));
                }
              }

              boolean validation = UtilityDAO.periodyearValidation(
                  lastPromotionDate == null ? empstartdate : lastPromotionDate, promotiondate, 4);
              log.debug("validation:" + validation);

              if (!validation) {
                throw new OBException(OBMessageUtils.messageBD("EHCM_EmpPromCantAllow"));
              }
            }
          }
        }
      }
      if (employinfo.getPosition().getGrade().equals(employinfo.getEmploymentgrade())) {
        if (promotion.getDecisionType().equals("CR")) {
          if (promotion.getPromotionType().equals("PR")) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpPromSameGrade"));
          }
        }
      }
      if (promotion.getNEWGrade().getSequenceNumber()
          .compareTo(promotion.getNewPosition().getGrade().getSequenceNumber()) > 1) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpPromGradeGreater"));
      }
      // cant able to cancel the promotion old position is not free
      if (promotion.getDecisionType().equals("CA")) {
        if (promotion.getEhcmEmpPerinfo() != null) {
          OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
                  + "' and enabled='N' order by creationDate desc ");
          info.setMaxResult(1);
          if (info.list().size() > 0) {
            EmploymentInfo empinfo = info.list().get(0);
            if (empinfo.getPosition() != null) {
              chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO
                  .chkPositionAvailableOrNot(promotion.getEhcmEmpPerinfo(), empinfo.getPosition(),
                      promotion.getStartDate(), null, promotion.getDecisionType(), false);
              if (chkPositionAvailableOrNot) {
                throw new OBException(OBMessageUtils.messageBD("EHCM_EmpProm_CantCancel"));
              }
              /*
               * if (empinfo.getPosition().getAssignedEmployee() != null) { if
               * (!empinfo.getPosition().getAssignedEmployee().getId()
               * .equals(promotion.getEhcmEmpPerinfo().getId())) { throw new
               * OBException(OBMessageUtils.messageBD("EHCM_EmpProm_CantCancel")); } }
               */
            }
          }
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Employee transfer   " + e);
      throw new OBException(e.getMessage());
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
      EHCMEmpPromotion promotion = (EHCMEmpPromotion) event.getTargetInstance();

      if (promotion.getDecisionStatus().equals("I")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpPromtion_Issued"));
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
      log.error("Exception in convertTohijriDate() Method : ", e);
      return "0";
    }
    return hijriDate;
  }

}
