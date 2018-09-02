<!--
 *************************************************************************
 * All Rights Reserved.
 * Contributor(s): Gopalakrishnan
 *************************************************************************
-->
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
<HTML xmlns="http://www.w3.org/1999/xhtml">
  <HEAD>
   <META http-equiv="Content-Type" content="text/html; charset=utf-8"></META> 
   <LINK rel="stylesheet" type="text/css" href="<%=style %>" id="paramCSS"></LINK>
   <link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>"></link>
    <link rel="stylesheet"  href="../web/js/jquery/css/demos.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/themes/base/jquery.ui.autocomplete.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/ui.jqgrid.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/common/select2.min.css"></link>    
    <style type="text/css">
    </style>
    <script type="text/javascript" src="../web/js/utils.js"></script>
    <script type="text/javascript" src="../web/js/windowKeyboard.js"></script>
    <script type="text/javascript" src="../web/js/searchs.js"></script>
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
        <% request.setAttribute("SaveStatus",request.getAttribute("SaveStatus")==null?"":request.getAttribute("SaveStatus"));
        if(request.getAttribute("SaveStatus").equals("Add-True")) { %>displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.saveoreditsuccess",lang) %>");<% }
        else if(request.getAttribute("SaveStatus").equals("Update-True")) { %>displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.saveoreditsuccess",lang) %>");<% }
        else if(request.getAttribute("SaveStatus").equals("False")) { if(request.getAttribute("ErrorMsg")!=null){%>displayMessage("E", "<%=Resource.getProperty("hcm.error",lang)%>","<%= (String)request.getAttribute("ErrorMsg") %>") <%}else{%>displayMessage("E", "<%=Resource.getProperty("hcm.error",lang)%>","<%= Resource.getProperty("hcm.saveorediterror",lang) %>");<% }} %>
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
        <INPUT type="hidden" name="inpAction" id="inpAction" value=""></INPUT>
        <INPUT type="hidden" name="SubmitType" id="SubmitType" value=""></INPUT>
        <INPUT type="hidden" name="inpEmployeeId" id="inpEmployeeId" value="${requestScope.inpEmployeeId}"></INPUT>
        <INPUT type="hidden" name="inpempCategory" id="inpempCategory" value="<%= request.getAttribute("inpempCategory") %>"></INPUT>
        <INPUT type="hidden" name="inpContractId" id="inpContractId" value="${requestScope.inpContractId}"></INPUT>
        <INPUT type="hidden" name="inpHireDate" id="inpHireDate" value="${requestScope.inpHireDate}"></INPUT>
        <INPUT type="hidden" name="inpAddressId" id="inpAddressId" value="${requestScope.inpAddressId}"></INPUT>
        <INPUT type="hidden" name="inptodayDate" id="inptodayDate" value="${requestScope.inptodayDate}"></INPUT>
            <INPUT type="hidden" name="inpminconser" id="inpminconser" value="<%= request.getAttribute("inpminconser") %>"></INPUT>
      <INPUT type="hidden" name="inpmaxconser" id="inpmaxconser" value="<%= request.getAttribute("inpmaxconser") %>"></INPUT>
        <INPUT type="hidden" name="inpNextTab" id="inpNextTab" value=""></INPUT>
           <INPUT type="hidden" name="inpEmpStatus" id="inpEmpStatus" value="<%= request.getAttribute("inpEmpStatus") %>"></INPUT>
      <INPUT type="hidden" id="inpEmployeeStatus" name="inpEmployeeStatus" value="<%= request.getAttribute("inpEmployeeStatus") %>"></INPUT>
      <INPUT type="hidden" id="inpCancelHiring" name="inpCancelHiring" value="<%= request.getAttribute("CancelHiring") %>"></INPUT>
        
        <jsp:include page="/web/jsp/ProcessBar.jsp"/>
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
                                 <td class="Main_ToolBar_Space"></td>
                                 <td style="width: 100%;"><div style="display: inline-block; position: relative; top: 8px; right: 30px; float: right;">
                                  <button onclick="correctContract();" class="ButtonLink" style="display:none;" id="correctionButton" type="button">
                                        <table class="Button">
                                            <tbody><tr>
                                                <td class="Button_left"><img border="0" src="../web/images/blank.gif" title="Cancel" alt="Cancel" class="Button_Icon Button_Icon_process"></td>
                                                <td id="Submit_BTNname" class="Button_text"><%= Resource.getProperty("hcm.correction", lang) %></td>
                                                <td class="Button_right"><img border="0" src="../web/images/blank.gif" title="Close" alt="Close" class="Button_Icon Button_Icon_process"></td>
                                            </tr></tbody>
                                        </table>
                                    </button>
                               </div></td>
                            </tr>
                        </table>
                    </DIV>
                    <TABLE width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_TabPane" id="tdtopTabs">
                          <TR id="paramParentTabContainer"><td class="tabBackGroundInit"></td></TR>
                          <TR id="paramMainTabContainer"><td class="tabBackGroundInit"><span style="background: none;" class="tabTitle"></span></td></tr>
                          <tr><td class="tabBackGround">
                            <div class="marginLeft">
                            <div><span class="dojoTabcurrentfirst"><div>
                                <span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%= Resource.getProperty("hcm.Contract",lang)%></a></span>
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
                                                <td><div id="TabEmpInfo" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadFunction('EMPINF');"><span class="LabelText"><%= Resource.getProperty("hcm.empmntinfo",lang)%></span></div>
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
                                               <td><img id="ImgEmpContract" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                             <td><div id="TabEmpContract" class="tabCurrent"><span class="LabelText">Contract</span></div>
                                              <div style="text-align: center;"><img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img></div>
                                             </td>
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
                                     <div id="TabEmpPerPayMethod" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadFunction('PERPAYMETHOD');"><span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></span>
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
                                                      <DIV id="ContractDetails">
                                                            <table style="width: 100%;padding: 2px;">
                                                            <tr>
                                                            <td>
                                                             <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                              <TBODY>
                                                               <TR class="FieldGroup_TopMargin"></TR>
                                                               <TR>
                                                                   <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                   <TD class="FieldGroupTitle" id="subGridTitle">Contract Details</TD>
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
                                                            <TR>
                                                                <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.contractType", lang)%></span></TD>
                                                                <td class="TextBox_ContentCell"><select id="inpContractType" name="inpContractType" class="ComboKey Combo_TwoCells_width" onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm()">
                                                                <option value='P'><%= Resource.getProperty("hcm.periodic.contract", lang)%></option>
                                                                <option value='T'><%= Resource.getProperty("hcm.task.contract", lang)%></option>
                                                                <option value='R'><%= Resource.getProperty("hcm.renewal.contract", lang)%></option>
                                                                <option value='TR'><%= Resource.getProperty("hcm.terminate.contract", lang)%></option>
                                                                </select></td>
                                                                <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.trxStatus", lang)%></span></TD>
                                                                <TD class="TextBox_ContentCell"  colspan="3">
                                                                    <input type="text" name="inpTrxStatus" id="inpTrxStatus"  maxlength="30" value="<% if (request.getAttribute("inpTrxStatus").equals("ISS")){%> <%=Resource.getProperty("hcm.issued", lang)%> <% }else{ %> <%=Resource.getProperty("hcm.underproc", lang) %><%}%>"  class="dojoValidateValid TextBox_TwoCells_width"  readonly></input>
                                                                </TD>
                                                                <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.contractNo", lang)%></span></TD>
                                                                <TD class="TextBox_ContentCell"> 
                                                                    <input type="text" name="inpContractNo" id="inpContractNo"  maxlength="30" value="${requestScope.inpContractNo}" class="dojoValidateValid TextBox_TwoCells_width required" onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm()"></input>
                                                                </TD>
                                                            </TR>
                                                             
                                                            <TR>
                                                                <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.startdate", lang) %></span></TD>
                                                                <td class="TextBox_ContentCell">
                                                                    <input type="text" id="inpStartDate" class="dojoValidateValid TextBox_btn_OneCell_width required" value="${requestScope.inpStartDate}" maxlength="10" name="inpStartDate"  onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"></input>
                                                                </td>
                                                                 
                                                                 <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.Duration", lang) %></span></TD>
                                                                 <TD class="">                                                       
                                                                <input type="text" id="inpDuration" class="dojoValidateValid" value="${requestScope.inpDuration}" maxlength="4" name="inpDuration"  onkeypress='return event.charCode >= 48 && event.charCode <= 57' onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); dateValidation();"
                                                                  style="width: 50px;background-color: #FFFFCC !important;"></input>
                                                                 </TD>
                                                                <TD class="" align="right" style="min-width:60px;"><span class="LabelText" ><%= Resource.getProperty("hcm.DurationType", lang) %></span></TD>
                                                                <td class="TextBox_ContentCell"><select id="inpDurationType" name="inpDurationType" class="ComboKey Combo_TwoCells_width"  onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();dateValidation();" style="width: 148px">
                                                                     <option value='d'><%= Resource.getProperty("hcm.days", lang) %></option>
                                                                     <option value='w'><%= Resource.getProperty("hcm.weeks", lang) %></option>
                                                                     <option value='m'><%= Resource.getProperty("hcm.months", lang) %></option>
                                                                      <option value='y'><%= Resource.getProperty("hcm.years", lang) %></option>
                                                                     </select></td>
                                                                   <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.expiryDate", lang) %></span></TD>
                                                                <td class="TextBox_ContentCell">
                                                                    <input type="text" id="inpEndDate" class="dojoValidateValid TextBox_btn_OneCell_width" value="${requestScope.inpEndDate}" maxlength="10" name="inpEndDate"   readonly></input>
                                                                </td>
                                                            </TR>
                                                            
                                                            <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.jobDescription", lang) %></span></TD>
                                                            <td colspan="4"><textarea id="inpJobDescription"   maxlength="2000" name="inpJobDescription" class="dojoValidateValid TextArea_FourCells_width TextArea_Medium_height"  onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm()"><%= request.getAttribute("inpJobDescription") %></textarea></td>  
                                                            <td colspan="3" rowspan="5">
                                                                <DIV id="SalaryGroup"  style="display:none;">
                                                                <table style="width: 100%;padding: 2px;">
                                                                <tr>
                                                                <td>
                                                                 <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                                  <TBODY>
                                                                   <TR class="FieldGroup_TopMargin"></TR>
                                                                   <TR>
                                                                       <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                       <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.salaryDetails", lang) %></TD>
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
                                                                <div id="jqgrid" style="width:100%; display:none;">
                                                                   <div align="center"><table id="SalaryList" class="scroll" cellpadding="0" cellspacing="0" width="100%"></table><div id="pager" class="scroll" style="text-align: center;"></div></div>
                                                                </div>
                                                            </td>
                                                            </TR>
                                                            <tr>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.contractDesc", lang) %></span></TD>
                                                            <td colspan="4"><textarea id="inpContractDesc" maxlength="2000" name ="inpContractDesc" class="dojoValidateValid TextArea_FourCells_width TextArea_Medium_height" onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm()"><%= request.getAttribute("inpContractDesc") %></textarea></td>  
                                                            </tr>
                                                             <tr>
                                                                <TD class="TitleCell"  align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.grade", lang) %></span></TD>
                                                                 <td class="TextBox_ContentCell" ><select id="inpGrade" style="width:180px;" name="inpGrade"   onkeydown="return onChangeEvent(event);" class="Combo Combo_OneCells_width"  onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();generateJob(this.value);">
                                                                </select>
                                                                </td>
                                                                  <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.emp.jobno", lang) %></span></TD>
                                                                
                                                                  <td class="TextBox_ContentCell" colspan="2"><select id="inpJobNo" style="width:160px;" name="inpJobNo" class="Combo Combo_OneCells_width" onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm()" >
                                                                </select>
                                                                </td>
                                                            </tr>
                                                            
                                                            <tr>
                                                             <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.letterNo", lang) %></span></TD>
                                                                 <td> <input type="text" name="inpletterNo" id="inpletterNo"  maxlength="30"  value="${requestScope.inpletterNo}" onkeydown="return onChangeEvent(event);" class="dojoValidateValid TextBox_TwoCells_width required" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"></input>
                                                                </td>
                                                               <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.letterDate", lang) %></span></TD>
                                                                <td class="TextBox_ContentCell" colspan="2">
                                                                    <input type="text" id="inpLettrDate" class="dojoValidateValid TextBox_btn_OneCell_width required" value="${requestScope.inpLettrDate}" maxlength="10" name="inpLettrDate"  onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"></input>
                                                                </td>
                                                            </tr>
                                                             <tr>
                                                             <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.DecisionNo", lang) %></span></TD>
                                                                 <td> <input type="text" name="inpDecisionNo" id="inpDecisionNo"  maxlength="30" value="${requestScope.inpDecisionNo}" class="dojoValidateValid TextBox_TwoCells_width " onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm()"></input>
                                                                </td>
                                                               <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.DecisionDate", lang) %></span></TD>
                                                                <td class="TextBox_ContentCell" colspan="2">
                                                                    <input type="text" id="inpDecisionDate" class="dojoValidateValid TextBox_btn_OneCell_width " value="${requestScope.inpDecisionDate}" maxlength="10" name="inpDecisionDate" readonly></input>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.annualBalance", lang)%> </span></TD>
                                                                 <td colspan="7" > <input type="number" name="inpAnnualBalance" id="inpAnnualBalance"  maxlength="30" value="${requestScope.inpAnnualBalance}" class="dojoValidateValid TextBox_TwoCells_width " onkeydown="return onChangeEvent(event);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm()"></input>
                                                                </td>
                                                            </tr>
                                                       </TABLE>
                                                       </div>
                                                       <DIV id="button">
                                                              <TABLE style="width:80%; margin-top: 10px;">
                                                              
                                                               <TR>
                                                                   <TD colspan="6" align="center">
                                                                       <div>
                                                                         <BUTTON type="button" id="inpissueDecision" class="ButtonLink" onclick="IssueDecision();" style="margin: 10px 0;">
                                                                              <TABLE class="Button">
                                                                                  <TR>
                                                                                      <TD class="Button_left"><IMG class="Button_Icon Button_Icon_process" alt="Issue Decision" title="Issue Decision" src="../web/images/blank.gif" border="0"></IMG></TD>
                                                                                      <TD class="Button_text" id="IssueButton_Nname"><%= Resource.getProperty("hcm.issuedecision",lang)%></TD>
                                                                                      <TD class="Button_right"></TD>
                                                                                  </TR>
                                                                              </TABLE>
                                                                          </BUTTON>
                                                                      </div>
                                                                  </TD>
                                                               </TR>
                                                              </TABLE>
                                                    </DIV> 
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
<script type="text/javascript">
var currentWindow='EMP',contextPath = '<%=request.getContextPath()%>';
var select = "<%=Resource.getProperty("finance.select", lang)%>",
gradeId='<%=request.getAttribute("inpGrade")%>',
jobNo ='<%=request.getAttribute("inpJobNo")%>',
changevalueask='<%= Resource.getProperty("hcm.changedvaluessave", lang) %>',
contractType='<%=request.getAttribute("inpContractType")%>',
durationType='<%=request.getAttribute("inpDurationType")%>',
askIssue='<%= Resource.getProperty("hcm.askIssue", lang) %>';
var contractserviceEmpty='<%= Resource.getProperty("hcm.contractserviceEmpty", lang) %>';
var contractservice='<%= Resource.getProperty("hcm.contractService", lang) %>';
var contractserviceto='<%= Resource.getProperty("hcm.to", lang) %>';
var contractserviceyears='<%= Resource.getProperty("hcm.years", lang) %>';
var issued="<%=request.getAttribute("inpTrxStatus")%>"
var gradeID='<%=request.getAttribute("inpGrade")==null ? "":request.getAttribute("inpGrade")%>';
var gradeName='<%=request.getAttribute("inpGradeName")==null ? "" :request.getAttribute("inpGradeName")%>';
var jobID='<%=request.getAttribute("inpJobNo")==null ? "":request.getAttribute("inpJobNo")%>';
var jobNum='<%=request.getAttribute("inpJobName")==null ? "" :request.getAttribute("inpJobName")%>';
var lang ='<%=lang%>',
direction='ltr';
    if(lang=="ar_SA"){
        direction='rtl';
    }
</script>
<script type="text/javascript">onLoadDo();</script>
<script type="text/javascript" src="../web/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../web/js/common/common.js"></script>
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.plugin.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura-ar.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.plus.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.ext.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="../web/js/jquery/grid.locale-en.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.widget.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.mouse.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.button.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.position.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.autocomplete.js"></script>
<script type="text/javascript" src="../web/js/jquery/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="../web/js/inlineNavGrid.js"></script>  
<script type="text/javascript" src="../web/js/common/select2.min.js"></script>
<script type="text/javascript" src="../web/sa.elm.ob.hcm/js/Contract.js?t=<%= new java.util.Date().getTime() %>"></script>
<script type="text/javascript">
var calendarInstance = $.calendars.instance('ummalqura');
    $("#inpGrade").select2(selectBoxAjaxPaging({
        url : function() {
            return '<%=request.getContextPath()%>/ContractAjax?action=getGrade'     
        },
        data: [{
              id: gradeID,
              text: gradeName
            }],
        
        size : "small"
    }));
    $("#inpGrade").on("select2:unselecting", function (e) {
        document.getElementById("inpGrade").options.length = 0;
      });     
    $("#inpJobNo").select2(selectBoxAjaxPaging({
        url : function() {
            return '<%=request.getContextPath()%>/ContractAjax?action=getJobNo'     
        },
        data: [{
            id: jobID,
            text: jobNum
            }],      
        size : "small"
    }));
    $("#inpJobNo").on("select2:unselecting", function (e) {
        document.getElementById("inpJobNo").options.length = 0;
      }); 
document.getElementById("select2-inpJobNo-container").style.backgroundColor="#F5F7F1"; 
document.getElementById("select2-inpGrade-container").style.backgroundColor="#F5F7F1"; 
<%if(!request.getAttribute("inpTrxStatus").equals("ISS")) {%>
$('#inpStartDate').calendarsPicker({calendar: calendarInstance,dateFormat: 'dd-mm-yyyy',onSelect: function() {
        dateValidation();
        enableForm();} ,showTrigger:  
'<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger" >'});

/*       $('#inpDecisionDate').calendarsPicker({calendar:  
    $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect: function() {enableForm();} ,showTrigger:  
'<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger" >'}); */

$('#inpLettrDate').calendarsPicker({calendar: calendarInstance, dateFormat: 'dd-mm-yyyy',onSelect: function() {enableForm();} ,showTrigger:  
'<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger" >'});
<% } %>
</script>
</HTML>