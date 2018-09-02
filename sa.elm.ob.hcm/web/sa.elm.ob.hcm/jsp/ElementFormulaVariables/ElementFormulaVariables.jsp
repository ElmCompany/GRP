<%@page import="sa.elm.ob.hcm.properties.Resource"%>
<%@page import="sa.elm.ob.hcm.ad_forms.ElementFormulaVariables.ElementFormulaVariablesVO"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.ArrayList"%>
<%
    List<ElementFormulaVariablesVO> elementFormulaList = (ArrayList<ElementFormulaVariablesVO>)
        request.getAttribute("ElementFormulaList");
    String lang = ((String)session.getAttribute("#AD_LANGUAGE"));
    String style="../web/skins/ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP.css";
    String toolBarStyle="../web/js/common/CommonFormLtr.css";
    if(lang.equals("ar_SA")){
     style="../web/skins/rtl/org.openbravo.userinterface.skin.250to300Comp/250to300Comp/Openbravo_ERP_250.css";
    toolBarStyle="../web/js/common/CommonFormRtl.css";
    }
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK rel="stylesheet" type="text/css" href="<%=style %>" id="paramCSS"></LINK>
   <link rel="stylesheet" type="text/css" href="<%=toolBarStyle %>"></link>
       <style>
        td.DataGrid_Body_Cell span.text { white-space: normal; }
        th.DataGrid_Header_Cell { white-space: normal; background-size: 100% 100%; }
    </style>
    
    <script>
    var OB = parent.parent.parent.parent.OB;
    function OnClosePopup() {
        OB.Layout.ClassicOBCompatibility.Popup.close("EHCM_ElementFormula_Hdr", "");
    }
    </script>
</head>
<body>
<form id="form" method="post" action="" name="frmMain">
<div style="height: 490px; overflow: auto; padding: 5px 15px;">
    <div style="text-align: center; margin-top: 10px;">
        <b style="font-size: 11pt;"><%= Resource.getProperty("hcm.elementformula.variables", lang) %></b>
    </div>
    <div>
        <TABLE class="FieldGroup" cellspacing="0" cellpadding="0" border="0">
            <TBODY>
              <TR class="FieldGroup_TopMargin"></TR>
              <TR>
                <TD class="FieldGroupTitle_Left"><IMG class="FieldGroupTitle_Left_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                <TD class="FieldGroupTitle"><%= Resource.getProperty("hcm.variables",lang)%></TD>
                <TD class="FieldGroupTitle_Right"><IMG class="FieldGroupTitle_Right_bg" border="0" src="../web/images/blank.gif"></IMG></TD>
                <TD class="FieldGroupContent"></TD>
              </TR>
              <TR class="FieldGroup_BottomMargin"></TR>
            </TBODY>
        </TABLE>
    </div>
    <div>
        <TABLE class="DataGrid_Header_Table_focus DataGrid_Body_Table_focus" width="100%" cellpadding="0" cellspacing="0" border="0" align="center">
            <thead>
                <tr class="Popup_Client_Selector_DataGrid_HeaderRow">
                    <th width="50px" class="DataGrid_Header_Cell"><%= Resource.getProperty("hcm.sno",lang)%></th>
                    <th width="30%" class="DataGrid_Header_Cell"><%= Resource.getProperty("hcm.code",lang)%></th>
                    <th width="30%" class="DataGrid_Header_Cell"><%= Resource.getProperty("hcm.name",lang)%></th>
                    <th width="30%" class="DataGrid_Header_Cell"><%= Resource.getProperty("hcm.description",lang)%></th>
                </tr>
            </thead>
            <tbody>
            <%
                int i = 0;
                for(ElementFormulaVariablesVO elementFormulaVO : elementFormulaList) { 
            %>
                <tr class="DataGrid_Body_Row DataGrid_Body_Row_<%= (i+1)%2 %>">
                    <td class="DataGrid_Body_Cell" style="text-align: center;"><span><%= (i+1) %></span></td>
                    <td class="DataGrid_Body_Cell" style="text-align: center;"><span class="text"><%= elementFormulaVO.getSearchKey() %></span></td>
                    <td class="DataGrid_Body_Cell" style="text-align: center;"><span class="text"><%= elementFormulaVO.getName() %></span></td>
                    <td class="DataGrid_Body_Cell" style="text-align: center;"><span class="text"><%= elementFormulaVO.getDescription() %></span></td>
                </tr>
            <%
                i++;
                }
            %>
            </tbody>
        </TABLE>
    </div>
    <BR>
    <div>
    	<TABLE class="Notes" cellspacing="0" cellpadding="0" border="0">
    		<TR>
    			<TD width="80px" valign="top">
    			Note 1 :
    			</TD>
    			<TD>
    			Global Values code can also be used in formula.<BR>
    			Corresponding values configured in 'Global Values' window will be applied.<BR>
    			</TD>
    		</TR>
    		<TR>
    			<TD  width="80px" valign="top">
    			Note 2 :
    			</TD>
    			<TD>
    			Values of other calculated elements can also be used in formula.<BR>
				Variable of that element must be Element Code in 'Element Type Definition' window prefixed by 'E_'.<BR>
				i.e 'E_'+&lt;Code&gt;<BR>
				For eg: BASIC - E_BASIC<BR>
				Make sure element value that has to be utilized in another element must have high priority than the utilizing element.<BR>
    			</TD>
    		</TR>
    	</TABLE>
    </div>
</div>
<div align="center" style="margin: 10px 0px;">
    <BUTTON type="button" id="submitButton" class="ButtonLink"  onclick="OnClosePopup()">
    <TABLE class="Button"><TR><TD class="Button_left"><IMG class="Button_Icon Button_Icon_ok" src="../web/images/blank.gif" border="0"></IMG></TD>
        <TD class="Button_text Button_width"><%= Resource.getProperty("hcm.close",lang)%></TD>
    <TD class="Button_right"></TD></TR></TABLE>
    </BUTTON>
</div>
</form>
</body>
</html>