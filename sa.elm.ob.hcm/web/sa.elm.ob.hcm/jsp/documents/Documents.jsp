<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="sa.elm.ob.utility.util.Utility"%>
<%@page import="java.util.List, java.util.ArrayList"%>
<%@page import="sa.elm.ob.hcm.ad_forms.documents.vo.DocumentsVO"%>
<%@page import="org.codehaus.jettison.json.JSONObject"%>
<%@page import="org.codehaus.jettison.json.JSONArray"%>
<%@ page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>
<%@page import="sa.elm.ob.utility.util.UtilityVO"%>

<%--   <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>  --%>
 
  
<%
     String lang = ((String)session.getAttribute("#AD_LANGUAGE"));
     /* List<DocumentsVO> doctype = (ArrayList<DocumentsVO>) request.getAttribute("inpDocTypeLs");  */
     String style="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css";
     String DialogBoxStyle="../web/js/common/CommonDialogFormLtr.css";
     String toolBarStyle="../web/js/common/CommonFormLtr.css";
     if(lang.equals("ar_SA"))
     {
       style="../web/skins/rtl/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP_250.css";
       toolBarStyle="../web/js/common/CommonFormRtl.css";
       DialogBoxStyle="../web/js/common/CommonDialogFormRtl.css";
     }
%>
<HTML xmlns="http://www.w3.org/1999/xhtml">
  <HEAD>
   <META http-equiv="Content-Type" content="text/html; charset=utf-8"></META> 
    <link rel="stylesheet" type="text/css" href="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css" id="paramCSS"></link>
    <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
   
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
         <link rel="stylesheet" type="text/css" href="../web/js/common/select2.min.css"></link>
    
    
    <link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
    <LINK rel="stylesheet" type="text/css" href="<%=toolBarStyle %>"></LINK>
    <LINK rel="stylesheet" type="text/css" href="<%=style %>" id="paramCSS"></LINK>
    <LINK rel="stylesheet" type="text/css" href="<%=DialogBoxStyle %>" ></LINK>
    
    <script type="text/javascript" src="../web/js/common/common.js"></script>
    <script type="text/javascript" src="../web/js/utils.js"></script>
    <script type="text/javascript" src="../web/js/windowKeyboard.js"></script>
    <script type="text/javascript" src="../web/js/searchs.js"></script>
    
    <script type="text/javascript" id="paramDirectory">var baseDirectory = "../web/";</script>
    <script type="text/javascript" id="paramLanguage">var defaultLang="en_US";</script>
    <script type="text/javascript" src="../web/js/default/DateTextBox.js"></script>
    <script type="text/javascript" src="../web/js/jscalendar/calendar.js"></script>
    <script type="text/javascript" src="../web/js/jscalendar/lang/calendar-lang.js"></script> 
   
    <script type="text/javascript" src="../web/js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.min_1.11.0.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.plugin.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura-ar.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.plus.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.ext.js"></script>
    <script type="text/javascript" src="../web/js/common/DateConverter.js"></script>
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/DateUtils.js"></script>
    
   
    <script type="text/javascript">
    var changesFlag=0;
    function validate(action) {return true;}
    function onLoadDo(){
        /*<<<<<OB Code>>>>>*/
        this.windowTables = new Array(new windowTableId('client', 'buttonOK'));
        setWindowTableParentElement();
        this.tabsTables = new Array(new tabTableId('tdtopTabs'));
        setTabTableParentElement();setBrowserAutoComplete(false);setFocusFirstControl(null, 'inpMailTemplate');
        resizeArea();updateMenuIcon('buttonMenu');onLoad();
        /*<<<<<OB Code>>>>>*/
        <%if(request.getAttribute("inpEmployeeIsActive").toString().equals("true")) {%>
        $('#inpIssuedDate').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect: function(dates) { enableForm(); } ,showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
       
         $('#inpValidDate').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect: function() {enableForm();}, showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger" >'});
         <% } %>
         
        /*  $('#inpEndDate').calendarsPicker({calendar:  
             $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect: function() {enableForm();}, showTrigger:  
         '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'}); */
    }
    function onResizeDo()
    {
        resizeArea();
        reSizeGrid();
    }
    function onClickRefresh() {
        document.getElementById("inpAction").value = "EditView";
        if (changesFlag == 1 && savevaliddata())
            
            OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
                if (result){
                    if (checkValidData()) {
                        document.getElementById("SubmitType").value="Save";
                        document.getElementById("inpAction").value = "EditView";
                        reloadTab(currentTab);
                    }
               }
               else {
                    document.getElementById("inpAction").value = "EditView";
                   reloadTab(currentTab);
               }
            }); 
   
        else
            reloadTab('DOC');  
    }
    function onClickGrid() {
        document.getElementById("inpAction").value = "GridView";
        
        if (changesFlag == 1 && savevaliddata())
            OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
                if (result)
                    reloadTabSave("DOC");
                else {
                    reloadTab("DOC");
                }
            });
        else
            reloadTab('DOC');  
    }
    function onLoad()
    {
        <% request.setAttribute("savemsg",request.getAttribute("savemsg")==null?"":request.getAttribute("savemsg")); %>
        <%  if(request.getAttribute("savemsg").equals("Success")) { %>
        displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.saveoreditsuccess", lang) %>");
        <%}%>
        //$(".isOriginal").val("Y")
        var isOriginal ="<%=request.getAttribute("inpIsOriginal")%>";
        if(isOriginal=='N'){
            $("input[name='inpIsOriginal'][value='N']").attr('checked','checked');
        }
        else if(isOriginal=='Y'){
            $("input[name='inpIsOriginal'][value='Y']").attr('checked','checked');
        }
            
        
        //alert($("#inpFileName").val());
        //$("#inpFile").hide();
        if(($("#inpFileName").val()==null || $("#inpFileName").val()=='') && ($("#inpPath").val()==null || $("#inpPath").val()=='')){
         $('#downloadimg').hide();
         //$("#filenam").text("No files Found");
        }
        else{
          $("#inpFile").hide();
          $("#filenam").text($("#inpFileName").val());
        }  
    }
    function enableSaveButton(flag) {
        if (flag == 'true' && document.getElementById('linkButtonSave_Relation').className != 'Main_ToolBar_Button' && document.getElementById('linkButtonSave_Relation').className != 'linkButtonSave_New' && document.getElementById('linkButtonSave_Relation').className != 'linkButtonSave') {
            document.getElementById('buttonSave_Relation').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_Relation';
            document.getElementById('buttonSave_New').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_New';
            document.getElementById('buttonSave').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save';
            $('#linkButtonSave_Relation, #linkButtonSave_New, #linkButtonSave').attr("class", "Main_ToolBar_Button");
        }
        else if (flag == 'false') {
            document.getElementById('buttonSave_Relation').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_Relation_disabled';
            document.getElementById('buttonSave_New').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_New_disabled';
            document.getElementById('buttonSave').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_disabled';
            $('#linkButtonSave_Relation, #linkButtonSave_New, #linkButtonSave').attr("class", "Main_ToolBar_Button_disabled");
        }
    }
    function enableForm() {
       changesFlag = 1;
/*         hideMessage();
 */         if(( document.getElementById('inpDocType').value !='0' ) &&   (document.getElementById('inpIssuedDate').value !="") && ( document.getElementById('inpIsOriginal').value !="" ) ){
              enableSaveButton("true");
         }
        else{
             enableSaveButton("false");
        } 
            
    }
    
    function savevaliddata(){
        var mandatory = '0';
        //validating mandatory fields 
        var Doctype = document.getElementById("inpDocType").value;
        var IssuedDate = document.getElementById("inpIssuedDate").value;
        var isOriginalorDuplicate = document.getElementById('inpIsOriginal').value;

        
        if(Doctype == '0' || IssuedDate == "" || isOriginalorDuplicate == ""){
            mandatory = '1';
        }
        if(mandatory =='1'){
            return false;
        }
        else{
            return true;
        }
  } 
  
    function upload(){
            $("#inpFile").click();
             if($('#inpEmployeeIsActive').val()=="true"){
                 enableForm();
             }
    }
    function namechange(){
         $("#inpFile").show();
         $("#filenam").hide();
         $("#inpFileName").val($('input[type=file]')[0].files[0].name);
         //text($('input[type=file]')[0].files[0].name);
         $("#inpFileType").val($('input[type=file]')[0].files[0].type);
         if($('#inpEmployeeIsActive').val()=="true"){
             enableForm();
         }
         $('#downloadimg').hide();
      }
    </script>
</HEAD>
<BODY onresize="onResizeDo();" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" id="paramBody">
    <FORM id="form" method="post" action="" name="frmMain" enctype='multipart/form-data'>
        <INPUT type="hidden" name="Command"></INPUT>
        <INPUT type="hidden" name="IsPopUpCall" value="1"></INPUT>
        <INPUT type="hidden" name="inpLastFieldChanged"></INPUT>
        <INPUT type="hidden" name="inpAction" id="inpAction" value=""></INPUT>
        <INPUT type="hidden" name="inpNextTab" id="inpNextTab" value="<%= request.getAttribute("inpNextTab") %>"></INPUT>
        <INPUT type="hidden" id="inpDocumentId" name="inpDocumentId" value="<%= request.getAttribute("inpDocumentId") %>"></INPUT>
        <INPUT type="hidden" id="inpEmployeeId" name="inpEmployeeId" value="<%= request.getAttribute("inpEmployeeId") %>"></INPUT>
        <INPUT type="hidden" name="inpempCategory" id="inpempCategory" value="<%= request.getAttribute("inpempCategory") %>"></INPUT>
        <INPUT type="hidden" name="inpEmploymentId" id="inpEmploymentId" value="<%= request.getAttribute("inpEmploymentId") %>"></INPUT>
        <INPUT type="hidden" name="inpAddressId" id="inpAddressId" value="<%=request.getAttribute("inpAddressId")%>"></INPUT>
         <INPUT type="hidden" name="inpEmpStatus" id="inpEmpStatus" value="<%= request.getAttribute("inpEmpStatus") %>"></INPUT>
        <INPUT type="hidden" id="inpEmployeeStatus" name="inpEmployeeStatus" value="<%= request.getAttribute("inpEmployeeStatus") %>"></INPUT>
        <INPUT type="hidden" id="SubmitType" name="SubmitType" value="<%= request.getAttribute("SubmitType") %>"></INPUT>
        <INPUT type="hidden" id="inpFileName" name="inpFileName" value="<%= request.getAttribute("inpFileName") %>"></INPUT>
        <INPUT type="hidden" name="inpAttachmentId" id=inpAttachmentId value="<%= request.getAttribute("inpAttachmentId")%>"></INPUT>
        <INPUT type="hidden" name="inpPath" id="inpPath" value="<%= Utility.escapeQuote(request.getAttribute("inpPath")) %>"></INPUT>
        <INPUT type="hidden" id="inpFileType" name="inpFileType" value=""></INPUT>
        <INPUT type="hidden" name="inpEmployeeIsActive" id="inpEmployeeIsActive" value="<%= request.getAttribute("inpEmployeeIsActive") %>"></INPUT>
        <div id="DownloadAttachmentDialog" style="display: none;">
         <iframe name="DownloadAttachmentFrame" id="DownloadAttachmentFrame" frameBorder="0" src="" width="100%" height="100%"></iframe>
        </div>
        <TABLE height="100%" border="0" cellpadding="0" cellspacing="0" id="main">
            <TR>
                <TD valign="top" id="tdleftTabs">
                    <table cellpadding="0" cellspacing="0" class="Main_ContentPane_LeftTabsBar" id="tdLeftTabsBars"></table>
                </TD>
                <TD valign="top">
                    <TABLE width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_Left">
                      <TR><TD class="Main_NavBar_bg_left" id="tdleftSeparator"></TD></TR>
                      <TR><TD class="Main_ToolBar_bg_left" valign="top"></TD></TR>
                      <TR><TD class="Main_Client_bg_left"></TD></TR>
                      <TR><TD class="Main_Bottom_bg_left"></TD></TR>
                    </TABLE>
                </TD>
                <TD valign="top">
                    <DIV class="Main_ContentPane_NavBar" id="tdtopButtons"><TABLE class="Main_ContentPane_NavBar" id="tdtopNavButtons"></TABLE></DIV>
                    <DIV class="Main_ContentPane_ToolBar" id="paramToolBar">
                        <table class="Main_ContentPane_ToolBar Main_ToolBar_bg" id="tdToolBar">
                            <tr>
                            <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                            <%-- <td width="2%" ><a href="javascript:void(0);" onClick="onClickNew();" class="Main_ToolBar_Button" onMouseOver="window.status='Create a New Record';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonNew"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.new",lang)%>" border="0" id="linkButtonNew"></a></td> 
                            <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td> --%>
                            <td width="2%" ><a href="javascript:void(0);" onClick="onClickGrid()" class="Main_ToolBar_Button" onMouseOver="window.status='Form View';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="linkButton_Relation"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Relation" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.gridview",lang) %>" border="0" id="linkButtonEdition"></a></td>
                            <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                            <td width="2%" ><a href="javascript:void(0);" onClick="onClickSave(this, 'Save');" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Save Record';return true;" onMouseOut="window.status='';return true;" id="linkButtonSave"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.save", lang)  %>" border="0" id="buttonSave"></a></td>
                            <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                            <td width="2%" ><a href="javascript:void(0);" onClick="onClickSave(this, 'New');" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Save and New';return true;" onMouseOut="window.status='';return true;" id="linkButtonSave_New"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_New_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.savenew", lang)  %>" border="0" id="buttonSave_New"></a></td>
                            <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                            <td width="2%" ><a href="javascript:void(0);" onClick="onClickSave(this, 'Grid');" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Save and Grid';return true;" onMouseOut="window.status='';return true;" id="linkButtonSave_Relation"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_Relation_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.savegrid", lang)  %>" border="0" id="buttonSave_Relation"></a></td>
                            <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                            <td width="2%" ><a href="javascript:void(0);" onClick="onClickRefresh()" class="Main_ToolBar_Button" onMouseOver="window.status='Reload Current Page';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonRefresh"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Refresh" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.reload",lang) %>" border="0" id="linkButtonRefresh"></a></td> 
                            <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                            <td class="Main_ToolBar_Space"></td>
                            </tr>
                        </table>
                    </DIV>
                    <TABLE width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_TabPane" id="tdtopTabs">
                          <TR id="paramParentTabContainer"><td class="tabBackGroundInit"></td></TR>
                          <TR id="paramMainTabContainer"><td class="tabBackGroundInit"><span style="background: none;" class="tabTitle"></span></td></TR>
                          <tr><td class="tabBackGround">
                            <div class="marginLeft">
                            <div><span class="dojoTabcurrentfirst"><div>
                                <span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%= Resource.getProperty("hcm.documents",lang)%></a></span>
                            </div></span></div>
                            </div>
                        </td></tr>
                    </TABLE>
                    <DIV class="Main_ContentPane_Client" style="overflow: auto;" id="client">
                        <TABLE class="dojoTabContainer" border="0" cellpadding="0" cellspacing="0" width="100%" summary="">
                            <TR><TD>
                                <TABLE class="dojoTabPaneWrapper" border="0" cellpadding="0" cellspacing="0" width="100%" summary="">
                                    <TR><TD>
                                        <TABLE cellpadding="0" cellspacing="0" id="messageBoxID" class="MessageBox">    
                                            <TBODY>
                                                <TR class="MessageBox_TopMargin">
                                                    <TD class="MessageBox_LeftMargin"></TD>
                                                    <TD></TD>
                                                    <TD class="MessageBox_RightMargin"></TD>
                                                </TR>
                                                <TR>
                                                    <TD class="MessageBox_LeftMargin"></TD>
                                                    <TD>
                                                        <TABLE cellpadding="0" cellspacing="0" class="MessageBox_Container">
                                                        <TBODY>
                                                            <TR>
                                                                <TD class="MessageBox_LeftTrans">
                                                                    <TABLE style="width: 100%; height: 100%" cellpadding="0" cellspacing="0">
                                                                        <TBODY>
                                                                            <TR><TD class="MessageBox_TopLeft"></TD></TR>
                                                                            <TR><TD class="MessageBox_Left"></TD></TR>
                                                                        </TBODY>
                                                                    </TABLE>
                                                                </TD>
                                                                <TD class="MessageBox_bg">
                                                                    <TABLE class="MessageBox_Top"><TBODY>
                                                                        <TR><TD>
                                                                            <SPAN>
                                                                                <TABLE cellpadding="0" cellspacing="0" class="MessageBox_Body_ContentCell">
                                                                                    <TBODY>
                                                                                        <TR>
                                                                                            <TD class="MessageBox_Icon_ContentCell">
                                                                                                <DIV class="MessageBox_Icon"></DIV>
                                                                                            </TD>
                                                                                            <TD style="vertical-align: top;" id="messageBoxIDContent">
                                                                                                <SPAN>
                                                                                                    <DIV class="MessageBox_TextTitle" id="messageBoxIDTitle"></DIV>
                                                                                                    <DIV class="MessageBox_TextDescription" id="messageBoxIDMessage"></DIV>
                                                                                                    <DIV class="MessageBox_TextSeparator"></DIV>
                                                                                                </SPAN>
                                                                                            </TD>
                                                                                        </TR>
                                                                                    </TBODY>
                                                                                </TABLE>
                                                                            </SPAN>
                                                                            
                                                                            <div id="hideMessage">
                                                                                <a style="color: yellow;font-size: 11.5px" href="javascript:void(0);" onClick="document.getElementById('messageBoxID').style.display = 'none';"><%=Resource.getProperty("hcm.hide",lang)%></a>
                                                                            </div>
                                                                     </TD></TR>
                                                                     </TBODY></TABLE>
                                                                </TD>
                                                                <TD class="MessageBox_RightTrans">
                                                                    <TABLE style="width: 100%; height: 100%" cellpadding="0" cellspacing="0">
                                                                        <TBODY>
                                                                            <TR><TD class="MessageBox_TopRight"></TD></TR>
                                                                            <TR><TD class="MessageBox_Right"></TD></TR>
                                                                        </TBODY>
                                                                    </TABLE>
                                                                </TD>
                                                            </TR>
                                                            <TR>
                                                                <TD rowspan="2" class="MessageBox_BottomLeft"></TD>
                                                                <TD class="MessageBox_BottomTrans MessageBox_bg"></TD>
                                                                <TD rowspan="2" class="MessageBox_BottomRight"></TD>
                                                            </TR>
                                                            <TR>
                                                                <TD class="MessageBox_Bottom"></TD>
                                                            </TR>
                                                        </TBODY>
                                                        </TABLE>
                                                    </TD>
                                                    <TD class="MessageBox_RightMargin"></TD>
                                                </TR>
                                                <TR class="MessageBox_BottomMargin">
                                                    <TD class="MessageBox_LeftMargin"></TD>
                                                    <TD></TD>
                                                    <TD class="MessageBox_RightMargin">
                                                    </TD>
                                                </TR>
                                            </TBODY>
                                        </TABLE>
                                        <div align="center">
                                             <table>
                                                <tbody>
                                                     <tr id="TabList">
                                                        <td>
                                                            <div id="TabEmployee" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMP');">
                                                                <span class="LabelText"><%= Resource.getProperty("hcm.employee",lang)%>
                                                                </span>
                                                            </div>
                                                            <div style="text-align: center;display:none;">
                                                                <img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img>
                                                            </div>
                                                            </td>
                                        
                                                            <td>
                                                                <img  id="ImgEmpInfo" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img>
                                                            </td>
                                                            
                                                            <td>
                                                                <div id="TabEmpInfo"  class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';"  onclick="reloadWindow('EMPINF');"> 
                                                                    <span class="LabelText"><%= Resource.getProperty("hcm.empmntinfo",lang)%>
                                                                    </span>
                                                                </div>
                                                            </td>
                                                            <td><img id="ImgEmpAddress" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                            <td><div id="TabEmpAddress" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';"  onclick="reloadWindow('EMPADD');"><span class="LabelText"><%= Resource.getProperty("hcm.empaddress",lang)%></span></div></td>
                                                            <td><img  id="ImgqualInfo" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                            <td><div id="TabqualInfo"  class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPQUAL');"><span class="LabelText"><%= Resource.getProperty("hcm.empqualinfo",lang)%></span></div></td>
                                                            <td>
                                                                <img  id="ImgDependent" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img>
                                                            </td>
                                                            
                                                            <td>
                                                            <div id="TabDependent" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('Dependent');">
                                                                <span class="LabelText"><%= Resource.getProperty("hcm.dependents",lang)%>
                                                                </span>
                                                            </div>
                                                            
                                                            </td>
                                                            
                                                            <td><img  id="Imgpreemp" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                            <td>
                                                                <div id="Tabpreemp" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';"  onclick="reloadWindow('PREEMP');" >
                                                                <span class="LabelText"><%= Resource.getProperty("hcm.emppreemp",lang)%></span></div>
                                                            </td>
                                                            <% if(request.getAttribute("inpempCategory").toString().equals("Y")) { %>
                                                           <td><img id="ImgEmpContract" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                          <td><div id="TabEmpContract" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPCTRCT');" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                                                          <%}else{ %>
                                                            <td><img id="ImgEmpContract" class="imageNotSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                            <td><div id="TabEmpContract" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                                                            <%} %>
                                                             <td><img  id="ImgMedIns" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                             <td><div id="TabMedIns" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('MEDIN');"><span class="LabelText"><%= Resource.getProperty("hcm.MedicalInsurance",lang)%></span></div>
                                                             </td>
                                                            <td>
                                                                <img  id="ImgAsset" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img>
                                                            </td>
                                                            <td>
                                                                <div id="TabAsset" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';"  onclick="reloadWindow('Asset');" >
                                                                    <span class="LabelText"><%= Resource.getProperty("hcm.Facilities&Assets",lang)%></span>
                                                                </div>
                                                            </td>
                                                            <td><img  id="ImgDocument" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                            
                                                            <td>
                                                                <div id="TabDocument" class="tabCurrent">
                                                                    <span class="LabelText"><%= Resource.getProperty("hcm.documents",lang)%></span>
                                                                </div>
                                                                <div style="text-align: center;">
                                                                    <img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img>
                                                                </div>
                                                            </td>
                                                              <td><img  id="ImgEmpPerPayMethod" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                   <td>
                                     <div id="TabEmpPerPayMethod" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('PERPAYMETHOD');"><span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></span>
                                     </div>
                                    </td>
                                                        </tr>
                                                    </tr>
                                                </tbody>
                                             </table>
                                             <div style="margin-bottom: 15px;"></div>
                                        </div>
                                        
                                        <div id="LoadingContent" style="display:none;">
                                            <div style="text-align: center;margin-top: 17%;"><img alt="Loading" src="../web/images/loading.gif"></img></div>
                                            <div style="color:#000000;font-family: sans-serif;font-size:14px;margin-top:10px;text-align: center;"><%= Resource.getProperty("hcm.loading", lang)%>... </div>
                                        </div>
                                        
                                        <div style="width:100%;">
                                            <div style="padding: 0 1%; width: 98%;">
                                                <div style="width:100%;" id="FormDetails">
                                                  <div align="center">
                                                        <TABLE style="width:80%; margin-top: 10px;">
                                                            <TR>
                                                             <TD class="TextBox_ContentCell" >
                                                                 <input type="text" name="inpName1" id="inpName1" maxlength="59" readonly value="<%= Utility.escapeQuote(request.getAttribute("inpName1")) %>" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);" dir="rtl"></input>
                                                                 <input type="hidden" name="inpHidName1" id="inpHidName1" value="<%= Utility.escapeQuote(request.getAttribute("inpName1")) %>"></input>
                                                             </TD>
                                                             <TD class="TitleCell" style="min-width:80px;padding-left: 0px"><span class="LabelText" ><%= Resource.getProperty("hcm.arabicname", lang)%></span></TD>
                                                            
                                                             <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.fullname", lang)%></span></TD>
                                                             <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpName2" id="inpName2" maxlength="59" readonly value="<%= Utility.escapeQuote(request.getAttribute("inpName2")) %>" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);"></input>
                                                                 <input type="hidden" name="inpHidName2" id="inpHidName2" value="<%= Utility.escapeQuote(request.getAttribute("inpName2")) %>"></input>
                                                             </TD>
                                                             <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.code", lang)%></span></TD>
                                                             <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpEmpNo" id="inpEmpNo" maxlength="59" readonly value="<%= Utility.escapeQuote(request.getAttribute("inpEmpNo")) %>" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);"></input>
                                                                <input type="hidden" name="inpHidEmpNo" id="inpHidEmpNo" value="<%= Utility.escapeQuote(request.getAttribute("inpEmpNo")) %>"></input>
                                                            </TD>
                                                         </TR>
                                                        </TABLE>
                                                    </div>
                                                    <%-- <DIV id="nameGRP">
                                                     <TABLE style="width:100%; margin-top: 10px;">
                                                     <TR>
                                                        <TD>
                                                           <table style="width: 80%;">
                                                               <tr>
                                                                <td>
                                                                   <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                                       <TBODY>
                                                                           <TR class="FieldGroup_TopMargin"></TR>
                                                                           <TR>
                                                                               <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                               <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.documents",lang)%></TD>
                                                                               <TD class="FieldGroupTitle_Right"><IMG class="FieldGroupTitle_Right_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                               <TD class="FieldGroupContent"></TD>
                                                                           </TR>    
                                                                           <TR class="FieldGroup_BottomMargin" style="display: none;"></TR>          
                                                                       </TBODY>
                                                                   </TABLE>
                                                                </td>
                                                               </tr>
                                                           </table>
                                                        </TD>
                                                      </TR>
                                                     </TABLE>
                                                    </DIV> --%>
                                                    <div align="center">
                                                      <TABLE style="width:80%; margin-top: 10px;">
                                                         <TR>
                                                         <TD colspan="2">
                                                           <table style="width: 80%;">
                                                               <tr>
                                                                <td>
                                                                   <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                                       <TBODY>
                                                                           <TR class="FieldGroup_TopMargin"></TR>
                                                                           <TR>
                                                                               <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                               <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.documents",lang)%></TD>
                                                                               <TD class="FieldGroupTitle_Right"><IMG class="FieldGroupTitle_Right_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                               <TD class="FieldGroupContent"></TD>
                                                                           </TR>    
                                                                           <TR class="FieldGroup_BottomMargin" style="display: none;"></TR>          
                                                                       </TBODY>
                                                                   </TABLE>
                                                                </td>
                                                               </tr>
                                                           </table>
                                                        </TD>
                                                      </TR>
                                                      <TR>
                                                             <TD class="TitleCell" align="left" style="width:110px;">
                                                                 <span class="LabelText" ><%= Resource.getProperty("hcm.documenttype", lang)%>
                                                                 </span>
                                                             </TD>
                                                             <TD class="TextBox_ContentCell">
                                                                  <SELECT id="inpDocType" name="inpDocType" class="ComboKey Combo_TwoCells_width required" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();">
                                                                    <%-- <OPTION value='0' selected><%= Resource.getProperty("hcm.select", lang)%></OPTION> --%>
                                                                        <%-- <% 
                                                                         if(doctype!=null && doctype.size()>0) { 
                                                                         for(DocumentsVO vo:doctype){%>
                                                                        <option value='<%= vo.getDoctypId()%>' <%if(request.getAttribute("inpDocType") !=null && request.getAttribute("inpDocType").equals(vo.getDoctypId())){%> selected<%} %>  ><span><%= vo.getCode() +"-"+ vo.getName()%></span></option>
                                                                        <%}}%>  --%>
                                                                   <%-- <OPTION value="H" <%if(request.getAttribute("inpDocType") !=null && request.getAttribute("inpDocType").equals("H")){%> selected<%} %>>Hiring Decision</OPTION>
                                                                   <OPTION value="O" <%if(request.getAttribute("inpDocType") !=null && request.getAttribute("inpDocType").equals("O")){%> selected<%} %>>Offer Letter</OPTION>
                                                                   <OPTION value="I" <%if(request.getAttribute("inpDocType") !=null && request.getAttribute("inpDocType").equals("I")){%> selected<%} %>>ID Copy</OPTION>
                                                                   <OPTION value="M" <%if(request.getAttribute("inpDocType") !=null && request.getAttribute("inpDocType").equals("M")){%> selected<%} %>>Medical Report</OPTION> --%> 
                                                                 </SELECT>
                                                             </TD>
                                                        </TR> 
                                                        <TR>
                                                              <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.issueddate",lang)%></span></TD>
                                                              <TD class="TextBox_btn_ContentCell">
                                                                 <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                                  <tr>
                                                                      <td class="TextBox_ContentCell">
                                                                            <%  if(request.getAttribute("inpIssuedDate")!= null ) { %>
                                                                                <a></a>
                                                                                   <input type="text" id="inpIssuedDate" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"  class="dojoValidateValid TextBox_btn_OneCell_width required" value="<%= request.getAttribute("inpIssuedDate") %>" maxlength="10" name="inpIssuedDate" onchange="enableForm();"></input>
                                                                           <%}
                                                                           else{%>
                                                                             <input type="text" id="inpIssuedDate" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"  class="dojoValidateValid TextBox_btn_OneCell_width required" value="<%= request.getAttribute("today") %>" maxlength="10" name="inpIssuedDate" onchange="enableForm();"></input>
                                                                             <%} %>   
                                                                       </td>
                                                                   </tr>
                                                                  </table> 
                                                              </TD> 
                                                        </TR> 
                                                        <TR>
                                                              <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.validdate",lang)%></span></TD>
                                                              <TD class="TextBox_btn_ContentCell">
                                                                 <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                                  <tr>
                                                                      <td class="TextBox_ContentCell">
                                                                            <%  if(request.getAttribute("inpValidDate")!= null ) {%>
                                                                                   <input type="text" id="inpValidDate" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"  class="dojoValidateValid TextBox_btn_OneCell_width" value="<%= request.getAttribute("inpValidDate") %>" maxlength="10" name="inpValidDate" onchange="enableForm();"></input>
                                                                           <%}
                                                                           else{ %>
                                                                             <input type="text" id="inpValidDate" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"  class="dojoValidateValid TextBox_btn_OneCell_width" value="" maxlength="10" name="inpValidDate" onchange="enableForm();"></input>
                                                                             <%} %>  
                                                                       </td>
                                                                   </tr>
                                                                  </table> 
                                                              </TD> 
                                                        </TR>
                                                        <TR>
                                                              <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.selectdocument",lang)%></span></TD>
                                                              <TD class="TextBox_btn_ContentCell"  style="padding-left:0.4%">
                                                                <input type="file" id="inpFile" name="inpFile" onchange="namechange();"></input>
                                                                <a href="#"><span id="filenam" onclick="upload();"></span></a>
                                                                <!-- <textarea rows="4" width="50" cols="30" id="inpDocumentVal" name="inpDocumentVal" onkeydown="return onchangeevent();" onchange="enableForm();">                                                               
                                                                </textarea>   -->                                                            
                                                               <a href="#"><img name="downloadimg" id="downloadimg" style="height:7%;width:3%" onclick="download();" src="../web/sa.elm.ob.hcm/images/download.ico"></img></a>
                                                              </TD>
                                                        </TR>
                                                        <TR>
                                                              <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.origduplicate",lang)%></span></TD>
                                                              <TD  class="TextBox_ContentCell">
                                                             <SELECT id="inpIsOriginal" name="inpIsOriginal" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();">
                                                                <option value='O' <%if(request.getAttribute("inpIsOriginal")!=null &&  request.getAttribute("inpIsOriginal").equals("O")){%> selected<%} %>><span><%=  Resource.getProperty("hcm.original",lang) %></span></option>
                                                                <option value='D' <%if(request.getAttribute("inpIsOriginal")!=null &&  request.getAttribute("inpIsOriginal").equals("D")){%> selected<%} %>><span><%=  Resource.getProperty("hcm.duplicate",lang) %></span></option>
                                                              
                                                              
                                                                 </SELECT> </TD> 
                                                        </TR>
                                                   </TABLE>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        </TD>
                                     </TR>
                                   </TABLE>
                              </TD></TR>
                            </TABLE>
                    </DIV>  
                    <TABLE width="100%" border="0" cellspacing="0" cellpadding="0" class="Menu_ContentPane_Bottom" id="tdbottomSeparator">
                      <TR>
                        <TD class="Main_Bottom_bg"><IMG src="../web/images/blank.gif" border="0"></IMG></TD>
                      </TR>
                    </TABLE>
                </TD>
                <TD valign="top">
                    <TABLE width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_Right" id="tdrightSeparator">
                      <TR><TD class="Main_NavBar_bg_right"></TD></TR>
                      <TR><TD class="Main_ToolBar_bg_right" valign="top"></TD></TR>
                      <TR><TD class="Main_Client_bg_right"></TD></TR>
                      <TR><TD class="Main_Bottom_bg_right"></TD></TR>
                    </TABLE>
                </TD>
            </TR>
            </TABLE>
    </FORM>
</BODY>
 
<script type="text/javascript" src="../web/js/common/select2.min.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.hcm/js/documents/Documents.js"></script> 
<script type="text/javascript">onLoadDo();</script>
<script type="text/javascript">
var contextPath = '<%=request.getContextPath()%>';
var currentTab = 'DOC';
var changedvaluessave = '<%= Resource.getProperty("hcm.changedvaluessave", lang) %>';
var seldoctype = '<%= Resource.getProperty("hcm.seldoctype", lang) %>';
var selectdoc = '<%= Resource.getProperty("hcm.selectdoc", lang) %>';
var docTypeId='<%= request.getAttribute("inpDocType") %>';
var docTypeName='<%=request.getAttribute("inpDocTypeName")%>'; 
getDocTypeOnLoad();
function getDocTypeOnLoad(){
    setTimeout(function () {
        $("#inpDocType").select2(selectBoxAjaxPaging({
            url : function() {
                return '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.documents.ajax/DocumentsAjax?inpAction=getDocumentType'
                },
           size : "small"
       }));
        $("#inpDocType").on("select2:unselecting", function (e) {
          document.getElementById("inpDocType").options.length = 0;
        });
    }, 100);
}
if(docTypeId!=null && docTypeId!="" ){
    var data = [{
        id: docTypeId,
        text: docTypeName
    }];
    $("#inpDocType").select2({
        data: data
    });
}
</script> 

</HTML> 
