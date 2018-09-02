package sa.elm.ob.hcm.ad_forms.holidaycalendar.ajax;

import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

import sa.elm.ob.finance.ad_process.RDVProcess.DAO.PenaltyActionDAO;
import sa.elm.ob.hcm.ad_forms.holidaycalendar.dao.HolidayCalendarDAOImpl;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;


public class HolidayCalendarAjax extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;
	DateFormat YearFormat = Utility.YearFormat;
	DateFormat dateFormat = Utility.dateFormat;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);
		Connection con = null;
		HolidayCalendarDAOImpl holidayCalendarDAOImpl = null;
		try {
			OBContext.setAdminMode();
			con = getConnection();
			holidayCalendarDAOImpl = new HolidayCalendarDAOImpl(con);
			String action = (request.getParameter("action") == null ? "" : request.getParameter("action"));
			if(action.equals("SaveHolidays")) {
				JSONObject result = new JSONObject();
				try {
					result.put("result", "0");
					boolean isSaved = holidayCalendarDAOImpl.addHoliday(vars.getClient(), vars.getOrg(), vars.getUser(), (String) request.getParameter("inpYear"), request.getParameter("inpHolidayDate"));

					if(isSaved) {
						result.put("result", "1");
					}
				}
				catch (final Exception e) {
					log4j.error("Exception in HolidayCalendarAjax - SaveHolidays : ", e);
				}
				finally {
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.setHeader("Cache-Control", "no-cache");
					response.getWriter().write(result.toString());
				}
			}
			else if(action.equals("GetHolidays")) {
				JSONObject result = new JSONObject();
				try {
					result.put("result", "0");
					result= holidayCalendarDAOImpl.setResponseHoliday( vars, request.getParameter("inpYear"));
					result.put("result", "1");
				}
				catch (final Exception e) {
					log4j.error("Exception in HolidayCalendarAjax - GetHolidays : ", e);
				}
				finally {
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.setHeader("Cache-Control", "no-cache");
					response.getWriter().write(result.toString());
				}
			}
			else if(action.equals("GetWeekenddays")) {
				JSONObject result = new JSONObject();
				try {
					int weekEndNo = Integer.parseInt(request.getParameter("inpweekend").toString());
					
					result= holidayCalendarDAOImpl.getDatesBasedOnNo(vars.getClient(), weekEndNo,request.getParameter("inpYear"));
					log4j.debug("result:"+result);
				}
				catch (final Exception e) {
					log4j.error("Exception in HolidayCalendarAjax - GetHolidays : ", e);
				}
				finally {
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.setHeader("Cache-Control", "no-cache");
					response.getWriter().write(result.toString());
				}
			}
			else if(action.equals("getGregorianDate")) {
				String  gregorianDate = null;
				JSONObject result = new JSONObject();
				try {
					
					gregorianDate= UtilityDAO.convertToGregorian_tochar(request.getParameter("hijiriDate"));
					result.put("gregorianDate", YearFormat.format(dateFormat.parse(gregorianDate)));
					
				}
				catch (final Exception e) {
					log4j.error("Exception in HolidayCalendarAjax - GetHolidays : ", e);
				}
				finally {
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.setHeader("Cache-Control", "no-cache");
					response.getWriter().write(result.toString());
				}
			}
			else if(action.equals("GetMinAndMaxDate")) {
				JSONObject result = new JSONObject();
				try {
					result.put("result", "0");
					result= holidayCalendarDAOImpl.getMinAndMaxDateInYear(request.getParameter("inpYear"));
					result.put("result", "1");
				}
				catch (final Exception e) {
					log4j.error("Exception in HolidayCalendarAjax - GetHolidays : ", e);
				}
				finally {
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.setHeader("Cache-Control", "no-cache");
					response.getWriter().write(result.toString());
				}
			}
			else if(action.equals("copyHolidays")) {
				JSONObject result = new JSONObject();
				try {
					
					holidayCalendarDAOImpl.copyPreviousYearHolidayCal(request.getParameter("inpYear"), vars.getClient(),vars.getOrg(),vars.getUser());
					
					result.put("result", "0");
					result= holidayCalendarDAOImpl.setResponseHoliday( vars, request.getParameter("inpYear"));
					result.put("result", "1");
					
				}
				catch (final Exception e) {
					log4j.error("Exception in HolidayCalendarAjax - GetHolidays : ", e);
				}
				finally {
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.setHeader("Cache-Control", "no-cache");
					response.getWriter().write(result.toString());
				}
			}
			else if(action.equals("getYearList")) {
				JSONObject jsob = new JSONObject();
				jsob = holidayCalendarDAOImpl.getYearList( request.getParameter("searchTerm"), Integer.parseInt(request.getParameter("pageLimit")), Integer.parseInt(request.getParameter("page")));
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.setHeader("Cache-Control", "no-cache");
				response.getWriter().write(jsob.toString());
			}
		}
		catch (final Exception e) {
			log4j.error("Exception in HolidayCalendarAjax : ", e);
		}
	}



	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doPost(request, response);
	}

	public String getServletInfo() {
		return "HolidayCalendarAjax Servlet.";
	}
}
