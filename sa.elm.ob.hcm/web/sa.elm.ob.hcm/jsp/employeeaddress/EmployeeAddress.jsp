 <%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="sa.elm.ob.utility.util.Utility"%>
<%@page import="java.util.List, java.util.ArrayList"%>
<%@page import="sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO"%>
<%@page import="org.codehaus.jettison.json.JSONObject"%>
<%@page import="org.codehaus.jettison.json.JSONArray"%>
<%@ page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>
  
 <%
    String lang = ((String)session.getAttribute("#AD_LANGUAGE"));

    /*  List<EmployeeVO> inpCountry = (ArrayList<EmployeeVO>) request.getAttribute("inpCountry"); */
     /* List<EmployeeVO> inpCity = (ArrayList<EmployeeVO>) request.getAttribute("inpCity"); */
    /*  List<EmployeeVO> inpAddressStyle = (ArrayList<EmployeeVO>) request.getAttribute("inpAddressStyle"); */

     String style="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css";
     String toolBarStyle="../web/js/common/CommonFormLtr.css";
     if(lang.equals("ar_SA")){
      style="../web/skins/rtl/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP_250.css";
     toolBarStyle="../web/js/common/CommonFormRtl.css";
     }
    
    String DialogBoxStyle="../web/js/common/CommonDialogFormLtr.css";
  
    %>
<HTML xmlns="http://www.w3.org/1999/xhtml">
  <HEAD>
   <META http-equiv="Content-Type" content="text/html; charset=utf-8"></META> 
           <LINK rel="stylesheet" type="text/css" href="<%=style %>" id="paramCSS"></LINK>
    <!-- <link rel="stylesheet" type="text/css" href="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css" id="paramCSS"></link> -->
       <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
   
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
     <link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>" ></link>
        <link rel="stylesheet" type="text/css" href="<%=DialogBoxStyle %>" ></link>
        <link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
         <link rel="stylesheet" type="text/css" href="../web/js/common/select2.min.css"></link>
       
    
    <style type="text/css">
    #CivilPicFile { visibility: hidden; } 
    #WorkPicFile { visibility: hidden; }
    </style>
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
<!--     <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.min_1.11.0.js"></script> 
 -->    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.plugin.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura-ar.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.plus.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.ext.js"></script>
    <script type="text/javascript" src="../web/js/common/DateConverter.js"></script>
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/DateUtils.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="../web/js/jquery/grid.locale-en.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.widget.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.position.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.autocomplete.js"></script>
    <script type="text/javascript" src="../web/sa.elm.ob.hcm/js/EmployeeList.js"></script>
    <script type="text/javascript" src="../web/js/jquery/jquery.jqGrid.min.js"></script>
     <script type="text/javascript" src="../web/js/common/select2.min.js"></script>
    
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
            <%if(request.getAttribute("inpEmployeeIsActive").toString().equals("true")) {%>
        $('#inpStartDate1').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect:function(dates) {enableForm();},showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger" >'});
        $('#inpEndDate1').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect:function() {enableForm();},showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
        $('#inpStartDate2').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect:function() {enableForm();},showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
        <% } %>
    }
    function onResizeDo(){resizeArea();reSizeGrid();}
    function onLoad()
    { 
        <% request.setAttribute("savemsg",request.getAttribute("savemsg")==null?"":request.getAttribute("savemsg")); %>
        <%  if(request.getAttribute("savemsg").equals("Success")) { %>
          displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.saveoreditsuccess", lang) %>");
          <%}%>
         
         
    }

    </script>
</HEAD>
<BODY onresize="onResizeDo();" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" id="paramBody">
 <FORM id="form" method="post" action="" name="frmMain">
   <INPUT type="hidden" name="Command"></INPUT>
   <INPUT type="hidden" name="IsPopUpCall" value="1"></INPUT>
   <INPUT type="hidden" name="inpLastFieldChanged"></INPUT>
   <INPUT type="hidden" name="inpAction" id="inpAction" value=""></INPUT>
   <INPUT type="hidden" name="SubmitType" id="SubmitType" value=""></INPUT>
   <INPUT type="hidden" name="inpAddressId" id="inpAddressId" value="<%= request.getAttribute("inpAddressId") %>"></INPUT>
   <INPUT type="hidden" name="inpEmployeeId" id="inpEmployeeId" value="<%= request.getAttribute("inpEmployeeId") %>"></INPUT>
   <INPUT type="hidden" name="inpempCategory" id="inpempCategory" value="<%= request.getAttribute("inpempCategory") %>"></INPUT>
   <INPUT type="hidden" name="inpHireDate" id="inpHireDate" value="<%= request.getAttribute("inpHireDate") %>"></INPUT>
   <INPUT type="hidden" name="inpActive" id="inpActive" value=""></INPUT>
      <INPUT type="hidden" name="inpprimarychk" id="inpprimarychk" value="<%= request.getAttribute("inpprimarychk") %>"></INPUT>
   
   <INPUT type="hidden" name="inpEmployeeIsActive" id="inpEmployeeIsActive" value="<%= request.getAttribute("inpEmployeeIsActive") %>"></INPUT>
    <INPUT type="hidden" name="inpEmpStatus" id="inpEmpStatus" value="<%= request.getAttribute("inpEmpStatus") %>"></INPUT>
      <INPUT type="hidden" id="inpEmployeeStatus" name="inpEmployeeStatus" value="<%= request.getAttribute("inpEmployeeStatus") %>"></INPUT>
      <INPUT type="hidden" id="inpCancelHiring" name="inpCancelHiring" value="<%= request.getAttribute("CancelHiring") %>"></INPUT>
    <INPUT type="hidden" name="selectCountryId" id="selectCountryId" value="<%= request.getAttribute("inpCountryId") %>"></INPUT>
      <INPUT type="hidden" name="selectCountryName" id="selectCountryName" value="<%= request.getAttribute("inpCountryName") %>"></INPUT>
    <INPUT type="hidden" name="selectCityId" id="selectCityId" value="<%= request.getAttribute("inpCityId") %>"></INPUT>
      <INPUT type="hidden" name="selectCityName" id="selectCityName" value="<%= request.getAttribute("inpCityName") %>"></INPUT>
      
       <INPUT type="hidden" name="selectSecCountryId" id="selectSecCountryId" value="<%= request.getAttribute("inpSecCountryId") %>"></INPUT>
      <INPUT type="hidden" name="selectSecCountryName" id="selectSecCountryName" value="<%= request.getAttribute("inpSecCountryName") %>"></INPUT>
      
    <INPUT type="hidden" name="selectSecCityId" id="selectSecCityId" value="<%= request.getAttribute("inpSecCityId") %>"></INPUT>
      <INPUT type="hidden" name="selectSecCityName" id="selectSecCityName" value="<%= request.getAttribute("inpSecCityName") %>"></INPUT>
       <INPUT type="hidden" name="select2AdstyleId" id="select2AdstyleId" value="<%= request.getAttribute("inpAddressStyleId") %>"></INPUT>
      <INPUT type="hidden" name="select2AdstyleName" id="select2AdstyleName" value="<%= request.getAttribute("inpAddressStyleName") %>"></INPUT>
   
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
                        <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                        <td width="2%" ><a href="javascript:void(0);" onClick="onClickSave(this, 'Save');" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Save Record';return true;" onMouseOut="window.status='';return true;" id="linkButtonSave"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.save", lang)  %>" border="0" id="buttonSave"></a></td>  
                        <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                        <td width="2%" ><a href="javascript:void(0);" onClick="onClickRefresh()" class="Main_ToolBar_Button" onMouseOver="window.status='Reload Current Page';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonRefresh"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Refresh" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.reload",lang) %>" border="0" id="linkButtonRefresh"></a></td> 
                        <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                        <td class="Main_ToolBar_Space"></td>
                    </tr>
                 </table>
             </DIV>
             <TABLE width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_TabPane" id="tdtopTabs">
             <TR id="paramParentTabContainer"><td class="tabBackGroundInit"></td></TR>
             <TR id="paramMainTabContainer">
                <TD class="tabBackGroundInit"><span style="background: none;" class="tabTitle"></span></TD>
             </TR>
             <TR>
                <TD class="tabBackGround">
                    <div class="marginLeft">
                        <div>
                            <span class="dojoTabcurrentfirst">
                                <div>
                                    <span>
                                        <a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%= Resource.getProperty("hcm.empaddress",lang)%>
                                        </a>
                                    </span>
                                </div>
                            </span>
                        </div>
                    </div>
                </TD>
             </TR>
            </TABLE>
            
               <DIV class="Main_ContentPane_Client" style="overflow: auto;width: 100%" id="client">
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
                        <table><tbody>
                        <tr id="TabList">
                        <td><div id="TabEmployee" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMP');">
                        <span class="LabelText"><%=Resource.getProperty("hcm.employee",lang)%></span></div>
                        </td>
                        <td><img id="ImgEmpInfo" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabEmpInfo" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPINF');"><span class="LabelText"><%= Resource.getProperty("hcm.empmntinfo",lang)%></span></div></td>
                        <td><img id="ImgEmpAddress" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabEmpAddress" class="tabCurrent" ><span class="LabelText"><%= Resource.getProperty("hcm.empaddress",lang)%></span></div>
                        <div style="text-align: center;"><img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img></div>
                        </td>
                        <td><img  id="ImgqualInfo" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabqualInfo"  class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPQUAL');"><span class="LabelText"><%= Resource.getProperty("hcm.empqualinfo",lang)%></span></div></td> 
                         <td>
                            <img  id="ImgDependent" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img>
                        </td>
                        <td>
                            <div id="TabDependent" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('Dependent');">
                                <span class="LabelText"><%= Resource.getProperty("hcm.dependent",lang)%>
                                    </span>
                            </div>
                            <div style="text-align: center;display:none;">
                                <img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img>
                            </div>
                        </td>  
                         <td><img  id="ImgEmpInfo" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                         <td><div id="Tabpreemp" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('PREEMP');" ><span class="LabelText"><%= Resource.getProperty("hcm.emppreemp",lang)%></span></div></td> 
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
                        <td><img  id="ImgAsset" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabAsset" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('Asset');"><span class="LabelText"><%= Resource.getProperty("hcm.Facilities&Assets",lang)%></span></div></td>
                        <td><img  id="ImgDocument" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabDocument" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('DOC');"><span class="LabelText"><%= Resource.getProperty("hcm.documents",lang)%></span></div></td>
                        
                         <td><img  id="ImgEmpPerPayMethod" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                   <td>
                                     <div id="TabEmpPerPayMethod" class="tabSelected" onclick="reloadWindow('PERPAYMETHOD')">
                                     <span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></span>
                                     </div>
                                    </td>
                        </tr>
                        </tbody></table>
                        <div style="margin-bottom: 5px;"></div>
                       </div>
                            <div style="width:100%;" id="FormDetails" align="center">
                                                <div align="center">
                                                <TABLE style="width:100%; margin-top: 10px;">
                                                    <TR>
                                                        <TD class="TextBox_ContentCell" align="right" style="padding-left: 80px;">
                                                            <input type="text" name="inpName1" id="inpName1" maxlength="59" value="<%= Utility.escapeQuote(request.getAttribute("inpName1")) %>" required="true" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);" dir="rtl"></input>
                                                            <input type="hidden" name="inpHidName1" id="inpHidName1" value="<%= Utility.escapeQuote(request.getAttribute("inpName1")) %>"></input>
                                                        </TD>
                                                        <TD class="TitleCell" align="left" style="min-width:10px;"><span class="LabelText" ><%= Resource.getProperty("hcm.arabicname", lang)%></span></TD>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.fullname", lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell">
                                                            <input type="text" name="inpName2" id="inpName2" maxlength="59" value="<%= Utility.escapeQuote(request.getAttribute("inpName2")) %>" required="true" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);"></input>
                                                            <input type="hidden" name="inpHidName2" id="inpHidName2" value="<%= Utility.escapeQuote(request.getAttribute("inpName2")) %>"></input>
                                                        </TD>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.code", lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell">
                                                            <input type="text" name="inpEmpNo" id="inpEmpNo" maxlength="59" value="<%= Utility.escapeQuote(request.getAttribute("inpEmpNo")) %>" required="true" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);"></input>
                                                            <input type="hidden" name="inpHidEmpNo" id="inpHidEmpNo" value="<%= Utility.escapeQuote(request.getAttribute("inpEmpNo")) %>"></input>
                                                        </TD>
                                                    </TR>
                                                </TABLE>
                                                </div>
                                            </div>
                    <div id="LoadingContent" style="display:none;">
                       <div style="text-align: center;margin-top: 17%;"><img alt="Loading" src="../web/images/loading.gif"></div>
                       <div style="color:#000000;font-family: sans-serif;font-size:14px;margin-top:10px;text-align: center;"><%= Resource.getProperty("hcm.loading", lang)%>... </div>
                    </div>
                    <div id ="primaryadd">  
                        <div id="primaryaddgrp">
                            <TABLE style="width:80%; margin-top: 10px;">
                                <TR>
                                    <TD>
                                        <table style="width: 100%;padding:0px;">
                                            <tr>
                                                <td>
                                                    <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                        <TBODY>
                                                        <TR class="FieldGroup_TopMargin"></TR>
                                                            <TR>
                                                                <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.primaryaddress",lang)%></TD>
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
                     </div>
                    <TABLE style=" margin-top: 10px;">
                        <TR>
                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.addressstyle",lang)%></span></TD>
                            <TD class="TextBox_ContentCell">
                                <SELECT id="inpAddressStyle" name="inpAddressStyle" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();">
                                <%-- <%if(inpAddressStyle !=null && inpAddressStyle.size()>0) { %>
                                    <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option>
                                    <% for(EmployeeVO vo:inpAddressStyle){%>
                                        <option value='<%= vo.getAddressStyleId()%>' <%if(request.getAttribute("inpAddressStyleId")!=null &&  request.getAttribute("inpAddressStyleId").equals(vo.getAddressStyleId())){%> selected<%} %>  ><span><%= vo.getAddressStyleName()%></span></option>
                                    <%}}%> --%>
                                </SELECT>
                            </TD>
                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.primary",lang)%></span></TD>
                            <TD class="TextBox_ContentCell" colspan=3>
                                <input type="checkbox" id="primarychk" name="primarychk" <%if(request.getAttribute("inpprimarychk").equals("Y")){%> checked <%} %> onclick="disableprimaryflag(this.value);enableForm();"></input>
                            </TD> 
                            
                            
                        </TR>
                        <TR>
                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.startdate",lang)%></span></TD>
                            <TD class="TextBox_btn_ContentCell">
                                <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                    <tr>
                                        <td class="TextBox_ContentCell">
                                            <%if(request.getAttribute("inpStartDate1")!= null ) {%>
                                            <input type="text" id="inpStartDate1"  onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"  class="dojoValidateValid TextBox_btn_OneCell_width required" value="<%= request.getAttribute("inpStartDate1") %>" maxlength="10" name="inpStartDate1" ></input>
                                            <%}else{ %>
                                             <input type="text" id="inpStartDate1" class="dojoValidateValid TextBox_btn_OneCell_width required" onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("today") %>" maxlength="10" name="inpStartDate1" ></input>
                                            <%} %> 
                                       </td>
                                   </tr>
                               </table>
                           </TD>
                           <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.enddate",lang)%></span></TD>
                           <TD class="TextBox_btn_ContentCell">
                                <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                    <tr>
                                        <td class="TextBox_ContentCell">
                                        <%if(request.getAttribute("inpEndDate1")!= null ) {%>
                                        <input type="text" id="inpEndDate1" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"  class="dojoValidateValid TextBox_btn_OneCell_width" value="<%= request.getAttribute("inpEndDate1") %>" maxlength="10" name="inpEndDate1" ></input>
                                         <%}else{ %>
                                        <input type="text" id="inpEndDate1" class="dojoValidateValid TextBox_btn_OneCell_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="" maxlength="10" name="inpEndDate1" ></input>
                                         <%} %> 
                                         </td>
                                    </tr>
                                </table>
                           </TD>                                             
                      </TR>
                                                     <TR>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.country",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" >
                                                            <SELECT id="inpCountry" name="inpCountry" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); changecountry(this);">
                                                             <%--  <%if(inpCountry!=null && inpCountry.size()>0) { %>
                                                                <option value="0" ><%= Resource.getProperty("hcm.select", lang) %></option>
                                                                <% for(EmployeeVO vo:inpCountry){%>
                                                                <option value='<%= vo.getCountryId()%>' <%if(request.getAttribute("inpCountryId")!=null &&  request.getAttribute("inpCountryId").equals(vo.getCountryId())){%> selected<%}else if (vo.getIsdefault().equals("Y")){%> selected<%}  %>  ><span><%= vo.getCountryName()%></span></option>
                                                                <%}}%> --%>
                                                            </SELECT>
                                                       </TD>
                                                         <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.city",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" >
                                                             <SELECT id="inpCity" name="inpCity" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();">
                                                             <%--  <%
                                                               if(inpCity!=null && inpCity.size()>0) { %>
                                                                <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option>
                                                                <% for(EmployeeVO vo:inpCity){%>
                                                                <option value='<%= vo.getCityId()%>' <%if(request.getAttribute("inpCityId")!=null &&  request.getAttribute("inpCityId").equals(vo.getCityId())){%> selected<%}else if (vo.getIsdefault().equals("Y")){%> selected<%} %>  ><span><%= vo.getCityName()%></span></option>
                                                                <%}}%> --%>
                                                            </SELECT> 
                                                        </TD>
                                                    </TR>  
                                                    <TR>
                                                      
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.district",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell">
                                                         <%if(request.getAttribute("inpDistrict")!= null ) {%>
                                                            <input type="text" id="inpDistrict" class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpDistrict") %>" maxlength="32" name="inpDistrict" ></input> 
                                                         <%}else{ %>
                                                            <input type="text" name="inpDistrict" maxlength="32" id="inpDistrict" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); " value=""  class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);" ></input>
                                                            <%} %> 
                                                        </TD>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.street",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell">
                                                         <%if(request.getAttribute("inpStreet")!= null ) {%>
                                                            <input type="text" id="inpStreet" class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpStreet") %>" maxlength="32" name="inpStreet" ></input> 
                                                          <%}else{ %>
                                                            <input type="text" name="inpStreet" maxlength="32" id="inpStreet"  value=""  onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); " class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);" ></input>
                                                          <%} %>
                                                        </TD>
                                                    </TR> 
                                                    <TR>
                                                          <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.address1",lang)%></span></TD>
                                                          <TD class="TextBox_ContentCell">
                                                            <%if(request.getAttribute("inpAdd1")!= null ) {%>
                                                                <input type="text" id="inpAdd1" class="dojoValidateValid TextBox_TwoCells_width required" onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpAdd1") %>" maxlength="32" name="inpAdd1" ></input> 
                                                            <%}else{ %>
                                                                <input type="text" name="inpAdd1" maxlength="32" id="inpAdd1"  value=""  class="dojoValidateValid TextBox_TwoCells_width required"  onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); " onkeydown="   return onChangeEvent(event);" ></input>
                                                            <%} %>
                                                          </TD>
                                                          <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.postbox",lang)%></span></TD>
                                                          <TD class="TextBox_ContentCell" colspan="3">
                                                            <%if(request.getAttribute("inpPostBox")!= null ) {%>
                                                                <input type="text" id="inpPostBox" class="dojoValidateValid TextBox_TwoCells_width required" onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpPostBox") %>" maxlength="32" name="inpPostBox" ></input> 
                                                            <%}else{ %>
                                                                <input type="text" name="inpPostBox" maxlength="32" id="inpPostBox" value=""  class="dojoValidateValid TextBox_TwoCells_width required" onkeydown=" enableForm();  return onChangeEvent(event);"></input>
                                                            <%}%>
                                                          </TD> 
                                                    </TR>  
                                                    <TR>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.address2",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell">
                                                            <%if(request.getAttribute("inpAdd2")!= null ) {%>
                                                                <input type="text" id="inpAdd2" class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpAdd2") %>" maxlength="32" name="inpAdd2" ></input> 
                                                            <%}else{ %>
                                                                <input type="text" name="inpAdd2" id="inpAdd2" maxlength="32" value=""  class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();  return onChangeEvent(event);" ></input>
                                                            <%}%>
                                                        </TD> 
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.postalcode",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" >
                                                            <%if(request.getAttribute("inpPostalcode")!= null ) {%>
                                                                <input type="text" name="inpPostalcode" maxlength="32" id="inpPostalcode"  value="<%= request.getAttribute("inpPostalcode") %>"  class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);enableForm();" ></input>
                                                            <%}else{ %>
                                                                <input type="text" name="inpPostalcode" maxlength="32" id="inpPostalcode" value=""  class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();  return onChangeEvent(event);" ></input>
                                                            <%}%>
                                                        </TD>
                                                   </TR>
                                            </TABLE> 
                    
                                             </div>
                                                <div id="secondaryadd">
                                                    <DIV id="secondaryaddgrp">
                                                        <TABLE style="width:80%; margin-top: 10px;">
                                                            <TR>
                                                                <TD>
                                                                    <table style="width: 100%;padding:0px;">
                                                                    <tr>
                                                                    <td>
                                                                    <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                                    <TBODY>
                                                                    <TR class="FieldGroup_TopMargin"></TR>
                                                                    <TR>
                                                                        <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                        <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.secondaryaddress",lang)%></TD>
                                                                        <TD class="FieldGroupTitle_Right"><IMG class="FieldGroupTitle_Right_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                        <TD class="FieldGroupContent"></TD>
                                                                    </TR>    
                                                                    <TR class="FieldGroup_BottomMargin" style="display: none;"></TR>          
                                                                        </TBODY>
                                                                    </TABLE></td></tr>
                                                          </table>
                                                          </TD></TR></TABLE></DIV>
                                                                <TABLE  style=" margin-top: 10px;">
                                                          <TR>
                                                         <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.startdate",lang)%></span></TD>
                                                         <TD class="TextBox_btn_ContentCell">
                                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                                <tr>
                                                                    <td class="TextBox_ContentCell">
                                                                     <%  
                                                                       if(request.getAttribute("inpStartDate2")!= null ) {%>
                                                                       <input type="text" id="inpStartDate2" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"  class="dojoValidateValid TextBox_btn_OneCell_width required" value="<%= request.getAttribute("inpStartDate2") %>" maxlength="10" name="inpStartDate2" ></input>
                                                                        <%}
                                                                       else{ %>
                                                                       <input type="text" id="inpStartDate2" class="dojoValidateValid TextBox_btn_OneCell_width required" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("today") %>" maxlength="10" name="inpStartDate2" ></input>
                                                                       <%} %> 
                                                                    </td>
                                                                </tr>
                                                             </table>
                                                         </TD>
                                                          <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.enddate",lang)%></span></TD>
                                                          <TD class="TextBox_btn_ContentCell">
                                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                                <tr>
                                                                    <td class="TextBox_ContentCell">
                                                                     <%  
                                                                       if(request.getAttribute("inpEndDate2")!= null ) {%>
                                                                    
                                                                       <input type="text" id="inpEndDate2" class="dojoValidateValid TextBox_btn_OneCell_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpEndDate2") %>" readonly  name="inpEndDate2" ></input>
                                                                        <%}else{ %>
                                                                       <input type="text" id="inpEndDate2" class="dojoValidateValid TextBox_btn_OneCell_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="" readonly name="inpEndDate2" ></input>
                                                                        
                                                                        <%} %>
                                                                    </td>
                                                                </tr>
                                                             </table>
                                                         </TD> 
                                                        <TD class="TitleCell" align="right" ><span class="LabelText" ><%= Resource.getProperty("hcm.active",lang)%></span></TD>
                                                          <TD class="TextBox_ContentCell">
                                                             <input type="checkbox" id="inpActiveflag" name="inpActiveflag"   <%if(request.getAttribute("inpActiveflag").equals("Y")){%> checked <%} %> onclick="disableactiveflag(this.value);enableForm();"></input>
                                                         </TD>  
                                                  
                                                   </TR>
                                                    <TR>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.country",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" >
                                                            <SELECT id="inpSecCountry" name="inpSecCountry" class="Combo Combo_TwoCells_width Combo_focus" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); changeseccountry(this);">
                                                            <%--   <%  if (inpCountry!=null && inpCountry.size()>0) { %>
                                                               
                                                               <option value="0" <%if(request.getAttribute("inpSecCountry")!=null &&  request.getAttribute("inpSecCountry").equals("0")){%> selected<%}%>  ><%= Resource.getProperty("hcm.select", lang) %></option>
                                                              
                                                                <% for(EmployeeVO vo:inpCountry){%>
                                                                
                                                                <option value='<%= vo.getCountryId()%>' <%if(request.getAttribute("inpSecCountry")!=null &&  request.getAttribute("inpSecCountry").equals(vo.getCountryId())){%> selected<%}else if (request.getAttribute("inpSecCountry")=="" && vo.getIsdefault().equals("Y")){%> selected<%}%>  ><span><%= vo.getCountryName()%></span></option>
                                                                <%}}%> --%>
                                                            </SELECT>
                                                       </TD>
                                                         <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.city",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" >
                                                            <SELECT id="inpSecCity" name="inpSecCity" class="Combo Combo_TwoCells_width Combo_focus" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();">
                                                             <%--  <% if(inpCity!=null && inpCity.size()>0) { %>
                                                                <option value="0" <%if(request.getAttribute("inpSecCity")!=null &&  request.getAttribute("inpSecCity").equals("0")){%> selected<%}%> ><%= Resource.getProperty("hcm.select", lang) %></option>
                                                                <% for(EmployeeVO vo:inpCity){%>
                                                                <option value='<%= vo.getCityId()%>' <%if(request.getAttribute("inpSecCity")!=null &&  request.getAttribute("inpSecCity").equals(vo.getCityId())){%> selected<%}else if (request.getAttribute("inpSecCity")=="" && vo.getIsdefault().equals("Y")){%> selected<%} %>  ><span><%= vo.getCityName()%></span></option>
                                                                <%}}%> --%>
                                                            </SELECT>
                                                        </TD>
                                                    </TR> 
                                                    <TR>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.district",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell">
                                                          <%if(request.getAttribute("inpSecDistrict")!= null ) {%>
                                                            <input type="text" name="inpSecDistrict" maxlength="32" id="inpSecDistrict" class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpSecDistrict") %>"  ></input> 
                                                         <%}else{ %>
                                                            <input type="text" name="inpSecDistrict" maxlength="32" id="inpSecDistrict"  value=""  class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);" ></input>
                                                            <%} %>
                                                        </TD>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.street",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell">
                                                            <%if(request.getAttribute("inpSecStreet")!= null ) {%>
                                                                <input type="text" id="inpSecStreet" name="inpSecStreet" class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpSecStreet") %>" maxlength="32" name="inpStreet" ></input> 
                                                            <%}else{ %>
                                                                <input type="text" name="inpSecStreet" maxlength="32" id="inpSecStreet"  value=""  class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);" ></input>
                                                            <%} %>                                                        
                                                        </TD>
                                                   </TR>  
                                                   <TR>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.address1",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell">
                                                            <%if(request.getAttribute("inpSecAdd1")!= null ) {%>
                                                                <input type="text" id="inpSecAdd1" class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpSecAdd1") %>" maxlength="32" name="inpSecAdd1" ></input> 
                                                            <%}else{ %>
                                                                <input type="text" name="inpSecAdd1" maxlength="32" id="inpAdd1"  value=""  class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();  return onChangeEvent(event);" ></input>
                                                            <%} %>                                                         
                                                         </TD>
                                                         <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.postbox",lang)%></span></TD>
                                                          <TD class="TextBox_ContentCell" colspan="3">
                                                            <%if(request.getAttribute("inpSecPostbox")!= null ) {%>
                                                                <input type="text" id="inpSecPostbox" class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpSecPostbox") %>" maxlength="32" name="inpSecPostbox" ></input> 
                                                            <%}else{ %>
                                                                <input type="text" name="inpSecPostbox" maxlength="32" id="inpSecPostbox" value=""  class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();  return onChangeEvent(event);"></input>
                                                            <%}%>                                                          
                                                          </TD> 
                                                    </TR>  
                                                    <TR>
                                                      <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.address2",lang)%></span></TD>
                                                      <TD class="TextBox_ContentCell">
                                                          <%if(request.getAttribute("inpSecAdd2")!= null ) {%>
                                                                <input type="text" id="inpSecAdd2" class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpSecAdd2") %>" maxlength="32" name="inpSecAdd2" ></input> 
                                                            <%}else{ %>
                                                                <input type="text" name="inpSecAdd2" maxlength="32" id="inpSecAdd2"  value=""  class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();  return onChangeEvent(event);" ></input>
                                                            <%}%>
                                                          </TD> 
                                                      <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.postalcode",lang)%></span></TD>
                                                      <TD class="TextBox_ContentCell" >
                                                          <%if(request.getAttribute("inpSecPostalcode")!= null ) {%>
                                                            <input type="text" name="inpSecPostalcode" maxlength="32" id="inpSecPostalcode"  value="<%= request.getAttribute("inpSecPostalcode") %>"  class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event); enableForm();" ></input>
                                                          <%}else{ %>
                                                            <input type="text" name="inpSecPostalcode" maxlength="32" id="inpSecPostalcode" value=""  class="dojoValidateValid TextBox_TwoCells_width" onkeydown=" enableForm();  return onChangeEvent(event);" ></input>
                                                          <%}%>
                                                      </TD>
                                                   </TR>
                    </TABLE>
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
<script type="text/javascript">
var contextPath = '<%=request.getContextPath()%>', currentWindow='EMP', currentTab = 'EMPADD';

 function reSizeGrid() {
    if (window.innerWidth) {
        gridW = window.innerWidth - 52;
        gridH = window.innerHeight - 241;
    }
    else if (document.body) {
        gridW = document.body.clientWidth - 52;
        gridH = document.body.clientHeight - 241;
    }
    if (gridW < 800)
        gridW = 800;
    if (gridH < 200)
        gridH = 200;

} 
var changesFlag = 0;


  function reloadTabSave(tab) {
    if (savevaliddata()){
        document.getElementById("SubmitType").value = "Save";
        var url = "&inpNextTab=" + tab;
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?' + url, '_self', null, true);
        return false;
    }
}  

 function reloadWindow(tab) {
    //hideMessage();
    if (changesFlag == 1 && savevaliddata()) OBAsk(changedvaluessave, function (result) {
      if (result) reloadTabSave(tab);
      else {
        reloadTab(tab);
      }
    });
    else reloadTab(tab);
  }
 //  Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Refresh
function enableSaveButton(flag) {
    if (flag == 'true' ) {
        document.getElementById('buttonSave').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save';
        $('#linkButtonSave_Relation, #linkButtonSave_New, #linkButtonSave').attr("class", "Main_ToolBar_Button");
        
        
    }
    else if (flag == 'false') {
        document.getElementById('buttonSave').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_disabled';
        $('#linkButtonSave_Relation, #linkButtonSave_New, #linkButtonSave').attr("class", "Main_ToolBar_Button_disabled");
    }
}
function enableForm() {
    changesFlag = 1;
    hideMessage();
    if(( document.getElementById('inpAddressStyle').value !=0 )  &&  ( document.getElementById('inpStartDate1').value !="")  && (document.getElementById('inpCountry').value !=0) && (document.getElementById('inpCity').value !=0)
            && ( document.getElementById('inpAdd1').value !="" ) && ( document.getElementById('inpPostBox').value !="" ) && ( document.getElementById('inpStartDate2').value !="")){
          if(document.getElementById('inpCancelHiring').value == "true" )
         {
          document.getElementById('linkButtonSave').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_disabled';
          enableSaveButton("false");
         }
          else
              {
          enableSaveButton("true");
              }
    }
    else{
         enableSaveButton("false");
    }
}


function onClickRefresh() {
    hideMessage();
    document.getElementById("inpAction").value = "EditView";
    if (changesFlag == 1 &&  savevaliddata() && (( document.getElementById('inpAddressStyle').value !=0 )  &&  ( document.getElementById('inpStartDate1').value !="")  && (document.getElementById('inpCountry').value !=0) && (document.getElementById('inpCity').value !=0)
            && ( document.getElementById('inpAdd1').value !="" ) && ( document.getElementById('inpPostBox').value !="" ) && ( document.getElementById('inpStartDate2').value !="")))
        OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
            if (result){
                document.getElementById("SubmitType").value="Save";
                   document.getElementById("inpAction").value = "EditView";
               reloadTab(currentTab);
           }
           else {
                document.getElementById("inpAction").value = "EditView";
               reloadTab(currentTab);
           }
        });
    else
        reloadTab('EMPADD');
}

function onClickSave(index, type) {
    if (index.className != 'Main_ToolBar_Button')
        return false;
    if (changesFlag == 1 &&  savevaliddata()) {
        if (type == "New") {
            showProcessBar(true, 2);
            document.getElementById("inpAction").value = "EditView";
            document.getElementById("SubmitType").value = "SaveNew";
        }
        if (type == "Save") {
            document.getElementById("inpAction").value = "EditView";
            document.getElementById("SubmitType").value = "Save";
            
        }
        reloadTab('EMPADD');
    }
}


function hideMessage() {
    $("#messageBoxID").hide();
}
function reloadTab(tab) {
    if (tab == 'EMP') {
         var url="";
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView' + url;
        document.frmMain.submit();
    }
    else if (tab == 'EMPINF') {
        var url="";
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView' + url;
        document.frmMain.submit();
    }
    else if(tab == 'EMPADD') {
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView';
        document.frmMain.submit();

    }
    else if (tab == 'EMPQUAL') {
        var url="";
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification?inpAction=GridView' + url;
        document.frmMain.submit();
    }
    else if (tab == 'Dependent') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents', '_self', null, true);
        return false;
    }
    else if(tab == 'Asset'){ 
        submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.asset.header/Asset', '_self', null, true);
        return false;
   }
    else if (tab == 'PREEMP') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
         return false;
    }
    else if (tab == 'EMPCTRCT') {
        var url="";
          var employeeId=document.getElementById("inpEmployeeId").value;
          document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
          document.frmMain.submit();
     }
    else if (tab == 'DOC') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.documents.header/Documents', '_self', null, true);
         return false;
    }
    else if (tab == 'PERPAYMETHOD') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.header/PersonalPaymentMethod', '_self', null, true);
         return false;
    }
    else if (tab == 'MEDIN') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.MedicalInsurance.header/MedicalInsurance', '_self', null, true);
         return false;
    }
}

function changecountry(index) {
       document.getElementById("inpCity").options.length = 0;

    setTimeout(function () {
        $("#inpCity").select2(selectBoxAjaxPaging({
            url :    function() {  return      '<%=request.getContextPath()%>/EmployeeAjax?action=GetCity&inpCountry='+index.value
            },
           
            
            
          size : "small"
        }))
        $("#inpCity").on("select2:unselecting", function (e) {
            document.getElementById("inpCity").options.length = 0;
          });
    }, 100);
}
function changeseccountry(index){
       document.getElementById("inpSecCity").options.length = 0;
        setTimeout(function () {
        $("#inpSecCity").select2(selectBoxAjaxPaging({
            url :    function() {  return      '<%=request.getContextPath()%>/EmployeeAjax?action=GetCity&inpCountry='+index.value
            },
           
            
            
          size : "small"
        }))
        $("#inpSecCity").on("select2:unselecting", function (e) {
            document.getElementById("inpSecCity").options.length = 0;
          });
        document.getElementById("select2-inpSecCity-container").style.backgroundColor="#F5F7F1";
    }, 100); 
}
 function savevaliddata(){
      var startdate=document.getElementById("inpStartDate1").value;
      var enddate = document.getElementById("inpEndDate1").value;
      var hiredate = document.getElementById("inpHireDate").value;
      var validdate=datevalidation(startdate,enddate);
      var validhiredate=checkvalidstartdate(startdate,hiredate);
      var mandatory = '0';
      var chkCancel = '0';
      
      //validating mandatory fields
      var addressstyle = document.getElementById("inpAddressStyle").value;
      var address1 = document.getElementById("inpAdd1").value;
      var postbox = document.getElementById("inpPostBox").value;
      var country = document.getElementById("inpCountry").value;
      var city= document.getElementById("inpCity").value;
      
      if(addressstyle == '' || address1 == "" || postbox == "" || startdate == "" || country == '' || city == ''){
          mandatory = '1';
      }
       if(document.getElementById('inpCancelHiring').value == "true" )
      {
          chkCancel = '1';
      }
      if(validdate == '1' || validhiredate=='1' || mandatory =='1' || chkCancel =='1'){
            return false;
      }
     
      else{
          return true;
      }
} 
function disableactiveflag(index){
    document.getElementById("inpActive").value=document.getElementById("inpActiveflag").checked;
    if(document.getElementById("inpActiveflag").checked == true){
         document.getElementById("inpEndDate2").value="";
    }
    else if(document.getElementById("inpActiveflag").checked == false){
        document.getElementById("inpEndDate2").value="<%= request.getAttribute("today") %>"
        document.getElementById("inpEndDate2").readOnly= true;
    } 
}
function disableprimaryflag(index){
    document.getElementById("inpprimarychk").value=document.getElementById("primarychk").checked;
  
}

function checkvalidstartdate(startdate,hiredate) 
{   
    var fdate = startdate.split("-");
    var fromdate=new Date();
         
    fromdate.setDate(fdate[0]);
    fromdate.setMonth(fdate[1]-1);
    fromdate.setFullYear(fdate[2]);

     
    var tdte = hiredate.split("-");
    var todate=new Date();
         
    todate.setDate(tdte[0]);
    todate.setMonth(tdte[1]-1);
    todate.setFullYear(tdte[2]);
    
 
    if (todate.getTime()> fromdate.getTime() )
    {

        OBAlert("<%=Resource.getProperty("hcm.validstartdate",lang)%>");
         return 1;
    } 
    else
        {
        return 0;
        }
}

//check date validation
function datevalidation(startValue,endValue) 
{   
    
    if(endValue.length!='0')
    {
    var fdate = startValue.split("-");
    var fromdate=new Date();
         
    fromdate.setDate(fdate[0]);
    fromdate.setMonth(fdate[1]-1);
    fromdate.setFullYear(fdate[2]);

     
    var tdte = endValue.split("-");
    var todate=new Date();
         
    todate.setDate(tdte[0]);
    todate.setMonth(tdte[1]-1);
    todate.setFullYear(tdte[2]);
    
 
    if (fromdate.getTime() >= todate.getTime())
    {

        OBAlert("<%=Resource.getProperty("hcm.datevalidation",lang)%>");
         return 1;
    } 
    else
        {
        return 0;
        }
    }
    else
    {
        return 0;
        }
}
var changedvaluessave = '<%= Resource.getProperty("hcm.changedvaluessave", lang) %>';
//primary Country
setTimeout(function () {
    $("#inpCountry").select2(selectBoxAjaxPaging({
        url :   function() {  return           '<%=request.getContextPath()%>/EmployeeAjax?action=getCountry'
        },
       
        
        
      size : "small"
    }));
     $("#inpCountry").on("select2:unselecting", function (e) {
        document.getElementById("inpCountry").options.length = 0;
      }); 
}, 100); 
if(document.getElementById("selectCountryId").value!=null && document.getElementById("selectCountryId").value!=""){
    var data = [{
                    id: document.getElementById("selectCountryId").value,
                    text: document.getElementById("selectCountryName").value
                  }];
                  $("#inpCountry").select2({
                    data: data
                  });
                  changecountry(document.getElementById("selectCountryId"));
                   if(document.getElementById("selectCityId").value!=null && document.getElementById("selectCityId").value!=""){

                      var data = [{
                                      id: document.getElementById("selectCityId").value,
                                      text: document.getElementById("selectCityName").value
                                    }];
                                    $("#inpCity").select2({
                                      data: data
                                    });
                                    } 
                  } 
//secondary Country
setTimeout(function () {
    $("#inpSecCountry").select2(selectBoxAjaxPaging({
        url :   function() {  return           '<%=request.getContextPath()%>/EmployeeAjax?action=getCountry'
        },
       
        
        
      size : "small"
    }));
     $("#inpSecCountry").on("select2:unselecting", function (e) {
        document.getElementById("inpSecCountry").options.length = 0;
      }); 
     document.getElementById("select2-inpSecCountry-container").style.backgroundColor="#F5F7F1";
}, 100); 
if(document.getElementById("selectSecCountryId").value!=null && document.getElementById("selectSecCountryId").value!=""){
    var data = [{
                    id: document.getElementById("selectSecCountryId").value,
                    text: document.getElementById("selectSecCountryName").value
                  }];
                  $("#inpSecCountry").select2({
                    data: data
                  });
                  changeseccountry(document.getElementById("selectSecCountryId"));
                  if(document.getElementById("selectSecCityId").value!=null && document.getElementById("selectSecCityId").value!=""){

                      var data = [{
                                      id: document.getElementById("selectSecCityId").value,
                                      text: document.getElementById("selectSecCityName").value
                                    }];
                                    $("#inpSecCity").select2({
                                      data: data
                                    });
                                    }
                  } 
//Address Style
setTimeout(function () {
    $("#inpAddressStyle").select2(selectBoxAjaxPaging({
        url :   function() {  return           '<%=request.getContextPath()%>/EmployeeAjax?action=getAddressStyle'
        },
       
        
        
      size : "small"
    }));
     $("#inpAddressStyle").on("select2:unselecting", function (e) {
        document.getElementById("inpAddressStyle").options.length = 0;
      }); 
}, 100); 
if(document.getElementById("select2AdstyleId").value!=null && document.getElementById("select2AdstyleId").value!=""){
    var data = [{
                    id: document.getElementById("select2AdstyleId").value,
                    text: document.getElementById("select2AdstyleName").value
                  }];
                  $("#inpAddressStyle").select2({
                    data: data
                  });
}
$( document ).ready(function() {
    if($('#inpEmployeeIsActive').val()=="false"){
    $("#inpAddressStyle").prop("disabled", true);
    $("#inpCountry").prop("disabled", true);
    $("#inpCity").prop("disabled", true);
    $("#inpSecCountry").prop("disabled", true);
    $("#inpSecCity").prop("disabled", true);
    document.getElementById("primarychk").setAttribute("disabled", "true");
    document.getElementById("inpActiveflag").setAttribute("disabled", "true");
    document.getElementById("inpStartDate1").setAttribute("disabled", "true");
    document.getElementById("inpEndDate1").setAttribute("disabled", "true");
    document.getElementById("inpDistrict").setAttribute("disabled", "true");
    document.getElementById("inpSecDistrict").setAttribute("disabled", "true");
    document.getElementById("inpStreet").setAttribute("disabled", "true");
    document.getElementById("inpAdd1").setAttribute("disabled", "true");
    document.getElementById("inpPostBox").setAttribute("disabled", "true");
    document.getElementById("inpAdd2").setAttribute("disabled", "true");
    document.getElementById("inpPostalcode").setAttribute("disabled", "true");
    document.getElementById("inpSecStreet").setAttribute("disabled", "true");
    document.getElementById("inpSecAdd1").setAttribute("disabled", "true");
    document.getElementById("inpSecAdd2").setAttribute("disabled", "true");
    document.getElementById("inpSecPostbox").setAttribute("disabled", "true");
    document.getElementById("inpSecPostalcode").setAttribute("disabled", "true");
    document.getElementById("inpStartDate2").setAttribute("disabled", "true");
    $('#inpStartDate2').prop('readonly',true); 
    $('#inpEndDate1').prop('readonly',true); 
    $('#inpStartDate1').prop('readonly',true); 
    }
});
</script>
</HTML>