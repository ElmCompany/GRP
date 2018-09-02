package sa.elm.ob.hcm.ad_forms.qualification.ajax;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

import sa.elm.ob.hcm.ad_forms.qualification.dao.QualificationDAO;
import sa.elm.ob.hcm.ad_forms.qualification.vo.QualificationVO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.utility.util.Utility;

public class QualificationAjax extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		VariablesSecureApp vars = new VariablesSecureApp(request);
		Connection con = null;
		QualificationDAO  dao = null;
		    String lang = vars.getLanguage().toString();
		try {
			con = getConnection();
			//commonDAO = new CommonDAO(con);
			dao = new QualificationDAO(con);
			String action = (request.getParameter("action") == null ? "" : request.getParameter("action"));
			if(action.equals("GetQualificationList")) {
				String employeeId = request.getParameter("inpEmployeeId");
				String QualificationId = request.getParameter("inpQualificationId");
				String searchFlag = request.getParameter("_search");
				String hijiriDate = "";
				QualificationVO qualVO = new QualificationVO();
				if(searchFlag != null && searchFlag.equals("true")) {
					if(!StringUtils.isEmpty(request.getParameter("edulevel")))
					  qualVO.setEducationlevel(request.getParameter("edulevel").replace("'", "''"));
					if(!StringUtils.isEmpty(request.getParameter("establishment")))
					  qualVO.setEstablishment(request.getParameter("establishment").replace("'", "''"));
					if(!StringUtils.isEmpty(request.getParameter("degree")))
					  qualVO.setDegree(request.getParameter("degree").replace("'", "''"));
					if(!StringUtils.isEmpty(request.getParameter("completionyear")))
					  qualVO.setCompletionyear(request.getParameter("completionyear").replace("'", "''"));
					if(!StringUtils.isEmpty(request.getParameter("licensesub")))
					  qualVO.setLicensesub(request.getParameter("licensesub").replace("'", "''"));
					if(!StringUtils.isEmpty(request.getParameter("expirydate"))){
					  hijiriDate = Utility.convertToGregorian(request.getParameter("expirydate"));
					  qualVO.setExpirydate(request.getParameter("expirydate_s") + "##" + hijiriDate);
					}
					if(!StringUtils.isEmpty(request.getParameter("startdate"))){
					  hijiriDate = Utility.convertToGregorian(request.getParameter("startdate"));
					  qualVO.setStartdate(request.getParameter("startdate_s") + "##" + hijiriDate);
                                        }
					if(!StringUtils.isEmpty(request.getParameter("enddate"))){
					  hijiriDate = Utility.convertToGregorian(request.getParameter("enddate"));
					  qualVO.setEnddate(request.getParameter("enddate_s") + "##" + hijiriDate);
                                        }
					if(!StringUtils.isEmpty(request.getParameter("location")))
                                          qualVO.setLocation(request.getParameter("location").replace("'", "''"));
				}
				JSONObject searchAttr = new JSONObject();
				searchAttr.put("rows", request.getParameter("rows").toString());
				searchAttr.put("page", request.getParameter("page").toString());
				searchAttr.put("search", searchFlag);
				searchAttr.put("sortName", request.getParameter("sidx").toString());
				searchAttr.put("sortType", request.getParameter("sord").toString());
				List<QualificationVO> list = dao.getQualificationList(vars.getClient(), request.getSession().getAttribute("Employee_ChildOrg").toString(), qualVO, searchAttr, employeeId, QualificationId);
				response.setContentType("text/xml");
				response.setCharacterEncoding("UTF-8");
				response.setHeader("Cache-Control", "no-cache");
				StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
				if(list.size() > 0) {
					String[] pageDetails = list.get(0).getStatus().split("_");
					xmlData.append("<page>" + pageDetails[0] + "</page><total>" + pageDetails[1] + "</total><records>" + pageDetails[2] + "</records>");
					for (int i = 1; i < list.size(); i++) {
					        qualVO = (QualificationVO) list.get(i);
						xmlData.append("<row id='" + qualVO.getQualificationId() + "'>");
						if(qualVO.getEducationlevel().equals("W"))
	                                          xmlData.append("<cell><![CDATA[" + Resource.getProperty("hcm.qualifications.Without", lang) + "]]></cell>");
						else if(qualVO.getEducationlevel().equals("P"))
                                                  xmlData.append("<cell><![CDATA[" + Resource.getProperty("hcm.qualifications.Preliminary", lang) + "]]></cell>");
						else if(qualVO.getEducationlevel().equals("I"))
                                                  xmlData.append("<cell><![CDATA[" + Resource.getProperty("hcm.qualifications.Intermediary", lang) + "]]></cell>");
						else if(qualVO.getEducationlevel().equals("S"))
                                                  xmlData.append("<cell><![CDATA[" + Resource.getProperty("hcm.qualifications.Secondary", lang) + "]]></cell>");
						else if(qualVO.getEducationlevel().equals("D"))
                                                  xmlData.append("<cell><![CDATA[" + Resource.getProperty("hcm.qualifications.Diploma", lang) + "]]></cell>");
					        else if(qualVO.getEducationlevel().equals("U"))
                                                  xmlData.append("<cell><![CDATA[" + Resource.getProperty("hcm.qualifications.University", lang) + "]]></cell>");
					        else if(qualVO.getEducationlevel().equals("H"))
                                                  xmlData.append("<cell><![CDATA[" + Resource.getProperty("hcm.qualifications.High", lang) + "]]></cell>");
					        else if(qualVO.getEducationlevel().equals("C"))
                                                  xmlData.append("<cell><![CDATA[" + Resource.getProperty("hcm.qualifications.Certification", lang) + "]]></cell>");
						//xmlData.append("<cell><![CDATA[" + qualVO.getEstablishment() + "]]></cell>");
	                                        xmlData.append("<cell><![CDATA[" + qualVO.getStartdate() + "]]></cell>");
                                                xmlData.append("<cell><![CDATA[" + qualVO.getEnddate() + "]]></cell>");
						xmlData.append("<cell><![CDATA[" + qualVO.getDegree() + "]]></cell>");
						xmlData.append("<cell><![CDATA[" + qualVO.getCompletionyear() + "]]></cell>");
	                                        xmlData.append("<cell><![CDATA[" + qualVO.getLocation() + "]]></cell>");
						xmlData.append("<cell><![CDATA[" + qualVO.getLicensesub() + "]]></cell>");
						xmlData.append("<cell><![CDATA[" + qualVO.getExpirydate() + "]]></cell>");
						xmlData.append("</row>");
					}
				}
				else
					xmlData.append("<page>" + 0 + "</page><total>" + 0 + "</total><records>" + 0 + "</records>");
				xmlData.append("</rows>");
				response.getWriter().write(xmlData.toString());
			}
			else if(action.equals("DeleteQualification")) {
                          response.setContentType("text/xml");
                          response.setCharacterEncoding("UTF-8");
                          response.setHeader("Cache-Control", "no-cache");
                          response.getWriter().write("<DeleteQualification>");
                          response.getWriter().write("<Response>" + dao.deleteEmployee(request.getParameter("inpQualificationId")) + "</Response>");
                          response.getWriter().write("</DeleteQualification>");
                  }

		}
		catch (final Exception e) {
			log4j.error("Error in EmployeeAjax : ", e);
		}
		finally {
			try {
				con.close();
			}
			catch (final SQLException e) {
				log4j.error("Error in EmployeeAjax : ", e);
			}
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doPost(request, response);
	}

	public String getServletInfo() {
		return "EmployeeAjax Servlet";
	}
}