 <%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.List, java.util.ArrayList"%>
<%@page import="sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO"%>
<%@page import="org.codehaus.jettison.json.JSONObject"%>
<%@page import="org.codehaus.jettison.json.JSONArray"%>
<%@page import="sa.elm.ob.utility.util.UtilityVO"%>
<%@page import="sa.elm.ob.utility.util.Utility"%>
 <%@ page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>

 <%
    String lang = ((String)session.getAttribute("#AD_LANGUAGE"));
 String strOrg = "'0':'" + Resource.getProperty("hcm.select", lang) + "'";
 List<UtilityVO> organizationList = (ArrayList<UtilityVO>) request.getAttribute("OrganizationList");
 for (UtilityVO vo : organizationList)
     strOrg += ",'" + vo.getId() + "':'" + vo.getName().replace("'", "\\'") + "'";
 
         String strsal = "'0':'" + Resource.getProperty("hcm.select", lang) + "'";
         List<EmployeeVO> titlelist = (ArrayList<EmployeeVO>) request.getAttribute("inpTitleList");
         for (EmployeeVO vo : titlelist)
             strsal += ",'" + vo.getTitleId() + "':'" + vo.getTitleName().replace("'", "\\'") + "'";
                 
                 String strcat = "'0':'" + Resource.getProperty("hcm.select", lang) + "'";
                 List<EmployeeVO> catlist = (ArrayList<EmployeeVO>) request.getAttribute("inpEmpCategory");
                 for (EmployeeVO vo : catlist)
                     strcat += ",'" + vo.getCategoryId() + "':'" + vo.getCategorycode().replace("'", "\\'") + "'";
                 String strEmployeeStatus = "'0':'" + Resource.getProperty("hcm.select", lang) + "'";     
                 List<EmployeeVO> employeeStatusList = (ArrayList<EmployeeVO>) request.getAttribute("inpEmployeeCurrentStatus");

                 for (EmployeeVO vo : employeeStatusList)
                   strEmployeeStatus += ",'" + vo.getStatus() + "':'" + vo.getEmployeeStatus().replace("'", "\\'") + "'";
                     
                 
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
  <!--   <link rel="stylesheet" type="text/css" href="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css" id="paramCSS"></link> -->
    <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
  <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/ui.jqgrid.css"></link>
       <link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>"></link>
    
    <link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
    <script type="text/javascript" src="../web/js/common/common.js"></script>
    <script type="text/javascript" src="../web/js/utils.js"></script>
    <script type="text/javascript" src="../web/js/windowKeyboard.js"></script>
            
    
    <style type="text/css">
    .grid-col {
  padding-right: 12px !important;
  padding-left:  20px !important;
}
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
        <% request.setAttribute("issuemsg",request.getAttribute("issuemsg")==null?"":request.getAttribute("issuemsg")); %>
        <%  if(request.getAttribute("issuemsg").equals("Issue Decision")) { %>
          displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.issuedecision.success", lang) %>");
          <%}%>
         
    }
    </script>
</HEAD>
<BODY onresize="onResizeDo();" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" id="paramBody">
    <FORM id="form" method="post" action="" name="frmMain">
        <INPUT type="hidden" name="Command"></INPUT>
    <INPUT type="hidden" name="IsPopUpCall" value="1"></INPUT>
    <INPUT type="hidden" name="inpLastFieldChanged"></INPUT>
    <INPUT type="hidden" id="inpRowId" name="inpRowId" value=""></INPUT>
    <INPUT type="hidden" id="inpAction" name="inpAction" value=""></INPUT>
    <INPUT type="hidden" id="inpEmployeeId" name="inpEmployeeId" value="<%= request.getAttribute("inpEmployeeId") %>"></INPUT>
    <INPUT type="hidden" name="inpempCategory" id="inpempCategory" value="<%= request.getAttribute("inpempCategory") %>"></INPUT>
     <INPUT type="hidden" name="inpEmpStatus" id="inpEmpStatus" value="<%= request.getAttribute("inpEmpStatus") %>"></INPUT>
    <INPUT type="hidden" id="inpEmpNo" name="inpEmpNo" value=""></INPUT>
    <INPUT type="hidden" id="inpName1" name="inpName1" value=""></INPUT>
   <INPUT type="hidden" id="inpArabicName" name="inpArabicName" value=""></INPUT>
      <INPUT type="hidden" id="inpChangereason" name="inpChangereason" value=""></INPUT>
      <INPUT type="hidden" id="inpEmployementstatus" name="inpEmployementstatus" value=""></INPUT>
      <INPUT type="hidden" id="inpDelecationcount" name="inpDelecationcount" value=""></INPUT>
      <INPUT type="hidden" id="inpEmployeeStatus" name="inpEmployeeStatus" value="<%= request.getAttribute("inpEmployeeStatus") %>"></INPUT>
   
    <INPUT type="hidden" id="inpCode" name="inpCode" value=""></INPUT>
    <INPUT type="hidden" id="inpSearchType" name="inpSearchType" value="<%= request.getAttribute("inpSearchType") %>"></INPUT>
    <INPUT type="hidden" name="inpIsActive" id="inpIsActive" value="<%= request.getAttribute("inpIsActive") %>"></INPUT>
    <INPUT type="hidden" id="inpAddressId" name="inpAddressId" value=""></INPUT>
    <INPUT type="hidden" id="inpcancelHiring" name="inpcancelHiring" value=""></INPUT>
    <INPUT type="hidden" id="inpHiring" name="inpHiring" value=""></INPUT>
    
      <!-- Report Download Popup--Start -->
     <!-- Hiring Decision Report -->
       <jsp:include page="/web/jsp/ProcessBar.jsp" />
      
    <div id="NewVersionOverlay" style="display: none; position: absolute; z-index: 50000; background-color: black; opacity: .5; top: 0px; left: 0px; width: 100%; height: 100%; filter: alpha(opacity=50)"></div>
                                              <div id="downloadreport" align="center" style="display: none; width: 520px; height: 200px; background: white; z-index: 50001; border: 2px solid #ff9c30; box-shadow: 0 0 20px #333; border-radius: 5px; position: absolute; margin: auto; top: 0; right: 0; bottom: 0; left: 0;">
                                              
                          <table>
                        
                                                <tr>
                                             <td class="TitleCell" Style ="align:Center"><span class="LabelText" id="inpIsjoiningWorkReqName"> <b> <%=Resource.getProperty("hcm.joiningrequest", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                            <TD class="TextBox_ContentCell">
                                                             <input type="checkbox"  id ="inpIsjoiningWorkReq" name="inpIsjoiningWorkReq"></input>
                                                         </TD>
                                             </tr>
                                          <TR>
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
                                               <!-- cancel hiring decision -->
                                               <div id="NewVersionOverlayCancel" style="display: none; position: absolute; z-index: 50000; background-color: black; opacity: .5; top: 0px; left: 0px; width: 100%; height: 100%; filter: alpha(opacity=50)"></div>
                                              <div id="cancelhiringDecisionreport" align="center" style="display: none; width: 520px; height: 200px; background: white; z-index: 50001; border: 2px solid #ff9c30; box-shadow: 0 0 20px #333; border-radius: 5px; position: absolute; margin: auto; top: 0; right: 0; bottom: 0; left: 0;">
                                              
                                                <table>
                        
                                                <tr>
                                             <td class="TitleCell" Style ="align:Center"><span class="LabelText" id="inpdecisionflowname"> <b> <%=Resource.getProperty("hcm.decisionFlow", lang).replace("'", "\\\'")%>
                                                </b>
                                                </span>
                                             </td>
                                            <TD class="TextBox_ContentCell">
                                                             <input type="checkbox"  id ="inpdecisionFlow" name="inpdecisionFlow"></input>
                                                         </TD>
                                             </tr>
                                          <TR>
                                      <TD  id="submitButton">
                                                <BUTTON type="button" id="Submit_linkBTN" class="ButtonLink" onclick="generateReportforcancel();" style="margin: 10px 0;">
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
                                                <BUTTON type="button" id="Submit_linkBTN" class="ButtonLink" onclick="closepopupforcancel();" style="margin: 10px 0;">
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
                    <td width="2%" ><a href="javascript:void(0);" onClick="onClickEditView()" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Form View';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonEdition"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.formview",lang) %>" border="0" id="linkButtonEdition"></a></td>
                    <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                    <td width="2%" ><a href="javascript:void(0);" onClick="onClickDelete();" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Delete Record';return true;" onMouseOut="window.status='';return true;" id="buttonDelete"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.delete", lang)  %>" border="0" id="linkButtonDelete"></a></td>
                    <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                    <td width="2%" ><a href="javascript:void(0);" onClick="onClickRefresh()" class="Main_ToolBar_Button" onMouseOver="window.status='Reload Current Page';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonRefresh"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Refresh" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.reload",lang) %>" border="0" id="linkButtonRefresh"></a></td>
                    <td class="Main_ToolBar_Separator_cell"><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                    <td width="2%" style="display :none"><a href="javascript:void(0);" onClick="onClickSearch()" class="Main_ToolBar_Button" onMouseOver="window.status='Filter Records';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonSearch"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Search<% if("1".equals(request.getAttribute("inpSearchType"))) { %>Filtered<% } %>" src="../web/images/blank.gif" title="" border="0" id="linkButtonSearch"></a></td> 
                    <td style="display:none" class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                      <td width="2%" ><a href="javascript:void(0);" onClick="onclickdownload()" class="Main_ToolBar_Button" onMouseOver="window.status='Download';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="download"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.download",lang) %>" border="0" id="LinkButtonDownload"></a></td> 
                        <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                   
                   <td style="width: 100%;">
                    <div style="text-align: right;">
                    <div  class="MainButtonDiv" style=" position: relative; top: 10px; ">
                    <button onclick="cancelEmployee();" class="ButtonLink" style="display:none;" id="cancelButton" type="button">
                        <table class="Button">
                            <tbody><tr>
                                <td class="Button_left"><img border="0" src="../web/images/blank.gif" title="Cancel" alt="Cancel" class="Button_Icon Button_Icon_process"></td>
                                <td id="Submit_BTNname" class="Button_text"><%= Resource.getProperty("hcm.cancel", lang) %></td>
                                <td class="Button_right"><img border="0" src="../web/images/blank.gif" title="Close" alt="Close" class="Button_Icon Button_Icon_process"></td>
                            </tr></tbody>
                        </table>
                    </button>
                     &nbsp;&nbsp;&nbsp;   
                </div>
                </div></td>
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
                                <span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;">Employee</a></span>
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
                        <table>
                        <tbody>
                            <tr id="TabList">
                                <td>
                                    <div id="TabEmployee" class="tabCurrent">
                                        <span class="LabelText"><%= Resource.getProperty("hcm.employee",lang)%>
                                        </span>
                                    </div>
                                    <div style="text-align: center;">
                                        <img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;">
                                        </img>
                                    </div>
                                </td>
            
                                <td>
                                    <img  id="ImgEmpInfo" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png">
                                    </img>
                                </td>
                                
                                <td>
                                    <div id="TabEmpInfo"  class="tabNotSelected">
                                        <span class="LabelText"><%= Resource.getProperty("hcm.empmntinfo",lang)%>
                                        </span>
                                    </div>
                                </td>
                                <td><img id="ImgEmpAddress" class="imageNotSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                               <td><div id="TabEmpAddress" class="tabNotSelected"><span class="LabelText"><%= Resource.getProperty("hcm.empaddress",lang)%></span></div></td>
                               
                               <td><img  id="ImgqualInfo" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                <td><div id="TabqualInfo" class="tabNotSelected"><span class="LabelText"><%= Resource.getProperty("hcm.empqualinfo",lang)%></span></div></td>
                            
                                    <td>
                                    <img  id="ImgDependent" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png">
                                    </img>
                                </td>
                                
                                <td>
                                    <div id="TabDependent" class="tabNotSelected">
                                        <span class="LabelText"><%= Resource.getProperty("hcm.dependents",lang)%>
                                        </span>
                                    </div>
                                </td>
                                <td><img  id="Imgpreemp" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                               <td><div id="Tabpreemp" class="tabNotSelected"><span class="LabelText"><%= Resource.getProperty("hcm.emppreemp",lang)%></span></div></td>
                                <% if(request.getAttribute("inpempCategory").toString().equals("Y")) { %>
                                 <td><img id="ImgEmpContract" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                   <td><div id="TabEmpContract" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';"  ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                                    <%}else{ %>
                                   <td><img id="ImgEmpContract" class="imageNotSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                     <td><div id="TabEmpContract" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                                    <%} %>
                               
                                
                                  <td><img  id="ImgMedIns" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                             <td><div id="TabMedIns" class="tabNotSelected"><span class="LabelText"><%= Resource.getProperty("hcm.MedicalInsurance",lang)%></span></div>
                             </td>
                              <td>
                                    <img  id="ImgAsset" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png">
                                    </img>
                                </td>
                                
                                
                                
                                <td>
                                    <div id="TabAsset" class="tabNotSelected">
                                        <span class="LabelText"><%= Resource.getProperty("hcm.Facilities&Assets",lang)%></span>
                                    </div>
                                </td>
                               <td><img  id="ImgDocument" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                <td><div id="TabDocument" class="tabNotSelected" onclick="reloadTab('DOC');"><span class="LabelText"><%= Resource.getProperty("hcm.documents",lang)%></span></div></td>     
                                  
                                  <td><img  id="ImgEmpPerPayMethod" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                   <td>
                                     <div id="TabEmpPerPayMethod" class="tabNotSelected">
                                     <span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></span>
                                     </div>
                                    </td>
                             </tr>
                        </tbody>
                        </table>
                        <div style="margin-bottom: 5px;"></div>
                    </div>
                       
                                        
                                        <div id="LoadingContent" style="display:none;">
                                            <div style="text-align: center;margin-top: 17%;"><img alt="Loading" src="../web/images/loading.gif"></div>
                                            <div style="color:#000000;font-family: sans-serif;font-size:14px;margin-top:10px;text-align: center;"><%= Resource.getProperty("hcm.loading", lang)%>... </div>
                                        </div>
                                        <div id="jqgrid" style="width:100%; display:none;">
                                            <div align="center"><table id="EmployeeList" class="scroll" cellpadding="0" cellspacing="0" width="100%"></table><div id="pager" class="scroll" style="text-align: center;"></div></div>
                                        </div>
                                        <div style="width:100%;display:none;">
                                            <div style="float: right; margin: 10 15 10 0;"><div id="NextLink" style="display:none;" class="hTabButton"><div class="hTabButtonNextImg"></div><div class="hTabButtonText"><%= Resource.getProperty("hcm.continue", lang) %></div></div></div>
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
<script type="text/javascript" src="../web/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="../web/js/jquery/grid.locale-en.js"></script>
<script type="text/javascript" src="../web/js/jquery/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.datepicker.js"></script>

<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.plugin.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura-ar.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.plus.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.ext.js"></script>
<script type="text/javascript">
var xmlhttp = new getXMLObject(), contextPath = '<%=request.getContextPath()%>', currentTab = 'EMP';
var nextLink = document.getElementById("NextLink");
nextLink.style.opacity = '0.5';
nextLink.style.filter = 'alpha(opacity=50)';
nextLink.style.cursor = '';
var searchFlag = 0, onSearch = 0, onDelete = 0, gridW, gridH;
var formId = "748014FEDECF44D3BA89EDAB65573FE7";
var employeeGrid = jQuery("#EmployeeList"), lastActiveType = 'Y';
var lang ='<%=lang%>',
direction='ltr';
    if(lang=="ar_SA"){
        direction='rtl';
     }
jQuery(function() {
   employeeGrid.jqGrid({
       direction :direction,
       url : '<%=request.getContextPath()%>/EmployeeAjax?action=GetEmployeeList',
       colNames : [ '<%= Resource.getProperty("hcm.organization", lang) %>', '<%= Resource.getProperty("hcm.code", lang) %>', '<%= Resource.getProperty("hcm.salutation", lang) %>',
               '<%= Resource.getProperty("hcm.fullname", lang) %>', '<%= Resource.getProperty("hcm.arabicname", lang) %>','<%= Resource.getProperty("hcm.gender", lang) %>','<%= Resource.getProperty("hcm.nationalid", lang) %>','<%= Resource.getProperty("hcm.hiredate", lang) %>',
               '<%= Resource.getProperty("hcm.employeetype", lang) %>', '<%= Resource.getProperty("hcm.persontype", lang) %>', '<%= Resource.getProperty("hcm.status", lang) %>','<%= Resource.getProperty("hcm.active", lang) %>' , '<%= Resource.getProperty("hcm.address", lang) %>','<%= Resource.getProperty("hcm.address", lang) %>','<%= Resource.getProperty("hcm.address", lang) %>','<%= Resource.getProperty("hcm.address", lang) %>','<%= Resource.getProperty("hcm.address", lang) %>','<%= Resource.getProperty("hcm.emp.shortstatus", lang) %>','<%= Resource.getProperty("hcm.emp.employeeid", lang) %>','<%= Resource.getProperty("hcm.cancelhiring", lang) %>','<%= Resource.getProperty("hcm.hiring", lang) %>','Employee Status'],
       colModel : [{
           name : 'org',
           index : 'org',
           hidden : true,
          stype : 'select',
           width : 100,
            searchoptions : {
               sopt : [ 'eq' ],
              value: { <%= strOrg %> },
               dataInit : function(e) {
                   e.className = "Combo gs_org_e";
                   e.style.padding = "0";
                   e.style.margin = "2px 0 0";
                   e.style.width = "95%";
                   e.style.height = "18px";
               }
           } 
       }, 
               {
                   name : 'value',
                   index : 'value',
                   width : 90,
               },
               {
                   name : 'Salutation',
                   index : 'Salutation',
                   width : 60,
                   hidden : true,
                   stype : 'select',
                   width : 100,
                    searchoptions : {
                       sopt : [ 'eq' ],
                      value: { <%= strsal %> },
                       dataInit : function(e) {
                           e.className = "Combo gs_org_e";
                           e.style.padding = "0";
                           e.style.margin = "2px 0 0";
                           e.style.width = "95%";
                           e.style.height = "18px";
                       }
                   } 
               },
               {
                   name : 'name',
                   index : 'name',
                   width : 250,
               },
               {
                   name : 'arabicname',
                   index : 'arabicname',
                   width : 250,
                   align:'right', classes:"grid-col"
               },
               {
                   name : 'gender',
                   index : 'gender',
                   width : 90,
                   stype : 'select',
                   searchoptions : {
                       sopt : [ 'eq' ],
                       value : '0:<%= Resource.getProperty("hcm.select", lang) %>;M:<%= Resource.getProperty("hcm.male", lang) %>;F:<%= Resource.getProperty("hcm.female", lang) %>',
                       dataInit : function(e) {
                           e.className = "Combo";
                           e.style.padding = "0";
                           e.style.margin = "2px 0 0";
                           e.style.height = "18px";
                       }
                   } 
               },
               {
                   name : 'nationality_identifier',
                   index : 'nationality_identifier',
                   width : 90,
               },
               {
                     name : 'hiredate',
                     index : 'hiredate',
                     width : 80,
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
                                         employeeGrid[0].triggerToolbar();
                                 }
                             });
                             e.style.width = "25%";
                             setTimeout(
                                     "$('#gs_hiredate').before('<select onchange=\"searchList(\\\'SD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_hiredate_s\"><option value=\"=\"><%= Resource.getProperty("hcm.Equalto", lang) %></option><option value=\">=\"><%= Resource.getProperty("hcm.GreaterthanorEqualto", lang) %></option><option value=\"<=\"><%= Resource.getProperty("hcm.LesserthanorEqualto", lang) %></option></select>');",
                                     10);
                         }
                     } 
               }, {
                   name : 'cat',
                   index : 'cat',
                   width : 100,
                   stype : 'select',
                   hidden : true,
                   width : 100,
                    searchoptions : {
                       sopt : [ 'eq' ],
                      value: { <%= strcat %> },
                       dataInit : function(e) {
                           e.className = "Combo gs_org_e";
                           e.style.padding = "0";
                           e.style.margin = "2px 0 0";
                           e.style.width = "95%";
                           e.style.height = "18px";
                       }
                   } 
               },
               {
                   name : 'persontype',
                   index : 'persontype',
                   width : 90,
               },
               {
                   name : 'status',
                   index : 'status',
                   width : 100,
                    stype : 'select',
                   searchoptions : {
                       sopt : [ 'eq' ],
                       value : '0:<%= Resource.getProperty("hcm.select", lang) %>;UP:<%= Resource.getProperty("hcm.underproc", lang) %>;I:<%= Resource.getProperty("hcm.issued", lang) %>;C:<%= Resource.getProperty("hcm.cancelled", lang) %>;TE:<%= Resource.getProperty("hcm.terminate.employment", lang) %>',
                       dataInit : function(e) {
                           e.className = "Combo";
                           e.style.padding = "0";
                           e.style.margin = "2px 0 0";
                           e.style.height = "18px";
                       }
                   } 
               },
               {
                   name: 'isactive', index: 'isactive', stype : 'select', width : 60,
                   searchoptions : {
                       sopt : [ 'eq' ],
                       value : '0:<%= Resource.getProperty("hcm.select", lang) %>;Y:<%= Resource.getProperty("hcm.yes", lang) %>;N:<%= Resource.getProperty("hcm.no", lang) %>',
                       dataInit : function(e) {
                           e.className = "Combo";
                           e.style.padding = "0";
                           e.style.margin = "1px 0 0";
                           e.style.height = "18px";
                       }
                   }
               },
               {
                    name: 'addressid',
                  index: 'addressid',
                    hidden :true
                  },
                  {
                      name: 'category',
                      index: 'category',
                      hidden :true
                  },
                  {
                      name: 'changereason',
                      index: 'changereason',
                      hidden :true
                  },
                  {
                      name: 'employementstatus',
                      index: 'employementstatus',
                        hidden :true
                   },
                   {
                       name: 'delecationcount',
                       index: 'delecationcount',
                         hidden :true
                   },
                   {
                       name: 'shortstatus',
                       index: 'shortstatus',
                         hidden :true
                   },
                   {
                       name: 'employeeid',
                       index: 'employeeid',
                         hidden :true
                   },
                   {
                       name: 'cancel',
                       index: 'cancel',
                         hidden :true
                   },
                   {
                       name: 'hiringDecision',
                       index: 'hiringDecision',
                         hidden :true
                   },
                   {
                       name: 'employeeStatus',
                       index: 'employeeStatus',
                       stype : 'select',
                       width : 100,
                        searchoptions : {
                           sopt : [ 'eq' ],
                          value: { <%= strEmployeeStatus %> },
                           dataInit : function(e) {
                               e.className = "Combo gs_org_e";
                               e.style.padding = "0";
                               e.style.margin = "2px 0 0";
                               e.style.width = "95%";
                               e.style.height = "18px";
                           }
                       }
                   }
                   
                  ],
       rowNum : 50,
       mtype: 'POST',
       rowList : [ 20, 50, 100, 200, 500 ],
       pager : '#pager',
       sortname : 'value',
       datatype : 'xml',
       rownumbers : true,
       viewrecords : true,
       sortorder : "asc",
       scrollOffset : 17,
       onSelectRow : function(id) {
           ChangeJQGridSelectRowColor(document.getElementById("inpRowId").value, id);
           var rowData = employeeGrid.getRowData(id);
           document.getElementById("inpRowId").value = id;
           document.getElementById("inpEmployeeId").value = rowData['employeeid'];
           document.getElementById("inpName1").value = rowData['name'];
           document.getElementById("inpArabicName").value = rowData['arabicname'];
           document.getElementById("inpCode").value = rowData['value'];
           document.getElementById("inpIsActive").value = rowData['isactive'];
           document.getElementById("inpempCategory").value = rowData['category'];
           document.getElementById("inpChangereason").value = rowData['changereason'];
           document.getElementById("inpEmployementstatus").value = rowData['employementstatus'];
           document.getElementById("inpDelecationcount").value = rowData['delecationcount'];
           document.getElementById("inpEmpStatus").value = id;
           document.getElementById("inpEmployeeStatus").value = rowData['shortstatus'];
           document.getElementById("inpcancelHiring").value = rowData['cancel'];
           document.getElementById("inpHiring").value = rowData['hiringDecision'];

           enableTabs();
           enableButton("true");
           enableQDMAttachmentIcon(id, formId, true, '<%=request.getContextPath()%>');
           document.getElementById("inpAddressId").value= rowData['addressid'];
           if(rowData['shortstatus']=='I'){
               if(document.getElementById("inpChangereason").value=='H' && document.getElementById("inpEmployementstatus").value == 'ACT' && document.getElementById("inpDelecationcount").value == 0){
                   CheckAlreadyCancel(rowData['value']);
               }
               else
                   document.getElementById("cancelButton").style.display="none";

               document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
               $('#buttonDelete').attr("class", "Main_ToolBar_Button_disabled");
           }
           else if(rowData['shortstatus']=='TE'){
               document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
               $('#buttonDelete').attr("class", "Main_ToolBar_Button_disabled");
           }
           else{
               document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete';
               $('#buttonDelete').attr("class", "Main_ToolBar_Button");
               document.getElementById("cancelButton").style.display="none"; 
           }
          
           if(rowData['cancel'] == "false" && rowData['hiringDecision'] == "false")
           {
            document.getElementById('LinkButtonDownload').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print_disabled';
           }
           else if(rowData['cancel'] == "true")
               {
               document.getElementById('LinkButtonDownload').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print';
               }
           else if(rowData['hiringDecision'] == "true")
           {
           document.getElementById('LinkButtonDownload').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print';
           }
           
         
          
           
       },
       ondblClickRow : function(id) {
         
           var rowData = employeeGrid.getRowData(id);
           document.getElementById("inpEmployeeId").value = rowData['employeeid'];
           document.getElementById("inpIsActive").value = rowData['isactive'];
           document.getElementById("inpEmpStatus").value = id;
           document.getElementById("inpEmployeeStatus").value = rowData['shortstatus'];
           
           onClickEditView();
       },
       beforeRequest : function() {
            resetTab();
           "" + employeeGrid.jqGrid('getGridParam', 'postData')['_search']
           if ("" + employeeGrid.getPostDataItem("_search") == "true" && onDelete == 0){
               document.getElementById("inpEmployeeId").value = "";
        document.getElementById("inpIsActive").value = "";
           }
           
           employeeGrid.setPostDataItem("inpEmployeeId", document.getElementById("inpEmployeeId").value);
           if (onDelete == 0)
               employeeGrid.setPostDataItem("DeleteEmployee", "");

           if ("" + employeeGrid.getPostDataItem("_search") == "true") {
               if ("" + employeeGrid.getPostDataItem("hiredate") != "") {
                   var date = "" + employeeGrid.getPostDataItem("hiredate");
                   var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                   if (!validformat.test(date))
                       employeeGrid.setPostDataItem("hiredate", "");
               }
               employeeGrid.setPostDataItem("hiredate_s", document.getElementById("gs_hiredate_s").value);
           }
           employeeGrid.setPostDataItem("_search", 'true');
       },
       onSortCol : function(index, columnIndex, sortOrder) {
           document.getElementById("inpEmployeeId").value = "";
           document.getElementById("inpIsActive").value = "";
           resetTab();
       },
       onPaging : function() {
           document.getElementById("inpEmployeeId").value = "";
           document.getElementById("inpIsActive").value = "";
           resetTab();
       },
       loadComplete : function() {
           enableButton("false");
           var idList = employeeGrid.getDataIDs();
           var idCount = idList.length;
           var idValue = document.getElementById("inpEmployeeId").value;

           if (idValue != "") {
               if (onDelete == 1) {
                   var flag = 0;
                   if (flag == 0) {
                       displayMessage("S", '<%=Resource.getProperty("hcm.success",lang)%>', '<%= Resource.getProperty("hcm.deletesuccess", lang) %>');
                       document.getElementById("inpEmployeeId").value = "";
                       document.getElementById("inpIsActive").value = "";
                       ChangeJQGridSelectRowColor(idValue, "");
                   }
                   onDelete = 0;
               }
               else {
                   employeeGrid.setSelection(idValue);
                   enableQDMAttachmentIcon(idValue, formId, true, '<%=request.getContextPath()%>');
               }
           }
           else
               enableQDMAttachmentIcon('', formId, false, '');
           ChangeJQGridAllRowColor(employeeGrid);
           //document.getElementById("gs_isactive").value = lastActiveType;
           $("#LoadingContent").hide();
           $("#jqgrid").show();
           if (idCount > 0)
               employeeGrid.trigger("resize");
           setTimeout(function() {
               if (document.getElementById("inpEmployeeId").value.length == 32) {
                   employeeGrid.closest(".ui-jqgrid-bdiv").scrollTop(22 * (employeeGrid.getInd(document.getElementById("inpEmployeeId").value)-2));
               }
           }, 100);
       }
   });
   employeeGrid.jqGrid('navGrid', '#pager', {
       edit : false,
       add : false,
       del : false,
       search : false,
       view : false,
       beforeRefresh : function() {
           document.getElementById("gs_hiredate").value = '=';
           if (onSearch == 1) {
               employeeGrid[0].clearToolbar();
               employeeGrid[0].toggleToolbar();
           }
           onSearch = 0;
           searchFlag = 0;
           resetTab();
           reSizeGrid();
       }
   }, {}, {}, {}, {});
  
   employeeGrid.jqGrid('filterToolbar', {
       searchOnEnter : false
   });
   employeeGrid[0].triggerToolbar();
   changeJQGridDisplay("EmployeeList", "inpEmployeeId");
}); 
function searchList(type) {
     if (type == "SD" && document.getElementById("gs_hiredate").value != "")
        employeeGrid[0].triggerToolbar(); 
}
function CheckAlreadyCancel(value){
     $.post('<%=request.getContextPath()%>/EmployeeAjax', {
         action : 'checkAlreadyCancel',
         inpEmpNo : value
     }, function(data) {
         var result = data.getElementsByTagName("result")[0].childNodes[0].nodeValue;
         if(result=='true'){
             document.getElementById("cancelButton").style.display="none"; 
         }
         else
               document.getElementById("cancelButton").style.display=""; 
        
     });
     return;
}

function onclickdownload(){
    
   if( document.getElementById('LinkButtonDownload').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print_disabled')
    {
       return false;
    }
    else
        {
      $.post('<%=request.getContextPath()%>/EmployeeAjax', {
             action : 'checkEmploymentStatus',
             inpEmployeeId : document.getElementById("inpEmployeeId").value
         }, function(data) {
             $(data).find("ChkEmployeeStatus").each(function()
                      {    
                          var result=$(this).find("result").text();
                          if(result =='true')
                              {
                              document.getElementById("NewVersionOverlay").style.display="none";
                              document.getElementById("downloadreport").style.display="none";
                              document.getElementById("NewVersionOverlayCancel").style.display="";
                              document.getElementById("cancelhiringDecisionreport").style.display="";
                              
                              }
                          else
                              {
                              document.getElementById("NewVersionOverlay").style.display="";
                              document.getElementById("downloadreport").style.display="";
                              }
                         
                          
                      });  
            
         });
        }


 }
 function generateReport(){
     closepopup(); /*  after click on submit close the popup */
     submitCommandForm('DEFAULT', false, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.HiringDecisionReport/HiringDecisionReport?inpAction=Submit', 'background_target', null, false);  
     $("#inpIsjoiningWorkReq").prop('checked',false);  /* unchecked the checkbox */
 }
 function generateReportforcancel()
 {
     closepopupforcancel();
     submitCommandForm('DEFAULT', false, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.cancelHiringDecisionReport/CancelHiringDecisionReport?inpAction=Submit', 'background_target', null, false);  
     $("#inpdecisionFlow").prop('checked',false);  /* unchecked the checkbox */
     
 }
 function closepopup(){
     /* close the report download popup */
        document.getElementById("NewVersionOverlay").style.display="none";
        document.getElementById("downloadreport").style.display="none";

     }
 function closepopupforcancel()
     {
     document.getElementById("NewVersionOverlayCancel").style.display="none";
     document.getElementById("cancelhiringDecisionreport").style.display="none";
     
     } 


function reSizeGrid() {
    if (window.innerWidth) {
        gridW = window.innerWidth - 52;
        gridH = window.innerHeight - 215;
    }
    else if (document.body) {
        gridW = document.body.clientWidth - 52;
        gridH = document.body.clientHeight - 215;
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
    employeeGrid.setGridWidth(gridW, true);
    employeeGrid.setGridHeight(gridH, true);
}
function onClickEditView() {
    if (document.getElementById('buttonEdition').className == 'Main_ToolBar_Button' && document.getElementById("inpEmployeeId").value != "") {
        document.getElementById("inpAction").value = "EditView";
        submitCommandForm('DEFAULT', true, null, 'Employee', '_self', null, true);
        return false;
    }
}
function onClickNew() {
    document.getElementById("inpAction").value = "EditView";
    document.getElementById("inpEmployeeId").value = "";
    document.getElementById("inpIsActive").value = "";
    submitCommandForm('DEFAULT', true, null, 'Employee', '_self', null, true);
    return false;
}

function enableButton(flag) {
    if (flag == "true" && document.getElementById('buttonEdition').className != 'Main_ToolBar_Button') {
        document.getElementById('buttonEdition').className = 'Main_ToolBar_Button';
        document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition';
        document.getElementById('buttonDelete').className = 'Main_ToolBar_Button';
        document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete';
        document.getElementById('LinkButtonDownload').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print';
        
    }
    else if (flag == 'false') {
        document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_disabled';
        document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
        document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
        document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
        document.getElementById('LinkButtonDownload').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print_disabled';
    }
}
function onClickDelete() {
    if (document.getElementById('buttonDelete').className == 'Main_ToolBar_Button' && document.getElementById("inpEmployeeId").value != "") {
        OBAsk('<%= Resource.getProperty("hcm.confirmdelete", lang) %>', function(result) {
            if (result) {

                xmlhttp = new getXMLObject();
                if (xmlhttp) {
                    var urlPath = "&inpEmployeeId=" + document.getElementById("inpEmployeeId").value+"&inpEmpstatus=" + document.getElementById("inpEmployeeStatus").value;

                    xmlhttp.open("GET", '<%=request.getContextPath()%>/EmployeeAjax?action=DeleteEmployee' + urlPath, true);
                    xmlhttp.onreadystatechange = function() {
                        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                            var resource = xmlhttp.responseXML.getElementsByTagName("DeleteEmployee");
                            var response = resource[0].getElementsByTagName("Response")[0].childNodes[0].nodeValue;
                            if (response == 'false') {
                                displayMessage("E", '<%=Resource.getProperty("hcm.error",lang)%>', '<%= Resource.getProperty("hcm.deletefailure", lang) %>')
                            }
                            else {
                                displayMessage("S", '<%=Resource.getProperty("hcm.success",lang)%>', '<%= Resource.getProperty("hcm.deletesuccess", lang) %>');
                                document.getElementById("inpEmployeeId").value = "";
                                employeeGrid.trigger("reloadGrid");
                            }
                        }
                    };
                    xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                    xmlhttp.send(null);
                }
            }
            else
                document.getElementById(document.getElementById("inpEmployeeId").value).focus();
        });
    } 
}
function onClickRefresh() {
    document.getElementById("inpEmployeeId").value = "";
    document.getElementById("inpIsActive").value = "";
    document.getElementById("inpAction").value = "GridView";
    resetTab();
    reloadWindow('EMP');
}
function reloadWindow(tab) {
    if (tab == 'EMP') {
        submitCommandForm('DEFAULT', true, null, 'Employee', '_self', null, true);
        return false;
    }
 if (tab == 'Qualification') {
    submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification', '_self', null, true);
     return false;
 }
    return false;
}
function enableTabs() {
    var tab = null, image = null;
//Onclick Employment Info Tab
    tab = document.getElementById("TabEmpInfo");
    image = document.getElementById("ImgEmpInfo");
    tab.className = "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
           reloadTab('EMPINF')
        });
        tab.attachEvent('onmouseover', function() {
            setTabClass(document.getElementById("TabEmpInfo"), true);
        });
        tab.attachEvent('onmouseout', function() {
            setTabClass(document.getElementById("TabEmpInfo"), false);
        });
    }
    else
        tab.addEventListener('click', function() {
           reloadTab('EMPINF')
        }, false);
    
    tab = document.getElementById("TabEmpAddress");
    image = document.getElementById("ImgEmpAddress");
     tab.className = "tabSelected";
     image.className = "imageSelected";
       if (tab.attachEvent) {
         tab.attachEvent('onclick', function() {
             reloadTab('EMPADD')
         });
         tab.attachEvent('onmouseover', function() {
             setTabClass(document.getElementById("TabEmpAddress"), true);
         });
         tab.attachEvent('onmouseout', function() {
             setTabClass(document.getElementById("TabEmpAddress"), false);
          });
     }
     else
          tab.addEventListener('click', function() {
              reloadTab('EMPADD')
         }, false);
       
       
    tab = document.getElementById("TabqualInfo");
    image = document.getElementById("ImgqualInfo");
    tab.className = "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
            reloadTab('Qualification')
        });
        tab.attachEvent('onmouseover', function() {
            setTabClass(document.getElementById("TabqualInfo"), true);
        });
        tab.attachEvent('onmouseout', function() {
            setTabClass(document.getElementById("TabqualInfo"), false);
        });
    }
    else
        tab.addEventListener('click', function() {
            reloadTab('Qualification')
        }, false);
      
    tab = document.getElementById("TabDependent");
    image = document.getElementById("ImgDependent");
    tab.className = "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
            reloadTab('Dependent')
        });
        tab.attachEvent('onmouseover', function() {
            setTabClass(document.getElementById("TabDependent"), true);
        });
        tab.attachEvent('onmouseout', function() {
            setTabClass(document.getElementById("TabDependent"), false);
        });
    }
    else
        tab.addEventListener('click', function() {
            reloadTab('Dependent')
        }, false); 
    //
    
    
     tab = document.getElementById("TabMedIns");
    image = document.getElementById("ImgMedIns");
    tab.className = "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
            reloadTab('MEDIN')
        });
        tab.attachEvent('onmouseover', function() {
            setTabClass(document.getElementById("TabMedIns"), true);
        });
        tab.attachEvent('onmouseout', function() {
            setTabClass(document.getElementById("TabMedIns"), false);
        });
    }
    else
        tab.addEventListener('click', function() {
            reloadTab('MEDIN')
        }, false);  
    //
    tab = document.getElementById("Tabpreemp");
    image = document.getElementById("Imgpreemp");
    tab.className= "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
            reloadTab('PREEMP')
        });
        tab.attachEvent('onmouseover', function() {
            setTabClass(document.getElementById("Tabpreemp"), true);
        });
        tab.attachEvent('onmouseout', function() {
            setTabClass(document.getElementById("Tabpreemp"), false);
        });
    }
    else
        tab.addEventListener('click', function() {
            reloadTab('PREEMP')
        }, false);  
    
    tab = document.getElementById("TabEmpContract");
    image = document.getElementById("ImgEmpContract");
    if(document.getElementById("inpempCategory").value=='Y'){
        tab.className = "tabSelected";
        image.className = "imageSelected";
          if (tab.attachEvent) {
            tab.attachEvent('onclick', function() {
                reloadTab('EMPCTRCT')
            });
            tab.attachEvent('onmouseover', function() {
                setTabClass(document.getElementById("TabEmpContract"), true);
            });
            tab.attachEvent('onmouseout', function() {
                setTabClass(document.getElementById("TabEmpContract"), false);
             });
        }
        else
             tab.addEventListener('click', function() {
                 reloadTab('EMPCTRCT')
            }, false);
    }
    else{
         tab.className = "tabNotSelected";
         image.className = "imageNotSelected";
         tab.parentNode.replaceChild(tab.cloneNode(true), tab);
         $(tab).attr('onmouseover', "").attr('onmouseout', "").attr('onclick', "");
    }
 
    
    tab = document.getElementById("TabAsset");
    image = document.getElementById("ImgAsset");
    tab.className = "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
            reloadTab('Asset')
        });
        tab.attachEvent('onmouseover', function() {
            setTabClass(document.getElementById("TabAsset"), true);
        });
        tab.attachEvent('onmouseout', function() {
            setTabClass(document.getElementById("TabAsset"), false);
        });
    }
    else
        tab.addEventListener('click', function() {
            reloadTab('Asset')
        }, false);  
    tab = document.getElementById("TabDocument");
    image = document.getElementById("ImgDocument");
    tab.className = "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
            reloadTab('Dependent')
        });
        tab.attachEvent('onmouseover', function() {
            setTabClass(document.getElementById("TabDocument"), true);
        });
        tab.attachEvent('onmouseout', function() {
            setTabClass(document.getElementById("TabDocument"), false);
        });
    }
    else
        tab.addEventListener('click', function() {
            reloadTab('DOC')
        }, false);  
    
    tab = document.getElementById("TabEmpPerPayMethod");
    image = document.getElementById("ImgEmpPerPayMethod");
    tab.className = "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
           reloadTab('PERPAYMETHOD')
        });
        tab.attachEvent('onmouseover', function() {
            setTabClass(document.getElementById("TabEmpPerPayMethod"), true);
        });
        tab.attachEvent('onmouseout', function() {
            setTabClass(document.getElementById("TabEmpPerPayMethod"), false);
        });
    }
    else
        tab.addEventListener('click', function() {
           reloadTab('PERPAYMETHOD')
        }, false);
}


function setTabClass(tab, type) {
    if (type == true)
        tab.className = "tabSelectedHOVER";
    else
        tab.className = "tabSelected";
}

function resetTab() {
     resetByTab(document.getElementById("TabEmpInfo"), document.getElementById("ImgEmpInfo"));
    resetByTab(document.getElementById("TabEmpAddress"), document.getElementById("ImgEmpAddress"));
    resetByTab(document.getElementById("TabqualInfo"), document.getElementById("ImgqualInfo"));
     resetByTab(document.getElementById("TabDependent"), document.getElementById("ImgDependent"));
    resetByTab(document.getElementById("Tabpreemp"), document.getElementById("ImgEmpInfo"));
    resetByTab(document.getElementById("TabEmpContract"), document.getElementById("ImgEmpContract"));
    resetByTab(document.getElementById("TabMedIns"), document.getElementById("ImgMedIns"));
    resetByTab(document.getElementById("TabAsset"), document.getElementById("ImgAsset"));
    resetByTab(document.getElementById("TabDocument"), document.getElementById("ImgDocument"));
    resetByTab(document.getElementById("TabEmpPerPayMethod"), document.getElementById("ImgEmpPerPayMethod"));    
    var nextLink = document.getElementById("NextLink");
    nextLink.style.opacity = '0.5';
    nextLink.style.filter = 'alpha(opacity=50)';
    nextLink.style.cursor = '';
    nextLink.parentNode.replaceChild(nextLink.cloneNode(true), nextLink);
    $('#NextLink').attr('onclick', "");
}

function resetByTab(tab, image) {
    tab.className = "tabNotSelected";
    image.className = "imageNotSelected";
    tab.parentNode.replaceChild(tab.cloneNode(true), tab);
    $('#' + tab.id).attr('onmouseover', "").attr('onmouseout', "").attr('onclick', "");
}
function onClickSearch() {
    document.getElementById("messageBoxID").style.display = "none";
    if (document.getElementById("inpSearchType").value == "0")
        document.getElementById("inpSearchType").value = "1";
    else if (document.getElementById("inpSearchType").value == "1")
        document.getElementById("inpSearchType").value = "0";
    searchTitle();
    document.getElementById("gs_em_qhr_doj").value = "=";
    employeeGrid[0].clearToolbar();
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
        if (tab == 'EMP') {
            submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employee.header/Employee', '_self', null, true);
            return false;
        }
        else if (tab == 'EMPINF') {
            var employeeId=document.getElementById("inpEmployeeId").value;
             submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
             return false;
          
        }
        else if(tab =='EMPADD'){
            submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView', '_self', null, true);
            return false;
                 
                }
        else if(tab == 'Dependent') {
               var employeeId=document.getElementById("inpEmployeeId").value;
               submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
               return false;
              
               }
        else if (tab == 'Qualification') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification', '_self', null, true);
         return false;
         }
   else if (tab == 'PREEMP') {
       submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
        return false;
   }
   else if (tab == 'EMPCTRCT') {
       var employeeId=document.getElementById("inpEmployeeId").value;
       submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
       return false;
  
    }
        
        else if(tab == 'Asset') {
             var employeeId=document.getElementById("inpEmployeeId").value;
             submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
             return false;
          
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
function cancelEmployee(){
    OBConfirm('<%= Resource.getProperty("hcm.confirm.cancel", lang) %>',
          function (result){
              if(result){
                  $.post('<%=request.getContextPath()%>/EmployeeAjax', {
                         action : 'checkCancel',
                         inpEmpid : document.getElementById("inpEmployeeId").value
                     }, function(data) {
                         var result = data.getElementsByTagName("result")[0].childNodes[0].nodeValue;
                         if(result=='true'){
                             document.getElementById("inpAction").value="Cancel";
                             submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employee.header/Employee', '_self', null, true);
                              return false;
                         }
                         else{
                             displayMessage("E", '<%=Resource.getProperty("hcm.error",lang)%>', '<%= Resource.getProperty("hcm.processfailed", lang) %>')
                             document.getElementById("inpEmployeeId").value = "";
                             employeeGrid.trigger("reloadGrid");
                             return false;
                         }
                     });
                 
              }
      });
        }

</script>
</HTML>
