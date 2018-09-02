/*
 *************************************************************************
 * All Rights Reserved.
 * Contributor(s): Gopalakrishnan
 *************************************************************************
*/
var xmlhttp = new getXMLObject();
var searchFlag = 0, onSearch = 0, onDelete = 0, gridW, gridH;
var formId = "748014FEDECF44D3BA89EDAB65573FE7";
var EmploymentGrpGrid = jQuery("#EmploymentGroupList"), lastActiveType = 'Y';

 jQuery(function() {
    EmploymentGrpGrid.jqGrid({
    	direction :direction,
        url : contextPath+'/EmploymentGroupAjax?action=GetEmploymentGroupList',
        colNames : [ 'Code', 'Name', 'Start Date','End Date'],
        colModel : [
                {
                    name : 'Code',
                    index : 'Code',
                    width : 60
                },
                {
                    name : 'Name',
                    index : 'Name',
                    width : 60
                },
                {
                    name : 'StartDate',
                    index : 'StartDate',
                    align : 'center',
                    width : 100,
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
                                	   EmploymentGrpGrid[0].triggerToolbar();
                               }
                           });
                           e.style.width = "25%";
                           setTimeout(
                                   "$('#gs_StartDate').before('<select onchange=\"searchList(\\\'SD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_StartDate_s\"><option value=\"=\">Equal to</option><option value=\">=\">Greater Than or Equal to</option><option value=\"<=\">Less than or Equal To</option></select>');",
                                   10);
                       }
                   }
                },
                {
                    name : 'enddate',
                    index : 'enddate',
                    align : 'center',
                    width : 100,
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
                                	   EmploymentGrpGrid[0].triggerToolbar();
                               }
                           });
                           e.style.width = "25%";
                           setTimeout(
                                   "$('#gs_enddate').before('<select onchange=\"searchList(\\\'LD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_enddate_s\"><option value=\"=\">Equal to</option><option value=\">=\">Greater than or Equal to</option><option value=\"<=\">Lesser than or Equal to</option></select>');",
                                   10);
                          
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
        loadComplete : function() {
        	var idValue = document.getElementById("inpEmploymentGrpId").value;
        	EmploymentGrpGrid.setSelection(idValue);
        	ChangeJQGridAllRowColor(EmploymentGrpGrid);
        	$("#jqgrid").show();
        },
        onSelectRow : function(id) {
           ChangeJQGridSelectRowColor(document.getElementById("inpEmploymentGrpId").value, id);
            var rowData = EmploymentGrpGrid.getRowData(id);
            document.getElementById("inpEmploymentGrpId").value = id;
            enableButton("true");
            if(rowData['status']=='Issued'){
            	 document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
          	     document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
            	$("#cancelButton").show();
            }
            else{
            	 document.getElementById('buttonDelete').className = 'Main_ToolBar_Button';
          	     document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete';
            	 $("#cancelButton").hide();
            	
            }
        },
        ondblClickRow : function(id) {
        	var rowData = EmploymentGrpGrid.getRowData(id);
            document.getElementById("inpEmploymentGrpId").value = id;
            onClickEditView();
        },
        beforeRefresh : function() {
            onSearch = 0;
            searchFlag = 0;
            reSizeGrid();
        },
        beforeRequest : function() {
            if ("" + EmploymentGrpGrid.getPostDataItem("_search") == "true") {
                   if ("" + EmploymentGrpGrid.getPostDataItem("StartDate") != "") {
                       var date = "" + EmploymentGrpGrid.getPostDataItem("StartDate");
                       var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                       if (!validformat.test(date))
                       	EmploymentGrpGrid.setPostDataItem("StartDate", "");
                   }
                   if ("" + EmploymentGrpGrid.getPostDataItem("enddtate") != "") {
                       var date = "" + EmploymentGrpGrid.getPostDataItem("enddtate");
                       var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                       if (!validformat.test(date))
                       	EmploymentGrpGrid.setPostDataItem("enddtate", "");
                   }
                   EmploymentGrpGrid.setPostDataItem("startdate_s", document.getElementById("gs_StartDate_s").value);
                   EmploymentGrpGrid.setPostDataItem("enddate_s", document.getElementById("gs_enddate_s").value);

            }
             EmploymentGrpGrid.setPostDataItem("_search", 'true');
        }
    });
    EmploymentGrpGrid.jqGrid('navGrid', '#pager', {
        edit : false,
        add : false,
        del : false,
        search : false,
        view : false
    }, {}, {}, {}, {});
   
    EmploymentGrpGrid.jqGrid('filterToolbar', {
        searchOnEnter : false
    });
    EmploymentGrpGrid[0].triggerToolbar();
    changeJQGridDisplay("EmploymentGroupList", "inpEmploymentGrpId");
}); 
 function searchList(type) {
	  if (type == "LD" && document.getElementById("gs_letterDate").value != "") EmploymentGrpGrid[0].triggerToolbar();
	  if (type == "SD" && document.getElementById("gs_StartDate").value != "") EmploymentGrpGrid[0].triggerToolbar();
	  if (type == "ED" && document.getElementById("gs_expiryDate").value != "") EmploymentGrpGrid[0].triggerToolbar();
	}

function reSizeGrid() {
    if (window.innerWidth) {
        gridW = window.innerWidth - 52;
        gridH = window.innerHeight - 241;
    }
    else if (document.body) {
        gridW = document.body.clientWidth - 52;
        gridH = document.body.clientHeight - 241;
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
    EmploymentGrpGrid.setGridWidth(gridW, true);
    EmploymentGrpGrid.setGridHeight(gridH, true);
}
function onClickEditView() {
    if ((document.getElementById('buttonEdition').className == 'Main_ToolBar_Button' && document.getElementById("inpEmploymentGrpId").value != "") ||($('#inpIssuance').val()=='I')) {
        document.getElementById("inpAction").value = "EditView";
        submitCommandForm('DEFAULT', true, null, 'Contract', '_self', null, true);
        return false;
    }
}
function onClickNew() {
	document.getElementById("inpEmploymentGrpId").value = '';
    document.getElementById("inpAction").value = "EditView";
    submitCommandForm('DEFAULT', true, null, 'Contract', '_self', null, true);
    return false;
}

function enableButton(flag) {
    if (flag == "true" && document.getElementById('buttonEdition').className != 'Main_ToolBar_Button') {
        document.getElementById('buttonEdition').className = 'Main_ToolBar_Button';
        document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition';
        document.getElementById('buttonDelete').className = 'Main_ToolBar_Button';
        document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete';
    }
    else if (flag == 'false') {
        document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_disabled';
        document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
        document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
        document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
    }
}
function onClickDelete() {
    if (document.getElementById('buttonDelete').className == 'Main_ToolBar_Button' && document.getElementById("inpEmploymentGrpId").value != "") {
        OBAsk(confirmdelete, function(result) {
            if (result) {

                xmlhttp = new getXMLObject();
                if (xmlhttp) {
                    var urlPath = "&inpEmploymentGrpId=" + document.getElementById("inpEmploymentGrpId").value;

                    xmlhttp.open("GET", contextPath+'/ContractAjax?action=DeleteContract' + urlPath, true);
                    xmlhttp.onreadystatechange = function() {
                        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                            var resource = xmlhttp.responseXML.getElementsByTagName("DeleteContract");
                            var response = resource[0].getElementsByTagName("Response")[0].childNodes[0].nodeValue;
                            if (response == 'false') {
                                displayMessage("E", error, deletefailure);
                                document.getElementById("inpEmploymentGrpId").value = "";
                                document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
                                document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
                            }
                            else {
                                displayMessage("S", success, deletesuccess);
                                document.getElementById("inpEmploymentGrpId").value = "";
                                document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
                                document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
                                EmploymentGrpGrid.trigger("reloadGrid");
                            }
                        }
                    };
                    xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                    xmlhttp.send(null);
                }
            }
            else
                document.getElementById(document.getElementById("inpEmploymentGrpId").value).focus();
        });
    }
}
function onClickRefresh() {
    var url="";
	document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.EmploymentGroup.header/EmploymentGroup?inpAction=GridView' + url;
    document.frmMain.submit();
}
//issuance
function cancelContract(){
	OBAsk(askCorrection, function(result) {
		if(result){
			 document.getElementById("SubmitType").value="CancelContract";
			 document.getElementById("inpTrxStatus").value="UP";
			 var url="";
		     var employeeId=document.getElementById("inpEmployeeId").value;
		     document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
		     document.frmMain.submit();	
		}
	     else{
	 		EmploymentGrpGrid.trigger('reloadGrid');
	 	}
	});
}
