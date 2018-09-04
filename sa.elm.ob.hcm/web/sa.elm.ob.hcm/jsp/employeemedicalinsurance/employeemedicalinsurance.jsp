<%@page import="sa.elm.ob.hcm.ad_forms.MedicalInsurance.vo.MedicalInsuranceVO"%>
<%@page import="sa.elm.ob.hcm.EhcmDependents"%>
<%@page import="sa.elm.ob.hcm.EHCMDeflookupsTypeLn"%>
<%@page import="sa.elm.ob.hcm.EhcmMedicalInsurance"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="sa.elm.ob.utility.util.Utility"%>
<%@page import="java.util.List, java.util.ArrayList"%>
<%@page import="sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO"%>
<%@page import="org.codehaus.jettison.json.JSONObject"%>
<%@page import="org.codehaus.jettison.json.JSONArray"%>
<%@ page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>
<%@page import="sa.elm.ob.utility.util.UtilityVO"%>

  
<%
     String lang = ((String)session.getAttribute("#AD_LANGUAGE"));
    /*  List<EHCMDeflookupsTypeLn> insuranceSchemaList =  (ArrayList<EHCMDeflookupsTypeLn>)request.getAttribute("inpInsuSchema"); */
     /* List<MedicalInsuranceVO> dependentList = (ArrayList<MedicalInsuranceVO>)request.getAttribute("inpDependent"); */
     List<MedicalInsuranceVO> MedicalInsuVOList = (ArrayList<MedicalInsuranceVO>)request.getAttribute("inpInsuCategory");
     
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
    <LINK rel="stylesheet" type="text/css" href="<%=toolBarStyle %>" ></LINK>
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
    <script type="text/javascript" src="../web/js/common/select2.min.js"></script>
   <!--  <script type="text/javascript" src="..web/sa.elm.ob.hcm/js/dependents/dependents.js"></script> -->
    
    
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
         $('#inpStartDate').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect: function() {enableForm();}, showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger" >'});
         
         $('#inpEndDate').calendarsPicker({calendar:  
             $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect: function() {enableForm();}, showTrigger:  
         '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
         <% } %>
    }
    function onResizeDo()
    {
        resizeArea();
        reSizeGrid();
    }
    function onLoad()
    {
        <%  request.setAttribute("savemsg",request.getAttribute("savemsg")==null?"":request.getAttribute("savemsg")); %>
        <%  if(request.getAttribute("savemsg").equals("Success")) { %>
        displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.saveoreditsuccess", lang) %>");
        <%}%>
       
         if(document.getElementById('inpCancelHiring').value == "true" )
         {
          document.getElementById('linkButtonNew').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled';
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
/*         hideMessage();
 */         if(( document.getElementById('inpInsComName').value !="" ) &&   (document.getElementById('inpInsuCategory').value !="")   &&  ( document.getElementById('inpInsuSchema').value !="")  &&
                (document.getElementById('inpMemshipNo').value !="" )&&   (document.getElementById('inpStartDate').value !="")  ){
     changesFlag = 1;

     enableSaveButton("true");

         }
        else{
            changesFlag = 0;

             enableSaveButton("false");
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
        <INPUT type="hidden" name="inpIsActive" id="inpIsActive" value="<%= request.getAttribute("inpIsActive") %>"></INPUT>
        <INPUT type="hidden" id="inpMedInsId" name="inpMedInsId" value="<%= request.getAttribute("inpMedInsId") %>"></INPUT>
        <INPUT type="hidden" name="inpempCategoryKey" id="inpempCategoryKey" value="<%= request.getAttribute("inpInsuCategoryKey") %>"></INPUT>
        <INPUT type="hidden" name="select2InsuSchemaId" id="select2InsuSchemaId" value="<%= request.getAttribute("inpInsuSchemaId") %>"></INPUT> 
        <INPUT type="hidden" name="select2InsuSchemaName" id="select2InsuSchemaName" value="<%= request.getAttribute("inpInsuSchemaName") %>"></INPUT> 
        <INPUT type="hidden" id="inpEmployeeId" name="inpEmployeeId" value="<%= request.getAttribute("inpEmployeeId") %>"></INPUT>
        <INPUT type="hidden" name="inpempCategory" id="inpempCategory" value="<%= request.getAttribute("inpempCategory") %>"></INPUT>
        <INPUT type="hidden" id="SubmitType" name="SubmitType" value="<%= request.getAttribute("SubmitType") %>"></INPUT>
        <INPUT type="hidden" id="inpGender" name="inpGender" value="<%= request.getAttribute("inpGender") %>"></INPUT>
        <INPUT type="hidden" id="inpAddressId" name="inpAddressId" value="<%= request.getAttribute("inpAddressId") %>"></INPUT>
        <INPUT type="hidden" id="empMarialSt" name="empMarialSt" value=""></INPUT>
        <INPUT type="hidden" id="empGender" name="empGender" value=""></INPUT>
        <INPUT type="hidden" name="inpEmpStatus" id="inpEmpStatus" value="<%= request.getAttribute("inpEmpStatus") %>"></INPUT>
        <INPUT type="hidden" id="inpEmployeeStatus" name="inpEmployeeStatus" value="<%= request.getAttribute("inpEmployeeStatus") %>"></INPUT>
       <INPUT type="hidden" id="inpCancelHiring" name="inpCancelHiring" value="<%= request.getAttribute("CancelHiring") %>"></INPUT>
        <INPUT type="hidden" name="select2DependentId" id="select2DependentId" value="<%= request.getAttribute("inpDependentId") %>"></INPUT>
      <INPUT type="hidden" name="select2DependentName" id="select2DependentName" value="<%= request.getAttribute("inpDependentName") %>"></INPUT>
      <INPUT type="hidden" name="inpEmployeeIsActive" id="inpEmployeeIsActive" value="<%= request.getAttribute("inpEmployeeIsActive") %>"></INPUT>
        
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
                            <%--<td width="2%" ><a href="javascript:void(0);" onClick="onClickNew();" class="Main_ToolBar_Button" onMouseOver="window.status='Create a New Record';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonNew"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.new",lang)%>" border="0" id="linkButtonNew"></a></td> 
                            <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>--%>
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
                                <span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%= Resource.getProperty("hcm.MedicalInsurance",lang)%></a></span>
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
                                                         
                                                             <td><img  id="ImgDependent" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                             <td><div id="TabDependent" class="tabSelected"  onmouseout="this.className='tabSelected';" onmouseover="this.className='tabSelectedHOVER';" onclick="reloadWindow('Dependent');"><span class="LabelText"><%= Resource.getProperty("hcm.dependent",lang)%></span></div></td>
                                                            

                                                             <td><img  id="ImgPreEmp" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                             <td><div id="Tabpreemp" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('PREEMP');" ><span class="LabelText"><%= Resource.getProperty("hcm.emppreemp",lang)%></span></div>
                                                             </td>
                                                             <% if(request.getAttribute("inpempCategory").toString().equals("Y")) { %>
                                                             <td><img id="ImgEmpContract" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                             <td><div id="TabEmpContract" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPCTRCT');" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                                                              <%}else{ %>
                                                               <td><img id="ImgEmpContract" class="imageNotSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                                <td><div id="TabEmpContract" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                                                             <%} %>
                                                             
                                                             
                                                                     <td>
                                                                <img  id="ImgMedIns" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img>
                                                             </td>
                                                            <td>
                                                            <div id="TabMedIns" class="tabCurrent">
                                                                <span class="LabelText"><%= Resource.getProperty("hcm.MedicalInsurance",lang)%>
                                                                </span>
                                                            </div>
                                                            <div style="text-align: center;">
                                                                <img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img>
                                                            </div>
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
                                                                <div id="TabDocument" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';"  onclick="reloadWindow('DOC');">
                                                                    <span class="LabelText"><%= Resource.getProperty("hcm.documents",lang)%></span>
                                                                </div>
                                                            </td>
                                                              <td><img  id="ImgEmpPerPayMethod" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                   <td>
                                     <div id="TabEmpPerPayMethod" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('PERPAYMETHOD');"><span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></span>
                                     </div>
                                    </td>
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
                                                     <div align="center">
                                                        <TABLE style="width:80%; margin-top: 10px;">
                                                            <TR>
                                                            <TD class="TextBox_ContentCell" >
                                                                 <input type="text" name="inpName1" id="inpName1" maxlength="59" readonly value="<%= Utility.escapeQuote(request.getAttribute("inpName1")) %>" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;"  dir="rtl"></input>
                                                                 <input type="hidden" name="inpHidName1" id="inpHidName1" value="<%= Utility.escapeQuote(request.getAttribute("inpName1")) %>"></input>
                                                            </TD>
                                                            <TD class="TitleCell"  align="left" style="min-width:80px;padding-left: 10px"><span class="LabelText"><%= Resource.getProperty("hcm.arabicname", lang)%></span></TD>
                                                             <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.fullname", lang)%></span></TD>
                                                             <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpName2" id="inpName2" maxlength="59" readonly value="<%= Utility.escapeQuote(request.getAttribute("inpName2")) %>" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;" ></input>
                                                                 <input type="hidden" name="inpHidName2" id="inpHidName2" value="<%= Utility.escapeQuote(request.getAttribute("inpName2")) %>"></input>
                                                             </TD>
                                                             <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.code", lang)%></span></TD>
                                                             <TD class="TextBox_ContentCell">
                                                                <input type="text" name="inpEmpNo" id="inpEmpNo" maxlength="59" readonly value="<%= Utility.escapeQuote(request.getAttribute("inpEmpNo")) %>" class="dojoValidateValid required TextBox_TwoCells_width" style="color: black;"></input>
                                                                <input type="hidden" name="inpHidEmpNo" id="inpHidEmpNo" value="<%= Utility.escapeQuote(request.getAttribute("inpEmpNo")) %>"></input>
                                                            </TD>
                                                         </TR>
                                                        </TABLE>
                                                    </div>
                                                      <TABLE style="width:80%; margin-top: 10px;">
                                                          <TR>
                                                             <TD colspan="6">
                                                               <table style="width: 80%;">
                                                                   <tr>
                                                                    <td>
                                                                       <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                                           <TBODY>
                                                                               <TR class="FieldGroup_TopMargin"></TR>
                                                                               <TR>
                                                                                   <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                                   <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.MedicalInsurance",lang)%></TD>
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
                                                                 
                                                                        <TD class="TitleCell" align="left" style="width:110px;">
                                                                            <span class="LabelText" ><%= Resource.getProperty("hcm.insurcompanyname", lang)%>
                                                                            </span>
                                                                        </TD>
                                                                        <TD class="TextBox_ContentCell"> 
                                                                              <%  
                                                                            if(request.getAttribute("inpInsComName")!= null ) {%>
                                                                                <input type="text" name="inpInsComName" id="inpInsComName" maxlength="35" style="overflow: hidden; text-overflow:ellipsis;" value="<%= request.getAttribute("inpInsComName") %>" class="dojoValidateValid  TextBox_TwoCells_width required"  onchange="enableForm();" onkeydown="return onChangeEvent(event);"></input>                                       
                                                                             <%}
                                                                              else{ %>
                                                                                <input type="text" name="inpInsComName" id="inpInsComName" maxlength="35" style="overflow: hidden; text-overflow:ellipsis;" value="" required="false"   class="dojoValidateValid  TextBox_TwoCells_width required"  onchange="enableForm();" onkeydown="return onChangeEvent(event);"></input>                                   
                                                                              <%} %>  
                                                                                                                   
                                                                         </TD>
                                                                        
                                                                    <!-- </TR>
                                                                    <TR> -->

                                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.insutype",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" >
                                                            <SELECT id="inpInsuCategory" name="inpInsuCategory" class="ComboKey Combo_TwoCells_width" onkeydown="return true;" onkeyup="enableForm();" onchange="showDependent(this.value); enableForm();">
                                                              <% if(MedicalInsuVOList!=null && MedicalInsuVOList.size()>0) {  %>
                                                             
                                                                <% for( MedicalInsuranceVO vo:MedicalInsuVOList){
                                                                %>
                                                                <option value='<%= vo.getSearchkey()%>' <%if(request.getAttribute("inpInsuCategoryKey") !=null && request.getAttribute("inpInsuCategoryKey").equals(vo.getSearchkey())){%> selected<%} %>  ><span><%= vo.getName()%></span></option>
                                                                <%}}%>
                                                            </SELECT>
                                                       </TD> 
                                                                       
                                                                       
                                                                       
                                                                       
                                                                       
                                                                       
                                                                        <TD id="inpDependentLabel" class="TitleCell" align="right" style="min-width:110px; display:none"><span class="LabelText" ><%= Resource.getProperty("hcm.dependent",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" >
                                                            <SELECT id="inpDependent" name="inpDependent" style="display:none"class="Combo Combo_TwoCells_width Combo_focus" onkeydown="return true;" onkeyup="enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();">
                                                              <%-- <% if(dependentList!=null && dependentList.size()>0) {  %>
                                                                <option value='0' ><%= Resource.getProperty("hcm.select",lang)%></option>
                                                                <% for(MedicalInsuranceVO vo:dependentList){%>                                                    
                                                                  <option value='<%= vo.getDependentId()%>' <%if(request.getAttribute("inpDependentId") !=null && request.getAttribute("inpDependentId").equals(vo.getDependentId())){%> selected<%} %>  ><span><%= vo.getRelationship()+"-"+vo.getName() %></span></option>   
                                                                <%}}%> --%>
                                                            </SELECT>
                                                       </TD>
                                                          
                                                                   <TR>  
                                                                         
                                                            
                                                                         
                                                                         
                                                                                  <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.insuschema",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" >
                                                            <SELECT id="inpInsuSchema" name="inpInsuSchema" class="ComboKey Combo_TwoCells_width" onkeydown="return true;" onkeyup="enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); ">
                                                              <%-- <% if(insuranceSchemaList!=null && insuranceSchemaList.size()>0) { %>
                                                               
                                                                <% for(EHCMDeflookupsTypeLn vo:insuranceSchemaList){%>  
                                                                <option value='<%= vo.getId()%>' <%if(request.getAttribute("inpInsuSchemaId") !=null && request.getAttribute("inpInsuSchemaId").equals(vo.getId())){%> selected<%} %>  ><span><%= vo.getName()%></span></option>
                                                                <%}}%> --%>
                                                            </SELECT>
                                                       </TD>
                                                                         
                                                                         
                                                                           <TD class="TitleCell" align="left" style="width:110px;">
                                                                            <span class="LabelText" ><%= Resource.getProperty("hcm.membershipno", lang)%>
                                                                            </span>
                                                                        </TD>
                                                                        <TD class="TextBox_ContentCell"> 
                                                                             <%  
                                                                            if(request.getAttribute("inpMemshipNo")!= null ) {%>
                                                                                <input type="text" name="inpMemshipNo" id="inpMemshipNo" maxlength="35" style="overflow: hidden; text-overflow:ellipsis;" value="<%= request.getAttribute("inpMemshipNo") %>" class="dojoValidateValid  TextBox_TwoCells_width required"  onchange="enableForm();" onkeydown="return onChangeEvent(event);"></input>                                       
                                                                             <%}
                                                                              else{ %>
                                                                                <input type="text" name="inpMemshipNo" id="inpMemshipNo" maxlength="35" style="overflow: hidden; text-overflow:ellipsis;" value="" required="false"   class="dojoValidateValid  TextBox_TwoCells_width required"  onchange="enableForm();" onkeydown="return onChangeEvent(event);"></input>                                   
                                                                              <%} %> 
                                                                                                                 
                                                                         </TD>
                                                                    
                                                        
                                                                   </TR> 
                                                                   <TR> 
                                                                       <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.startdate",lang)%></span></TD>
                                                                       <TD class="TextBox_btn_ContentCell">
                                                                        <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                                          <tr>
                                                                              <td class="TextBox_ContentCell">
                                                                                <%  
                                                                                if(request.getAttribute("inpStartDate")!= null ) {%>
                                                                                     <input type="text" id="inpStartDate" class="dojoValidateValid TextBox_btn_OneCell_width required" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpStartDate") %>" maxlength="10" name="inpStartDate" ></input> 
                                                                                 <%}
                                                                                  else{ %>
                                                                                  <input type="text" id="inpStartDate" class="dojoValidateValid TextBox_btn_OneCell_width required" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("today") %>" maxlength="10" name="inpStartDate" ></input>
                                                                                  <%} %> 
<%--                                                                                   <input type="text" id="inpStartDate" class="dojoValidateValid TextBox_btn_OneCell_width required" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("today") %>" maxlength="10" name="inpStartDate" ></input> 
 --%>                                                                                  
                                                                              </td>
                                                                         </tr>
                                                                        </table>
                                                                     </TD>
                                                                      <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.enddate",lang)%></span></TD>
                                                                       <TD class="TextBox_btn_ContentCell" >
                                                                        <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                                          <tr>
                                                                                <td class="TextBox_ContentCell">
                                                                                 <%  
                                                                                if(request.getAttribute("inpEndDate")!= null ) {%>
                                                                                     <input type="text" id="inpEndDate" class="dojoValidateValid TextBox_btn_OneCell_width " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); " readonly value="<%= request.getAttribute("inpEndDate") %>" maxlength="10" name="inpEndDate" ></input> 
                                                                                 <%}
                                                                                  else{ %>
                                                                                  <input type="text" id="inpEndDate" class="dojoValidateValid TextBox_btn_OneCell_width " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="" maxlength="10" name="inpEndDate" ></input>
                                                                                  <%} %>  
<%--                                                                                   <input type="text" id="inpEndDate" class="dojoValidateValid TextBox_btn_OneCell_width " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); " readonly value="<%= request.getAttribute("today") %>" maxlength="10" name="inpEndDate" ></input> 
 --%>                                                                             </td>
                                                                         </tr>
                                                                        </table>
                                                                       </TD>
                                                                    <!-- </TR>   
                                                                    <TR> -->
                                                            
                                                                    </TR> 
                                                                    <TR> 
                                                              
                                                                   <!--  </TR>  
                                                                    <TR> --> 
                                                          
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
<script type="text/javascript">onLoadDo();</script>
<script type="text/javascript">
var contextPath = '<%=request.getContextPath()%>', currentTab = 'MEDIN';
var validNatvalidFailed='0',validNatexistFailed='0' ;
/* 
function onClickEditView() {
    if (document.getElementById('buttonEdition').className == 'Main_ToolBar_Button' && document.getElementById("inpEmployeeId").value != "") {
        document.getElementById("inpAction").value = "EditView";
        submitCommandForm('DEFAULT', true, null, 'Dependents', '_self', null, true);
        return false;
    }
} */
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
function onClickNew() {
    if( document.getElementById('linkButtonNew').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled')
    {
       return false;
    }
    document.getElementById("inpAction").value = "EditView";
    document.getElementById("SubmitType").value = "New";
    //reloadTab(currentTab);

    if (changesFlag == 1) {
        OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
            if (result) {
                reloadTabSave(currentTab);
            }
            else {
                reloadTab(currentTab);
            }
        });
    }
    else {
        reloadTab('MEDIN');
    } 
}
function onClickRefresh() {
    document.getElementById("inpAction").value = "EditView";
    //reloadTab(currentTab);

    if (changesFlag == 1)
        OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
            if (result){
                if (savevaliddata()) {
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
        reloadTab('MEDIN');
}
function resetTab() {

   resetByTab(document.getElementById("TabMedIns"));
/*   
   var nextLink = document.getElementById("NextLink");
   nextLink.style.opacity = '0.5';
   nextLink.style.filter = 'alpha(opacity=50)';
   nextLink.style.cursor = '';
   nextLink.parentNode.replaceChild(nextLink.cloneNode(true), nextLink);
   $('#NextLink').attr('onclick', ""); */
  document.getElementById("inpEmployeeId").value = "";
  document.getElementById("inpIsActive").value = ""; 
}
function resetByTab(tab) {
    tab.className = "tabNotSelected";
    //image.className = "imageNotSelected";
    tab.parentNode.replaceChild(tab.cloneNode(true), tab);
    $('#' + tab.id).attr('onmouseover', "").attr('onmouseout', "").attr('onclick', "");
}
function reloadWindow(tab) {
     //hideMessage();
      if (changesFlag == 1) OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function (result) {
        if (result) reloadTabSave(tab);
        else {
          reloadTab(tab);
        }
      });
      else reloadTab(tab);
}
function enableTabs() {
    var nextLink = document.getElementById("NextLink");
    nextLink.style.opacity = '1';
    nextLink.style.filter = 'alpha(opacity=100)';
    nextLink.style.cursor = 'pointer';
    if (nextLink.attachEvent)
        nextLink.attachEvent('onclick', function() {
            reloadWindow('ID')
        });
    else
        nextLink.addEventListener('click', function() {
            reloadWindow('ID')
        }, false);

    var tab = null, image = null;

    tab = document.getElementById("TabEmployee");
    image = document.getElementById("ImgEmpInfo");
    tab.className = "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
            reloadWindow('ID')
        });
        tab.attachEvent('onmouseover', function() {
            setTabClass(document.getElementById("TabEmployee"), true);
        });
        tab.attachEvent('onmouseout', function() {
            setTabClass(document.getElementById("TabEmployee"), false);
        });
    }
    else
        tab.addEventListener('click', function() {
            reloadWindow('ID')
        }, false);
    
    tab = document.getElementById("TabMedIns");
    image = document.getElementById("ImgMedIns");
    tab.className = "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
            reloadWindow('MEDIN')
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
            reloadWindow('MEDIN')
        }, false);
    tab = document.getElementById("TabEmpInfo");
    image = document.getElementById("ImgMedIns");
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
    
}

function setTabClass(tab, type) {
    if (type == true)
        tab.className = "tabSelectedHOVER";
    else
        tab.className = "tabSelected";
}
function reloadTab(tab) {
    if (tab == 'EMP') {
       /*  submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employee.header/Employee', '_self', null, true);
        return false; */
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView';
        document.frmMain.submit();
    }
    else if (tab == 'EMPQUAL') {
        var url=""; 
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification?inpAction=GridView&inpEmployeeId=' + employeeId;
        document.frmMain.submit();
    }
    else if (tab == 'EMPINF') {
        var url="";
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView&inpEmployeeId='+employeeId;
        document.frmMain.submit();
    }
    else if (tab == 'Dependent') {
        //document.getElementById("inpAction").value = "GridView";
        document.getElementById("inpIsActive").value = "";
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents', '_self', null, true);
    }
    else if (tab=='EMPADD') {
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView';
       document.frmMain.submit();
    }
    else if (tab == 'PREEMP') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
         return false;
    }
    else if (tab == 'EMPCTRCT') {
        var url="";
          var employeeId=document.getElementById("inpEmployeeId").value;
          document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
          document.frmMain.submit();
     }
     else if(tab == 'Asset') {
         var employeeId=document.getElementById("inpEmployeeId").value;
         document.frmMain.action =  contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset?inpAction=GridView&inpEmployeeId='+employeeId;
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
    return false;
}
 function reloadTabSave(tab) {
    if (savevaliddata()) {
       document.getElementById("SubmitType").value = "Save";
       //document.getElementById("inpAction").value = "GridView";

       var url = "&inpNextTab=" + tab;
       submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.MedicalInsurance.header/MedicalInsurance?' + url, '_self', null, true);
       return false;
    }
}  
function onClickGrid() {
    //hideMessage();
    document.getElementById("inpAction").value = "GridView";
    //reloadTab("DependentI")
    if (changesFlag == 1)
        OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
            if (result)
                reloadTabSave("MEDIN");
            else {
                reloadTab("MEDIN");
            }
        });
    else
        reloadTab('MEDIN');
}

function onClickSave(index, type) {
    if (index.className != 'Main_ToolBar_Button')
        return false;
  
    if (changesFlag == 1 &&  savevaliddata() ) {
         if (type == "Grid") {
            document.getElementById("inpAction").value = "GridView";
            document.getElementById("SubmitType").value = "SaveGrid";
           // tab="MEDIN";
        }
        if (type == "New") {
            document.getElementById("inpAction").value = "EditView";
            //document.getElementById("inpDependentId").value ="";
            document.getElementById("SubmitType").value = "SaveNew";
          //  tab="MEDIN";
        } 
        if (type == "Save") {
            document.getElementById("inpAction").value = "EditView";
            document.getElementById("SubmitType").value = "Save";
           // tab="MEDIN";
        }
        reloadTab('MEDIN');
    }
}
showDependent(document.getElementById('inpInsuCategory').value);
function showDependent(value) {
    if(( value !="" ) && (value==='D')){
        $("#inpDependent").show();
        $("#inpDependentLabel").show();
      //dependent
        setTimeout(function () {
            $("#inpDependent").select2(selectBoxAjaxPaging({
                url :   function() {  return           '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.MedicalInsurance.ajax/MedicalInsuranceAjax?action=getDependent&employeeId='+document.getElementById("inpEmployeeId").value
                },
               
                
                
              size : "small"
            }));
             $("#inpDependent").on("select2:unselecting", function (e) {
                document.getElementById("inpDependent").options.length = 0;
              }); 
        }, 100); 
        if(document.getElementById("select2DependentId").value!=null && document.getElementById("select2DependentId").value!=""){
            var data = [{
                            id: document.getElementById("select2DependentId").value,
                            text: document.getElementById("select2DependentName").value
                          }];
                          $("#inpDependent").select2({
                            data: data
                          });
                         
                          } 

     }
    else{
         $('#inpDependent').next(".select2-container").hide();
         $("#inpDependentLabel").hide(); 
         document.getElementById('inpDependent').value= "0";
        /*  $("#inpDependent").select2("val", ""); */
 
           /* $("#inpDependent").hide();
           $("#inpDependentLabel").hide(); 
           document.getElementById('inpDependent').value= "0"; */
           
    } 
        
}

function savevaliddata(){
    var startdate=document.getElementById("inpStartDate").value;
    var enddate = document.getElementById("inpEndDate").value;
    var dependent = document.getElementById("inpDependent").value;
    var category = document.getElementById("inpInsuCategory").value;
    var membershipNo = document.getElementById("inpMemshipNo").value;
    var employeeId=document.getElementById("inpEmployeeId").value;
    
  
    // Date Validation
    var validdate=datevalidation(startdate,enddate);
    
    //Dependent is Mandatory for Category is Dependent
    if(category == 'D' && (dependent == "" || dependent == 0)){
        OBAlert('<%= Resource.getProperty("hcm.fill.dependent", lang) %>');
        dependent='1';
    }
    // Dependent and MembershipNo should be Unique
   var duplicate=AlreadyExistInsurance(dependent,membershipNo,employeeId);
    
 
    
   if(validdate == '1' || dependent == '1' || duplicate == 1){
       return false;
   }
   else{
       return true;
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
    
 
    if (fromdate.getTime() > todate.getTime())
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

function AlreadyExistInsurance(dependent,membershipno,employeeId){
    var validationchk=0;
     if(dependent!='' && membershipno!=''){
         $.ajax({
             type:'GET',
             url:"<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.MedicalInsurance.ajax/MedicalInsuranceAjax?action=CheckInsuranceAlreadyExist",            
             data:{inpdependents:dependent,inpMembershipNo:membershipno,inpempId:employeeId,inpmedicalinsuId:document.getElementById("inpMedInsId").value},
             dataType:'xml',
             async:false,
             success:function(data)
             {       
                 $(data).find("CheckInsuranceAlreadyExist").each(function()
                 {    
                     var result=$(this).find("value").text();
                   
                     if(result =="true"){
                         validationchk=1;
                         OBAlert('<%= Resource.getProperty("hcm.record.already.exist", lang) %>');                      
                     }
                     else{
                         validationchk=0;
                     }
                 });             
             }
         });
         
    }
      return validationchk;
}

//Insurance schema
setTimeout(function () {
    $("#inpInsuSchema").select2(selectBoxAjaxPaging({
        url :   function() {  return           '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.MedicalInsurance.ajax/MedicalInsuranceAjax?action=getInsuranceSchema'
        },
       
        
        
      size : "small"
    }));
     $("#inpInsuSchema").on("select2:unselecting", function (e) {
        document.getElementById("inpInsuSchema").options.length = 0;
      }); 
}, 100); 
if(document.getElementById("select2InsuSchemaId").value!=null && document.getElementById("select2InsuSchemaId").value!=""){
    var data = [{
                    id: document.getElementById("select2InsuSchemaId").value,
                    text: document.getElementById("select2InsuSchemaName").value
                  }];
                  $("#inpInsuSchema").select2({
                    data: data
                  });
                  
                  } 
$( document ).ready(function() {
    if($('#inpEmployeeIsActive').val()=="false"){
    $("#inpInsuSchema").prop("disabled", true);
    $("#inpInsuCategory").prop("disabled", true);
    $("#inpDependent").prop("disabled", true);
    document.getElementById("inpInsComName").setAttribute("disabled", "true");
    document.getElementById("inpMemshipNo").setAttribute("disabled", "true");
    $('#inpStartDate').prop('readonly',true);
    $('#inpEndDate').prop('readonly',true);
    }
});             
</script> 
</HTML> 
