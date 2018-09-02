package sa.elm.ob.hcm.ad_forms.asset.ajax;

import java.io.IOException;
import java.math.BigDecimal;
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

import sa.elm.ob.hcm.ad_forms.asset.dao.AssetDAO;
import sa.elm.ob.hcm.ad_forms.asset.vo.AssetVO;
import sa.elm.ob.utility.util.Utility;

public class AssetAjax extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection con = null;
    AssetDAO dao = null;
    try {
      con = getConnection();
      // commonDAO = new CommonDAO(con);
      dao = new AssetDAO(con);
      String action = (request.getParameter("action") == null ? "" : request.getParameter("action"));
      if (action.equals("GetAssetList")) {
        String employeeId = request.getParameter("inpEmployeeId");
        String AssetId = request.getParameter("inpAssetId");
        String searchFlag = request.getParameter("_search");
        String hijiriDate = "";
        AssetVO assetVO = new AssetVO();

        if (searchFlag != null && searchFlag.equals("true")) {
          if (!StringUtils.isEmpty(request.getParameter("name")))
            assetVO.setAssetname(request.getParameter("name").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("letterNo")))
            assetVO.setLetterno(request.getParameter("letterNo").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("decisionNo")))
            assetVO.setDecisionno(request.getParameter("decisionNo").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("description")))
            assetVO.setDecisionno(request.getParameter("description").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("balance")))
            assetVO.setBalance(new BigDecimal(request.getParameter("balance")));
          if (!StringUtils.isEmpty(request.getParameter("letterdate"))) {
            hijiriDate = Utility.convertToGregorian(request.getParameter("letterdate"));
            assetVO.setLetterdate(request.getParameter("letterdate_s") + "##" + hijiriDate);
          }
          if (!StringUtils.isEmpty(request.getParameter("startdate"))) {
            hijiriDate = Utility.convertToGregorian(request.getParameter("startdate"));
            assetVO.setStartdate(request.getParameter("startdate_s") + "##" + hijiriDate);
          }
          if (!StringUtils.isEmpty(request.getParameter("enddate"))) {
            hijiriDate = Utility.convertToGregorian(request.getParameter("enddate"));
            assetVO.setEnddate(request.getParameter("enddate_s") + "##" + hijiriDate);
          }
        }
        JSONObject searchAttr = new JSONObject();
        searchAttr.put("rows", request.getParameter("rows").toString());
        searchAttr.put("page", request.getParameter("page").toString());
        searchAttr.put("search", searchFlag);
        searchAttr.put("sortName", request.getParameter("sidx").toString());
        searchAttr.put("sortType", request.getParameter("sord").toString());
        List<AssetVO> list = dao.getAssetList(vars.getClient(),
            request.getSession().getAttribute("Employee_ChildOrg").toString(), assetVO, searchAttr,
            employeeId, AssetId);
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
        if (list.size() > 0) {
          String[] pageDetails = list.get(0).getStatus().split("_");
          xmlData.append("<page>" + pageDetails[0] + "</page><total>" + pageDetails[1]
              + "</total><records>" + pageDetails[2] + "</records>");
          for (int i = 1; i < list.size(); i++) {
            assetVO = (AssetVO) list.get(i);
            xmlData.append("<row id='" + assetVO.getAssetId() + "'>");
            // xmlData.append("<cell><![CDATA[" + qualVO.getEstablishment() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + assetVO.getAssetname() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + assetVO.getStartdate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + assetVO.getEnddate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + assetVO.getLetterno() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + assetVO.getLetterdate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + assetVO.getDecisionno() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + assetVO.getBalance() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + assetVO.getDescription() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + assetVO.getFlag() + "]]></cell>");
            xmlData.append("</row>");
          }
        } else
          xmlData.append("<page>" + 0 + "</page><total>" + 0 + "</total><records>" + 0
              + "</records>");
        xmlData.append("</rows>");
        response.getWriter().write(xmlData.toString());
      } else if (action.equals("DeleteAsset")) {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write("<DeleteAsset>");
        response.getWriter().write(
            "<Response>" + dao.deleteAsset(request.getParameter("inpAssetId")) + "</Response>");
        response.getWriter().write("</DeleteAsset>");
      }

    } catch (final Exception e) {
      log4j.error("Error in AssetAjax : ", e);
    } finally {
      try {
        con.close();
      } catch (final SQLException e) {
        log4j.error("Error in AssetAjax : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doPost(request, response);
  }

  public String getServletInfo() {
    return "AssetAjax Servlet";
  }
}