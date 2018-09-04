<!--
 *************************************************************************
 * All Rights Reserved.
 * 
 *************************************************************************
-->
 <%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="sa.elm.ob.utility.util.Utility"%>
<%@page import="java.util.List, java.util.ArrayList"%>
<%@page import="sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO"%>
<%@page import="org.codehaus.jettison.json.JSONObject"%>
<%@page import="org.codehaus.jettison.json.JSONArray"%>
<%@ page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>

 <%
    String lang = ((String)session.getAttribute("#AD_LANGUAGE"));

    String style="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css";
    String toolBarStyle="../web/js/common/CommonFormLtr.css";
    if(lang.equals("ar_SA")){
     style="../web/skins/rtl/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP_250.css";
    toolBarStyle="../web/js/common/CommonFormRtl.css";
    }
    %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
    <fmt:setBundle basename="sa.elm.ob.hcm.properties.applicationresources"/>
<HTML xmlns="http://www.w3.org/1999/xhtml">
  <HEAD>
   <META http-equiv="Content-Type" content="text/html; charset=utf-8"></META> 
  <LINK rel="stylesheet" type="text/css" href="<%=style %>" id="paramCSS"></LINK>
  <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/ui.jqgrid.css"></link>
    <link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
    <script type="text/javascript" src="../web/js/common/common.js"></script>
    <script type="text/javascript" src="../web/js/utils.js"></script>
    <script type="text/javascript" src="../web/js/windowKeyboard.js"></script>
    <style type="text/css">
    </style>
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
    }
    function onResizeDo(){resizeArea();reSizeGrid();}
    function onLoad()
    {
         <% request.setAttribute("savemsg",request.getAttribute("savemsg")==null?"":request.getAttribute("savemsg")); %>
         <%  if(request.getAttribute("savemsg").equals("Success")) { %>
           displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.saveoreditsuccess", lang) %>");
           <%}%> 
           if(document.getElementById('inpCancelHiring').value == "true" )
           {
            document.getElementById('linkButtonNew').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled';
           }
    }
    </script>
</HEAD>
<BODY onresize="onResizeDo();" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" id="paramBody">
    <FORM id="form" method="post" action="" name="frmMain">
        <INPUT type="hidden" name="Command"></INPUT>
    <INPUT type="hidden" name="IsPopUpCall" value="1"></INPUT>
    <INPUT type="hidden" name="inpLastFieldChanged"></INPUT>
    <INPUT type="hidden" id="inpAction" name="inpAction" value=""></INPUT>
    <INPUT type="hidden" name="inpAssetId" id="inpAssetId" value="<%= request.getAttribute("inpAssetId") %>"></INPUT>
    <INPUT type="hidden" name="inpEmployeeId" id="inpEmployeeId" value="<%= request.getAttribute("inpEmployeeId") %>"></INPUT>
    <INPUT type="hidden" name="inpempCategory" id="inpempCategory" value="<%= request.getAttribute("inpempCategory") %>"></INPUT>
    <INPUT type="hidden" name="inpQualificationId" id="inpQualificationId" value=""></INPUT>
    <INPUT type="hidden" name="inpEmploymentId" id="inpEmploymentId" value="<%= request.getAttribute("inpEmploymentId") %>"></INPUT>
    <INPUT type="hidden" name="inpAddressId" id="inpAddressId" value="<%= request.getAttribute("inpAddressId") %>"></INPUT>
     <INPUT type="hidden" name="inpEmpStatus" id="inpEmpStatus" value="<%= request.getAttribute("inpEmpStatus") %>"></INPUT>
      <INPUT type="hidden" id="inpEmployeeStatus" name="inpEmployeeStatus" value="<%= request.getAttribute("inpEmployeeStatus") %>"></INPUT>
       <INPUT type="hidden" id="inpCancelHiring" name="inpCancelHiring" value="<%= request.getAttribute("CancelHiring") %>"></INPUT>

       
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
                    <td width="2%" ><a href="javascript:void(0);" onClick="onClickNew();" class="Main_ToolBar_Button" onMouseOver="window.status='Create a New Record';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonNew"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New" src="../web/images/blank.gif" title="<%= Resource.getProperty("hr.new",lang)%>" border="0" id="linkButtonNew"></a></td>
                    <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                    <td width="2%" ><a href="javascript:void(0);" onClick="onClickEditView()" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Form View';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonEdition"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hr.formview",lang) %>" border="0" id="linkButtonEdition"></a></td>
                    <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                    <td width="2%" ><a href="javascript:void(0);" onClick="onClickDelete();" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Delete Record';return true;" onMouseOut="window.status='';return true;" id="buttonDelete"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hr.delete", lang)  %>" border="0" id="linkButtonDelete"></a></td>
                    <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                    <td width="2%" ><a href="javascript:void(0);" onClick="onClickRefresh()" class="Main_ToolBar_Button" onMouseOver="window.status='Reload Current Page';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonRefresh"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Refresh" src="../web/images/blank.gif" title="<%= Resource.getProperty("hr.reload",lang) %>" border="0" id="linkButtonRefresh"></a></td>
                    <td class="Main_ToolBar_Separator_cell"><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                    <td width="2%" style="display :none"><a href="javascript:void(0);" onClick="onClickSearch()" class="Main_ToolBar_Button" onMouseOver="window.status='Filter Records';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonSearch"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Search<% if("1".equals(request.getAttribute("inpSearchType"))) { %>Filtered<% } %>" src="../web/images/blank.gif" title="" border="0" id="linkButtonSearch"></a></td> 
                    <td style="display:none" class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
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
                                <span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%= Resource.getProperty("hcm.Facilities&Assets",lang)%></a></span>
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
                                        <td>
                                            <div id="TabEmployee" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadTab('EMP');">
                                            <span class="LabelText"><%=Resource.getProperty("hcm.employee",lang)%></span>
                                            </div>
                                        </td>
                                        <td>
                                            <img id="ImgEmpInfo" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img>
                                        </td>
                                        <td>
                                            <div id="TabEmpInfo" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadTab('EMPINFO');">
                                            <span class="LabelText"><%= Resource.getProperty("hcm.empmntinfo",lang)%></span>
                                            </div>
                                        </td>
                                        <td>
                                            <img id="ImgEmpAddress" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img>
                                        </td>
                                        <td>
                                            <div id="TabEmpAddress" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadTab('EMPAddress');">
                                            <span class="LabelText"><%= Resource.getProperty("hcm.empaddress",lang)%></span>
                                            </div>
                                        </td>
                                        <td>
                                            <img  id="ImgqualInfo" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img>
                                        </td>
                                        <td>
                                            <div id="TabqualInfo" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadTab('EMPQual');">
                                            <span class="LabelText"><%= Resource.getProperty("hcm.empqualinfo",lang)%></span>
                                            </div>
                                        </td>
                                        <td>
                                            <img  id="ImgDependent" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png">
                                            </img>
                                        </td>
                                        <td>
                                            <div id="TabDependent" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadTab('EMPDependent');">
                                            <span class="LabelText"><%= Resource.getProperty("hcm.dependents",lang)%></span>
                                            </div>
                                        </td>
                                         <td><img  id="ImgPreEmp" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                        <td><div id="Tabpreemp" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadTab('PREEMP');" ><span class="LabelText"><%= Resource.getProperty("hcm.emppreemp",lang)%></span></div></td>                  
                                        
                                        <% if(request.getAttribute("inpempCategory").toString().equals("Y")) { %>
                                         <td><img id="ImgEmpContract" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                          <td><div id="TabEmpContract" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadTab('EMPCTRCT');" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                                          <%}else{ %>
                                           <td><img id="ImgEmpContract" class="imageNotSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                            <td><div id="TabEmpContract" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                                         <%} %>
                                         
                                          <td><img  id="ImgMedIns" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                         <td><div id="TabMedIns" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadTab('MEDIN');"><span class="LabelText"><%= Resource.getProperty("hcm.MedicalInsurance",lang)%></span></div>
                                         </td>
                                       <td>
                                            <img  id="ImgAsset" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png">
                                            </img>
                                        </td>
                                        <td>   
                                            <div id="TabAsset" class="tabCurrent">
                                            <span class="LabelText"><%= Resource.getProperty("hcm.Facilities&Assets",lang)%></span>
                                            </div> 
                                            <div style="text-align: center;"><img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img>
                                            </div>
                                        </td>
                                        <td><img  id="ImgDocument" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                        <td><div id="TabDocument" class=tabSelected onclick="reloadTab('DOC');"><span class="LabelText"><%= Resource.getProperty("hcm.documents",lang)%></span></div></td>     
                                                                  
                          <td><img  id="ImgEmpPerPayMethod" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                   <td>
                                     <div id="TabEmpPerPayMethod" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadTab('PERPAYMETHOD');"><span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></span>
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
                                                        <TD class="TextBox_ContentCell" style="padding-left: 50px;width: 20px;">
                                                           <input type="text" name="inpName1" id="inpName1" maxlength="59" value="<%= Utility.escapeQuote(request.getAttribute("inpName1")) %>" required="true" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black; width:300px" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);" dir="rtl"></input>
                                                           <input type="hidden" name="inpHidName1" id="inpHidName1" value="<%= Utility.escapeQuote(request.getAttribute("inpName1")) %>"></input>
                                                       </TD>
                                                       <TD class="" align="left" style="min-width:70px;padding-left: 10px"><span class="LabelText" ><%= Resource.getProperty("hcm.arabicname", lang)%></span></TD>
                                                       <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.fullname", lang)%></span></TD>
                                                       <TD class="TextBox_ContentCell">
                                                           <input type="text" name="inpName2" id="inpName2" maxlength="59" value="<%= Utility.escapeQuote(request.getAttribute("inpName2")) %>" required="true" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black; width:300px" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);"></input>
                                                           <input type="hidden" name="inpHidName2" id="inpHidName2" value="<%= Utility.escapeQuote(request.getAttribute("inpName2")) %>"></input>
                                                       </TD>
                                                       <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.code", lang)%></span></TD>
                                                       <TD class="TextBox_ContentCell">
                                                           <input type="text" name="inpEmpNo" id="inpEmpNo" maxlength="59" value="<%= Utility.escapeQuote(request.getAttribute("inpEmpNo")) %>" required="true" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black; width:300px" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);"></input>
                                                           <input type="hidden" name="inpHidEmpNo" id="inpHidEmpNo" value="<%= Utility.escapeQuote(request.getAttribute("inpEmpNo")) %>"></input>
                                                       </TD>
                                                   </TR>
                                               </TABLE>
                                              </div>
                                       </div>
                                         <%-- <div style="width:100%;" id="FormDetails" align="center">
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
 --%>                                        <div id="LoadingContent" style="display:none;">
                                            <div style="text-align: center;margin-top: 17%;"><img alt="Loading" src="../web/images/loading.gif"></div>
                                            <div style="color:#000000;font-family: sans-serif;font-size:14px;margin-top:10px;text-align: center;"><%= Resource.getProperty("construction.loading", lang)%>... </div>
                                        </div>
                                        <div id="jqgrid" style="width:100%; display:none;">
                                            <div align="center"><table id="AssetList" class="scroll" cellpadding="0" cellspacing="0" width="100%"></table><div id="pager" class="scroll" style="text-align: center;"></div></div>
                                        </div>
                                                           <div style="width:100%;">
                                            <div style="padding: 0 1%; width: 98%;">
                                                <div style="width:100%;" id="FormDetails">
                                                    <div align="center">
                                                        <TABLE style="width:80%; margin-top: 10px;">

                                                        </TABLE>
                                                    </div>
                                                </div>
                                            </div>
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
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.min_1.11.0.js"></script> 
<script type="text/javascript" src="../web/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="../web/js/jquery/grid.locale-en.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.widget.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.position.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.autocomplete.js"></script>
<script type="text/javascript" src="../web/sa.elm.ob.hcm/js/EmployeeList.js"></script>
<script type="text/javascript" src="../web/js/jquery/jquery.jqGrid.min.js"></script>
    
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.plugin.js"></script>
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura-ar.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.plus.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.ext.js"></script>
<script type="text/javascript">
var xmlhttp = new getXMLObject(), contextPath = '<%=request.getContextPath()%>';
var currentWindow='EMP',currentTab = 'EMPAsset';
var searchFlag = 0, onSearch = 0, onDelete = 0, gridW, gridH;
var formId = "748014FEDECF44D3BA89EDAB65573FE7";
var assetGrid = jQuery("#AssetList"), lastActiveType = 'Y';
var lang ='<%=lang%>',
direction='ltr';
    if(lang=="ar_SA"){
        direction='rtl';
     }
 jQuery(function() {
     assetGrid.jqGrid({
         direction :direction,
        url : '<%=request.getContextPath()%>/AssetAjax?action=GetAssetList',
        colNames : [ '<fmt:message key="hcm.documentno"></fmt:message>',
                     '<fmt:message key="hcm.assetname"></fmt:message>',
                     '<fmt:message key="hcm.startdate"></fmt:message>',
                     '<fmt:message key="hcm.enddate"></fmt:message>',
                     '<fmt:message key="hcm.letterNo"></fmt:message>',
                     '<fmt:message key="hcm.letterDate"></fmt:message>',
                     '<fmt:message key="hcm.decisionno"></fmt:message>',
                     '<fmt:message key="hcm.balance"></fmt:message>',
                     '<fmt:message key="hcm.description"></fmt:message>',
                     '<fmt:message key="hcm.flag"></fmt:message>'],
        colModel : [
                    {
                        name : 'documentNo',
                        index : 'documentNo',
                        width : 140
                    },
                    {
                        name : 'name',
                        index : 'name',
                        width : 140
                    },
                    {
                        name : 'startdate',
                        index : 'startdate',
                        width : 60,
                        searchoptions : {
                            dataInit : function(e) {
                           
                           $(e).calendarsPicker({
                               calendar: $.calendars.instance('ummalqura'),
                               dateFormat: 'dd-mm-yyyy',
                               showTrigger:  
                                   '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
                               changeMonth : true,
                               changeYear : true,
                               onClose : function(dateText, inst) {
                                   if (dateText != "")
                                       assetGrid[0].triggerToolbar();
                               }
                           });
                           e.style.width = "25%";
                            setTimeout(
                                    "$('#gs_startdate').before('<select onchange=\"searchList(\\\'SD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_startdate_s\"><option value=\"=\"><%= Resource.getProperty("hcm.Equalto", lang) %></option><option value=\">=\"><%= Resource.getProperty("hcm.GreaterthanorEqualto", lang) %></option><option value=\"<=\"><%= Resource.getProperty("hcm.LesserthanorEqualto", lang) %></option></select>');",
                                      10);
                       }
                   }
                },
                {
                    name : 'enddate',
                    index : 'enddate',
                    width : 60,
                    searchoptions : {
                       dataInit : function(e) {
                           
                           $(e).calendarsPicker({
                               calendar: $.calendars.instance('ummalqura'),
                               dateFormat: 'dd-mm-yyyy',
                               showTrigger:  
                                   '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
                               changeMonth : true,
                               changeYear : true,
                               onClose : function(dateText, inst) {
                                   if (dateText != "")
                                       assetGrid[0].triggerToolbar();
                               }
                           });
                           e.style.width = "25%";
                           setTimeout(
                                   "$('#gs_enddate').before('<select onchange=\"searchList(\\\'ED\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_enddate_s\"><option value=\"=\"><%= Resource.getProperty("hcm.Equalto", lang) %></option><option value=\">=\"><%= Resource.getProperty("hcm.GreaterthanorEqualto", lang) %></option><option value=\"<=\"><%= Resource.getProperty("hcm.LesserthanorEqualto", lang) %></option></select>');",
                                   10);
                       }
                   }
                },
                {
                    name : 'letterNo',
                    index : 'letterNo',
                    width : 60
                },
                {
                    name : 'letterdate',
                    index : 'letterdate',
                    width : 60,
                     searchoptions : {
                        dataInit : function(e) {
                            
                            $(e).calendarsPicker({
                                calendar: $.calendars.instance('ummalqura'),
                                dateFormat: 'dd-mm-yyyy',
                                showTrigger:  
                                    '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
                                changeMonth : true,
                                changeYear : true,
                                onClose : function(dateText, inst) {
                                    if (dateText != "")
                                        assetGrid[0].triggerToolbar();
                                }
                            });
                            e.style.width = "40%";
                            setTimeout(
                                    "$('#gs_letterdate').before('<select onchange=\"searchList(\\\'LD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_letterdate_s\"><option value=\"=\"><%= Resource.getProperty("hcm.Equalto", lang) %></option><option value=\">=\"><%= Resource.getProperty("hcm.GreaterthanorEqualto", lang) %></option><option value=\"<=\"><%= Resource.getProperty("hcm.LesserthanorEqualto", lang) %></option></select>');",
                                    10);
                        }
                    } 
              },
                {
                    name : 'decisionNo',
                    index : 'decisionNo',
                    width : 60
                },
                {
                    name : 'balance',
                    index : 'balance',
                    width : 60
                },
                {
                    name : 'description',
                    index : 'description',
                    width : 120
                },
                {
                    name : 'flag',
                    index : 'flag',
                    hidden: true 
                }
                
                ],
        rowNum : 50,
        mtype: 'POST',
        rowList : [ 20, 50, 100, 200, 500 ],
        pager : '#pager',
        sortname : 'name',
        datatype : 'xml',
        rownumbers : true,
        viewrecords : true,
        sortorder : "asc",
        scrollOffset : 17,
        loadComplete : function() {
            enableButton("false");
            $("#jqgrid").show();
            ChangeJQGridAllRowColor(assetGrid);
        },
        onSelectRow : function(id) {
            ChangeJQGridSelectRowColor(document.getElementById("inpAssetId").value, id);
            var rowData = assetGrid.getRowData(id);
            document.getElementById("inpAssetId").value= id;
            enableButton("true");
            endisableButton(rowData['flag']);
            enableQDMAttachmentIcon(id, formId, true, '<%=request.getContextPath()%>');
        },
        ondblClickRow : function(id) {
            var rowData = assetGrid.getRowData(id);
            document.getElementById("inpAssetId").value = id;
            onClickEditView();
        },
        beforeRefresh : function() {
            document.getElementById("gs_letterdate").value = '=';
            document.getElementById("gs_startdate").value = '=';
            document.getElementById("gs_enddate").value = '=';
            if (onSearch == 1) {
                assetGrid[0].clearToolbar();
                assetGrid[0].toggleToolbar();
            }
            onSearch = 0;
            searchFlag = 0;
            reSizeGrid();
        },
        onSortCol : function(index, columnIndex, sortOrder) {
            document.getElementById("inpAssetId").value = "";
        },
        onPaging : function() {
            document.getElementById("inpAssetId").value = "";
        },
        beforeRequest : function() {
            assetGrid.setPostDataItem("inpAssetId", document.getElementById("inpAssetId").value);
            assetGrid.setPostDataItem("inpEmployeeId", document.getElementById("inpEmployeeId").value);
             if ("" + assetGrid.getPostDataItem("_search") == "true") {
                    if ("" + assetGrid.getPostDataItem("letterdate") != "") {
                        var date = "" + assetGrid.getPostDataItem("letterdate");
                        var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                        if (!validformat.test(date))
                            assetGrid.setPostDataItem("letterdate", "");
                    }
                    if ("" + assetGrid.getPostDataItem("startdate") != "") {
                        var date = "" + assetGrid.getPostDataItem("startdate");
                        var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                        if (!validformat.test(date))
                            assetGrid.setPostDataItem("startdate", "");
                    }
                    if ("" + assetGrid.getPostDataItem("enddate") != "") {
                        var date = "" + assetGrid.getPostDataItem("enddate");
                        var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                        if (!validformat.test(date))
                            assetGrid.setPostDataItem("enddate", "");
                    }
                    assetGrid.setPostDataItem("letterdate_s", document.getElementById("gs_letterdate_s").value);
                    assetGrid.setPostDataItem("startdate_s", document.getElementById("gs_startdate_s").value);
                    assetGrid.setPostDataItem("enddate_s", document.getElementById("gs_enddate_s").value);
             }
             assetGrid.setPostDataItem("_search", 'true');
        }
    });
     assetGrid.jqGrid('navGrid', '#pager', {
        edit : false,
        add : false,
        del : false,
        search : false,
        view : false,
        beforeRefresh : function() {
             if (onSearch == 1) {
                 assetGrid[0].clearToolbar();
                 assetGrid[0].toggleToolbar();
             }
             onSearch = 0;
             searchFlag = 0;
             reSizeGrid();
        }
    }, {}, {}, {}, {});
   
     assetGrid.jqGrid('filterToolbar', {
        searchOnEnter : false
    });
     assetGrid[0].triggerToolbar();
    changeJQGridDisplay("AssetList", "inpAssetId");
}); 

function searchList(type) {
    if (type == "LD" && document.getElementById("gs_letterdate").value != "")
        assetGrid[0].triggerToolbar(); 
    else if (type == "SD" && document.getElementById("gs_startdate").value != "")
        assetGrid[0].triggerToolbar(); 
    else if (type == "ED" && document.getElementById("gs_enddate").value != "")
        assetGrid[0].triggerToolbar(); 
}

function reSizeGrid() {
    if (window.innerWidth) {
        gridW = window.innerWidth - 52;
        gridH = window.innerHeight - 260;
    }
    else if (document.body) {
        gridW = document.body.clientWidth - 52;
        gridH = document.body.clientHeight - 260;
    }
    if (onSearch == 1) {
        gridH = gridH - 23;
        if (navigator.userAgent.toLowerCase().indexOf("webkit") != -1)
            gridH++;
    }
    else if (parseInt(document.getElementById("client").scrollHeight) + 77 > parseInt(document.body.clientHeight))
        gridW = gridW - 14;
    if (gridW < 800)
        gridW = 800;
    if (gridH < 200)
        gridH = 200;
    assetGrid.setGridWidth(gridW, true);
    assetGrid.setGridHeight(gridH, true);
}
function onClickEditView() {
    if (document.getElementById('buttonEdition').className == 'Main_ToolBar_Button' && document.getElementById("inpAssetId").value != "") {
        document.getElementById("inpAction").value = "EditView";
        submitCommandForm('DEFAULT', true, null, 'Asset', '_self', null, true);
        return false;
    }
}
function onClickNew() {
    if( document.getElementById('linkButtonNew').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled')
    {
       return false;
    }
    document.getElementById("inpAssetId").value = '';
    document.getElementById("inpAction").value = "EditView";
    submitCommandForm('DEFAULT', true, null, 'Asset', '_self', null, true);
    return false;
}

function enableButton(flag) {
    if (flag == "true" && document.getElementById('buttonEdition').className != 'Main_ToolBar_Button') {
        document.getElementById('buttonEdition').className = 'Main_ToolBar_Button';
        document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition';
        document.getElementById('buttonDelete').className = 'Main_ToolBar_Button';
        document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete';
    }
    else if (flag == 'false') {
        document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_disabled';
        document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
        document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
        document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
    }    
}
function endisableButton(assertflag){
    //alert(assertflag);
    if(assertflag =="N"){
        document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_disabled';
        document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
        document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
        document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
    }
}
function onClickDelete() {
    if (document.getElementById('buttonDelete').className == 'Main_ToolBar_Button' && document.getElementById("inpAssetId").value != "") {
        OBAsk('<%= Resource.getProperty("hcm.confirmdelete", lang) %>', function(result) {
            if (result) {

                xmlhttp = new getXMLObject();
                if (xmlhttp) {
                    var urlPath = "&inpAssetId=" + document.getElementById("inpAssetId").value;

                    xmlhttp.open("GET", '<%=request.getContextPath()%>/AssetAjax?action=DeleteAsset' + urlPath, true);
                    xmlhttp.onreadystatechange = function() {
                        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                            var resource = xmlhttp.responseXML.getElementsByTagName("DeleteAsset");
                            var response = resource[0].getElementsByTagName("Response")[0].childNodes[0].nodeValue;
                            if (response == 'false') {
                                displayMessage("E", '<%=Resource.getProperty("hcm.error",lang)%>', '<%= Resource.getProperty("hcm.deletefailure", lang) %>')
                            }
                            else {
                                displayMessage("S", '<%=Resource.getProperty("hr.success",lang)%>', '<%= Resource.getProperty("hcm.deletesuccess", lang) %>');
                                document.getElementById("inpAssetId").value = "";
                                assetGrid.trigger("reloadGrid");
                                enableButton("false");
                            }
                        }
                    };
                    xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                    xmlhttp.send(null);
                }
            }
            else
                document.getElementById(document.getElementById("inpAssetId").value).focus();
        });
    }
}
function onClickRefresh() {
    document.getElementById("inpAssetId").value = "";
    document.getElementById("inpAction").value = "GridView";
    reloadWindow('EMPAsset');
}

function reloadWindow(tab) {
    if (tab == 'EMPAsset') {
        submitCommandForm('DEFAULT', true, null, 'Asset', '_self', null, true);
        return false;
    }
    return false;
}

function setTabClass(tab, type) {
    if (type == true)
        tab.className = "tabSelectedHOVER";
    else
        tab.className = "tabSelected";
}

function onClickSearch() {
    document.getElementById("messageBoxID").style.display = "none";
    if (document.getElementById("inpSearchType").value == "0")
        document.getElementById("inpSearchType").value = "1";
    else if (document.getElementById("inpSearchType").value == "1")
        document.getElementById("inpSearchType").value = "0";
    searchTitle();
    assetGrid[0].clearToolbar();
}
function searchTitle() {
    if (document.getElementById("inpSearchType").value == "0") {
        document.getElementById("linkButtonSearch").className = "Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Search";
        document.getElementById("linkButtonSearch").title = '<%= Resource.getProperty("hcm.employee.activeemployees",lang) %>';
    }
    else if (document.getElementById("inpSearchType").value == "1") {
        document.getElementById("linkButtonSearch").className = "Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_SearchFiltered";
        document.getElementById("linkButtonSearch").title = '<%= Resource.getProperty("hcm.employee.allemployees",lang) %>';
    }
}
function reloadTab(tab) {
    var url="";
    if (tab == 'EMP') {
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView' + url;
        document.frmMain.submit();
    }else if(tab == 'EMPINFO'){ 
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView&inpEmployeeId='+employeeId;
        document.frmMain.submit();
    }
    else if(tab == 'EMPAddress'){ 
        var employeeId=document.getElementById("inpEmployeeId").value;
         document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpEmployeeId='+employeeId;
        document.frmMain.submit(); 
    }
    else if(tab == 'EMPDependent'){ 
         var employeeId=document.getElementById("inpEmployeeId").value;
         document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpAction=GridView&inpEmployeeId='+employeeId;
         document.frmMain.submit();
    }
    else if (tab == 'EMPQual') {
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification?inpAction=GridView&inpEmployeeId='+employeeId;
        document.frmMain.submit();
   }
    else if (tab == 'PREEMP') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
         return false;
    }
    else if (tab == 'EMPAsset') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset', '_self', null, true);
    }
    else if (tab == 'EMPCTRCT') {
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
        document.frmMain.submit();
   }
  else if (tab == 'DOC') {
      submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.documents.header/Documents', '_self', null, true);
       return false;
  }
  else if (tab == 'PERPAYMETHOD') {
      submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.header/PersonalPaymentMethod', '_self', null, true);
       return false;
  }
  else if (tab == 'MEDIN') {
      submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.MedicalInsurance.header/MedicalInsurance', '_self', null, true);
       return false;
  }
}
</script>
</HTML>