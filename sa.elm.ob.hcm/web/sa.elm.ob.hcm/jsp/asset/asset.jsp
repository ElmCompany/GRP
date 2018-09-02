<!--
 *************************************************************************
 * All Rights Reserved.
 
 *************************************************************************
-->
<%@ page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>
<%@ page import="sa.elm.ob.utility.util.Utility" %>

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
   <link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>"></link>
    <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
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
        <%if(request.getAttribute("inpEmployeeIsActive").toString().equals("true")) {%>
         $('#inpStartDate').calendarsPicker({calendar:  
             $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect : function(dates){enableForm();},showTrigger:  
         '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
    
         $('#inpEndDate').calendarsPicker({calendar:  
             $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect : function(dates){enableForm();},showTrigger:  
         '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
          $('#inpLetterDate').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect : function(dates){enableForm();} ,showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'}); 
          <% } %>
    }
    function onResizeDo(){resizeArea();}
    function onLoad()
    {
        <%if(request.getAttribute("inpdescription")!=null){%>
        document.getElementById("inpdescription").innerHTML='<%=request.getAttribute("inpdescription")%>';
        <%}%>
        <%if(request.getAttribute("inpestablishname")!=null){%>
        document.getElementById("inpestablishname").innerHTML="<%=request.getAttribute("inpestablishname")%>";
        <%}%>
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
        <INPUT type="hidden" name="inpAction" id="inpAction" value=""></INPUT>
        <INPUT type="hidden" name="SubmitType" id="SubmitType" value=""></INPUT>
         <INPUT type="hidden" name="inpAssetId" id="inpAssetId" value="<%= request.getAttribute("inpAssetId") %>"></INPUT>
         <INPUT type="hidden" name="inpQualificationId" id="inpQualificationId" value="<%= request.getAttribute("inpQualificationId") %>"></INPUT>
        <INPUT type="hidden" name="inpEmployeeId" id="inpEmployeeId" value="<%= request.getAttribute("inpEmployeeId") %>"></INPUT>
        <INPUT type="hidden" name="inpempCategory" id="inpempCategory" value="<%= request.getAttribute("inpempCategory") %>"></INPUT>
        <INPUT type="hidden" name="inpEmploymentId" id="inpEmploymentId" value="<%= request.getAttribute("inpEmploymentId") %>"></INPUT>
        <INPUT type="hidden" name="inpAddressId" id="inpAddressId" value="<%= request.getAttribute("inpAddressId") %>"></INPUT>
        <INPUT type="hidden" name="inpEmpStatus" id="inpEmpStatus" value="<%= request.getAttribute("inpEmpStatus") %>"></INPUT>
        <INPUT type="hidden" id="inpEmployeeStatus" name="inpEmployeeStatus" value="<%= request.getAttribute("inpEmployeeStatus") %>"></INPUT>
         <INPUT type="hidden" id="inpCancelHiring" name="inpCancelHiring" value="<%= request.getAttribute("CancelHiring") %>"></INPUT>
        <INPUT type="hidden" name="inpEmployeeIsActive" id="inpEmployeeIsActive" value="<%= request.getAttribute("inpEmployeeIsActive") %>"></INPUT>
       
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
                            </tr>
                        </table>
                    </DIV>
                    <TABLE width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_TabPane" id="tdtopTabs">
                          <TR id="paramParentTabContainer"><td class="tabBackGroundInit"></td></TR>
                          <TR id="paramMainTabContainer"><td class="tabBackGroundInit"><span style="background: none;" class="tabTitle"></span></td></TR>
                          <tr><td class="tabBackGround">
                            <div class="marginLeft">
                            <div><span class="dojoTabcurrentfirst"><div>
                                <span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%= Resource.getProperty("hcm.Facilities&Assets",lang)%></a></span>
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
                                                                    <TABLE class="MessageBox_Top">
                                                                    <TBODY>
                                                                        <TR><TD>
                                                                                <TABLE style="cellpadding:0; cellspacing:0;" class="MessageBox_Body_ContentCell">
                                                                                    <TBODY>
                                                                                        <TR>
                                                                                            <TD class="MessageBox_Icon_ContentCell">
                                                                                                <DIV class="MessageBox_Icon"></DIV>
                                                                                            </TD>
                                                                                            <TD style="vertical-align: top;" id="messageBoxIDContent">
                                                                                                    <DIV class="MessageBox_TextTitle" id="messageBoxIDTitle"></DIV>
                                                                                                    <DIV class="MessageBox_TextDescription" id="messageBoxIDMessage"></DIV>
                                                                                                    <DIV class="MessageBox_TextSeparator"></DIV>
                                                                                            </TD>
                                                                                        </TR>
                                                                                    </TBODY>
                                                                                </TABLE>
                                                                            
                                                                            <div id="hideMessage">
                                                                                 <a style="color: yellow;font-size: 11.5px" href="javascript:void(0);" onClick="document.getElementById('messageBoxID').style.display = 'none';"><%=Resource.getProperty("hcm.hide",lang)%></a>
                                                                            </div>
                                                                     </TD></TR>
                                                                     </TBODY>
                                                                     </TABLE>
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
                                            <div id="TabEmployee" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMP');">
                                            <span class="LabelText"><fmt:message key="hcm.employee"></fmt:message></span>
                                            </div>
                                        </td>
                                        <td>
                                            <img id="ImgEmpInfo" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img>
                                        </td>
                                        <td>
                                            <div id="TabEmpInfo" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPINF');">
                                            <span class="LabelText"><%= Resource.getProperty("hcm.empmntinfo",lang)%></span>
                                            </div>
                                        </td>
                                        <td>
                                            <img id="ImgEmpAddress" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img>
                                        </td>
                                        <td>
                                            <div id="TabEmpAddress" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPADD');">
                                            <span class="LabelText"><%= Resource.getProperty("hcm.empaddress",lang)%></span>
                                            </div>
                                        </td>
                                        <td>
                                            <img  id="ImgqualInfo" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img>
                                        </td>
                                        <td>
                                            <div id="TabqualInfo" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPQUAL');">
                                            <span class="LabelText"><%= Resource.getProperty("hcm.empqualinfo",lang)%></span>
                                            </div>
                                            
                                        </td>
                                        <td>
                                            <img  id="ImgDependent" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png">
                                            </img>
                                        </td>
                                        <td>
                                            <div id="TabDependent" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('Dependent');">
                                            <span class="LabelText"><%= Resource.getProperty("hcm.dependents",lang)%></span>
                                            </div>
                                        </td>
                                        <td><img  id="ImgPreEmp" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                        <td><div id="Tabpreemp" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('PREEMP');" >
                                        <span class="LabelText"><%= Resource.getProperty("hcm.emppreemp",lang)%></span>
                                        </div></td>  
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
                                        <td><div id="TabDocument" class=tabSelected onclick="reloadWindow('DOC');"><span class="LabelText"><%= Resource.getProperty("hcm.documents",lang)%></span></div></td>     
                          <td><img  id="ImgEmpPerPayMethod" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                   <td>
                                     <div id="TabEmpPerPayMethod" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('PERPAYMETHOD');"><span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></span>
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
                                                           <input type="text" name="inpName1" id="inpName1" maxlength="59" value="<%= Utility.escapeQuote(request.getAttribute("inpName1")) %>" required="true" readonly class="dojoValidateValid TextBox_TwoCells_width" style="color: black; width:300px" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);" dir="rtl"></input>
                                                           <input type="hidden" name="inpHidName1" id="inpHidName1" value="<%= Utility.escapeQuote(request.getAttribute("inpName1")) %>"></input>
                                                       </TD>
                                                       <TD class="" align="left" style="min-width:70px;padding-left: 10px"><span class="LabelText" ><%= Resource.getProperty("hcm.arabicname", lang)%></span></TD>
                                                       <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.fullname", lang)%></span></TD>
                                                       <TD class="TextBox_ContentCell">
                                                           <input type="text" name="inpName2" id="inpName2" maxlength="59" value="<%= Utility.escapeQuote(request.getAttribute("inpName2")) %>" required="true" readonly class="dojoValidateValid TextBox_TwoCells_width" style="color: black; width:300px" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);"></input>
                                                           <input type="hidden" name="inpHidName2" id="inpHidName2" value="<%= Utility.escapeQuote(request.getAttribute("inpName2")) %>"></input>
                                                       </TD>
                                                       <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.code", lang)%></span></TD>
                                                       <TD class="TextBox_ContentCell">
                                                           <input type="text" name="inpEmpNo" id="inpEmpNo" maxlength="59" value="<%= Utility.escapeQuote(request.getAttribute("inpEmpNo")) %>" required="true" readonly class="dojoValidateValid TextBox_TwoCells_width" style="color: black; width:300px" onkeydown="return onKeydownHeadeValues(this, event);" onblur="resetHeaderValues(this);"></input>
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
                                        
                                        <div style="width:100%;">
                                            <div style="padding: 0 1%; width: 98%;">
                                                <div style="width:100%;" id="FormDetails">
                                                    <div align="center">
                                                        <TABLE style="width:100%; margin-top: 10px;">
                                                        <!-- /*<<<<<Your Code Here>>>>>*/ -->
                                                          <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><fmt:message key="hcm.assetname"></fmt:message></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <%if(request.getAttribute("inpAsset")!= null ) {%>
                                                                    <input type="text" name="inpAsset" id="inpAsset" maxlength="30" value="<%= Utility.escapeQuote(request.getAttribute("inpAsset")) %>"   class="dojoValidateValid TextBox_TwoCells_width required"  onkeydown="return true;" onkeyup="enableForm();" />
                                                                <% }else{%>
                                                                     <input type="text" name="inpAsset" id="inpAsset" maxlength="30" value=""   class="dojoValidateValid TextBox_TwoCells_width required"  onkeydown="return true;" onkeyup="enableForm();" />
                                                                <%} %>
                                                            </TD>
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
                                                                          <input type="text" id="inpStartDate" class="dojoValidateValid TextBox_btn_OneCell_width required" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpStartDate") %>" maxlength="10" name="inpStartDate" ></input>
                                                                          <%} %> 
                                                                      </td>
                                                                 </tr>
                                                                </table>
                                                               </TD> 
                                                            <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.enddate",lang)%></span></TD>
                                                           <TD class="TextBox_btn_ContentCell" colspan=3>
                                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                              <tr>
                                                                    <td class="TextBox_ContentCell">
                                                                    <%  
                                                                    if(request.getAttribute("inpEndDate")!= null ) {%>
                                                                         <input type="text" id="inpEndDate" class="dojoValidateValid TextBox_btn_OneCell_width " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"  value="<%= request.getAttribute("inpEndDate") %>" maxlength="10" name="inpEndDate" ></input> 
                                                                     <%}
                                                                      else{ %>
                                                                      <input type="text" id="inpEndDate" class="dojoValidateValid TextBox_btn_OneCell_width " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="" maxlength="10" name="inpEndDate" ></input>
                                                                      <%} %> 
                                                                 </td>
                                                             </tr>
                                                            </table>
                                                           </TD>   
                                                           </TR>
                                                           <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><fmt:message key="hcm.letterNo"></fmt:message></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <%if(request.getAttribute("inpLetterNo")!= null ) {%>
                                                                    <input type="text" name="inpLetterNo" id="inpLetterNo" maxlength="30" value="<%= request.getAttribute("inpLetterNo") %>"   class="dojoValidateValid TextBox_TwoCells_width " onkeydown="return onChangeEvent(event);enableForm();" />
                                                                <% }else{%>
                                                                     <input type="text" name="inpLetterNo" id="inpLetterNo" maxlength="30" value=""   class="dojoValidateValid TextBox_TwoCells_width " onkeydown="return onChangeEvent(event);enableForm();" />
                                                                <%} %>
                                                            </TD>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><fmt:message key="hcm.letterDate"></fmt:message></span></TD>
                                                               <TD class="TextBox_btn_ContentCell">
                                                                <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                                  <tr>
                                                                      <td class="TextBox_ContentCell">
                                                                       <%  
                                                                        if(request.getAttribute("inpLetterDate")!= null ) {%>
                                                                             <input type="text" id="inpLetterDate" class="dojoValidateValid TextBox_btn_OneCell_width " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpLetterDate") %>" maxlength="10" name="inpLetterDate" ></input> 
                                                                         <%}
                                                                          else{ %>
                                                                         <input type="text" id="inpLetterDate" class="dojoValidateValid TextBox_btn_OneCell_width " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="" maxlength="10" name="inpLetterDate" ></input>
                                                                          <%} %> 
                                                                      </td>
                                                                 </tr>
                                                                </table>
                                                            </TD>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><fmt:message key="hcm.decisionno"></fmt:message></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <%if(request.getAttribute("inpDecisionNo")!= null ) {%>
                                                                    <input type="text" name="inpDecisionNo" id="inpDecisionNo" maxlength="30" value="<%= request.getAttribute("inpDecisionNo") %>"   class="dojoValidateValid TextBox_TwoCells_width " onkeydown="return onChangeEvent(event);enableForm();" />
                                                                <% }else{%>
                                                                     <input type="text" name="inpDecisionNo" id="inpDecisionNo" maxlength="30" value=""   class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return onChangeEvent(event);enableForm();" />
                                                                <%} %>
                                                            </TD>
                                                           </TR>
                                                            <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><fmt:message key="hcm.balance"></fmt:message></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <%if(request.getAttribute("inpBalance")!= null ) {%>
                                                                    <input type="text" name="inpBalance" id="inpBalance" maxlength="20" value="<%= request.getAttribute("inpBalance") %>" class="dojoValidateValid TextBox_TwoCells_width " onkeydown="return isFloatOnKeyDown(this, event);" onchange="enableForm();" />
                                                                <% }else{%>
                                                                     <input type="text" name="inpBalance" id="inpBalance" maxlength="20" value=""   class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return isFloatOnKeyDown(this, event);" onchange="enableForm();" />
                                                                <%} %>
                                                            </TD>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><fmt:message key="hcm.description"></fmt:message></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                                <textarea rows="4" width=50 cols="30" maxlength="2000" class="TextBox_btn_TwoCells_width" id="inpdescription" name="inpdescription" onkeydown="return onChangeEvent(event);changeToEditingMode('onkeydown');" onfocus="setCursorAtTheEnd(this,event);" oncontextmenu="changeToEditingMode('oncontextmenu');" onkeypress="changeToEditingMode('onkeypress');" onchange="enableForm();" >
                                                               </textarea>
                                                               
                                                            </TD>
                                                            
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
//Main Functions
var currentTab = 'Asset';
var currentWindow='EMP';
var changesFlag=0;
function onClickRefresh() {
    hideMessage();
    document.getElementById("inpAction").value = "EditView";
    if (changesFlag == 1)
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
        reloadTab('Asset');
}
function onClickNew() {
    if( document.getElementById('linkButtonNew').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled')
    {
       return false;
    }
    hideMessage();
    document.getElementById("inpAction").value = "EditView";
   
    if (changesFlag == 1) {
        OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
            
            if (result) {
                 if(checkdata()){

             document.getElementById("SubmitType").value="SaveNew";
                    reloadTab(currentTab);
                     }
            }
            else {
                document.getElementById("inpAssetId").value = "";
                document.getElementById("inpAction").value = "EditView";
                reloadTab(currentTab);
            }
        });
    }
    else {
        document.getElementById("inpAssetId").value = "";
        reloadTab('Asset');
    }
}

function enableSaveButton(flag) {
    if (flag == 'true' && document.getElementById('linkButtonSave_Relation').className != 'Main_ToolBar_Button') {
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
    hideMessage();
    if($("#inpAsset").val() !='' && $("#inpStartDate").val() != '' ){
         enableSaveButton("true");
    }
    else{
        enableSaveButton("false");
    }
}

function hideMessage() {
    $("#messageBoxID").hide();
}
/* function reloadTabSave(tab) {
  
    if (checkValidData()) {
        document.getElementById("SubmitType").value = "Save";
        var url = "&inpNextTab=" + tab;
        submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification', '_self', null, true);
        return false;
        return false;
    }
} */
function reloadTab(tab) {
    var url="";
  
    if (tab == 'EMP') {
         document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView' + url;
        document.frmMain.submit();
    }
    else if (tab == 'EMPINF') {
         var employeeId=document.getElementById("inpEmployeeId").value;
         document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView&inpEmployeeId='+employeeId;
         document.frmMain.submit();
    }
    else if(tab == 'EMPADD'){ 
        var employeeId=document.getElementById("inpEmployeeId").value;
         document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpEmployeeId='+employeeId;
        document.frmMain.submit(); 
    }
    else if (tab == 'EMPQUAL') {
       <%--  var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification?inpEmployeeId='+employeeId;
        document.frmMain.submit();  --%>
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification?inpAction=GridView&inpEmployeeId='+employeeId;
        document.frmMain.submit();
         <%-- submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification' + url, '_self', null, true);
         return false;  --%>
    }
    else if(tab == 'Dependent'){ 
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpAction=GridView&inpEmployeeId='+employeeId;
        document.frmMain.submit();
   }
    else if (tab == 'PREEMP') {
        submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
         return false;
    }
    else if(tab == 'Asset'){ 
        submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.asset.header/Asset' + url, '_self', null, true);
        return false;
   }
    else if (tab == 'EMPCTRCT') {
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
        document.frmMain.submit();
    }
    else if (tab == 'DOC') {
      submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.documents.header/Documents', '_self', null, true);
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
 function checkdata()
{
        var startdate=document.getElementById("inpStartDate").value;
         var enddate = document.getElementById("inpEndDate").value;
         var name = document.getElementById("inpAsset").value;

         var mandatoryflag = '0',validate = '0';
         if(enddate!=null && enddate !=""){
               validate=checkdatevalidation(startdate,enddate);
         }
         if(startdate == '' || name == '' ){
             OBAlert('<%= Resource.getProperty("hcm.qualifications.mandatory", lang) %>');
             mandatoryflag = '1';
         }
         if(validate == '1' || mandatoryflag == '1'){
             return false;
         }else{
               return true;                  
         }
  }

function onClickSave(index, type) {

    
    if (index.className != 'Main_ToolBar_Button')
        return false;
    if (changesFlag == 1 && checkdata() ) {
        if (type == "Grid") {
            
            showProcessBar(true, 2);
            document.getElementById("inpAction").value = "GridView";
            document.getElementById("SubmitType").value = "SaveGrid";
        }
        if (type == "New") {
        
            showProcessBar(true, 2);
            document.getElementById("inpAction").value = "EditView";
            document.getElementById("SubmitType").value = "SaveNew";
        }
        if (type == "Save") {
            
           // showProcessBar(true, 2);
            document.getElementById("inpAction").value = "EditView";
            document.getElementById("SubmitType").value = "Save";
            
            
        }
        reloadTab('Asset');
    }
}
function reloadWindow(tab) {
    hideMessage();
    if (changesFlag == 1)
        OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
            if (result)
                reloadTabSave(tab);
            else {
                reloadTab(tab);
            }
        });
    else
        reloadTab(tab);
}
function reloadTabSave(tab) {
        document.getElementById("SubmitType").value = "Save";
        var url = "&inpNextTab=" + tab;
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.asset.header/Asset?inpAction=EditView'+url;
        document.frmMain.submit();
}

function onClickGridView() {
    hideMessage();
    document.getElementById("inpAction").value = "GridView";
    if (changesFlag == 1)
        OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
            if (result){
                  if(checkdata()){
                document.getElementById("SubmitType").value="SaveGrid";
                document.getElementById("inpAction").value = "GridView";
                reloadTab(currentTab);
                  }
            }
            else {
                document.getElementById("inpAction").value = "GridView";
                reloadTab(currentTab);
            }
        });
    else
        reloadTab('Asset');
}

var textBox = document.getElementById("inpdescription");

textBox.onfocus = function() {
    $("#inpdescription").val($.trim($("#inpdescription").val()));
};
$( document ).ready(function() {
    if($('#inpEmployeeIsActive').val()=="false"){
    document.getElementById("inpAsset").setAttribute("disabled", "true");
    document.getElementById("inpLetterNo").setAttribute("disabled", "true");
    document.getElementById("inpDecisionNo").setAttribute("disabled", "true");
    document.getElementById("inpBalance").setAttribute("disabled", "true");
    document.getElementById("inpdescription").setAttribute("disabled", "true");
    $('#inpStartDate').prop('readonly',true);
    $('#inpEndDate').prop('readonly',true);
    $('#inpLetterDate').prop('readonly',true);
    }
});
</script>
</HTML>
