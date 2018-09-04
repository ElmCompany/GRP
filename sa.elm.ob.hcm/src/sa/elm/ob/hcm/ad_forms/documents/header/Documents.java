package sa.elm.ob.hcm.ad_forms.documents.header;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.EhcmDocuments;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ad_forms.documents.dao.DocumentsDAO;
import sa.elm.ob.hcm.ad_forms.documents.vo.DocumentsVO;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class Documents extends HttpSecureAppServlet {

  /**
   * dependents related to employee details
   */
  public static final String TABLE_EHCM_EMP_DOCUMENT = "4B83D01D9E49404E96A30DBE2DC0736F";
  private static final String TMP_DIR_PATH = "/tmp";
  private static final long serialVersionUID = 1L;
  private File tmpDir = null, destinationDir = null;
  File attachmentFile = null;

  public Documents() {
    super();
  }

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;

    tmpDir = new File(TMP_DIR_PATH);
    if (!tmpDir.isDirectory()) {
      new File(TMP_DIR_PATH).mkdir();
    }
  }

  public String getServletInfo() {
    return "Employee Document Attachment";
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }

  @SuppressWarnings({ "rawtypes", "unused" })
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    log4j.debug("action:" + request.getParameter("inpAction"));
    log4j.debug("submittype:" + request.getParameter("SubmitType"));
    log4j.debug("inpNextTab:" + request.getParameter("inpNextTab"));

    RequestDispatcher dispatch = null;

    String action = (request.getParameter("inpAction") == null ? ""
        : request.getParameter("inpAction"));
    String submittype = (request.getParameter("SubmitType") == null ? ""
        : request.getParameter("SubmitType"));
    String employeeId = request.getParameter("inpEmployeeId") == null ? ""
        : request.getParameter("inpEmployeeId");
    String nextTab = request.getParameter("inpNextTab") == null ? ""
        : request.getParameter("inpNextTab");
    String inpempstatus = (request.getParameter("inpEmpStatus") == null ? ""
        : request.getParameter("inpEmpStatus"));
    log4j.debug("inpempstatus" + inpempstatus);
    String inpEmployeeStatus = (request.getParameter("inpEmployeeStatus") == null ? ""
        : request.getParameter("inpEmployeeStatus"));
    log4j.debug("inpEmployeeStatus" + inpEmployeeStatus);
    /*
     * String inpName1 = request.getParameter("inpName1"); log4j.debug("inpname" + inpName1);
     */

    String attachmentId = "";
    String attachmentType = request.getParameter("inpDocType");

    try {
      OBContext.setAdminMode();
      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      String date = df.format(new Date());
      date = dateYearFormat.format(df.parse(date));
      date = UtilityDAO.convertTohijriDate(date);

      EmployeeDAO empdao = null;
      DocumentsDAO dao = null;
      DocumentsVO docvo = null;
      EhcmDocuments doc = null;

      String documentId = (request.getParameter("inpDocumentId") == null ? ""
          : request.getParameter("inpDocumentId"));

      Connection con = getConnection();
      empdao = new EmployeeDAO(con);
      dao = new DocumentsDAO(con);

      if (submittype.equals("Save") || submittype.equals("SaveNew")
          || submittype.equals("SaveGrid")) {
        request.setAttribute("today", date);
        String filename = request.getParameter("inpFileName");
        attachmentType = request.getParameter("inpDocType");

        if (documentId == null || documentId.equals("") || documentId.equals("null")) {
          doc = OBProvider.getInstance().get(EhcmDocuments.class);
          documentId = SequenceIdData.getUUID();
          doc.setId(documentId);
        } else {
          doc = OBDal.getInstance().get(EhcmDocuments.class, documentId);
        }
        documentId = doc.getId();

        DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();

        fileItemFactory.setSizeThreshold(1 * 124 * 124); // 1 MB
        fileItemFactory.setRepository(tmpDir);
        ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
        HashMap<File, FileItem> fileList = new HashMap<File, FileItem>();

        try {
          List items = uploadHandler.parseRequest(request);
          Iterator itr = items.iterator();

          String realPath = "";
          realPath = globalParameters.strFTPDirectory + "/EmpDocuments/" + TABLE_EHCM_EMP_DOCUMENT
              + "_" + documentId;
          destinationDir = new File(realPath);

          itr = items.iterator();
          while (itr.hasNext()) {
            FileItem item = (FileItem) itr.next();
            log4j.debug("file:" + item.getName());
            log4j.debug("formfiles:" + item.isFormField());
            if (!item.isFormField() && !item.getName().equals(null) && !item.getName().equals("")
                && !item.getName().equals("null")) {
              File tmp = new File(realPath);
              if (tmp.isDirectory())
                FileUtils.forceDelete(tmp);

              if (!destinationDir.isDirectory()) {
                new File(realPath).mkdirs();
              }
            }

            if (!item.isFormField()) {
              if ("".equals(item.getName().trim()))
                continue;
              attachmentFile = new File(destinationDir, item.getName());
              String name = item.getName();

              if (attachmentFile.exists()) {
                attachmentFile.delete();
              }
              item.write(attachmentFile);

              fileList.put(attachmentFile, item);
            }
          }

          VariablesSecureApp vars = new VariablesSecureApp(request);

          if (documentId == null || documentId.equals("") || documentId.equals("null")) {
            doc.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
            doc.setOrganization(OBDal.getInstance().get(Organization.class, vars.getOrg()));
            doc.setCreationDate(new java.util.Date());
            doc.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          }

          doc.setUpdated(new java.util.Date());
          doc.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));

          EhcmEmpPerInfo perinfo = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          doc.setEhcmEmpPerinfo(perinfo);
          doc.setIsoriginal(request.getParameter("inpIsOriginal").toString());
          if (request.getParameter("inpDocType") != null
              && request.getParameter("inpDocType") != "")
            doc.setDoctype(request.getParameter("inpDocType").toString());
          if (request.getParameter("inpIssuedDate") != null
              && request.getParameter("inpIssuedDate") != "")
            doc.setIssueddate(
                empdao.convertGregorian(request.getParameter("inpIssuedDate").toString()));
          if (request.getParameter("inpValidDate") != null
              && request.getParameter("inpValidDate") != "")
            doc.setValiddate(
                empdao.convertGregorian(request.getParameter("inpValidDate").toString()));
          else
            doc.setValiddate(null);
          // doc.setFile(attachmentId);
          log4j.debug("isOriginal:" + request.getParameter("inpIsOriginal").toString());

          doc.setNewOBObject(true);
          OBDal.getInstance().save(doc);
          OBDal.getInstance().flush();
          int seq = 10;

          Attachment attach = OBProvider.getInstance().get(Attachment.class);
          attach.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
          attach.setOrganization(OBDal.getInstance().get(Organization.class, vars.getOrg()));
          attach.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          attach.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          attach.setName(filename);
          attach.setSequenceNumber((long) seq);
          attach.setTable(OBDal.getInstance().get(Table.class, TABLE_EHCM_EMP_DOCUMENT));
          attach.setRecord(documentId);
          attach.setPath(realPath);
          OBDal.getInstance().save(attach);
          OBDal.getInstance().flush();

          attachmentId = attach.getId();

          doc.setFile(attach);
          // log4j.debug("fileid:" + attach.getId());
          // log4j.debug("docattachid:" + doc.getFile().getId());

          OBDal.getInstance().save(doc);
          OBDal.getInstance().flush();

        } catch (final Exception e) {
          log4j.error("Exception in Employee Documents :", e);
        }

        OBDal.getInstance().commitAndClose();

        documentId = doc.getId();
        request.setAttribute("inpDocumentId", documentId);
        request.setAttribute("savemsg", "Success");

        if (nextTab.equals("") || nextTab.equals("null")) {
          if (submittype.equals("SaveNew")) {
            documentId = "";
            request.setAttribute("inpDocumentId", "");
            action = "EditView";
          } else if (submittype.equals("Save")) {
            request.setAttribute("inpdocumentId", (documentId == null ? "" : documentId));
            action = "EditView";
          } else if (submittype.equals("SaveGrid")) {
            documentId = "";
            action = "GridView";
          }
        } else {
          ServletContext context = this.getServletContext();
          if (!nextTab.equals("") && !nextTab.equals("DOC") && !nextTab.equals("null")) {
            log4j.debug("nextTab:" + nextTab);
            String redirectStr = empdao.redirectStr(nextTab, employeeId, inpempstatus,
                inpEmployeeStatus);
            response.sendRedirect(context.getContextPath() + redirectStr);
          }
        }
      } else if ((submittype.equals("email")) && (!submittype.equals(""))) {
        VariablesSecureApp vars = new VariablesSecureApp(request);

        String inpToUsers = request.getParameter("inpUsers");
        String inpDocs = request.getParameter("inpDocs");
        DocumentsVO msg = null;
        try {
          msg = dao.startEmailProcess(vars.getClient(), vars.getUser(), documentId, inpToUsers,
              employeeId, inpDocs);
        } catch (Exception e) {
          log4j.error("Exception while sending email:" + e);
          request.setAttribute("EmailMsg", "Exception while sending email");
          request.setAttribute("EmailResult", "0");
        }
        request.setAttribute("EmailMsg", msg.getMessage());
        request.setAttribute("EmailResult", msg.getResult());
      }

      if (action.equals("EditView")) {
        VariablesSecureApp vars = new VariablesSecureApp(request);

        request.setAttribute("today", date);
        request.setAttribute("CancelHiring",
            empdao.checkEmploymentStatusCancel(vars.getClient(), employeeId));
        if (!(documentId.equals(null)) && !(documentId.equals(""))
            && !(documentId.equals("null"))) {
          log4j.debug("ifenter");
          docvo = dao.getDocumentEditList(employeeId, documentId);
          request.setAttribute("inpDocumentId", documentId);
          request.setAttribute("inpEmployeeId", employeeId);
          request.setAttribute("inpDocType", Utility.nullToEmpty(docvo.getDoctype()));
          request.setAttribute("inpDocTypeName", DocumentsDAO.getDocumentType(docvo.getDoctype()));
          request.setAttribute("inpIssuedDate", Utility.nullToEmpty(docvo.getIssueddate()));
          request.setAttribute("inpValidDate", Utility.nullToEmpty(docvo.getValiddate()));
          log4j.debug("inpIsOriginal" + docvo.getIsoriginal());
          request.setAttribute("inpIsOriginal", Utility.nullToEmpty(docvo.getIsoriginal()));
          request.setAttribute("inpFileName", Utility.nullToEmpty(docvo.getFilename()));
          request.setAttribute("inpPath", Utility.nullToEmpty(docvo.getPath()));
          // request.setAttribute("inpDocTypeLs", DocumentsDAO.getDocType(vars.getClient(),
          // request.getParameter("searchTerm"), 20, 1, vars.getRole(), vars.getOrg()));
          request.setAttribute("inpDocTypeLs",
              dao.getDocTypeList(vars.getClient(), vars.getOrg(), null));

          // employee details
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
          if (objEmployee.getGradeClass() != null) {
            if (objEmployee.getGradeClass().isContract()) {
              request.setAttribute("inpempCategory", "Y");
            } else {
              request.setAttribute("inpempCategory", "");
            }
          } else {
            request.setAttribute("inpempCategory", "");
          }
          request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
          request.setAttribute("inpName1", objEmployee.getArabicfullname());
          request.setAttribute("inpName2",
              objEmployee.getName().concat(" ").concat(objEmployee.getFathername()).concat(" ")
                  .concat(objEmployee.getGrandfathername()));
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else
            request.setAttribute("inpAddressId", empdao.getEmployeeAddressId(employeeId));
          if (request.getParameter("inpEmpStatus") != null) {
            request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
          }
          if (request.getParameter("inpEmployeeStatus") != null) {
            request.setAttribute("inpEmployeeStatus",
                request.getParameter("inpEmployeeStatus").toString());
          }
          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/documents/Documents.jsp");
        } else {
          log4j.debug("ELSE");
          request.setAttribute("today", date);
          request.setAttribute("inpDocType", "0");
          // request.setAttribute("inpDocTypeLs", DocumentsDAO.getDocType(vars.getClient(),
          // request.getParameter("searchTerm"), 20, 1, vars.getRole(), vars.getOrg()));
          request.setAttribute("inpDocTypeLs",
              dao.getDocTypeList(vars.getClient(), vars.getOrg(), null));
          request.setAttribute("inpEmployeeId", employeeId);
          request.setAttribute("inpIsOriginal", null);
          request.setAttribute("inpIssuedDate", date);
          request.setAttribute("inpValidDate", "");
          request.setAttribute("inpFileName", "");
          request.setAttribute("inpPath", "");

          // employee details
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
          request.setAttribute("inpDocumentId", "");
          if (objEmployee.getGradeClass() != null) {
            if (objEmployee.getGradeClass().isContract()) {
              request.setAttribute("inpempCategory", "Y");
            } else {
              request.setAttribute("inpempCategory", "");
            }
          } else {
            request.setAttribute("inpempCategory", "");
          }
          if (request.getParameter("inpEmpStatus") != null) {
            request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
          }
          if (request.getParameter("inpEmployeeStatus") != null) {
            request.setAttribute("inpEmployeeStatus",
                request.getParameter("inpEmployeeStatus").toString());
          }
          request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
          request.setAttribute("inpName1", objEmployee.getArabicfullname());
          request.setAttribute("inpName2",
              objEmployee.getName().concat(" ").concat(objEmployee.getFathername()).concat(" ")
                  .concat(objEmployee.getGrandfathername()));

          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals("")) {
            request.setAttribute("inpAddressId", null);
          } else
            request.setAttribute("inpAddressId", empdao.getEmployeeAddressId(employeeId));
          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/documents/Documents.jsp");
        }

      } else if (action.equals("") || action.equals("GridView")) {
        VariablesSecureApp vars = new VariablesSecureApp(request);
        ehcmempstatusv view = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
        EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class,
            view.getEhcmEmpPerinfo().getId());
        request.setAttribute("inpEmployeeId", employeeId);
        request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
        // EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
        action = "GridView";
        request.setAttribute("inpDocumentId", "");
        if (objEmployee.getGradeClass() != null) {
          if (objEmployee.getGradeClass().isContract()) {
            request.setAttribute("inpempCategory", "Y");
          } else {
            request.setAttribute("inpempCategory", "");
          }
        } else {
          request.setAttribute("inpempCategory", "");
        }
        if (request.getParameter("inpEmpStatus") != null) {
          request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
        }
        if (request.getParameter("inpEmployeeStatus") != null) {
          request.setAttribute("inpEmployeeStatus",
              request.getParameter("inpEmployeeStatus").toString());
        }
        request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
        request.setAttribute("inpName1", objEmployee.getArabicfullname());
        request.setAttribute("inpName2", objEmployee.getName().concat(" ").concat(
            StringUtils.isNotEmpty(objEmployee.getFathername()) ? objEmployee.getFathername() : "")
            .concat(" ")
            .concat(StringUtils.isNotEmpty(objEmployee.getGrandfathername())
                ? objEmployee.getGrandfathername()
                : ""));
        if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
          request.setAttribute("inpAddressId", null);
        else
          request.setAttribute("inpAddressId", empdao.getEmployeeAddressId(employeeId));
        request.setAttribute("inpDocType",
            dao.getDocTypeList(vars.getClient(), vars.getOrg(), null));
        request.setAttribute("CancelHiring",
            empdao.checkEmploymentStatusCancel(vars.getClient(), employeeId));

        dispatch = request
            .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/documents/DocumentsList.jsp");
      }
    } catch (final Exception e) {
      dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
      log4j.error("Error in Document : ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
      OBContext.restorePreviousMode();
      try {
        if (dispatch != null) {
          response.setContentType("text/html; charset=UTF-8");
          response.setCharacterEncoding("UTF-8");
          dispatch.include(request, response);
        } else
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (final Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log4j.error("Error in Document : ", e);
      }
    }
  }
}
