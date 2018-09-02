package sa.elm.ob.hcm.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Holds constant value for Payroll
 *
 */
public class PayrollConstants {
  public static final String ELEMENT_PAYSCALE_CODE = "PAYSALGS";
  public static final String ELEMENT_GRADERATE_CODE = "GRADERATE";
  public static final String ELEMENT_EMPLOYMENT_GRADE_CODE = "EMP_GRADE";
  public static final String ELEMENT_EMPLOYEE_COUNTRY_CODE = "EMP_COUNTRY";
  public static final String ELEMENT_BUSINESS_MISSION_TYPE_CODE = "BM_TYPE";
  public static final String ELEMENT_BUSINESS_MISSION_COUNTRY_CATEGORY_CODE = "BM_COUNTRYCAT";
  public static final String ELEMENT_BUSINESS_MISSION_HOUSING_CODE = "BM_HOUSING";
  public static final String ELEMENT_BUSINESS_MISSION_FOOD_CODE = "BM_FOOD";
  public static final String ELEMENT_BUSINESS_MISSION_DAYS = "BM_DAYS";
  public static final String ELEMENT_BUSINESS_MISSION_DAYS_BEFORE = "BM_DAYS_BEFORE";
  public static final String ELEMENT_BUSINESS_MISSION_DAYS_BEFORE_RATE = "BM_DAYS_BEFORE_RATE";
  public static final String ELEMENT_BUSINESS_MISSION_DAYS_AFTER = "BM_DAYS_AFTER";
  public static final String ELEMENT_BUSINESS_MISSION_DAYS_AFTER_RATE = "BM_DAYS_AFTER_RATE";
  public static final String ELEMENT_BUSINESS_MISSION_PAYMENT_AMT = "BM_PAYMENT_AMT";
  public static final String ELEMENT_BUSINESS_MISSION_ADVANCE_AMT = "BM_ADVANCE_AMT";
  public static final String ALLOWANCE_FIXEDAMOUNT = "FA";
  public static final String ALLOWANCE_PERCENTAGE = "P";
  public static final String ALLOWANCE_PERCENT_BASIC = "BS";
  public static final String ALLOWANCE_PERCENT_FIRSTSTEPGRADE = "GS1";
  public static final BigDecimal PERCENTAGE_MAXIMUM = new BigDecimal("100");
  public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  public static final int HOURS = 24;
  public static final int MINUTES = 60;
  public static final int SECONDS = 60;
  public static final int MILLISECONDS = 1000;
  public static final String ALLOWANCE_ELEMENTCTGRY_TYPE = "01";
  public static final String BASIC_ELEMENTCTGRY_TYPE = "02";
  public static final String DEDUCTION_ELEMENTCTGRY_TYPE = "03";
  public static final String PENSION_ELEMENTCTGRY_TYPE = "04";
  public static final String OTHER_ELEMENTCTGRY_TYPE = "06";
  public static final String OVERTIME_WORKINGDAYS = "OTWD";
  public static final String OVERTIME_WORKINGHOURS = "OTWH";
  public static final String OVERTIME_WEEKEND1DAYS = "OTW1D";
  public static final String OVERTIME_WEEKEND1HOURS = "OTW1H";
  public static final String OVERTIME_WEEKEND2DAYS = "OTW2D";
  public static final String OVERTIME_WEEKEND2HOURS = "OTW2H";
  public static final String OVERTIME_FETERDAYS = "OTFD";
  public static final String OVERTIME_FETERHOURS = "OTFH";
  public static final String OVERTIME_HAJJDAYS = "OTHD";
  public static final String OVERTIME_HAJJHOURS = "OTHH";
  public static final String OVERTIME_NATIONALDAYS = "OTND";
  public static final String OVERTIME_NATIONALHOURS = "OTNH";
  public static final String GLOBAL_WORKINGHOURS_MONTH = "G_WH_MONTH";
  public static final String GLOBAL_SCHOLARSHIP_REWARD_DAYS = "G_Scholarship_Reward_Days";
  public static final String SCHOLARSHIP_COUNTRY_CTGRY = "ST_Country_Category";
  public static final String SCHOLARSHIP_DAYS = "ST_DAYS";
  public static final String EMP_MARRIED = "EMP_MARRIED";
  public static final String EMP_NOOFCHILDS = "EMP_NOOFCHILDS";

}
