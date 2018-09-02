package sa.elm.ob.hcm.ad_forms.asset.dao;

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

import sa.elm.ob.hcm.ad_forms.asset.vo.AssetVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class AssetDAO {
  private Connection conn = null;
  VariablesSecureApp vars = null;
  private static Logger log4j = Logger.getLogger(AssetDAO.class);

  public AssetDAO(Connection con) {
    this.conn = con;
  }

  public AssetVO getAssetEditList(String assetId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    AssetVO assetVO = null;
    try {
      st = conn
          .prepareStatement("SELECT ehcm_emp_asset_id, ehcm_emp_perinfo_id,"
              + "(select eut_convert_to_hijri (to_char(start_date,'YYYY-MM-DD HH24:MI:SS' ))) as startdate,"
              + "(select eut_convert_to_hijri (to_char(end_date,'YYYY-MM-DD HH24:MI:SS' ))) as enddate,"
              + "(select eut_convert_to_hijri (to_char(letter_date,'YYYY-MM-DD HH24:MI:SS' ))) as letterdate,"
              + " name as name ,decision_no as decisionNo , letter_no as letterNo,balance as balance,description "
              + "FROM ehcm_emp_asset WHERE ehcm_emp_asset_id = ?");

      st.setString(1, assetId);

      rs = st.executeQuery();
      if (rs.next()) {
        assetVO = new AssetVO();
        assetVO.setAssetId(Utility.nullToEmpty(rs.getString("ehcm_emp_asset_id")));
        assetVO.setStartdate(rs.getString("startdate") == null ? "" : rs.getString("startdate"));
        assetVO.setEnddate(rs.getString("enddate") == null ? "" : rs.getString("enddate"));
        assetVO.setLetterdate(rs.getString("letterdate") == null ? "" : rs.getString("letterdate"));
        assetVO.setAssetname(Utility.nullToEmpty(rs.getString("name")));
        assetVO.setLetterno(Utility.nullToEmpty(rs.getString("letterNo")));
        assetVO.setDecisionno(Utility.nullToEmpty(rs.getString("decisionNo")));
        assetVO.setBalance(rs.getBigDecimal("balance"));
        assetVO.setDescription(Utility.nullToEmpty(rs.getString("description")));

      }
    } catch (final SQLException e) {
      log4j.error("", e);
    } catch (final Exception e) {
      log4j.error("", e);
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("", e);
      }
    }
    return assetVO;
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

  public boolean deleteAsset(String qualId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {

      st = conn.prepareStatement("DELETE FROM ehcm_emp_asset WHERE ehcm_emp_asset_id = ?");
      st.setString(1, qualId);
      st.executeUpdate();

    } catch (final SQLException e) {
      log4j.error("", e);
      return false;
    } catch (final Exception e) {
      log4j.error("", e);
      return false;
    } finally {
      try {
        st.close();
      } catch (final SQLException e) {
        log4j.error("", e);
        return false;
      }
    }
    return true;
  }

  public List<AssetVO> getAssetList(String clientId, String childOrgId, AssetVO assetVO,
      JSONObject searchAttr, String selEmployeeId, String AssetId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<AssetVO> ls = new ArrayList<AssetVO>();
    AssetVO aVO = null;
    String sqlQuery = "", whereClause = "", orderClause = "";
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    String date = "";
    try {
      aVO = new AssetVO();
      aVO.setStatus("0_0_0");
      ls.add(aVO);
      int offset = 0, totalPage = 0, totalRecord = 0;
      int rows = Integer.parseInt(searchAttr.getString("rows")), page = Integer.parseInt(searchAttr
          .getString("page"));

      whereClause = " AND ad_client_id = '" + clientId + "' ";

      if (searchAttr.has("search") && searchAttr.getString("search").equals("true")) {

        if (!StringUtils.isEmpty(assetVO.getAssetname()))
          whereClause += " and name ilike '%" + assetVO.getAssetname() + "%'";
        if (!StringUtils.isEmpty(assetVO.getLetterno()))
          whereClause += " and letter_no ilike '%" + assetVO.getLetterno() + "%'";
        if (!StringUtils.isEmpty(assetVO.getDecisionno()))
          whereClause += " and decision_no ilike '%" + assetVO.getDecisionno() + "%'";
        if (!StringUtils.isEmpty(assetVO.getDescription()))
          whereClause += " and description ilike '%" + assetVO.getDescription() + "%'";
        if (assetVO.getBalance() != null)
          whereClause += " and CAST(balance AS TEXT) like '%" + assetVO.getBalance() + "%'";

        if (!StringUtils.isEmpty(assetVO.getLetterdate()))
          whereClause += " and letter_date " + assetVO.getLetterdate().split("##")[0]
              + " to_timestamp('" + assetVO.getLetterdate().split("##")[1]
              + "', 'yyyy-MM-dd HH24:MI:SS') ";
        if (!StringUtils.isEmpty(assetVO.getStartdate()))
          whereClause += " and start_date " + assetVO.getStartdate().split("##")[0]
              + " to_timestamp('" + assetVO.getStartdate().split("##")[1]
              + "', 'yyyy-MM-dd HH24:MI:SS') ";
        if (!StringUtils.isEmpty(assetVO.getEnddate()))
          whereClause += " and end_date " + assetVO.getEnddate().split("##")[0] + " to_timestamp('"
              + assetVO.getEnddate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";
      }

      orderClause = " order by " + searchAttr.getString("sortName") + " "
          + searchAttr.getString("sortType");
      // Get Row Count
      sqlQuery = "select count(ehcm_cus_asset_v_id) from ehcm_cus_asset_v where ehcm_emp_perinfo_id ='"
          + selEmployeeId + "'";
      sqlQuery += whereClause;
      st = conn.prepareStatement(sqlQuery);
      rs = st.executeQuery();
      if (rs.next())
        totalRecord = rs.getInt("count");

      // Selected Qualification Row
      if (AssetId != null && AssetId.length() == 32) {
        sqlQuery = "  select tb.rowno from (SELECT row_number() OVER (" + orderClause
            + ") as rowno, ehcm_cus_asset_v_id  from  ehcm_cus_asset_v WHERE ehcm_emp_perinfo_id ='"
            + selEmployeeId + "'";
        sqlQuery += whereClause;
        sqlQuery += orderClause;
        sqlQuery += ")tb where tb.ehcm_cus_asset_v_id = '" + AssetId + "';";
        st = conn.prepareStatement(sqlQuery);
        rs = st.executeQuery();
        if (rs.next()) {
          int rowNo = rs.getInt("rowno"), currentPage = rowNo / rows;
          if (currentPage == 0) {
            page = 1;
            offset = 0;
          } else {
            page = currentPage;
            if ((rowNo % rows) == 0)
              offset = ((page - 1) * rows);
            else {
              offset = (page * rows);
              page = currentPage + 1;
            }
          }
        }
      } else {
        if (totalRecord > 0) {
          totalPage = totalRecord / rows;
          if (totalRecord % rows > 0)
            totalPage += 1;
          offset = ((page - 1) * rows);
          if (page > totalPage) {
            page = totalPage;
            offset = ((page - 1) * rows);
          }
        } else {
          page = 0;
          totalPage = 0;
          offset = 0;
        }
      }
      if (totalRecord > 0) {
        totalPage = totalRecord / rows;
        if (totalRecord % rows > 0)
          totalPage += 1;
      } else {
        page = 0;
        totalPage = 0;
      }

      // Adding Page Details
      aVO.setStatus(page + "_" + totalPage + "_" + totalRecord);
      ls.remove(0);
      ls.add(aVO);

      // Employee Details
      sqlQuery = "SELECT ehcm_cus_asset_v_id, ehcm_emp_perinfo_id,start_date as startdate,end_date as enddate,"
          + "letter_date as letterdate, name as name ,decision_no as decisionNo , letter_no as letterNo,balance as balance,description,flag "
          + "FROM ehcm_cus_asset_v WHERE ehcm_emp_perinfo_id ='" + selEmployeeId + "'";
      sqlQuery += whereClause;
      sqlQuery += orderClause;
      sqlQuery += " limit " + rows + " offset " + offset;
      st = conn.prepareStatement(sqlQuery);
      log4j.debug("Asset Info : " + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        aVO = new AssetVO();
        aVO.setAssetId(Utility.nullToEmpty(rs.getString("ehcm_cus_asset_v_id")));
        aVO.setAssetname(Utility.nullToEmpty(rs.getString("name")));
        // qVO.setEstablishment(Utility.nullToEmpty(rs.getString("establishment")));
        if (rs.getDate("startdate") != null) {
          date = df.format(rs.getDate("startdate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          aVO.setStartdate(date);
        } else
          aVO.setStartdate("");
        if (rs.getDate("enddate") != null) {
          date = df.format(rs.getDate("enddate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          aVO.setEnddate(date);
        } else
          aVO.setEnddate("");
        if (rs.getDate("letterdate") != null) {
          date = df.format(rs.getDate("letterdate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          aVO.setLetterdate(date);
        } else
          aVO.setLetterdate("");
        aVO.setLetterno(Utility.nullToEmpty(rs.getString("letterNo")));
        aVO.setDecisionno(Utility.nullToEmpty(rs.getString("decisionNo")));
        aVO.setBalance(rs.getBigDecimal("balance"));
        aVO.setDescription(Utility.nullToEmpty(rs.getString("description")));
        aVO.setFlag(rs.getString("flag"));
    
        ls.add(aVO);
      }
    } catch (final Exception e) {
      log4j.error("Exception in getEmployeeList", e);
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getEmployeeList", e);
      }
    }
    return ls;
  }
}
