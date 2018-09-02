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
   <META http-equiv="Content-Type" content="text/html; charset=utf-8"></META> 
    <LINK rel="stylesheet" type="text/css" href="<%=style %>" id="paramCSS"></LINK>
      <link rel="stylesheet" type="text/css" href="../web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/org.openbravo.client.application/ob-toolbar-styles.css"></link>
     <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
       <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/ui.jqgrid.css"></link>
    <link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/common/select2.min.css"></link>
    <script type="text/javascript" src="../web/js/common/common.js"></script>
    <script type="text/javascript" src="../web/js/utils.js"></script>
    <script type="text/javascript" src="../web/js/windowKeyboard.js"></script>
    
    <script type="text/javascript" src="../web/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="../web/js/jquery/grid.locale-en.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.widget.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.position.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.autocomplete.js"></script>
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.plugin.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura-ar.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.plus.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.ext.js"></script>
    
    <script type="text/javascript" src="../web/js/searchs.js"></script>
    <script type="text/javascript" id="paramDirectory">var baseDirectory = "../web/";</script>
    <script type="text/javascript" id="paramLanguage">var defaultLang="en_US";</script>
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
            $('#inpCalculationDate').calendarsPicker({calendar:  
                $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect: function() {reloadGrid(this.value);} ,showTrigger:  
            '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger" >'});
   
      
    }
    function onResizeDo(){resizeArea();reSizeGrid();}
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
                            <td width="2%" ><a href="javascript:void(0);" onClick="onClickRefresh()" class="Main_ToolBar_Button" onMouseOver="window.status='Reload Current Page';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonRefresh"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Refresh" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.reload",lang) %>" border="0" id="linkButtonRefresh"></a></td> 
                            <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                            <td class="Main_ToolBar_Space"></td>
                            </tr>
                        </table>
                    </DIV>
                    <TABLE width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_TabPane" id="tdtopTabs">
                          <TR id="paramParentTabContainer"><td class="tabBackGroundInit"></td></TR>
                          <TR id="paramMainTabContainer"><td class="tabBackGroundInit"><span style="background: none;" class="tabTitle"></span></td></tr>
                          <tr><td class="tabBackGround">
                            <div class="marginLeft">
                            <div><span class="dojoTabcurrentfirst"><div>
                                <span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%=Resource.getProperty("hcm.absence.accrual", lang)%></a></span>
                            </div></span></div>
                            </div>
                        </td></TR>
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
                                                                            
                                                                            <div id="hideMessage" style="float: right; margin-top:-13px; margin-right:10px;">
                                                                                <a style="color: yellow;font-size: 11.5px" href="javascript:void(0);" onClick="document.getElementById('messageBoxID').style.display = 'none';"><%=Resource.getProperty("hcm.hide", lang).replace("'", "\\\'")%></a>
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
                                        
                                        <div id="LoadingContent" style="display:none;">
                                            <div style="text-align: center;margin-top: 17%;"><img alt="Loading" src="../web/images/loading.gif"></div>
                                            <div style="color:#000000;font-family: sans-serif;font-size:14px;margin-top:10px;text-align: center;"><%=Resource.getProperty("hcm.loading", lang).replace("'", "\\\'")%></div>
                                        </div>
                                        
                                        <div style="width:100%;">
                                            <div style="padding: 0 1%; width: 98%;">
                                                <div style="width:100%;" id="FormDetails">
                                                    <div align="center">
                                                        <TABLE style="width:80%; margin-top: 10px;">
                                                           <tr>
                                                             <td class="TitleCell"><span class="LabelText" id="inpEmployeeLabel"> <b> <%=Resource.getProperty("hcm.employee", lang).replace("'", "\\\'")%>
                                                                </b>
                                                                </span>
                                                             </td>
                                                             <td class="TextBox_ContentCell">
                                                                <select id="inpEmployee" class="ComboKey Combo_TwoCells_width" onchange="reloadGrid(this.value);getEmpDetails(this.value);">
                                                                 <option value="0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                                  <%   if(empArray.length() > 0){
                                                                        for (int i = 0, size = empArray.length(); i < size; i++){
                                                                           JSONObject objectInArray = empArray.getJSONObject(i);%>
                                                                    <option value='<%= objectInArray.get("id")%>' ><span><%= objectInArray.get("empName") %></span></option>
                                                                   <%}}%>
                                                                </select>
                                                             </td>
                                                             <td class="TitleCell"><span class="LabelText" id="inpAbsenceTypeLabel"> <b> <%=Resource.getProperty("hcm.absencetype", lang).replace("'", "\\\'")%>
                                                                </b>
                                                                </span>
                                                             </td>
                                                             <td class="TextBox_ContentCell">
                                                                <select id="inpAbsenceType" class="ComboKey Combo_TwoCells_width" onchange="reloadGrid(this.value)">
                                                                 <option value="0"><%= Resource.getProperty("hcm.--select--",lang).replace("'", "\\\'") %></option>
                                                                  <%   if(absTypArray.length() > 0){
                                                                        for (int i = 0, size = absTypArray.length(); i < size; i++){
                                                                           JSONObject objectInArray = absTypArray.getJSONObject(i);%>
                                                                    <option value='<%= objectInArray.get("id")%>' ><span><%= objectInArray.get("absTypeName") %></span></option>
                                                                   <%}}%>
                                                                </select>
                                                             </td>
                                             <td class="TitleCell"><span class="LabelText" id="inpStartDateLabel"> <b> <%=Resource.getProperty("hcm.absence.accrual.calculationdate", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>                                             
                                             <td class="TextBox_ContentCell">
                                                    <input type="text" id="inpCalculationDate" class="dojoValidateValid TextBox_btn_OneCell_width required" onchange="document.getElementById('messageBoxID').style.display = 'none'"  value="<%= (request.getAttribute("inpCalculationDate")!=null ? request.getAttribute("inpCalculationDate") : "") %>" maxlength="10" name="inpCalculationDate" ></input> 
                                                
                                             </td>
                                          </tr> 
                                          <tr>
                                           <TD class="TitleCell" ><span class="LabelText" ><%= Resource.getProperty("hcm.persontype", lang)%></span></TD>
                                        <TD class="TextBox_ContentCell">  
                                            <input type="text" name="inpPersonType" id="inpPersonType" maxlength="35"  readonly value="" required="false"   class="dojoValidateValid  TextBox_TwoCells_width" ></input>                                      </TD>
                                        
                                           <td class="TitleCell"><span class="LabelText" id="inpHireDateLabel"> <b> <%=Resource.getProperty("hcm.hiredate", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>                                             
                                             <td class="TextBox_ContentCell">
                                                    <input type="text" id="inpHireDate" class="dojoValidateValid TextBox_btn_OneCell_width" readonly onchange="document.getElementById('messageBoxID').style.display = 'none'"  value="" maxlength="10" name="inpHireDate" ></input> 
                                                
                                             </td>
                                             <TD class="TitleCell" ><span class="LabelText" ><%= Resource.getProperty("hcm.emp.seccode", lang) %></span></TD>
                                                            
                                                             <TD class="TextBox_ContentCell">  
                                            <input type="text" name="inpSectionCode" id="inpSectionCode" maxlength="20" readonly value="" required="false"   class="dojoValidateValid  TextBox_TwoCells_width"></input> 
                                             </TD>
                                          </tr>
                                          <tr>
                                          <td class="TitleCell"><span name="inpGradeLabel" class="LabelText" id="inpGradeLabel"> <%= Resource.getProperty("hcm.emp.grade", lang) %></span></td>
                                                            <td class="TextBox_ContentCell">
                                                             <input type="text" name="inpGrade" id="inpGrade" maxlength="20" readonly value=""  class="dojoValidateValid  TextBox_TwoCells_width"></input></td>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.jobno", lang) %></span></TD>
                                                            <td class="TextBox_ContentCell">
                                                            <input type="text" name="inpJobno" id="inpJobno" maxlength="20" readonly value=""  class="dojoValidateValid  TextBox_TwoCells_width"></input></td>
                                           <td class="TitleCell"><span name="inpEmpGradeLabel" class="LabelText" id="inpEmpGradeLabel"> <%= Resource.getProperty("hcm.emp.empGrade", lang) %></span></td>
                                               <TD class="TextBox_ContentCell">  
                                            <input type="text" name="inpEmpGrade" id="inpEmpGrade" maxlength="20" readonly value=""  class="dojoValidateValid  TextBox_TwoCells_width"></input> 
                                             </TD>            
                                                            
                                          </tr>
                                          <tr>
                                           <td class="TitleCell"><span name="inpAssignedDeptLabel" class="LabelText" id="inpAssignedDeptLabel"> <%= Resource.getProperty("hcm.absence.accrual.assigned.department", lang) %></span></td>
                                               <TD class="TextBox_ContentCell">  
                                            <input type="text" name="inpAssignedDept" id="inpAssignedDept" maxlength="20" readonly value=""  class="dojoValidateValid  TextBox_TwoCells_width"></input> 
                                             </TD> 
                                             <td class="TitleCell"><span name="inpEmployeeCategoryLabel" class="LabelText" id="inpEmployeeCategoryLabel"> <%= Resource.getProperty("hcm.empcategory", lang) %></span></td>
                                               <TD class="TextBox_ContentCell">  
                                            <input type="text" name="inpEmployeeCategory" id="inpEmployeeCategory" maxlength="20" readonly value=""  class="dojoValidateValid  TextBox_TwoCells_width"></input> 
                                             </TD> 
                                            
                                              <td class="TitleCell"><span name="inpStatusLabel" class="LabelText" id="inpStatusLabel"> <%= Resource.getProperty("hcm.status", lang) %></span></td>
                                                            <td class="TextBox_ContentCell"><select id="inpStatus" class="Combo Combo_TwoCells_width" disabled>
                                                            </select></td>  
                                          </tr>
                                                        </TABLE>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <br></br>
                                        <div id="absenceAccrualGrid" style="width:100%; display:none;">
                                        <div align="center"><table id="absenceAccrualList" class="scroll" cellpadding="0" cellspacing="0" width="100%"></table><div id="absenceAccrualPager" class="scroll" style="text-align: center;"></div></div>
                                      </div>
                                    </TD></TR>
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
<script type="text/javascript">onLoadDo();</script>
<script type="text/javascript" src="../web/js/jquery/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="../web/js/common/select2.min.js"></script>
<script type="text/javascript" src="../web/sa.elm.ob.hcm/js/absenceAccruals/AbsenceAccruals.js"></script>


<script type="text/javascript">
var invalidDateFormat='<%= Resource.getProperty("hcm.entervalid.dateformat", lang) %>';
var contextPath = '<%=request.getContextPath()%>',
employee='<%= Resource.getProperty("hcm.employee", lang) %>',
absenceType='<%= Resource.getProperty("hcm.absencetype", lang) %>';
fromDate='<%= Resource.getProperty("hcm.absence.accrual.calculation.form", lang) %>';
toDate='<%= Resource.getProperty("hcm.absence.accrual.calculation.to", lang) %>';
entitlement='<%= Resource.getProperty("hcm.absence.accrual.entitlement", lang) %>';
leaves='<%= Resource.getProperty("hcm.absence.accrual.leaves", lang) %>';
subtype='<%= Resource.getProperty("hcm.absence.accrual.subType", lang) %>';
hiredate='<%= Resource.getProperty("hcm.hiredate", lang) %>';
category='<%= Resource.getProperty("hcm.empcategory", lang) %>';
netentitlement='<%= Resource.getProperty("hcm.absence.accrual.netentitlement", lang) %>';
department='<%= Resource.getProperty("hcm.department", lang) %>';
var lang ='<%=lang%>',
direction='ltr';
    if(lang=="ar_SA"){
        direction='rtl';
     }
</script>
</HTML>
