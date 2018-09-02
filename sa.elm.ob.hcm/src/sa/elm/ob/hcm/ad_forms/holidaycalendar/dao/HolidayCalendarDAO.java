package sa.elm.ob.hcm.ad_forms.holidaycalendar.dao;

import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;

import sa.elm.ob.hcm.ad_forms.holidaycalendar.vo.HolidayCalendarVO;

/**
 * Interface for all Holiday Calendar related DB Operations
 * 
 * @author divya -08-03-2018
 *
 */
public interface HolidayCalendarDAO {

	/**
	 * get list of dates based on weekdays no
	 * @param clientId
	 * @param weekDaysNo
	 * @param year
	 * @return
	 * @throws Exception
	 */
	JSONObject getDatesBasedOnNo(String clientId,int weekDaysNo,String year)  throws Exception;
	
	/**
	 * get Holiday List based on HolidayList Reference
	 * @return
	 * @throws Exception
	 */
	List<HolidayCalendarVO> getHolidayList() throws Exception;
	
	/**
	 * insert date into db
	 * @param clientId
	 * @param orgId
	 * @param userId
	 * @param year
	 * @param holidayDate
	 * @return
	 * @throws Exception
	 */
	boolean addHoliday(String clientId, String orgId, String userId, String year, String holidayDate) throws Exception;
	
	/**
	 * get holiday 
	 * @param vars
	 * @param year
	 * @return
	 * @throws Exception
	 */
	JSONObject setResponseHoliday(VariablesSecureApp vars, String year) throws Exception;
	
	/**
	 * get Min and max date for year
	 * @param year
	 * @return
	 */
	JSONObject getMinAndMaxDateInYear(String year);
	
	/**
	 * copy holiday from previous year
	 * @param year
	 * @param clientId
	 * @param orgId
	 * @param userId
	 * @throws Exception
	 */
	void copyPreviousYearHolidayCal(String year, String clientId,String orgId,String userId) throws Exception;
	/**
	 * check already copy the holiday or not
	 * @param year
	 * @param clientId
	 * @return
	 * @throws Exception
	 */
	boolean checkAlreadyCopied(String year,String clientId) throws Exception;
	
}