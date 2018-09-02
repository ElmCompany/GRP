<!--
 *************************************************************************
 * All Rights Reserved.
 * Contributor(s): Gopalakrishnan
 *************************************************************************
-->
<%@ page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>
 <%@ page import="java.util.Date" %>
 <%
    String lang = ((String)session.getAttribute("#AD_LANGUAGE"));

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
    <link rel="stylesheet"  href="../web/js/jquery/css/demos.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/themes/base/jquery.ui.autocomplete.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/org.openbravo.client.application/ob-toolbar-styles.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
     <link rel="stylesheet" type="text/css" href="../web/js/common/select2.min.css"></link>
    <style type="text/css">
    .calendars-popup {
      z-index: 50001;
      position: absolute;
     
    }
    .OBToolbarIconButton_icon_undo{
      margin-top: 2;
    } 
    
    .select2-container {
  position: absolute;
    }
   
    </style>
    <script type="text/javascript" src="../web/js/common/common.js"></script>
    <script type="text/javascript" src="../web/js/utils.js"></script>
    <script type="text/javascript" src="../web/js/windowKeyboard.js"></script>
    <script type="text/javascript" src="../web/js/searchs.js"></script>
    <script type="text/javascript" id="paramDirectory">var baseDirectory = "../web/";</script>
    <script type="text/javascript" id="paramLanguage">var defaultLang="en_US";</script>
    <script type="text/javascript" src="../web/js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="../web/js/common/OBToolTip.js"></script>
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
      <%if(!request.getAttribute("inpIssuance").equals("I") && !request.getAttribute("inpIssuance").equals("TE")) {%>
      $('#inpStartDate').calendarsPicker({calendar:  
          $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect: function() {enableForm();generateJob(null)},showTrigger:  
      '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
     <% } %>
     $('#decisionDate').calendarsPicker({calendar:  
         $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect: function() {enableForm();},showTrigger:  
     '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
   
     
    }
    function onResizeDo(){resizeArea();}
    function onLoad()
    {
        <% request.setAttribute("SaveStatus",request.getAttribute("SaveStatus")==null?"":request.getAttribute("SaveStatus"));
        if(request.getAttribute("SaveStatus").equals("Add-True")) { %>displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.saveoreditsuccess",lang) %>");<% }
        else if(request.getAttribute("SaveStatus").equals("Update-True")) { %>displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.saveoreditsuccess",lang) %>");<% }
        else if(request.getAttribute("SaveStatus").equals("False")) { if(request.getAttribute("ErrorMsg")!=null){%>displayMessage("E", "<%=Resource.getProperty("hcm.error",lang)%>","<%= (String)request.getAttribute("ErrorMsg") %>") <%}else{%>displayMessage("E", "<%=Resource.getProperty("hcm.error",lang)%>","<%= Resource.getProperty("hcm.saveorediterror",lang) %>");<% }} %>
        
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
        <INPUT type="hidden" name="inpEmployeeId" id="inpEmployeeId" value="<%= request.getAttribute("inpEmployeeId") %>"></INPUT>
        <INPUT type="hidden" name="inpempCategory" id="inpempCategory" value="<%= request.getAttribute("inpempCategory") %>"></INPUT>
        <INPUT type="hidden" name="inpEmploymentId" id="inpEmploymentId" value="<%= request.getAttribute("inpEmploymentId") %>"></INPUT>
        <INPUT type="hidden" name="inpIssuance" id="inpIssuance" value="<%= request.getAttribute("inpIssuance") %>"></INPUT>
        <INPUT type="hidden" name="inpAddressId" id="inpAddressId" value="<%= request.getAttribute("inpAddressId") %>"></INPUT>
        <INPUT type="hidden" name="inpHireDate" id="inpHireDate" value="${requestScope.inpHireDate}"></INPUT>
        <INPUT type="hidden" name="inpissued" id="inpissued" value="<%= request.getAttribute("inpissued") %>"></INPUT>
          <INPUT type="hidden" name="inpEmpStatus" id="inpEmpStatus" value="<%= request.getAttribute("inpEmpStatus") %>"></INPUT>
      <INPUT type="hidden" id="inpEmployeeStatus" name="inpEmployeeStatus" value="<%= request.getAttribute("inpEmployeeStatus") %>"></INPUT>
       <INPUT type="hidden" id="inpCancelHiring" name="inpCancelHiring" value="<%= request.getAttribute("cancelHiring") %>"></INPUT>
       <INPUT type="hidden" name="inpNextTab" id="inpNextTab" value=""></INPUT>
        <jsp:include page="/web/jsp/ProcessBar.jsp"/>
         <div id="NewVersionOverlay" style="display: none; position: absolute; z-index: 50000; background-color: black; opacity: .5; top: 0px; left: 0px; width: 100%; height: 100%; filter: alpha(opacity=50)"></div>
                                              <div id="CancelHiringDecision" align="center" style="display: none; width: 520px; height: 200px; background: white; z-index: 50001; border: 2px solid #ff9c30; box-shadow: 0 0 20px #333; border-radius: 5px; position: absolute; margin: auto; top: 0; right: 0; bottom: 0; left: 0;">
                                              
                                                <table>
                        
                                                <tr>
                                             <td class="TitleCell" Style ="align:Center"><span class="LabelText"> <b> <%=Resource.getProperty("hcm.decisionno", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                            <TD class="TextBox_ContentCell">
                                              <input type="text" name="inpCanDecisionNo" id="inpCanDecisionNo" maxlength="30" required="true" class="dojoValidateValid TextBox_TwoCells_width required" onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" ></input>
                                             </TD>
                                              </tr>
                                             <tr>
                                             <td class="TitleCell" Style ="align:Center"><span class="LabelText"> <b> <%=Resource.getProperty("hcm.decisiondate", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                            <TD class="TextBox_ContentCell">
                                                <input type="text" id="decisionDate" class="dojoValidateValid TextBox_btn_OneCell_width required"  maxlength="10" name="decisionDate" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" ></input>
                                              </TD>
           
                                              </tr>
                                          <TR>
                                      <TD  id="submitButton">
                                                <BUTTON type="button" id="Submit_linkBTN" class="ButtonLink" onclick="cancelHireProcess();" style="margin: 10px 0;">
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
                          
                                             <TD  id="submitButton">
                                                <BUTTON type="button" id="Submit_linkBTN" class="ButtonLink" onclick="closepopup();" style="margin: 10px 0;">
                                                   <TABLE class="Button">
                                                      <TR>
                                                         <TD class="Button_left"><IMG class="Button_Icon Button_Icon_process" alt="Submit" title="Submit" src="../web/images/blank.gif" border="0"></IMG></TD>
                                                         <TD class="Button_text" id="Submit_BTNname">
                                                            <%= Resource.getProperty("hcm.cancel",lang).replace("'", "\\\'") %>
                                                         </TD>
                                                         <TD class="Button_right"></TD>
                                                      </TR>
                                              </TABLE>
                                              </BUTTON>
                                              </TD>
                                              </TR>
                                              </table>
                                              </div>
                                               <!-- Report Download Popup--End -->
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
                                <td width="2%" ><a href="javascript:void(0);" onClick="onClickNew();" class="Main_ToolBar_Button" onMouseOver="window.status='Create a New Record';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonNew"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.new",lang)%>" border="0" id="linkButtonNew"></a></td>
                                <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                                <td width="2%" ><a href="javascript:void(0);" onClick="onClickGridView()" class="Main_ToolBar_Button" onMouseOver="window.status='Form View';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonRelation"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Relation" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.gridview",lang) %>" border="0" id="linkButtonEdition"></a></td>
                                <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                                <td width="2%" ><a href="javascript:void(0);" onClick="onClickSave(this, 'Save');" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Save Record';return true;" onMouseOut="window.status='';return true;" id="linkButtonSave"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.save", lang)  %>" border="0" id="buttonSave"></a></td>  
                                <td width="2%" ><a href="javascript:void(0);" onClick="onClickSave(this, 'New');" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Save and New';return true;" onMouseOut="window.status='';return true;" id="linkButtonSave_New"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_New_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.savenew", lang)  %>" border="0" id="buttonSave_New"></a></td> 
                                <td width="2%" ><a href="javascript:void(0);" onClick="onClickSave(this, 'Grid');" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Save and Grid';return true;" onMouseOut="window.status='';return true;" id="linkButtonSave_Relation"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_Relation_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.savegrid", lang)  %>" border="0" id="buttonSave_Relation"></a></td>
                                <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                                 <td width="2%" ><a href="javascript:void(0);" onClick="onClickRefresh()" class="Main_ToolBar_Button" onMouseOver="window.status='Reload Current Page';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonRefresh"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Refresh" src="../web/images/blank.gif" title="<%= Resource.getProperty("hr.reload",lang) %>" border="0" id="linkButtonRefresh"></a></td> 
                                 <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                               
                                <td width="2%" ><a href="javascript:void(0);" onClick="onclickCancel()" class="OBToolbarIconButton" onMouseOver="window.status='Cancel';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="cancelhiring"><img class="OBToolbarIconButton_icon_undo OBToolbarIconButton" src="../web/images/blank.gif"  width = "100%" height = "90%" title="<%= Resource.getProperty("hcm.cancelhiring",lang) %>" border="0" id="LinkButtonCancel"></a></td> 
                                 <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                                
                               
                                
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
                                <span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%= Resource.getProperty("hcm.empmntinfo",lang)%></a></span>
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
                                          
                                       
                                                
                                                <td><div id="TabEmployee" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadFunction('EMP');">
                                                    <span class="LabelText"><%= Resource.getProperty("hcm.employee",lang)%></span></div>
                                                </td>
                        
                                                <td><img id="ImgEmpInfo" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                <td><div id="TabEmpInfo" class="tabCurrent"><span class="LabelText"><%= Resource.getProperty("hcm.empmntinfo",lang)%></span></div>
                                                    <div style="text-align: center;"><img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img></div>
                                                </td>
                                                <td><img id="ImgEmpAddress" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                <td><div id="TabEmpAddress" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadFunction('EMPADD');" ><span class="LabelText"><%= Resource.getProperty("hcm.empaddress",lang)%></span></div></td>
                                                <td><img  id="ImgqualInfo" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                 <td><div id="TabqualInfo" class="tabSelected"  onmouseout="this.className='tabSelected';" onmouseover="this.className='tabSelectedHOVER';" onclick="reloadFunction('Qualification');"><span class="LabelText"><%= Resource.getProperty("hcm.empqualinfo",lang)%></span></div></td>
                                               <td><img  id="ImgDependent" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                               <td><div id="TabDependent" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadFunction('Dependent');">
                                                 <span class="LabelText"><%= Resource.getProperty("hcm.dependent",lang)%>
                                                 </span>
                                                  </div>
                                               </td>
                                                <td><img  id="ImgPreEmp" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                <td><div id="Tabpreemp" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadFunction('PREEMP');" ><span class="LabelText"><%= Resource.getProperty("hcm.emppreemp",lang)%></span></div></td>
                                                    <% if(request.getAttribute("inpempCategory").toString().equals("Y")) { %>
                                                           <td><img id="ImgEmpContract" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                          <td><div id="TabEmpContract" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadFunction('EMPCTRCT');" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                                                          <%}else{ %>
                                                            <td><img id="ImgEmpContract" class="imageNotSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                            <td><div id="TabEmpContract" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                                                            <%} %>
                                                            <td><img  id="ImgMedIns" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                          <td><div id="TabMedIns" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadFunction('MEDIN');"><span class="LabelText"><%= Resource.getProperty("hcm.MedicalInsurance",lang)%></span></div>
                                                        </td>
                                                <td><img  id="ImgAsset" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                <td><div id="TabAsset" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadFunction('Asset');"><span class="LabelText"><%= Resource.getProperty("hcm.Facilities&Assets",lang)%></span></div>
                                                </td>
                                                <td><img  id="ImgDocument" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                     <td><div id="TabDocument" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadFunction('DOC');"><span class="LabelText"><%= Resource.getProperty("hcm.documents",lang)%></span></div></td>
                                                         <td><img  id="ImgEmpPerPayMethod" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                   <td>
                                     <div id="TabEmpPerPayMethod" class="tabSelected" onclick="reloadFunction('PERPAYMETHOD')">
                                     <span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></span>
                                     </div>
                                    </td>
                                        </tr>
                                        </tbody></table>
                                     <div style="margin-bottom: 5px;"></div>
                                      </div>
                                        <div id="LoadingContent" style="display:none;">
                                            <div style="text-align: center;margin-top: 17%;"><img alt="Loading" src="../web/images/loading.gif"></div>
                                            <div style="color:#000000;font-family: sans-serif;font-size:14px;margin-top:10px;text-align: center;"><%= Resource.getProperty("hcm.loading", lang)%>... </div>
                                        </div>
                                        
                                        <div style="width:100%;">
                                            <div style="padding: 0 1%; width: 98%;">
                                                <div style="width:100%;" id="FormDetails" class="readonly">
                                                
                                                  <div align="center">
                                                        <TABLE style="width:80%; margin-top: 10px;">
                                                           <TR>
                                                            <TD class="TextBox_ContentCell" style="padding-left: 50px;width: 20px;">
                                                                 <input type="text" name="inpName1" id="inpName1" maxlength="59" readonly value="<%= request.getAttribute("inpName1")%>" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);" dir="rtl"></input>
                                                            </TD>
                                                             <TD class="" align="left" style="min-width:70px;padding-left: 10px"><span class="LabelText" ><%= Resource.getProperty("hcm.arabicname", lang)%></span></TD>
                                                             <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.fullname", lang)%></span></TD>
                                                             <TD class="TextBox_ContentCell" colspan="3">
                                                                <input type="text" name="inpName2" id="inpName2" maxlength="59" readonly value="<%= request.getAttribute("inpName2") %>" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);"></input>
                                                             </TD>
                                                             <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.code", lang)%></span></TD>
                                                             <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpEmpNo" id="inpEmpNo" maxlength="59" readonly value="<%= request.getAttribute("inpEmpNo") %>" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);"></input>
                                                            </TD>
                                                         </TR>
                                                        </TABLE>
                                                    </div>
                                                         <DIV id="PrimaryEmp">
                                                            <table style="width: 100%;padding: 2px;">
                                                            <tr>
                                                            <td>
                                                             <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                              <TBODY>
                                                               <TR class="FieldGroup_TopMargin"></TR>
                                                               <TR>
                                                                   <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                   <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.primary.employment",lang)%></TD>
                                                                   <TD class="FieldGroupTitle_Right"><IMG class="FieldGroupTitle_Right_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                   <TD class="FieldGroupContent"></TD>
                                                               </TR>    
                                                               <TR class="FieldGroup_BottomMargin" style="display: none;"></TR>          
                                                              </TBODY>
                                                              </TABLE>
                                                               </td>
                                                              </tr>
                                                           </table>
                                                           </DIV>
                                                    <div align="center">
                                                        <TABLE style="width:80%; margin-top: 10px;">
                                                        <!-- /*<<<<<Your Code Here>>>>>*/ -->
                                                          <TR>
                                                            <td class="TitleCell"><span name="inpGradeLabel" class="LabelText" id="inpGradeLabel"> <%= Resource.getProperty("hcm.emp.grade", lang) %></span></td>
                                                            <td class="TextBox_ContentCell"><select id="inpGrade" name="inpGrade" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();generateJob(this.value);generateEmpgrade(this.value);"></select></td>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.jobno", lang) %></span></TD>
                                                            <td class="TextBox_ContentCell"><select id="inpJobno" name="inpJobno" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();generateFields(this.value)"></select></td>
                                                          </TR>
                                                          
                                                            <TR>                                                         
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.jobcode", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <select name="inpJobCode" id="inpJobCode"  maxlength="30" value="<%=request.getAttribute("inpJobCode")%>" required="false" class="Combo Combo_TwoCells_width" onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></select>
                                                            </TD>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.jobtitle", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpJobTitle" id="inpJobTitle" maxlength="30" value="<%=request.getAttribute("inpJobTitle")%>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);"  onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input>
                                                            </TD>
                                                            </TR>
                                                                                                                      
                                                            <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.deptcode", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <%-- <input type="text" name="inpDeptCode" id="inpDeptCode"  maxlength="30" value="<%=request.getAttribute("inpDeptCode")%>"  required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input> --%>
                                                            <select name="inpDeptCode" id="inpDeptCode"  maxlength="30" value="<%=request.getAttribute("inpDeptCode")%>"  required="false" class="Combo Combo_TwoCells_width" onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></select>
                                                            </TD>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.deptname", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpDeptName" id="inpDeptName"  maxlength="30" value="<%=request.getAttribute("inpDeptName")%>"  required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);"  onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input>
                                                            </TD>
                                                            </TR>
                                                            
                                                            <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.seccode", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <select name="inpSectionCode" id="inpSectionCode" name ="inpSectionCode" maxlength="30" value="<%=request.getAttribute("inpSectionCode")%>" required="false" class="Combo Combo_TwoCells_width" onkeydown="return onChangeEvent(event);"  onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></select>
                                                            </TD>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.secname", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpSectionName" id="inpSectionName" name="inpSectionName" maxlength="30" value="<%=request.getAttribute("inpSectionName")%>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);"  onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input>
                                                            </TD>
                                                            </TR>
                                                            
                                                            <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.location", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpLocation" id="inpLocation" maxlength="30" value="<%=request.getAttribute("inpLocation")%>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);"  onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input>
                                                            </TD>
                                                            <td class="TitleCell"><span name="inpPayLabel" class="LabelText" id="inpPayLabel"><%= Resource.getProperty("hcm.emp.payroll", lang) %></span></td>
                                                            <td class="TextBox_ContentCell"><select id="inpPayRoll" name="inpPayRoll" class="ComboKey Combo_TwoCells_width "></select></td>
                                                            </TR>
                                                            
                                                            <TR>
                                                            <td class="TitleCell"><span name="inpStatusLabel" class="LabelText" id="inpStatusLabel"> <%= Resource.getProperty("hcm.status", lang) %></span></td>
                                                            <td class="TextBox_ContentCell"><select id="inpStatus" class="Combo Combo_TwoCells_width" disabled>
                                                            <option value="ACT" <%if(request.getAttribute("inpStatus")!=null && request.getAttribute("inpStatus").equals("ACT")){ %> selected <%} %>><%= Resource.getProperty("hcm.active", lang) %></option>
                                                             <option value="INACT" <%if(request.getAttribute("inpStatus")!=null && request.getAttribute("inpStatus").equals("INACT")){ %> selected <%} %> ><%= Resource.getProperty("hcm.inactive", lang) %></option>
                                                             <option value="Terminated" <%if(request.getAttribute("inpStatus")!=null && request.getAttribute("inpStatus").equals("TE")){ %> selected <%} %> ><%= Resource.getProperty("hcm.terminate", lang) %></option>
                                                            </select></td>
                                                            <td class="TitleCell"><span name="inpEmpCatLabel" class="LabelText" id="inpEmpCatLabel"> <%= Resource.getProperty("hcm.emp.employmnetcategory", lang) %></span></td>
                                                            <td class="TextBox_ContentCell"><select id="inpEmpCat" name="inpEmpCat" class="Combo Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"></select></td>
                                                            </TR>
                                                            
                                                            <TR>             
                                                            <td class="TitleCell"><span name="inpEmpGradeLabel" class="LabelText" id="inpEmpGradeLabel"> <%= Resource.getProperty("hcm.emp.empGrade", lang) %></span></td>
                                                            <td class="TextBox_ContentCell"><select id="inpEmpGrade" name="inpEmpGrade" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();generatePayscale(this.value);"></select></td>
                                                            
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.government.agency", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpgovAgency" id="inpgovAgency" maxlength="30" value="<%=request.getAttribute("inpgovAgency")== null ? "" :request.getAttribute("inpgovAgency") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);"  onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input>
                                                                </TD>
                                                            </TR>
                                                            
                                                            <TR>
                                                            <td class="TitleCell"><span name="inpPayScaleLabel" class="LabelText" id="inpPayScaleLabel"> <%= Resource.getProperty("hcm.emp.payscale", lang) %></span></td>
                                                            <td class="TextBox_ContentCell"><select id="inpPayScale" name="inpPayScale" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); generateGradeStep(this.value);"">
<%--                                                             <option> <%= Resource.getProperty("hcm.select", lang) %></option>
 --%>                                                            </select></td>
                                                            <td class="TitleCell"><span name="inpGradeSteplabel" class="LabelText" id="inpGradeSteplabel"> <%= Resource.getProperty("hcm.emp.gradeStep", lang) %></span></td>
                                                            <td class="TextBox_ContentCell"><select id="inpGradeStep" name="inpGradeStep" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"></select></td>
                                                            </TR>
                                                            
                                                            <TR>
                                                            <td class="TitleCell"><span name="inpReasonLabel" class="LabelText" id="inpReasonLabel"> <%= Resource.getProperty("hcm.emp.changereason", lang) %></span></td>
                                                           <%if(request.getAttribute("inpissued").equals("UP")){ %>
                                                            <td class="TextBox_ContentCell"><select id="inpReason" name="inpReason" class="Combo Combo_TwoCells_width">
                                                            <option value="H" ><%= Resource.getProperty("hcm.hiring", lang) %></option>  
                                                            </select></td>
                                                            <%} 
                                                            else { %>
                                                             <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpReasonLabel" id="inpReasonLabel" maxlength="30" value="<%=request.getAttribute("inpReasonLabel")%>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);"  onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input>
                                                            </TD>
                                                           <% }%>
                                                            
                                                            
                                                            <td class="TitleCell"><span name="inpEmpNoLabel" class="LabelText" id="inpEmpNoLabel"> <%= Resource.getProperty("hcm.emp.empNo", lang) %></span></td>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpEmpNo" id="inpEmpNo" maxlength="30" value="<%=request.getAttribute("inpEmpNo")%>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input>
                                                            </TD>
                                                            </TR>
                                                            
                                                            <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.startdate", lang) %></span></TD>
                                                            <td class="TextBox_ContentCell">
                                                                <input type="text" id="inpStartDate" class="dojoValidateValid TextBox_btn_OneCell_width" value="<%= request.getAttribute("inpStartDate") %>" maxlength="10" name="inpStartDate"  onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input>
                                                            </td>
                                                            
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.enddate", lang) %></span></TD>
                                                            <td class="TextBox_ContentCell">
                                                                <input type="text" id="inpEndDate" class="dojoValidateValid TextBox_btn_OneCell_width" value="<%= request.getAttribute("inpEndDate")==null ? "":request.getAttribute("inpEndDate") %>" maxlength="10" name="inpEndDate" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input>
                                                            </td>
                                                            </TR>
                                                            <TR>
                                                             <td class="TitleCell"><span name="inpDecisionNoLabel" class="LabelText" id="inpDecisionNoLabel"> <%= Resource.getProperty("hcm.decisionno", lang) %></span></td>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpDecisionNo" id="inpDecisionNo" maxlength="30" value="<%=request.getAttribute("inpDecisionNo")==null ? "":request.getAttribute("inpDecisionNo")%>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input>
                                                            </TD>
                                                             <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.DecisionDate", lang) %></span></TD>
                                                            <td class="TextBox_ContentCell">
                                                                <input type="text" id="inpDecisionDate" class="dojoValidateValid TextBox_btn_OneCell_width" value="<%= request.getAttribute("inpDecisionDate")==null ? "":request.getAttribute("inpDecisionDate") %>" maxlength="10" name="inpDecisionDate" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" readonly></input>
                                                            </td>
                                                            </TR>
                                                             <tr>
                                                             <td class="TitleCell"><span class="LabelText" id="inpSupervisorLabel"> <b> <%=Resource.getProperty("hcm.supervisor", lang).replace("'", "\\\'")%>
                                                                </b>
                                                                </span>
                                                             </td>
                                                             <td class="TextBox_ContentCell">
                                                                <select id="inpSupervisorId" name ="inpSupervisorId" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableFormSupervisor(this.value);" >
                                                               <%--  <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option> --%>
                                                                </select>
                                                             </td>
                                                              <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.joinreqflag", lang) %></span></TD>
                                                              <td class="TextBox_ContentCell">
                                                              <input type="checkbox" id="inpjoinflag" name="inpjoinflag" <%if(request.getAttribute("inpjoinflag")!=null&& request.getAttribute("inpjoinflag").equals(true)){%> checked <%} %> disabled></input>
                                                             </td>
                                                             </TD>
                                                          </tr>
                                                            
                                                        </TABLE>
                                                    </div>
                                                      <DIV id="SecondaryEmp">
                                                            <table style="width: 100%;padding: 2px;">
                                                            <tr>
                                                            <td>
                                                             <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                              <TBODY>
                                                               <TR class="FieldGroup_TopMargin"></TR>
                                                               <TR>
                                                                   <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                   <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.secondary.employment",lang)%></TD>
                                                                   <TD class="FieldGroupTitle_Right"><IMG class="FieldGroupTitle_Right_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                   <TD class="FieldGroupContent"></TD>
                                                               </TR>    
                                                               <TR class="FieldGroup_BottomMargin" style="display: none;"></TR>          
                                                              </TBODY>
                                                              </TABLE>
                                                               </td>
                                                              </tr>
                                                           </table>
                                                           </DIV>
                                                           <div align="center">
                                                        <TABLE style="width:80%; margin-top: 10px;">
                                                        <!-- /*<<<<<Your Code Here>>>>>*/ -->
                                                          <TR>
                                                            <td class="TitleCell"><span name="inpSecGradeLabel" class="LabelText" id="inpSecGradeLabel"> <%= Resource.getProperty("hcm.emp.grade", lang) %></span></td>
                                                            <td class="TextBox_ContentCell">
                                                            <!-- <select id="inpSecGrade" name="inpSecGrade" class="Combo Combo_TwoCells_width" disabled onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();generateJob(this.value);generateEmpgrade(this.value);"></select> -->
                                                            <select type="text" name="inpSecGrade" id="inpSecGrade" value="<%= request.getAttribute("inpSecGrade") %>" required="false" class="Combo Combo_TwoCells_width" readonly></select>
                                                            </td>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.jobno", lang) %></span></TD>
                                                            <td class="TextBox_ContentCell">
                                                            <!-- <select id="inpSecJobno" name="inpSecJobno" class="Combo Combo_TwoCells_width" disabled onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();generateFields(this.value)"></select> dojoValidateValid TextBox_TwoCells_width -->
                                                            <select type="text" name="inpSecJobno" id="inpSecJobno"   value="<%= request.getAttribute("inpSecJobno") %>" required="false" class="Combo Combo_TwoCells_width"  readonly></select>
                                                            </td>
                                                          </TR>
                                                          
                                                            <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.jobcode", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <select type="text" name="inpSecJobCode" id="inpSecJobCode"  maxlength="30" value="<%= request.getAttribute("inpSecJobno") %>" required="false" class="Combo Combo_TwoCells_width"readonly></select>
                                                            </TD>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.jobtitle", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpSecJobTitle" id="inpSecJobTitle" maxlength="30" value="<%= request.getAttribute("inpSecJobTitle") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width"readonly></input>
                                                            </TD>
                                                            </TR>
                                                                                                                      
                                                            <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.deptcode", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <select type="text" name="inpSecDeptCode" id="inpSecDeptCode"  maxlength="30" value="<%= request.getAttribute("inpSecDeptCode") %>"  required="false" class="Combo Combo_TwoCells_width"readonly></select>
                                                            </TD>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.deptname", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpSecDeptName" id="inpSecDeptName"  maxlength="30" value="<%= request.getAttribute("inpSecDeptName") %>"  required="false" class="dojoValidateValid TextBox_TwoCells_width"  readonly></input>
                                                            </TD>
                                                            </TR>
                                                            
                                                            <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.seccode", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <select type="text" name="inpSecSectionCode" id="inpSecSectionCode" name ="inpSecSectionCode" maxlength="30" value="<%= request.getAttribute("inpSecSectionCode") %>" required="false" class="Combo Combo_TwoCells_width"  readonly></select>
                                                            </TD>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.secname", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpSecSectionName" id="inpSecSectionName" name="inpSecSectionName" maxlength="30" value="<%= request.getAttribute("inpSecSectionName") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width"  readonly></input>
                                                            </TD>
                                                            </TR>
                                                            
                                                            <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.location", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpSecLocation" id="inpSecLocation" maxlength="30" value="<%= request.getAttribute("inpSecLocation") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width"readonly></input>
                                                            </TD>
                                                            </TR>
                                                            <TR>
                                                            <td class="TitleCell"><span name="inpSecReasonLabel" class="LabelText" id="inpSecReasonLabel"> <%= Resource.getProperty("hcm.emp.changereason", lang) %></span></td>
                                                            <td class="TextBox_ContentCell"><select id="inpSecReason" name="inpReason" class="Combo Combo_TwoCells_width" disabled>
                                                            <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option>
                                                             <option value="DIN" <%if(request.getAttribute("inpSecReason")!=null && request.getAttribute("inpSecReason").equals("DIN")){ %> selected <%} %> ><%= Resource.getProperty("hcm.employinfo.delegation.insdie.department", lang) %></option>
                                                              <option value="DOUT" <%if(request.getAttribute("inpSecReason")!=null && request.getAttribute("inpSecReason").equals("DOUT")){ %> selected <%} %>><%= Resource.getProperty("hcm.employinfo.delegation.outside.department", lang) %></option>
                                                            </select></td>
                                                            <td class="TitleCell"><span name="inpEmpNoLabel" class="LabelText" id="inpEmpNoLabel"> <%= Resource.getProperty("hcm.emp.empNo", lang) %></span></td>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpSecEmpNo" id="inpSecEmpNo" maxlength="30" value="<%= request.getAttribute("inpSecEmpNo") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width"  readonly></input>
                                                            </TD>
                                                            </TR>
                                                            
                                                            <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.startdate", lang) %></span></TD>
                                                            <td class="TextBox_ContentCell">
                                                                <input type="text" id="inpSecStartDate" class="dojoValidateValid TextBox_btn_OneCell_width" value="<%= request.getAttribute("inpSecStartDate")==null ? "":request.getAttribute("inpSecStartDate")  %>" maxlength="10" name="inpSecStartDate"readonly></input>
                                                            </td>
                                                            
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.enddate", lang) %></span></TD>
                                                            <td class="TextBox_ContentCell">
                                                                <input type="text" id="inpSecEndDate" class="dojoValidateValid TextBox_btn_OneCell_width" value="<%= request.getAttribute("inpSecEndDate")==null ? "":request.getAttribute("inpSecEndDate")  %>" maxlength="10" name="inpEndDate"  readonly></input>
                                                            </td>
                                                            </TR>
                                                            <TR>
                                                             <td class="TitleCell"><span name="inpDecisionNoLabel" class="LabelText" id="inpDecisionNoLabel"> <%= Resource.getProperty("hcm.decisionno", lang) %></span></td>
                                                            <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpSecDecisionNo" id="inpSecDecisionNo" maxlength="30" value="<%= request.getAttribute("inpSecDecisionNo") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width"  readonly></input>
                                                            </TD>
                                                             <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.DecisionDate", lang) %></span></TD>
                                                            <td class="TextBox_ContentCell">
                                                                <input type="text" id="inpDecisionDate" class="dojoValidateValid TextBox_btn_OneCell_width" value="<%= request.getAttribute("inpSecDecisionDate")==null ? "":request.getAttribute("inpSecDecisionDate")  %>" maxlength="10" name="inpDecisionDate"  readonly></input>
                                                            </td>
                                                            </TR>
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
<script type="text/javascript">
var currentWindow='EMP',contextPath = '<%=request.getContextPath()%>';
var gradeId='<%=request.getAttribute("inpGrade")%>',
gradeName='<%=request.getAttribute("inpGradeName")%>',
jobNo ='<%=request.getAttribute("inpJobno")%>',
jobNoName ='<%=request.getAttribute("inpJobnoName")%>',
empCat ='<%=request.getAttribute("inpEmpCat")%>',
empCatName ='<%=request.getAttribute("inpEmpCatName")%>',
payScale='<%=request.getAttribute("inpPayScale")%>',
payScaleName='<%=request.getAttribute("inpPayScaleName")%>',
gradeStep='<%=request.getAttribute("inpGradeStep")%>',
gradeStepName='<%=request.getAttribute("inpGradeStepName")%>',
empGrade='<%=request.getAttribute("inpEmpGrade")%>',
empGradeName='<%=request.getAttribute("inpEmpGradeName")%>',
payRollId='<%=request.getAttribute("inpPayRoll")%>',
payRollName='<%=request.getAttribute("inpPayRollName")%>',
jobCode='<%=request.getAttribute("inpJobCode")%>',
inpJobCodeValue='<%=request.getAttribute("inpJobCodeValue")%>',
deptCode='<%=request.getAttribute("inpDeptCode")%>',
inpDeptCodeValue='<%=request.getAttribute("inpDeptCodeValue")%>',
sectCode='<%=request.getAttribute("inpSectionCode")%>',
inpSectionCodeValue='<%=request.getAttribute("inpSectionCodeValue")%>',
secGradeCode='<%=request.getAttribute("inpSecGrade")%>',
inpSecGradeValue='<%=request.getAttribute("inpSecGradeValue")%>',
secJobNo='<%=request.getAttribute("inpSecJobno")%>',
inpSecJobnoValue='<%=request.getAttribute("inpSecJobnoValue")%>',
secJobCode='<%=request.getAttribute("inpSecJobCode")%>',
inpSecJobCodeValue='<%=request.getAttribute("inpSecJobCodeValue")%>',
secDeptCode='<%=request.getAttribute("inpSecDeptCode")%>',
inpSecDeptCodeValue='<%=request.getAttribute("inpSecDeptCodeValue")%>',
secSectCode='<%=request.getAttribute("inpSecSectionCode")%>',
inpSecSectionCodeValue='<%=request.getAttribute("inpSecSectionCodeValue")%>',
<%-- url = "<%=request.getContextPath()%>/EmploymentAjax?action=getGrade",
 --%>
 select = "<%=Resource.getProperty("hcm.select", lang)%>",
changevalueask='<%= Resource.getProperty("hcm.changedvaluessave", lang) %>';
var inpSupervisorId='<%=request.getAttribute("inpSupervisorId")==null ? "":request.getAttribute("inpSupervisorId")%>';
var inpSupervisorName='<%=request.getAttribute("inpSupervisorName")==null ? "" :request.getAttribute("inpSupervisorName")%>';
var status ='<%= request.getAttribute("inpStatus")%>';
  
<%-- $("#inpGrade").select2(selectBoxAjaxPaging({
    url : function() {
           return '<%=request.getContextPath()%>/EmploymentAjax?action=getGrade'
    },
    size : "small"
})); --%>

function onclickCancel(){
       if( document.getElementById('LinkButtonCancel').className == 'OBToolbarIconButton_icon_undo OBToolbarIconButtonDisabled')
       {
          return false;
       }
       else
           {
    /* show the popup  */
    document.getElementById("NewVersionOverlay").style.display="";
    document.getElementById("CancelHiringDecision").style.display="";
           }

 }
function cancelHireProcess(){
    $.ajax({
        type:'GET',
        url:"<%=request.getContextPath()%>/EmploymentAjax?action=Submit",   
        data:{inpEmploymentId:document.getElementById("inpEmploymentId").value,inpEmployeeId:document.getElementById("inpEmployeeId").value,decisionNo:document.getElementById("inpCanDecisionNo").value,decisionDate:document.getElementById("decisionDate").value},
        dataType:'text',
        async:true,
        success:function(data)
        {       
            if(data == "success")
                {
                    OBAlert('<%=Resource.getProperty("hcm.issuedecision.success", lang) %>');
                closepopup();
                }
            else 
                {
                OBAlert('<%=Resource.getProperty("hcm.fields.mandatory", lang) %>');
                }
       
            
        }
        
    });
 
}
function closepopup(){
    /* close the report download popup */
       document.getElementById("NewVersionOverlay").style.display="none";
       document.getElementById("CancelHiringDecision").style.display="none";

    }
</script>
<script type="text/javascript" src="../web/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="../web/js/jquery/grid.locale-en.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.widget.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.mouse.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.button.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.draggable.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.position.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.autocomplete.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.resizable.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.dialog.js"></script> 
 <script type="text/javascript" src="../web/js/common/select2.min.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.hcm/js/Employment.js"></script>
</HTML>
