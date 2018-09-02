package sa.elm.ob.hcm.ad_forms.holidaycalendar.dao;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.SQLQuery;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.EHCMHolidayCalendar;
import sa.elm.ob.hcm.ad_forms.holidaycalendar.vo.HolidayCalendarVO;
import sa.elm.ob.utility.EUT_HijriDates;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * This process class used for Holiday CalendarDAO Implementation
 * 
 * @author divya -08-03-2018
 *
 */

public class HolidayCalendarDAOImpl implements HolidayCalendarDAO {

	private Connection conn = null;
	private static Logger log4j = Logger.getLogger(HolidayCalendarDAOImpl.class);

	public static String HOLIDAY_REFERENCE_ID = "FAE9DB51DFDB4A30AA3087137ED7077A";
	public static String HOLIDAYTYPE_WEEKEND1 = "WE1";
	public static String HOLIDAYTYPE_WEEKEND2 = "WE2";
	public static String HOLIDAYTYPE_NATIONALHOLIDAY = "NH";
	public static String HOLIDAYTYPE_ADHA = "AD";
	public static String HOLIDAYTYPE_FETER = "FE";

	public HolidayCalendarDAOImpl(Connection con) {
		this.conn = con;
	}

	DateFormat YearFormat = Utility.YearFormat;
	DateFormat dateFormat = Utility.dateFormat;

	@Override
	@SuppressWarnings({ "rawtypes" })
	public JSONObject getDatesBasedOnNo(String clientId, int weekDaysNo, String year) {
		String sql = null;
		String hijiriDate = null;
		JSONObject result = new JSONObject(), json = null;
		JSONArray jsonArr = new JSONArray();
		int originalweekDaysNo = 0;
		try {
			originalweekDaysNo = weekDaysNo;
			if(weekDaysNo > 6) {
				weekDaysNo = originalweekDaysNo % 7;
			}
			sql = " select max(hijri_date),gregorian_date,(SELECT EXTRACT(DOW FROM  gregorian_date)) from eut_hijri_dates where   hijri_date like :hijri_date and (SELECT EXTRACT(DOW FROM  gregorian_date))=:weekend "
					+ " and gregorian_date not in ( select holidaydate from ehcm_holiday_calendar where year = :year and ad_client_id=:clientId ) group by gregorian_date ";
			SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(sql);
			query.setParameter("hijri_date", "%" + year + "%");
			query.setParameter("weekend", weekDaysNo);
			query.setParameter("year", Long.valueOf(year.toString()));
			query.setParameter("clientId", clientId);
			log4j.debug("where :" + query.toString());
			List datelist = query.list();
			if(datelist != null && datelist.size() > 0) {
				for (Object o : datelist) {
					Object[] row = (Object[]) o;
					hijiriDate = row[0].toString();
					json = new JSONObject();
					json.put("year", hijiriDate.substring(0, 4));
					json.put("month", hijiriDate.substring(4, 6));
					json.put("day", hijiriDate.substring(6, 8));
					if(originalweekDaysNo > 6) {
						json.put("dateclass", HOLIDAYTYPE_WEEKEND2 + originalweekDaysNo);
					}
					else {
						json.put("dateclass", HOLIDAYTYPE_WEEKEND1 + originalweekDaysNo);

					}
					json.put("holidayDate", row[1].toString());
					jsonArr.put(json);
				}
				result.put("weekendList", jsonArr);
			}
		}
		catch (final Exception e) {
			log4j.error("Exception in getWeekend() :", e);
			return result;
		}
		return result;
	}

	@Override
	public List<HolidayCalendarVO> getHolidayList() {
		List<org.openbravo.model.ad.domain.List> refList = new ArrayList<org.openbravo.model.ad.domain.List>();
		List<HolidayCalendarVO> holidayCalVOList = new ArrayList<HolidayCalendarVO>();
		HolidayCalendarVO holidayCalVO = null;
		try {
			OBQuery<org.openbravo.model.ad.domain.List> reflistQry = OBDal.getInstance().createQuery(org.openbravo.model.ad.domain.List.class, " as e where e.reference.id=:refid and e.searchKey not in ('WE1','WE2')  order by e.sequenceNumber");
			reflistQry.setNamedParameter("refid", HOLIDAY_REFERENCE_ID);
			refList = reflistQry.list();
			if(refList.size() > 0) {
				for (org.openbravo.model.ad.domain.List list : refList) {
					holidayCalVO = new HolidayCalendarVO();
					holidayCalVO.setHolidaylistValue(list.getSearchKey());
					holidayCalVO.setHolidaylistName(list.getName());
					holidayCalVOList.add(holidayCalVO);
				}
				return holidayCalVOList;
			}
		}
		catch (final Exception e) {
			log4j.error("Exception in getHolidayList", e);
			return holidayCalVOList;
		}
		finally {
		}
		return holidayCalVOList;
	}

	public boolean addHoliday(String clientId, String orgId, String userId, String year, String holidayDate) {
		Boolean success = false;
		EHCMHolidayCalendar holidayCal = null;
		List<EHCMHolidayCalendar> holCalList = new ArrayList<EHCMHolidayCalendar>();
		try {
			OBContext.setAdminMode();

			//delete old one
			OBQuery<EHCMHolidayCalendar> holcalQry = OBDal.getInstance().createQuery(EHCMHolidayCalendar.class, " as e where e.fiscalYear=:year and e.client.id=:clientId ");
			holcalQry.setNamedParameter("year", Long.valueOf(year.toString()));
			holcalQry.setNamedParameter("clientId", clientId);
			holCalList = holcalQry.list();
			log4j.debug("holCalList1:" + holCalList.size());
			if(holCalList.size() > 0) {
				for (EHCMHolidayCalendar holcal : holCalList) {
					OBDal.getInstance().remove(holcal);
					OBDal.getInstance().flush();
					success = true;
				}
			}
			JSONObject jsob = new JSONObject(holidayDate), json = null;
			JSONArray jsonArray = jsob.getJSONArray("list");
			log4j.debug("jsonArray:" + jsonArray.length());
			if(jsonArray.length() > 0) {
				for (int i = 0; i < jsonArray.length(); i++) {
					if(!jsonArray.get(i).equals(null)) {
						json = jsonArray.getJSONObject(i);
						if(json.has("holidayDate")) {

							holidayCal = OBProvider.getInstance().get(EHCMHolidayCalendar.class);
							holidayCal.setClient(OBDal.getInstance().get(Client.class, clientId));
							holidayCal.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
							holidayCal.setCreatedBy(OBDal.getInstance().get(User.class, userId));
							holidayCal.setCreationDate(new java.util.Date());
							holidayCal.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
							holidayCal.setUpdated(new java.util.Date());
							holidayCal.setFiscalYear(Long.valueOf(year.toString()));
							log4j.debug("holidayDate:" + json.getString("holidayDate"));
							holidayCal.setHolidaydate(dateFormat.parse(UtilityDAO.convertToGregorian_tochar(json.getString("holidayDate"))));
							if(json.getString("dateclass").contains(HOLIDAYTYPE_WEEKEND1))
								holidayCal.setHolidayType(HOLIDAYTYPE_WEEKEND1);
							else if(json.getString("dateclass").contains(HOLIDAYTYPE_WEEKEND2))
								holidayCal.setHolidayType(HOLIDAYTYPE_WEEKEND2);
							else
								holidayCal.setHolidayType(json.getString("dateclass"));
							OBDal.getInstance().save(holidayCal);
							//}
						}
					}
				}
			}
			// if just deselect/removing previous date then send true
		}
		catch (final Exception e) {
			log4j.error("Exception in addHoliday() :", e);
			try {
				conn.rollback();
			}
			catch (final Exception e1) {
				log4j.error("Exception in addHoliday() :", e1);
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	public JSONObject setResponseHoliday(VariablesSecureApp vars, String year) {
		JSONArray jsonArray = new JSONArray();
		JSONArray weekEndArray = new JSONArray();
		JSONObject result = new JSONObject(), json = null,weekEndJson=null;
		String hijiriDate = null;
		String sql = null;
		
		try {

			sql = " select cal.holidaydate,cal.holiday_type, case when  cal.holiday_type ='WE1' then cast((SELECT EXTRACT(DOW FROM  cal.holidaydate)) as int)  when cal.holiday_type ='WE2' then cast((SELECT EXTRACT(DOW FROM  cal.holidaydate)+7) as int)  else null end  from ehcm_holiday_calendar cal  where cal.year=:year and cal.ad_client_id=:clientId  order by cal.holiday_type desc ";
			SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(sql);
			query.setParameter("year", Long.valueOf(year.toString()));
			query.setParameter("clientId", vars.getClient());
			log4j.debug("where :" + query.toString());
			List datelist = query.list();
			if(datelist != null && datelist.size() > 0) {
				for (Object o : datelist) {
					Object[] row = (Object[]) o;
					json = new JSONObject();
					hijiriDate = UtilityDAO.convertToHijriDate(YearFormat.format(YearFormat.parse(row[0].toString())));
					json.put("year", hijiriDate.substring(6, 10));
					json.put("month", hijiriDate.substring(3, 5));
					json.put("day", hijiriDate.substring(0, 2));
					if(row[2] != null)
						json.put("dateclass", row[1].toString() + row[2]);
					else
						json.put("dateclass", row[1].toString());
					json.put("holidayDate", hijiriDate.substring(0, 2)+"-"+hijiriDate.substring(3, 5)+"-"+hijiriDate.substring(6, 10));
					jsonArray.put(json);
					//setting weekend days 
					if(row[2]!=null) {
						if (weekEndJson!=null && (row[2].toString().equals(weekEndJson.getString("weekdays")))) {
							continue;
						}
        					else {
        					weekEndJson = new JSONObject();
        					weekEndJson.put("weekdays",  row[2]);
    						weekEndArray.put(weekEndJson);
        					}
					}
				}
				result.put("weekendDays", weekEndArray);
				result.put("weekendList", jsonArray);
			}
			log4j.debug("result:" + result);
		}
		catch (final Exception e) {
			log4j.error("Exception in HolidayCalendarAjax - setResponseHoliday : ", e);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	public JSONObject getMinAndMaxDateInYear(String year) {
		String sql = null;
		JSONObject result = new JSONObject();
		String minhijiriDate = null;
		String maxhijiriDate = null;
		try {
			sql = " select min(hijri_date) ,max(hijri_date)from eut_hijri_dates where hijri_date ilike ? ";
			SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(sql);
			query.setParameter(0, "%" + year + "%");
			log4j.debug("where :" + query.toString());
			List datelist = query.list();
			if(datelist != null && datelist.size() > 0) {
				Object[] row = (Object[]) datelist.get(0);
				minhijiriDate = row[0].toString();
				maxhijiriDate = row[1].toString();
				result.put("mindate", minhijiriDate);
				result.put("maxdate", maxhijiriDate);
			}
		}
		catch (final Exception e) {
			log4j.error("Exception in getMinAndMaxDateInYear() :", e);
			return result;
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void copyPreviousYearHolidayCal(String year, String clientId, String orgId, String userId) throws Exception {
		EHCMHolidayCalendar holidayCal = null;
		List<EUT_HijriDates> hijriDateList = new ArrayList<EUT_HijriDates>();
		String sql = null, holidaysql = null;
		String weekendsql = null;
		OBQuery<EUT_HijriDates> hijriDateQry = null;
		SQLQuery query = null;
		try {
			OBContext.setAdminMode();

			/*
			holidaysql = " as a where a.hijriDate in  (select  (cast(substring(hd. hijriDate ,1,4)as int )+1)||substring(hd. hijriDate ,5,4) from EHCM_Holiday_Calendar cal , EUT_HijiriDates hd "
					+ " where cal. fiscalYear=:year   and cal.holidayType =:holidayType and cal.client.id=:clientId and hd.gregorianDate= cal.holidaydate  order by cal.holidaydate ) group by a.gregorianDate";
			//feter holiday insert 
			hijriDateQry = OBDal.getInstance().createQuery(EUT_HijriDates.class, holidaysql);
			hijriDateQry.setNamedParameter("year", Long.valueOf(year.toString()));
			hijriDateQry.setNamedParameter("holidayType", HOLIDAYTYPE_FETER);
			hijriDateQry.setNamedParameter("clientId", clientId);*/
			
			
			//feter holiday
			holidaysql = "select max(a.hijri_date) , a.gregorian_date  from eut_hijri_dates as a where a.hijri_date in  (select  (cast(substring(hd.hijri_date ,1,4)as int )+1)||substring(hd.hijri_date ,5,4) from ehcm_holiday_calendar cal , eut_hijri_dates hd "
					+ " where cal.year=:year   and cal.holiday_type =:holidayType and cal.ad_client_id=:clientId and hd.gregorian_date= cal.holidaydate  order by cal.holidaydate ) group by a.gregorian_date  ";
			query = OBDal.getInstance().getSession().createSQLQuery(holidaysql);
			query.setParameter("year", Long.valueOf(year.toString())-1);
			query.setParameter("holidayType", HOLIDAYTYPE_FETER);
			query.setParameter("clientId", clientId);
			log4j.debug("query :" + query.toString());
			List datelist = query.list();
			if(datelist != null && datelist.size() > 0) {
				for (Object o : datelist) {
					Object[] row = (Object[]) o;
						holidayCal = OBProvider.getInstance().get(EHCMHolidayCalendar.class);
						holidayCal.setClient(OBDal.getInstance().get(Client.class, clientId));
						holidayCal.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
						holidayCal.setCreatedBy(OBDal.getInstance().get(User.class, userId));
						holidayCal.setCreationDate(new java.util.Date());
						holidayCal.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
						holidayCal.setUpdated(new java.util.Date());
						holidayCal.setFiscalYear(Long.valueOf(year.toString()));
						holidayCal.setHolidaydate((Date) row[1]);
						holidayCal.setHolidayType(HOLIDAYTYPE_FETER);
						holidayCal.setCopy(true);
						OBDal.getInstance().save(holidayCal);
					}
				}
			
			//National holiday insert 
			query = OBDal.getInstance().getSession().createSQLQuery(holidaysql);
			query.setParameter("year", Long.valueOf(year.toString())-1);
			query.setParameter("holidayType", HOLIDAYTYPE_NATIONALHOLIDAY);
			query.setParameter("clientId", clientId);
			List nationalDayList = query.list();
			if(nationalDayList != null && nationalDayList.size() > 0) {
				for (Object o : nationalDayList) {
					Object[] row = (Object[]) o;
					holidayCal = OBProvider.getInstance().get(EHCMHolidayCalendar.class);
					holidayCal.setClient(OBDal.getInstance().get(Client.class, clientId));
					holidayCal.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
					holidayCal.setCreatedBy(OBDal.getInstance().get(User.class, userId));
					holidayCal.setCreationDate(new java.util.Date());
					holidayCal.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
					holidayCal.setUpdated(new java.util.Date());
					holidayCal.setFiscalYear(Long.valueOf(year.toString()));
					holidayCal.setHolidaydate((Date) row[1]);
					holidayCal.setHolidayType(HOLIDAYTYPE_NATIONALHOLIDAY);
					holidayCal.setCopy(true);
					OBDal.getInstance().save(holidayCal);
				}
			}
			//Adha holiday insert 
			query = OBDal.getInstance().getSession().createSQLQuery(holidaysql);
			query.setParameter("year", Long.valueOf(year.toString())-1);
			query.setParameter("holidayType", HOLIDAYTYPE_ADHA);
			query.setParameter("clientId", clientId);
			List adhaList = query.list();
			if(adhaList != null && adhaList.size() > 0) {
				for (Object o : adhaList) {
					Object[] row = (Object[]) o;
					holidayCal = OBProvider.getInstance().get(EHCMHolidayCalendar.class);
					holidayCal.setClient(OBDal.getInstance().get(Client.class, clientId));
					holidayCal.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
					holidayCal.setCreatedBy(OBDal.getInstance().get(User.class, userId));
					holidayCal.setCreationDate(new java.util.Date());
					holidayCal.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
					holidayCal.setUpdated(new java.util.Date());
					holidayCal.setFiscalYear(Long.valueOf(year.toString()));
					holidayCal.setHolidaydate((Date) row[1]);
					holidayCal.setHolidayType(HOLIDAYTYPE_ADHA);
					holidayCal.setCopy(true);
					OBDal.getInstance().save(holidayCal);
				}
			}
			sql = " select distinct (SELECT EXTRACT(DOW FROM  holidaydate)),holiday_type  from ehcm_holiday_calendar where year=:year and ad_client_id=:clientId and holiday_type in ( select distinct holiday_type from ehcm_holiday_calendar where year=:year "
					+ " and holiday_type in ('WE1','WE2')) ";
			query = OBDal.getInstance().getSession().createSQLQuery(sql);
			query.setParameter("year", Long.valueOf(year.toString())-1	);
			query.setParameter("clientId", clientId);
			query.setParameter("year", Long.valueOf(year.toString())-1);
			List weekendlist = query.list();
			if(weekendlist != null && weekendlist.size() > 0) {
				for (Object o : weekendlist) {
					Object[] row = (Object[]) o;
					weekendsql = " select max(hijri_date),gregorian_date,cast((SELECT EXTRACT(DOW FROM  gregorian_date)) as int )" + 
							" from eut_hijri_dates where   hijri_date like :hijri_date and (SELECT EXTRACT(DOW FROM  gregorian_date))=:weekend " + 
							" and gregorian_date not in ( select  a.gregorian_date  from eut_hijri_dates as a where a.hijri_date in " + 
							" (select  (cast(substring(hd.hijri_date ,1,4)as int )+1)||substring(hd.hijri_date ,5,4) from ehcm_holiday_calendar cal , " + 
							" eut_hijri_dates hd " + 
							"where cal.year=:year   and cal.holiday_type in ('FE','AD','NH') and cal.ad_client_id=:clientId " + 
							" and hd.gregorian_date= cal.holidaydate  order by cal.holidaydate ) group by a.gregorian_date ) group by gregorian_date ";
					query = OBDal.getInstance().getSession().createSQLQuery(weekendsql); // (cast(substring(hd. hijri_date ,1,4)as int )+1)||substring(hd. hijri_date ,5,4)
					query.setParameter("hijri_date", "%" + (Long.valueOf(year.toString())) + "%");
					query.setParameter("weekend", (row[0]));
					query.setParameter("year", Long.valueOf(year.toString())-1);
					query.setParameter("clientId", clientId);
					log4j.debug("where :"+query.getQueryString());
					List hijiriDateList = query.list();
					if(hijiriDateList != null && hijiriDateList.size() > 0) {
						for (Object hirjidate : hijiriDateList) {
							Object[] hijiri = (Object[]) hirjidate;
							holidayCal = OBProvider.getInstance().get(EHCMHolidayCalendar.class);
							holidayCal.setClient(OBDal.getInstance().get(Client.class, clientId));
							holidayCal.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
							holidayCal.setCreatedBy(OBDal.getInstance().get(User.class, userId));
							holidayCal.setCreationDate(new java.util.Date());
							holidayCal.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
							holidayCal.setUpdated(new java.util.Date());
							holidayCal.setFiscalYear(Long.valueOf(year.toString()));
							holidayCal.setHolidaydate((Date) hijiri[1]);
							holidayCal.setHolidayType(row[1].toString());
							holidayCal.setCopy(true);
							OBDal.getInstance().save(holidayCal);
						}
					}
				}
			}
			OBDal.getInstance().flush();
		}
		catch (final Exception e) {
			log4j.error("Exception in copyPreviousYearHolidayCal() :", e);
			try {
				conn.rollback();
			}
			catch (final Exception e1) {
				log4j.error("Exception in copyPreviousYearHolidayCal() :", e1);
			}
		}

	}
	public boolean checkAlreadyCopied(String year,String clientId) {
		List<EHCMHolidayCalendar> holCalList = new ArrayList<EHCMHolidayCalendar>();
		try {
			OBQuery<EHCMHolidayCalendar> holCalQry = OBDal.getInstance().createQuery(EHCMHolidayCalendar.class, " as e where e.fiscalYear=:year  and iscopy='Y' and e.client.id=:clientId ");
			holCalQry.setNamedParameter("year", year);
			holCalQry.setNamedParameter("clientId", clientId);
			holCalQry.setMaxResult(1);
			holCalList= holCalQry.list();
			
			if(holCalList.size()>0) {
				return true;
			}
			else {
				return false;
			}
		}
		catch (final Exception e) {
			log4j.error("Exception in getMinAndMaxDateInYear() :", e);
			return false;
		}
	}
	
	@SuppressWarnings({ "resource", "rawtypes" })
	public  JSONObject getYearList( String searchTerm, int pagelimit, int page) {
		JSONObject jsob = null;
		JSONArray jsonArray = new JSONArray();
		SQLQuery query = null;
		String whereclause="";
		int startYear=1430;
	//	pagelimit=10;
		try {
			jsob = new JSONObject();
			
			if(searchTerm != null && !searchTerm.equals(""))
				whereclause=" and substring(e. hijri_date,1,4) ilike :year ";
			
			query = OBDal.getInstance().getSession().createSQLQuery(" select   distinct substring(e. hijri_date,1,4) from eut_hijri_dates e  where cast(substring(e. hijri_date,1,4) as int) >=:startYear  " + whereclause +" and substring (hijri_date,1,4) not in ('1500','1600') "); 
			query.setParameter("startYear", startYear);
			
			if(searchTerm != null && !searchTerm.equals(""))
				query.setParameter("year", "%"+searchTerm+"%");
			
			log4j.debug("where :"+query.getQueryString());
			List totalYearList = query.list();
			jsob.put("totalRecords", totalYearList.size());

			
			
			
			query = OBDal.getInstance().getSession().createSQLQuery(" select   distinct substring(e. hijri_date,1,4) from eut_hijri_dates e where cast(substring(e. hijri_date,1,4) as int) >=:startYear   " + whereclause +" and substring (hijri_date,1,4) not in ('1500','1600') order by substring(e. hijri_date,1,4) asc " ); 
			query.setParameter("startYear", startYear);
			if(searchTerm != null && !searchTerm.equals(""))
				query.setParameter("year", "%"+searchTerm+"%");
			
			
			query.setFirstResult((page - 1) * pagelimit); // equivalent to OFFSET
			query.setMaxResults(pagelimit) ;
			log4j.debug("where :"+query.getQueryString());
			List hijiriYearList = query.list();
			
			if(hijiriYearList != null && hijiriYearList.size() > 0) {
				for (Object hirjidate : hijiriYearList) {
					JSONObject jsonData = new JSONObject();
					jsonData.put("id",  hirjidate.toString());
					jsonData.put("recordIdentifier", hirjidate.toString());
					jsonArray.put(jsonData);
				}
			}
			//jsob.put("totalRecords", jsonArray.length());
			if(jsonArray.length() > 0)
				jsob.put("data", jsonArray);
			else
				jsob.put("data", "");
			
		}
		catch (final Exception e) {
			log4j.error("Exception in getYearList :", e);
			return jsob;
		}
		finally {
		}
		return jsob;
	}
}