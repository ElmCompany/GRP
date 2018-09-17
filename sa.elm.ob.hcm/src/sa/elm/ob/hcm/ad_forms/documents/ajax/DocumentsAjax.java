package sa.elm.ob.hcm.ad_forms.documents.ajax;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

import sa.elm.ob.hcm.ad_forms.documents.dao.DocumentsDAO;
import sa.elm.ob.hcm.ad_forms.documents.vo.DocumentsVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class DocumentsAjax extends HttpSecureAppServlet {
  /**
   * 
   */
  public static final String TABLE_EHCM_EMP_DOCUMENT = "4B83D01D9E49404E96A30DBE2DC0736F";
  private File destinationDir = null;
  File attachmentFile = null;
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection con = null;
    DocumentsDAO docdao = null;
    DocumentsVO docvo = null;
    String employeeId = request.getParameter("inpEmployeeId");
    String action = request.getParameter("inpAction");
    log4j.debug("Action" + action);
    try {
      OBContext.setAdminMode();
      con = getConnection();
      docdao = new DocumentsDAO(con);
      if (action.equals("Download") || action.equals("DownloadGrid")) {
        log4j.debug("enteredajax");
        String documentId = (request.getParameter("inpDocumentId") == null ? ""
            : request.getParameter("inpDocumentId"));

        String fileName = (request.getParameter("inpFileName") == null ? ""
            : request.getParameter("inpFileName"));
        String realPath = "";
        if (action.equals("DownloadGrid")) {
          docvo = docdao.getDocumentEditList(employeeId, documentId);
          fileName = docvo.getFilename();
          realPath = docvo.getPath();
        } else if (action.equals("Download")) {
          realPath = request.getParameter("inpPath");
        }
        if (fileName != null) {
          destinationDir = new File(realPath);
          log4j.debug("realpath:" + realPath);
          log4j.debug("destinationDir:" + destinationDir);
          log4j.debug("fileName:" + fileName);
          log4j.debug("documentId:" + documentId);

          attachmentFile = new File(destinationDir, fileName);
          if (attachmentFile.exists()) {
            log4j.debug("documentId:" + attachmentFile);

            // response.setHeader("Content-Type",
            // getServletContext().getMimeType(attachmentFile.getName()));
            response.setContentType("application/force-download");
            response.setContentLength((int) attachmentFile.length());
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + attachmentFile.getName() + "\"");
            BufferedInputStream input = null;
            BufferedOutputStream output = null;
            try {
              input = new BufferedInputStream(new FileInputStream(attachmentFile));
              output = new BufferedOutputStream(response.getOutputStream());

              byte[] buffer = new byte[response.getBufferSize()];
              for (int length = 0; (length = input.read(buffer)) > 0;) {
                output.write(buffer, 0, length);
              }
            } finally {
              if (output != null)
                try {
                  output.flush();
                  output.close();
                } catch (IOException ignore) {
                }
              if (input != null)
                try {
                  input.close();
                } catch (IOException ignore) {
                }
            }
            return;
          }
        }

      } else if (action.equals("getDocumentList")) {
        String docId = request.getParameter("inpDocumentId");
        docvo = new DocumentsVO();
        String searchFlag = request.getParameter("_search");
        String hijiriHireDate = "";
        log4j.debug("searchFlag:" + searchFlag);
        log4j.debug("validatrew:" + request.getParameter("validdate"));

        if (searchFlag != null && searchFlag.equals("true")) {
          if (!StringUtils.isEmpty(request.getParameter("docname")))
            docvo.setFilename(request.getParameter("docname"));
          if (!StringUtils.isEmpty(request.getParameter("doctype"))
              && !"0".equals(request.getParameter("doctype")))
            docvo.setDoctype(request.getParameter("doctype"));
          if (!StringUtils.isEmpty(request.getParameter("issueddate"))) {
            hijiriHireDate = Utility.convertToGregorian(request.getParameter("issueddate"));
            docvo.setIssueddate(request.getParameter("issueddate_s") + "##" + hijiriHireDate);
          }
          if (!StringUtils.isEmpty(request.getParameter("validdate"))) {
            log4j.debug("validdate:" + request.getParameter("validdate"));
            hijiriHireDate = Utility.convertToGregorian(request.getParameter("validdate"));
            docvo.setValiddate(request.getParameter("validdate_s") + "##" + hijiriHireDate);
          }
          if (!StringUtils.isEmpty(request.getParameter("isOriginal"))) {
            log4j.debug("isOriginal:" + request.getParameter("isOriginal"));
            docvo.setIsoriginal(request.getParameter("isOriginal"));
          }

        }
        JSONObject searchAttr = new JSONObject();
        searchAttr.put("rows", request.getParameter("rows").toString());
        searchAttr.put("page", request.getParameter("page").toString());
        searchAttr.put("search", searchFlag);
        searchAttr.put("sortName", request.getParameter("sidx").toString());
        searchAttr.put("sortType", request.getParameter("sord").toString());
        List<DocumentsVO> list = docdao.getDocumentsList(vars.getClient(), employeeId, docvo,
            vars.getOrg(), searchAttr, docId);

        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
        // log4j.debug("totalRecord:" + totalRecord + ">> list size" + list.size());
        String doctypeval = "";
        if (list.size() > 0) {
          String[] pageDetails = list.get(0).getStatus().split("_");
          xmlData.append("<page>" + pageDetails[0] + "</page><total>" + pageDetails[1]
              + "</total><records>" + pageDetails[2] + "</records>");

          for (int i = 1; i < list.size(); i++) {
            DocumentsVO VO = (DocumentsVO) list.get(i);

            log4j.debug("page" + pageDetails);

            /*
             * if (!(VO.getDoctype() == "null") && !(VO.getDoctype() == (null))) { if
             * (VO.getDoctype().equals("H")) doctypeval = "Hiring Decision"; else if
             * (VO.getDoctype().equals("O")) doctypeval = "Offer Letter"; else if
             * (VO.getDoctype().equals("I")) doctypeval = "ID Copy"; else if
             * (VO.getDoctype().equals("M")) doctypeval = "Medical Report"; log4j.debug("vodoctyp" +
             * doctypeval);
             */

            xmlData.append("<row id='" + VO.getId() + "'>");
            // xmlData.append("<cell><![CDATA[" + VO.getId() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getFilename() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getDoctype() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getIssueddate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getValiddate() + "]]></cell>");
            if (VO.getIsoriginal().equals("O")) {
              String IsOriginal = "Original";
              xmlData.append("<cell><![CDATA[" + IsOriginal + "]]></cell>");
            }
            if (VO.getIsoriginal().equals("D")) {
              String IsOriginal = "Duplicate";
              xmlData.append("<cell><![CDATA[" + IsOriginal + "]]></cell>");
            }

            xmlData
                .append("<cell><![CDATA[" + VO.getPath() + "/" + VO.getFilename() + "]]></cell>");
            xmlData.append("</row>");
          }
        } else
          xmlData
              .append("<page>" + 0 + "</page><total>" + 0 + "</total><records>" + 0 + "</records>");
        xmlData.append("</rows>");
        response.getWriter().write(xmlData.toString());

      } else if (action.equals("deleteDocument")) {
        String rowval = request.getParameter("rowval");
        log4j.debug("rowval" + rowval);
        String arrdocidval = rowval.replace("[", "");
        String[] arrdocidval1 = arrdocidval.replace("]", ",").split(",");
        log4j.debug("length" + arrdocidval1.length);
        for (int i = 0; i < arrdocidval1.length; i++) {
          log4j.debug("rowval" + arrdocidval1[i] + "i" + i);
          String docId = arrdocidval1[i];
          docdao.deleteDocument(docId);
        }
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write("<DeleteDocument>");
        response.getWriter().write("<Response>" + true + "</Response>");
        response.getWriter().write("</DeleteDocument>");

      }

      if (action.equals("getDocumentType")) {

        JSONObject jsob = new JSONObject();
        // DocumentsDAO.getDocType(vars.getClient(),
        // request.getParameter("searchTerm"), 20, 1, vars.getRole(), vars.getOrg())
        jsob = DocumentsDAO.getDocType(vars.getClient(), request.getParameter("searchTerm"),
            Integer.parseInt(request.getParameter("pageLimit")),
            Integer.parseInt(request.getParameter("page")), vars.getRole(), vars.getOrg());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());

      }
      log4j.debug(action);
      if (action.equals("CheckValidData")) {
        JSONObject jsob = new JSONObject();
        String isExist = "N";
        boolean doc = false;
        Date validDate = null;
        String docTypeId = request.getParameter("inpDocType");
        String empId = request.getParameter("inpEmployeeId");
        String docId = request.getParameter("inpDocumentId");
        Date issuedDate = UtilityDAO.convertToGregorianDate(request.getParameter("inpIssuedDate"));

        if (request.getParameter("inpValidDate") != "") {
          validDate = UtilityDAO.convertToGregorianDate(request.getParameter("inpValidDate"));
        }
        doc = docdao.checkValidData(empId, docId, docTypeId, issuedDate, validDate);
        if (doc) {
          isExist = "Y";
        }
        jsob.put("isExists", isExist);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsob.toString());
      }
      if (action.equals("DateValidation")) {
        JSONObject jsob = new JSONObject();
        String isExist = "N";
        Date validDate = null;
        Date issuedDate = UtilityDAO.convertToGregorianDate(request.getParameter("inpIssuedDate"));
        if (request.getParameter("inpValidDate") != "") {
          validDate = UtilityDAO.convertToGregorianDate(request.getParameter("inpValidDate"));

          if (issuedDate.after(validDate)) {
            isExist = "Y";
          }
        }
        jsob.put("isExists", isExist);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsob.toString());
      }
      if (action.equals("getUsersList")) {
        DocumentsVO searchVO = new DocumentsVO();

        String sortColName = request.getParameter("sidx");
        String sortColType = request.getParameter("sord");
        String searchFlag = request.getParameter("_search");
        int page, limit;

        if (searchFlag != null && searchFlag.equals("true")) {
          if (request.getParameter("employee") != null)
            searchVO.setUserName(request.getParameter("employee").replace("'", "''"));
          if (request.getParameter("email") != null)
            searchVO.setEmailId(request.getParameter("email").replace("'", "''"));
        }

        try {
          page = Integer.parseInt(vars.getStringParameter("page"));
        } catch (Exception e) {

          log4j.error("Exception in getting userlist : ", e);
          page = 1;
        }
        try {
          limit = Integer.parseInt(vars.getStringParameter("rows"));
        } catch (Exception e) {
          limit = 20;
        }

        List<DocumentsVO> usersList = new ArrayList<DocumentsVO>();
        usersList = docdao.getUsersList(page, limit, sortColType, sortColName, vars.getClient(),
            searchVO, searchFlag);

        int pages = 0, totalPages = 0, count = 0;
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuilder sb = new StringBuilder("<?xml version='1.0'  encoding='utf-8' ?>");
        sb.append("<rows>");

        if (usersList.size() > 0) {

          for (DocumentsVO vo : usersList) {
            pages = vo.getPage();
            totalPages = vo.getTotalPages();
            count = vo.getCount();
            sb.append("<row id = '" + vo.getUserId() + "'>");
            sb.append("<cell><![CDATA[" + vo.getUserName() + "]]></cell>");
            sb.append("<cell><![CDATA[" + vo.getEmailId() + "]]></cell>");
            sb.append("</row>");
          }

          sb.append("<page>" + pages + "</page>");
          sb.append("<total>" + totalPages + "</total>");
          sb.append("<records>" + count + "</records>");

        } else {
          sb.append("<page>" + pages + "</page>");
          sb.append("<total>" + totalPages + "</total>");
          sb.append("<records>" + count + "</records>");
        }
        sb.append("</rows>");
        response.getWriter().write(sb.toString());
      }

    } catch (final Exception e) {
      log4j.error("Error in DocumentAjax : ", e);
    } finally {
      try {
        con.close();
      } catch (final SQLException e) {
        log4j.error("Error in DocumentAjax : ", e);
      }
      OBContext.restorePreviousMode();
    }
  }
}
