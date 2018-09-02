<%@page import="sa.elm.ob.utility.util.Utility"%>
<%@page import="org.codehaus.jettison.json.JSONObject, org.codehaus.jettison.json.JSONArray"%>
<%@page import="sa.elm.ob.hcm.ad_reports.positionTransaction.PositionTransactionsDetailsVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>
    <%@ page import="java.util.List,java.util.ArrayList" %>
    <% 
    String lang = ((String)session.getAttribute("#AD_LANGUAGE"));
    
    JSONObject empList = (JSONObject) request.getAttribute("EmployeeList");
    JSONObject absTyp = (JSONObject) request.getAttribute("AbsenceTypeList");
    JSONArray empArray=empList.getJSONArray("data");
    JSONArray absTypArray=absTyp.getJSONArray("data");

    String style="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css";
    String toolBarStyle="../web/js/common/CommonFormLtr.css";
    if(lang.equals("ar_SA")){
     style="../web/skins/rtl/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP_250.css";
    toolBarStyle="../web/js/common/CommonFormRtl.css";
    }
    %>
<HTML xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%= Resource.getProperty("hcm.absencedecison.title",lang) %></TITLE>
<META http-equiv="Content-Type" content="text/html; charset=utf-8"></META> 
<LINK rel="stylesheet" type="text/css" href="<%=style %>" id="paramCSS"></LINK>
<link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>"></link> 
<link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
<link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>   
<link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
<link rel="stylesheet" type="text/css" href="../web/js/common/select2.min.css"></link>

<script type="text/javascript" src="../web/js/common/common.js"></script>
<script type="text/javascript" src="../web/js/utils.js"></script>
<script type="text/javascript" src="../web/js/windowKeyboard.js"></script>
<script type="text/javascript" src="../web/js/searchs.js"></script>
<script type="text/javascript" id="paramDirectory">var baseDirectory = "../web/";</script>
<script type="text/javascript" id="paramLanguage">var defaultLang="en_US";</script>

<!-- <script type="text/javascript" src="../web/js/default/DateTextBox.js"></script>
<script type="text/javascript" src="../web/js/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../web/js/jscalendar/lang/calendar-lang.js"></script>  -->  

<style type="text/css">
  .ui-autocomplete-input {height: 21px; border-radius:0px !important; width:252px;font-size: 12px; padding: 0 0 0 3px; float: left;border:1px solid #CDD7BB;}
   .ui-button-icon-primary{left: .9em !important;} 
    ul{border:1px solid #FF9C30 !important;}        
</style>               
<script type="text/javascript">
function validate(action) {
    return true;
}
function onLoadDo() {
    /*<<<<<OB Code>>>>>*/
    this.windowTables = new Array(new windowTableId('client', 'buttonOK'));
    setWindowTableParentElement();
    this.tabsTables = new Array(new tabTableId('tdtopTabs'));
    setTabTableParentElement();
    setBrowserAutoComplete(false);
    setFocusFirstControl(null, 'inpMailTemplate');
    resizeArea();
    updateMenuIcon('buttonMenu');
}
function onResizeDo() {
    resizeArea();
}
</script>
</HEAD>
   <BODY onresize="onResizeDo();" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" id="paramBody">
      <FORM id="form" method="post" action="" name="frmMain">
         <INPUT type="hidden" name="Command"></INPUT>
         <INPUT type="hidden" name="IsPopUpCall" value="1"></INPUT>
         <INPUT type="hidden" name="inpLastFieldChanged"></INPUT>
         <INPUT type="hidden" name="inpAction" id="inpAction" value=""></INPUT>
         
         <INPUT type="hidden" name="inpEmpId" id="inpEmpId" value=""></INPUT>
         <INPUT type="hidden" name="inpAbsenceTypeId" id="inpAbsenceTypeId" value=""></INPUT>         
         <INPUT type="hidden" name="inpAbsenceDecisionId" id="inpAbsenceDecisionId" value=""></INPUT>
                   
         <TABLE height="100%" border="0" cellpadding="0" cellspacing="0" id="main">
            <TR>
               <TD valign="top">
                  <DIV class="Main_ContentPane_NavBar" id="tdtopButtons">
                     <TABLE class="Main_ContentPane_NavBar" id="tdtopNavButtons"></TABLE>
                  </DIV>
                  </TD>
           <TD valign="top">
               <TABLE width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_Left">
                 <TR><TD class="Main_NavBa r_bg_left" id="tdleftSeparator"></TD></TR>
                 <TR><TD class="Main_ToolBar_bg_left" valign="top"></TD></TR>
                 <TR><TD class="Main_Client_bg_left"></TD></TR>
                 <TR><TD class="Main_Bottom_bg_left"></TD></TR>
               </TABLE>
           </TD> 
           <TD valign="top">
                  <DIV class="Main_ContentPane_ToolBar" id="paramToolBar">
                     <table class="Main_ContentPane_ToolBar Main_ToolBar_bg"
                        id="tdToolBar">
                        <tr>
                           <td class="Main_ToolBar_Separator_cell"><img
                              src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                           <td width="2%"><a href="javascript:void(0);"
                              onClick="onClickRefresh()" class="Main_ToolBar_Button"
                              onMouseOver="window.status='Reload Current Page';return true;"
                              onMouseOut="window.status='';return true;"
                              onclick="this.hideFocus=true" onblur="this.hideFocus=false"
                              id="buttonRefresh"><img
                              class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Refresh"
                              src="../web/images/blank.gif" title="finance.reload"
                              border="0" id="linkButtonRefresh"></a></td>
                           <td class="Main_ToolBar_Separator_cell"><img
                              src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                           <td class="Main_ToolBar_Space"></td>
                        </tr>
                     </table>
                  </DIV>
                  <TABLE width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_TabPane" id="tdtopTabs">
                     <TR id="paramParentTabContainer">
                        <td class="tabBackGroundInit"></td>
                     </TR>
                     <TR id="paramMainTabContainer">
                        <td class="tabBackGroundInit"><span style="background: none;" class="tabTitle"></span></td>
                     </TR>
                     <tr>
                        <td class="tabBackGround">
                           <div class="marginLeft">
                              <div>
                                 <span class="dojoTabcurrentfirst">
                                    <div>
                                       <span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;">
                                       <%= Resource.getProperty("hcm.absencedecison.title",lang).replace("'", "\\\'") %>
                                       </a></span>
                                    </div>
                                 </span>
                              </div>
                           </div>
                        </td>
                     </tr>
                  </TABLE>
                  <DIV class="Main_ContentPane_Client" style="overflow: auto; display: none;" id="client">
                     <TABLE class="dojoTabContainer" border="0" cellpadding="0" cellspacing="0" width="100%" >
                        <TR>
                           <TD>
                              <TABLE class="dojoTabPaneWrapper" align="right" border="0" cellpadding="0" cellspacing="0" width="100%" summary="">
                                 <TR>
                                    <TD>
                                       <TABLE cellpadding="0" cellspacing="0" id="messageBoxID" class="MessageBox">
                                          <TBODY>
                                             <TR class="MessageBox_TopMargin" align="right">
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
                                                                     <TR>
                                                                        <TD class="MessageBox_TopLeft"></TD>
                                                                     </TR>
                                                                     <TR>
                                                                        <TD class="MessageBox_Left"></TD>
                                                                     </TR>
                                                                  </TBODY>
                                                               </TABLE>
                                                            </TD>
                                                            <TD class="MessageBox_bg">
                                                               <TABLE class="MessageBox_Top">
                                                                  <TBODY>
                                                                     <TR>
                                                                        <TD>
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
                                                                           <div id="hideMessage" style="float: right; margin-top:-13px; margin-right:10px;">
                                                                              <a style="color: yellow;font-size: 11.5px" href="javascript:void(0);" onClick="document.getElementById('messageBoxID').style.display = 'none';"><%= Resource.getProperty("hcm.hide",lang) %></a>
                                                                           </div>
                                                                        </TD>
                                                                     </TR>
                                                                  </TBODY>
                                                               </TABLE>
                                                            </TD>
                                                            <TD class="MessageBox_RightTrans">
                                                               <TABLE style="width: 100%; height: 100%" cellpadding="0" cellspacing="0">
                                                                  <TBODY>
                                                                     <TR>
                                                                        <TD class="MessageBox_TopRight"></TD>
                                                                     </TR>
                                                                     <TR>
                                                                        <TD class="MessageBox_Right"></TD>
                                                                     </TR>
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
                                       <div id="LoadingContent" style="display:none;">
                                          <div style="text-align: center;margin-top: 17%;"><img alt="Loading" src="../web/images/loading.gif"></div>
                                          <div style="color:#000000;font-family: sans-serif;font-size:14px;margin-top:10px;text-align: center;"><%= Resource.getProperty("hcm.loading",lang).replace("'", "\\\'") %> </div>
                                       </div>
                                       <TABLE  align="center" width="100%">
                                          <TR>
                                             <TD>&nbsp;</TD>
                                          </TR>
                                          <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpEmpNoLabel"> <b> <%=Resource.getProperty("hcm.employeeno", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                             <td class="TextBox_ContentCell">
                                                <select id="inpEmpNo" class="ComboKey Combo_TwoCells_width" onchange="getDecision()">
                                                 <option value="0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                  <%   if(empArray.length() > 0){
                                                        for (int i = 0, size = empArray.length(); i < size; i++){
                                                           JSONObject objectInArray = empArray.getJSONObject(i);%>
                                                    <option value='<%= objectInArray.get("id")%>' ><span><%= objectInArray.get("empName") %></span></option>
                                                   <%}}%>
                                                </select>
                                             </td>
                                          </tr>                                        
                                          
                                            <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpAbsenceTypeLabel"> <b> <%=Resource.getProperty("hcm.absencetype", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                             <td class="TextBox_ContentCell">
                                                <select id="inpAbsenceType" class="ComboKey Combo_TwoCells_width" onchange="getDecision()">
                                                 <option value="0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                  <%   if(absTypArray.length() > 0){
                                                        for (int i = 0, size = absTypArray.length(); i < size; i++){
                                                           JSONObject objectInArray = absTypArray.getJSONObject(i);%>
                                                    <option value='<%= objectInArray.get("id")%>' ><span><%= objectInArray.get("absTypeName") %></span></option>
                                                   <%}}%>
                                                </select>
                                             </td>
                                          </tr>
                                      <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpAbsenceDecisonLabel"> <b> <%=Resource.getProperty("hcm.decisionno", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                             <td class="TextBox_ContentCell">
                                                <select id="inpAbsenceDecision" class="ComboKey Combo_TwoCells_width">
                                                 <option value="0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                   <%-- <%   if(decNoArray.length() > 0){
                                                        for (int i = 0, size = decNoArray.length(); i < size; i++){
                                                           JSONObject objectInArray = absTypArray.getJSONObject(i);%>
                                                    <option value='<%= objectInArray.get("id")%>' ><span><%= objectInArray.get("recordIdentifier") %></span></option>
                                                   <%}}%> --%> 
                                                </select>
                                             </td>
                                          </tr>
                                          <TR  align = "center">
                                             <td></td>
                                             <TD  id="submitButton">
                                                <BUTTON type="button" id="Submit_linkBTN" class="ButtonLink" onclick="generateReport();" style="margin: 10px 0;">
                                                   <TABLE class="Button">
                                                      <TR>
                                                         <TD class="Button_left"><IMG class="Button_Icon Button_Icon_process" alt="Submit" title="Submit" src="../web/images/blank.gif" border="0"></IMG></TD>
                                                         <TD class="Button_text" id="Submit_BTNname">
                                                            <%= Resource.getProperty("hcm.printreport.submit",lang).replace("'", "\\\'") %>
                                                         </TD>
                                                         <TD class="Button_right"></TD>
                                                      </TR>
                                                   </TABLE>
                                                </BUTTON>
                                             </TD>
                                          </TR>
                                       </TABLE>
                                    </TD>
                                 </TR>
                              </TABLE>
                           </TD>
                        </TR>
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
<script type="text/javascript">onLoadDo();</script>
<script type="text/javascript" src="../web/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.widget.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.position.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.button.js"></script>
<script type="text/javascript" src="../web/js/jquery/grid.locale-en.js"></script>
<script type="text/javascript" src="../web/js/jquery/datejs/jquery-ui.min.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.autocomplete.js"></script> 
<script type="text/javascript" src="../web/js/common/select2.min.js"></script>
  
<script type="text/javascript">
function reloadWindow() {
    submitCommandForm('DEFAULT', false, null, 'AbsenceDecision', '_self', null, false);
    return false;
}
function onClickRefresh() {
    document.getElementById("inpAction").value = "";
    reloadWindow();
}

 //Process to call the respective report.
function generateReport() {    
    document.getElementById("inpEmpId").value = document.getElementById("inpEmpNo").value;    
    document.getElementById("inpAbsenceTypeId").value = document.getElementById("inpAbsenceType").value;
    document.getElementById("inpAbsenceDecisionId").value = document.getElementById("inpAbsenceDecision").value;        
    document.getElementById("inpAction").value = "Submit";
    
    if($("#inpAbsenceDecision").val()==null || $("#inpAbsenceDecision").val()=="0"){
        OBAlert('<%= Resource.getProperty("hcm.absencedecison.decisionnomandatory",lang).replace("'", "\\\'") %>');
        return false;  
    }
        
    openServletNewWindow('PRINT_PDF', true, null, 'AbsenceDecision', null, false, '700', '1000', true);
    return false;    
}
 
$( "#inpEmpNo").select2(selectBoxAjaxPaging({size: "small",
    placeholder: true}));
$( "#inpAbsenceDecision").select2(selectBoxAjaxPaging({size: "small",
    placeholder: true}));
$( "#inpAbsenceType").select2(selectBoxAjaxPaging({size: "small",
    placeholder: true}));
    
document.getElementById("select2-inpEmpNo-container").style.backgroundColor="#F5F7F1";
document.getElementById("select2-inpAbsenceType-container").style.backgroundColor="#F5F7F1";

function getDecision(){
	getDecisonNo($("#inpEmpNo").val(), $("#inpAbsenceType").val());
}
getDecisonNo(null, null);
function getDecisonNo(empId, absenceTypeId){
	if(empId=='0')
		empId=null;
	if(absenceTypeId=='0')
		absenceTypeId=null;
    document.getElementById("inpAbsenceDecision").options.length = 0; 
    setTimeout(function () {
        $("#inpAbsenceDecision").select2(selectBoxAjaxPaging({
            url : function() {
                return '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.absencedecision/AbsenceDecision?inpAction=getDecisonNoList&empId='+empId+'&absenceTypeId='+absenceTypeId
           },
           size : "small"
       }));           
        $("#inpAbsenceDecision").on("select2:unselect", function (e) {
          document.getElementById("inpAbsenceDecision").options.length = 0;                   
        });
        //document.getElementById("select2-inpAbsenceDecision-container").style.backgroundColor="#F5F7F1";
    }, 100);
 }
   </script>
</HTML>