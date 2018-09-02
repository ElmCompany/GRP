<%@page import="sa.elm.ob.hcm.ad_forms.holidaycalendar.vo.HolidayCalendarVO"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="sa.elm.ob.hcm.properties.Resource" errorPage="/web/jsp/ErrorPage.jsp" %>
<%@page import="sa.elm.ob.utility.util.Utility"%>
<%@ page import="java.util.List, java.util.ArrayList,java.util.Date, java.lang.Integer, java.util.StringTokenizer" errorPage="/web/jsp/ErrorPage.jsp" %>
<%
    String lang = ((String)session.getAttribute("#AD_LANGUAGE"));
	List<HolidayCalendarVO> holidayList = (ArrayList<HolidayCalendarVO>) request.getAttribute("HolidayList");
    String style="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css";
    String toolBarStyle="../web/js/common/CommonFormLtr.css";
    if(lang.equals("ar_SA")){
     style="../web/skins/rtl/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP_250.css";
    toolBarStyle="../web/js/common/CommonFormRtl.css";
    }
    %>
<html xmlns:="http://www.w3.org/1999/xhtml">
<head>
<META http-equiv="Content-Type" content="text/html; charset=utf-8"></META>
<LINK rel="stylesheet" type="text/css" href="<%=style %>" id="paramCSS"></LINK>
   <link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>"></link>
    <link rel="stylesheet" type="text/css" href="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.css"></link>
    <link rel="stylesheet" type="text/css" href="../web/js/jquery/jquery-ui-1.8.11.custom.css"></link>
     <link rel="stylesheet" type="text/css" href="../web/js/common/select2.min.css"></link>
    
 <style type="text/css">
 
 .calendars-month,.calendars-month-header{
 width:  20em !important;
 }
  .calendars-dow-0{
  width:2.5em !important;
  }
  .calendars-dow-1{
  width:2.5em !important;
  }
  .calendars-dow-2{
  width:2.5em !important;
  }
  .calendars-dow-3{
  width:2.5em !important;
  }
  .calendars-dow-4{
  width:2.5em !important;
  }
  .calendars-dow-5{
  width:2.5em !important;
  }
   .calendars-dow-6{
  width:2.5em !important;
  }
     
.calendars-cmd-next,.calendars-cmd-prev,.calendars-cmd-today { visibility: hidden; }
	.Checkbox_focused, .Radio_focused { border: none !important; outline: none !important; }
	.WE1_day { background-color: #23d329 !important; }
	.WE10_day { background-color: #23d329 !important; }
	.WE11_day { background-color: #23d329 !important; }
	.WE12_day { background-color: #23d329 !important; }
	.WE13_day { background-color: #23d329 !important; }
	.WE14_day { background-color: #23d329 !important; }
	.WE15_day { background-color: #23d329 !important; }
	.WE16_day { background-color: #23d329 !important; }
	.WE2_day { background-color: #FFDD00  !important; }
	.WE27_day { background-color: #FFDD00  !important; }
	.WE28_day { background-color: #FFDD00  !important; }
	.WE29_day { background-color: #FFDD00  !important; }
	.WE210_day { background-color: #FFDD00  !important; }
	.WE211_day { background-color: #FFDD00  !important; }
	.WE212_day { background-color: #FFDD00  !important; }
	.WE213_day { background-color: #FFDD00  !important; }
	.NH_day {background-color: #f44245 !important;}
	.AD_day {background-color: #0000FF !important;}
	.FE_day {background-color: #e437e8 !important;}
	div.weekEnd1 { height:12px; width:18px; border:1px solid #A7ABB4; background-color: #23d329; }
	div.weekEnd2 { height:12px; width:18px; border:1px solid #A7ABB4; background-color: #FFDD00 ; margin-top: 2px; }
	div.feterholiday { height:12px; width:18px; border:1px solid #A7ABB4; background-color: #e437e8; margin-top: 2px; }
		div.adhaholiday { height:12px; width:18px; border:1px solid #A7ABB4; background-color: #0000FF; margin-top: 2px; }
		div.nationalHoliday { height:12px; width:18px; border:1px solid #A7ABB4; background-color: #f44245; margin-top: 2px; }
		.LabelTextFont { font-size: 13px; }
	
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
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.plus.js"></script>
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.js"></script> 
    <script type="text/javascript" src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/jquery.calendars.picker.ext.js"></script>
<script type="text/javascript">
function validate(action) {
	return true;
}
function onLoadDo() {
	
	  
    
   
	this.windowTables = new Array(new windowTableId('client', 'buttonOK'));
	setWindowTableParentElement();
	this.tabsTables = new Array(new tabTableId('tdtopTabs'));
	setTabTableParentElement();
	setBrowserAutoComplete(false);
	setFocusFirstControl(null, 'inpMailTemplate');
	updateMenuIcon('buttonMenu');
	resizeArea();
	onLoad();
	/*  $('#HolidayCalendar').calendarsPicker({calendar:  !important; background-color: #23d329;
	        $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect : function(dates){enableForm();} ,showTrigger:  
	    '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'}); */
	 
}
function onResizeDo() {
	resizeArea();
}
function onLoad() {
	changeFlag=0;
	document.getElementById("SaveButton").style.display = "none";
	document.getElementById("inpYear").value = "<%=request.getAttribute("CurrentYear")%>"
	onChangeYear(document.getElementById("inpYear").value);

}
</script>
</head>
<body onload="onLoadDo();" onresize="onResizeDo();" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" id="paramBody">
<form method="post" action="" name="frmMain" id="frmMain">
<input type="hidden" name="Command"></input>
<input type="hidden" name="IsPopUpCall" value="1"></input>
<input type="hidden" name="inpLastFieldChanged"></input>
<input type="hidden" name="inpHolidayDate" id="inpHolidayDate" value=""></input>
<input type="hidden" name="inpLeaveType" id="inpLeaveType" value=""></input>
<input type="hidden" name="inpWeekend" id="inpWeekend" value=""></input>
<input type="hidden" name="inpCompanyId" id="inpCompanyId" value=""></input>
<input type="hidden" name="inpCompanyLocId" id="inpCompanyLocId" value=""></input>
<input type="hidden" name="inpCalendarId" id="inpCalendarId" value=""></input>
<input type="hidden" name="inpCalendarNameId" id="inpCalendarNameId" value=""></input>
<input type="hidden" name="inpCalendarName" id="inpCalendarName" value=""></input>
<input type="hidden" name="inpDownloadSheetType" id="inpDownloadSheetType" value="S"></input>
<input type="hidden" name="inpOverwriteHolidays" id="inpOverwriteHolidays" value="N"></input>
<input type="hidden" name="act" id="act" value=""></input>
<jsp:include page="/web/jsp/ProcessBar.jsp"/>
<div id="DivBlackOverlay" style="display: none; position: absolute; z-index: 50000; width: 100%; height: 100%; top: 0px; left: 0px; background-color: black; opacity: 0.5;"></div>
<div id="DivHolidayDialog" style="display: none; width: 520px; height: 200px; background: white; z-index: 50001; border: 2px solid #ff9c30; box-shadow: 0 0 20px #333; border-radius: 5px; position: absolute; margin: auto; top: 0; right: 0; bottom: 0; left: 0;">
	<table cellspacing="7">
	<tr><td><br/></td></tr>
	<tr>
		<td align="right" style="min-width: 80px;"><span class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.date", lang)%>&nbsp;:&nbsp;</span></td>
		<td><span class="LabelText LabelTextFont" id="lblDialogDate"></span></td>
	</tr>
	<tr>
		<td align="right"><span class="LabelText LabelTextFont"><%= Resource.getProperty("hcm.holidaycalendar.type", lang)%>&nbsp;:&nbsp;</span></td>
		<td>
	


			 <input type="radio" id="inpWeekend1"   class='radio-button'  value="inpWeekend1" name="inpHoliday" >
            <label for="inpWeekend1" class="LabelText LabelTextFont radio-label"><%= Resource.getProperty("hcm.holidaycalendar.weekend1", lang)%></label>&nbsp;&nbsp;
            <input type="radio" id="inpWeekend2"   class='radio-button'  value="inpWeekend2" name="inpHoliday"  >
            <label for="inpWeekend2" class="LabelText LabelTextFont radio-label"><%= Resource.getProperty("hcm.holidaycalendar.weekend2", lang)%></label> 
            <input type="radio" id="inpHoliday" class='radio-button'   value="inpHoliday" name="inpHoliday" >
            <label for="inpHoliday" class="LabelText LabelTextFont radio-label"><%= Resource.getProperty("hcm.holidaycalendar.holiday", lang)%></label>
            
		</td>
	</tr>
	<%-- <tr id='TRLeaveReason'>
		<td align="right"><span class="LabelText LabelTextFont"><%= Resource.getProperty("hcm.holidaycalendar.reason", lang)%>&nbsp;:&nbsp;</span></td>
		<td><textarea id="inpReason" name="inpReason" maxlength="254" class="dojoValidateValid required" style="width: 350px; resize: none; color: #333333; font-family: arial,sans-serif; font-size: 12px; height: 50px; padding: 1px; padding-left: 3px;"></textarea></td>
	</tr> --%>
	 <tr id="inpHolidayList" style="display:none;">
                                                            
          <td class="TitleCell" align="right" style="min-width:110px;"><span class="LabelText" ><%= Resource.getProperty("hcm.holidaycalendar.holiday", lang) %></span></td>
     <td class="TextBox_ContentCell">
          <select id="inpHolidaylist" name="inpHolidaylist" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';">
                 <option value="0"><%= Resource.getProperty("hcm.select", lang) %> </option>
               <%  if(holidayList!=null && holidayList.size()>0) { %>
                     <% for(HolidayCalendarVO vo:holidayList){ %>
                     <option value='<%= vo.getHolidaylistValue()%>' ><span><%= vo.getHolidaylistName()%></span></option>
                     <%}}%>
          </select>
     </td>
     </tr>
	<tr>
		<td></td>
		<td><div id="errMsgHolidayDialog" style="color:blue; font-size:9pt; display: none;"><p><img src="../web/images/warning.jpg" width="12" height="12"></img>&nbsp;<span id="errMsgHolidayDialogTxt"></span></p></div></td>
	</tr>
	</table>
	<div class="ui-dialog-buttonset" style="float: right; margin-right: 20px;">
		<button type="button" class="ui-button ui-corner-all ui-button-text-only" role="button" style="background: url(&quot;../web/Grid/Modern/backgroundHeader.png&quot;) repeat-x scroll 50% 100% transparent; font-size: 12px; font-weight: normal; border: 1px solid rgb(211, 211, 211); color: rgb(85, 85, 85); font-family: Verdana,Arial,sans-serif;"
			onmouseover="this.style.border='1px solid #999999';this.style.color='#212121';" onmouseout="this.style.border='1px solid #D3D3D3';this.style.color='#555555';" onclick='onClosePopup();'>
			<span class="ui-button-text" style="padding: 0.35em 1em;"><%= Resource.getProperty("hcm.cancel", lang)%></span></button>
		&nbsp;
		<button type="button" class="ui-button ui-corner-all ui-button-text-only" role="button" style="background: url(&quot;../web/Grid/Modern/backgroundHeader.png&quot;) repeat-x scroll 50% 100% transparent; font-size: 12px; font-weight: normal; border: 1px solid rgb(211, 211, 211); color: rgb(85, 85, 85); font-family: Verdana,Arial,sans-serif;"
		onmouseover="this.style.border='1px solid #999999';this.style.color='#212121';" onmouseout="this.style.border='1px solid #D3D3D3';this.style.color='#555555';" onclick="setHoliday();">
			<span class="ui-button-text" style="padding: 0.35em 1.5em;"><%= Resource.getProperty("hcm.ok", lang)%></span></button>
	</div>
</div>
<div id="DiVDownloadDialog" style="display: none; width: 500px; height: 200px; background: white; z-index: 50001; border: 2px solid #ff9c30; box-shadow: 0 0 20px #333; border-radius: 5px; position: absolute; margin: auto; top: 0; right: 0; bottom: 0; left: 0;">
    <img style="float: right; cursor: pointer; margin: 4px 4px; width: 11px; height: 11px;" onclick="onClosePopup()" src="../web/images/Cross.jpg"></img>
    <div align="center" style="margin-top: 10px;"><span class="LabelText" style="font-size: 14px;"><%= Resource.getProperty("hcm.holidaycalendar.download.title",lang)%></span></div>
    <div id="DivUploadBlock" align="center" style="width: 100%; padding-top: 30px;">
		<table style="margin: 0 auto;">
            <tr class="TRDownloadFile">
				<td class="TitleCell" align="right" style="min-width: 150px;"><span class="LabelText"><%= Resource.getProperty("hr.type", lang)%></span></td>
                <td class="TextBox_ContentCell" style="min-width: 600px; padding-top: 7px;">
					<p style="float: left;">
						<input type="radio" id="inpDownloadType_S" name="inpDownloadType_S" checked="checked" onclick="onClickDownloadSheet('S');"></input>
						<label for="inpDownloadType_S" class="LabelText" style="vertical-align: top; cursor: pointer;"><%= Resource.getProperty("hcm.holidaycalendar.download.sample", lang)%></label>&nbsp;&nbsp;
						<input type="radio" id="inpDownloadType_D" name="inpDownloadType_S" onclick="onClickDownloadSheet('D');"></input>
						<label for="inpDownloadType_D" class="LabelText" style="vertical-align: top; cursor: pointer;"><%= Resource.getProperty("hcm.holidaycalendar.download.data", lang)%></label>&nbsp;&nbsp;
					</p>
				</td>
            </tr>
         </table>
    </div>
	<div align="center" style="margin-top: 10px;">
		<button type="button" class="ui-button ui-corner-all ui-button-text-only" role="button" style="background: url(&quot;../web/Grid/Modern/backgroundHeader.png&quot;) repeat-x scroll 50% 100% transparent; font-size: 12px; font-weight: normal; border: 1px solid rgb(211, 211, 211); color: rgb(85, 85, 85); font-family: Verdana,Arial,sans-serif;"
        onmouseover="this.style.border='1px solid #999999';this.style.color='#212121';" onmouseout="this.style.border='1px solid #D3D3D3';this.style.color='#555555';" onclick="downloadHolidaySheet();">
            <span class="ui-button-text" style="padding: 0.35em 1.5em;"><%= Resource.getProperty("hr.download", lang)%></span></button>
        &nbsp;&nbsp;
        <button type="button" class="ui-button ui-corner-all ui-button-text-only" role="button" style="background: url(&quot;../web/Grid/Modern/backgroundHeader.png&quot;) repeat-x scroll 50% 100% transparent; font-size: 12px; font-weight: normal; border: 1px solid rgb(211, 211, 211); color: rgb(85, 85, 85); font-family: Verdana,Arial,sans-serif;"
            onmouseover="this.style.border='1px solid #999999';this.style.color='#212121';" onmouseout="this.style.border='1px solid #D3D3D3';this.style.color='#555555';" 
            onclick='onClosePopup();'>
            <span class="ui-button-text" style="padding: 0.35em 1em;"><%= Resource.getProperty("hr.close", lang)%></span></button>
    </div>
</div>
<div id="DiVUploadDialog" style="display: none; width: 500px; height: 200px; background: white; z-index: 50001; border: 2px solid #ff9c30; box-shadow: 0 0 20px #333; border-radius: 5px; position: absolute; margin: auto; top: 0; right: 0; bottom: 0; left: 0;">
    <img style="float: right; cursor: pointer; margin: 4px 4px; width: 11px; height: 11px;" onclick="onClosePopup()" src="../web/images/Cross.jpg"></img>
    <div align="center" style="margin-top: 10px;"><span class="LabelText" style="font-size: 14px;"><%= Resource.getProperty("hcm.holidaycalendar.upload.title",lang)%></span></div>
    <div id="DivUploadBlock" align="center" style="width: 100%; padding-top: 30px;">
		<table style="margin: 0 auto;">
            <tr>
				<td class="TitleCell" align="right" style="min-width: 150px;"><span class="LabelText"><%= Resource.getProperty("hr.file", lang)%></span></td>
                <td class="TextBox_ContentCell" style="min-width: 600px;">
                    <input type="file" class="dojoValidateValid required TextBox_TwoCells_width" id="inpUploadFile" name="inpUploadFile" style="padding: 0px; height: 22px;"></input>
                </td>
            </tr>
            <tr>
				<td class="TitleCell" align="right" style="min-width: 150px;"><span class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.upload.overwrite", lang)%></span></td>
                <td class="TextBox_ContentCell" style="min-width: 600px;">
                    <input type="checkbox" id="inpChkOverwrite" name="inpChkOverwrite" onclick="document.getElementById('inpOverwriteHolidays').value = (this.checked ? 'Y' : 'N')" style="padding: 0px; height: 22px;"></input>
                </td>
            </tr>
         </table>
    </div>
	<div align="center" style="margin-top: 10px;">
		<button type="button" class="ui-button ui-corner-all ui-button-text-only" role="button" style="background: url(&quot;../web/Grid/Modern/backgroundHeader.png&quot;) repeat-x scroll 50% 100% transparent; font-size: 12px; font-weight: normal; border: 1px solid rgb(211, 211, 211); color: rgb(85, 85, 85); font-family: Verdana,Arial,sans-serif;"
        onmouseover="this.style.border='1px solid #999999';this.style.color='#212121';" onmouseout="this.style.border='1px solid #D3D3D3';this.style.color='#555555';" onclick="uploadHolidaySheet();">
            <span class="ui-button-text" style="padding: 0.35em 1.5em;"><%= Resource.getProperty("hr.upload", lang)%></span></button>
        &nbsp;&nbsp;
        <button type="button" class="ui-button ui-corner-all ui-button-text-only" role="button" style="background: url(&quot;../web/Grid/Modern/backgroundHeader.png&quot;) repeat-x scroll 50% 100% transparent; font-size: 12px; font-weight: normal; border: 1px solid rgb(211, 211, 211); color: rgb(85, 85, 85); font-family: Verdana,Arial,sans-serif;"
            onmouseover="this.style.border='1px solid #999999';this.style.color='#212121';" onmouseout="this.style.border='1px solid #D3D3D3';this.style.color='#555555';" 
            onclick='onClosePopup();'>
            <span class="ui-button-text" style="padding: 0.35em 1em;"><%= Resource.getProperty("hr.close", lang)%></span></button>
    </div>
</div>
<div id="DivUploadErrorDialog" style="display: none; width: 800px; height: 400px; background: white; z-index: 50001; border: 2px solid #ff9c30; box-shadow: 0 0 20px #333; border-radius: 5px; position: absolute; margin: auto; top: 0; right: 0; bottom: 0; left: 0;">
    <img style="float: right; cursor: pointer; margin: 4px 4px; width: 11px; height: 11px;" onclick="onClosePopup()" src="../web/images/Cross.jpg"></img>
    <div align="center" style="margin-top: 10px;"><span class="LabelText" style="font-size: 14px;"><%= Resource.getProperty("payroll.uploadvalue.upload.errorlist",lang)%></span></div>
    <div style="padding: 10px;">
    <div style="overflow: auto; height: 300px;">
        <TABLE id="UploadErrorList" class="DataGrid_Header_Table_focus DataGrid_Body_Table_focus" width="100%" cellpadding="0" cellspacing="0" border="0" align="center">
        <tbody>
            <tr class="Popup_Client_Selector_DataGrid_HeaderRow">
                <th width="10%" class="DataGrid_Header_Cell"><%= Resource.getProperty("hr.sno",lang) %></th>
                <th width="20%" class="DataGrid_Header_Cell"><%= Resource.getProperty("hr.lineno", lang) %></th>
                <th width="70%" class="DataGrid_Header_Cell"><%= Resource.getProperty("hr.sno",lang) %></th>
            </tr>
        </tbody>
        </TABLE>
    </div></div>
    <div id="DivUploadError" align="center" style="width: 100%; padding: 10px 0px;">
        <button type="button" class="ui-button ui-corner-all ui-button-text-only" role="button" style="background: url(&quot;../web/Grid/Modern/backgroundHeader.png&quot;) repeat-x scroll 50% 100% transparent; font-size: 12px; font-weight: normal; border: 1px solid rgb(211, 211, 211); color: rgb(85, 85, 85); font-family: Verdana,Arial,sans-serif;"
            onmouseover="this.style.border='1px solid #999999';this.style.color='#212121';" onmouseout="this.style.border='1px solid #D3D3D3';this.style.color='#555555';" 
            onclick='onClosePopup();'>
            <span class="ui-button-text" style="padding: 0.35em 1em;"><%= Resource.getProperty("hr.close", lang)%></span></button>
    </div>
</div>
<table height="100%" border="0" cellpadding="0" cellspacing="0" id="main">
<tr>
	<td valign="top" id="tdleftTabs"><table cellpadding="0" cellspacing="0" class="Main_ContentPane_LeftTabsBar" id="tdLeftTabsBars"></table></td>
	<td valign="top">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_Left">
			<tr><td class="Main_NavBar_bg_left" id="tdleftSeparator"></td></tr>
			<tr><td class="Main_ToolBar_bg_left" valign="top"></td></tr>
			<tr><td class="Main_Client_bg_left"></td></tr>
			<tr><td class="Main_Bottom_bg_left"></td></tr>
		</table>
	</td>
	<td valign="top">
		<div class="Main_ContentPane_NavBar" id="tdtopButtons"><table class="Main_ContentPane_NavBar" id="tdtopNavButtons"></table></div>
		<div class="Main_ContentPane_ToolBar" id="paramToolBar">
			<table class="Main_ContentPane_ToolBar Main_ToolBar_bg" id="tdToolBar">
			<tr>
				<td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
			    <td width="2%" ><a href="javascript:void(0);" onClick="onClickRefresh()" class="Main_ToolBar_Button" onMouseOver="window.status='Reload Current Page';return true;" onMouseOut="window.status='';return true;" onclick="this.hideFocus=true" onblur="this.hideFocus=false" id="buttonRefresh"><img class="Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Refresh" src="../web/images/blank.gif" title="<%= Resource.getProperty("hcm.reload",lang) %>" border="0" id="linkButtonRefresh"></a></td> 
				<td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
				<td width="90%">
					<%-- <div style="text-align: right;"><div style="position: relative; top: 10px; right: 50px;">
						<button type="button" class="ButtonLink" id="SaveButton" onclick="onClickSave()" style="display: none;">
						    <table class="Button">
						      <tr>
						        <td class="Button_left"><img class="Button_Icon Button_Icon_process" alt="Submit" title="Submit" src="../web/images/blank.gif" border="0"></img></td>
						        <td class="Button_text"><%= Resource.getProperty("hcm.save", lang)%></td>
						        <td class="Button_right"></td>
						      </TR>
						    </table>
						</button>
					</div></div>
				</td>
				<td class="Main_ToolBar_Separator_cell" ><img src="../web/images/blank.gif" class="Main_ToolBar_Separator"></td>
				<td width="100%">
					<div style="text-align: right;"><div style="position: relative; top: 10px; right: 70px;">
						<button type="button" class="ButtonLink" id="CopyButton" onclick="onClickCopy()">
						    <table class="Button">
						      <tr>
						        <td class="Button_left"><img class="Button_Icon Button_Icon_process" alt="COPY" title="COPY" src="../web/images/blank.gif" border="0"></img></td>
						        <td class="Button_text"><%= Resource.getProperty("hcm.holidaycalendar.copy.from.previousyear.calendar", lang)%></td>
						        <td class="Button_right"></td>
						      </TR>
						    </table>
						</button>
					</div></div> --%>
					<div style="text-align: right;"><div class="MainButtonDiv" style="position: relative; top: 10px;">
        <BUTTON type="button" class="ButtonLink" id="SaveButton" onclick="onClickSave()" style="display: none;" >
            <TABLE class="Button"><TR><TD class="Button_left"><IMG class="Button_Icon Button_Icon_process" alt="Approve" title="Approve" src="../web/images/blank.gif" border="0"></IMG></TD><TD class="Button_text"><%= Resource.getProperty("hcm.save",lang) %></TD><TD class="Button_right"></TD></TR></TABLE>
        </BUTTON>
        <span>&nbsp;&nbsp;&nbsp;</span>
        <BUTTON type="button" class="ButtonLink" id="CopyButton" onclick="onClickCopy()">
            <TABLE class="Button"><TR><TD class="Button_left"><IMG class="Button_Icon Button_Icon_process" alt="Rework" title="Rework" src="../web/images/blank.gif" border="0"></IMG></TD><TD class="Button_text"><%= Resource.getProperty("hcm.holidaycalendar.copy.from.previousyear.calendar",lang)%></TD><TD class="Button_right"></TD></TR></TABLE>
        </BUTTON>
    </div>
    </div>
				</td>
				<td class="Main_ToolBar_Space"></td>
			</tr>
			</table>
		</div>
 		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_TabPane" id="tdtopTabs">
			<tr id="paramParentTabContainer"><td class="tabBackGroundInit"></td></tr>
			<tr id="paramMainTabContainer"><td class="tabBackGroundInit"><span style="background: none;" class="tabTitle"></span></td></tr>
			<tr><td class="tabBackGround">
				<div class="marginLeft"><div><span class="dojoTabcurrentfirst"><div>
					<span><a class="dojoTabLink" href="javascript:void(0);" onclick="return false;" onMouseOver="return true;" onMouseOut="return true;"><%= Resource.getProperty("hcm.holidaycalendar.title",lang)%></a></span>
				</div></span></div></div>
			</td></tr>
        </table>
		<div class="Main_ContentPane_Client" style="overflow: auto;" id="client">
			<table class="dojoTabContainer" border="0" cellpadding="0" cellspacing="0" width="100%" summary="">
			<tr><td>
				<table class="dojoTabPaneWrapper" border="0" cellpadding="0" cellspacing="0" width="100%" summary=""><tr><td>
				<!--  Manual Code Starts -->
				<div>
				<TABLE cellpadding="0" cellspacing="0" id="messageBoxID" class="MessageBox"><TBODY>
					<TR class="MessageBox_TopMargin"><TD class="MessageBox_LeftMargin"></TD><TD></TD><TD class="MessageBox_RightMargin"></TD></TR>
                    <TR><TD class="MessageBox_LeftMargin"></TD><TD><TABLE cellpadding="0" cellspacing="0" class="MessageBox_Container"><TBODY><TR>
						<TD class="MessageBox_LeftTrans"><TABLE style="width: 100%; height: 100%" cellpadding="0" cellspacing="0"><TBODY><TR><TD class="MessageBox_TopLeft"></TD></TR><TR><TD class="MessageBox_Left"></TD></TR></TBODY></TABLE></TD>
						<TD class="MessageBox_bg"><TABLE class="MessageBox_Top"><TBODY><TR><TD><SPAN><TABLE cellpadding="0" cellspacing="0" class="MessageBox_Body_ContentCell"><TBODY><TR><TD class="MessageBox_Icon_ContentCell"><DIV class="MessageBox_Icon"></DIV></TD>
						<TD style="vertical-align: top;" id="messageBoxIDContent"><SPAN><DIV class="MessageBox_TextTitle" id="messageBoxIDTitle"></DIV><DIV class="MessageBox_TextDescription" id="messageBoxIDMessage"></DIV><DIV class="MessageBox_TextSeparator"></DIV></SPAN></TD></TR></TBODY></TABLE></SPAN>
						<div id="hideMessage"><a style="color: yellow;font-size: 11.5px" href="javascript:void(0);" onClick="document.getElementById('messageBoxID').style.display = 'none';"><%=Resource.getProperty("hcm.hide",lang)%></a></div></TD></TR></TBODY></TABLE></TD>
						<TD class="MessageBox_RightTrans"><TABLE style="width: 100%; height: 100%" cellpadding="0" cellspacing="0"><TBODY><TR><TD class="MessageBox_TopRight"></TD></TR><TR><TD class="MessageBox_Right"></TD></TR></TBODY></TABLE></TD>
					</TR><TR><TD rowspan="2" class="MessageBox_BottomLeft"></TD><TD class="MessageBox_BottomTrans MessageBox_bg"></TD><TD rowspan="2" class="MessageBox_BottomRight"></TD></TR><TR><TD class="MessageBox_Bottom"></TD></TR></TBODY></TABLE></TD><TD class="MessageBox_RightMargin"></TD></TR><TR class="MessageBox_BottomMargin"><TD class="MessageBox_LeftMargin"></TD><TD></TD><TD class="MessageBox_RightMargin"></TD></TR></TBODY>
				</TABLE>
				<table width="100%"  style="border-collapse: separate; border-spacing: 15px;"><tbody>
					<tr>
					<td  align="right" style="width: 0.2%; class="TitleCell"><span class="LabelText" id="inpYearListLabel" ><%=Resource.getProperty("hcm.holidaycalendar.year", lang)%> 
                        </span>
                     </td>
                     <td class="TextBox_ContentCell" style="width: 1%;">
                        <select id="inpYear" class="ComboKey Combo_TwoCells_width" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();" >
                        </select>
                     </td>
					<%-- <td align="right" style="width: 5%;">
						<span class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.year", lang)%>&nbsp;:&nbsp;&nbsp;</span>
					</td>
					<td  style="width: 1%;">
						<select align="left"  class="ComboKey Combo_TwoCells_width" name="inpYear" id="inpYearList" onchange="document.getElementById('messageBoxID').style.display = 'none';enableForm();">
							<%
							int addiYear = 10, sYear = 1438, eYear = sYear + addiYear;
							if(request.getAttribute("StartYear") != null && !request.getAttribute("StartYear").equals(0)) {
								int year = Integer.parseInt(request.getAttribute("StartYear").toString());
								if(sYear > year)
									sYear = year;
								else
									eYear = year + addiYear;
							}
							for (int i = sYear; i < eYear; i++) {
							%><option value="<%=i%>"><%=i%></option>
							<% } %>
						</select>
					</td> --%>
					<td style="width: 1%;">
						<span class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.weekend1", lang)%>&nbsp;:</span>
					</td>
					<td style="width: 10%;">
						<table style="border-collapse: separate; border-spacing: 2px;"><tbody>
						<tr>
							<td><span style="padding: 2px;"><input type="checkbox" id="inpDay6" name="inpDay6" disabled="disabled" onclick="onClickWeekend('6', this.checked)">
							<label for="inpDay6" class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.saturday", lang)%></label></span></td>
							<td><span style="padding: 2px;"><input type="checkbox" id="inpDay0" name="inpDay0" disabled="disabled" onclick="onClickWeekend('0', this.checked)">
							<label for="inpDay0" class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.sunday", lang)%></label></span></td>
							<td><span style="padding: 2px;"><input type="checkbox" id="inpDay1" name="inpDay1" disabled="disabled" onclick="onClickWeekend('1', this.checked)">
							<label for="inpDay1" class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.monday", lang)%></label></span></td>
							<td><span style="padding: 2px;"><input type="checkbox" id="inpDay2" name="inpDay2" disabled="disabled" onclick="onClickWeekend('2', this.checked)">
							<label for="inpDay2" class="LabelText"><%=  Utility.escapeHTML(Resource.getProperty("hcm.holidaycalendar.tuesday", lang))%></label></span></td>
						
						</tr>
						<tr>
						<td><span style="padding: 2px;"><input type="checkbox" id="inpDay3" name="inpDay3" disabled="disabled" onclick="onClickWeekend('3', this.checked)">
							<label for="inpDay3" class="LabelText"><%=  Utility.escapeHTML(Resource.getProperty("hcm.holidaycalendar.wednesday", lang))%></label></span></td>
							<td><span style="padding: 2px;"><input type="checkbox" id="inpDay4" name="inpDay4" disabled="disabled" onclick="onClickWeekend('4', this.checked)">
							<label for="inpDay4" class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.thursday", lang)%></label></span></td>
							<td><span style="padding: 2px;"><input type="checkbox" id="inpDay5" name="inpDay5" disabled="disabled" onclick="onClickWeekend('5', this.checked)">
							<label for="inpDay5" class="LabelText"><%=  Utility.escapeHTML(Resource.getProperty("hcm.holidaycalendar.friday", lang))%></label></span></td>
							<td></td>
						</tr>
						</tbody></table>
					</td>
					<td  style="width: 1%;">
						<span class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.weekend2", lang)%>&nbsp;:&nbsp;&nbsp;</span>
					</td>
					 <td  style="width: 10%;">
						<table style="border-collapse: separate; border-spacing: 2px;"><tbody>
						<tr>
							 <td><span style="padding: 2px;"><input type="checkbox" id="inpDay13" name="inpDay13" disabled="disabled" onclick="onClickWeekend('13', this.checked)">
							<label for="inpDay13" class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.saturday", lang)%></label></span></td>
							<td><span style="padding: 2px;"><input type="checkbox" id="inpDay7" name="inpDay7" disabled="disabled" onclick="onClickWeekend('7', this.checked)">
							<label for="inpDay7" class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.sunday", lang)%></label></span></td>
							<td><span style="padding: 2px;"><input type="checkbox" id="inpDay8" name="inpDay8" disabled="disabled" onclick="onClickWeekend('8', this.checked)">
							<label for="inpDay8" class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.monday", lang)%></label></span></td>
							<td><span style="padding: 2px;"><input type="checkbox" id="inpDay9" name="inpDay9" disabled="disabled" onclick="onClickWeekend('9', this.checked)">
							<label for="inpDay9" class="LabelText"><%=  Utility.escapeHTML(Resource.getProperty("hcm.holidaycalendar.tuesday", lang))%></label></span></td> 
							
						</tr>
					 <tr>
						<td><span style="padding: 2px;"><input type="checkbox" id="inpDay10" name="inpDay10" disabled="disabled" onclick="onClickWeekend('10', this.checked)">
							<label for="inpDay10" class="LabelText"><%=  Utility.escapeHTML(Resource.getProperty("hcm.holidaycalendar.wednesday", lang))%></label></span></td>
							<td><span style="padding: 2px;"><input type="checkbox" id="inpDay11" name="inpDay11" disabled="disabled" onclick="onClickWeekend('11', this.checked)">
							<label for="inpDay11" class="LabelText"><%= Resource.getProperty("hcm.holidaycalendar.thursday", lang)%></label></span></td>
							<td><span style="padding: 2px;"><input type="checkbox" id="inpDay12" name="inpDay12" disabled="disabled" onclick="onClickWeekend('12', this.checked)">
							<label for="inpDay12" class="LabelText"><%=  Utility.escapeHTML(Resource.getProperty("hcm.holidaycalendar.friday", lang))%></label></span></td>
							<td></td> 
						</tr> 
						</tbody></table>
					</td> 
					<%-- <td style="width: 5%;">
						<table><tbody>
							<tr>
								<td><div class="weekEnd1"></div></td>
								<td><span><label class="LabelText" style="padding-left: 5px;"><%= Resource.getProperty("hcm.holidaycalendar.weekend1", lang) %></label></span><td>
								<td><div class="weekEnd2"></div></td>
								<td><span><label class="LabelText" style="padding-left: 5px;"><%= Resource.getProperty("hcm.holidaycalendar.weekend2", lang) %></label></span></td>
							</tr>
							<tr>
								<td><div class="feterholiday"></div></td>
								<td><span><label class="LabelText" style="padding-left: 5px;"><%= Resource.getProperty("hcm.holidaycalendar.feter.holiday", lang) %></label></span><td>
								<td><div class="adhaholiday"></div></td>
								<td><span><label class="LabelText" style="padding-left: 5px;"><%= Resource.getProperty("hcm.holidaycalendar.adha.holiday", lang) %></label></span></td>
							</tr>
							
							<tr>
							<td><div class="nationalHoliday"></div></td>
								<td><span><label class="LabelText" style="padding-left: 5px;"><%= Resource.getProperty("hcm.holidaycalendar.national.holiday", lang) %></label></span><td>
								</tr>
						</tbody></table>
					</td> --%>
					</tr>
                     
				</tbody>
				</table>
				<div align="center" id="TRCalendar" style=" width: 100%;">
					<div id="HolidayCalendar" style="display: block;padding: 0 0 10px 0;"></div>
				</div>
				<table width="100%" align="center" style="border-collapse: separate; border-spacing: 8px;"><tbody>
					<tr>
					<td style="width: 5%;" align="center">
						<table><tbody>
							<tr>
								<td><div class="weekEnd1"></div></td>
								<td><span><label class="LabelText" style="padding-left: 5px;"><%= Resource.getProperty("hcm.holidaycalendar.weekend1", lang) %></label>&nbsp;&nbsp;&nbsp;</span><td>
								<td></td>
								<td><div class="weekEnd2"></div></td>
								<td><span><label class="LabelText" style="padding-left: 5px;"><%= Resource.getProperty("hcm.holidaycalendar.weekend2", lang) %></label>&nbsp;&nbsp;&nbsp;</span></td>
								<td><div class="feterholiday"></div></td>
								<td><span><label class="LabelText" style="padding-left: 5px;"><%= Resource.getProperty("hcm.holidaycalendar.feter.holiday", lang) %>&nbsp;&nbsp;&nbsp;</label></span><td>
								<td><div class="adhaholiday"></div></td>
								<td><span><label class="LabelText" style="padding-left: 5px;"><%= Resource.getProperty("hcm.holidaycalendar.adha.holiday", lang) %></label>&nbsp;&nbsp;&nbsp;</span></td>
							
							<td><div class="nationalHoliday"></div></td>
								<td><span><label class="LabelText" style="padding-left: 5px;"><%= Resource.getProperty("hcm.holidaycalendar.national.holiday", lang) %></label>&nbsp;&nbsp;&nbsp;</span><td>
								</tr>
						</tbody></table>
						</tr>
				</tbody>
				</table>
					</td>
				</div>
				<!--  Manual Code Ends -->
				</td></tr>
        		</table>
        	</td></tr>
        	</table>
		</div>  
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="Menu_ContentPane_Bottom" id="tdbottomSeparator"><tr><td class="Main_Bottom_bg"><IMG src="../web/images/blank.gif" border="0"></IMG></td></tr></table>
	</td>
	<td valign="top">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="Main_ContentPane_Right" id="tdrightSeparator">
			<tr><td class="Main_NavBar_bg_right"></td></tr>
			<tr><td class="Main_ToolBar_bg_right" valign="top"></td></tr>
			<tr><td class="Main_Client_bg_right"></td></tr>
			<tr><td class="Main_Bottom_bg_right"></td></tr>
		</table>
	</td>
</tr>
</table>
</form>
</body>
 <script type="text/javascript" src="../web/js/common/select2.min.js"></script> 
<script type="text/javascript">
var lastClickedDate = '', natDays =  new Array(),ajax=false,isHolidayDialogOpen = false;
var startYear = 1430;
var currentYear="<%=request.getAttribute("CurrentYear")%>";
var maxDate="<%=request.getAttribute("maxDate")%>";
var minDate="<%=request.getAttribute("minDate")%>";
var weekend1=false,weekend2=false,holidayListVal=false,dateUpdate=false;
var weekend1Year="",weekend2Year="";
var changeFlag=0;
var lasclickedDate="";
var holidayList = {};
var day="",month="",year="";
var language="<%=lang.split("_")[0].toString()%>";
var calendar= $.calendars.instance('ummalqura',language);
var lastclickedYear="<%=request.getAttribute("CurrentYear")%>";
holidayList.list = [];
var weekdaysName = ['<%= Resource.getProperty("hcm.holidaycalendar.sunday", lang) %>', '<%= Resource.getProperty("hcm.holidaycalendar.monday", lang) %>', '<%= Utility.escapeHTML(Resource.getProperty("hcm.holidaycalendar.tuesday", lang)) %>', '<%= Utility.escapeHTML(Resource.getProperty("hcm.holidaycalendar.wednesday", lang)) %>', '<%= Resource.getProperty("hcm.holidaycalendar.thursday", lang) %>', '<%= Utility.escapeHTML(Resource.getProperty("hcm.holidaycalendar.friday", lang)) %>', '<%= Resource.getProperty("hcm.holidaycalendar.saturday", lang) %>'];
var isHolidayDialogOpen = false, isUploadDialogOpen = false, isDownloadDialogOpen = false, isUploadErrorDialogOpen = false;
var weekEnd1="WE1",weekEnd2="WE2";
var holidayCount=0;
var holidayArray = new Array();
function onClickRefresh() {
	if(changeFlag==1){
	    OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function(result) {
            if (result){
            	saveHoliday();
            }
            else{
            	$('#inpHolidayDate, #act').val('');
            	document.getElementById("messageBoxID").style.display = "none";
            	submitCommandForm('DEFAULT', false, null, 'HolidayCalendar', '_self', null, false);
            }
	    });
	}
	else{
	$('#inpHolidayDate, #act').val('');
	document.getElementById("messageBoxID").style.display = "none";
	submitCommandForm('DEFAULT', false, null, 'HolidayCalendar', '_self', null, false);
	}
}
function nationalDays(date, inMonth) {
	if (inMonth) {
		var day=date.day();
		 var month=date.month();
		 var  Class =holidayArray[month][day][0];
		// if ( Class.indexOf(weekEnd1) != -1 && !weekend1) {
			//weekend1 = true;
			//weekend1Year = date.year();
			/* var day = Class;
			var pattern = weekEnd1;
			var n = day.substr(day.indexOf(pattern) + pattern.length,
					day.length);
			document.getElementById("inpDay" + n).checked = true; */
		//}
		//if ( Class.indexOf(weekEnd2) != -1 && !weekend2) {
			//weekend2 = true;
			//weekend2Year = date.year();
			/* var day = Class; 
			var pattern = weekEnd2; 
			var n = day.substr(day.indexOf(pattern) + pattern.length,
					day.length); 
			document.getElementById("inpDay" +n).checked = true; */

		//}  
		
		 return {
				dateClass : Class + '_day'
			} 
	}
	return {};
}
calendarpicker();
function calendarpicker() {
	resetHoliday();
	$('#HolidayCalendar')
			.calendarsPicker(
					{
						calendar : $.calendars.instance('ummalqura',language),
						hideIfNoPrevNext : true,
						// monthsToShow: [2, 6],
						monthsToShow : [ 2, 6 ],
						monthsToStep : 0,
						monthsOffset : 0,
						monthsToJump : 0,
						changeMonth : false,
						changeYear : false,
						showMonth : false,
						showOtherMonths : false,
						closeOnSelect : true,
						onDate : nationalDays,
						selectOtherMonths : false,
						showCurrentYearOnly : true,
						minDate : calendar.newDate(parseFloat(minDate.substr(0,
								4)), parseFloat(minDate.substr(4, 2)),
								parseFloat(minDate.substr(6, 2))),
						maxDate : calendar.newDate(parseFloat(maxDate.substr(0,
								4)), parseFloat(maxDate.substr(4, 2)),
								parseFloat(maxDate.substr(6, 2))),
						onSelect : function(date) {
							onClickHolidayCheckBox();
							holidayListVal = false;
							lasclickedDate = date;
							document.getElementById("lblDialogDate").innerHTML = calendar
									.newDate(date[0]._year, date[0]._month,
											date[0]._day).formatDate(
											'\'Day\' d \'of\' MM, yyyy');
									if(holidayArray[date[0]._month][date[0]._day][0]!=""){
									if (holidayArray[date[0]._month][date[0]._day][0].indexOf("WE1") >= 0) {
										document.getElementById("inpWeekend1").checked = true;
										$("#inpHolidayList").hide();
									} else if (holidayArray[date[0]._month][date[0]._day][0].indexOf("WE2") >= 0) {
										document.getElementById("inpWeekend2").checked = true;
										$("#inpHolidayList").hide();
									} else {
										document.getElementById("inpHoliday").checked = true;
										document
												.getElementById("inpHolidaylist").value = holidayArray[date[0]._month][date[0]._day][0];
										holidayListVal = true;
										$("#inpHolidayList").show();
									}
									}
									else{
										  $('input[name=inpHoliday]').removeAttr('checked');
									      $('input[name=inpHoliday]').removeAttr('checked1');
									      $("#inpHolidayList").hide();
									}
								openHolidayDialog();
							if (!holidayListVal) {
								document.getElementById("inpHolidaylist").value = "0";
							}
							return true;
						},
						onClose : function(date) {

							return true;
						},
						onChangeMonthYear : function() {
							return false;
						}

					});
}
function enableForm() {
	if (changeFlag == 1) {
		OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>',
				function(result) {
					if (result) {
						saveHoliday();
						$("#inpYear").val(lastclickedYear);
						onChangeYear(lastclickedYear);
						changeFlag = 0;
					} else {
						lastclickedYear = $("#inpYear").val();
						onChangeYear($("#inpYear").val());
					}
				});
	} else {
		lastclickedYear = $("#inpYear").val();
		onChangeYear($("#inpYear").val());
	}
}
function onChangeYear(year) {
	resetHoliday();
	changeFlag = 0;
	holidayList = {};
	holidayList.list = [];
	weekend1 = false;
	weekend2 = false;
	//weekend1Year = "";
	//weekend2Year = "";
	var calendar = $.calendars.instance('ummalqura',language);
	getMinAndMaxDate(year);
	getHoliday(year);
	/* for (var i = 0; i <= 13; i++) {
		document.getElementById("inpDay" + i).disabled = false;
		document.getElementById("inpDay" + i).checked = false;
	} */
	//weekend1Year=year;
    //weekend2Year=year;
      $('input[name=inpHoliday]').removeAttr('checked');
      $('input[name=inpHoliday]').removeAttr('checked1');

	setTimeout(function() {
		$('#HolidayCalendar').calendarsPicker(
				'option',
				{
					calendar : calendar,
					minDate : calendar.newDate(
							parseFloat(minDate.substr(0, 4)),
							parseFloat(minDate.substr(4, 2)),
							parseFloat(minDate.substr(6, 2))),
					maxDate : calendar.newDate(
							parseFloat(maxDate.substr(0, 4)),
							parseFloat(maxDate.substr(4, 2)),
							parseFloat(maxDate.substr(6, 2)))
				}).calendarsPicker('showMonth', parseInt(year, 10),
				parseInt('1', 10));
		copyButtonHide();
	}, 10);
}
function copyButtonHide() {
	
for (var i = 0; i <= 13; i++) {
	if(document.getElementById("inpDay" + i).checked==true){
		weekend1=true;
	}
}
if(weekend1 || ($("#inpYear").val()==startYear)){
	$("#CopyButton").hide();
}
else{
	$("#CopyButton").show();
}
}
function onClickWeekend(id, index) {
	var day=0,month=0;
	changeFlag=1;
	document.getElementById("messageBoxID").style.display = "none";
	var year = document.getElementById("inpYear").value;
	id = parseInt(id);
	if(id <= 6){
		for ( var i = 0; i <= 6; i++) {
			
			if(document.getElementById("inpDay" + i).checked && id!=i){// && (weekend1Year==year || weekend1Year==""))
				 OBAlert('<%= Resource.getProperty("hcm.holidaycalendar.only.one.weekend1", lang) %>');
					document.getElementById("inpDay" + id).checked =false;
			        return false;
			}
		}
	}
	if(id >= 7){
		for ( var i = 7; i <= 13; i++) {
			if(document.getElementById("inpDay" + i).checked && id!=i ){//&& (weekend1Year==year || weekend1Year=="")
				 OBAlert('<%= Resource.getProperty("hcm.holidaycalendar.only.one.weekend2", lang) %>');
				 document.getElementById("inpDay" + id).checked =false;
			        return false;
			}
		}
	}
		for ( var i = 0; i <= 13; i++) {   
			
			if(document.getElementById("inpDay" + i).checked &&  id!=i &&  (i%7==id || i+7==id) ){//&& (weekend1Year==year || weekend1Year=="")
				 OBAlert('<%= Resource.getProperty("hcm.holidaycalendar.sameWeekend.should.not.add", lang) %>');
				 document.getElementById("inpDay" + id).checked =false;
			        return false;
			}
		}
	if(index){
		var diffWeek="";
	var firstDate= calendar.newDate(parseFloat(minDate.substr(0,4)),parseFloat(minDate.substr(4,2)),parseFloat(minDate.substr(6,2)));
	var firstDatedayofWeek= calendar.dayOfWeek(firstDate);
	var lastDate=  calendar.newDate(parseFloat(maxDate.substr(0,4)),parseFloat(maxDate.substr(4,2)),parseFloat(maxDate.substr(6,2)));
	var FirstDate=calendar.newDate(parseFloat(minDate.substr(0,4)),parseFloat(minDate.substr(4,2)),parseFloat(minDate.substr(6,2)));;
	if(id > 6){
		 diffWeek=(parseInt(id)-7)-parseInt(firstDatedayofWeek);	
	}
	else{
	 diffWeek=parseInt(id)-parseInt(firstDatedayofWeek);
	}
    var date = firstDate.add(diffWeek,'d'); 
    for (;;) {
    	if ( date >= FirstDate && date <=lastDate ) {
		 day=date.day();
		 month=date.month();
		 if (day < 10) {
			    day = "0" + date.day();
			  }

			  if (month < 10) {
			    month = "0" + date.month();
			  }
 		if(id <=6){
		 holidayArray[parseInt(date.month())][parseInt(date.day())][0]= weekEnd1+id;
		}
		else{
			 holidayArray[parseInt(date.month())][parseInt(date.day())][0]= weekEnd2+id;
		}
		 holidayArray[parseInt(date.month())][parseInt(date.day())][1]=date.day();
		 holidayArray[parseInt(date.month())][parseInt(date.day())][2]= date.month();
		 holidayArray[parseInt(date.month())][parseInt(date.day())][3]=date.year();
		 holidayArray[parseInt(date.month())][parseInt(date.day())][4]= day + "-" + month + "-" + date.year() ;
    	}
		 date.add(7,'d');

		if (date > lastDate)
			break;
    }
    setTimeout(function () {
    	 $("#CopyButton").hide();
		  var calendar = $.calendars.instance('ummalqura',language);
		  $('#HolidayCalendar').calendarsPicker('option', { calendar: calendar }).
		    calendarsPicker('showMonth', parseInt($("#inpYear").val(), 10),
		    parseInt('1', 10));
		  document.getElementById("SaveButton").style.display = "";
        }, 10);
	}
	else{
		if(id >6){
			$('.WE2'+id+ '_day').removeClass('WE2'+id+ '_day');
			for ( var i = 1; i <= 12; i++) {//month
				for ( var j = 1; j <= 30; j++) {// day
					if (holidayArray[i][j][0] !== "") {
						if(holidayArray[i][j][0].indexOf(weekEnd2)!=-1) {
							 holidayArray[i][j][0]="";
							 holidayArray[i][j][1]="";
							 holidayArray[i][j][2]="";
							 holidayArray[i][j][3]="";
							 holidayArray[i][j][4]="";
						}
					}
					}
				}
			
		}
		else{
			$('.WE1'+id+ '_day').removeClass('WE1'+id+ '_day');
			for ( var i = 1; i <= 12; i++) {//month
				for ( var j = 1; j <= 30; j++) {// day
					
					if (holidayArray[i][j][0] !== "") {
						 if(holidayArray[i][j][0].indexOf(weekEnd1)!=-1) {
							 holidayArray[i][j][0]="";
							 holidayArray[i][j][1]="";
							 holidayArray[i][j][2]="";
							 holidayArray[i][j][3]="";
							 holidayArray[i][j][4]="";
						}
					}
					}
				}
		}
		
		document.getElementById("SaveButton").style.display = "";
	}
}
function openHolidayDialog() {
	isHolidayDialogOpen = true;
	$("#DivBlackOverlay, #DivHolidayDialog").show();
}
function onClosePopup() {
	$("#DivBlackOverlay, #DivHolidayDialog").hide();
}

var radio_button=false;
$('.radio-button').on("click", function(event){
    var this_input=$(this);
    var temp=false;
        if(this_input.attr('checked1')=='11')
    {
        this_input.attr('checked1','11')
        temp=true;
    }
    else
    {
        temp=false;
        this_input.attr('checked1','22')
    }
    $('.radio-button').prop('checked', false);
    $('.radio-button').attr('checked1','22')
    if(temp)
    {
        this_input.prop('checked', false);
        this_input.attr('checked1','22')
    }
    else{
        this_input.prop('checked', true);
        this_input.attr('checked1','11')
    }
    if (document.getElementById("inpHoliday").checked == false)
		$("#inpHolidayList").hide();
	else {
		$("#inpHolidayList").show();
	} 
});

function onClickHolidayCheckBox() {
	document.getElementById("inpHoliday").checked=false;
	document.getElementById("inpWeekend1").checked=false;
	document.getElementById("inpWeekend2").checked=false;
}
function onClickSave() {
	OBConfirm('<%= Resource.getProperty("hcm.holidaycalendar.save", lang) %>',
			function(result) {
				if (result) {
					saveHoliday();
				}
			});
}
function saveHoliday() {
	weekend1 = false;
	weekend2 = false;
	  showProcessBar(true, 2);
	  for ( var i = 1; i <= 12; i++) {
			for ( var j = 1; j <= 30; j++) {
				if (holidayArray[i][j][0] !== "") {
					var len = holidayList.list.length;
					holidayList.list[len] = {};
					    holidayList.list[len]['year'] =  (holidayArray[i][j][3] == "" ? "" : holidayArray[i][j][3]);
						holidayList.list[len]['month'] = (holidayArray[i][j][2] == "" ? "" : holidayArray[i][j][2]);
						holidayList.list[len]['day'] =(holidayArray[i][j][1] == "" ? "" : holidayArray[i][j][1]);
						holidayList.list[len]['dateclass'] = (holidayArray[i][j][0] == "" ? "" : holidayArray[i][j][0]);
						holidayList.list[len]['holidayDate'] = (holidayArray[i][j][4] == "" ? "" : holidayArray[i][j][4]);
				}
			}
		}
	  document.getElementById("inpHolidayDate").value = JSON.stringify(holidayList);
	 $.post('<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.holidaycalendar.ajax/HolidayCalendarAjax', {
		action : 'SaveHolidays',
		inpYear : lastclickedYear,
		inpHolidayDate : document.getElementById("inpHolidayDate").value
	}, function(data) {
		$("#SaveButton").hide();
		showProcessBar(false);
		if (data.result == '1') {
			getHoliday(lastclickedYear);
			changeFlag=0;
			displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.holidaycalendar.savesuccess",lang) %>");
		}
		else {
			displayMessage("E", "<%=Resource.getProperty("hcm.error",lang)%>","<%= Resource.getProperty("hcm.holidaycalendar.saveerror",lang) %>");
		}
	}); 
	}
function setHoliday() {
	  changeFlag = 1;
	  dateUpdate = false;
	  day = lasclickedDate[0]._day;
	  month = lasclickedDate[0]._month;
	var year = lasclickedDate[0]._year;
	  if (lasclickedDate[0]._day < 10) {
	    day = "0" + lasclickedDate[0]._day;
	  }

	  if (lasclickedDate[0]._month < 10) {
	    month = "0" + lasclickedDate[0]._month;
	  }
	  var date = calendar.newDate(parseInt(lasclickedDate[0]._year), parseInt(month), parseInt(day));
	  var dateofWeek = calendar.dayOfWeek(date);

	  if (document.getElementById("inpHoliday").checked === true) {
	    if (document.getElementById("inpHolidaylist").value != "0") {

	      holidaydeSelect();
	      	holidayArray[parseInt(month)][parseInt(day)][0] =  document.getElementById("inpHolidaylist").value;
	        holidayArray[parseInt(month)][parseInt(day)][1] = parseInt(month);
		  	holidayArray[parseInt(month)][parseInt(day)][2] = parseInt(day);
		  	holidayArray[parseInt(month)][parseInt(day)][3] =parseInt(year);
		  	holidayArray[parseInt(month)][parseInt(day)][4] =day + "-" + month + "-" + year; 
	    } else {

	    }
	  } else if (document.getElementById("inpWeekend1").checked === true ) {
		  if(weekEndVal(day, month, lasclickedDate[0]._year, true, dateofWeek)){
		    holidaydeSelect();
			holidayArray[parseInt(month)][parseInt(day)][0] = "WE1" + dateofWeek;
		    holidayArray[parseInt(month)][parseInt(day)][1] = parseInt(month);
		  	holidayArray[parseInt(month)][parseInt(day)][2] = parseInt(day);
		  	holidayArray[parseInt(month)][parseInt(day)][3] =parseInt(year);
		  	holidayArray[parseInt(month)][parseInt(day)][4] =day + "-" + month + "-" + year; 
		  }


	  } else if (document.getElementById("inpWeekend2").checked === true ) {
		  if(weekEndVal(day, month, lasclickedDate[0]._year, false, dateofWeek)){
			  holidaydeSelect();
		    
			holidayArray[parseInt(month)][parseInt(day)][0] = "WE2" + (dateofWeek + 7);
			holidayArray[parseInt(month)][parseInt(day)][1] = month;
			holidayArray[parseInt(month)][parseInt(day)][2] = day;
			holidayArray[parseInt(month)][parseInt(day)][3] =year
			holidayArray[parseInt(month)][parseInt(day)][4] =day + "-" + month + "-" + year; 
		  }
	  	

	  }
	  else{
		  holidaydeSelect();
	        
	  } 
	  validateWeekend();
	  setTimeout(function () {
		  $("#CopyButton").hide();
	    var calendar = $.calendars.instance('ummalqura',language);
	    $('#HolidayCalendar').calendarsPicker('option', {
	      calendar: calendar
	    }).calendarsPicker('showMonth', parseInt($("#inpYear").val(), 10), parseInt('1', 10));
	    document.getElementById("SaveButton").style.display = "";
	  }, 10);
	  onClosePopup();
	}
	//getHoliday(currentYear);
	function validateWeekend(){
		var year = document.getElementById("inpYear").value;
		var weekEnd1  = new Array(7), weekEnd2 = new Array(7); 
		for ( var i = 1; i <= 12; i++) {//month
			for ( var j = 1; j <= 30; j++) {// day
				if (holidayArray[i][j][0] !== "") {
					if(holidayArray[i][j][0].indexOf("WE1")!=-1) {
						var date= calendar.newDate(year,i,j);
						var dateofWeek= calendar.dayOfWeek(date);
						weekEnd1[dateofWeek] = true;
					} else if(holidayArray[i][j][0].indexOf("WE2")!=-1) {
						var date= calendar.newDate(year,i,j);
						var dateofWeek= calendar.dayOfWeek(date)+7;
						weekEnd2[dateofWeek] = true;
					}
				}
				}
			}
		
		for(var i = 0; i <= 6; i++) {
			if(weekEnd1[i]) {
				document.getElementById("inpDay"+i).checked=true;
			} else {
				document.getElementById("inpDay"+i).checked=false;
			}
		}
		for(var i = 7; i <= 13; i++) {
			if(weekEnd2[i]) {
				document.getElementById("inpDay"+i).checked=true;
			} else {
				document.getElementById("inpDay"+i).checked=false;
			}
		}
	}
	
function getHoliday(year) {
	 holidayList = {};
	 holidayList.list = [];
	 for (var i = 0; i <= 13; i++) {
		document.getElementById("inpDay" + i).disabled=false;
		document.getElementById("inpDay" + i).checked=false;

		}
	$.ajax({
	    type:'GET',
		url: '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.holidaycalendar.ajax/HolidayCalendarAjax',
		data: {action : 'GetHolidays',
			inpYear : year},
	    dataType: 'json',
	    async:false,
           success: function(data) {
           	document.getElementById("inpYear").focus();
			var weekEndList = data.weekendList;
			var weekEndDayList=data.weekendDays;
			if(weekEndDayList!=null){
				 for (var i in weekEndDayList) { 
					 document.getElementById("inpDay" + parseInt(weekEndDayList[i].weekdays)).checked = true;
				 }
			}
			if(weekEndList!=null){
			 for (var i in weekEndList) { 
				holidayArray[parseInt(weekEndList[i].month)][parseInt(weekEndList[i].day)][0]= weekEndList[i].dateclass;
				holidayArray[parseInt(weekEndList[i].month)][parseInt(weekEndList[i].day)][1]= weekEndList[i].day;
				holidayArray[parseInt(weekEndList[i].month)][parseInt(weekEndList[i].day)][2]= weekEndList[i].month;
				holidayArray[parseInt(weekEndList[i].month)][parseInt(weekEndList[i].day)][3]= weekEndList[i].year;
				holidayArray[parseInt(weekEndList[i].month)][parseInt(weekEndList[i].day)][4]= weekEndList[i].day+"-"+weekEndList[i].month+"-"+weekEndList[i].year; 

			 }
				if (data.length > 0) {
					setTimeout(function () {
						  var calendar = $.calendars.instance('ummalqura',language);
						  $('#HolidayCalendar').calendarsPicker('option', { calendar: calendar }).
						    calendarsPicker('showMonth', parseInt($("#inpYear").val(), 10),
						    parseInt('1', 10));
				        }, 10);
				}
				$("#CopyButton").hide();
			}
			else{
				copyButtonHide();
			}
           }
	});
}
function getMinAndMaxDate() {
	$.ajax({
	    type:'GET',
		url: '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.holidaycalendar.ajax/HolidayCalendarAjax',
		data: {action : 'GetMinAndMaxDate',
			inpYear : $("#inpYear").val()},
	    dataType: 'json',
	    async:false,
           success: function(data) {
		minDate=data.mindate;
		maxDate=data.maxdate;
           }
	});
}
function onClickCopy(){
	OBAsk('<%= Resource.getProperty("hcm.holidaycalendar.copy.from.previousyear.holiday", lang) %>', function(result) {
           if (result){
	$.ajax({
	    type:'GET',
		url: '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.holidaycalendar.ajax/HolidayCalendarAjax',
		data: {action : 'copyHolidays',
			inpYear : $("#inpYear").val()},
	    dataType: 'json',
	    async:false,
           success: function(datares) {
           	document.getElementById("inpYear").focus();
           	
			var weekEndList = datares.weekendList;
			var weekEndDayList=datares.weekendDays;
			if(weekEndDayList!=null){
				 for (var i in weekEndDayList) { 
					 document.getElementById("inpDay" + parseInt(weekEndDayList[i].weekdays)).checked = true;
				 }
			}
			if(weekEndList!=null){
				 for (var i in weekEndList) { 
					holidayArray[parseInt(weekEndList[i].month)][parseInt(weekEndList[i].day)][0]= weekEndList[i].dateclass;
					holidayArray[parseInt(weekEndList[i].month)][parseInt(weekEndList[i].day)][1]= weekEndList[i].day;
					holidayArray[parseInt(weekEndList[i].month)][parseInt(weekEndList[i].day)][2]= weekEndList[i].month;
					holidayArray[parseInt(weekEndList[i].month)][parseInt(weekEndList[i].day)][3]= weekEndList[i].year;
					holidayArray[parseInt(weekEndList[i].month)][parseInt(weekEndList[i].day)][4]= weekEndList[i].day+"-"+weekEndList[i].month+"-"+weekEndList[i].year; 
				 }
                 $("#CopyButton").hide();
			}
			/*  for (var i in weekEndList) { 
			   var len = holidayList.list.length;
			    holidayList.list[len] = {};
				holidayList.list[len]['month'] = weekEndList[i].month;
				holidayList.list[len]['day'] =weekEndList[i].day;
				holidayList.list[len]['dateclass'] = weekEndList[i].dateclass;
				holidayList.list[len]['year'] = weekEndList[i].year;
				holidayList.list[len]['holidayDate'] = weekEndList[i].day+"-"+weekEndList[i].month+"-"+weekEndList[i].year;
			 }
			 
			 var data = holidayList.list;
				if (data.length > 0) {
					for ( var i in data) {
						 holidayArray[parseInt(data[i].month)][parseInt(data[i].day)][0]= data[i].dateclass;
					}
				} */
					 if (datares.result == '1') {
							displayMessage("S", "<%=Resource.getProperty("hcm.success",lang)%>","<%= Resource.getProperty("hcm.holidaycalendar.copy.success",lang) %>");
						}
						else {
							displayMessage("E", "<%=Resource.getProperty("hcm.error",lang)%>","<%= Resource.getProperty("hcm.holidaycalendar.copy.error",lang) %>");
						}
					setTimeout(function () {
						  var calendar = $.calendars.instance('ummalqura');
						  $('#HolidayCalendar').calendarsPicker('option', { calendar: calendar,
                              minDate : calendar.newDate(
                                      parseFloat(minDate.substr(0, 4)),
                                      parseFloat(minDate.substr(4, 2)),
                                      parseFloat(minDate.substr(6, 2))),
                              maxDate : calendar.newDate(
                                      parseFloat(maxDate.substr(0, 4)),
                                      parseFloat(maxDate.substr(4, 2)),
                                      parseFloat(maxDate.substr(6, 2)))     
                        
                        }).
						    calendarsPicker('showMonth', parseInt($("#inpYear").val(), 10),
						    parseInt('1', 10));
				        }, 10);
				
           }
	});
           }
           else{
           	
           }
	 });
}
// week end validation
function weekEndVal(day, month, year, weekEnd1, dateofWeek) {
	// loop it from 0 to 13 means weekend1-1 to 7 and weekend2 -8 to 14
	for (var i = 0; i <= 13; i++) {
		// checking if we click weekend2 radio button
		if (!weekEnd1) {
		// checking already weekend 2 selected  - by using dateofweek+7,ie- dateofweek is 6 means then 6+7 =13 other than 13th
		//anyother checkbox or not.. only if i is greater than 6 (7 to 13)
			if (i > 6 && document.getElementById("inpDay" + i).checked
					&& ((dateofWeek + 7) != i)) {
				OBAlert('<%= Resource.getProperty("hcm.holidaycalendar.only.one.weekend2", lang) %>');
				return false;
			}
			// checking already same day selected as weekend1 - by using dateofweek,ie- dateofweek is 6.
			//checking already 6th is selected as weekend1 or nor
			else if(i <= 6 && document.getElementById("inpDay" + i).checked
					&& dateofWeek == i){
				OBAlert('<%= Resource.getProperty("hcm.holidaycalendar.sameWeekend.should.not.add", lang) %>');
				return false;
			}

		}
		// checking if we click weekend1 radio button
		else {
			// checking already weekend 1 selected   - by using dateofweek,ie- dateofweek is 6 means  other than 6th
			//anyother checkbox or not only if i is lesser than or equal to 6 (0 to 6)
			if (i <= 6 && document.getElementById("inpDay" + i).checked
					&& dateofWeek != i) {
				OBAlert('<%= Resource.getProperty("hcm.holidaycalendar.only.one.weekend1", lang) %>');
				return false;
			}
			// checking already same day selected as weekend2  - by using dateofweek+7,ie- dateofweek is 6 means then 6+7 =13 
			//checking already 13th is selected as weekend2 or nor
			else if(i > 6 && document.getElementById("inpDay" + i).checked
					&& ((dateofWeek + 7) == i)){
				OBAlert('<%= Resource.getProperty("hcm.holidaycalendar.sameWeekend.should.not.add", lang) %>');
				return false;
			}
		}
	}
	return true;
}

function holidaydeSelect() {
	 holidayArray[lasclickedDate[0]._month][lasclickedDate[0]._day][0]="";
	 holidayArray[lasclickedDate[0]._month][lasclickedDate[0]._day][1]="";
	 holidayArray[lasclickedDate[0]._month][lasclickedDate[0]._day][2]="";
	 holidayArray[lasclickedDate[0]._month][lasclickedDate[0]._day][3]="";
	 holidayArray[lasclickedDate[0]._month][lasclickedDate[0]._day][4]="";

	 
	return true;
}

function resetHoliday() {
	$("#SaveButton").hide();
	for (var i = 1; i <= 12; i++) {
		holidayArray[i] = new Array();
		for (var j = 1; j <= 30; j++) {
			holidayArray[i][j] = new Array();
			holidayArray[i][j][0] = '';//class
			holidayArray[i][j][1] = '';//day
			holidayArray[i][j][2] = '';//month
			holidayArray[i][j][3] = '';//year
			holidayArray[i][j][4] = '';//holidaydate
		}
	}
}

getYearList("<%=request.getAttribute("CurrentYear")%>");
function getYearList(year){
    document.getElementById("inpYear").value = "0";
        $("#inpYear").select2(selectBoxAjaxPaging({
            url : function() {
                return '<%=request.getContextPath()%>/sa.elm.ob.hcm.ad_forms.holidaycalendar.ajax/HolidayCalendarAjax?action=getYearList'
           },
           data: [{
   	        id: year,
   	        text: year
   	      }],
           size : "small"
       }));
        $("#inpYear").on("select2:unselecting", function (e) {
    	      document.getElementById("inpYear").options.length = 0;
    	    });
 }
</script>
</html>