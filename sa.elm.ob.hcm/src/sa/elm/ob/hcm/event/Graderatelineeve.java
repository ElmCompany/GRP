package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DalConnectionProvider;

import sa.elm.ob.hcm.ehcmgraderatelines;
import sa.elm.ob.utility.util.Utility;

public class Graderatelineeve extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ehcmgraderatelines.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  PreparedStatement ps = null, ps1 = null;
  ResultSet rs = null, rs1 = null;

  String result = "";

  private Logger log = Logger.getLogger(this.getClass());

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();

      ehcmgraderatelines graderateline = (ehcmgraderatelines) event.getTargetInstance();
      final Property sequencetype = entities[0].getProperty(ehcmgraderatelines.PROPERTY_LINENO);
      final Property startdate = entities[0].getProperty(ehcmgraderatelines.PROPERTY_STARTDATE);
      final Property enddate = entities[0].getProperty(ehcmgraderatelines.PROPERTY_ENDDATE);
      final Property grade = entities[0].getProperty(ehcmgraderatelines.PROPERTY_GRADE);

      ConnectionProvider conn = new DalConnectionProvider(false);
      if (graderateline.getEndDate() != null) {
        if (graderateline.getEndDate().compareTo(graderateline.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDateStartDateComp"));
        }
      }

      String tdate = "";
      String fdate = Utility.formatDate(graderateline.getStartDate());
      if (graderateline.getEndDate() == null) {
        tdate = "21-06-2058";
      } else {
        tdate = Utility.formatDate(graderateline.getEndDate());
      }

      BigDecimal Midvalue = graderateline.getMidvalue();
      BigDecimal Maximum = graderateline.getMaximum();
      BigDecimal Minimum = graderateline.getMinimum();
      BigDecimal value = graderateline.getSearchKey();
      // if any one the value field should empty then throw the error
      // if (Midvalue == null && Maximum == null && Minimum == null && value == null)
      //
      // {
      // throw new OBException(OBMessageUtils.messageBD("Ehcm_value"));
      // }
      // Allow only either fixed value or midvalue and max value and minimumvalue
      if (graderateline.getCurrency() != null) {
        if (value.compareTo(BigDecimal.ZERO) == 0
            && (Maximum == null || Minimum == null || Midvalue == null)) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_EitherFixed_Value"));
        } else if (value.compareTo(BigDecimal.ZERO) > 0
            && (Maximum != null || Minimum != null || Midvalue != null)) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_EitherFixed_Value"));
        }
      } else {
        if (value.compareTo(BigDecimal.ZERO) > 0
            && (Maximum != null || Minimum != null || Midvalue != null)) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_EitherFixed_Value"));
        }
      }

      if (Minimum != null && Maximum != null && Midvalue != null) {
        if (Minimum.compareTo(Maximum) > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_MaxMinMidValue"));
        } else if ((Midvalue.compareTo(Maximum) > 0) || (Midvalue.compareTo(Minimum) < 0)) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_MaxMinMidValue"));
        }
      }

      OBQuery<ehcmgraderatelines> type1 = OBDal.getInstance().createQuery(ehcmgraderatelines.class,
          " lineNo ='" + graderateline.getLineNo() + "' and ehcmGraderates.id = '"
              + graderateline.getEhcmGraderates().getId() + "' and client.id ='"
              + graderateline.getClient().getId() + "' ");
      if (!event.getPreviousState(sequencetype).equals(event.getCurrentState(sequencetype))) {
        if (type1.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Lineno"));
        }
      }
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || ((event.getPreviousState(enddate) != null && event.getCurrentState(enddate) != null)
              && !event.getPreviousState(enddate).equals(event.getCurrentState(enddate)))
          || event.getCurrentState(enddate) == null || event.getPreviousState(enddate) == null
          || !event.getPreviousState(grade).equals(event.getCurrentState(grade))) {
        ps1 = conn.getPreparedStatement("select * from ehcm_graderatelines where ehcm_grade_id ='"
            + graderateline.getGrade().getId() + "' and ad_client_id='"
            + graderateline.getClient().getId() + "' and ehcm_graderates_id='"
            + graderateline.getEhcmGraderates().getId()
            + "' and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fdate
            + "') and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
            + tdate
            + "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
            + fdate + "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
            + tdate + "','dd-MM-yyyy'))) and ehcm_graderatelines_id <> '" + graderateline.getId()
            + "'");
        rs1 = ps1.executeQuery();
        if (rs1.next()) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_graderateline"));
        }
      }

      // If currency is present then value is mandatory
      if (graderateline.getCurrency() != null && graderateline.getSearchKey() == null) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_GradeValue_Mandatory"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating graderatelineevent event: " + e);
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
      ehcmgraderatelines graderateline = (ehcmgraderatelines) event.getTargetInstance();
      ConnectionProvider conn = new DalConnectionProvider(false);
      BigDecimal Midvalue = graderateline.getMidvalue();
      BigDecimal Maximum = graderateline.getMaximum();
      BigDecimal Minimum = graderateline.getMinimum();
      BigDecimal value = graderateline.getSearchKey();
      if (graderateline.getEndDate() != null) {
        if (graderateline.getEndDate().compareTo(graderateline.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDateStartDateComp"));
        }
      }
      String tdate = "";
      String fdate = Utility.formatDate(graderateline.getStartDate());
      if (graderateline.getEndDate() == null) {
        tdate = "21-06-2058";
      } else {
        tdate = Utility.formatDate(graderateline.getEndDate());
      }

      ps1 = conn.getPreparedStatement("select * from ehcm_graderatelines where ehcm_grade_id ='"
          + graderateline.getGrade().getId() + "' and ad_client_id='"
          + graderateline.getClient().getId() + "' and ehcm_graderates_id='"
          + graderateline.getEhcmGraderates().getId()
          + "' and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fdate
          + "') and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
          + tdate
          + "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
          + fdate + "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
          + tdate + "','dd-MM-yyyy'))) ");
      rs1 = ps1.executeQuery();
      if (rs1.next()) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_graderateline"));
      }

      if (graderateline.getCurrency() != null) {
        if (value.compareTo(BigDecimal.ZERO) == 0
            && (Maximum == null || Minimum == null || Midvalue == null)) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_EitherFixed_Value"));
        } else if (value.compareTo(BigDecimal.ZERO) > 0
            && (Maximum != null || Minimum != null || Midvalue != null)) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_EitherFixed_Value"));
        }
      } else {
        if (value.compareTo(BigDecimal.ZERO) > 0
            && (Maximum != null || Minimum != null || Midvalue != null)) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_EitherFixed_Value"));
        }
      }
      if (Minimum != null && Maximum != null && Midvalue != null) {
        if (Minimum.compareTo(Maximum) > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_MaxMinMidValue"));
        } else if ((Midvalue.compareTo(Maximum) > 0) || (Midvalue.compareTo(Minimum) < 0)) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_MaxMinMidValue"));
        }
      }

      OBQuery<ehcmgraderatelines> type1 = OBDal.getInstance().createQuery(ehcmgraderatelines.class,
          " lineNo ='" + graderateline.getLineNo() + "' and ehcmGraderates.id = '"
              + graderateline.getEhcmGraderates().getId() + "' and client.id ='"
              + graderateline.getClient().getId() + "' ");
      if (type1.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Lineno"));
      }

      // If currency is present then value is mandatory
      if (graderateline.getCurrency() != null && graderateline.getSearchKey() == null) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_GradeValue_Mandatory"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating graderatelineevent " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
