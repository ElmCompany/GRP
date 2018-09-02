 <%@page import="sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="sa.elm.ob.utility.util.Utility"%>
<%@page import="java.util.List, java.util.ArrayList"%>
<%@page import="sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO"%>
<%@page import="org.codehaus.jettison.json.JSONObject"%>
<%@page import="org.codehaus.jettison.json.JSONArray"%>
<%@ page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>
  
 <%
    String lang = ((String)session.getAttribute("#AD_LANGUAGE"));

     List<EmployeeVO> catlist = (ArrayList<EmployeeVO>) request.getAttribute("inpEmpCategory");
     /* List<EmployeeVO> inpCountry = (ArrayList<EmployeeVO>) request.getAttribute("inpCountry"); */
/*      List<EmployeeVO> inpCity = (ArrayList<EmployeeVO>) request.getAttribute("inpCity");
 */      List<EmployeeVO> inpActionType = (ArrayList<EmployeeVO>) request.getAttribute("inpActionType"); 
     List<EmployeeVO> inpNationalList = (ArrayList<EmployeeVO>) request.getAttribute("inpNationalList");
     List<EmployeeVO> inpTitleList = (ArrayList<EmployeeVO>) request.getAttribute("inpTitleList");
    
     List<EmployeeVO> inpReligionList = (ArrayList<EmployeeVO>) request.getAttribute("inpReligionList");
    String style="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css";
    String DialogBoxStyle="../web/js/common/CommonDialogFormLtr.css";
    String toolBarStyle="../web/js/common/CommonFormLtr.css";

    if(lang.equals("ar_SA")){
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
        <link rel="stylesheet" type="text/css" href="../web/js/common/htabs.css"></link>
        <link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>" />
        <LINK rel="stylesheet" type="text/css" href="<%=style %>" id="paramCSS"></LINK>
        <link rel="stylesheet" type="text/css" href="<%=DialogBoxStyle %>" />
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
        
        
        <%if(request.getAttribute("inpstatus")!=null && (request.getAttribute("inpstatus").equals("UP"))){%> 
        $('#inpDoj').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect: function(dates) { agecalculator();enableForm(); } ,showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
       
        $('#inpStartDate').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect: function() {enableForm();} ,showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger" >'});
        
        $('#inpHireDate').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect: function() {enableForm();} ,showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
        
        $('#inpGovHireDate').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect: function() {enableForm();} ,showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
        
     
        
        $('#inpMcsLetterDate').calendarsPicker({calendar:  
            $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect: function() {enableForm();}, showTrigger:  
        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
        
        <% }%>
       
       <% if(request.getAttribute("inpstatus")!=null && (request.getAttribute("inpstatus").equals("I")) && request.getAttribute("inpEndDate")==""  &&  request.getAttribute("inpExtendService")!=null && (request.getAttribute("inpExtendService").equals(false))){%> 
       $('#inpDoj').calendarsPicker({calendar:  
           $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect: function(dates) { agecalculator();enableForm(); } ,showTrigger:  
       '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
       
       
       <% }%>
       
       
       <% if(request.getAttribute("inpstatus")!=null && (request.getAttribute("inpstatus").equals("I"))||(request.getAttribute("inpstatus").equals("UP"))) {%>
        
       $('#inpmary').calendarsPicker({calendar:  
           $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect: function() {enableForm();}, showTrigger:  
       '<span class="inpMaryImage" > <img  src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup"  class="trigger"> </span>'}); 
    
       
        
       <% }%>
       
      
       
       <% if(request.getAttribute("inpstatus")!=null && (request.getAttribute("inpstatus").equals("C"))){%> 
       $('#inpStartDate').calendarsPicker({calendar:  
           $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect: function() {enableForm();}, showTrigger:  
       '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger" >'});
       
       $('#inpMcsLetterDate').calendarsPicker({calendar:  
           $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy', onSelect: function() {enableForm();}, showTrigger:  
       '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
       <% }%>
       if(document.getElementById('inpMarStat').value=="M"){
           document.getElementById("inpmary").style.display="";
           document.getElementById("inplabelmary").style.display="";
           $('.inpMaryImage').show();
          // document.getElementById("inpMaryImage").style.display="";
        
          
       }
       else{
           document.getElementById("inpmary").style.display="none";
           document.getElementById("inplabelmary").style.display="none";
           $('.inpMaryImage').hide();
          // document.getElementById("inpMaryImage").style.display="none";
       }
        
    }
    function onResizeDo(){resizeArea();reSizeGrid();agecalculator();}

    function onLoad()
    {
        <% request.setAttribute("savemsg",request.getAttribute("savemsg")==null?"":request.getAttribute("savemsg")); %>
      <%  if(request.getAttribute("savemsg").equals("Success")) { %>
        displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.saveoreditsuccess", lang) %>");
        <%}%>
        agecalculator();
        <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("I")){%> 
        document.getElementById("inpissueDecision").style.display="none";
        <%} %>
        
        
         <%
         if(request.getAttribute("inpstatus")!=null &&( request.getAttribute("inpstatus").equals("C") || request.getAttribute("inpstatus").equals("TE") || (request.getAttribute("inpstatus").equals("I") && request.getAttribute("inpEndDate")!="") 
             || (request.getAttribute("inpEmpCurrentStatus")!=null && request.getAttribute("inpEmpCurrentStatus").equals("Termination")) || (request.getAttribute("inpEmpCurrentStatus")!=null && request.getAttribute("inpEmpCurrentStatus").equals("Suspension End")))){%> 
         $('.ComboKey').prop('disabled', 'disabled');
         $('.TextBox_TwoCells_width').prop('readonly',true); 
         $('.TextBox_btn_OneCell_width').prop('readonly',true); 
         $('.dojoValidateValid_focus').prop('readonly',true); 
         $('.Combo').prop('disabled', 'disabled'); 
         DisplaylogicCancel();

        <%} %> 
        <% if(request.getAttribute("inpstatus")!=null && (request.getAttribute("inpstatus").equals("C"))){%> 
        $('#inpMcsLetterNo').prop('readonly',false); 
        $('#inpDecisionNo').prop('readonly',false); 
        $('#inpMcsLetterDate').prop('readonly',false); 
        $('#inpStartDate').prop('readonly',false); 
        $('#inpmary').prop('readonly',true);
        document.getElementById("inpHireDate").style.display="none"; 
        document.getElementById("inpGovHireDate").style.display="none";
        document.getElementById("inphireLabel").style.display="none";
        document.getElementById("inpgovhireLabel").style.display="none";
        <%} %>
        <% if(request.getAttribute("inpExtendService")!=null && (request.getAttribute("inpExtendService").equals(true))){%> 
        $('#inpDoj').prop('readonly',true); 
       <%} %> 
       
        <%  if(request.getAttribute("inpstatus")!=null && (request.getAttribute("inpstatus").equals("TE"))){ %>
        $('#inpMcsLetterNo').prop('readonly',true); 
        $('#inpDecisionNo').prop('readonly',true); 
        $('#inpMcsLetterDate').prop('readonly',true); 
        $('#inpStartDate').prop('readonly',true);
        $('#inpmary').prop('readonly',true);

        document.getElementById("inpissueDecision").style.display="none";
        <%} %> 
        var url = document.URL.split('<%=request.getContextPath()%>');
        var civsrc = url[0];
        civsrc+="<%=request.getContextPath()%>"; 
        civsrc+="/utility/ShowImage?id=";
        civsrc+="<%=request.getAttribute("inpCivimg")%>";
        $("#CivilPic").attr("src",civsrc);
        <%if(request.getAttribute("inpCivimg") !=null && request.getAttribute("inpCivimg") !=""){%> 
        $("#CivilPicText").hide();
        $("#CivilPic").show();
        <%}else{%>
          $("#CivilPic").attr("src",'#');
        <%}%>
        var worksrc = url[0];
        worksrc+="<%=request.getContextPath()%>"; 
        worksrc+="/utility/ShowImage?id=";
        worksrc+="<%=request.getAttribute("inpWrkimg")%>"   
        $("#WorkPic").attr("src",worksrc);
        <%if(request.getAttribute("inpWrkimg") !=null && request.getAttribute("inpWrkimg") !=""){%> 
        $("#WorkPicText").hide();
        $("#WorkPic").show();
        <%}else{%>
          $("#WorkPic").attr("src",'#');
        <%}%>
        document.getElementById("inpStatus").value = $("#inpstatus").find('option:selected').val();
        if(document.getElementById('inpcancelHiring').value == "false" && document.getElementById('inpHiringDecision').value == "false" )
        {
         document.getElementById('LinkButtonDownload').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print_disabled';
        }
        else if(document.getElementById('inpcancelHiring').value == "true" )
            {
          document.getElementById('LinkButtonDownload').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print';
        }
        else if(document.getElementById('inpHiringDecision').value == "true")
            {
            document.getElementById('LinkButtonDownload').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print';

            }

       
    }
    
    function agecalculator()
    {
         var year,date,month=0;
         var maximumcurrentdate=0;
         
         var dateString=document.getElementById("inpDoj").value;
         var now = "<%=request.getAttribute("today")%>";
        
       var dobyear=dateString.substring(6,10);
         var dobmonth= dateString.substring(3,5)  ;
         var dobdate= dateString.substring(0,2) ;
         var nowyear = now.substring(6,10);
         var nowmonth = now.substring(3,5);
         var nowdate = now.substring(0,2);
         
         year=nowyear-dobyear;
         month=nowmonth-dobmonth
         if(month<0){
             year-- ;
             month = 12 - parseInt(dobmonth) + parseInt(nowmonth);
             
             if( nowdate <  dobdate){
                 month--;
             }
         }                                                    
         else if(month==0 && nowdate <  dobdate){
             year-- ;
             month=11;
             
         }
         else if(month > 1  && nowdate <  dobdate){
              month--;
         }
      
         if (nowdate > dobdate){
             date = nowdate - dobdate;
             $('#inpAgeMD').val(date);

         }
          else if (nowdate < dobdate)
          {
              var today = nowdate;
              //month--;
              nowmonth=nowmonth-1;
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
        $('#inpAgeMY').val(year); 
        $('#inpAgeMM').val(parseInt(month));
    }
    function DisplaylogicCancel(){
        var status ="<%=request.getAttribute("inpstatus")%>";
        var isTerminated = "<%=request.getAttribute("inpEmpCurrentStatus")!=null && request.getAttribute("inpEmpCurrentStatus").equals("Termination")%>";
        var isSuspended = "<%=request.getAttribute("inpEmpCurrentStatus")!=null && request.getAttribute("inpEmpCurrentStatus").equals("Suspension End")%>";
        $.post('<%=request.getContextPath()%>/EmployeeAjax', {
            action : 'checkAlreadyCancelIssue',
            inpEmpNo : document.getElementById("inpEmpNo").value
        }, function(data) {
            var result = data.getElementsByTagName("result")[0].childNodes[0].nodeValue;
            if(result=='true'){
                document.getElementById("inpissueDecision").style.display="none"; 
            }
            else if(status=="TE")
                document.getElementById("inpissueDecision").style.display="none"; 
            else if (isTerminated=='true' || isSuspended=='true' )
                document.getElementById("inpissueDecision").style.display="none"; 
            else
                  document.getElementById("inpissueDecision").style.display=""; 
            
            if(document.getElementById('inpMarStat').value=="M"){
                document.getElementById("inpmary").style.display="";
            }
            else{
                document.getElementById("inpmary").style.display="none";
            }
        });
    }
    </script>
</HEAD>
<BODY onresize="onResizeDo();" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" id="paramBody">
 <FORM id="form" method="post" action="" name="frmMain">
   <INPUT type="hidden" name="Command"></INPUT>
   <INPUT type="hidden" name="IsPopUpCall" value="1"></INPUT>
   <INPUT type="hidden" name="inpLastFieldChanged"></INPUT>
   <INPUT type="hidden" name="inpAction" id="inpAction" value=""></INPUT>
       <INPUT type="hidden" name="inpempCategory" id="inpempCategory" value="<%= request.getAttribute("inpempCategory") %>"></INPUT>
      <INPUT type="hidden" name="inpExEmployeeId" id="inpExEmployeeId" value="<%= request.getAttribute("inpExEmployeeId") %>"></INPUT>
       <INPUT type="hidden" name="inpminage" id="inpminage" value="<%= request.getAttribute("inpminage") %>"></INPUT>
      <INPUT type="hidden" name="inpmaxage" id="inpmaxage" value="<%= request.getAttribute("inpmaxage") %>"></INPUT>
     <INPUT type="hidden" name="inpStatus" id="inpStatus" value="<%= request.getAttribute("inpstatus") %>"></INPUT>
      <INPUT type="hidden" name="selectCountryId" id="selectCountryId" value="<%= request.getAttribute("inpCountryId") %>"></INPUT>
      <INPUT type="hidden" name="selectCountryName" id="selectCountryName" value="<%= request.getAttribute("inpCountryName") %>"></INPUT>
       <INPUT type="hidden" name="selectCityId" id="selectCityId" value="<%= request.getAttribute("inpCityId") %>"></INPUT>
      <INPUT type="hidden" name="selectCityName" id="selectCityName" value="<%= request.getAttribute("inpCityName") %>"></INPUT>
       <INPUT type="hidden" name="inpStatus" id="inpStatus" value="<%= request.getAttribute("inpstatus") %>"></INPUT>
     <% if(request.getAttribute("inpstatus")!=null && (request.getAttribute("inpstatus").equals("C") || request.getAttribute("inpstatus").equals("I")  )){%>
<% if(request.getAttribute("inpstatus").equals("I")) { %>  
<INPUT type="hidden" name="inpSalutation" id="inpSalutation" value="<%= request.getAttribute("inpSalutation") %>"></INPUT>
   <INPUT type="hidden" name="inpEmpCat" id="inpEmpCat" value="<%= request.getAttribute("inpEmpCat") %>"></INPUT> 
  <% } else if(request.getAttribute("inpstatus").equals("C")) { %> 
      <INPUT type="hidden" name="inpTitle" id="inpTitle" value="<%= request.getAttribute("inpTitle") %>"></INPUT>
      <%--  <INPUT type="hidden" name="inpCountry" id="inpCountry" value="<%= request.getAttribute("inpCountryId") %>"></INPUT> 
         <INPUT type="hidden" name="inpCity" id="inpCity" value="<%= request.getAttribute("inpCityId") %>"></INPUT>  --%>
         <INPUT type="hidden" name="inpNat" id="inpNat" value="<%= request.getAttribute("inpNat") %>"></INPUT> 
         <INPUT type="hidden" name="inpRel" id="inpRel" value="<%= request.getAttribute("inpRel") %>"></INPUT> 
                  <INPUT type="hidden" name="inpGen" id="inpGen" value="<%= request.getAttribute("inpGen") %>"></INPUT>
  <INPUT type="hidden" name="inpMarStat" id="inpMarStat" value="<%= request.getAttribute("inpMarStat") %>"></INPUT> 
    <INPUT type="hidden" name="inpBlodTy" id="inpBlodTy" value="<%= request.getAttribute("inpBlodTy") %>"></INPUT>  
  <%} %>
        <%} %>
                <INPUT type="hidden" name="inpSalText" id="inpSalText" value="<%= request.getAttribute("inpSalText") %>"></INPUT>
   <INPUT type="hidden" name="SubmitType" id="SubmitType" value=""></INPUT>
   <INPUT type="hidden" name="inpcivfilebyte" id="inpcivfilebyte" value=""></INPUT>
   <INPUT type="hidden" name="inpcivfilesize" id="inpcivfilesize" value=""></INPUT>
   <INPUT type="hidden" name="inpcivfilename" id="inpcivfilename" value=""></INPUT>
   <INPUT type="hidden" name="inpcivfiletype" id="inpcivfiletype" value=""></INPUT>
   <INPUT type="hidden" name="inpwrkfilebyte" id="inpwrkfilebyte" value=""></INPUT>
   <INPUT type="hidden" name="inpwrkfilesize" id="inpwrkfilesize" value=""></INPUT>
   <INPUT type="hidden" name="inpwrkfilename" id="inpwrkfilename" value=""></INPUT>
   <INPUT type="hidden" name="inpwrkfiletype" id="inpwrkfiletype" value=""></INPUT> 

   
   <INPUT type="hidden" name="inpEmployeeId" id="inpEmployeeId" value="<%= request.getAttribute("inpEmployeeId") %>"></INPUT>
   <INPUT type="hidden" id="inpAddressId" name="inpAddressId" value="<%= request.getAttribute("inpAddressId") %>"></INPUT>
    <INPUT type="hidden" name="inpEmpStatus" id="inpEmpStatus" value="<%= request.getAttribute("inpEmpStatus") %>"></INPUT>
      <INPUT type="hidden" id="inpEmployeeStatus" name="inpEmployeeStatus" value="<%= request.getAttribute("inpEmployeeStatus") %>"></INPUT>
      <INPUT type="hidden" id="inpcancelHiring" name="inpcancelHiring" value="<%= request.getAttribute("cancelHiring") %>"></INPUT>
      <INPUT type="hidden" id="inpHiringDecision" name="inpHiringDecision" value="<%= request.getAttribute("Hiringdecision") %>"></INPUT>
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
                                                             <input type="checkbox"  id ="inpIsjoiningWorkReq" name="inpIsjoiningWorkReq"s></input>
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
                 <TR><TD class="Main_NavBa r_bg_left" id="tdleftSeparator"></TD></TR>
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
                        <td width="2%" ><a href="javascript:void(0);" onClick="onclickdownload()" class="Main_ToolBar_Button" onMouseOver="window.status='Download';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="download"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.download",lang) %>" border="0" id="LinkButtonDownload"></a></td> 
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
                                        <a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%= Resource.getProperty("hcm.employee",lang)%>
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
               <TABLE class="dojoTabContainer" border="0" cellpadding="0" cellspacing="0" width="100%" summary=""><TR><TD>
                <TABLE class="dojoTabPaneWrapper" border="0" cellpadding="0" cellspacing="0" width="100%" summary=""><TR><TD>
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
                            <div id="TabEmployee" class="tabCurrent"><span class="LabelText"><%= Resource.getProperty("hcm.employee",lang)%></span></div>
                            <div style="text-align: center;">
                                <img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;">
                                </img>
                            </div>
                        </td>
                      <% if(request.getAttribute("inpEmployeeId").toString().length()!=32) { %>
                        <td><img  id="ImgEmpInfo" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabEmpInfo"  class="tabNotSelected"><span class="LabelText"><%= Resource.getProperty("hcm.empmntinfo",lang)%></span></div></td>
                        
                        <td><img id="ImgEmpAddress" class="imageNotSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                       <td><div id="TabEmpAddress" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.empaddress",lang)%></span></div></td>
                      
                        <td><img  id="ImgqualInfo" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabqualInfo"  class="tabNotSelected"><span class="LabelText"><%= Resource.getProperty("hcm.empqualinfo",lang)%></span></div></td>                     
                        
                        <td><img  id="ImgDependent" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabDependent" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.dependent",lang)%></span></div>
                        <div style="text-align: center;display:none;"><img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img></div></td>
                         <td><img  id="ImgPreEmp" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                               <td><div id="Tabpreemp" class="tabNotSelected"><span class="LabelText"><%= Resource.getProperty("hcm.emppreemp",lang)%></span></div></td>
                         <td><img id="ImgEmpContract" class="imageNotSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabEmpContract" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                          <td><img id="ImgMedIns" class="imageNotSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabMedIns" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.MedicalInsurance",lang)%></span></div></td>
                        
                         <td><img  id="ImgAsset" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabAsset" class="tabNotSelected"><span class="LabelText"><%= Resource.getProperty("hcm.Facilities&Assets",lang)%></span></div>
                          <td><img  id="ImgDocument" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabDocument" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.documents",lang)%></span></div>
                        </td>
                        <td><img  id="ImgEmpPerPayMethod" class="imageNotSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabEmpPerPayMethod" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></span></div>
                        </td>
                         <%}else{%>
                          <td><img  id="ImgEmpInfo" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabEmpInfo"  class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPINF');"><span class="LabelText"><%= Resource.getProperty("hcm.empmntinfo",lang)%></span></div></td>
                        
                        <td><img id="ImgEmpAddress" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                       <td><div id="TabEmpAddress" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPADD');"><span class="LabelText"><%= Resource.getProperty("hcm.empaddress",lang)%></span></div></td>
                      
                        <td><img  id="ImgqualInfo" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabqualInfo"  class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPQUAL');"><span class="LabelText"><%= Resource.getProperty("hcm.empqualinfo",lang)%></span></div></td>                     
                        <td><img  id="ImgDependent" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabDependent" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('Dependent');"><span class="LabelText"><%= Resource.getProperty("hcm.dependent",lang)%></span></div>
                        <div style="text-align: center;display:none;"><img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img></div></td>
                        <td><img  id="ImgPreEmp" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                               <td><div id="Tabpreemp" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('PREEMP');" ><span class="LabelText"><%= Resource.getProperty("hcm.emppreemp",lang)%></span></div>
                        <% if(request.getAttribute("inpempCategory").toString().equals("Y")) { %>
                          <td><img id="ImgEmpContract" class="imageSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                          <td><div id="TabEmpContract" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('EMPCTRCT');" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                          <%}else{ %>
                          <td><img id="ImgEmpContract" class="imageNotSelected" alt="" src="../web/images/DoubleArrow.png"></img></td>
                           <td><div id="TabEmpContract" class="tabNotSelected" ><span class="LabelText"><%= Resource.getProperty("hcm.Contract",lang)%></span></div></td>
                           <%} %>
                          
                               <div style="text-align: center;display:none;"><img alt="" src="../web/images/OrangeArrowDown.png" style="margin-top: -1px;"></img></div></td>
                       
                         <td><img  id="ImgMedIns" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                             <td><div id="TabMedIns" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('MEDIN');"><span class="LabelText"><%= Resource.getProperty("hcm.MedicalInsurance",lang)%></span></div>
                        </td>
                       
                       
                             <td><img  id="ImgAsset" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                             <td><div id="TabAsset" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('Asset');"><span class="LabelText"><%= Resource.getProperty("hcm.Facilities&Assets",lang)%></span></div>
                        </td>
                          <td><img  id="ImgDocument" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                        <td><div id="TabDocument" class="tabSelected" onmouseover="this.className='tabSelectedHOVER';" onmouseout="this.className='tabSelected';" onclick="reloadWindow('DOC');"><span class="LabelText"><%= Resource.getProperty("hcm.documents",lang)%></span></div>  
                        
                        <td><img  id="ImgEmpPerPayMethod" class="imageSelected"   alt="" src="../web/images/DoubleArrow.png"></img></td>
                                   <td>
                                     <div id="TabEmpPerPayMethod" class="tabSelected" onclick="reloadWindow('PERPAYMETHOD')">
                                     <span class="LabelText"><%= Resource.getProperty("hcm.EmpPerPayMethod",lang)%></span>
                                     </div>
                                    </td>
                         <%} %>
                             
                       
                        </tr>
                    </tbody>
                 </table>
                 <div style="margin-bottom: 5px;"></div>
             </div>
                   <div id="LoadingContent" style="display:none;">
                       <div style="text-align: center;margin-top: 17%;"><img alt="Loading" src="../web/images/loading.gif"></div>
                       <div style="color:#000000;font-family: sans-serif;font-size:14px;margin-top:10px;text-align: center;"><%= Resource.getProperty("hcm.loading", lang)%>... </div>
                   </div>
                   
                   <div style="width:100%;">
                       <div style="padding: 0 1%; width: 98%;">
                           <div style="width:100%;height:890px;" id="FormDetails">
<!--                               <div align="left">
-->                                <DIV id="employee">
                                   <TABLE style="width:80%; margin-top: 10px;">
                                    <TR>
                                         <TD class="TitleCell" align="left" style="width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.actiontype", lang)%></span>
                                        </TD>
                                        <TD class="TextBox_ContentCell">
                                             <SELECT id="inpSalutation" name="inpSalutation" class="ComboKey Combo_TwoCells_width"  <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("I")){%> disabled<%} %> onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this); changesal(this);" onkeydown="return true;" onkeyup="enableForm(this);">
                                                  <%
                                                   if(inpActionType!=null && inpActionType.size()>0) { %>
                                                    <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option>
                                                    <% for(EmployeeVO vo:inpActionType){%>
                                                         <option value='<%= vo.getActTypeId()%>' <%if(request.getAttribute("inpSalutation") !=null && request.getAttribute("inpSalutation").equals(vo.getActTypeId())){%> selected<%} %>><span><%= vo.getActTypeValue()+" - "+vo.getActTypeName()%></span></option>
                                                    <%}}%>
                                                </SELECT>
                                             <input class="dojoValidateValid" type="hidden" name="inpActionType" id="inpActionType" maxlength="35" value="<%= request.getAttribute("inpActionTypeList")==null ? "":request.getAttribute("inpActionTypeList") %>" readonly class="" style="width:70%;" /> 
                                        </TD>
                                        <TD class="TitleCell" align="left" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.persontype", lang)%></span></TD>
                                        <TD class="TextBox_ContentCell">  
                                            <input type="text" name="inpPersonType" id="inpPersonType" maxlength="35" style="overflow: hidden; text-overflow:ellipsis;" readonly value="<%= request.getAttribute("inpPersonType")==null ? "":request.getAttribute("inpPersonType") %>" required="false"   class="dojoValidateValid  TextBox_TwoCells_width" />                                       </TD>
                                        <TD class="TitleCell" align="left" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.status", lang)%></span></TD>
                                        <TD class="TextBox_ContentCell"> 
                                             <select id="inpstatus" name="inpstatus"  disabled>
                                                 <option value='UP' <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("UP")){%> selected<%} %> ><span><%= Resource.getProperty("hcm.underproc",lang)%></span></option>
                                                 <option value='I' <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("I")){%> selected<%} %>><span><%= Resource.getProperty("hcm.issued",lang)%></span></option> 
                                                  <option value='C' <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("C")){%> selected<%} %>><span><%= Resource.getProperty("hcm.cancelled",lang)%></span></option>
                                                  <option value='TE' <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("TE")){%> selected<%} %>><span><%= Resource.getProperty("hcm.terminate.employment",lang)%></span></option>
                                             </select>
                                       </TD>
                                    </TR>
                                    <TR>
                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.empcategory",lang)%></span></TD>
                                        <TD class="TextBox_ContentCell">
                                           <SELECT id="inpEmpCat" name="inpEmpCat"  <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("I")){%> disabled<%} %> class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this);" onkeydown="return true;" onkeyup="enableForm(this);">
                                               <%  if(catlist!=null && catlist.size()>0) { %>
                                               <%--  <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option> --%>
                                                <% for(EmployeeVO vo:catlist){
                                                     %>
                                                <option value='<%= vo.getCategoryId()%>' <%if(request.getAttribute("inpEmpCat") !=null && request.getAttribute("inpEmpCat").equals(vo.getCategoryId())){%> selected<%} %>  ><span><%= vo.getCategorycode()%></span></option>
                                                <%}}%>
                                           </SELECT>
                                        </TD>
                                        <TD class="TitleCell"  id="inphireLabel" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.hiredate",lang)%></span></TD>
                                        <TD class="TextBox_btn_ContentCell">
                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                 <tr>
                                               <td class="TextBox_ContentCell">
                                                     <input type="text" id="inpHireDate" class="dojoValidateValid TextBox_btn_OneCell_width required" onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this); "  value="<%= request.getAttribute("inpHireDate")=="" ? request.getAttribute("today"):request.getAttribute("inpHireDate") %>"  <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("I")){%> readonly<%} %> maxlength="10" name="inpHireDate" ></input> 
                                                  </td>
                                              </tr>
                                             </table>
                                        </TD>
                                        <TD class="TitleCell" id="inpgovhireLabel" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.govhiredate",lang)%></span>
                                        </TD>
                                        <TD class="TextBox_btn_ContentCell">
                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                              <tr>
                                                 <td class="TextBox_ContentCell">
                                                     <input type="text" id="inpGovHireDate" class="dojoValidateValid TextBox_btn_OneCell_width " <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("I")){%> readonly<%} %> onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this); "  value="<%= request.getAttribute("inpGovHireDate") %>" maxlength="10" name="inpGovHireDate" ></input> 
                                                 </td>
                                             </tr>
                                            </table>
                                        </TD>
                                    </TR>
                                    <TR>
                                       <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.startdate",lang)%></span></TD>
                                       <TD class="TextBox_btn_ContentCell">
                                        <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                          <tr>
                                              <td class="TextBox_ContentCell">
                                                     <input type="text" id="inpStartDate"  class="dojoValidateValid TextBox_btn_OneCell_width required" <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("I")){%> readonly<%} %> onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this); "  value="<%= request.getAttribute("inpStartDate")==""? request.getAttribute("today"):request.getAttribute("inpStartDate") %>" maxlength="10" name="inpStartDate" ></input> 
                                              </td>
                                         </tr>
                                        </table>
                                       </TD>
                                       <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.enddate",lang)%></span></TD>
                                       <TD class="TextBox_btn_ContentCell" >
                                        <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                          <tr>
                                                <td class="TextBox_ContentCell">
                                                     <input type="text" id="inpEndDate" class="dojoValidateValid TextBox_btn_OneCell_width " onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this); " readonly value="<%= request.getAttribute("inpEndDate") %>" maxlength="10" name="inpEndDate" ></input> 
                                             </td>
                                         </tr>
                                        </table>
                                       </TD>
                                        <TD class="TitleCell" id="inpEmployeeStatusLabel" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.absence.accrual.employee.status",lang)%></span>
                                        </TD>
                                        <TD class="TextBox_btn_ContentCell">
                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                              <tr>
                                                 <td class="TextBox_ContentCell">
                                                     <input type="text" id="inpEmpCurrentStatus" class="dojoValidateValid TextBox_TwoCells_width "  readonly onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this); "  value="<%= request.getAttribute("inpEmpCurrentStatus") %>" maxlength="20" name="inpEmpCurrentStatus" ></input> 
                                                 </td>
                                             </tr>
                                            </table>
                                        </TD>
                                      </TR>
                                      <TR>
                                           <TD class="TitleCell" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.mcsno",lang)%></span></TD>
                                           <TD class="TextBox_ContentCell">
                                            <input type="text" name="inpMcsLetterNo" id="inpMcsLetterNo" maxlength="30" <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("I")){%> readonly<%} %>   value="<%= request.getAttribute("inpMcsLetterNo") %>" required="true"  class="dojoValidateValid TextBox_TwoCells_width required" onkeydown="return true;" onkeyup="enableForm(this);" />
                                           </TD>
                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.mcsdate",lang)%></span></TD>
                                           <TD class="TextBox_btn_ContentCell" colspan=3>
                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                              <tr>
                                               <td class="TextBox_ContentCell">
                                                     <input type="text" id="inpMcsLetterDate" class="dojoValidateValid TextBox_btn_OneCell_width"  <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("I")){%> readonly<%} %>    value="<%= request.getAttribute("inpMcsLetterDate") %>" maxlength="10" name="inpMcsLetterDate" onkeydown="return true;" onkeyup="enableForm(this);" ></input> 
                                               </td>
                                             </tr>
                                            </table>
                                           </TD>
                                     </TR>
                                     <TR>
                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.decisionno",lang)%></span></TD>
                                        <TD class="TextBox_ContentCell">
                                                  <input type="text" name="inpDecisionNo" id="inpDecisionNo"  <%if(request.getAttribute("inpstatus")!=null && request.getAttribute("inpstatus").equals("I")){%> readonly<%} %>   maxlength="30" value="<%= request.getAttribute("inpDecisionNo") %>" required="true"  class="dojoValidateValid TextBox_TwoCells_width required" onkeydown="return true;" onkeyup="enableForm(this);"/>
                                        </TD>
                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.decisiondate",lang)%></span></TD>
                                        <TD class="TextBox_btn_ContentCell" colspan=3>
                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                              <tr>
                                               <td class="TextBox_ContentCell">
                                                    <input type="text" id="inpDecisionDate" class="dojoValidateValid TextBox_btn_OneCell_width"  value="<%= request.getAttribute("inpDecisionDate") %>" readonly  maxlength="10" name="inpDecisionDate" onkeydown="return true;" onkeyup="enableForm(this);" ></input>
                                               </td>
                                             </tr>
                                            </table>
                                         </TD>
                                     </TR>
                                 </TABLE>
                               </DIV>  
                                    <!-- Name Field Group -->
                                                         
                                    
                                      <DIV id="name1">
                                    <TABLE>
                                    
                                    <TR>
                                    <TD>
                                    <DIV id="nameGRP">
                                     <TABLE style="width:80%; margin-top: 10px;">
                                     <TR>
                                        <TD colspan="6">
                                           <table style="width: 100%;padding: 2px;">
                                               <tr>
                                                <td>
                                                   <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                       <TBODY>
                                                           <TR class="FieldGroup_TopMargin"></TR>
                                                           <TR>
                                                               <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                               <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.name",lang)%></TD>
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
                                    </DIV>
            
                                    <DIV id="name">
                                     <TABLE style="width:80%; margin-top: 10px;">
                                        <TR>
                                           <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.title",lang)%></span></TD>
                                           <TD class="TextBox_ContentCell" >
                                            <SELECT id="inpTitle" name="inpTitle" class="ComboKey Combo_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this);">
                                            <%  if(inpTitleList!=null && inpTitleList.size()>0) { %>
                                                <%-- <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option> --%>
                                                <% for(EmployeeVO vo:inpTitleList){
                                                     %>
                                                <option value='<%= vo.getTitleId()%>' <%if(request.getAttribute("inpTitle") !=null && request.getAttribute("inpTitle").equals(vo.getTitleId())){%> selected<%} %>  ><span><%= vo.getTitleName()%></span></option>
                                                <%}}%>
                                            </SELECT>
                                           </TD>
                                           <TD class="" align="center" style="min-width:110px;" colspan=2><span class="LabelText" ><%= Resource.getProperty("hcm.gender",lang)%></span>
<!--                                            </TD>
                                           <TD class="TextBox_ContentCell" colspan=2 style="width: 30px;"> -->
                                               <SELECT id="inpGen" name="inpGen" colspan="2" class="ComboKey Combo_OneCells_width" onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this);" style="width: 95px;">
                                                 <option value='F' <%if(request.getAttribute("inpGen") !=null && request.getAttribute("inpGen").equals("F")){%> selected<%} %> ><span><%= Resource.getProperty("hcm.female",lang)%></span></option>
                                                   <option value='M' <%if(request.getAttribute("inpGen") !=null  && request.getAttribute("inpGen").equals("M")){%> selected<%} %> ><span><%= Resource.getProperty("hcm.male",lang)%></span></option>
                                               </SELECT>
                                           </TD>
                                         </TR>
                                        <TR>
                                           <TD class="" align="center" style="min-width:110px;" colspan=2><span class="LabelText" ><b><%= Resource.getProperty("hcm.arabic",lang)%></b></span></TD>
                                           <TD class="" align="center" style="min-width:110px;" colspan=4><span class="LabelText" >English</span></TD>
                                        </TR>
                                        <TR>
                                           <TD class="TitleCell" align="center" colspan=6><span class="LabelText" ></span></TD>
                                        </TR>
                                        <TR>
                                             <TD class="TitleCell" align="right" ><span class="LabelText" ><%= Resource.getProperty("hcm.firstname",lang)%></span></TD>
                                             <TD class="" >
                                                    <input type="text" name="inpAraFName" id="inpAraFName" maxlength="30" value="<%=request.getAttribute("inpAraFName") %>"  required="true" class="dojoValidateValid TextBox_TwoCells_width required" onkeydown="return true;" onkeyup="enableForm(this);" dir="rtl"/>
                                             </TD>
                                             <TD class="TextBox_ContentCell" colspan=4>
                                                <input type="text" name="inpEngFName" id="inpEngFName" maxlength="30" value="<%=request.getAttribute("inpEngFName") %>"   required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" dir="ltr !important"/>
                                             </TD>
                                        </TR>
                                        <TR>
                                           <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.fathername",lang)%></span></TD>
                                            <TD class="TextBox_ContentCell">
                                                <input type="text" name="inpAraFatName" id="inpAraFatName" maxlength="30" value="<%=request.getAttribute("inpAraFatName") %>"   required="false" class="dojoValidateValid TextBox_TwoCells_width required" required="true"   class="dojoValidateValid TextBox_TwoCells_width required" onkeydown="return true;" onkeyup="enableForm(this);"  dir="rtl"/>
                                            </TD>
                                            <TD class="TextBox_ContentCell"  colspan=4>
                                                <input type="text" name="inpEngFatName" id="inpEngFatName" maxlength="30" value="<%=request.getAttribute("inpEngFatName") %>" required="false"   class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" dir="ltr !important" />
                                            </TD>
                                        </TR>
                                        <TR>
                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.grandfather",lang)%></span></TD>
                                            <TD class="TextBox_ContentCell">
                                                <input type="text" name="inpAraGraFatName" id="inpAraGraFatName" maxlength="30" value="<%=request.getAttribute("inpAraGraFatName") %>"   required="true" class="dojoValidateValid TextBox_TwoCells_width required" onkeydown="return true;" onkeyup="enableForm(this);" dir="rtl" />
                                            </TD>
                                            <TD class="TextBox_ContentCell"  colspan=4>
                                            <input type="text" name="inpEngGraFatName" id="inpEngGraFatName" maxlength="30" value="<%=request.getAttribute("inpEngGraFatName") %>"  required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);"  dir="ltr !important"/>
                                            </TD>
                                        </TR>
                                        <TR>
                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.fourthname",lang)%></span></TD>
                                            <TD class="TextBox_ContentCell">
                                                <input type="text" name="inpAraFourthName" id="inpAraFourthName" maxlength="30" value="<%=request.getAttribute("inpAraFourthName") %>"  required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" dir="rtl" />
                                            </TD>
                                            <TD class="TextBox_ContentCell"  colspan=4>
                                                <input type="text" name="inpEngFourthName" id="inpEngFourthName" maxlength="30" value="<%=request.getAttribute("inpEngFourthName") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);"  dir="ltr !important"/>
                                            </TD>
                                        </TR>
                                        <TR>
                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.family",lang)%></span></TD>
                                            <TD class="TextBox_ContentCell">
                                              <input type="text" name="inpAraFamName" id="inpAraFamName" maxlength="30" value="<%=request.getAttribute("inpAraFamName") %>" required="true"  class="dojoValidateValid TextBox_TwoCells_width required" onkeydown="return true;" onkeyup="enableForm(this);"  dir="rtl"/>
                                            </TD>
                                            
                                            <TD class="TextBox_ContentCell"  colspan=4>
                                                <input type="text" name="inpEngFamName" id="inpEngFamName" maxlength="30" value="<%=request.getAttribute("inpEngFamName") %>" required="false"  class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);"  dir="ltr !important"/>
                                            </TD>
                                        </TR>
                                        
                                         
                                        
                                        
                                     </TABLE>
                                     </DIV>
                                     
                                     </TD>
                                     
                                     <TD>
                                        <!-- Picture Field Group -->
                                     <DIV id="picturegrp">
                                    <TABLE style="width:80%; margin-top: 10px;">
                                     <TR>
                                       <TD colspan="6">
                                           <table style="width: 100%;padding: 2px;">
                                               <tr>
                                               <td>
                                                   <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                    <TBODY>
                                                       <TR class="FieldGroup_TopMargin"></TR>
                                                       <TR>
                                                           <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                           <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.picture",lang)%></TD>
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
                                   </DIV>
                                   
                                   <DIV id="picture">
                                     <TABLE style="width:80%; margin-top: 10px;">
                                      <TR>
                                        <TD class="" align="center" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.civicpicture",lang)%></span></TD>
                                        <TD class="" align="center" style="min-width:110px;" colspan="5"><span class="LabelText" ><%= Resource.getProperty("hcm.workpicture",lang)%></span></TD>
                                       </TR>
                                      <TR>
                                         <TD  class="" align="right" style="min-width:110px;" >
                                          <input type="text"   align="center"  id="CivilPicText"  style="min-width:110px; min-height:180px; "  value="" >
                                          <input type='file'  align="center"  style="min-width:110px;" onchange="enableForm(this);"  name="CivilPicFile" id="CivilPicFile"> 
                                          <img id="CivilPic" align="center" style="width: 200px;height: 200px; display:none"  src='#' alt="CivicPic" />
                                       </TD>
                                        <TD  class="" align="right" style="min-width:110px;" colspan="5">
                                          <input type="text" align="center"     id="WorkPicText"  style="min-width:110px; min-height:180px; "  value="">
                                          <input type='file'  align="center" style="min-width:110px;"  onchange="enableForm(this);"  name="WorkPicFile" id="WorkPicFile"> 
                                          <img id="WorkPic" align="center"  style="width: 200px;height: 200px; display:none" src="#" alt="WorkPic" />
                                       </TD>
                                      </TR>
                                     </TABLE>
                                   </DIV>
                                       
                               </TD>
                               </TR>
                               
                               </TABLE>
                               </DIV>  
            
                                           <DIV id="person1">
                                           <TABLE style="width:80%; margin-top: 10px;">
                                           <TR>
                                           <TD>
                                           <!-- Personal Information Field Group -->
                                               <DIV id="personalgrp">
                                                 <TABLE style="width:80%; margin-top: 10px;">
                     <TR>
                                                    <TD>
                                                        <table style="width: 100%;padding: 2px;">
                                                         <tr>
                                                         <td>
                                                          <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                           <TBODY>
                                                            <TR class="FieldGroup_TopMargin"></TR>
                                                            <TR>
                                                                <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.personalinfo",lang)%></TD>
                                                                <TD class="FieldGroupTitle_Right"><IMG class="FieldGroupTitle_Right_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                <TD class="FieldGroupContent"></TD>
                                                            </TR>    
                                                            <TR class="FieldGroup_BottomMargin" style="display: none;"></TR>          
                                                           </TBODY>
                                                          </TABLE></td></tr>
                                                          </table>
                                                          </TD></TR></TABLE></DIV>
                                                         
                                                         </TD>
                                                         <TD>
                                                         <DIV id="personalgrp">
                                                            <table style="width: 100%;padding: 2px;">
                                                            <tr>
                                                            <td>
                                                             <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
                                                              <TBODY>
                                                               <TR class="FieldGroup_TopMargin"></TR>
                                                               <TR>
                                                                   <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                                                                   <TD class="FieldGroupTitle" id="subGridTitle"><%= Resource.getProperty("hcm.contactdetails",lang)%></TD>
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
                                                         </TD>
                                                         </TR>
                                                      </TD>
                                                      </TR>
                                                      <TR><TD>
                                               <DIV id="personal">
                                               <TABLE style="width:80%; margin-top: 10px;">
                                                    <TR>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.nationalidentifier",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell">
                                                            <input type="text" name="inpNatIdf" id="inpNatIdf" maxlength="10" value="<%=request.getAttribute("inpNatIdf") %>" onkeydown="return true;" onkeyup="enableForm(this);" onkeypress="return isNumberKey(event);" required="false" class="dojoValidateValid TextBox_TwoCells_width required" onkeydown="return onChangeEvent(event); enableForm(this);" />
                                                        </TD>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.employeeno",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" colspan=3>
                                                            <input type="text" name="inpEmpNo" id="inpEmpNo" maxlength="30" value="<%=request.getAttribute("inpEmpNo") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width required" onkeydown="return true;" onkeyup="enableForm(this);" />
                                                        </TD> 
                                                    </TR>
                                                    <TR>
                                                         <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.dob",lang)%></span></TD>
                                                         <TD class="TextBox_btn_ContentCell">
                                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                             <tr>
                                                                 <td class="TextBox_ContentCell">
                                                                  <input type="text" id="inpDoj" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this);agecalculator();"  class="dojoValidateValid TextBox_btn_OneCell_width required" value="<%= request.getAttribute("inpDoj")=="" ? request.getAttribute("today"): request.getAttribute("inpDoj") %>" maxlength="10" name="inpDoj" onchange="agecalculator();" onkeydown="return true;" onkeyup="enableForm(this);"></input>
                                                                 </td>
                                                              </tr>
                                                             </table>
                                                         </TD><TD  class="TextBox_OneCells_width" align="center" style="min-width:110px;"></TD>
                                                         <TD><span>Y</span></TD><TD>
                                                         <span align="center">M</span></TD>
                                                         <TD><span align="center">D</span></TD>
                                                    </TR>
                                                     <TR>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.country",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" >
                                                            <SELECT id="inpCountry" name="inpCountry" class="ComboKey Combo_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this); changecountry(this);">
                                                             <%--  <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option>
                                                              <% if(inpCountry!=null && inpCountry.size()>0) { %>
                                                                <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option>
                                                                <% for(EmployeeVO vo:inpCountry){%>
                                                                 <option value='<%= vo.getCountryId()%>' <%if(request.getAttribute("inpCountryId")!=null &&  request.getAttribute("inpCountryId").equals(vo.getCountryId())){%> selected<%}else if ( request.getAttribute("inpCountryId").equals("") && vo.getIsdefault().equals("Y")){%> selected<%}  %>  ><span><%= vo.getCountryName()%></span></option>
                                                                <%}}%> --%>
                                                            </SELECT>
                                                       </TD>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.age",lang)%></span></TD>
                                                        <TD class="">                                                       
                                                           <input type="text"  name="inpAgeMY" id="inpAgeMY" maxlength="2" value="" readonly required="false" 
                                                           class="dojoValidateValid" onkeydown="return onChangeEvent(event);"  style="width: 30px;"/>
                                                        </TD><TD>
                                                           <input type="text"  name="inpAgeMM" id="inpAgeMM" maxlength="2" value="" readonly required="false" 
                                                           class="dojoValidateValid" onkeydown="return onChangeEvent(event);"  style="width: 30px;"/>
                                                           </TD><TD>
                                                            <input type="text" name="inpAgeMD" id="inpAgeMD" maxlength="2" value="" readonly required="false" 
                                                            class="dojoValidateValid" onkeydown="return onChangeEvent(event);"  style="width: 30px;"/>
                                                        </TD>
                                                    </TR>
                                                    <TR>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.city",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" >
                                                            <SELECT id="inpCity" name="inpCity" class="ComboKey Combo_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this);">
                                                             <%--  <%
                                                               if(inpCity!=null && inpCity.size()>0) { %>
                                                                <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option>
                                                                <% for(EmployeeVO vo:inpCity){%>
                                                                
                                                                <option value='<%= vo.getCityId()%>' <%if(request.getAttribute("inpCityId")!=null &&  request.getAttribute("inpCityId").equals(vo.getCityId())){%> selected<%}else if (request.getAttribute("inpCityId").equals("") && vo.getIsdefault().equals("Y")){%> selected<%} %>  ><span><%= vo.getCityName()%></span></option>
                                                                <%}}%> --%>
                                                            </SELECT>
                                                        </TD>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.marialstatus",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" colspan=3>
                                                             <SELECT id="inpMarStat" name="inpMarStat" class="ComboKey Combo_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this);enableMarryField();">
                                                                  <option value='S' <%if(request.getAttribute("inpMarStat")!=null &&  request.getAttribute("inpMarStat").equals("S")){%> selected<%} %>><span> <%= Resource.getProperty("hcm.single",lang)%></span></option>
                                                                  <option value='M' <%if(request.getAttribute("inpMarStat")!=null &&  request.getAttribute("inpMarStat").equals("M")){%> selected<%} %>><span><%= Resource.getProperty("hcm.married",lang)%></span></option>
                                                                  <option value='Wdow' <%if( request.getAttribute("inpMarStat")!=null && request.getAttribute("inpMarStat").equals("Wdow")){%> selected<%} %> ><span><%= Resource.getProperty("hcm.widow",lang)%></span></option>
                                                                  <option value='Div' <%if( request.getAttribute("inpMarStat")!=null &&  request.getAttribute("inpMarStat").equals("Div")){%> selected<%} %>><span><%= Resource.getProperty("hcm.divorced",lang)%></span></option>
                                                             </SELECT>
                                                        </TD>
                                                    </TR>
                                                    
                                                    
                                                    <TR>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.townofbirth",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" >
                                                           <input type="text" name="inpTownBirth" id="inpTownBirth" maxlength="30" value="<%= request.getAttribute("inpTownBirth") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);"/>
                                                        </TD>
                                                        <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.nationality",lang)%></span></TD>
                                                        <TD class="TextBox_ContentCell" colspan=3>
                                                             <SELECT id="inpNat" name="inpNat" class="ComboKey Combo_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this);">
                                                               <%  if(inpNationalList!=null && inpNationalList.size()>0) { %>
                                                               <%--  <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option> --%>
                                                                <% for(EmployeeVO vo:inpNationalList){  %>
                                                                <option value='<%= vo.getNationalId()%>' <%if( request.getAttribute("inpNat")!=null && request.getAttribute("inpNat").equals(vo.getNationalId())){%> selected<%} else if (request.getAttribute("inpNat")==null && vo.getIsdefault().equals("Y")){%> selected<%} %>    ><span><%= vo.getNationalCode()%></span></option>
                                                                <%}}%>
                                                             </SELECT>
                                                        </TD>
                                                    </TR>
                                                    <TR>
                                                          <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.bloodtype",lang)%></span></TD>
                                                          <TD class="TextBox_ContentCell" >
                                                           <SELECT id="inpBloTy" name="inpBlodTy" class="Combo Combo_TwoCells_width Combo_focus" onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this);">
                                                                  <option value="0" <%if(request.getAttribute("inpBlodTy")!=null && request.getAttribute("inpBlodTy").equals("0")){%> selected<%} %> ><%= Resource.getProperty("hcm.select", lang) %></option>
                                                                <option value="O+" <%if(request.getAttribute("inpBlodTy")!=null && request.getAttribute("inpBlodTy").equals("O+")){%> selected<%} %> >O +ve</option>
                                                                <option value="O-" <%if( request.getAttribute("inpBlodTy")!=null && request.getAttribute("inpBlodTy").equals("O-")){%> selected<%} %> >O -ve</option>
                                                                <option value="A+" <%if(request.getAttribute("inpBlodTy")!=null && request.getAttribute("inpBlodTy").equals("A+")){%> selected<%} %> >A +ve</option>
                                                                <option value="A-" <%if( request.getAttribute("inpBlodTy")!=null && request.getAttribute("inpBlodTy").equals("A-")){%> selected<%} %> >A -ve</option>
                                                                <option value="B+" <%if( request.getAttribute("inpBlodTy")!=null && request.getAttribute("inpBlodTy").equals("B+")){%> selected<%} %>>B +ve</option>
                                                                <option value="B-" <%if( request.getAttribute("inpBlodTy")!=null && request.getAttribute("inpBlodTy").equals("B-")){%> selected<%} %> >B -ve</option>
                                                                <option value="AB+" <%if( request.getAttribute("inpBlodTy")!=null && request.getAttribute("inpBlodTy").equals("AB+")){%> selected<%} %>>AB +ve</option>
                                                                <option value="AB-" <%if( request.getAttribute("inpBlodTy")!=null && request.getAttribute("inpBlodTy").equals("AB-")){%> selected<%} %> >AB -ve</option>  
                                                           </SELECT>
                                                          </TD>
                                                          <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.religion",lang)%></span></TD>
                                                          <TD class="TextBox_ContentCell" colspan="3">
                                                            <SELECT id="inpRel" name="inpRel" class="ComboKey Combo_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this);">
                                                                <%  if(inpReligionList!=null && inpReligionList.size()>0) { %>
                                                                <%-- <option value="0"><%= Resource.getProperty("hcm.select", lang) %></option> --%>
                                                                <% for(EmployeeVO vo:inpReligionList){ %>
                                                                
                                                                <option value='<%= vo.getReligionId()%>' <%if(request.getAttribute("inpRelId")!=null && request.getAttribute("inpRelId").equals(vo.getReligionId())){%> selected<%} else if (request.getAttribute("inpRelId")==null && vo.getIsdefault().equals("Y")){%> selected<%} %> ><span><%= vo.getReligionCode()%></span></option>
                                                                <%}}%>
                                                            </SELECT>
                                                          </TD> 
                                                    </TR>  
                                                    <TR>
                                                      <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.weight",lang)%></span></TD>
                                                      <TD class="TextBox_ContentCell" >
                                                          <input type="text" name="inpWeight" id="inpWeight" maxlength="30" value="<%= request.getAttribute("inpWeight") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" />
                                                      </TD>
                                                      <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.height",lang)%></span></TD>
                                                      <TD class="TextBox_ContentCell" colspan=3>
                                                          <input type="text" name="inpHeight" id="inpHeight" maxlength="30" value="<%= request.getAttribute("inpHeight") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" />
                                                      </TD>
                                                    </TR>
                                                    </TABLE>
                                                    </DIV> 
                                                    </TD>
                                                    <TD>
                                             
                                                    
                                        <!-- Contact Information Field Group -->
                                                   <DIV id="contact">
                                                         <TABLE style="width:80%; margin-top: 10px;">
                                                         <TR>
                                                            <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.email",lang)%></span></TD>
                                                            <TD class="TextBox_ContentCell" colspan=5>
                                                               <input type="text" name="inpEmail" id="inpEmail" maxlength="30" value="<%= request.getAttribute("inpEmail") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" />

                                                                </TD>
                                                         </TR>
                                                          <TR>
                                                              <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.mobileno",lang)%></span></TD>
                                                              <TD class="TextBox_ContentCell"  colspan=5>
                                                               <input type="text" name="inpMobno" id="inpMobno" maxlength="30" value="<%= request.getAttribute("inpMobno") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" />
                                                              </TD>
                                                         </TR>
                                                         <TR>
                                                              <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.homeno",lang)%></span></TD>
                                                              <TD class="TextBox_ContentCell"  colspan=5>
                                                               <input type="text" name="inpHomeNo" id="inpHomeNo" maxlength="30" value="<%= request.getAttribute("inpHomeNo") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" />
                                                              </TD>
                                                         </TR>
                                                         <TR>
                                                              <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.workno",lang)%></span></TD>
                                                              <TD class="TextBox_ContentCell"  colspan=5>
                                                               <input type="text" name="inpWorkNo" id="inpWorkNo" maxlength="30" value="<%= request.getAttribute("inpWorkNo") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" />
                                                              </TD>
                                                         </TR>
                                                         <TR>
                                                             <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.office",lang)%></span></TD>
                                                             <TD class="TextBox_ContentCell"  colspan=5>
                                                               <input type="text" name="inpOff" id="inpOff" maxlength="30" value="<%= request.getAttribute("inpOff") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" />
                                                             </TD>
                                                         </TR>
                                                        <TR>
                                                             <TD class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.location",lang)%></span></TD>
                                                             <TD class="TextBox_ContentCell"  colspan=5>
                                                               <input type="text" name="inpLoc" id="inpLoc" maxlength="30" value="<%= request.getAttribute("inpLoc") %>" required="false" class="dojoValidateValid TextBox_TwoCells_width" onkeydown="return true;" onkeyup="enableForm(this);" />
                                                             </TD>
                                                        </TR> 
                                                         <TR>
                                                             <TD id="inplabelmary" class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.Married",lang)%></span></TD>
                                                            
                                                             
                                                             <TD class="TextBox_btn_ContentCell">
                                            <table border="0" cellspacing="0" cellpadding="0" summary=""  style="padding-top: 0px;">
                                                 <tr>
                                               <td class="TextBox_ContentCell">
                                                 <%  
                                                                    if(request.getAttribute("inpmary")!= null ) {%>
                                                                         <input type="text" id="inpmary" class="dojoValidateValid TextBox_btn_OneCell_width  " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this);"  value="<%= request.getAttribute("inpmary") %>" maxlength="10" name="inpmary" ></input> 
                                                                     <%}
                                                                      else{ %>
                                                                      <input type="text" id="inpmary" class="dojoValidateValid TextBox_btn_OneCell_width  " onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm(this); "  value="" maxlength="10" name="inpmary" ></input>
                                                                      <%} %> 
                                                   
                                                  </td>
                                              </tr>
                                             </table>
                                        </TD>
                                                        </TR>
                                                        
                                                        </TABLE>
                                                    </DIV> 
                                                    </TD>
                                                    </TR>
                                             </TABLE>
                                             </DIV> 
                                              <DIV id="button">
                                              <TABLE style="width:80%; margin-top: 10px;">
                                              
                                               <TR>
                                                   <TD colspan="6" align="center">
                                                       <div>
                                                           <BUTTON type="button" id="Submit_linkBTN" class="ButtonLink" onclick="" style="margin: 10px 0; display:none;">
                                                              <TABLE class="Button">
                                                                  <TR>
                                                                      <TD class="Button_left"><IMG class="Button_Icon Button_Icon_process" alt="Submit" title="Submit" src="../web/images/blank.gif" border="0"></IMG></TD>
                                                                      <TD class="Button_text" id="Submit_BTNname"><%= Resource.getProperty("hcm.employment",lang)%></TD>
                                                                      <TD class="Button_right"></TD>
                                                                  </TR>
                                                              </TABLE>
                                                          </BUTTON>
                                                          <BUTTON type="button" id="inpissueDecision" class="ButtonLink" onclick="IssueDecision();" style="margin: 10px 0;">
                                                              <TABLE class="Button">
                                                                  <TR>
                                                                      <TD class="Button_left"><IMG class="Button_Icon Button_Icon_process" alt="Submit" title="Submit" src="../web/images/blank.gif" border="0"></IMG></TD>
                                                                      <TD class="Button_text" id="Submit_BTNname"><%= Resource.getProperty("hcm.issuedecision",lang)%></TD>
                                                                      <TD class="Button_right"></TD>
                                                                  </TR>
                                                              </TABLE>
                                                          </BUTTON>
                                                      </div>
                                                  </TD>
                                               </TR>
                                              </TABLE>
                                              </DIV> 
                                             <!--  </div> -->
                                          </div>
                                      </div>
                                  </div>
                                </TD>
                                        </TR>
                                        </TABLE>
                                        </TD>
                                        </TR>
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
    
<script type="text/javascript">
var contextPath = '<%=request.getContextPath()%>', currentTab = 'EMP',
validsal='0', validPosition='1',
valStartDateFailed='0',validEmpFailed='0',validNatvalidFailed='0',validNatexistFailed='0',validEmpno='0',validEmpNatId='0' ;
<%  if(request.getAttribute("inpstatus")!=null && (request.getAttribute("inpstatus").equals("UP") || (request.getAttribute("inpstatus").equals("I") && request.getAttribute("inpEndDate")==""))){ %>

$('#CivilPicText').click(function(){
    $('#CivilPicFile').click();
});

$('#CivilPic').click(function(){
    $('#CivilPicFile').click();
});

$('#WorkPicText').click(function(){
    $('#WorkPicFile').click();
});

$('#WorkPic').click(function(){
    $('#WorkPicFile').click();
});
<%}%>
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

function onClickGridView() {
    hideMessage();
    document.getElementById("inpAction").value = "GridView";
    if (changesFlag == 1)
        OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
            if (result){
             if(checkValidData()){
                document.getElementById("SubmitType").value="SaveGrid";
               reloadTab(currentTab);
                }
           }
           else {
               reloadTab(currentTab);
           }
        });
    else
        reloadTab('EMP');
}

function onclickdownload(){
     if( document.getElementById('LinkButtonDownload').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Print_disabled')
        {
           return false;
        }
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
 function generateReport(){
     closepopup(); /*  after click on submit close the popup */
     submitCommandForm('DEFAULT', false, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.HiringDecisionReport/HiringDecisionReport?inpAction=Submit', 'background_target', null, false);  
     $("#inpIsjoiningWorkReq").prop('checked',false);  /* unchecked the checkbox */
 }
 function generateReportforcancel()
 {
     closepopupforcancel();
    <%--  $.post('<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.cancelHiringDecisionReport/CancelHiringDecisionReport', {
         inpAction : 'Submit'
         
     }); --%>
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





function reloadTabSave(tab) {
    if (checkValidData()) {
        document.getElementById("SubmitType").value = "Save";
        var url = "&inpNextTab=" + tab;
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.employee.header/Employee?' + url, '_self', null, true);
        return false;
    }
}
function checkValidData() {
    <%--  if($("#inpAgeMY").val() < 17){
        OBAlert('<%= Resource.getProperty("hcm.dateisgreater", lang) %>');
        document.getElementById("inpDoj").focus();
        return false;
    } --%>
   
      if(document.getElementById('inpMarStat').value=="M" && document.getElementById('inpmary').value==""){
        OBAlert('<%= Resource.getProperty("hcm.MarrieddateNotNull", lang) %>');
        validEmpno='1';
    }   
     if(document.getElementById("inpEmpNo").value==""){
         OBAlert('<%= Resource.getProperty("hcm.fill.employee.no", lang) %>');
         validEmpno='1';
     }
     if(document.getElementById("inpMcsLetterNo").value==""){
         OBAlert('<%= Resource.getProperty("hcm.fill.mcsletter.no", lang) %>');
         validEmpno='1';
     }
     if(document.getElementById("inpDecisionNo").value==""){
         OBAlert('<%= Resource.getProperty("hcm.fill.decision.no", lang) %>');
         validEmpno='1';
     }
     if(document.getElementById("inpNatIdf").value==""){
         OBAlert('<%= Resource.getProperty("hcm.fill.employee.nationalidentifier", lang) %>');
         validEmpNatId='1';
     }
     if(document.getElementById("inpCity").value=="")
     {
     OBAlert('<%= Resource.getProperty("hcm.selectCity", lang) %>');
     validcity='1';
     }
     else
         validcity='0';
     if(document.getElementById("inpSalutation").value=="0"){ 
     OBAlert('<%= Resource.getProperty("hcm.selectactiontype", lang) %>');
     validsal='1';
     } 
        else
         validsal='0'; 
      if(document.getElementById("inpstatus").value=='C'){
          
          $.ajax({
              type:'GET',
              url:"<%=request.getContextPath()%>/EmployeeAjax?action=getStartdate",            
              data:{inpEmpNo:document.getElementById("inpEmpNo").value},
              dataType:'xml',
              async:false,
              success:function(data)
              {       
                  $(data).find("ChkEmp").each(function()
                  {    
                      var exstartdate=$(this).find("result").text();
                      var startdate=document.getElementById("inpStartDate").value;
                     
                      if (OBCompareDate(startdate, exstartdate) == -1 || OBCompareDate(startdate, exstartdate) == 0) {
                          OBAlert('<%= Resource.getProperty("hcm.cancelstartdate>exstartdate",lang)%> (' + document.getElementById("inpStartDate").value + ')', function(r) {
                              showCalendar('frmMain.inpStartDate', document.frmMain.inpStartDate.value, false);
                          });
                          valStartDateFailed='1';
                      }
                      
                      else{
                          valStartDateFailed='0';
                      }
                  });             
              }
          });
     } 
      if(document.getElementById("inpstatus").value!='C'){
      if(document.getElementById("inpEmpNo").value!=''){
          $.ajax({
              type:'GET',
              url:"<%=request.getContextPath()%>/EmployeeAjax?action=CheckEmployeeNo",            
              data:{inpEmpNo:document.getElementById("inpEmpNo").value,inpEmpId:document.getElementById("inpEmployeeId").value,inpstatus: document.getElementById("inpstatus").value},
              dataType:'xml',
              async:false,
              success:function(data)
              {       
                  $(data).find("CheckEmployeeNo").each(function()
                  {    
                      var result=$(this).find("value").text();
                      if(result =='true'){
                          OBAlert('<%= Resource.getProperty("hcm.same.employee.exist", lang) %>');
                          validEmpFailed='1';
                      }
                      else{
                          validEmpFailed='0';
                      }
                  });             
              }
          });
     } 
      
       if (document.getElementById("inpNatIdf").value!=''){
         
         $.ajax({
             type:'GET',
             url:"<%=request.getContextPath()%>/EmployeeAjax?action=CheckNationalNum",            
             data:{inpNationalId:document.getElementById("inpNatIdf").value,inpEmployeeId:document.getElementById("inpEmployeeId").value},
             dataType:'xml',
             async:false,
             success:function(data)
             {       
                 $(data).find("ChkNatID").each(function()
                 {    
                     var result=$(this).find("Valid").text();
                     if(result!="true"){
                         validNatvalidFailed='1';
                         OBAlert('<%= Resource.getProperty("hcm.invalid.nataion.identify", lang) %>');
                     }
                     else{
                         validNatvalidFailed='0';
                     }
                 });             
             }
         });
       
     }
      if (document.getElementById("inpNatIdf").value!=''){
          
          $.ajax({
              type:'GET',
              url:"<%=request.getContextPath()%>/EmployeeAjax?action=CheckNationalNum",            
              data:{inpNationalId:document.getElementById("inpNatIdf").value,inpEmployeeId:document.getElementById("inpEmployeeId").value},
              dataType:'xml',
              async:false,
              success:function(data)
              {       
                  $(data).find("ChkNatID").each(function()
                  {    
                      var result=$(this).find("Exist").text();
                      if(result !='false'){
                          validNatexistFailed='1';
                          OBAlert('<%= Resource.getProperty("hcm.same.nationalid.exists", lang) %>');
                           return false;
                      }
                      else{
                          validNatexistFailed='0';
                      }
                  });             
              }
          });
        
      } 
      
      if(document.getElementById("inpDoj").value !=''){
          $.ajax({
              type:'GET',
              url:"<%=request.getContextPath()%>/EmployeeAjax?action=Checkdatevalidation",            
              data:{hiredate:document.getElementById("inpHireDate").value,dobdate:document.getElementById("inpDoj").value},
              dataType:'xml',
              async:false,
              success:function(data)
              {       
                  $(data).find("Checkdatevalidation").each(function()
                  {    
                      var result=$(this).find("value").text();
                      if(result =='false'){
                          validEmpFailed='1';
                      }
                      
                      else{
                          validEmpFailed='0';
                      }
                  });             
              }
           });
          
                     
                if(validEmpFailed=='1'){
                var a = document.getElementById("inpminage").value;
                var b= document.getElementById("inpmaxage").value;
                
                
                OBAlert("Employee age should be in between "+$('#inpminage').val()+" to "  +$('#inpmaxage').val()+" years ");
                return false;  
                }
      }
      
      }
     
     if(validNatexistFailed =='1' || validNatvalidFailed =='1' || validsal=='1'|| validEmpFailed =='1' || valStartDateFailed =='1' || validEmpno =='1' || validEmpNatId =='1' || validcity == '1'){
          return false; 
     }
        
     else{
         return true;
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
function enableForm(domObj) {
    if (domObj && $(domObj).attr('readonly')==='readonly') {
        return;
    }
    changesFlag = 1;
    hideMessage();
    document.getElementById("inpissueDecision").style.display="none"; 
    if(  ( document.getElementById('inpSalutation').value !="" ) &&   (document.getElementById('inpEmpCat').value !="")   &&  ( document.getElementById('inpStartDate').value !="")  &&
            (document.getElementById('inpHireDate').value !="" )&&  ( document.getElementById('inpMcsLetterNo').value !="" )  && (document.getElementById('inpTitle').value !="")  && 
            (document.getElementById('inpDecisionNo').value !="" )   &&   (document.getElementById('inpGen').value !="") &&  ( document.getElementById('inpAraFName').value !="")
            && (document.getElementById('inpAraFatName').value !="" ) && (document.getElementById('inpAraGraFatName').value !="" ) &&   ( document.getElementById('inpAraGraFatName').value !="" )
            && (document.getElementById('inpAraFamName').value !="") && (document.getElementById('inpNatIdf').value !="" )
            && (document.getElementById('inpEmpNo').value !="")  && (document.getElementById('inpDoj').value !="") && (document.getElementById('inpMarStat').value !="") 
            && (document.getElementById('inpCountry').value !="")  && (document.getElementById('inpCity').value !="") && (document.getElementById('inpRel').value !="") ){
        enableSaveButton("true");

    }
    else{
         enableSaveButton("false");
    }
    
    
        
}
$(function () {
    $(":file").change(function () {
        if(this.id=="CivilPicFile"){
        if (this.files && this.files[0]) {
            var reader = new FileReader();
            if(this.files[0].type.split("/")[1]!="jpeg" &&  this.files[0].type.split("/")[1]!="png" && this.files[0].type.split("/")[1]!="jpg"){
                OBAlert('<%= Resource.getProperty("hcm.valid.image.format", lang) %>');
            }
            else{
                 reader.onload = imageIsLoaded;
                 document.getElementById("inpcivfilename").value = this.files[0].name;
                 document.getElementById("inpcivfiletype").value = this.files[0].type;
                 document.getElementById("inpcivfilesize").value = this.files[0].size;
                 reader.readAsDataURL(this.files[0]);
            }
        }
        }
        else if (this.id=="WorkPicFile"){
            if (this.files && this.files[0]) {
                var reader = new FileReader();
                if(this.files[0].type.split("/")[1]!="jpeg" &&  this.files[0].type.split("/")[1]!="png" && this.files[0].type.split("/")[1]!="jpg"){
                    OBAlert('<%= Resource.getProperty("hcm.valid.image.format", lang) %>');
                }else{
                      reader.onload = imageIsLoadeds;
                      document.getElementById("inpwrkfilename").value = this.files[0].name;
                      document.getElementById("inpwrkfiletype").value = this.files[0].type;
                      document.getElementById("inpwrkfilesize").value = this.files[0].size;
                      reader.readAsDataURL(this.files[0]);
                }
              
            }
            }
       
    });
});

function imageIsLoaded(e) {
    $('#CivilPic').attr('src', e.target.result);
    document.getElementById("inpcivfilebyte").value = e.target.result;
    document.getElementById("CivilPic").style.display="";
    document.getElementById("CivilPicText").style.display="none";

};
function imageIsLoadeds(e) {
    $('#WorkPic').attr('src', e.target.result);
    document.getElementById("inpwrkfilebyte").value = e.target.result;
    document.getElementById("WorkPic").style.display="";
    document.getElementById("WorkPicText").style.display="none";

};
function onClickNew() {
    hideMessage();
    document.getElementById("inpAction").value = "EditView";
    if (changesFlag == 1) {
        OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
             if (result){
                 if(checkValidData()){
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
        document.getElementById("inpEmployeeId").value = "";
        reloadTab('EMP');
    }
}
function onClickRefresh() {
    hideMessage();
    document.getElementById("inpAction").value = "EditView";
    if (changesFlag == 1)
        OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
            if (result){
                if(checkValidData()){
                document.getElementById("SubmitType").value="Save";
               reloadTab(currentTab);
                }
           }
           else {
               reloadTab(currentTab);
           }
        });
    else
        reloadTab('EMP');
}

function onClickSave(index, type) {
    if (index.className != 'Main_ToolBar_Button')
        return false;
    if (changesFlag == 1 && checkValidData()) {
        if (type == "Grid") {
            document.getElementById("inpAction").value = "GridView";
            document.getElementById("SubmitType").value = "SaveGrid";
        }
        if (type == "New") {
            document.getElementById("inpAction").value = "EditView";
            document.getElementById("SubmitType").value = "SaveNew";
        }
        if (type == "Save") {
            var bit = $('#CivilPic').attr('src');
            document.getElementById("inpStatus").value = $("#inpstatus").find('option:selected').val();
            document.getElementById("inpAction").value = "EditView";
            document.getElementById("SubmitType").value = "Save";
            
        }
        reloadTab('EMP');
    }
}

function onClickDelete() {
    if (document.getElementById("inpEmployeeId").value != "") {
        OBAsk('<%= Resource.getProperty("hcm.confirmdelete", lang) %>', function(result) {
            if (result) {
                hideMessage();
                document.getElementById("inpAction").value = "GridView";
                document.getElementById("SubmitType").value = "Delete";
                reloadTab('EMP');
            }
        });
    }
}
function hideMessage() {
    $("#messageBoxID").hide();
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
    if (checkValidData()) {
        document.getElementById("SubmitType").value = "Save";
        var url = "&inpNextTab=" + tab;
        document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView'+url;
        document.frmMain.submit();
    }
}

function reloadTab(tab) {
    
     var employeeId ="";
     if(document.getElementById("inpstatus").value == 'C' || document.getElementById("inpstatus").value == 'TE')
     {
       employeeId=document.getElementById("inpExEmployeeId").value;
     }
     else
      {
        employeeId=document.getElementById("inpEmployeeId").value;
      }
    if (tab == 'EMP') {
           inpSalutation= document.getElementById("inpSalutation").value;
          var  url ="&inpEmpCat=" +  document.getElementById("inpEmpCat").value;
          submitCommandForm('DEFAULT', true, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employee.header/Employee?&inpSalutation='+inpSalutation+'&'+url, '_self', null, true);
         return false;
        
    }
    else if (tab == 'EMPINF') {
          submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
         return false;
        
      
    }
    else if (tab=='EMPADD') {
          submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView&inpEmployeeId='+employeeId, '_self', null, true);
          return false;
            
       }
   else if (tab == 'Dependent') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
        return false;
    }
   else if (tab == 'EMPCTRCT') {
       var url="";
         document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
         document.frmMain.submit();
    }
    else if (tab == 'EMPQUAL') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
        return false;
  
    }
    else if(tab == 'Asset'){
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
        return false;
    }
    else if (tab == 'PREEMP') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
         return false;
    }
    else if (tab == 'DOC') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.documents.header/Documents?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
         return false;
    }
    else if (tab == 'PERPAYMETHOD') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.header/PersonalPaymentMethod?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
         return false;
    }
    else if (tab == 'MEDIN') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.MedicalInsurance.header/MedicalInsurance?inpAction=GridView&inpEmployeeId='+employeeId, '_self', null, true);
         return false;
    }
}
function changesal(index) {
    if (index.value.length == 32) {
        $.post('<%=request.getContextPath()%>/EmployeeAjax', {
            action : 'GetPersonActiveType',
            inpActiveTypeId : index.value
        }, function(data) {
            var NAME = data.getElementsByTagName("NAME")[0].childNodes[0].nodeValue;
            if(data.getElementsByTagName("PERTYPE")[0].childNodes[0]!=null)
            var PERTYPE = data.getElementsByTagName("PERTYPE")[0].childNodes[0].nodeValue;
            else
                 var PERTYPE ="";
          document.getElementById("inpActionType").value=NAME;
          document.getElementById("inpPersonType").value=PERTYPE;
        });
    }
}

function enableMarryField(){
    if(document.getElementById('inpMarStat').value=="M"){
        document.getElementById("inpmary").style.display="";
        document.getElementById("inplabelmary").style.display="";
        $('.inpMaryImage').show();
         
    }
    else{
        document.getElementById("inpmary").style.display="none";
        document.getElementById("inplabelmary").style.display="none";
        $('.inpMaryImage').hide();
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

function IssueDecision(){
    OBConfirm('<%= Resource.getProperty("hcm.confirm.issue", lang) %>',
            function (result){
                if(result ){
                     if(document.getElementById("inpstatus").value!='C' && (document.getElementById("inpSalText").value=='HE' || document.getElementById("inpSalText").value=='HSP' || document.getElementById("inpSalText").value=='HC' ||document.getElementById("inpSalText").value=='HA'||document.getElementById("inpSalText").value=='HP')){
                    $.ajax({
                        type:'GET',
                        url:"<%=request.getContextPath()%>/EmployeeAjax?action=CheckBusinessPartner",            
                        data:{inpSalText:document.getElementById("inpSalText").value,inpEmployeeId:document.getElementById("inpEmployeeId").value},
                        dataType:'xml',
                        async:false,
                        success:function(data)
                        {       
                            $(data).find("CheckBusinessPartner").each(function()
                            {   
                                var result=$(this).find("result").text();
                                var Exist=$(this).find("Exist").text();
                             
                                 if(result =='false'){
                                    validBpartner='1';
                                }
                                 else
                                     {
                                     validBpartner='0';
                                     }
                                 if(Exist =='false'){
                                     validBpartnerValue='1';
                                 }
                                  else
                                      {
                                      validBpartnerValue='0';
                                      }
                               
                                
                            });             
                        }
                         
                     });
                     
                     if(validBpartner=='1'){
                         OBAlert('<%= Resource.getProperty("hcm.employee.Bpartnercategory", lang) %>');
                         return false;
                        }
                     else if(validBpartnerValue=='1')
                         {
                           OBAlert('<%= Resource.getProperty("hcm.employee.BpartnerValueExist", lang) %>');
                         return false;
                         }
                     

                     $.ajax({
                         type:'GET',
                         url:"<%=request.getContextPath()%>/EmploymentAjax?action=CheckPosAvailableOrNot",            
                         data:{inpEmployeeId:document.getElementById("inpEmployeeId").value},
                         dataType:'xml',
                         async:false,
                         success:function(data)
                         {       
                             $(data).find("CheckPosAvailableOrNot").each(function()
                             {    
                                 var result=$(this).find("result").text();
                                 if(result =='true'){
                                     validPosition='0';
                                 }
                                 
                                 else{
                                     validPosition='1';
                                 }
                             });             
                         }
                      });
                     if(validPosition=='0'){
                         OBAlert('<%= Resource.getProperty("hcm.position.not.available", lang) %>');
                         return false;
                        }
                     }
                   
                     $.ajax({
                         type:'GET',
                         url:"<%=request.getContextPath()%>/EmployeeAjax?action=Checkdatevalidation",            
                         data:{hiredate:document.getElementById("inpHireDate").value,dobdate:document.getElementById("inpDoj").value},
                         dataType:'xml',
                         async:false,
                         success:function(data)
                         {       
                             $(data).find("Checkdatevalidation").each(function()
                             {    
                                 var result=$(this).find("value").text();
                                 if(result =='false'){
                                     validEmpFailed='1';
                                 }
                                 
                                 else{
                                     validEmpFailed='0';
                                 }
                             });             
                         }
                      });
                
    if(validEmpFailed=='1'){
        var a = document.getElementById("inpminage").value;
        var b= document.getElementById("inpmaxage").value;
        
        OBAlert("Employee age should be inbetween "+$('#inpminage').val()+" to "  +$('#inpmaxage').val()+" years ");
         return false;  
    }
    else{
        if(document.getElementById("inpstatus").value!='C'){
            
         $.post('<%=request.getContextPath()%>/EmployeeAjax', {
             action : 'checkRecEmpment',
             inpEmployeeId : document.getElementById("inpEmployeeId").value
         }, function(data) {
             var result = data.getElementsByTagName("result")[0].childNodes[0].nodeValue;

             if(result=='true'){
                 document.getElementById("inpAction").value="IssueDecision";
                    document.getElementById("SubmitType").value="Save";
                    var submitType= document.getElementById("SubmitType").value;
                    var url ="inpEmployeeId=" + document.getElementById("inpEmployeeId").value;
                    var url ="&inpSalutation=" + document.getElementById("inpSalutation").value;

                    document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employee.header/Employee?&SubmitType='+submitType+'&'+url;
                    document.frmMain.submit();
                    displayMessage("S", '<%=Resource.getProperty("hcm.success",lang)%>', '<%= Resource.getProperty("hcm.deletesuccess", lang) %>');
             }
             else{

                 OBAlert('<%= Resource.getProperty("hcm.fill.employment.detail", lang) %>');
                return false;
             }
             
         });
    }
       
        else
        {
            
        document.getElementById("inpAction").value="IssueDecision";
         document.getElementById("SubmitType").value="Save";
        var submitType= document.getElementById("SubmitType").value; 
        // var url = "inpEmployeeId=" + document.getElementById("inpEmployeeId").value;
         var  url ="&inpSalutation=" +  document.getElementById("inpSalutation").value;
          url +="&inpEmpCat=" +  document.getElementById("inpEmpCat").value;

         document.frmMain.action = '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.employee.header/Employee?&SubmitType='+submitType+'&'+url;
         document.frmMain.submit();
         displayMessage("S", '<%=Resource.getProperty("hcm.success",lang)%>', '<%= Resource.getProperty("hcm.deletesuccess", lang) %>');
                    }
    }
    }
    });
    }
function isNumberKey(evt) {
    var charCode = (evt.which) ? evt.which : event.keyCode;
    if ((charCode < 48 || charCode > 57))
        return false;

    return true;
   }

//country
/* 
$( "#inpCountry").select2(selectBoxAjaxPaging({size: "small",
    placeholder: true})); */

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
 
$( document ).ready(function() {
    if($('#inpstatus').val()=="I"){
    $("#inpCountry").prop("disabled", true);
    $("#inpCity").prop("disabled", true);
    $("#inpNat").prop("disabled", true);
    $("#inpRel").prop("disabled", true);
    document.getElementById("inpNatIdf").setAttribute("readonly", "true");
    document.getElementById("inpEmpNo").setAttribute("readonly", "true");
    }
});
</script>
</HTML>