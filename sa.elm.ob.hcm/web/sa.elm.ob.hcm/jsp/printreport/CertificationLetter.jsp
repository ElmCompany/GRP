 <%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
 <%@page import="sa.elm.ob.hcm.ad_reports.certificationletter.CertificationLetter"%>
 <%@ page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp"%>
 <%@ page import="java.util.List,java.util.ArrayList" errorPage="/web/jsp/ErrorPage.jsp" %>
 <%     
    String lang = ((String)session.getAttribute("#AD_LANGUAGE"));
     String style="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css";
     if(lang.equals("ar_SA")){
         style="../web/skins/rtl/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP_250.css";
     }
 %>
<HTML>
<HEAD>
 <META http-equiv="Content-Type" content="text/html; charset=utf-8"></META>
    <TITLE><%=Resource.getProperty("hcm.printreport.title",lang)%></TITLE>
    <LINK rel="stylesheet" type="text/css" href="<%=style %>" id="paramCSS"></LINK>  
      <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/ui.jqgrid.css"></link>
    <script type="text/javascript" id="paramDirectory">var baseDirectory = "../web/";</script> 
    <script type="text/javascript" id="paramLanguage">var defaultLang="en_US";</script>
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
    <style type="text/css">
    #client
    {
        height:auto !important;
    }
    </style>
    <script type="text/javascript">
    function onLoadDo()
    {               
        var inpWindowID = "<%= request.getAttribute("inpWindowID")%>";
        if(inpWindowID=="DCC32BEEF53841FF9A5B1F1585156930"){
            $('#certificate_report').show();
        }
    }
    function onResizeDo()
    {
        //resizePopup();
    }
    
    </script>
      
</HEAD>
<BODY leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="onLoadDo();" onresize="onResizeDo();" onfocus="window.close();" id="paramBody">
<FORM name="frmMain" method="post" action="">
  <INPUT type="hidden" name="Command"></INPUT>  
  <INPUT type="hidden" name="action" id="action" value=""></INPUT>
    
  <INPUT type="hidden" name="inpTabId" id=inpTabId value="<%= request.getAttribute("inpTabId")%>"></INPUT>
  <INPUT type="hidden" name="inpWindowID" id="inpWindowID" value="<%=request.getAttribute("inpWindowID")%>"></INPUT>
  <INPUT type="hidden" name="inpRecordId" id=inpRecordId value="<%= request.getAttribute("inpRecordId")%>"></INPUT>
       
 <TABLE cellspacing="0" cellpadding="0" width="100%" id="table_header">
  <TR>
    <TD>
      <TABLE cellspacing="0" cellpadding="0" class="Popup_ContentPane_NavBar" id="tdToolBar">
        <TR class="Popup_NavBar_bg"><TD></TD>
          <TD class="Popup_NavBar_separator_cell"></TD>
          <TD class="Popup_NavBar_Popup_title_cell"><SPAN><%=Resource.getProperty("hcm.printreport.title",lang)%></SPAN></TD>
          <TD class="Popup_NavBar_separator_cell"></TD>
        </TR>
      </TABLE>
    </TD>
    <div class="Popup_ContentPane_CircleLogo">
      <div class="Popup_WindowLogo">
        <img class="Popup_WindowLogo_Icon Popup_WindowLogo_Icon_process" src="../web/images/blank.gif" border="0/">
      </div>
    </div>
  </TR>
  <TR>
    <TD>
      <TABLE cellspacing="0" cellpadding="0" class="Popup_ContentPane_SeparatorBar" id="tdtopTabs">
        <TR>
          <TD class="Popup_SeparatorBar_bg"></TD>
        </TR>
      </TABLE>
    </TD>
  </TR>
  <tr>
  <td>
     <table cellspacing="0" cellpadding="0" class="Popup_ContentPane_InfoBar">
        <tbody><tr>
          <td class="Popup_InfoBar_Icon_cell"><img src="../web/images/blank.gif" border="0" class="Popup_InfoBar_Icon_info"></td>
          <td class="Popup_InfoBar_text_table">
            <table>
              <tbody><tr>
                <td class="Popup_InfoBar_text" id="processHelp"><%=Resource.getProperty("hcm.printreport.title",lang)%> </td>
              </tr>
            </tbody></table>
          </td>
        </tr>
      </tbody></table>
    </td>
</tr>
</TABLE>

    <TABLE cellspacing="0" cellpadding="0" width="100%">
      <TR>
        <TD>
          <DIV class="Popup_ContentPane_Client" style="overflow: auto;" id="client">               
          <TABLE cellspacing="0" cellpadding="0" class="Popup_Client_TablePopup">           
            <TR>
            <td>    
            <table>   
            <tr><td></td></tr>
            <tr id="certificate_report">
           <td colspan="2"><table>
                <tr id="certificate_report_select">
                    <TD class="TitleCell" align="left"><SPAN class="LabelText"> <%=Resource.getProperty("hcm.certificationletter.report",lang)%></SPAN></TD>
                    <TD class="TextBox_btn_ContentCell TextBox_ReadOnly_ContentCell">
                        <TABLE border="0" cellpadding="0" cellspacing="0"><TR><TD class="TextBox_ReadOnly_ContentCell">
                        <SELECT name="inpCertReport" class="ComboKey Combo_TwoCells_width" id="inpCertReport" >
                            <option value="0"><%= Resource.getProperty("hcm.--select--", lang) %></option>
                            <option value="CLWTOUTSL"><%= Resource.getProperty("hcm.certificationletter.withoutsalary", lang) %></option>
                            <option value="CLWTDTSL"><%= Resource.getProperty("hcm.certificationletter.withdetailedsalary", lang) %></option>
                            <option value="CLWTTOTSL"><%= Resource.getProperty("hcm.certificationletter.withtotalsalary", lang) %></option>
                            <option value="CLWTTOTSL_AE"><%= Resource.getProperty("hcm.certificationletter.withtotalsalaryarabeng", lang) %></option>                                                      
                        </SELECT>
                        </TD></TR></TABLE>
                    </TD>
                </tr>
                </table>
                </td>
                </tr>
                <tr><td>&nbsp;</td></tr>
            <TR> 
              <TD class="Button_CenterAlign_ContentCell"  id="Submit_inp_td" colspan="4">
              <DIV id="newDiscard">
              <DIV id="Submit_inp">           
                 <BUTTON type="button" id="Submit_linkBTN" class="ButtonLink" onclick="GenerateReport();">
                 <TABLE class="Button">
                   <TR>
                     <TD class="Button_left"><IMG class="Button_Icon Button_Icon_process" alt="Submit" title="Submit" src="../web/images/blank.gif" border="0"></IMG></TD>
                     <TD class="Button_text" id="Submit_BTNname"><%= Resource.getProperty("hcm.printreport.submit",lang)%></TD>
                     <TD class="Button_right"></TD>          
                   </TR>
                 </TABLE>
               </BUTTON>
             </DIV>
             </DIV>
             </TD>
           </TR> 
            </table>                    
              </td></TR>
            </TABLE>        
          </DIV>
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
<script type="text/javascript">
function GenerateReport(){
    $("#action").val("DownloadReport");          
    if($('#inpWindowID').val()=='DCC32BEEF53841FF9A5B1F1585156930')  {   
    	var certReport = $("#inpCertReport").val();
        if($("#inpCertReport").val()=='0'){
             OBAlert("<%= Resource.getProperty("hcm.printreport.pleaseselectonereport",lang)%>");
             return false;
         }
        submitCommandForm('DEFAULT', false, null, '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_reports.printreport/PrintReport?report='+certReport, 'background_target', null, false);        
    }    
    closePage();
    return true;
}
</script>
</HTML>
