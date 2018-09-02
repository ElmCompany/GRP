package sa.elm.ob.hcm.ad_forms.holidaycalendar.header;

import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

import sa.elm.ob.hcm.ad_forms.holidaycalendar.dao.HolidayCalendarDAOImpl;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * this handler file handle the servlet request and response for Holiday Calendar and redirect to JSP Pages
 * @author divya -08-03-2018
 *
 */
public class HolidayCalendar extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;


	public HolidayCalendar() {
		super();
	}


	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Connection con = null;
		RequestDispatcher dispatch = null;
		String action = "", reqType = "";
		DateFormat dateFormat = Utility.YearFormat;

		try {
			OBContext.setAdminMode();
			con= getConnection();
			HolidayCalendarDAOImpl holidayCalendarDAOImpl = new HolidayCalendarDAOImpl(con);
			action = Utility.nullToEmpty(request.getParameter("act"));
			reqType = (request.getParameter("reqType") == null ? "" : request.getParameter("reqType"));
			if("".equals(action)) {
				VariablesSecureApp vars = new VariablesSecureApp(request);

				log4j.debug("curdate:"+UtilityDAO.convertTohijriDate(dateFormat.format(new Date())));
				String currentDate=UtilityDAO.convertTohijriDate(dateFormat.format(new Date()));
				
				request.setAttribute("CurrentDate",currentDate);
				request.setAttribute("CurrentYear", currentDate.split("-")[2]);
				log4j.debug("CurrentYear:"+currentDate.split("-")[2]);
				request.setAttribute("StartYear", currentDate.split("-")[2]);
				request.setAttribute("HolidayList",holidayCalendarDAOImpl.getHolidayList() );
				JSONObject minAndMaxDate= holidayCalendarDAOImpl.getMinAndMaxDateInYear(currentDate.split("-")[2]);
				request.setAttribute("minDate",minAndMaxDate.getString("mindate"));
				request.setAttribute("maxDate",minAndMaxDate.getString("maxdate"));

				dispatch = request.getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/holidaycalendar/HolidayCalendar.jsp");
			}
		}
		catch (final Exception e) {
			if("".equals(reqType))
				dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
			log4j.error("Exception in HolidayCalendar : ", e);
		}
		finally {
			try {
				con.close();
				if(dispatch != null) {
					response.setContentType("text/html; charset=UTF-8");
					response.setCharacterEncoding("UTF-8");
					dispatch.include(request, response);
				}
			}
			catch (final Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				log4j.error("Exception in HolidayCalendar : ", e);
			}
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doPost(request, response);
	}

	public String getServletInfo() {
		return "HolidayCalendar Servlet";
	}
}