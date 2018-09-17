package sa.elm.ob.hcm.ad_forms.personalpaymentmethod.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.currency.Currency;

import sa.elm.ob.finance.EfinBankBranch;
import sa.elm.ob.hcm.EHCMPayPmtTypeMethod;
import sa.elm.ob.hcm.EHCMPersonalPaymethd;
import sa.elm.ob.hcm.EHCMPpmBankdetail;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.hcm.ad_forms.personalpaymentmethod.vo.PersonalPaymentMethodVO;
import sa.elm.ob.utility.util.Utility;

//Payment Method DAO Implement file
public class PersonalPaymentMethodDAO implements PaymentMethodDAO {
  private Connection conn = null;
  VariablesSecureApp vars = null;
  private static Logger log4j = Logger.getLogger(EmployeeDAO.class);

  public PersonalPaymentMethodDAO(Connection con) {
    this.conn = con;
  }

  /**
   * 
   * @param clientId
   * @return personal payment method code
   */
  public List<PersonalPaymentMethodVO> getpaymenttypecode(String clientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    List<PersonalPaymentMethodVO> ls = new ArrayList<PersonalPaymentMethodVO>();
    try {
      sql = "select ehcm_pay_pmt_type_method_id, paymenttypecode,paymenttypename from ehcm_pay_pmt_type_method where ad_client_id=? ";
      st = conn.prepareStatement(sql);
      st.setString(1, clientId);
      log4j.debug("getpaymenttypecode:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        PersonalPaymentMethodVO eVO = new PersonalPaymentMethodVO();
        eVO.setpayrollpaytypemethodId(
            Utility.nullToEmpty(rs.getString("ehcm_pay_pmt_type_method_id")));
        eVO.setpaymenttypecode(Utility.nullToEmpty(rs.getString("paymenttypecode")));
        eVO.setpaymenttypename(Utility.nullToEmpty(rs.getString("paymenttypename")));
        ls.add(eVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getpaymenttypecode", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in getpaymenttypecode", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getpaymenttypecode", e);
        return ls;
      }
    }
    return ls;
  }

  /**
   * 
   * @param clientId
   * @return currency
   */
  public List<PersonalPaymentMethodVO> getcurrency(String clientId, String paycode) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    List<PersonalPaymentMethodVO> ls = new ArrayList<PersonalPaymentMethodVO>();
    try {
      sql = "SELECT c_currency_id,iso_code FROM c_currency " + "WHERE c_currency_id IN "
          + "(SELECT c_currency_id FROM ehcm_payroll_payment_method lne "
          + "JOIN ehcm_pay_pmt_type_method hdr ON hdr.ehcm_pay_pmt_type_method_id = lne.ehcm_pay_pmt_type_method_id"
          + " WHERE hdr.ehcm_pay_pmt_type_method_id = ? AND lne.ad_client_id = ?) ";
      st = conn.prepareStatement(sql);
      log4j.debug(paycode);
      st.setString(1, paycode);
      st.setString(2, clientId);
      log4j.debug("currency:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        PersonalPaymentMethodVO eVO = new PersonalPaymentMethodVO();
        eVO.setcurrencyId(Utility.nullToEmpty(rs.getString("c_currency_id")));
        eVO.setcurrency(Utility.nullToEmpty(rs.getString("iso_code")));
        ls.add(eVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getcurrency", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in getcurrency", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getcurrency", e);
        return ls;
      }
    }
    return ls;
  }

  /**
   * 
   * @param clientId
   * @param PersonalPaymethdId
   * @param searchFlag
   * @param vo
   * @param sortColName
   * @param sortColType
   * @return bank detail records
   */
  public List<PersonalPaymentMethodVO> getbankdetaillist(String clientId, String PersonalPaymethdId,
      String searchFlag, PersonalPaymentMethodVO vo, String sortColName, String sortColType) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    List<PersonalPaymentMethodVO> ls = new ArrayList<PersonalPaymentMethodVO>();
    try {
      sql = "select ln.ehcm_ppm_bankdetail_id, ln.efin_bank_id,ln.bankbranch,ln.accountname,ln.accountnumber,ln.percentage, "
          + " coalesce(eut_convert_to_hijri(to_char(ln.startdate,'yyyy-MM-dd')),null) as startdate, "
          + " coalesce(eut_convert_to_hijri(to_char(ln.enddate,'yyyy-MM-dd')),null) as enddate,ln.isdefault from ehcm_ppm_bankdetail ln left join efin_bank bk on bk.efin_bank_id=ln.efin_bank_id "
          + " where ln.ad_client_id=? and ln.ehcm_personal_paymethd_id=? ";

      if (searchFlag.equals("true")) {
        if (!StringUtils.isEmpty(vo.getbankname()))
          // if (vo.getbankname() != null)
          sql += " and bk.bankname ilike '%" + vo.getbankname() + "%'";
        if (vo.getbankbranch() != null)
          sql += " and ln.bankbranch ilike '%" + vo.getbankbranch() + "%'";
        if (vo.getaccountname() != null)
          sql += " and ln.accountname ilike '%" + vo.getaccountname() + "%'";
        if (vo.getaccountnum() != null)
          sql += " and ln.accountnumber ilike '%" + vo.getaccountnum() + "%'";
        if (vo.getpercentage() != null)
          sql += " and ln.percentage ='" + vo.getpercentage() + "'";
        if (vo.getStartdate() != null)
          sql += " and ln.startdate " + vo.getStartdate().split("##")[0] + " to_timestamp('"
              + vo.getStartdate().split("##")[1].toString() + "', 'yyyy-MM-dd HH24:MI:SS')";
        if (vo.getEnddate() != null)
          sql += " and ln.enddate " + vo.getEnddate().split("##")[0] + " to_timestamp('"
              + vo.getEnddate().split("##")[1].toString() + "', 'yyyy-MM-dd HH24:MI:SS')";

        if (vo.getDefaultval() != null)
          sql += " and ln.isdefault ='" + vo.getDefaultval() + "'";

      }
      // Sorting
      if (sortColName.equals("Bank_Name")) {
        sql += " order by bk.bankname ";
      } else if (sortColName.equals("Bank_Branch")) {
        sql += " order by ln.bankbranch ";
      } else if (sortColName.equals("Account_Name")) {
        sql += " order by ln.accountname ";
      } else if (sortColName.equals("Account_Number")) {
        sql += " order by ln.accountnumber ";
      } else if (sortColName.equals("Percentage")) {
        sql += " order by ln.percentage ";
      } else if (sortColName.equals("Start_Date")) {
        sql += " order by ln.startdate ";
      } else if (sortColName.equals("End_Date")) {
        sql += " order by ln.enddate ";
      } else if (sortColName.equals("Default")) {
        sql += " order by ln.isdefault ";
      } else {
        sql += " order by " + sortColName;
      }
      sql += " " + sortColType;

      st = conn.prepareStatement(sql);
      st.setString(1, clientId);
      st.setString(2, PersonalPaymethdId);
      log4j.debug("query for bank detail:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        PersonalPaymentMethodVO eVO = new PersonalPaymentMethodVO();
        eVO.setbankdetailId(Utility.nullToEmpty(rs.getString("ehcm_ppm_bankdetail_id")));
        eVO.setbankname(Utility.nullToEmpty(rs.getString("efin_bank_id")));
        eVO.setbankbranch(Utility.nullToEmpty(rs.getString("bankbranch")));
        eVO.setaccountname(Utility.nullToEmpty(rs.getString("accountname")));
        eVO.setaccountnum(Utility.nullToEmpty(rs.getString("accountnumber")));
        eVO.setpercentage(Utility.nullToEmpty(rs.getString("percentage")));
        eVO.setStartdate(Utility.nullToEmpty(rs.getString("startdate")));
        eVO.setEnddate(Utility.nullToEmpty(rs.getString("enddate")));
        if (rs.getString("isdefault").equals("Y")) {
          eVO.setisdefault(true);
        } else {
          eVO.setisdefault(false);
        }
        ls.add(eVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getbankdetaillist", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in getbankdetaillist", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getbankdetaillist", e);
        return ls;
      }
    }
    return ls;
  }

  /**
   * 
   * @param clientId
   * @return bank name
   */
  public List<PersonalPaymentMethodVO> getbankname(String clientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    List<PersonalPaymentMethodVO> ls = new ArrayList<PersonalPaymentMethodVO>();
    try {
      sql = "select efin_bank_id,bankname from efin_bank where ad_client_id=?";
      st = conn.prepareStatement(sql);
      st.setString(1, clientId);
      log4j.debug("bankname:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        PersonalPaymentMethodVO eVO = new PersonalPaymentMethodVO();
        eVO.setbankdetailId(Utility.nullToEmpty(rs.getString("efin_bank_id")));
        eVO.setbankname(Utility.nullToEmpty(rs.getString("bankname")));
        ls.add(eVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getbankname", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in getbankname", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getbankname", e);
        return ls;
      }
    }
    return ls;
  }

  /**
   * 
   * @param clientId
   * @param userId
   * @param vo
   * @return insert records in payment method
   */
  public String addPerPayMethod(String clientId, String userId, PersonalPaymentMethodVO vo) {
    EHCMPersonalPaymethd perpaymethod = null;
    try {
      OBContext.setAdminMode();
      perpaymethod = OBProvider.getInstance().get(EHCMPersonalPaymethd.class);
      perpaymethod
          .setCode(OBDal.getInstance().get(EHCMPayPmtTypeMethod.class, vo.getpaymenttypecode()));
      perpaymethod
          .setName(OBDal.getInstance().get(EHCMPayPmtTypeMethod.class, vo.getpaymenttypename()));
      perpaymethod.setCurrency(OBDal.getInstance().get(Currency.class, vo.getcurrency()));
      perpaymethod
          .setEhcmEmpPerinfo(OBDal.getInstance().get(EhcmEmpPerInfo.class, vo.getehcmemployeeId()));
      perpaymethod.setDefault(vo.getisdefault());
      OBDal.getInstance().save(perpaymethod);
      OBDal.getInstance().flush();
      log4j.debug("getid perpaymethod:" + perpaymethod.getId());
      return perpaymethod.getId();

    } catch (Exception e) {
      log4j.error("error while addPerPayMethod", e);
      return null;
      // TODO: handle exception
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * 
   * @param clientId
   * @param userId
   * @param vo
   * @return update records in payment method
   */
  public String updateaddPerPayMethod(String clientId, String userId, PersonalPaymentMethodVO vo,
      String perpaymethodId) {
    EHCMPersonalPaymethd perpaymethod = null;
    try {
      OBContext.setAdminMode();
      perpaymethod = OBDal.getInstance().get(EHCMPersonalPaymethd.class, perpaymethodId);
      perpaymethod
          .setCode(OBDal.getInstance().get(EHCMPayPmtTypeMethod.class, vo.getpaymenttypecode()));
      perpaymethod
          .setName(OBDal.getInstance().get(EHCMPayPmtTypeMethod.class, vo.getpaymenttypename()));
      perpaymethod.setCurrency(OBDal.getInstance().get(Currency.class, vo.getcurrency()));
      perpaymethod
          .setEhcmEmpPerinfo(OBDal.getInstance().get(EhcmEmpPerInfo.class, vo.getehcmemployeeId()));
      perpaymethod.setDefault(vo.getisdefault());
      OBDal.getInstance().save(perpaymethod);
      OBDal.getInstance().flush();
      return perpaymethod.getId();
    } catch (Exception e) {
      log4j.error("error while updateaddPerPayMethod", e);
      return null;
      // TODO: handle exception
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * 
   * @param clientId
   * @param EmployeeId
   * @param searchFlag
   * @param vo
   * @param sortColName
   * @param sortColName
   * @return get payment method records
   */
  public List<PersonalPaymentMethodVO> GetPaymentMethodList(String clientId, String EmployeeId,
      String searchFlag, PersonalPaymentMethodVO vo, String sortColName, String sortColType) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    List<PersonalPaymentMethodVO> ls = new ArrayList<PersonalPaymentMethodVO>();
    try {
      sql = "select ehcm_personal_paymethd_id, pptm.paymenttypecode as value ,pptm.paymenttypename as name ,cr.iso_code as curname,ppm.isdefault as default  "
          + "from ehcm_personal_paymethd ppm "
          + "join ehcm_pay_pmt_type_method pptm on pptm.ehcm_pay_pmt_type_method_id=ppm.value "
          + "join c_currency cr on cr.c_currency_id=ppm.c_currency_id "
          + "where ppm.ad_client_id=? and ppm.ehcm_emp_perinfo_id=? ";

      if (searchFlag.equals("true")) {
        if (vo.getpaymenttypecode() != null)
          sql += " and pptm.paymenttypecode ilike '%" + vo.getpaymenttypecode() + "%'";
        if (vo.getpaymenttypename() != null)
          sql += " and pptm.paymenttypename ilike '%" + vo.getpaymenttypename() + "%'";
        if (vo.getcurrency() != null)
          sql += " and cr.iso_code ilike  '%" + vo.getcurrency() + "%'";
        if (vo.getDefaultval() != null)
          sql += " and ppm.isdefault ='" + vo.getDefaultval() + "'";
      }
      // Sorting
      if (sortColName.equals("Payment_Type_Code")) {
        sql += " order by pptm.paymenttypecode ";
      } else if (sortColName.equals("Payment_Type_Name")) {
        sql += " order by pptm.paymenttypename ";
      } else if (sortColName.equals("Currency")) {
        sql += " order by cr.iso_code ";
      } else if (sortColName.equals("Default")) {
        sql += " order by ppm.isdefault ";
      } else {
        sql += " order by " + sortColName;
      }
      sql += " " + sortColType;

      st = conn.prepareStatement(sql);
      st.setString(1, clientId);
      st.setString(2, EmployeeId);
      log4j.debug("query for payment method:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        PersonalPaymentMethodVO eVO = new PersonalPaymentMethodVO();
        eVO.setpersonalpaymentmethodId(
            Utility.nullToEmpty(rs.getString("ehcm_personal_paymethd_id")));
        eVO.setpaymenttypecode(Utility.nullToEmpty(rs.getString("value")));
        eVO.setpaymenttypename(Utility.nullToEmpty(rs.getString("name")));
        eVO.setcurrency(Utility.nullToEmpty(rs.getString("curname")));
        eVO.setDefaultval(Utility.nullToEmpty(rs.getString("default").equals("Y") ? "Yes" : "No"));

        ls.add(eVO);

      }
    } catch (final SQLException e) {
      log4j.error("Exception in GetPaymentMethodList", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in GetPaymentMethodList", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in GetPaymentMethodList", e);
        return ls;
      }
    }
    return ls;
  }

  /**
   * 
   * @param payrollpaytypemethodId
   * @return get Payroll Payment detail
   */
  public List<PersonalPaymentMethodVO> Getpayrollpaymentdetailrecords(
      String payrollpaytypemethodId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    List<PersonalPaymentMethodVO> ls = new ArrayList<PersonalPaymentMethodVO>();
    try {
      sql = "select distinct header.ehcm_pay_pmt_type_method_id,cr.iso_code as curname,  line.c_currency_id as c_currency_id from ehcm_pay_pmt_type_method  header "
          + "left join EHCM_Payroll_Payment_Method line on line.ehcm_pay_pmt_type_method_id= header.ehcm_pay_pmt_type_method_id "
          + "left join c_currency cr on cr.c_currency_id=line.c_currency_id "
          + "where header.ehcm_pay_pmt_type_method_id=?";
      st = conn.prepareStatement(sql);
      st.setString(1, payrollpaytypemethodId);
      log4j.debug("query for payrollpaymentdetail:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        PersonalPaymentMethodVO eVO = new PersonalPaymentMethodVO();
        eVO.setpersonalpaymentmethodId(
            Utility.nullToEmpty(rs.getString("ehcm_pay_pmt_type_method_id")));
        eVO.setcurrency(Utility.nullToEmpty(rs.getString("curname")));
        eVO.setcurrencyId(Utility.nullToEmpty(rs.getString("c_currency_id")));
        ls.add(eVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in Getpayrollpaymentdetailrecords", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in Getpayrollpaymentdetailrecords", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in Getpayrollpaymentdetailrecords", e);
        return ls;
      }
    }
    return ls;
  }

  /**
   * 
   * @param clientId
   * @param PersonalPaymethdId
   * @param searchFlag
   * @param vo
   * @return get count of Bank detail records
   */
  public int getBankdetailCount(String clientId, String PersonalPaymethdId, String searchFlag,
      PersonalPaymentMethodVO vo) {
    PreparedStatement st = null;
    ResultSet rs = null;
    int totalRecord = 0;
    String sqlQuery = "";
    try {
      sqlQuery = " select count(ehcm_ppm_bankdetail_id) as totalRecord  from ehcm_ppm_bankdetail ln "
          + "left join efin_bank bk on bk.efin_bank_id=ln.efin_bank_id"
          + " where ln.ehcm_personal_paymethd_id = ? ";
      if (searchFlag.equals("true")) {
        if (!StringUtils.isEmpty(vo.getbankname()))
          // if (vo.getbankname() != null)
          sqlQuery += " and bk.bankname ilike '%" + vo.getbankname() + "%'";
        if (vo.getbankbranch() != null)
          sqlQuery += " and ln.bankbranch ilike '%" + vo.getbankbranch() + "%'";
        if (vo.getaccountname() != null)
          sqlQuery += " and ln.accountname ilike '%" + vo.getaccountname() + "%'";
        if (vo.getaccountnum() != null)
          sqlQuery += " and ln.accountnumber ilike '%" + vo.getaccountnum() + "%'";
        if (vo.getpercentage() != null)
          sqlQuery += " and ln.percentage ='" + vo.getpercentage() + "'";
        if (vo.getStartdate() != null)
          sqlQuery += " and ln.startdate " + vo.getStartdate().split("##")[0] + " to_timestamp("
              + vo.getStartdate().split("##")[1].toString() + "', 'yyyy-MM-dd HH24:MI:SS')";
        if (vo.getEnddate() != null)
          sqlQuery += " and ln.enddate " + vo.getEnddate().split("##")[0] + " to_timestamp('"
              + vo.getEnddate().split("##")[1].toString() + "', 'yyyy-MM-dd HH24:MI:SS')";
        if (vo.getDefaultval() != null)
          sqlQuery += " and ln.isdefault ='" + vo.getDefaultval() + "'";

      }
      st = conn.prepareStatement(sqlQuery);
      st.setString(1, PersonalPaymethdId);
      rs = st.executeQuery();
      if (rs.next())
        totalRecord = rs.getInt("totalRecord");
    } catch (final SQLException e) {
      log4j.error("", e);
    } catch (final Exception e) {
      log4j.error("", e);
    } finally {
      try {
        st.close();
      } catch (final SQLException e) {
        log4j.error("", e);
      }
    }
    return totalRecord;
  }

  /**
   * 
   * @param clientId
   * @param EmployeeId
   * @param searchFlag
   * @param vo
   * @return get count of PaymentMethod records
   */
  public int getPaymentMethodCount(String clientId, String EmployeeId, String searchFlag,
      PersonalPaymentMethodVO vo) {
    PreparedStatement st = null;
    ResultSet rs = null;
    int totalRecord = 0;
    String sqlQuery = "";
    try {
      sqlQuery = "select count(ehcm_personal_paymethd_id) as totalRecord "
          + " from ehcm_personal_paymethd ppm join ehcm_pay_pmt_type_method pptm on pptm.ehcm_pay_pmt_type_method_id=ppm.value "
          + " join c_currency cr on cr.c_currency_id=ppm.c_currency_id where ppm.ad_client_id=? and ppm.ehcm_emp_perinfo_id=? ";
      if (searchFlag.equals("true")) {

        if (vo.getpaymenttypecode() != null)
          sqlQuery += " and pptm.paymenttypecode ilike '%" + vo.getpaymenttypecode() + "%'";
        if (vo.getpaymenttypename() != null)
          sqlQuery += " and pptm.paymenttypename ilike '%" + vo.getpaymenttypename() + "%'";
        if (vo.getcurrency() != null)
          sqlQuery += " and cr.iso_code ilike  '%" + vo.getcurrency() + "'";
        if (vo.getDefaultval() != null)
          sqlQuery += " and ppm.isdefault ='" + vo.getDefaultval() + "'";

      }

      st = conn.prepareStatement(sqlQuery);
      st.setString(1, clientId);
      st.setString(2, EmployeeId);
      rs = st.executeQuery();
      if (rs.next())
        totalRecord = rs.getInt("totalRecord");
    } catch (final SQLException e) {
      log4j.error("", e);
    } catch (final Exception e) {
      log4j.error("", e);
    } finally {
      try {
        st.close();
      } catch (final SQLException e) {
        log4j.error("", e);
      }
    }
    return totalRecord;
  }

  /**
   * 
   * @param percentange
   * @param paymentMethodId
   * @param clientId
   * @param bankDetailId
   * @return true/false (if greater than 100 then False else True)
   */
  public boolean checkPercentageValidation(BigDecimal percentange, String paymentMethodId,
      String clientId, String bankDetailId) {
    List<EHCMPpmBankdetail> ls = new ArrayList<EHCMPpmBankdetail>();
    BigDecimal percent = percentange;
    String sql = "";
    try {
      if (percent.compareTo(new BigDecimal(0)) < 0) {
        return false;
      } else {
        if (bankDetailId != null && !bankDetailId.contains("jqg")) {
          sql = "and e.id <>:bankdetailId ";
        }
        OBQuery<EHCMPpmBankdetail> count = OBDal.getInstance().createQuery(EHCMPpmBankdetail.class,
            " as e where e.ehcmPersonalPaymethd.id=:paymentmethodId and e.client.id=:clientId "
                + sql);
        count.setNamedParameter("paymentmethodId", paymentMethodId);
        count.setNamedParameter("clientId", clientId);
        if (bankDetailId != null && !bankDetailId.contains("jqg")) {
          count.setNamedParameter("bankdetailId", bankDetailId);
        }
        ls = count.list();
        if (ls.size() > 0) {
          for (EHCMPpmBankdetail bankdetail : ls) {
            percent = percent.add(bankdetail.getPercentage());

          }
        }
        if (percent.compareTo(new BigDecimal(100)) > 0) {
          return false;
        } else {
          return true;
        }
      }

    } catch (Exception e) {
      log4j.error("error while checkPercentageValidation", e);
      return false;
    }

  }

  /**
   * 
   * @param bankDetailId
   * @param accountnumber
   * @param personalpaymentmethodId
   * @return true/false (if unique return true else return false based on the start date and end
   *         date)
   */
  public boolean checkaccountNumber(String bankDetailId, String accountnumber,
      String personalpaymentmethodId, String rowids, String startdate, String enddate) {
    List<EHCMPpmBankdetail> ls = new ArrayList<EHCMPpmBankdetail>();
    String hql = "";
    try {

      if (rowids.contains("jqg")) {
        rowids = null;
      }

      String st_date = "", st_enddate = "";
      Date end_date = null;
      st_date = convertToGregorian_tochar(startdate);

      DateFormat dateFormat = Utility.dateFormat;
      Date start_date = dateFormat.parse(st_date);
      if (enddate != "") {
        st_enddate = convertToGregorian_tochar(enddate);
        end_date = dateFormat.parse(st_enddate);
      } else {
        st_enddate = "21-06-2058";
      }
      if (rowids != null) {
        hql = "  and e.id <>:rowid ";
      }
      OBQuery<EHCMPpmBankdetail> count = OBDal.getInstance().createQuery(EHCMPpmBankdetail.class,
          " as e where  ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:st_date) "
              + " and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:st_enddate)) "
              + " or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:st_date) "
              + " and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:st_enddate))) and e.efinBank.id=:BankId and e.accountNumber=:accountnumber and e.ehcmPersonalPaymethd.id=:personalpaymethod "
              + hql);
      count.setNamedParameter("st_date", st_date);
      count.setNamedParameter("st_enddate", st_enddate);
      count.setNamedParameter("BankId", bankDetailId);
      count.setNamedParameter("accountnumber", accountnumber);
      count.setNamedParameter("personalpaymethod", personalpaymentmethodId);
      if (rowids != null) {
        count.setNamedParameter("rowid", rowids);
      }
      ls = count.list();
      if (ls.size() > 0) {
        return false;
      }
    } catch (Exception e) {
      log4j.error("error while Account Number Validation", e);
      return false;
    }
    return true;

  }

  /**
   * 
   * @param accountnumber
   * @return
   */
  public String checkIbanvalidation(String accountnumber) {
    String accnum = null;

    try {
      SQLQuery Query = OBDal.getInstance().getSession()
          .createSQLQuery("select ehcm_changebank_account_num(?);");

      Query.setParameter(0, accountnumber);

      if (Query.list().size() > 0) {
        Object row = (Object) Query.list().get(0);
        accnum = (String) row;
      }
    } catch (final Exception e) {
      e.printStackTrace();

    }
    return accnum;
  }

  public List<EfinBankBranch> getBankBranch(String bankId) {
    List<EfinBankBranch> branchList = null;
    try {
      OBQuery<EfinBankBranch> count = OBDal.getInstance().createQuery(EfinBankBranch.class,
          "as e where e.efinBank.id=:BankId ");
      count.setNamedParameter("BankId", bankId);
      branchList = count.list();
    } catch (Exception e) {
      log4j.debug("Exception getBankBranch :" + e);
    }

    return branchList;

  }

  /**
   * Lists all the branches.
   * 
   * @param clientId
   * @return
   */
  public List<EfinBankBranch> getBankBranchOnBank(String clientId) {
    List<EfinBankBranch> branchList = null;
    try {
      OBQuery<EfinBankBranch> count = OBDal.getInstance().createQuery(EfinBankBranch.class,
          "as e where e.client.id=:clientId ");
      count.setNamedParameter("clientId", clientId);
      branchList = count.list();
    } catch (Exception e) {
      log4j.debug("Exception getBankBranch :" + e);
    }

    return branchList;

  }

  public List<EfinBankBranch> getBankBranchOnLoad(String clientId) {

    List<EfinBankBranch> branchList = null;
    try {
      OBQuery<EfinBankBranch> count = OBDal.getInstance().createQuery(EfinBankBranch.class,
          "as e where e.client.id=:clientId ");
      count.setNamedParameter("clientId", clientId);
      branchList = count.list();
    } catch (Exception e) {
      log4j.debug("Exception getBankBranch :" + e);
    }

    return branchList;

  }

  /**
   * 
   * @param employeeId
   * @param perpaymethodId
   * @param clientId
   * @return true/false (if Default PPM already exist then True else False)
   */
  public boolean checkDefaultPersonalPaymentMethodAlreadyExists(String employeeId,
      String perpaymethodId, String clientId) {
    List<EHCMPersonalPaymethd> ls = new ArrayList<EHCMPersonalPaymethd>();
    try {
      OBQuery<EHCMPersonalPaymethd> count = OBDal.getInstance().createQuery(
          EHCMPersonalPaymethd.class,
          " as e where e.ehcmEmpPerinfo.id=:empId and e.client.id=:clientId and default ='Y' and e.id <>:ppmID ");
      count.setNamedParameter("empId", employeeId);
      count.setNamedParameter("clientId", clientId);
      count.setNamedParameter("ppmID", perpaymethodId);

      ls = count.list();
      log4j.debug("count :" + ls.size());
      if (ls.size() > 0) {
        return true;
      }
      // return true;
    } catch (Exception e) {
      log4j.error("error while checkDefaultPersonalPaymentMethodAlreadyExists", e);
      return false;
    }
    return false;
  }

  /**
   * 
   * @param employeeId
   * @param perpaymethodId
   * @param clientId
   * @param payCode
   * @param currency
   * @return true/false (if record already exist with same currency and same payment type.
   */
  public boolean checkPersonalPaymentMethodAlreadyExists(String employeeId, String perpaymethodId,
      String clientId, String payCode, String currency) {
    List<EHCMPersonalPaymethd> ls = new ArrayList<EHCMPersonalPaymethd>();
    try {
      OBQuery<EHCMPersonalPaymethd> count = OBDal.getInstance().createQuery(
          EHCMPersonalPaymethd.class,
          " where ehcmEmpPerinfo.id =:empId and client.id =:clientId and code.id =:payCode"
              + " and currency.id =:currencyID and id <>:ppmID ");
      count.setNamedParameter("empId", employeeId);
      count.setNamedParameter("clientId", clientId);
      count.setNamedParameter("ppmID", perpaymethodId);
      count.setNamedParameter("payCode", payCode);
      count.setNamedParameter("currencyID", currency);

      ls = count.list();

      log4j.debug("count of  checkPersonalPaymentMethodAlreadyExists:" + ls.size());
      if (ls.size() > 0) {
        return true;
      }
      // return true;
    } catch (Exception e) {
      log4j.error("error while checkPersonalPaymentMethodAlreadyExists", e);
      return false;
    }
    return false;
  }

  public boolean checkDefaultPPMBankDefaultAlreadyExists(String perpaymethodId, String Isdefault,
      String clientId, String rowids) {
    List<EHCMPpmBankdetail> ls = new ArrayList<EHCMPpmBankdetail>();
    try {
      if (Isdefault.equals("true")) {
        OBQuery<EHCMPpmBankdetail> count = OBDal.getInstance().createQuery(EHCMPpmBankdetail.class,
            "as e where e.ehcmPersonalPaymethd.id=:PaymentMethodId and e.default='Y' and e.id<>:rowids");
        count.setNamedParameter("PaymentMethodId", perpaymethodId);
        count.setNamedParameter("rowids", rowids);
        // count.setFilterOnActive(false);
        ls = count.list();
        log4j.debug("count :" + ls.size());
        if (ls.size() > 0) {
          return true;
        }
      }
    } catch (Exception e) {
      log4j.error("error while checkPersonalPaymentBank Default AlreadyExists", e);
      return false;
    }
    return false;
  }

  public boolean deletePersonalPayment(String personalPaymethdId) {
    // TODO Auto-generated method stub
    List<EHCMPersonalPaymethd> ls = new ArrayList<EHCMPersonalPaymethd>();
    List<EHCMPpmBankdetail> lsBank = new ArrayList<EHCMPpmBankdetail>();
    try {
      OBQuery<EHCMPersonalPaymethd> count = OBDal.getInstance()
          .createQuery(EHCMPersonalPaymethd.class, " as e where e.id=:paymentId ");
      count.setNamedParameter("paymentId", personalPaymethdId);
      count.setMaxResult(1);
      ls = count.list();
      OBQuery<EHCMPpmBankdetail> countBank = OBDal.getInstance().createQuery(
          EHCMPpmBankdetail.class, " as e where e.ehcmPersonalPaymethd.id=:paymentId ");
      countBank.setNamedParameter("paymentId", personalPaymethdId);
      lsBank = countBank.list();
      if ((ls.size() > 0) && (lsBank.size() == 0)) {
        OBDal.getInstance().remove(ls.get(0));
        OBDal.getInstance().flush();
      } else {
        return false;
      }
      return true;
    } catch (Exception e) {
      log4j.error("error while deletePersonalPayment", e);
      return false;
    }
  }

  public static String convertToGregorian_tochar(String hijriDate) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String gregDate = "";
    try {
      st = OBDal.getInstance().getConnection()
          .prepareStatement("select to_char(eut_convertto_gregorian('" + hijriDate
              + "'),'dd-MM-yyyy') as eut_convertto_gregorian");
      log4j.debug(st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        gregDate = rs.getString("eut_convertto_gregorian");
      }
    }

    catch (final Exception e) {
      log4j.error("Exception in convertToGregorian_tochar() Method : ", e);
      return "0";
    }
    return gregDate;
  }
}
