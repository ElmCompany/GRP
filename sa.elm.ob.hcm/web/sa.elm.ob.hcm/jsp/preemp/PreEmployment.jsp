 <%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.List, java.util.ArrayList"%>
<%@page import="sa.elm.ob.utility.util.Utility"%>
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
             $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect : function(dates){enableForm(); yearcalculation();},showTrigger:  
         '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
    
         $('#inpEndDate').calendarsPicker({calendar:  
             $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect : function(dates){enableForm(); yearcalculation(); },showTrigger:  
         '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
        $('#inpexpirydate').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect : function(dates){enableForm();} ,showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
        <% } %>
    }
    function onResizeDo(){resizeArea();yearcalculation();}
    function onLoad()
    {
        yearcalculation();
        <%if(request.getAttribute("inpposition")!=null){%>
        var strPosition = '<%=request.getAttribute("inpposition")%>';
        //strPosition = escapeHTML(strPosition).replace("\n", "'").replace("\r ", "'+")
      
     
        document.getElementById("inpposition").innerHTML=strPosition;
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
    function yearcalculation()
    {
         var calendarInstance = $.calendars.instance('ummalqura');
         var year,date,month=0;
         var maximumcurrentdate=0;
         
         var dateString=document.getElementById("inpStartDate").value;
         var now = document.getElementById("inpEndDate").value;
         if(dateString.length >0 && now.length > 0 ){
             /**
             Adding the following two lines to fix this issue 
             http://182.18.161.127/mantis/view.php?id=3950
             */
             now = calendarInstance.newDate(parseInt(now.substring(6,10)),parseInt(now.substring(3,5)), parseInt(now.substring(0,2)));
             now = now.add(1,'d');
             
             var dobyear=dateString.substring(6,10);
             var dobmonth= dateString.substring(3,5);
             var dobdate= dateString.substring(0,2) ;
             var nowyear = now.year();
             var nowmonth = now.month();
             var nowdate = now.day();
             
             year=nowyear-dobyear;
             month=nowmonth-dobmonth;
             
             if(month<0){
                 year-- ;
                 month = 12 - parseInt(dobmonth) + parseInt(nowmonth);
                 
                 if(nowdate <  dobdate)
                     month-- ; 
             }else if(month==0 && nowdate <  dobdate){
                 year-- ;
                 month=11;
             }
             if (nowdate > dobdate){
                 date = nowdate - dobdate;
                 $('#inpAgeMD').val(date);
             }else if (nowdate < dobdate){
                 var today = nowdate;
                 nowmonth =parseInt(nowmonth)-1;
                 if(nowmonth <10){
                     nowmonth="0"+nowmonth;
                     }  
                   
                  $.post('<%=request.getContextPath()%>/EmployeeAjax', {
                      action : 'getDays',
                      monthyear : '%'+nowyear+nowmonth+'%'
                  }, function(data) {
                      maximumcurrentdate = data.getElementsByTagName("noofdays")[0].childNodes[0].nodeValue;
                      date = parseInt(maximumcurrentdate) - parseInt(dobdate) + parseInt(today);
                       $('#inpAgeMD').val(date);
                  });
              } 
              else
              {
                  date = 0;
                  if (month == 12)
                  {
                     years++;
                     month = 0;
                  }
                  $('#inpAgeMD').val(date);

              }
               if( nowdate< dobdate){
                 month--;
                 $('#inpAgeMM').val(month); 
             }
             else{
                 $('#inpAgeMM').val(month); 

             }  
            // $('#inpAgeMM').val(month); 
            // $('#inpAgeMM').val(parseInt(month)); 
            $('#inpAgeMY').val(year); 
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
         <INPUT type="hidden" name="inpPreEmplymentId" id="inpPreEmplymentId" value="<%= request.getAttribute("inpPreEmplymentId") %>"></INPUT>
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
                                 <td width="2%" ><a href="javascript:void(0);" onClick="onClickRefresh()" class="Main_ToolBar_Button" onMouseOver="window.status='Reload Current Page';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonRefresh"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Refresh" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.reload",lang) %>" border="0" id="linkButtonRefresh"></a></td> 
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
                                <span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%= Resource.getProperty("hcm.emppreemp",lang)%></a></span>
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
                        <td><div id="TabEmployee" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMP');"><span class="LabelText"><%=Resource.getProperty("hcm.employee",lang)%></span></div></td>
                        <td><img id="ImgEmpInfo" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabEmpInfo" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPINF');"><span class="LabelText"><%= Resource.getProperty("hcm.empmntinfo",lang)%></span></div></td>
                        <td><img id="ImgEmpAddress" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabEmpAddress" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPADD');"><span class="LabelText"><%= Resource.getProperty("hcm.empaddress",lang)%></span></div></td>
                        <td><img  id="ImgqualInfo" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabqualInfo" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPQUAL');"><span class="LabelText"><%= Resource.getProperty("hcm.empqualinfo",lang)%></span></div></td>   
                        <td><img  id="ImgDependent" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabDependent" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('Dependent');"><span class="LabelText"><%= Resource.getProperty("hcm.dependents",lang)%></span></div>
                        <td><img  id="ImgPreEmp" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="Tabpreemp" class="tabCurrent"><span class="LabelText"><%= Resource.getProperty("hcm.emppreemp",lang)%></span></div>
                        <div style="text-align: center"><img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img></div></td>
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
                        <td><div id="TabDocument" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('DOC');"><span class="LabelText"><%= Resource.getProperty("hcm.documents",lang)%></span></div>  
                           <td><img  id="ImgEmpPerPayMethod" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                   <td>
                                     <div id="TabEmpPerPayMethod" class="tabSelected"  onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('PERPAYMETHOD');"><span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></span>
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
                                                        <!-- /*<<<<<Your Code Here>>>>>*/ -->
                                                          <TR>
                                                            
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.preemp.empname", lang) %></span></TD>
                                                         <TD class="TextBox_ContentCell">
                                            
                                                                <input type="text" name="inpempname" id="inpempname" maxlength="30" value="<%= request.getAttribute("inpempname")==null ? "": request.getAttribute("inpempname")%>"   class="dojoValidateValid TextBox_TwoCells_width required " onkeydown="return true;" onkeyup="enableForm();"/>
                                                                
                                                            </TD>
                                                            
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.startdate",lang)%></span></TD>
                                                               <TD class="TextBox_btn_ContentCell">
                                                                <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                                  <tr>
                                                                      <td class="TextBox_ContentCell">
                                                                             <input type="text" id="inpStartDate" class="dojoValidateValid TextBox_btn_OneCell_width required" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpStartDate")==null? request.getAttribute("today"): request.getAttribute("inpStartDate") %>" maxlength="10" name="inpStartDate" ></input> 
                                                                      </td>
                                                                 </tr>
                                                                </table>
                                                               </TD>
                                      
                                      
                                                            <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.enddate",lang)%></span></TD>
                                                           <TD class="TextBox_btn_ContentCell" colspan=3>
                                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                              <tr>
                                                                    <td class="TextBox_ContentCell">
                                                                         <input type="text" id="inpEndDate" class="dojoValidateValid TextBox_btn_OneCell_width required " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();"  value="<%= request.getAttribute("inpEndDate")==null? request.getAttribute("today"): request.getAttribute("inpEndDate")  %>" maxlength="10" name="inpEndDate" ></input> 
                                                                 </td>
                                                             </tr>
                                                            </table>
                                                           </TD>
                                                           </TR>
                                                          <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" >Years/Months/Days</span></TD>
                                                       <TD class="TextBox_btn_ContentCell">
                                                                          <span style="padding-left:2%;padding-right:2%">Y</span>
                                                                            <input type="text"  name="inpAgeMY" id="inpAgeMY" maxlength="2" value="" readonly required="false" 
                                                                               class="dojoValidateValid" onkeydown="return onChangeEvent(event);"  style="width: 30px;padding-left:2%;padding-top:2%;"></input>
                                                                           <span align="center"  style="padding-left:2%;padding-right:2%;">   M    </span>
                                                                           
                                                                             <input type="text"  name="inpAgeMM" id="inpAgeMM" maxlength="2" value="" readonly required="false" 
                                                                              class="dojoValidateValid" onkeydown="return onChangeEvent(event);"  style="width: 30px;padding-left:2%;padding-top:2%;"></input>
                                                                           <span align="center"  style="padding-left:2%;padding-right:2%">   D   </span>
                                                                            <input type="text"  name="inpAgeMD" id="inpAgeMD" maxlength="2" value="" readonly required="false" 
                                                                              class="dojoValidateValid" onkeydown="return onChangeEvent(event);"  style="width: 30px;padding-left:2%;padding-top:2%;"></input>
                                                                       </TD> 
                                                        
                                                              <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.preemp.grade", lang) %></span></TD>
                                                                        <TD class="TextBox_ContentCell">
                                                        
                                                        <input type="text" name="inpgrade" id="inpgrade" maxlength="30" value="<%= request.getAttribute("inpgrade")==null?"": request.getAttribute("inpgrade")%>"   class="dojoValidateValid TextBox_TwoCells_width " onkeydown="return onChangeEvent(event);enableForm();" />
                                                            
                                                              </TD>
                                                            
                                                            
                                                                                                                      
                                                            
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.preemp.empcat", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                            
                                                                <input type="text" name="inpempcat" id="inpempcat" maxlength="30" value="<%= request.getAttribute("inpempcat")==null?"": request.getAttribute("inpempcat") %>"   class="dojoValidateValid TextBox_TwoCells_width " onkeydown="return onChangeEvent(event);enableForm();" />
                                                               
                                                            </TD>
                                                            
                                                     
                                                            
                                                            </TR>
                                                            <TR>
                                                             <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.preemp.dep", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                            
                                                                <input type="text" name="inpdep" id="inpdep" maxlength="30" value="<%= request.getAttribute("inpdep")==null?"": request.getAttribute("inpdep") %>"   class="dojoValidateValid TextBox_TwoCells_width  " onkeydown="return onChangeEvent(event);enableForm();" />
                                                                
                                                            </TD>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.preemp.detail", lang) %></span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                        
                                                        <input type="text" name="inpotherdet" id="inpotherdet" maxlength="30" value="<%= request.getAttribute("inpotherdet")==null?"": request.getAttribute("inpotherdet") %>"   class="dojoValidateValid TextBox_TwoCells_width " onkeydown="return onChangeEvent(event);enableForm();" />
                                                               
                                                            </TD>
                                                            
                                                            
                                                            <%-- <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.expirydate",lang)%></span></TD>
                                                           <TD class="TextBox_btn_ContentCell">
                                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                              <tr>
                                                                    <td class="TextBox_ContentCell">
                                                                    <%  
                                                                    if(request.getAttribute("inpexpirydate")!= null ) {%>
                                                                         <input type="text" id="inpexpirydate" class="dojoValidateValid TextBox_btn_OneCell_width  " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="<%= request.getAttribute("inpexpirydate") %>" maxlength="10" name="inpexpirydate" ></input> 
                                                                     <%}
                                                                      else{ %>
                                                                      <input type="text" id="inpexpirydate" class="dojoValidateValid TextBox_btn_OneCell_width  " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(); "  value="" maxlength="10" name="inpexpirydate" ></input>
                                                                      <%} %> 
                                                                 </td>
                                                             </tr>
                                                            </table>
                                                           </TD> 
                                                                          --%>       
                                                             
                                       </TR>
                                       <TR>
                                                            
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" >Position</span></TD>
                                                            <TD class="TextBox_ContentCell">
                                                          
                                                                <textarea rows="4" width=50 cols="50" id="inpposition"  maxlength="2000" name="inpposition" class="TextBox_btn_TwoCells_width" onkeydown="return onChangeEvent(event);" onfocus="setCursorAtTheEnd(this,event);" oncontextmenu="changeToEditingMode('oncontextmenu');"  onkeypress="changeToEditingMode('onkeypress');"      onkeydown="changeToEditingMode('onkeydown');"  onchange="enableForm();" >
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
var currentTab = 'PREEMP';
var changesFlag=0;
function onClickRefresh() {
    hideMessage();
    document.getElementById("inpAction").value = "EditView";
    if (changesFlag == 1)
        OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
            if (result){
                if(checkdata()){
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
        reloadTab('PREEMP');
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
                
                reloadTab(currentTab);
            }
        });
    }
    else {
        document.getElementById("inpPreEmplymentId").value = "";
        reloadTab('PREEMP');
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
   // alert(document.getElementById("inpempname").value);
    if($("#inpempname").val() !='' && $("#inpStartDate").val() != '' && $("#inpEndDate").val() !='' ){
         enableSaveButton("true");
    }
    else{
        enableSaveButton("false");
    }
}

function hideMessage() {
    $("#messageBoxID").hide();
}
function reloadWindow(tab) {
    hideMessage();
    if (changesFlag == 1 && checkdata())
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
    if (checkdata()) {
        document.getElementById("SubmitType").value = "Save";
        var url = "&inpNextTab=" + tab;
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment?inpAction=EditView'+url;
        document.frmMain.submit();
    }
}
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
         submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification' + url, '_self', null, true);
         return false; 
    }
   
    else if(tab == 'Dependent'){ 
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpAction=GridView&inpEmployeeId='+employeeId;
        document.frmMain.submit();
   }
    else if (tab == 'EMPCTRCT') {
        var url="";
          var employeeId=document.getElementById("inpEmployeeId").value;
          document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpEmployeeId='+employeeId;
          document.frmMain.submit();
     }
   else if(tab == 'PREEMP'){ 
       
       submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment' , '_self', null, true);
       return false; 
  }
   else if(tab == 'Asset'){ 
       submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.asset.header/Asset' + url, '_self', null, true);
       return false;
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
function checkdata() {
var startdateflag='0',enddateflag='0',overlapflag='0',mandatoryflag='0',hiredateflag='0';
    var startdate = document.getElementById("inpStartDate").value;
    var enddate = document.getElementById("inpEndDate").value;
    var today = "<%=request.getAttribute("today")%>";
    var hiredate = "<%=request.getAttribute("inpHireDate")%>";
    
    if (dateCompare(startdate,enddate) == -1 || dateCompare(startdate,enddate) == 0) {
        OBAlert('<%= Resource.getProperty("hcm.enddate<startdate",lang)%>' );
        startdateflag = '1';
    }
    if (dateCompare(enddate,today)== -1) {
        OBAlert('<%= Resource.getProperty("hcm.enddate&startdate.doesnotallow.future",lang)%> (' + document.getElementById("inpEndDate").value + ')', function(r) {
            showCalendar('frmMain.inpEndDate', document.frmMain.inpEndDate.value, false);
        });
        startdateflag = '1';
    }
    if (dateCompare(startdate,today) == -1) {
        OBAlert('<%= Resource.getProperty("hcm.enddate&startdate.doesnotallow.future",lang)%> (' + document.getElementById("inpStartDate").value + ')', function(r) {
            showCalendar('frmMain.inpStartDate', document.frmMain.inpStartDate.value, false);
        });
        enddateflag = '1';
    }
    if (dateCompare(startdate,hiredate) == -1 || dateCompare(enddate,hiredate) == -1) {
        OBAlert('<%= Resource.getProperty("hcm.startdate.enddate.lessthan.hiredate",lang)%> (' + hiredate + ')', function(r) {
            showCalendar('frmMain.inpStartDate', document.frmMain.inpStartDate.value, false);
        });
        hiredateflag = '1';
    }
    $.ajax({
        type:'GET',
        url:'<%=request.getContextPath()%>/PreEmploymentAjax?action=ChkOverlapRecords',
        data:{inpstartdate:document.getElementById("inpStartDate").value,inpenddate: document.getElementById("inpEndDate").value,inpEmployeeId : document.getElementById("inpEmployeeId").value,inpPreEmplymentId : document.getElementById("inpPreEmplymentId").value},
        dataType:'xml',
        async:false,
        success:function(data)
        {       
            $(data).find("OverLapRecords").each(function()
            {    
                var result=$(this).find("Result").text();
                if(result > 0){
                    overlapflag='1';
                    OBAlert('<%= Resource.getProperty("hcm.previousemployment.already.exists.sameperiod", lang) %>');
                     return false;
                }
                else{
                    overlapflag='0';
                }
            });             
        }
    });
    if(document.getElementById("inpempname").value=="" || document.getElementById("inpStartDate").value=="" || document.getElementById("inpEndDate").value==""){
        mandatoryflag='1'; 
        OBAlert('<%= Resource.getProperty("hcm.qualifications.mandatory", lang) %>');
    }
    if (startdateflag == '1' || enddateflag=='1' || overlapflag=='1' || mandatoryflag=='1' || hiredateflag=='1') {
        return false;
    }
    else {
        return true;
    }
}

 
function onClickSave(index, type) {
    
    if (index.className != 'Main_ToolBar_Button')
        return false;
    if (changesFlag == 1 && checkdata() ) {
        if (type == "Grid") {
            document.getElementById("inpAction").value = "GridView";
            document.getElementById("SubmitType").value = "SaveGrid";
        }
        if (type == "New") {
            document.getElementById("inpAction").value = "EditView";
            document.getElementById("SubmitType").value = "SaveNew";
        }
        if (type == "Save") {
            document.getElementById("inpAction").value = "EditView";
            document.getElementById("SubmitType").value = "Save";
        }
        reloadTab('PREEMP');
    }
}

function onClickGridView() {
    hideMessage();
    document.getElementById("inpAction").value = "GridView";
    if (changesFlag == 1 )
        OBAsk('<%=Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
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
        reloadTab('PREEMP');
}
/*  function moveCaretToStart(el) {
     var elemLen = el.value.length;
     if(elemLen==0){
         if (typeof el.selectionStart == "number") {
                el.selectionStart = el.selectionEnd = 0;
            } else if (typeof el.createTextRange != "undefined") {
                el.focus();
                var range = el.createTextRange();
                range.collapse(true);
                range.select();
            }  
     }
     else{
         el.selectionStart = elemLen;
          el.selectionEnd = elemLen;

          el.focus();
     }

   
} */

var textBox = document.getElementById("inpposition");
textBox.onfocus = function() {
      $("#inpposition").val($.trim($("#inpposition").val()));
   /*  moveCaretToStart(textBox);

    // Work around Chrome's little problem
    window.setTimeout(function() {
        moveCaretToStart(textBox);
    }, 1); */
}; 
$( document ).ready(function() {
    if($('#inpEmployeeIsActive').val()=="false"){
    document.getElementById("inpempname").setAttribute("disabled", "true");
    document.getElementById("inpAgeMY").setAttribute("disabled", "true");
    document.getElementById("inpAgeMM").setAttribute("disabled", "true");
    document.getElementById("inpAgeMD").setAttribute("disabled", "true");
    document.getElementById("inpgrade").setAttribute("disabled", "true");
    document.getElementById("inpempcat").setAttribute("disabled", "true");
    document.getElementById("inpdep").setAttribute("disabled", "true");
    document.getElementById("inpotherdet").setAttribute("disabled", "true");
    document.getElementById("inpposition").setAttribute("disabled", "true");
    $('#inpStartDate').prop('readonly',true);
    $('#inpEndDate').prop('readonly',true);
    }
});
</script>
</HTML>
