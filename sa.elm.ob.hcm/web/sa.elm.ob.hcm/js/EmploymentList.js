/*
 *************************************************************************
 * All Rights Reserved.
 * Contributor(s): Gopalakrishnan
 *************************************************************************
*/
var xmlhttp = new getXMLObject(), currentTab = 'EMPINF',clickGrid='N';
var searchFlag = 0, onSearch = 0, onDelete = 0, gridW, gridH;
var formId = "748014FEDECF44D3BA89EDAB65573FE7";
var employmentGrid = jQuery("#EmploymentList"), lastActiveType = 'Y';
var statusValue=document.getElementById("strStatus").value;
var changeReasonValue=document.getElementById("strChangeReason").value;
//issuance dont allow to edit and delete
$( document ).ready(function() {
	if($('#inpIssuance').val()=='I' || $('#inpIssuance').val()=='TE' ){
	     document.getElementById('buttonNew').className = 'Main_ToolBar_Button_disabled';
	     document.getElementById('linkButtonNew').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled';
	     document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_disabled';
	     document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
	     document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
	     document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
	}
	if(document.getElementById('inpCancelHiring').value != "")
	{
     document.getElementById('LinkButtonCancel').className = 'OBToolbarIconButton_icon_undo OBToolbarIconButtonDisabled';
	}
	
   
   
	

	  $('#decisionDate').calendarsPicker({calendar:  
          $.calendars.instance('ummalqura'),dateFormat: 'dd-mm-yyyy',onSelect: function() {enableForm();},showTrigger:  
      '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">'});
});

 jQuery(function() {
    employmentGrid.jqGrid({
    	direction :direction,
        url : contextPath+'/EmploymentAjax?action=GetEmploymentList',
        colNames : [ 'Position Grade', 'Job No', 'Job Code','Department Code','Section Code','Employment Grade','Pay Scale',
                     'Grade Step','Start Date','End Date','Status','Change Reason'],
             colModel : [
                     {
                         name : 'grade',
                         index : 'grade',
                         width : 60
                     },
                     {
                         name : 'jobno',
                         index : 'jobno',
                         width : 60
                     },
                     {
                         name : 'jobcode',
                         index : 'jobcode',
                         width : 80
                     },
                     {
                         name : 'DepartmentCode',
                         index : 'DepartmentCode',
                         width : 80
                     },
                     {
                         name : 'sectioncode',
                         index : 'sectioncode',
                         width : 60
                     },
                     {
                         name : 'EmploymentGrade',
                         index : 'EmploymentGrade',
                         width : 80
                     },
                     {
                         name : 'Payscale',
                         index : 'Payscale',
                         width : 40
                     },
                     {
                         name : 'gradeStep',
                         index : 'gradeStep',
                         width : 50
                     },
                     {
                    	 name : 'startdate',
                    	 index : 'startdate',
                    	 width : 80,
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
                                        	 employmentGrid[0].triggerToolbar();
                                     }
                                 });
                                 e.style.width = "25%";
                                 setTimeout(
                                         "$('#gs_startdate').before('<select onchange=\"searchList(\\\'SD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_startdate_s\"><option value=\"=\">" + equalto + "</option><option value=\">=\">" + greaterthanequalto + "</option><option value=\"<=\">" + lessthanequalto + "</option></select>');",
                                         10);
                             }
                         }
                     },
                     {
                    	 name : 'enddate',
                    	 index : 'enddate',
                    	 width : 80,
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
                                        	 employmentGrid[0].triggerToolbar();
                                     }
                                 });
                                 e.style.width = "25%";
                                 setTimeout(
                                         "$('#gs_enddate').before('<select onchange=\"searchList(\\\'ED\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_enddate_s\"><option value=\"=\">" + equalto + "</option><option value=\">=\">" + greaterthanequalto + "</option><option value=\"<=\">" + lessthanequalto + "</option></select>');",
                                         10);                             }
                         }
                     
                     },
                    {
                         name: 'Status', index: 'Status', 	stype : 'select', width : 60,	searchoptions : {
                    		sopt : [ 'eq' ],
                    		value : statusValue,
                    		dataInit : function(e) {
                    			e.className = "Combo gs_org_e";
                    			e.style.padding = "0";
                    			e.style.margin = "2px 0 0";
                    			e.style.width = "95%";
                    			e.style.height = "18px";
                    		}
                    	} 
                     },
                     {
                     	name:'ChangeReason', index:'ChangeReason' , stype : 'select',width : 80,searchoptions : {
                    		sopt : [ 'eq' ],
                    		value : changeReasonValue,
                    		dataInit : function(e) {
                    			e.className = "Combo gs_org_e";
                    			e.style.padding = "0";
                    			e.style.margin = "2px 0 0";
                    			e.style.width = "95%";
                    			e.style.height = "18px";
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
        	enableButton("false");
        	var idValue = document.getElementById("inpEmploymentId").value;
        	employmentGrid.setSelection(idValue);
        	ChangeJQGridAllRowColor(employmentGrid);
        	$("#jqgrid").show();
        },
        onSelectRow : function(id) {
        	
            ChangeJQGridSelectRowColor(document.getElementById("inpEmploymentId").value, id);
            var rowData = employmentGrid.getRowData(id);
            document.getElementById("inpEmploymentId").value = id;
            enableButton("true");
            if(document.getElementById('inpCancelHiring').value == "true")
            {
             document.getElementById('LinkButtonCancel').className = 'OBToolbarIconButton_icon_undo OBToolbarIconButton';
        		}
            
        	if($('#inpIssuance').val()=='I'){
        		
       	     document.getElementById('buttonNew').className = 'Main_ToolBar_Button_disabled';
       	     document.getElementById('linkButtonNew').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled';
       	     document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_disabled';
       	     document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
       	     document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
       	     document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
       	}
        	if( $('#inpIssuance').val()=='TE'){
        		
          	     
          	     document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
          	     document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
          	}
        },
        ondblClickRow : function(id) {
        	if($('#inpIssuance').val()=='I'){
        		clickGrid='Y';
        	}
            var rowData = employmentGrid.getRowData(id);
            document.getElementById("inpEmploymentId").value = id;
            onClickEditView();
        },
        beforeRequest : function() {
        	 employmentGrid.setPostDataItem("inpEmployeeId", document.getElementById("inpEmployeeId").value);
        	 if ("" + employmentGrid.getPostDataItem("_search") == "true") {
                 if ("" + employmentGrid.getPostDataItem("startdate") != "") {
                     var date = "" + employmentGrid.getPostDataItem("startdate");
                     var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                     if (!validformat.test(date))
                    	 employmentGrid.setPostDataItem("startdate", "");
                 }
                 if ("" + employmentGrid.getPostDataItem("enddate") != "") {
                     var date = "" + employmentGrid.getPostDataItem("enddate");
                     var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                     if (!validformat.test(date))
                    	 employmentGrid.setPostDataItem("enddate", "");
                 }
                 employmentGrid.setPostDataItem("startdate_s", document.getElementById("gs_startdate_s").value);
                 employmentGrid.setPostDataItem("enddate_s", document.getElementById("gs_enddate_s").value);
          }
        	 employmentGrid.setPostDataItem("_search", 'true');

        }
    });
    employmentGrid.jqGrid('navGrid', '#pager', {
        edit : false,
        add : false,
        del : false,
        search : false,
        view : false
    }, {}, {}, {}, {});
   
    employmentGrid.jqGrid('filterToolbar', {
        searchOnEnter : false
    });
    employmentGrid[0].triggerToolbar();
    changeJQGridDisplay("EmploymentList", "inpEmployeeId");
}); 
/*function searchList(type) {
    if (type == "SD" && document.getElementById("gs_em_qhr_doj").value != "")
        employmentGrid[0].triggerToolbar();
}*/

function searchList(type) {
     if (type == "SD" && document.getElementById("gs_startdate").value != "")
    	 employmentGrid[0].triggerToolbar(); 
    else if (type == "ED" && document.getElementById("gs_enddate").value != "")
    	employmentGrid[0].triggerToolbar(); 
}

function reSizeGrid() {
    if (window.innerWidth) {
        gridW = window.innerWidth - 52;
        gridH = window.innerHeight - 260;
    }
    else if (document.body) {
        gridW = document.body.clientWidth - 52;
        gridH = document.body.clientHeight - 260;
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
    employmentGrid.setGridWidth(gridW, true);
    employmentGrid.setGridHeight(gridH, true);
}
function onClickEditView() {
    if ((document.getElementById('buttonEdition').className == 'Main_ToolBar_Button' && document.getElementById("inpEmploymentId").value != "") ||($('#inpIssuance').val()=='I')) {
    	if(document.getElementById('buttonEdition').className == 'Main_ToolBar_Button_disabled' && clickGrid !='Y'){
    		return false;
    	}
        document.getElementById("inpAction").value = "EditView";
        submitCommandForm('DEFAULT', true, null, 'Employment', '_self', null, true);
        return false;
    }
}
function onClickNew() {
	if(document.getElementById('buttonNew').className == 'Main_ToolBar_Button'){
		document.getElementById("inpEmploymentId").value = '';
	    document.getElementById("inpAction").value = "EditView";
	    submitCommandForm('DEFAULT', true, null, 'Employment', '_self', null, true);
	    return false;	
	}
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
    if (document.getElementById('buttonDelete').className == 'Main_ToolBar_Button' && document.getElementById("inpEmploymentId").value != "") {
        OBAsk(confirmdelete, function(result) {
            if (result) {

                xmlhttp = new getXMLObject();
                if (xmlhttp) {
                    var urlPath = "&inpEmploymentId=" + document.getElementById("inpEmploymentId").value;

                    xmlhttp.open("GET", contextPath+'/EmploymentAjax?action=DeleteEmployment' + urlPath, true);
                    xmlhttp.onreadystatechange = function() {
                        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                            var resource = xmlhttp.responseXML.getElementsByTagName("DeleteEmployment");
                            var response = resource[0].getElementsByTagName("Response")[0].childNodes[0].nodeValue;
                            if (response == 'false') {
                                displayMessage("E", error, deletefailure)
                            }
                            else {
                                displayMessage("S", success, deletesuccess);
                                document.getElementById("inpEmploymentId").value = "";
                                employmentGrid.trigger("reloadGrid");
                            }
                        }
                    };
                    xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                    xmlhttp.send(null);
                }
            }
            else
                document.getElementById(document.getElementById("inpEmploymentId").value).focus();
        });
    }
}
function onClickRefresh() {
    document.getElementById("inpAction").value = "GridView";
    reloadTab('EMPINF');
}
function reloadWindow(tab) {
    if (tab == 'EMPINF') {
        submitCommandForm('DEFAULT', true, null, 'Employee', '_self', null, true);
        return false;
    }
/*     else (tab == 'EMPI') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/com.qualiantech.hr.ad_forms.employeesetup.identity.header/Identity', '_self', null, true);
        return false;
    }
 */
    return false;
}


function setTabClass(tab, type) {
    if (type == true)
        tab.className = "tabSelectedHOVER";
    else
        tab.className = "tabSelected";
}
function reloadTab(tab) {
	var url="";
	document.getElementById("inpEmpNo").value=document.getElementById("inpEmpNo").value;
    if (tab == 'EMP') {
     // 	submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView', '_self', null, true);
     //   return false;
 	document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView' + url;
     document.frmMain.submit();
    }
    else if (tab == 'EMPINF') {
    	document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employment.header/Employment' + url;
        document.frmMain.submit();
    }
    else if (tab == 'Dependent') {
   	 var url="";
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpAction=GridView&inpEmployeeId='+employeeId;
        document.frmMain.submit();
   }
    else if (tab == 'Qualification') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification', '_self', null, true);
        return false;
    }
    else if(tab == 'Asset'){
    	submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset', '_self', null, true);
        return false;
    }
    else if (tab == 'EMPCTRCT') {
        var url="";
          var employeeId=document.getElementById("inpEmployeeId").value;
          document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
          document.frmMain.submit();
     }
    else if (tab == 'EMPADD') {
    	document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView';
        document.frmMain.submit();
    }
    else if (tab == 'PREEMP') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
         return false;
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
}
