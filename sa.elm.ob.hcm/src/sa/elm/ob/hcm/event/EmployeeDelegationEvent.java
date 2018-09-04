package sa.elm.ob.hcm.event;

import java.math.BigInteger;
import java.text.SimpleDateFormat;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.Query;
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

import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author gopalakrishnan on 16/11/2016
 * 
 */

public class EmployeeDelegationEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EmployeeDelegation.ENTITY_NAME) };

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
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      final Property employee = entities[0].getProperty(EmployeeDelegation.PROPERTY_EHCMEMPPERINFO);
      final Property newposition = entities[0].getProperty(EmployeeDelegation.PROPERTY_NEWPOSITION);
      final Property decisionType = entities[0]
          .getProperty(EmployeeDelegation.PROPERTY_DECISIONTYPE);
      EmployeeDelegation delegation = (EmployeeDelegation) event.getTargetInstance();
      final Property startDate = entities[0].getProperty(EmployeeDelegation.PROPERTY_STARTDATE);
      final Property endDate = entities[0].getProperty(EmployeeDelegation.PROPERTY_ENDDATE);
      log.debug("update");

      // compare startdate and enddate
      if (delegation.getEndDate() != null) {
        if (delegation.getEndDate().compareTo(delegation.getStartDate()) < 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_StartDateGreThan_EndDate"));
        }
      }
      // compare startdate with previous enddate
      if (delegation.getOriginalDecisionNo() != null) {
        if (delegation.getEndDate() != null && delegation.getEndDate()
            .compareTo(delegation.getOriginalDecisionNo().getStartDate()) < 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_StartDateGreThan_EndDate"));
        }
      }
      // compare start with hire date
      if (delegation.getStartDate().compareTo(delegation.getEhcmEmpPerinfo().getHiredate()) < 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_StartDateVsHireDate"));
      }

      // compare start date and end date with one year
      if (delegation.getEndDate() != null) {
        String strStartDate = convertTohijriDate(dateYearFormat.format(delegation.getStartDate()));
        String strEndDate = convertTohijriDate(
            dateYearFormat.format(event.getCurrentState(endDate)));
        log.debug("strStartDate:" + strStartDate);
        Boolean CheckYear = UtilityDAO.yearValidation(strStartDate, strEndDate);
        if (!CheckYear) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Delegated_year"));
        }
      }
      // if (!event.getCurrentState(employee).equals(event.getPreviousState(employee))) {
      // check delegation already associated
      if (delegation.getDecisionType().equals("CR")
          && (!event.getCurrentState(newposition).equals(event.getPreviousState(newposition))
              || !event.getCurrentState(startDate).equals(event.getPreviousState(startDate))
              || !event.getCurrentState(endDate).equals(event.getPreviousState(endDate)))) {

        String delegationQry = "select e.id from Ehcm_Emp_Delegation as e"
            + " where e.ehcmEmpPerinfo.id=:employee and e.enabled='Y' and e.newPosition.id=:newposition and e.decisionStatus!='UP' "
            + " and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
            + " and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
            + " or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
            + " and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))  ";

        Query query = OBDal.getInstance().getSession().createQuery(delegationQry);
        query.setParameter("newposition", delegation.getNewPosition().getId());
        query.setParameter("employee", delegation.getEhcmEmpPerinfo().getId());
        query.setParameter("fromdate", Utility.formatDate(delegation.getStartDate()));
        query.setParameter("todate", Utility.formatDate(delegation.getEndDate()));

        if (query.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Delegation_Not_Possible"));
        }
      }
      // }
      EmploymentInfo objEmplInfo = delegation.getEhcmEmploymentInfo();
      if (delegation.getNewPosition() != null
          && !event.getCurrentState(newposition).equals(event.getPreviousState(newposition))) {
        EhcmPosition objNewposition = OBDal.getInstance().get(EhcmPosition.class,
            delegation.getNewPosition().getId());
        // check position grade higher than 2 grade of current grade
        if (objEmplInfo.getEmploymentgrade().getSequenceNumber()
            .compareTo(objNewposition.getGrade().getSequenceNumber()) < 0) {
          try {
            SQLQuery gradeQuery = OBDal.getInstance().getSession().createSQLQuery(
                "select count(seqno) as seqcount from ehcm_grade where ehcm_grade_id in( select ehcm_grade_id from ehcm_grade "
                    + " where seqno >'" + objEmplInfo.getEmploymentgrade().getSequenceNumber()
                    + "' order by seqno asc limit 2) and ehcm_grade_id='"
                    + objNewposition.getGrade().getId() + "'");
            if (gradeQuery.list().size() > 0) {
              Object row = (Object) gradeQuery.list().get(0);
              if (((BigInteger) row).compareTo(BigInteger.ZERO) == 0) {
                throw new OBException(OBMessageUtils.messageBD("Ehcm_Position_Grade_high"));
              }
            }
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

        }
      }
      // check original decision number
      if (!event.getCurrentState(decisionType).equals(event.getPreviousState(decisionType))) {
        if (delegation.getDecisionType().equals("CA")
            || delegation.getDecisionType().equals("UP")) {
          if (delegation.getOriginalDecisionNo() == null) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Original_Decisionblank"));
          }
        }

        // check original decision number
        if (delegation.getDecisionType().equals("CA")) {
          if (delegation.getCancelDate() == null) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Delegation_CancelDate"));
          }
        }
      }
      // End Date is mandatory for Create and Update
      if (delegation.getDecisionType() != null && !delegation.getDecisionType().equals("CA")
          && delegation.getEndDate() == null) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Enddate_Mandatory"));
      }
    } catch (

    OBException e) {
      log.error(" Exception while updating Employee Delegation  " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @SuppressWarnings("unchecked")
  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      final Property employmentInfo = entities[0]
          .getProperty(EmployeeDelegation.PROPERTY_EHCMEMPLOYMENTINFO);
      EmployeeDelegation delegation = (EmployeeDelegation) event.getTargetInstance();
      String employmentInfoId = null;
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + delegation.getEhcmEmpPerinfo().getId()
              + "' and enabled='Y'  and alertStatus='ACT' order by created desc ");
      empInfo.setMaxResult(1);
      if (empInfo.list().size() > 0) {
        for (EmploymentInfo empinfo : empInfo.list()) {
          employmentInfoId = empinfo.getId();
        }
      }

      // compare startdate and enddate
      if (delegation.getEndDate() != null
          && delegation.getEndDate().compareTo(delegation.getStartDate()) < 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_StartDateGreThan_EndDate"));
      }
      // compare startdate with previous enddate
      if (delegation.getOriginalDecisionNo() != null) {
        if (delegation.getEndDate() != null && delegation.getEndDate()
            .compareTo(delegation.getOriginalDecisionNo().getStartDate()) < 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_StartDateGreThan_EndDate"));
        }
      }
      // compare start with hire date
      if (delegation.getStartDate().compareTo(delegation.getEhcmEmpPerinfo().getHiredate()) < 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_StartDateVsHireDate"));
      }
      // compare start date and end date with in one year
      String strStartDate = null;
      String strEndDate = null;
      if (delegation.getEndDate() != null) {
        strStartDate = convertTohijriDate(dateYearFormat.format(delegation.getStartDate()));
        strEndDate = convertTohijriDate(dateYearFormat.format(delegation.getEndDate()));
        log.debug("strStartDate:" + strStartDate);

        Boolean CheckYear = UtilityDAO.yearValidation(strStartDate, strEndDate);
        if (!CheckYear) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Delegated_year"));
        }
      }
      // update employment info id (recent active record)
      event.setCurrentState(employmentInfo,
          OBDal.getInstance().get(EmploymentInfo.class, employmentInfoId));

      // check delegation already associated
      if (delegation.getDecisionType().equals("CR")) {

        String delegationQry = "select e.id from Ehcm_Emp_Delegation as e"
            + " where e.ehcmEmpPerinfo.id=:employee and e.enabled='Y' and e.newPosition.id=:newposition and e.decisionStatus!='UP' "
            + " and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
            + " and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
            + " or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
            + " and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))  ";

        Query query = OBDal.getInstance().getSession().createQuery(delegationQry);
        query.setParameter("newposition", delegation.getNewPosition().getId());
        query.setParameter("employee", delegation.getEhcmEmpPerinfo().getId());
        query.setParameter("fromdate", Utility.formatDate(delegation.getStartDate()));
        query.setParameter("todate", Utility.formatDate(delegation.getEndDate()));

        if (query.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Delegation_Not_Possible"));
        }
      }
      EmploymentInfo objEmplInfo = OBDal.getInstance().get(EmploymentInfo.class, employmentInfoId);
      if (delegation.getNewPosition() != null) {
        EhcmPosition objNewposition = OBDal.getInstance().get(EhcmPosition.class,
            delegation.getNewPosition().getId());

        // check position grade higher than 2 grade of current grade
        if (objEmplInfo.getEmploymentgrade().getSequenceNumber()
            .compareTo(objNewposition.getGrade().getSequenceNumber()) < 0) {
          try {
            SQLQuery gradeQuery = OBDal.getInstance().getSession().createSQLQuery(
                "select count(seqno) as seqcount from ehcm_grade where ehcm_grade_id in( select ehcm_grade_id from ehcm_grade "
                    + " where seqno >'" + objEmplInfo.getEmploymentgrade().getSequenceNumber()
                    + "' order by seqno asc limit 2) and ehcm_grade_id='"
                    + objNewposition.getGrade().getId() + "'");

            if (gradeQuery.list().size() > 0) {
              Object row = (Object) gradeQuery.list().get(0);
              log.debug(row);
              if (((BigInteger) row).compareTo(BigInteger.ZERO) == 0) {
                throw new OBException(OBMessageUtils.messageBD("Ehcm_Position_Grade_high"));
              }
            }
          } catch (OBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }

      }
      // check original decision number
      if (delegation.getDecisionType().equals("CA") || delegation.getDecisionType().equals("UP")) {
        if (delegation.getOriginalDecisionNo() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Original_Decisionblank"));
        }
      }
      // check original decision number
      if (delegation.getDecisionType().equals("CA")) {
        if (delegation.getCancelDate() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Delegation_CancelDate"));
        }
      }
      // End Date is mandatory for Create and Update
      if (delegation.getDecisionType() != null && !delegation.getDecisionType().equals("CA")
          && delegation.getEndDate() == null) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Enddate_Mandatory"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating Employee Delegation   " + e);
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
      EmployeeDelegation delegation = (EmployeeDelegation) event.getTargetInstance();
      if (delegation.getDecisionStatus().equals("I")) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Delegation_Issued"));
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
        log.debug("row:" + row.toString());
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
