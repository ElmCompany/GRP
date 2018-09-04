package sa.elm.ob.hcm.ad_forms.documents.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.SQLQuery;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.poc.EmailManager;
import org.openbravo.utils.CryptoUtility;

import sa.elm.ob.hcm.EhcmDocuments;
import sa.elm.ob.hcm.EhcmEmpDocType;
import sa.elm.ob.hcm.ad_forms.documents.vo.DocumentsVO;
import sa.elm.ob.utility.util.Utility;

public class DocumentsDAO {
  private static Logger log4j = Logger.getLogger(DocumentsDAO.class);
  private static Connection conn = null;
  VariablesSecureApp vars = null;

  public DocumentsDAO(Connection con) {
    this.conn = con;
  }

  public List<DocumentsVO> getDocumentsList(String clientId, String employeeId, DocumentsVO vo,
      String OrgId, JSONObject searchAttr, String selDocId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<DocumentsVO> docvo = new ArrayList<DocumentsVO>();
    DocumentsVO dcvo = null;
    String whereClause = "", orderClause = "", countQry = "", sqlQuery = "";

    try {
      dcvo = new DocumentsVO();
      dcvo.setStatus("0_0_0");
      docvo.add(dcvo);

      int offset = 0, totalPage = 0, totalRecord = 0;
      int rows = Integer.parseInt(searchAttr.getString("rows")),
          page = Integer.parseInt(searchAttr.getString("page"));

      whereClause = " AND doc.ad_client_id = '" + clientId + "' ";
      if (searchAttr.has("search") && searchAttr.getString("search").equals("true")) {
        if (!StringUtils.isEmpty(vo.getFilename()))
          whereClause += " and (f.name) ilike '%" + vo.getFilename() + "%'";
        if (!StringUtils.isEmpty(vo.getDoctype()))
          whereClause += " and doctype ='" + vo.getDoctype() + "'";
        if (!StringUtils.isEmpty(vo.getIssueddate()))
          whereClause += " and issueddate " + vo.getIssueddate().split("##")[0] + " to_timestamp('"
              + vo.getIssueddate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";
        if (!StringUtils.isEmpty(Utility.nullToEmpty(vo.getValiddate())))
          whereClause += " and validdate " + vo.getValiddate().split("##")[0] + " to_timestamp('"
              + vo.getValiddate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";
        if (!StringUtils.isEmpty(vo.getIsoriginal()))
          if (!vo.getIsoriginal().equals("0")) {
            whereClause += " and isOriginal='" + vo.getIsoriginal() + "'";
          }

      }

      if (StringUtils.equals(searchAttr.getString("sortName"), "value"))
        searchAttr.put("sortName", "docname");

      orderClause = " order by " + searchAttr.getString("sortName") + " "
          + searchAttr.getString("sortType");

      // Get Row Count
      countQry = " SELECT count(doc.ehcm_documents_id) as count  FROM ehcm_documents doc  join c_file f on doc.c_file_id=f.c_file_id "
          + "where ehcm_emp_perinfo_id = '" + employeeId + "'";
      countQry += whereClause;
      log4j.debug("countQry:" + countQry.toString());
      st = conn.prepareStatement(countQry);
      rs = st.executeQuery();
      if (rs.next())
        totalRecord = rs.getInt("count");
      log4j.debug("totalrecord:" + totalRecord);

      // Selected Document Row
      if (selDocId != null && selDocId.length() == 32) {
        sqlQuery = "  select tb.rowno from (SELECT row_number() OVER (" + orderClause
            + ") as rowno,  FROM ehcm_documents doc  join c_file f on doc.c_file_id=f.c_file_id "
            + "where ehcm_emp_perinfo_id = '" + employeeId + "'";
        sqlQuery += whereClause;
        sqlQuery += orderClause;
        sqlQuery += ")tb where tb.ehcm_emp_perinfo_id = '" + selDocId + "';";
        log4j.debug("selected:" + sqlQuery.toString());

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
      dcvo.setStatus(page + "_" + totalPage + "_" + totalRecord);
      log4j.debug("page" + page + "_" + totalPage + "_" + totalRecord);
      docvo.remove(0);
      docvo.add(dcvo);

      // Documents list
      sqlQuery = "SELECT doc.ehcm_documents_id as id,dt.name as doctype,(select eut_convert_to_hijri(to_char(doc.issueddate,'YYYY-MM-DD HH24:MI:SS'))) as issueddate,"
          + "(select eut_convert_to_hijri(to_char(doc.validdate,'YYYY-MM-DD HH24:MI:SS'))) as validdate,doc.isoriginal,f.name as docname,f.path"
          + " FROM ehcm_documents doc  join c_file f on doc.c_file_id=f.c_file_id "
          + "JOIN ehcm_emp_doctype dt on dt.ehcm_emp_doctype_id=doc.doctype "
          + "where ehcm_emp_perinfo_id = '" + employeeId + "'";
      sqlQuery += whereClause;
      sqlQuery += orderClause;
      sqlQuery += " limit " + rows + " offset " + offset;
      log4j.debug("documents:" + sqlQuery.toString());

      st = conn.prepareStatement(sqlQuery);
      rs = st.executeQuery();

      log4j.debug("Employeeid : " + employeeId);
      log4j.debug("Employee Info : " + st.toString());

      while (rs.next()) {
        dcvo = new DocumentsVO();
        dcvo.setDoctype(Utility.nullToEmpty(rs.getString("doctype")));
        dcvo.setIssueddate(Utility.nullToEmpty(rs.getString("issueddate")));
        dcvo.setValiddate(Utility.nullToEmpty(rs.getString("validdate")));
        dcvo.setIsoriginal(Utility.nullToEmpty(rs.getString("isoriginal")));
        dcvo.setFilename(Utility.nullToEmpty(rs.getString("docname")));
        dcvo.setPath(Utility.nullToEmpty(rs.getString("path")));
        dcvo.setId(Utility.nullToEmpty(rs.getString("id")));
        docvo.add(dcvo);
        log4j.debug(dcvo.getDoctype());
        log4j.debug("doctype:" + rs.getString("doctype"));
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
    return docvo;
  }

  /**
   * 
   * @param employeeId
   * @param documentId
   * @return documents value
   */

  public DocumentsVO getDocumentEditList(String employeeId, String documentId) {

    // TODO Auto-generated method stub
    PreparedStatement st = null;
    ResultSet rs = null;
    DocumentsVO docvo = null;
    try {
      String docqry = "SELECT doc.doctype as doctype,(select eut_convert_to_hijri(to_char(doc.issueddate,'YYYY-MM-DD HH24:MI:SS'))) as issueddate,"
          + "(select eut_convert_to_hijri(to_char(doc.validdate,'YYYY-MM-DD HH24:MI:SS'))) as validdate,doc.isoriginal,f.name as docname,f.path"
          + " FROM ehcm_documents doc  join c_file f on doc.c_file_id=f.c_file_id "
          + "where ehcm_emp_perinfo_id = '" + employeeId + "'";
      if (documentId != "" || documentId != null || documentId != "null") {
        docqry = docqry + " and ehcm_documents_id = '" + documentId + "'";
      }
      st = conn.prepareStatement(docqry);

      // st.setString(0, employeeId);
      log4j.debug("Employeeid : " + employeeId);

      log4j.debug("Employee Info : " + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        docvo = new DocumentsVO();
        docvo.setDoctype(Utility.nullToEmpty(rs.getString("docType")));
        docvo.setIssueddate(Utility.nullToEmpty(rs.getString("issueddate")));
        docvo.setValiddate(Utility.nullToEmpty(rs.getString("validdate")));
        docvo.setIsoriginal(Utility.nullToEmpty(rs.getString("isoriginal")));
        docvo.setFilename(Utility.nullToEmpty(rs.getString("docname")));
        docvo.setPath(Utility.nullToEmpty(rs.getString("path")));
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
    return docvo;
  }

  public boolean deleteDocument(String docId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String realPath = "", cFileId = "";
    try {
      st = conn.prepareStatement(
          "SELECT f.path as path,f.c_file_id as c_file_id FROM ehcm_documents doc JOIN c_file f ON f.c_file_id=doc.c_file_id WHERE ehcm_documents_id='"
              + docId + "'");
      rs = st.executeQuery();
      if (rs.next()) {
        realPath = rs.getString("path");
        cFileId = rs.getString("c_file_id");
      }

      File tmp = new File(realPath);
      if (tmp.isDirectory())
        FileUtils.forceDelete(tmp);

      st = conn.prepareStatement("DELETE FROM ehcm_documents WHERE ehcm_documents_id = ?");
      st.setString(1, docId);
      log4j.debug("delete query:" + st.toString());
      st.executeUpdate();

      st = conn.prepareStatement("DELETE FROM c_file WHERE c_file_id = ?");
      st.setString(1, cFileId);
      log4j.debug("delete query:" + st.toString());
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

  public boolean checkValidData(String docTypeId, Date issuedDate, Date validDate) {
    OBQuery<EhcmDocuments> docList = null;
    try {
      docList = OBDal.getInstance().createQuery(EhcmDocuments.class,
          "as e where e.doctype=:docId and ((e.issueddate >=:issued and e.validdate <=:valid) "
              + "or (e.issueddate <=:valid and e.validdate >=:issued)"
              + "or (e.issueddate >=:issued and e.validdate >=:issued))");
      docList.setNamedParameter("docId", docTypeId);
      docList.setNamedParameter("issued", issuedDate);
      docList.setNamedParameter("valid", validDate);
      log4j.debug(docList.list().size());
      if (docList.list().size() > 0) {
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.debug("Exception in checkValidData :" + e);
    }
    return false;
  }

  public static String getDocumentType(String docTypeId) {
    OBQuery<EhcmEmpDocType> docList = null;
    String returndata = null;
    try {
      docList = OBDal.getInstance().createQuery(EhcmEmpDocType.class,
          " as e where e.id='" + docTypeId + "'");
      docList.setMaxResult(1);
      log4j.debug(docList);
      returndata = docList.list().get(0).getValidationCode() + "-"
          + docList.list().get(0).getName();
    } catch (Exception e) {
      e.printStackTrace();
      log4j.debug("Exception getDocumentTypeList :" + e);
    }
    return returndata;
  }

  @SuppressWarnings("resource")
  public synchronized static JSONObject getDocType(String clientId, String searchTerm,
      int pagelimit, int page, String roleId, String orgId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();
      countQuery.append(" select count(distinct doc.ehcm_emp_doctype_id) as count ");
      selectQuery.append(
          " select distinct doc.code as docCode , doc.name as docName, doc.ehcm_emp_doctype_id as docId ");
      fromQuery.append(" from ehcm_emp_doctype doc" + " where doc.ad_org_id in ("
          + Utility.getChildOrg(clientId, orgId)
          + ") and doc.ad_org_id in( select ad_org_id from ad_role_orgaccess where ad_role_id = ? and ad_client_id = ? ) "
          + "and doc.ad_client_id=?");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and ( doc.code ilike '%" + searchTerm.toLowerCase() + "%' )");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());

      st.setString(1, roleId);
      st.setString(2, clientId);
      st.setString(3, clientId);
      log4j.debug(st);

      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords - 1);

      if (totalRecords > 0) {
        fromQuery.append(" order by doc.code limit ? offset ? ");
        st = conn.prepareStatement((selectQuery.append(fromQuery)).toString());
        st.setString(1, roleId);
        st.setString(2, clientId);
        st.setString(3, clientId);
        st.setInt(4, pagelimit);
        st.setInt(5, (page - 1) * pagelimit);
      }
      log4j.debug(st);
      rs = st.executeQuery();

      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("docId"));
        String combineData = rs.getString("docCode") + "-" + rs.getString("docName");
        jsonData.put("recordIdentifier", combineData);
        jsonArray.put(jsonData);
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      e.printStackTrace();
      return jsob;
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();
      } catch (Exception e) {

      }
    }
    return jsob;
  }

  public List<DocumentsVO> getDocTypeList(String ClientId, String OrgId, String id) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<DocumentsVO> ls = new ArrayList<DocumentsVO>();
    try {
      String qry = "select code,name,ehcm_emp_doctype_id,isactive from ehcm_emp_doctype where ad_client_id=?  order by code";
      // log4j.debug("id:" + id);
      /*
       * if (id == null || id.equals("") || id.equals("null")) { qry += "order by name"; } else {
       * qry += "and ehcm_documents_id=? order by name"; }
       */
      st = conn.prepareStatement(qry);
      st.setString(1, ClientId);
      // st.setString(2, OrgId);
      /*
       * if (!(id == null)) st.setString(3, id);
       */

      log4j.debug("doctype:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        DocumentsVO dVO = new DocumentsVO();
        dVO.setCode(Utility.nullToEmpty(rs.getString("code")));
        dVO.setName(Utility.nullToEmpty(rs.getString("name")));
        dVO.setDoctypact(Utility.nullToEmpty(rs.getString("isactive")));
        dVO.setDoctypId(Utility.nullToEmpty(rs.getString("ehcm_emp_doctype_id")));

        ls.add(dVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getDocType", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in getDocType", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getDocType", e);
        return ls;
      }
    }
    return ls;
  }

  public List<DocumentsVO> getUsersList(int page, int limit, String sortColType, String sortColName,
      String clientId, DocumentsVO searchVO, String searchFlag) {
    List<DocumentsVO> usersList = null;
    DocumentsVO userVO = null;
    SQLQuery usrQuery = null;
    StringBuffer query = null;
    int count = 0, totalPages = 0, start = 0;
    try {
      OBContext.setAdminMode(true);
      query = new StringBuffer();

      query.append(
          "select value||'-'||arabicname||' '||arabicfatname||' '||arbgrafaname as employee, ehcm_emp_perinfo_id, email from ehcm_emp_perinfo where ad_client_id='"
              + clientId
              + "' and status='I' and value not in (select value from ehcm_emp_perinfo where status='C') ");

      if (searchFlag.equals("true")) {
        if (searchVO.getUserName() != null && !searchVO.getUserName().equals(""))
          query.append(" and (value ilike '%" + searchVO.getUserName() + "%' or arabicname ilike '%"
              + searchVO.getUserName() + "%' or arabicfatname ilike '%" + searchVO.getUserName()
              + "%' or arbgrafaname ilike '%" + searchVO.getUserName() + "%') ");
        if (searchVO.getEmailId() != null && !searchVO.getEmailId().equals(""))
          query.append(" and email ilike '%" + searchVO.getEmailId() + "%' ");
      }

      if (sortColName != null)
        query.append(" order by " + sortColName + " " + sortColType);
      log4j.debug("getUsersList->" + query.toString());
      usrQuery = OBDal.getInstance().getSession().createSQLQuery(query.toString());

      count = usrQuery.list().size();
      if (count > 0) {
        totalPages = (int) (count) / limit;

        if ((int) (count) % limit > 0)
          totalPages = totalPages + 1;
        start = (page - 1) * limit;

        if (page > totalPages) {
          page = totalPages;
          start = (page - 1) * limit;
        }
      } else {
        totalPages = 0;
        page = 0;
      }
      usrQuery.setFirstResult(start);
      usrQuery.setMaxResults(limit);

      if (usrQuery != null) {
        usersList = new ArrayList<DocumentsVO>();
        if (usrQuery.list().size() > 0) {
          for (Iterator iterator = usrQuery.list().listIterator(); iterator.hasNext();) {
            Object[] objects = (Object[]) iterator.next();
            userVO = new DocumentsVO();
            userVO.setPage(page);
            userVO.setTotalPages(totalPages);
            userVO.setCount(count);

            userVO.setUserName(objects[0] == null ? "" : objects[0].toString());
            userVO.setUserId(objects[1] == null ? "" : objects[1].toString());
            userVO.setEmailId(objects[2] == null ? "-" : objects[2].toString());
            usersList.add(userVO);
          }
        }
      }
    } catch (Exception e) {
      log4j.error("Exception in recls ", e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return usersList;
  }

  /*
   * Sends the email to users
   */
  private static final String EMAIL_FROM = "EmailFrom";
  private static final String EMAIL_TO = "EmailTo";
  private static final String EMAIL_BCC = "Bcc";
  private static final String EMAIL_SUBJECT = "Subject";
  private static final String EMAIL_BODY = "Body";
  private static final String EMAIL_ATTACHMENTS = "EmailAttachments";

  private static final String SMTP_SERVER = "SmtpServer";
  private static final String SMTP_SERVERACCOUNT = "SmtpServerAccount";
  private static final String SMTP_PASSWORD = "SmtpServerPassword";
  private static final String SMTP_AUTHORIZATION = "isSmtpAuthorization";
  private static final String SMTP_CONNECTIONSECURITY = "SmtpConnectionSecurity";
  private static final String SMTP_PORT = "SmtpPorst";

  @SuppressWarnings({ "resource", "null" })
  public DocumentsVO startEmailProcess(String clientId, String userId, String documentId,
      String inpToUsers, String employeeId, String inpDocs) throws ServletException {
    int email = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;
    DocumentsVO msg = new DocumentsVO();
    msg.setMessage("");
    msg.setResult("");
    String emailFrom = "", mailSubject = "", empName = "", generatedDate = "";
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    DateFormat dateFrmt = new SimpleDateFormat(dateFormat);
    Date date = new Date();
    generatedDate = dateFrmt.format(date);
    final String loggedInUser = "select name from ad_user where ad_user_id=?";
    final String smtpServerInfo = "select "
        + "smtpserver,smtpserveraccount,smtpserverpassword,issmtpauthorization,smtpconnectionsecurity,smtpport "
        + "from c_poc_configuration "
        + "where ad_client_id=? and isactive='Y' order by created desc limit 1 ";
    final String subjectQry = "select decisionno||'-'||name||' '||'HR documents' as mailsubject, name as empname from ehcm_emp_perinfo where ehcm_emp_perinfo_id=? ";
    final String docQry = "select fl.path||'/'||fl.name as doc from ehcm_documents doc left join c_file fl on fl.c_file_id=doc.c_file_id where ehcm_documents_id=?";

    HashMap<String, String> smtpDetails = null; // to hold smtp server details
    List<HashMap<String, String>> messages = null; // to hold the messages
    HashMap<String, String> message = null; // to hold message details
    String attachments = "";

    try {
      smtpDetails = new HashMap<String, String>();
      ps = conn.prepareStatement(smtpServerInfo);
      ps.setString(1, clientId);
      rs = ps.executeQuery();
      if (rs.next()) {
        String isAuthorization = (rs.getString("issmtpauthorization") == null ? "false" : "true");

        smtpDetails.put(SMTP_SERVER, rs.getString("smtpserver"));
        smtpDetails.put(SMTP_SERVERACCOUNT, rs.getString("smtpserveraccount"));
        smtpDetails.put(SMTP_PASSWORD, rs.getString("smtpserverpassword"));
        smtpDetails.put(SMTP_AUTHORIZATION, isAuthorization);
        smtpDetails.put(SMTP_CONNECTIONSECURITY, rs.getString("smtpconnectionsecurity"));
        smtpDetails.put(SMTP_PORT, rs.getString("smtpport"));
        emailFrom = rs.getString("smtpserveraccount");
      } else {
        log4j.debug("Smtp Server Details are not found.");
        msg.setMessage("Smtp Server Details are not found.");
        msg.setResult("0");
        throw new OBException("Smtp Server Details are not found.");
      }

      log4j.debug("Server:" + smtpDetails.get(SMTP_SERVER) + ",Auth:"
          + Boolean.valueOf(smtpDetails.get(SMTP_AUTHORIZATION)) + ",Account:"
          + smtpDetails.get(SMTP_SERVERACCOUNT) + ",Security:"
          + smtpDetails.get(SMTP_CONNECTIONSECURITY) + ",Port:"
          + Integer.parseInt(smtpDetails.get(SMTP_PORT)));
      try {
        messages = new ArrayList<HashMap<String, String>>();

        ps = conn.prepareStatement(subjectQry);
        ps.setString(1, employeeId);
        rs = ps.executeQuery();
        if (rs.next()) {
          mailSubject = rs.getString("mailsubject");
          empName = rs.getString("empname");
        }
        ps = conn.prepareStatement(loggedInUser);
        ps.setString(1, userId);
        rs = ps.executeQuery();
        if (rs.next()) {
          empName = rs.getString("name");
        }
        if (inpDocs != null && !inpDocs.equals("")) {
          JSONObject json = new JSONObject(inpDocs);
          JSONArray arr = json.getJSONArray("Documents");

          for (int i = 0; i < arr.length(); i++) {
            JSONObject jsob = arr.getJSONObject(i);
            ps = conn.prepareStatement(docQry);
            ps.setString(1, jsob.getString("DocId"));
            rs = ps.executeQuery();
            if (rs.next()) {
              attachments += "," + rs.getString("doc");
            }
          }
        }
        attachments = attachments.replaceFirst(",", "");
        if (inpToUsers != null && !inpToUsers.equals("")) {
          JSONObject json = new JSONObject(inpToUsers);
          JSONArray arr = json.getJSONArray("List");

          StringBuffer mailContent = new StringBuffer("Hi,");
          mailContent.append("\r\n \r\n");
          mailContent.append("PFA Documents.");
          mailContent.append("\r\n \r\n");

          for (int i = 0; i < arr.length(); i++) {
            JSONObject jsob = arr.getJSONObject(i);
            message = new HashMap<String, String>();
            message.put(EMAIL_FROM, emailFrom);
            message.put(EMAIL_TO, jsob.getString("EmailId"));
            message.put(EMAIL_BCC, "");
            message.put(EMAIL_SUBJECT, mailSubject);
            message.put(EMAIL_BODY,
                mailContent + "Email is generated by " + empName + " on " + generatedDate + "");
            message.put(EMAIL_ATTACHMENTS, attachments);
            messages.add(message);
            log4j.debug("Sender->" + emailFrom + "/receiver->" + jsob.getString("EmailId"));
            log4j.debug("message size->" + messages.size());
            if (messages.size() == 100) {
              try {
                email = sendEmails(messages, smtpDetails);
                if (email == 1) {
                  msg.setMessage("Email(s) sent successfully");
                  msg.setResult("1");
                } else {
                  msg.setMessage("Problems while sending the email");
                  msg.setResult("0");
                }
                messages.clear();
              } catch (Exception e) {
                msg.setMessage("Problems while sending the email");
                msg.setResult("0");
              }
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (messages.size() > 0) {
        try {
          email = sendEmails(messages, smtpDetails);
          if (email == 1) {
            msg.setMessage("Email(s) sent successfully");
            msg.setResult("1");
          } else {
            msg.setMessage("Problems while sending the email");
            msg.setResult("0");
          }
          messages.clear();
        } catch (Exception e) {
          msg.setMessage("Problems while sending the email");
          msg.setResult("0");
        }
      }
      log4j.debug("msg->" + msg.getMessage());
    } catch (SQLException e) {
      log4j.error("Exception while retreving the email," + e);
      msg.setMessage("Exception while retreving the email");
      msg.setResult("0");
    } finally {
      try {
        if (ps != null)
          ps.close();
      } catch (SQLException e) {
        log4j.error("Exception while closing the prepared statement," + e);
        msg.setMessage("Exception while retreving the email");
        msg.setResult("0");
      }
    }
    return msg;
  }

  private int sendEmails(List<HashMap<String, String>> messages,
      HashMap<String, String> smtpDetails) throws ServletException {
    List<File> attachments = null;
    int email = 1;
    for (HashMap<String, String> message : messages) {
      String attachmentLocation = message.get(EMAIL_ATTACHMENTS);
      attachmentLocation = (attachmentLocation == null) ? "" : attachmentLocation;
      if (!attachmentLocation.equals("")) {
        attachments = new ArrayList<File>();
        String[] fileLocations = attachmentLocation.split(",");

        for (String location : fileLocations) {
          File file = new File(location);
          if (file.exists())
            attachments.add(file);
        }
      }

      try {
        EmailManager.sendEmail(smtpDetails.get(SMTP_SERVER),
            Boolean.valueOf(smtpDetails.get(SMTP_AUTHORIZATION)),
            smtpDetails.get(SMTP_SERVERACCOUNT),
            CryptoUtility.decrypt(smtpDetails.get(SMTP_PASSWORD)),
            smtpDetails.get(SMTP_CONNECTIONSECURITY), Integer.parseInt(smtpDetails.get(SMTP_PORT)),
            message.get(EMAIL_FROM), message.get(EMAIL_TO), null, message.get(EMAIL_BCC), null,
            message.get(EMAIL_SUBJECT), message.get(EMAIL_BODY), null, attachments, new Date(),
            null);
      } catch (Exception e) {
        email = 0;
        log4j.error("Exception while sending email:" + e);
        final String exceptionClass = e.getClass().toString().replace("class ", "");
        String exceptionString = "Problems while sending the email" + e;
        exceptionString = exceptionString.replace(exceptionClass, "");
        throw new ServletException(exceptionString);
      }
    }
    return email;
  }
}
