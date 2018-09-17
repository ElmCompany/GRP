package sa.elm.ob.hcm.ad_process;

/**
 * 
 * @author Divya
 *
 */
public class DecisionTypeConstants {

  // Decision Type Option
  public static final String DECISION_TYPE_CREATE = "CR";
  public static final String DECISION_TYPE_UPDATE = "UP";
  public static final String DECISION_TYPE_EXTEND = "EX";
  public static final String DECISION_TYPE_CUTOFF = "CO";
  public static final String DECISION_TYPE_CANCEL = "CA";
  public static final String DECISION_TYPE_HOLD = "HO";
  public static final String DECISION_TYPE_BUSINESSMISSION_PAYMENT = "BP";
  public static final String DECISION_TYPE_SCHOLARSHIP_PAYMENT = "SP";
  // public static String DECISION_TYPE_BUSINESSPAYMENT_CASE = "BPC";
  public static final String DECISION_TYPE_OVERTIMEPAYMENT = "OP";
  public static final String DECISION_TYPE_TICKET_PAYMENT = "TP";

  // employment Info Change Reason
  public static final String CHANGEREASON_SECONDMENT = "SEC";
  public static final String CHANGEREASON_EXTEND_SECONDMENT = "EXSEC";
  public static final String CHANGEREASON_CUTOFF_SECONDMENT = "COSEC";
  public static final String CHANGEREASON_JWR_SECONDMENT = "JWRSEC";
  public static final String CHANGEREASON_SECONDMENT_DELAY = "SECDLY";

  // employment status
  public static final String EMPLOYMENTSTATUS_SECONDMENT = "SE";
  public static final String EMPLOYMENTSTATUS_EXTENDSERVICE = "EOS";
  public static final String EMPLOYMENTSTATUS_EXTRASTEP = "ES";
  public static final String EMPLOYMENTSTATUS_ACTIVE = "AC";
  public static final String EMPLOYMENTSTATUS_SUSPENDED = "SD";
  // status
  public static final String Status_active = "ACT";
  public static final String Status_Inactive = "INACT";

  // one day calculation in milisecond
  public static final int ONE_DAY_IN_MILISEC = 1 * 24 * 3600 * 1000;

}
