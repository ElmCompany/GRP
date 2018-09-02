package sa.elm.ob.hcm.ad_forms.qualification.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;

import sa.elm.ob.hcm.ad_forms.qualification.vo.QualificationVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class QualificationDAO {
  private Connection conn = null;
  VariablesSecureApp vars = null;
  private static Logger log4j = Logger.getLogger(QualificationDAO.class);

  public QualificationDAO(Connection con) {
    this.conn = con;
  }

	public QualificationVO getEmployeeEditList(String employeeId) {
		PreparedStatement st = null;
		ResultSet rs = null;
		QualificationVO qualificationVO = null;
		try {
			st = conn.prepareStatement("SELECT qual.ehcm_qualification_id, qual.ehcm_emp_perinfo_id,  (select eut_convert_to_hijri (to_char(qual.startdate,'YYYY-MM-DD HH24:MI:SS' )))	 as startdate, "
					+ " (select eut_convert_to_hijri (to_char(qual.enddate,'YYYY-MM-DD HH24:MI:SS' )))	as enddate, (select eut_convert_to_hijri (to_char(qual.expirydate,'YYYY-MM-DD HH24:MI:SS' ))) as expirydate, "
					+ "	 qual.establishment as establishment ,qual.degree as degree , qual.completionyear as completeyear,qual.location as location	, qual.licensesub as licensesub,qual.edulevel as edulevel FROM ehcm_qualification qual WHERE qual.ehcm_qualification_id = ?");

			st.setString(1, employeeId);

			rs = st.executeQuery();
			if(rs.next()) {
				qualificationVO = new QualificationVO();
				qualificationVO.setQualificationId(Utility.nullToEmpty(rs.getString("ehcm_qualification_id")));
				qualificationVO.setStartdate(rs.getString("startdate") == null ? "" : rs.getString("startdate"));
				qualificationVO.setEnddate(rs.getString("enddate") == null ? "" : rs.getString("enddate"));
				qualificationVO.setExpirydate(rs.getString("expirydate") == null ? "" : rs.getString("expirydate"));
				qualificationVO.setEstablishment(Utility.nullToEmpty(rs.getString("establishment")));
				qualificationVO.setDegree(Utility.nullToEmpty(rs.getString("degree")));

				qualificationVO.setCompletionyear(Utility.nullToEmpty(rs.getString("completeyear")));
				qualificationVO.setLocation(Utility.nullToEmpty(rs.getString("location")));
				qualificationVO.setLicensesub(Utility.nullToEmpty(rs.getString("licensesub")));
				qualificationVO.setEducationlevel(Utility.nullToEmpty(rs.getString("edulevel")));

			}
		}
		catch (final SQLException e) {
			log4j.error("", e);
		}
		catch (final Exception e) {
			log4j.error("", e);
		}
		finally {
			try {
				st.close();
				rs.close();
			}
			catch (final SQLException e) {
				log4j.error("", e);
			}
		}
		return qualificationVO;
	}
  public Date convertGregorian(String hijridate) {
    log4j.debug("hi:" + hijridate);
    String gregDate = Utility.convertToGregorian(hijridate);
    log4j.debug("gregDate:" + gregDate);
    Date greDate = null;
    try {
      DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
      greDate = df1.parse(gregDate);
      log4j.debug("greDate:" + greDate);
    } catch (Exception e) {
      log4j.error("Exception creating multiple transactions from payments", e);

    }
    return greDate;

  }
  
  public boolean deleteEmployee(String qualId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {

            st = conn.prepareStatement("DELETE FROM ehcm_qualification WHERE ehcm_qualification_id = ?");
            st.setString(1, qualId);
            st.executeUpdate();

    }
    catch (final SQLException e) {
            log4j.error("", e);
            return false;
    }
    catch (final Exception e) {
            log4j.error("", e);
            return false;
    }
    finally {
            try {
                    st.close();
            }
            catch (final SQLException e) {
                    log4j.error("", e);
                    return false;
            }
    }
    return true;
}
  
  public List<QualificationVO> getQualificationList(String clientId, String childOrgId, QualificationVO qualVO, JSONObject searchAttr, String selEmployeeId, String QualificationId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<QualificationVO> ls = new ArrayList<QualificationVO>();
    QualificationVO qVO = null;
    String sqlQuery = "", whereClause = "", orderClause = "";
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    String date = "";
    try {
      qVO = new QualificationVO();
      qVO.setStatus("0_0_0");
      ls.add(qVO);
            int offset = 0, totalPage = 0, totalRecord = 0;
            int rows = Integer.parseInt(searchAttr.getString("rows")), page = Integer.parseInt(searchAttr.getString("page"));

            whereClause = " AND ad_client_id = '" + clientId + "' ";

            if(searchAttr.has("search") && searchAttr.getString("search").equals("true")) {
                   
                    if(!StringUtils.isEmpty(qualVO.getEducationlevel()))
                      whereClause += " and edulevel ilike '%" + qualVO.getEducationlevel() + "%'";
                    if(!StringUtils.isEmpty(qualVO.getEstablishment()))
                      whereClause += " and establishment ilike '%" + qualVO.getEstablishment() + "%'";
                    if(!StringUtils.isEmpty(qualVO.getDegree()))
                      whereClause += " and degree ilike '%" + qualVO.getDegree() + "%'";
                    if(!StringUtils.isEmpty(qualVO.getCompletionyear()))
                      whereClause += " and CAST(completionyear AS TEXT) like '%" + qualVO.getCompletionyear() + "%'";
                    if(!StringUtils.isEmpty(qualVO.getLicensesub()))
                      whereClause += " and licensesub ilike '%" + qualVO.getLicensesub() + "%'";
                    if(!StringUtils.isEmpty(qualVO.getLocation()))
                      whereClause += " and location ilike '%" + qualVO.getLocation() + "%'";
                    if(!StringUtils.isEmpty(qualVO.getExpirydate()))
                      whereClause += " and expirydate " + qualVO.getExpirydate().split("##")[0] + " to_timestamp('" + qualVO.getExpirydate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";
                    if(!StringUtils.isEmpty(qualVO.getStartdate()))
                      whereClause += " and startdate " + qualVO.getStartdate().split("##")[0] + " to_timestamp('" + qualVO.getStartdate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";
                    if(!StringUtils.isEmpty(qualVO.getEnddate()))
                      whereClause += " and enddate " + qualVO.getEnddate().split("##")[0] + " to_timestamp('" + qualVO.getEnddate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

            }

            orderClause = " order by " + searchAttr.getString("sortName") + " " + searchAttr.getString("sortType");
            // Get Row Count
            sqlQuery = "select count(ehcm_qualification_id) from ehcm_qualification where ehcm_emp_perinfo_id ='"+ selEmployeeId +"'"; 
            sqlQuery += whereClause;
            st = conn.prepareStatement(sqlQuery);
            rs = st.executeQuery();
            if(rs.next())
                    totalRecord = rs.getInt("count");

            // Selected Qualification Row
            if(QualificationId != null && QualificationId.length() == 32) {
                    sqlQuery = "  select tb.rowno from (SELECT row_number() OVER ("
                                    + orderClause
                                    + ") as rowno, ehcm_qualification_id  from  ehcm_qualification WHERE ehcm_emp_perinfo_id ='"+ selEmployeeId +"'";
                    sqlQuery += whereClause;
                    sqlQuery += orderClause;
                    sqlQuery += ")tb where tb.ehcm_qualification_id = '" + QualificationId + "';";
                    st = conn.prepareStatement(sqlQuery);
                    rs = st.executeQuery();
                    if(rs.next()) {
                            int rowNo = rs.getInt("rowno"), currentPage = rowNo / rows;
                            if(currentPage == 0) {
                                    page = 1;
                                    offset = 0;
                            }
                            else {
                                    page = currentPage;
                                    if((rowNo % rows) == 0)
                                            offset = ((page - 1) * rows);
                                    else {
                                            offset = (page * rows);
                                            page = currentPage + 1;
                                    }
                            }
                    }
            }
            else {
                    if(totalRecord > 0) {
                            totalPage = totalRecord / rows;
                            if(totalRecord % rows > 0)
                                    totalPage += 1;
                            offset = ((page - 1) * rows);
                            if(page > totalPage) {
                                    page = totalPage;
                                    offset = ((page - 1) * rows);
                            }
                    }
                    else {
                            page = 0;
                            totalPage = 0;
                            offset = 0;
                    }
            }
            if(totalRecord > 0) {
                    totalPage = totalRecord / rows;
                    if(totalRecord % rows > 0)
                            totalPage += 1;
            }
            else {
                    page = 0;
                    totalPage = 0;
            }

            // Adding Page Details
            qVO.setStatus(page + "_" + totalPage + "_" + totalRecord);
            ls.remove(0);
            ls.add(qVO);

            // Employee Details
            sqlQuery="select ehcm_qualification_id,edulevel,establishment,degree,startdate,enddate,completionyear,licensesub,location,expirydate from ehcm_qualification where ehcm_emp_perinfo_id='"+selEmployeeId+"'";
            sqlQuery += whereClause;
            sqlQuery += orderClause;
            sqlQuery += " limit " + rows + " offset " + offset;
            st = conn.prepareStatement(sqlQuery);
            log4j.debug("Qualification Info : " + st.toString());
            rs = st.executeQuery();
            while (rs.next()) {
                    qVO = new QualificationVO();
                    qVO.setQualificationId(Utility.nullToEmpty(rs.getString("ehcm_qualification_id")));
                    qVO.setEducationlevel(Utility.nullToEmpty(rs.getString("edulevel")));
                    //qVO.setEstablishment(Utility.nullToEmpty(rs.getString("establishment")));
                    if(rs.getDate("startdate") != null) {
                      date = df.format(rs.getDate("startdate"));
                      date = dateYearFormat.format(df.parse(date));
                      date = UtilityDAO.convertTohijriDate(date);
                      qVO.setStartdate(date);
                    }
                    else
                      qVO.setStartdate("");
                    if(rs.getDate("enddate") != null) {
                      date = df.format(rs.getDate("enddate"));
                      date = dateYearFormat.format(df.parse(date));
                      date = UtilityDAO.convertTohijriDate(date);
                      qVO.setEnddate(date);
                    }
                    else
                      qVO.setEnddate("");
                    qVO.setDegree(Utility.nullToEmpty(rs.getString("degree")));
                    qVO.setCompletionyear(Utility.nullToEmpty(rs.getString("completionyear")));
                    qVO.setLocation(Utility.nullToEmpty(rs.getString("location")));
                    qVO.setLicensesub(Utility.nullToEmpty(rs.getString("licensesub")));
                    if(rs.getDate("expirydate") != null) {
                      date = df.format(rs.getDate("expirydate"));
                      date = dateYearFormat.format(df.parse(date));
                      date = UtilityDAO.convertTohijriDate(date);
                      qVO.setExpirydate(date);
                    }
                    else
                      qVO.setExpirydate("");
                      ls.add(qVO);
            }
    }
    catch (final Exception e) {
            log4j.error("Exception in getEmployeeList", e);
    }
    finally {
            try {
                    st.close();
                    rs.close();
            }
            catch (final SQLException e) {
                    log4j.error("Exception in getEmployeeList", e);
            }
    }
    return ls;
}

}
