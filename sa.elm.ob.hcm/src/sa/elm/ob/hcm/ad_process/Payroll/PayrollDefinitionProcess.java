package sa.elm.ob.hcm.ad_process.Payroll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;
import sa.elm.ob.utility.util.UtilityDAO;

public class PayrollDefinitionProcess extends DalBaseProcess {
  private static final Logger log = LoggerFactory.getLogger(EHCMPayrollDefinition.class);

  @SuppressWarnings("unused")
  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub

    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    int periodType = 0, year = 0, MM = 0, YYYY = 0, maxYear = 0;
    Date nextStartDate = null;
    try {
      OBContext.setAdminMode(true);
      String payrollid = (String) bundle.getParams().get("Ehcm_Payroll_Definition_ID");
      EHCMPayrollDefinition PayrollDefinition = OBDal.getInstance().get(EHCMPayrollDefinition.class,
          payrollid);
      Date periodEnd = PayrollDefinition.getPeriodEnddate();
      Date periodStartDate = PayrollDefinition.getStartDate();
      final String clientId = (String) bundle.getContext().getClient();
      final String orgId = PayrollDefinition.getOrganization().getId();
      final String userId = (String) bundle.getContext().getUser();
      final String roleId = (String) bundle.getContext().getRole();
      PreparedStatement st = null;
      ResultSet rs = null;
      Connection conn = OBDal.getInstance().getConnection();
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      NumberFormat Numformatter = new DecimalFormat("00");

      year = (int) (long) PayrollDefinition.getEhcmYear();
      periodType = (int) (long) PayrollDefinition.getEhcmPayrollPeriodTypes().getPeriodsperyear();

      if (periodType == 12) {
        String hijiridate = UtilityDAO.convertTohijriDate(periodEnd.toString());
        MM = Integer.valueOf(hijiridate.split("-")[1]);
        YYYY = Integer.valueOf(hijiridate.split("-")[2]);
        maxYear = Integer.valueOf(hijiridate.split("-")[2]) + year;
        String MaxGregDate = UtilityDAO
            .convertToGregorian("01-" + (hijiridate.split("-")[1]) + "-" + maxYear);
        Date fmtMaxGregDate = format.parse(MaxGregDate);
        String fstGerDate = UtilityDAO.convertToGregorian(
            "01-" + (hijiridate.split("-")[1]) + "-" + (hijiridate.split("-")[2]));
        Date startDate = format.parse(fstGerDate);
        if (periodStartDate.compareTo(startDate) > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Startdate_Validation"));
        }
        st = conn.prepareStatement(
            " delete from ehcm_payrolldef_period where ehcm_payroll_definition_id=?");
        st.setString(1, payrollid);
        st.executeUpdate();
        conn.commit();

        while (startDate.compareTo(fmtMaxGregDate) < 0) {
          String payrollRun = UtilityDAO
              .convertToGregorian(Numformatter.format(PayrollDefinition.getPayrollRun()) + "-"
                  + hijiridate.split("-")[1] + "-" + hijiridate.split("-")[2]);
          Date payrollRungregorian = format.parse(payrollRun);
          String DirDeposite = UtilityDAO
              .convertToGregorian(Numformatter.format(PayrollDefinition.getDirectDeposite()) + "-"
                  + hijiridate.split("-")[1] + "-" + hijiridate.split("-")[2]);
          Date DirDepositegregorian = format.parse(DirDeposite);
          EHCMPayrolldefPeriod periodDates = OBProvider.getInstance()
              .get(EHCMPayrolldefPeriod.class);
          periodDates.setClient(OBDal.getInstance().get(Client.class, clientId));
          periodDates.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
          periodDates.setEnabled(true);
          periodDates.setCreatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setCreationDate(new java.util.Date());
          periodDates.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setUpdated(new java.util.Date());
          periodDates.setEhcmPayrollDefinition(PayrollDefinition);
          periodDates.setStatus("OP");
          periodDates.setPayrollRunDate(payrollRungregorian);
          periodDates.setDirectDepositeDate(DirDepositegregorian);
          periodDates.setEhcmPeriod(hijiridate.split("-")[1] + "-" + hijiridate.split("-")[2]);
          periodDates.setStartDate(startDate);
          periodDates.setEndDate(periodEnd);

          MM = MM + 1;
          if (MM > 12) {
            MM = MM - 12;
            YYYY = YYYY + 1;
          }

          hijiridate = "01-" + Numformatter.format(MM) + "-" + YYYY;
          fstGerDate = UtilityDAO.convertToGregorian(hijiridate);
          nextStartDate = format.parse(fstGerDate);

          st = conn.prepareStatement(
              " select to_char(max(gregorian_date),'yyyy-mm-dd') as endofdate from eut_hijri_dates where gregorian_date >= '"
                  + startDate + "' and gregorian_date <'" + nextStartDate + "'");
          rs = st.executeQuery();
          if (rs.next()) {
            periodEnd = rs.getDate("endofdate");
          }
          periodDates.setEndDate(periodEnd);
          OBDal.getInstance().save(periodDates);
          OBDal.getInstance().flush();
          startDate = nextStartDate;
        }
      } else if (periodType == 6) {
        String hijiridate = UtilityDAO.convertTohijriDate(periodEnd.toString());
        MM = Integer.valueOf(hijiridate.split("-")[1]);
        YYYY = Integer.valueOf(hijiridate.split("-")[2]);
        if (MM == 1) {
          MM = 12;
          YYYY = YYYY - 1;
        } else
          MM = MM - 1;
        String fstGerDate = UtilityDAO
            .convertToGregorian("01-" + Numformatter.format(MM) + "-" + Numformatter.format(YYYY));
        Date startDate = format.parse(fstGerDate);
        maxYear = YYYY + year;
        String MaxGregDate = UtilityDAO
            .convertToGregorian("01-" + Numformatter.format(MM) + "-" + maxYear);
        Date fmtMaxGregDate = format.parse(MaxGregDate);
        if (periodStartDate.compareTo(startDate) > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Startdate_Validation"));
        }
        st = conn.prepareStatement(
            " delete from ehcm_payrolldef_period where ehcm_payroll_definition_id=?");
        st.setString(1, payrollid);
        st.executeUpdate();
        conn.commit();
        while (startDate.compareTo(fmtMaxGregDate) < 0) {
          EHCMPayrolldefPeriod periodDates = OBProvider.getInstance()
              .get(EHCMPayrolldefPeriod.class);
          periodDates.setClient(OBDal.getInstance().get(Client.class, clientId));
          periodDates.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
          periodDates.setEnabled(true);
          periodDates.setCreatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setCreationDate(new java.util.Date());
          periodDates.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setUpdated(new java.util.Date());
          periodDates.setEhcmPayrollDefinition(PayrollDefinition);
          periodDates.setStatus("OP");
          periodDates.setEhcmPeriod(hijiridate.split("-")[1] + "-" + hijiridate.split("-")[2]);
          periodDates.setStartDate(startDate);
          periodDates.setEndDate(periodEnd);

          MM = MM + 2;
          if (MM > 12) {
            MM = MM - 12;
            YYYY = YYYY + 1;
          }

          hijiridate = "01-" + Numformatter.format(MM) + "-" + YYYY;
          fstGerDate = UtilityDAO.convertToGregorian(hijiridate);
          nextStartDate = format.parse(fstGerDate);

          st = conn.prepareStatement(
              " select to_char(max(gregorian_date),'yyyy-mm-dd') as endofdate from eut_hijri_dates where gregorian_date >= '"
                  + startDate + "' and gregorian_date <'" + nextStartDate + "'");
          rs = st.executeQuery();
          if (rs.next()) {
            periodEnd = rs.getDate("endofdate");
          }
          periodDates.setEndDate(periodEnd);
          periodDates.setPayrollRunDate(periodEnd);
          periodDates.setDirectDepositeDate(periodEnd);
          String hijirienddate = UtilityDAO.convertTohijriDate(periodEnd.toString());
          periodDates
              .setEhcmPeriod(hijirienddate.split("-")[1] + "-" + hijirienddate.split("-")[2]);

          OBDal.getInstance().save(periodDates);
          OBDal.getInstance().flush();

          startDate = nextStartDate;
        }
      } else if (periodType == 4) {
        String hijiridate = UtilityDAO.convertTohijriDate(periodEnd.toString());
        MM = Integer.valueOf(hijiridate.split("-")[1]);
        YYYY = Integer.valueOf(hijiridate.split("-")[2]);
        if (MM < 3) {
          MM = 10 + MM;
          YYYY = YYYY - 1;
        } else
          MM = MM - 2;
        String fstGerDate = UtilityDAO
            .convertToGregorian("01-" + Numformatter.format(MM) + "-" + Numformatter.format(YYYY));
        Date startDate = format.parse(fstGerDate);
        maxYear = YYYY + year;
        String MaxGregDate = UtilityDAO
            .convertToGregorian("01-" + Numformatter.format(MM) + "-" + maxYear);
        Date fmtMaxGregDate = format.parse(MaxGregDate);
        if (periodStartDate.compareTo(startDate) > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Startdate_Validation"));
        }
        st = conn.prepareStatement(
            " delete from ehcm_payrolldef_period where ehcm_payroll_definition_id=?");
        st.setString(1, payrollid);
        st.executeUpdate();
        conn.commit();
        while (startDate.compareTo(fmtMaxGregDate) < 0) {
          EHCMPayrolldefPeriod periodDates = OBProvider.getInstance()
              .get(EHCMPayrolldefPeriod.class);
          periodDates.setClient(OBDal.getInstance().get(Client.class, clientId));
          periodDates.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
          periodDates.setEnabled(true);
          periodDates.setCreatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setCreationDate(new java.util.Date());
          periodDates.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setUpdated(new java.util.Date());
          periodDates.setEhcmPayrollDefinition(PayrollDefinition);
          periodDates.setStatus("OP");
          periodDates.setEhcmPeriod(hijiridate.split("-")[1] + "-" + hijiridate.split("-")[2]);
          periodDates.setStartDate(startDate);
          periodDates.setEndDate(periodEnd);

          MM = MM + 3;
          if (MM > 12) {
            MM = MM - 12;
            YYYY = YYYY + 1;
          }
          hijiridate = "01-" + Numformatter.format(MM) + "-" + YYYY;
          fstGerDate = UtilityDAO.convertToGregorian(hijiridate);
          nextStartDate = format.parse(fstGerDate);

          st = conn.prepareStatement(
              " select to_char(max(gregorian_date),'yyyy-mm-dd') as endofdate from eut_hijri_dates where gregorian_date >= '"
                  + startDate + "' and gregorian_date <'" + nextStartDate + "'");
          rs = st.executeQuery();
          if (rs.next()) {
            periodEnd = rs.getDate("endofdate");
          }
          periodDates.setEndDate(periodEnd);
          periodDates.setPayrollRunDate(periodEnd);
          periodDates.setDirectDepositeDate(periodEnd);
          String hijirienddate = UtilityDAO.convertTohijriDate(periodEnd.toString());
          periodDates
              .setEhcmPeriod(hijirienddate.split("-")[1] + "-" + hijirienddate.split("-")[2]);
          OBDal.getInstance().save(periodDates);
          OBDal.getInstance().flush();
          startDate = nextStartDate;
        }
      } else if (periodType == 2) {
        String hijiridate = UtilityDAO.convertTohijriDate(periodEnd.toString());
        MM = Integer.valueOf(hijiridate.split("-")[1]);
        YYYY = Integer.valueOf(hijiridate.split("-")[2]);
        if (MM < 6) {
          MM = 7 + MM;
          YYYY = YYYY - 1;
        } else
          MM = MM - 5;
        String fstGerDate = UtilityDAO
            .convertToGregorian("01-" + Numformatter.format(MM) + "-" + Numformatter.format(YYYY));
        Date startDate = format.parse(fstGerDate);
        maxYear = YYYY + year;
        String MaxGregDate = UtilityDAO
            .convertToGregorian("01-" + Numformatter.format(MM) + "-" + maxYear);
        Date fmtMaxGregDate = format.parse(MaxGregDate);
        if (periodStartDate.compareTo(startDate) > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Startdate_Validation"));
        }
        st = conn.prepareStatement(
            " delete from ehcm_payrolldef_period where ehcm_payroll_definition_id=?");
        st.setString(1, payrollid);
        st.executeUpdate();
        conn.commit();
        while (startDate.compareTo(fmtMaxGregDate) < 0) {
          EHCMPayrolldefPeriod periodDates = OBProvider.getInstance()
              .get(EHCMPayrolldefPeriod.class);
          periodDates.setClient(OBDal.getInstance().get(Client.class, clientId));
          periodDates.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
          periodDates.setEnabled(true);
          periodDates.setCreatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setCreationDate(new java.util.Date());
          periodDates.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setUpdated(new java.util.Date());
          periodDates.setEhcmPayrollDefinition(PayrollDefinition);
          periodDates.setStatus("OP");
          periodDates.setEhcmPeriod(hijiridate.split("-")[1] + "-" + hijiridate.split("-")[2]);
          periodDates.setStartDate(startDate);
          periodDates.setEndDate(periodEnd);

          MM = MM + 6;
          if (MM > 12) {
            MM = MM - 12;
            YYYY = YYYY + 1;
          }
          hijiridate = "01-" + Numformatter.format(MM) + "-" + YYYY;
          fstGerDate = UtilityDAO.convertToGregorian(hijiridate);
          nextStartDate = format.parse(fstGerDate);

          st = conn.prepareStatement(
              " select to_char(max(gregorian_date),'yyyy-mm-dd') as endofdate from eut_hijri_dates where gregorian_date >= '"
                  + startDate + "' and gregorian_date <'" + nextStartDate + "'");
          rs = st.executeQuery();
          if (rs.next()) {
            periodEnd = rs.getDate("endofdate");
          }
          periodDates.setEndDate(periodEnd);
          periodDates.setPayrollRunDate(periodEnd);
          periodDates.setDirectDepositeDate(periodEnd);
          String hijirienddate = UtilityDAO.convertTohijriDate(periodEnd.toString());
          periodDates
              .setEhcmPeriod(hijirienddate.split("-")[1] + "-" + hijirienddate.split("-")[2]);
          OBDal.getInstance().save(periodDates);
          OBDal.getInstance().flush();
          startDate = nextStartDate;
        }
      } else if (periodType == 52) {
        String hijiridate = UtilityDAO.convertTohijriDate(periodEnd.toString());
        Calendar c = Calendar.getInstance();
        c.setTime(periodEnd);
        c.add(Calendar.DATE, -6); // number of days to add
        String startdt = format.format(c.getTime());
        Date startDate = format.parse(startdt);
        Date endDate = periodEnd;
        // find Maxdate
        String Maxhijiridate = UtilityDAO.convertTohijriDate(startdt);
        YYYY = Integer.valueOf(Maxhijiridate.split("-")[2]);
        maxYear = YYYY + year;
        String MaxGregDate = UtilityDAO.convertToGregorian(
            (Maxhijiridate.split("-")[0]) + "-" + (Maxhijiridate.split("-")[1]) + "-" + maxYear);
        Date fmtMaxGregDate = format.parse(MaxGregDate);
        String enddt = "", hijiriEnddate = "", oldMonth = hijiridate.split("-")[1];
        if (periodStartDate.compareTo(startDate) > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Startdate_Validation"));
        }
        st = conn.prepareStatement(
            " delete from ehcm_payrolldef_period where ehcm_payroll_definition_id=?");
        st.setString(1, payrollid);
        st.executeUpdate();
        conn.commit();
        int j = 0;
        int max = periodType * year;
        while (startDate.compareTo(fmtMaxGregDate) < 0) {
          j++;
          EHCMPayrolldefPeriod periodDates = OBProvider.getInstance()
              .get(EHCMPayrolldefPeriod.class);
          periodDates.setClient(OBDal.getInstance().get(Client.class, clientId));
          periodDates.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
          periodDates.setEnabled(true);
          periodDates.setCreatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setCreationDate(new java.util.Date());
          periodDates.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setUpdated(new java.util.Date());
          periodDates.setEhcmPayrollDefinition(PayrollDefinition);
          periodDates.setStatus("OP");
          periodDates.setStartDate(startDate);

          c.setTime(startDate);
          c.add(Calendar.DATE, 6);
          enddt = format.format(c.getTime());
          endDate = format.parse(enddt);
          hijiriEnddate = UtilityDAO.convertTohijriDate(enddt);
          if (!oldMonth.equals(hijiriEnddate.split("-")[1])) {
            j = 1;
          }

          periodDates.setEhcmPeriod(
              "W" + j + "-" + hijiriEnddate.split("-")[1] + "-" + hijiriEnddate.split("-")[2]);
          periodDates.setEndDate(endDate);
          periodDates.setPayrollRunDate(endDate);
          periodDates.setDirectDepositeDate(endDate);

          oldMonth = hijiriEnddate.split("-")[1];
          OBDal.getInstance().save(periodDates);
          OBDal.getInstance().flush();
          c.setTime(periodDates.getStartDate());
          c.add(Calendar.DATE, 7);
          startdt = format.format(c.getTime());
          startDate = format.parse(startdt);
        }
      } else if (periodType == 24) {
        String hijiridate = UtilityDAO.convertTohijriDate(periodEnd.toString());
        Calendar c = Calendar.getInstance();
        c.setTime(periodEnd);
        c.add(Calendar.DATE, -13); // number of days to add
        String startdt = format.format(c.getTime());
        Date startDate = format.parse(startdt);
        Date endDate = periodEnd;
        // maxdate
        String Maxhijiridate = UtilityDAO.convertTohijriDate(startdt);
        YYYY = Integer.valueOf(Maxhijiridate.split("-")[2]);
        maxYear = YYYY + year;
        String MaxGregDate = UtilityDAO.convertToGregorian(
            (Maxhijiridate.split("-")[0]) + "-" + (Maxhijiridate.split("-")[1]) + "-" + maxYear);
        Date fmtMaxGregDate = format.parse(MaxGregDate);
        String enddt = "", hijiriEnddate = "", oldMonth = hijiridate.split("-")[1];
        if (periodStartDate.compareTo(startDate) > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Startdate_Validation"));
        }
        st = conn.prepareStatement(
            " delete from ehcm_payrolldef_period where ehcm_payroll_definition_id=?");
        st.setString(1, payrollid);
        st.executeUpdate();
        conn.commit();
        int j = 0;
        int max = periodType * year;
        while (startDate.compareTo(fmtMaxGregDate) < 0) {
          j++;
          EHCMPayrolldefPeriod periodDates = OBProvider.getInstance()
              .get(EHCMPayrolldefPeriod.class);
          periodDates.setClient(OBDal.getInstance().get(Client.class, clientId));
          periodDates.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
          periodDates.setEnabled(true);
          periodDates.setCreatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setCreationDate(new java.util.Date());
          periodDates.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
          periodDates.setUpdated(new java.util.Date());
          periodDates.setEhcmPayrollDefinition(PayrollDefinition);
          periodDates.setStatus("OP");
          periodDates.setStartDate(startDate);

          c.setTime(periodDates.getStartDate());
          c.add(Calendar.DATE, 13);
          enddt = format.format(c.getTime());
          endDate = format.parse(enddt);
          hijiriEnddate = UtilityDAO.convertTohijriDate(enddt);
          if (!oldMonth.equals(hijiriEnddate.split("-")[1])) {
            j = 1;
          }
          periodDates.setEhcmPeriod(
              "W" + j + "-" + hijiriEnddate.split("-")[1] + "-" + hijiriEnddate.split("-")[2]);
          periodDates.setEndDate(endDate);
          periodDates.setPayrollRunDate(endDate);
          periodDates.setDirectDepositeDate(endDate);
          oldMonth = hijiriEnddate.split("-")[1];
          OBDal.getInstance().save(periodDates);
          OBDal.getInstance().flush();
          c.setTime(periodDates.getStartDate());
          c.add(Calendar.DATE, 14);
          startdt = format.format(c.getTime());
          startDate = format.parse(startdt);
        }
      }
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }

  }
}
