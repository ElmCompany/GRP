<%@page import="sa.elm.ob.utility.util.Utility"%>
<%@page import="org.codehaus.jettison.json.JSONObject, org.codehaus.jettison.json.JSONArray"%>
<%@page import="sa.elm.ob.hcm.ad_reports.SecondmentDuringPeriod.SecondmentDuringPeriodVO"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>
<%@ page import="java.util.List,java.util.ArrayList" %>
    <% 
    String lang = ((String)session.getAttribute("#AD_LANGUAGE"));
   
    JSONObject empList = (JSONObject) request.getAttribute("inpEmployeeList");
    JSONArray empArray=empList.getJSONArray("data");
    
    List<SecondmentDuringPeriodVO> depList =  (ArrayList<SecondmentDuringPeriodVO> )request.getAttribute("inpDeptList");
    List<SecondmentDuringPeriodVO> empTypeList = (List<SecondmentDuringPeriodVO> )request.getAttribute("inpEmployeeTypeList");
    List<SecondmentDuringPeriodVO> gradeFromList = (List<SecondmentDuringPeriodVO> )request.getAttribute("inpGradeFromList");
    List<SecondmentDuringPeriodVO> gradeToList = (List<SecondmentDuringPeriodVO> )request.getAttribute("inpGradeToList");
    List<SecondmentDuringPeriodVO> lineManagerList = (List<SecondmentDuringPeriodVO> )request.getAttribute("inpLineManagerList");

    String style="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css";
    String toolBarStyle="../web/js/common/CommonFormLtr.css";
    if(lang.equals("ar_SA")){
     style="../web/skins/rtl/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP_250.css";
    toolBarStyle="../web/js/common/CommonFormRtl.css";
    }
    %>
<HTML xmlns="http://www.w3.org/1999/xhtml">
  <HEAD>
   <META http-equiv="Content-Type" content="text/html; charset=utf-8"></META> 
   <LINK rel="stylesheet" type="text/css" href="<%=style %>" id="paramCSS"></LINK>
   <link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>"></link>
    <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/common/select2.min.css"></link>
    <style type="text/css">
    </style>
    <script type="text/javascript" src="../web/js/common/common.js"></script>
    <script type="text/javascript" src="../web/js/utils.js"></script>
    <script type="text/javascript" src="../web/js/windowKeyboard.js"></script>
    <script type="text/javascript" src="../web/js/searchs.js"></script>
    <script type="text/javascript" id="paramDirectory">var baseDirectory = "../web/";</script>
    <script type="text/javascript" id="paramLanguage">var defaultLang="en_US";</script>
    <script type="text/javascript" src="../web/js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.min_1.11.0.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.plugin.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura-ar.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.plus.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.ext.js"></script>
    <script type="text/javascript">
    function validate(action) {return true;}
    function onLoadDo(){
        /*<<<<<OB Code>>>>>*/
        this.windowTables = new Array(new windowTableId('client', 'buttonOK'));
        setWindowTableParentElement();
        this.tabsTables = new Array(new tabTableId('tdtopTabs'));
        setTabTableParentElement();setBrowserAutoComplete(false);setFocusFirstControl(null, 'inpMailTemplate');
        resizeArea();updateMenuIcon('buttonMenu');onLoad();
        /*<<<<<OB Code>>>>>*/
         $('#inpFromDate').calendarsPicker({calendar:  
             $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect : function(dates){enableForm();},showTrigger:  
         '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
    
         $('#inpToDate').calendarsPicker({calendar:  
             $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect : function(dates){enableForm();},showTrigger:  
         '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
      
    }
    function onResizeDo(){resizeArea();}
    function onLoad()
    {
       
    }
    </script>
</HEAD>
  <BODY onresize="onResizeDo();" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" id="paramBody">
      <FORM id="form" method="post" action="" name="frmMain">
         <INPUT type="hidden" name="Command"></INPUT>
         <INPUT type="hidden" name="IsPopUpCall" value="1"></INPUT>
         <INPUT type="hidden" name="inpLastFieldChanged"></INPUT>
         <INPUT type="hidden" name="inpAction" id="inpAction" value=""></INPUT>          
          <INPUT type="hidden" name="inpempId" id="inpempId" value=""></INPUT> 
          <INPUT type="hidden" name="inpdeptId" id="inpdeptId" value=""></INPUT>
          <INPUT type="hidden" name="inpempTypeId" id="inpempTypeId" value=""></INPUT>
          <INPUT type="hidden" name="inpgradeFromId" id="inpgradeFromId" value=""></INPUT>
          <INPUT type="hidden" name="inpgradeToId" id="inpgradeToId" value=""></INPUT> 
           <INPUT type="hidden" name="inpLineManagerId" id="inpLineManagerId" value=""></INPUT> 
           <INPUT type="hidden" name="inpgenderId" id="inpgenderId" value=""></INPUT>                    
           <INPUT type="hidden" name="fromdate" id="fromdate" value=""></INPUT>
           <INPUT type="hidden" name="todate" id="todate" value=""></INPUT>
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
               </TABLE></TD>
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
                              border="0" id="linkButtonRefresh"></img></a></td>
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
                                       <%= Resource.getProperty("hcm.secondmentDuringPeriod.report",lang).replace("'", "\\\'") %>
                                       </a></span>
                                    </div>
                                 </span>
                              </div>
                           </div>
                        </td>
                     </tr>
                  </TABLE>
                  <DIV class="Main_ContentPane_Client" style="overflow: auto; display: none;" id="client">
                     <TABLE class="dojoTabContainer" border="0" cellpadding="0" cellspacing="0" style="width: 100%" >
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
                                                                              <a style="color: yellow;font-size: 11.5px" href="javascript:void(0);" onClick="document.getElementById('messageBoxID').style.display = 'none';"><%= Resource.getProperty("finance.hide",lang) %></a>
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
                                          <div style="color:#000000;font-family: sans-serif;font-size:14px;margin-top:10px;text-align: center;"><%= Resource.getProperty("finance.loading",lang).replace("'", "\\\'") %> </div>
                                       </div>
                                       <TABLE  align="center" width="100%">
                                          <TR>
                                             <TD>&nbsp;</TD>
                                          </TR>
                                         <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpEmployeeNameLabel"> <b> <%=Resource.getProperty("hcm.empname", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                             <td class="TextBox_ContentCell">
                                                <select id="inpEmpName" class="Combo Combo_TwoCells_width" onchange="getDepartmentList(this.value)">
                                                  <option value="0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                </select>
                                             </td>
                                          </tr>   
                                           <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpDept"> <b> <%=Resource.getProperty("hcm.department", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                             <td class="TextBox_ContentCell">
                                                <select name="inpDepartment" id="inpDepartment" class="Combo Combo_TwoCells_width" >
                                                 <option value = "0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                 <%   if(depList.size() > 0){
                                                      for(SecondmentDuringPeriodVO vo:depList){ %>
                                                      
                                                   <option value='<%= vo.getOrgId()%>' ><span><%= vo.getOrgName()%></span></option>
                                                   <%}}%>
                                                </select>
                                             </td>
                                             </tr>
                                             <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpEmployeeTypeLabel"> <b> <%=Resource.getProperty("hcm.employeetype", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                             <td class="TextBox_ContentCell">
                                                <select id="inpEmployeeType" class="Combo Combo_TwoCells_width">
                                                 <option value="0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                  <% 
                                                      if (empTypeList!=null && empTypeList.size() > 0) 
                                                          for(SecondmentDuringPeriodVO vo : empTypeList) { %>    
                                                          <option value="<%= vo.getGradeClassId() %>"><%= vo.getGradeClassName() %></option>
                                                      <% } %>   
                                                </select>
                                             </td>
                                          </tr>
                                            
                                             <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpEmployeeGradeFromLabel"> <b> <%=Resource.getProperty("hcm.secondmentDuringPeriod.gradeFrom", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                             <td class="TextBox_ContentCell">
                                                <select id="inpEmployeeGradeFrom" class="Combo Combo_TwoCells_width">
                                                 <option value="0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                  <% 
                                                      if (gradeFromList!=null && gradeFromList.size() > 0) 
                                                          for(SecondmentDuringPeriodVO sup : gradeFromList) { %>    
                                                          <option value="<%= sup.getGradeId() %>"><%= sup.getGradeName() %></option>
                                                      <% } %>   
                                                </select>
                                             </td>
                                          </tr>
                                          
                                          
                                           <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpEmployeeGradeToLabel"> <b> <%=Resource.getProperty("hcm.secondmentDuringPeriod.gradeTo", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                             <td class="TextBox_ContentCell">
                                                <select id="inpEmployeeGradeTo" class="Combo Combo_TwoCells_width">
                                                 <option value="0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                  <% 
                                                      if (gradeToList!=null && gradeToList.size() > 0) 
                                                          for(SecondmentDuringPeriodVO sup : gradeToList) { %>    
                                                          <option value="<%= sup.getGradeId() %>"><%= sup.getGradeName() %></option>
                                                      <% } %>   
                                                </select>
                                             </td>
                                          </tr>
                                          <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpLineManagerLabel"> <b> <%=Resource.getProperty("hcm.secondmentDuringPeriod.linemanager", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                             <td class="TextBox_ContentCell">
                                                <select id="inpLineManager" class="Combo Combo_TwoCells_width">
                                                 <option value="0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                  <% 
                                                      if (lineManagerList!=null && lineManagerList.size() > 0) 
                                                          for(SecondmentDuringPeriodVO sup : lineManagerList) { %>    
                                                          <option value="<%= sup.getsupervisorId() %>"><%= sup.getsupervisorName() %></option>
                                                      <% } %>   
                                                </select>
                                             </td>
                                          </tr>
                                          <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpGenderLabel"> <b> <%=Resource.getProperty("hcm.gender", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                             <td class="TextBox_ContentCell">
                                                <select id="inpGender" class="Combo Combo_TwoCells_width">
                                                 <option value="0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                     
                                                   <option value="F"><%=Resource.getProperty("hcm.female", lang).replace("'", "\\\'") %></option>
                                                   <option value="M"><%=Resource.getProperty("hcm.male", lang).replace("'", "\\\'") %></option>
                                                        
                                                </select>
                                             </td>
                                          </tr>
                                          <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpStartDate"> <b> <%=Resource.getProperty("hcm.FromDate", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                             <td class="TextBox_ContentCell">
                                                  <input type="text" id="inpFromDate" class="dojoValidateValid TextBox_btn_OneCell_width required" onchange="document.getElementById('messageBoxID').style.display = 'none';onChangeField(); "  value="<%= request.getAttribute("inpDate") %>" maxlength="10" name="inpFromDate"  ></input>
                                             </td>
                                             </tr>
                                             <tr>
                                             <td class="TitleCell"><span class="LabelText" id="inpEndDate"> <b> <%=Resource.getProperty("hcm.ToDate", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                              <td class="TextBox_ContentCell">                                                                 
                                                   <input type="text" id="inpToDate" class="dojoValidateValid TextBox_btn_OneCell_width required" onchange="document.getElementById('messageBoxID').style.display = 'none';onChangeField(); "  value="<%= request.getAttribute("inpDate") %>" maxlength="10" name="inpToDate" ></input>                                                                     
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
    submitCommandForm('DEFAULT', false, null, 'SecondmentDuringPeriod', '_self', null, false);
    return false;
}
function onClickRefresh() {
    document.getElementById("inpAction").value = "";
    reloadWindow();
}
 //Process to call the respective report.
function generateReport() {
     document.getElementById("inpempId").value = document.getElementById("inpEmpName").value; 
     document.getElementById("inpdeptId").value = document.getElementById("inpDepartment").value;
     document.getElementById("inpempTypeId").value = document.getElementById("inpEmployeeType").value;
     document.getElementById("inpgradeFromId").value = document.getElementById("inpEmployeeGradeFrom").value;
     document.getElementById("inpgradeToId").value = document.getElementById("inpEmployeeGradeTo").value;
     document.getElementById("inpLineManagerId").value = document.getElementById("inpLineManager").value;
     document.getElementById("inpgenderId").value = document.getElementById("inpGender").value;
     document.getElementById("fromdate").value = document.getElementById("inpFromDate").value;
     document.getElementById("todate").value = document.getElementById("inpToDate").value;
     document.getElementById("inpAction").value = "Submit";
         
     if(document.getElementById("fromdate").value == "")
     {
     OBAlert('<%= Resource.getProperty("hcm.fromdate.empty",lang).replace("'", "\\\'") %>');
  return false;
     }
     if(document.getElementById("todate").value == "")
     {
     OBAlert('<%= Resource.getProperty("hcm.todate.empty",lang).replace("'", "\\\'") %>');
  return false;
     }
     //check the date format validation
     var fromDate=$("#inpFromDate").val();
     var toDate=$("#inpToDate").val();
     var dateformat = /^(0?[1-9]|[12][0-9]|3[01])[\/\-](0?[1-9]|1[012])[\/\-]\d{4}$/;
     if(fromDate!="")
         {
     if(fromDate.match(dateformat))
     {
             
     }
     else
         {
             OBAlert('<%= Resource.getProperty("hcm.entervalid.dateformat",lang).replace("'", "\\\'") %>');
         return false;
         }
         }
     if(toDate!="")
     {
           if(toDate.match(dateformat))
               {
         
               }
           else
                 {
         OBAlert('<%= Resource.getProperty("hcm.entervalid.dateformat",lang).replace("'", "\\\'") %>');
         return false;
         }
     }
     fromDate = new Date(fromDate.substring(3,5)+"/"+fromDate.substring(0,2)+"/"+fromDate.substring(6,10));
     toDate = new Date(toDate.substring(3,5)+"/"+toDate.substring(0,2)+"/"+toDate.substring(6,10));
           if(toDate < fromDate)
                {
              OBAlert('<%= Resource.getProperty("hcm.todateGreaterthanFromDate",lang).replace("'", "\\\'") %>');
             return false;
        
             }
           
    
      openServletNewWindow('PRINT_PDF', true, null, 'SecondmentDuringPeriod', null, false, '700', '1000', true);
    return false;
}
(function( $ ) {
    $.widget( "ui.combobox", {
        _create: function() {
            var self = this,select = this.element.hide(),selected = select.children( ":selected" ),value = selected.val() ? selected.text() : "";
            var input = this.input = $( "<input  placeholder=\"<%= Resource.getProperty("hcm.--select--", lang) %>\">" )
                .insertAfter( select )
                .val( value )
                .css('background-color', '#FFC')
                .autocomplete({
                        delay: 0,
                        minLength: 0,
                        source: function( request, response ) {
                            var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
                            response( select.children( "option" ).map(function() {
                                var text = $( this ).text();                                
                                if ( this.value && ( !request.term || matcher.test(text) ) )
                                    return {
                                        label: text.replace(
                                                new RegExp("(?![^&;]+;)(?!<[^<>]*)(" +
                                                        $.ui.autocomplete.escapeRegex(request.term) +
                                                        ")(?![^<>]*>)(?![^&;]+;)", "gi"), 
                                                        "<strong>$1</strong>" ),
                                                        value: text,
                                                        option: this};
                                }) );
                            },
                        select: function( event, ui ) {
                            ui.item.option.selected = true;
                            self._trigger( "selected", event, {
                                item: ui.item.option
                                });                    
                        },
                        change: function( event, ui ) {  
                            if($(select).attr("id")=='inpEmpName')
                            {       
                                // Selected an item, nothing to do
                                if ( ui.item ) {
                                    return;
                                }

                                var typedValue = $('#inpEmpName').next().val();
                                // Search for a match (case-insensitive)
                                var value = typedValue,
                                    valueLowerCase = value.toLowerCase(),
                                    valid = false;
                                
                                $("#inpEmpName > option").each(function() {
                                    if ( this.text.toLowerCase() === valueLowerCase ) {
                                        valid = true;
                                        allowPrint = true;
                                        return false;
                                    }
                                    else 
                                        allowPrint = false;
                                });

                                // Found a match, nothing to do
                                if ( valid ) {
                                    return;
                                }
                                $('#inpEmpName').next().val( "" );
                            }
                            if($(select).attr("id")=='inpDepartment')
                            {       
                                // Selected an item, nothing to do
                                if ( ui.item ) {
                                    return;
                                }

                                var typedValue = $('#inpDepartment').next().val();
                                // Search for a match (case-insensitive)
                                var value = typedValue,
                                    valueLowerCase = value.toLowerCase(),
                                    valid = false;
                                
                                $("#inpDepartment > option").each(function() {
                                    if ( this.text.toLowerCase() === valueLowerCase ) {
                                        valid = true;
                                        allowPrint = true;
                                        return false;
                                    }
                                    else 
                                        allowPrint = false;
                                });

                                // Found a match, nothing to do
                                if ( valid ) {
                                    return;
                                }
                                $('#inpDepartment').next().val( "" );
                            }                    
                        },
                         search: function( event, ui ) { 
                            if($(select).attr("id")=='inpEmpName'  )
                            {           
                                if($("#inpEmpName").next().val()=="")
                                    $("#inpEmpName").val("");
                            }
                            if($(select).attr("id")=='inpDepartment'  )
                            {           
                                if($("#inpDepartment").next().val()=="")
                                    $("#inpDepartment").val("");
                            }                            
                        }                     
                });
                input.addClass( "ui-widget ui-widget-content autocomplete-widget-content ui-corner-left autocomp_hght autocomp_wdth" );
            input.data( "autocomplete" )._renderItem = function( ul, item ) {
                return $( "<li></li>" )
                .data( "item.autocomplete", item )
                .append( "<a>" + item.label + "</a>" )
                .appendTo( ul );
            };
            input.click(function() {
                this.select();
            });
            this.div = $("<div class='divComboImage'></div>")
                    .insertAfter( input )
                    .append(  $( "<button type='button'>&nbsp;</button>" )                              
                        .css({'padding-top':"0px","height":"21px"}) 
                        .attr( "tabIndex", -1 )
                        .attr( "title", "Show All Items" )                      
                        .button({icons: {primary: "ui-icon-triangle-1-s"},text: false})
                        .removeClass( "ui-corner-all" )
                        .addClass( "ui-corner-right ui-button-icon" )
                        .addClass("ui_flip_image ")
                        .click(function() {
                            if ( input.autocomplete( "widget" ).is( ":visible" ) ) {
                                input.autocomplete( "close" );
                                return;
                            }
                            input.autocomplete( "search", "" );
                            input.focus();  
                        }) 
                    )
                    .css({'float':'left'});
            },
            
            destroy: function() {
                this.input.remove();
                this.div.remove();
                this.element.show();
                $.Widget.prototype.destroy.call( this );
            }
        
    });
    
    
})( jQuery );
 
$( "#inpEmpName").select2(selectBoxAjaxPaging({size: "small",
    placeholder: true}));
$( "#inpDepartment").select2(selectBoxAjaxPaging({size: "small",
    placeholder: true}));
    
document.getElementById("select2-inpDepartment-container").style.backgroundColor="#F5F7F1";
document.getElementById("select2-inpEmpName-container").style.backgroundColor="#F5F7F1"; 
document.getElementById("inpEmployeeType").style.backgroundColor="#F5F7F1"; 
document.getElementById("inpEmployeeGradeFrom").style.backgroundColor="#F5F7F1"; 
document.getElementById("inpEmployeeGradeTo").style.backgroundColor="#F5F7F1"; 
document.getElementById("inpLineManager").style.backgroundColor="#F5F7F1"; 
document.getElementById("inpGender").style.backgroundColor="#F5F7F1";  
$("#inpEmpName").select2(selectBoxAjaxPaging({
    url : function() {
        return '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.SecondmentDuringPeriod/SecondmentDuringPeriod?inpAction=getEmployeesList'
    },
    size : "small"
}
  ));
  
document.getElementById("select2-inpEmpName-container").style.backgroundColor="#F5F7F1"; 

function getDepartmentList(empId){  
        document.getElementById("inpDepartment").value = "00";
             $("#inpDepartment").select2(selectBoxAjaxPaging({
                url : function() {
                    return '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.SecondmentDuringPeriod/SecondmentDuringPeriod?inpAction=getDepartmentList&inpEmpId='+empId
                },
                size : "small"
            }));
             $("#inpDepartment").on("select2:unselect", function (e) {
                 document.getElementById("inpDepartment").options.length = 0;                   
               });
            document.getElementById("select2-inpDepartment-container").style.backgroundColor="#F5F7F1"; 
            getEmpTypeList(empId);
        
        
} 
function getEmpTypeList(empId){
    $.post("<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.SecondmentDuringPeriod/SecondmentDuringPeriod", {
        inpAction : 'getEmpType',
        inpEmpId : empId
    }, function(data) {
        var inpEmployeeType = document.getElementById("inpEmployeeType");
        inpEmployeeType.options.length = 0;
        for (var i in data.data) {
            $(inpEmployeeType).append($('<option>', {
                value : data.data[i].id,
                html : data.data[i].emptype
            }));
        }
   });
    getEmpGradeList(empId);
}
function getEmpGradeList(empId){
    $.post("<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.SecondmentDuringPeriod/SecondmentDuringPeriod", {
        inpAction : 'getEmpGrade',
        inpEmpId : empId
    }, function(data) {
        var inpEmployeeGradeFrom = document.getElementById("inpEmployeeGradeFrom");
        inpEmployeeGradeFrom.options.length = 0;
        for (var i in data.data) {
            $(inpEmployeeGradeFrom).append($('<option>', {
                value : data.data[i].id,
                html : data.data[i].empgrade
            }));
        }
        
        var inpEmployeeGradeTo = document.getElementById("inpEmployeeGradeTo");
        inpEmployeeGradeTo.options.length = 0;
        for (var i in data.data) {
            $(inpEmployeeGradeTo).append($('<option>', {
                value : data.data[i].id,
                html : data.data[i].empgrade
            }));
        }
   });
    getLineManagerList(empId);
}
function getLineManagerList(empId){
    $.post("<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.SecondmentDuringPeriod/SecondmentDuringPeriod", {
        inpAction : 'getLineManager',
        inpEmpId : empId
    }, function(data) {
        var inpLineManager = document.getElementById("inpLineManager");
        inpLineManager.options.length = 0;
        for (var i in data.data) {
            $(inpLineManager).append($('<option>', {
                value : data.data[i].id,
                html : data.data[i].linemanager
            }));
        }
   });
    getGenderList(empId);
}
function getGenderList(empId){
    $.post("<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.SecondmentDuringPeriod/SecondmentDuringPeriod", {
        inpAction : 'getGender',
        inpEmpId : empId
    }, function(data) {
        var inpGender = document.getElementById("inpGender");
        inpGender.options.length = 0;
        for (var i in data.data) {
            $("#inpGender").append($('<option>', {
                value : data.data[i].id,
                html : data.data[i].gender
            }));
        }
   });


    if(empId!=0){
        // document.getElementById("inpDepartment").disabled = true;
         document.getElementById("inpEmployeeType").disabled = true;
         document.getElementById("inpEmployeeGradeFrom").disabled = true;
         document.getElementById("inpEmployeeGradeTo").disabled = true;
         document.getElementById("inpLineManager").disabled = true;
         document.getElementById("inpGender").disabled = true;
     }else{
         document.getElementById("inpDepartment").disabled = false;
         document.getElementById("inpEmployeeType").disabled = false;
         document.getElementById("inpEmployeeGradeFrom").disabled = false;
         document.getElementById("inpEmployeeGradeTo").disabled = false;
         document.getElementById("inpLineManager").disabled = false;
         document.getElementById("inpGender").disabled = false;
          onClickRefresh(); 
         
     }
}
 
 </script>
</HTML>