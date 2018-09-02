<%@page import="sa.elm.ob.finance.EfinBankBranch"%>
<%@page import="sa.elm.ob.utility.util.Utility"%>
<%@ page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>
<%@page import="sa.elm.ob.hcm.ad_forms.personalpaymentmethod.vo.PersonalPaymentMethodVO"%>
<%@page import="java.util.List,java.util.ArrayList"%>

<% String lang = ((String)session.getAttribute("#AD_LANGUAGE"));
     List<PersonalPaymentMethodVO> inpvalue = (ArrayList<PersonalPaymentMethodVO>) request.getAttribute("inpvalue");
     List<PersonalPaymentMethodVO> inpname = (ArrayList<PersonalPaymentMethodVO>) request.getAttribute("inpname");
     List<PersonalPaymentMethodVO> inpcurrency = (ArrayList<PersonalPaymentMethodVO>) request.getAttribute("inpcurrency");
     List<PersonalPaymentMethodVO> inpbranchList = (ArrayList<PersonalPaymentMethodVO>) request.getAttribute("inpbranch");
     String strbranchList =  "0:" + Resource.getProperty("hcm.select", lang) + ";";

     String strbanklist =  "0:" + Resource.getProperty("hcm.select", lang) + ";";
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
   <!-- <link rel="stylesheet" type="text/css" href="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css" id="paramCSS"></link> -->
    <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
    <style type="text/css"></style>
     <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/ui.jqgrid.css"></link>
    <link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
    <script type="text/javascript" src="../web/js/common/common.js"></script>
    <script type="text/javascript" src="../web/js/utils.js"></script>
    <script type="text/javascript" src="../web/js/windowKeyboard.js"></script>
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
        
         /* $('#inpStartDate').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect: function() {enableForm();}, showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger" >'});
         
         $('#inpEndDate').calendarsPicker({calendar:  
             $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect: function() {enableForm();}, showTrigger:  
         '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'}); */
    }
    function onResizeDo(){resizeArea();reSizeGrid();}
    


    function onLoad()
    {
         <%  request.setAttribute("savemsg",request.getAttribute("savemsg")==null?"":request.getAttribute("savemsg")); %>
         <%  if(request.getAttribute("savemsg").equals("Success")) { %>
         displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.saveoreditsuccess", lang) %>");
         <%}%>
        
        
        var header="<%= request.getAttribute("inpehcmPersonalPaymethdId") %>";  
        var payCode="<%= request.getAttribute("inppaycode") %>";
        var isbanktransfer ="<%= request.getAttribute("inpisbanktransfer") %>";
        if(header != "null" && header != "" && isbanktransfer!="false"){
            document.getElementById("bankdetailsGRP").style.display="";
            document.getElementById("jqgrid").style.display="";
        }
        else{
             document.getElementById("bankdetailsGRP").style.display="none";
        }
       
        //document.getElementById("bankdetailsGRP").style.display=""

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
        <INPUT type="hidden" name="strbank" id="strbank" value=""></INPUT>
                <INPUT type="hidden" name="strbranch" id="strbranch" value=""></INPUT>
        
        <INPUT type="hidden" name="inpEmployeeId" id="inpEmployeeId" value="<%= request.getAttribute("inpEmployeeId") %>"></INPUT>
             <INPUT type="hidden" name="inpEmpStatus" id="inpEmpStatus" value="<%= request.getAttribute("inpEmpStatus") %>"></INPUT>
      <INPUT type="hidden" id="inpEmployeeStatus" name="inpEmployeeStatus" value="<%= request.getAttribute("inpEmployeeStatus") %>"></INPUT>
                   <INPUT type="hidden" name="inpempCategory" id="inpempCategory" value="<%= request.getAttribute("inpempCategory") %>"></INPUT>
        <INPUT type="hidden" name="inpEmployeeIsActive" id="inpEmployeeIsActive" value="<%= request.getAttribute("inpEmployeeIsActive") %>"></INPUT>
        <INPUT type="hidden" name="inpehcmPersonalPaymethdId" id="inpehcmPersonalPaymethdId" value="<%= request.getAttribute("inpehcmPersonalPaymethdId") %>"></INPUT>
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
                            <td width="2%" ><a href="javascript:void(0);" onClick="onClickGrid()" class="Main_ToolBar_Button" onMouseOver="window.status='Form View';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="linkButton_Relation"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Relation" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.gridview",lang) %>" border="0" id="linkButtonEdition"></a></td>
                           <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
                           <!--    <td width="2%" ><a href="javascript:void(0);" onClick="onClickDelete();" class="Main_ToolBar_Button" onMouseOver="window.status='Delete Record';return true;" onMouseOut="window.status='';return true;" id="buttonDelete"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete" src="../web/images/blank.gif" title="<%= Resource.getProperty("construction.delete", lang)  %>" border="0" id="linkButtonDelete"></a></td> -->
                           
                             <td width="2%" ><a href="javascript:void(0);" onClick="onClickSave(this, 'Save');" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Save Record';return true;" onMouseOut="window.status='';return true;" id="linkButtonSave"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.save", lang)  %>" border="0" id="buttonSave"></a></td>  
                              <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                                <td width="2%" ><a href="javascript:void(0);" onClick="onClickSave(this, 'New');" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Save and New';return true;" onMouseOut="window.status='';return true;" id="linkButtonSave_New"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_New_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.savenew", lang)  %>" border="0" id="buttonSave_New"></a></td> 
                                 <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                                <td width="2%" ><a href="javascript:void(0);" onClick="onClickSave(this, 'Grid');" class="Main_ToolBar_Button_disabled" onMouseOver="window.status='Save and Grid';return true;" onMouseOut="window.status='';return true;" id="linkButtonSave_Relation"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_Relation_disabled" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.savegrid", lang)  %>" border="0" id="buttonSave_Relation"></a></td>          
                            <td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></img></td>
                            <td width="2%" ><a href="javascript:void(0);" onClick="onClickRefresh()" class="Main_ToolBar_Button" onMouseOver="window.status='Reload Current Page';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonRefresh"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Refresh" src="../web/images/blank.gif" title="<%= Resource.getProperty("construction.reload",lang) %>" border="0" id="linkButtonRefresh"></a></td> 
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
                                <span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></a></span>
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
                                                       <!-- <TABLE style="width:80%; margin-top: 10px;"> --> 
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
                                                          <td>
                                                              <img  id="ImgDependent" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img>
                                                          </td>
                                                          <td>
                                                           <div id="TabDependent" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('Dependent');">
                                                               <span class="LabelText"><%= Resource.getProperty("hcm.dependents",lang)%>
                                                               </span>
                                                           </div>
                                                          </td>
                                                          <td><img  id="Imgpreemp" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                                                          <td>
                                                            <div id="Tabpreemp" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';"  onclick="reloadWindow('PREEMP');">
                                                            <span class="LabelText"><%= Resource.getProperty("hcm.emppreemp",lang)%></span>
                                                            </div>
                                                          </td>
                                                          
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
                                                                <img  id="ImgAsset" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img>
                                                          </td>
                                                          <td>
                                                            <div id="TabAsset" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';"  onclick="reloadWindow('Asset');">
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
                                                                                <div id="TabEmpPerPayMethod" class="tabCurrent" >
                                                                                        <span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%>
                                                                                </span>
                                                                              </div>
                                                                                            <div style="text-align: center;">
                                                                                             <img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;">
                                                                                         </img>
                                                                                         </div>
                                                                                        </td>
                                                                         </tr>
                    </tbody>
                 </table>
                 <div style="margin-bottom: 5px;"></div>
             </div>
                        <div id="LoadingContent" style="display:none;">
                       <div style="text-align: center;margin-top: 17%;"><img alt="Loading" src="../web/images/loading.gif"></img></div>
                       <div style="color:#000000;font-family: sans-serif;font-size:14px;margin-top:10px;text-align: center;"><%= Resource.getProperty("hcm.loading", lang)%>... </div>
                   </div>
                                             <div style="width:100%;">
                                             <div style="padding: 0 1%; width: 98%;">
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
                                   <TABLE style="width:80%; margin-top: 10px;">
                                   
                                        <TR>
                                         <TD class="TitleCell" align="left" style="width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.EmpPayTypeCode", lang)%></span>
                                        </TD>
                                             <TD class="TextBox_ContentCell">
                                           <SELECT id="inppaycode" name="inppaycode" class="ComboKey Combo_TwoCells_width" onkeydown="return true;" onkeyup="enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';changepaymentcodename(this.value,null);">
                                            <%  if(inpvalue.size()>0) { %>
                                                <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option>
                                                <% for(PersonalPaymentMethodVO vo:inpvalue){
                                                  %>
                                                <option value='<%= vo.getpayrollpaytypemethodId()%>' <%if(request.getAttribute("inpsavedvalue") !=null && request.getAttribute("inpsavedvalue").equals(vo.getpayrollpaytypemethodId())){%> selected<%} %>><span><%= vo.getpaymenttypecode()%></span></option>
                                                <%}}%>
                                            </SELECT>
                                           
                                        </TD>
                                          <TD class="TitleCell" align="left" style="width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.EmpPayTypeName", lang)%></span>
                                        </TD>
                                                  <TD class="TextBox_ContentCell">
                                           <SELECT id="inppayname" name="inppayname" class="ComboKey Combo_TwoCells_width" onkeydown="return true;" onkeyup="enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none'; changepaymentcodename(null,this.value);">
                                            <%  if(inpname !=null && inpname.size()>0) { %>
                                                <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option>
                                                <% for(PersonalPaymentMethodVO vo:inpname){
                                                     %>
                                                <option value='<%= vo.getpayrollpaytypemethodId()%>' <%if(request.getAttribute("inpsavedname") !=null && request.getAttribute("inpsavedname").equals(vo.getpayrollpaytypemethodId())){%> selected<%} %>><span><%= vo.getpaymenttypename()%></span></option>
                                                <%}}%>
                                            </SELECT>
                                           
                                        </TD>
                                        </TR>
                                          <TR>
                                             <TD class="TitleCell" align="left" style="width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.Currency", lang)%></span>
                                        </TD>
                                                   <TD class="TextBox_ContentCell">
                                           <SELECT id="inppaycurrency" name="inppaycurrency" class="ComboKey Combo_TwoCells_width" onkeydown="return true;" onkeyup="enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();">
                                            <%  if(inpcurrency.size()>0) { %>
                                                <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option>
                                                <% for(PersonalPaymentMethodVO vo:inpcurrency){
                                                     %>
                                                <option value='<%= vo.getcurrencyId()%>' <%if(request.getAttribute("inpsavedcurrency") !=null && request.getAttribute("inpsavedcurrency").equals(vo.getcurrencyId())){%> selected<%} %>><span><%= vo.getcurrency()%></span></option>
                                                <%}}%>
                                            </SELECT>
                                                
                                        </TD> 
                                        </TR>
                               
                                          <TR>
                                          <TD class="TitleCell" align="right" ><span class="LabelText" ><%= Resource.getProperty("hcm.default",lang)%></span></TD>
                                                          <TD class="TextBox_ContentCell">
                                                             <input type="checkbox" id="inpdefaultflag" name="inpdefaultflag" onkeydown="return true;" onkeyup="enableForm();" onchange="document.getElementById('messageBoxID').style.display = 'none'; enableForm();" <%if( (request.getAttribute("inpdefaultflag") != null) && request.getAttribute("inpdefaultflag").equals(true)){%> checked <%} %>  ></input>
                                                         </TD> 
                                         
                                        </TR>  
                                               
                                                 <!-- Bank Details Group -->
                                       
                                    
                                        <TR>
                                         <td colspan="4" >
                                                                <DIV id="bankdetailsGRP" style="display:none;">
                                                                <table style="width: 100%;padding: 2px;">
                                                                <tr>
                                                                <td>
                                                                 <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                                  <TBODY>
                                                                   <TR class="FieldGroup_TopMargin"></TR>
                                                                   <TR>
                                                                       <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                       <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.bankdetails", lang) %></TD>
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
                                                       <!--         <div id ="space" style="height:80%;">
                                                               
                                                               </div> -->
                                                               
                                                                <div id="jqgrid" style="width:100%; display:none;">
                                                                   <div align="center">
                                                                    <table id="bankdetailslist" class="scroll" cellpadding="0" cellspacing="0" width="100%">
                                                                    </table><div id="pager" class="scroll" style="text-align: center;">
                                                                   </div>
                                                                   </div>
                                                                </div>
                                                            </td>
                                    </TR>
                                    </TABLE>
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
<% List<PersonalPaymentMethodVO> inpbank = (ArrayList<PersonalPaymentMethodVO>)  request.getAttribute("inpbank");
  //strbanklist = "{";
  for (PersonalPaymentMethodVO vo : inpbank){
    strbanklist += vo.getbankdetailId() + ":" +  vo.getbankname() + ";";
  }
  // strbanklist += "}";
    List<EfinBankBranch> inpbranch = (ArrayList<EfinBankBranch>)  request.getAttribute("inpbranch");
  for (EfinBankBranch vo : inpbranch){
    strbranchList += vo.getId() + ":" +  vo.getBranchCode() + " "+  vo.getBranchName() +";";
  }
  %>
  document.getElementById("strbank").value="<%=strbanklist.substring(0,strbanklist.lastIndexOf(";"))%>";
   document.getElementById("strbranch").value="<%=strbranchList.substring(0,strbranchList.lastIndexOf(";"))%>"; 
  
var changedvalue="<%= Resource.getProperty("hcm.changedvaluessave", lang) %>";
var currentTab="PERPAYMETHD";
var askSave='<%= Resource.getProperty("hcm.action.askSave", lang) %>';
var askDelete='<%= Resource.getProperty("hcm.action.askDelete", lang) %>';
var percentageValidation='<%= Resource.getProperty("hcm.percentagevalidation", lang) %>';
var accountnumbernotnull='<%= Resource.getProperty("hcm.accountnumbernotnull", lang) %>';
var DefaultValidation='<%= Resource.getProperty("hcm.DefaultValidation", lang) %>';
var InvalidIban='<%= Resource.getProperty("hcm.invalidIban", lang) %>';
var accountnumberunique = '<%= Resource.getProperty("hcm.accountnumberunique", lang) %>';
var startdatenotnull= '<%= Resource.getProperty("hcm.startdate.notnull", lang) %>';
var alreadyDefaultPPM='<%= Resource.getProperty("hcm.defaultpersonalpaymethod", lang) %>';
var changedvaluessave='<%= Resource.getProperty("hcm.changedvaluessave", lang) %>';
var alreadyExistPPM='<%= Resource.getProperty("hcm.personalpaymethodExist", lang) %>';
var EnddateShouldbenull='<%= Resource.getProperty("hcm.EnddateShouldbenull", lang) %>';
var yes= '<%= Resource.getProperty("hcm.yes", lang) %>';
var no= '<%= Resource.getProperty("hcm.no", lang) %>';
var select= '<%= Resource.getProperty("hcm.select", lang) %>';
var lang ='<%=lang%>',
direction='ltr';
    if(lang=="ar_SA"){
        direction='rtl';
     }
    var equalto='<%= Resource.getProperty("hcm.Equalto", lang) %>';
    var greaterthanequalto='<%= Resource.getProperty("hcm.GreaterthanorEqualto", lang) %>';
    var lessthanequalto= '<%= Resource.getProperty("hcm.LesserthanorEqualto", lang) %>';    
    document.getElementById("strbranch").value="<%=strbranchList.substring(0,strbranchList.lastIndexOf(";"))%>";
</script>
<script type="text/javascript" src="../web/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="../web/js/jquery/grid.locale-en.js"></script>
<script type="text/javascript" src="../web/js/jquery/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="../web/js/ui/jquery.ui.datepicker.js"></script>
<script type="text/javascript" src="../web/sa.elm.ob.hcm/js/PersonalPaymentMethod.js"></script>
<script type="text/javascript" src="../web/js/inlineNavGrid.js"></script>  
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.plugin.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.ummalqura-ar.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.plus.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.js"></script> 
<script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.ext.js"></script>
<script type="text/javascript">
var changesFlag =0,contextPath="<%=request.getContextPath()%>";
var bankdetailGrid = jQuery("#bankdetailslist");

 </script>
</HTML>