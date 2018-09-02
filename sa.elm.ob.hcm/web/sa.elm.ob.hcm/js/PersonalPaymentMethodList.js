 
var onSearch=0;
function onClickRefresh() {
	//PaymentMethodList
	 //   document.getElementById("inpEmployeeId").value = "";
	    //document.getElementById("inpIsActive").value = "";
	  //  document.getElementById("inpAction").value = "GridView";
	    //resetTab();
	    //reloadWindow('EMP');
	  //  reloadTab('PERPAYMETHOD');
	    document.getElementById("inpAction").value = "GridView";
	    resetTab();
	    reloadTab('PERPAYMETHOD');
	}
function resetTab() {
	   //resetByTab(document.getElementById("inpehcmPersonalPaymethdId"));
	 // document.getElementById("inpEmployeeId").value = "";
	 // document.getElementById("inpIsActive").value = ""; 
	}
function resetByTab(tab) {
    tab.className = "tabNotSelected";
    tab.parentNode.replaceChild(tab.cloneNode(true), tab);
    $('#' + tab.id).attr('onmouseover', "").attr('onmouseout', "").attr('onclick', "");
}
 function reSizeGrid() {
	    if (window.innerWidth) {
	        gridW = window.innerWidth - 52;
	        gridH = window.innerHeight - 270;
	    }
	    else if (document.body) {
	        gridW = document.body.clientWidth - 52;
	        gridH = document.body.clientHeight - 270;
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
	    PaymentMethodListGrid.setGridWidth(gridW, true);
	    PaymentMethodListGrid.setGridHeight(gridH, true);
	}

//Load Payment Method Grid
var paymentmethodUrl = "&inpEmployeeId=" + document.getElementById("inpEmployeeId").value;

 jQuery(function () {
 PaymentMethodListGrid.jqGrid({
	 direction :direction,
     url: contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=GetPaymentMethodList' + paymentmethodUrl,
     colNames: ['Payment Type Code', 'Payment Type Name', 'Currency','Default'],
     colModel: [{
       name: 'Payment_Type_Code',
       index: 'Payment_Type_Code',
       width: 100
       
     }, {
       name: 'Payment_Type_Name',
       index: 'Payment_Type_Name',
       width: 40
     }, 
     {
         name: 'Currency',
         index: 'Currency',
         width: 40
       },
    {
        name: 'Default',
        index: 'Default',
         width: 40,
         editable: true,
         edittype: 'checkbox', 
       	stype : 'select',
         searchoptions : {
      		sopt : [ 'eq' ],
             value : '0:'+select+';Y:'+yes+';N:'+no,
             dataInit : function(e) {
                 e.className = "Combo gs_Default_e";
                 e.style.padding = "0";
                e.style.margin = "2px 0 0";
                e.style.width = "95%";
                 e.style.height = "18px";
             }
         }}],
       rowNum : 50,
       mtype: 'POST',
       rowList : [ 20, 50, 100, 200, 500 ],
       pager : '#pager',
       sortname : 'Payment_Type_Code',
       datatype : 'xml',
       rownumbers : true,
       viewrecords : true,
       sortorder : "asc",
       scrollOffset : 17,
  //     onSelectRow : function(id) {
     /*      ChangeJQGridSelectRowColor(document.getElementById("inpEmployeeId").value, id);
           var rowData = employeeGrid.getRowData(id);
           document.getElementById("inpEmployeeId").value = rowData['employeeid'];
           document.getElementById("inpName1").value = rowData['name'];
           document.getElementById("inpArabicName").value = rowData['arabicname'];
           document.getElementById("inpCode").value = rowData['value'];
           document.getElementById("inpIsActive").value = rowData['isactive'];
           document.getElementById("inpempCategory").value = rowData['category'];
           document.getElementById("inpChangereason").value = rowData['changereason'];
           document.getElementById("inpEmployementstatus").value = rowData['employementstatus'];
           document.getElementById("inpDelecationcount").value = rowData['delecationcount'];
           document.getElementById("inpEmpStatus").value = id;
           document.getElementById("inpEmployeeStatus").value = rowData['shortstatus'];
           enableTabs();
           enableButton("true");
           enableQDMAttachmentIcon(id, formId, true, '<%=request.getContextPath()%>');
           document.getElementById("inpAddressId").value= rowData['addressid'];
           if(rowData['shortstatus']=='I'){
               if(document.getElementById("inpChangereason").value=='H' && document.getElementById("inpEmployementstatus").value == 'ACT' && document.getElementById("inpDelecationcount").value == 0){
                   CheckAlreadyCancel(rowData['value']);
               }
               else
                   document.getElementById("cancelButton").style.display="none";

               document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
               $('#buttonDelete').attr("class", "Main_ToolBar_Button_disabled");
           }
           else if(rowData['shortstatus']=='TE'){
               document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
               $('#buttonDelete').attr("class", "Main_ToolBar_Button_disabled");
           }
           else{
               document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete';
               $('#buttonDelete').attr("class", "Main_ToolBar_Button");
               document.getElementById("cancelButton").style.display="none"; 
           }
          */
           
    //   },
       onSelectRow: function (id) {
           var rowData = jQuery("#PaymentMethodList").getRowData(id);
           document.getElementById("inpehcmPersonalPaymethdId").value = id;
           enableButton("true");
         },
       ondblClickRow : function(id) {
    	       var rowData = jQuery("#PaymentMethodList").getRowData(id);
           document.getElementById("inpehcmPersonalPaymethdId").value = id;
           onClickEditView();
          
//           var rowData = employeeGrid.getRowData(id);
//           document.getElementById("inpEmployeeId").value = rowData['employeeid'];
//           document.getElementById("inpIsActive").value = rowData['isactive'];
//           document.getElementById("inpEmpStatus").value = id;
//           document.getElementById("inpEmployeeStatus").value = rowData['shortstatus'];
//           
//           onClickEditView();
       },
    //   beforeRequest : function() {
    /*        resetTab();
           "" + employeeGrid.jqGrid('getGridParam', 'postData')['_search']
           if ("" + employeeGrid.getPostDataItem("_search") == "true" && onDelete == 0){
               document.getElementById("inpEmployeeId").value = "";
               document.getElementById("inpIsActive").value = "";
           }
           
           employeeGrid.setPostDataItem("inpEmployeeId", document.getElementById("inpEmployeeId").value);
           if (onDelete == 0)
               employeeGrid.setPostDataItem("DeleteEmployee", "");

           if ("" + employeeGrid.getPostDataItem("_search") == "true") {
               if ("" + employeeGrid.getPostDataItem("hiredate") != "") {
                   var date = "" + employeeGrid.getPostDataItem("hiredate");
                   var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                   if (!validformat.test(date))
                       employeeGrid.setPostDataItem("hiredate", "");
               }
               employeeGrid.setPostDataItem("hiredate_s", document.getElementById("gs_hiredate_s").value);
           }
           employeeGrid.setPostDataItem("_search", 'true');*/
     //  },
       onSortCol : function(index, columnIndex, sortOrder) {
       /*    document.getElementById("inpEmployeeId").value = "";
           document.getElementById("inpIsActive").value = "";
           resetTab();*/
       },
       onPaging : function() {
        /*   document.getElementById("inpEmployeeId").value = "";
           document.getElementById("inpIsActive").value = "";
           resetTab();*/
       },
       loadComplete : function() {
           /*enableButton("false");
           var idList = employeeGrid.getDataIDs();
           var idCount = idList.length;
           var idValue = document.getElementById("inpEmployeeId").value;

           if (idValue != "") {
               if (onDelete == 1) {
                   var flag = 0;
                   if (flag == 0) {
                       displayMessage("S", '<%=Resource.getProperty("hcm.success",lang)%>', '<%= Resource.getProperty("hcm.deletesuccess", lang) %>');
                       document.getElementById("inpEmployeeId").value = "";
                       document.getElementById("inpIsActive").value = "";
                       ChangeJQGridSelectRowColor(idValue, "");
                   }
                   onDelete = 0;
               }
               else {
                   employeeGrid.setSelection(idValue);
                   enableQDMAttachmentIcon(idValue, formId, true, '<%=request.getContextPath()%>');
               }
           }
           else
               enableQDMAttachmentIcon('', formId, false, '');
           ChangeJQGridAllRowColor(employeeGrid);
           document.getElementById("gs_isactive").value = lastActiveType;
           $("#LoadingContent").hide();
           $("#jqgrid").show();
           if (idCount > 0)
               employeeGrid.trigger("resize");
           setTimeout(function() {
               if (document.getElementById("inpEmployeeId").value.length == 32) {
                   employeeGrid.closest(".ui-jqgrid-bdiv").scrollTop(22 * (employeeGrid.getInd(document.getElementById("inpEmployeeId").value)-2));
               }
           }, 100);*/
    	   reSizeGrid();
       }
       
//       beforeRequest: function () {
//     	  jQuery("#PaymentMethodList").setPostDataItem("inpEmployeeId", document.getElementById("inpEmployeeId").value);
//         if ("" + jQuery("#PaymentMethodList").getPostDataItem("_search") == "true") {
//           if ("" + jQuery("#PaymentMethodList").getPostDataItem("startdate") != "") {
//             var date = "" + jQuery("#PaymentMethodList").getPostDataItem("startdate");
//             var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
//             if (!validformat.test(date)) jQuery("#PaymentMethodList").setPostDataItem("startdate", "");
//           }
//           if ("" + jQuery("#PaymentMethodList").getPostDataItem("enddate") != "") {
//             var date = "" + jQuery("#PaymentMethodList").getPostDataItem("enddate");
//             var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
//             if (!validformat.test(date)) jQuery("#PaymentMethodList").setPostDataItem("enddate", "");
//           }
//           jQuery("#PaymentMethodList").setPostDataItem("startdate_s", document.getElementById("gs_startdate_s").value);
//           jQuery("#PaymentMethodList").setPostDataItem("enddate_s", document.getElementById("gs_enddate_s").value);
//         }
//         jQuery("#PaymentMethodList").setPostDataItem("_search", 'true');
//
//       }
   });
 PaymentMethodListGrid.jqGrid('navGrid', '#pager', {
       edit : false,
       del : false,
       search : false,
       view : false,
       add : false,
       beforeRefresh : function() {
          // document.getElementById("gs_hiredate").value = '=';
           if (onSearch == 1) {
        	   PaymentMethodListGrid[0].clearToolbar();
        	   PaymentMethodListGrid[0].toggleToolbar();
           }
           onSearch = 0;
           searchFlag = 0;
           resetTab();
           reSizeGrid();
       }
   }, {}, {}, {}, {});
  
 PaymentMethodListGrid.jqGrid('filterToolbar', {
       searchOnEnter : false
   });
  PaymentMethodListGrid[0].triggerToolbar();
   //changeJQGridDisplay("EmployeeList", "inpEmployeeId");
});
 
function reloadTab(tab) {
		    if (tab == 'EMP') {
		      //  submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employee.header/Employee', '_self', null, true);
		      //  return false;
		     	var url="";
		     	document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView' + url;
		        document.frmMain.submit();
		    }
		    else if (tab == 'EMPINF') {
		        var url="";
		        var employeeId=document.getElementById("inpEmployeeId").value;
		        document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView&inpEmployeeId='+employeeId;
		        document.frmMain.submit();
		    }
		    else if(tab =='EMPADD'){
		              document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView';
		                document.frmMain.submit();
		            }
		    else if(tab == 'Dependent') {
		           var url="";
		           var employeeId=document.getElementById("inpEmployeeId").value;
		           document.frmMain.action = contextPath +'/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpAction=GridView&inpEmployeeId='+employeeId;
		           document.frmMain.submit();
		           }
		    else if (tab == 'EMPQUAL') {
		          submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification', '_self', null, true);
		          return false;
		     }
		   else if (tab == 'PREEMP') {
		          submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
		          return false;
		    }
		  else if (tab == 'EMPCTRCT') {
		         var url="";
		         var employeeId=document.getElementById("inpEmployeeId").value;
		         document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
		         document.frmMain.submit();
		   }
		    
		  else if(tab == 'Asset') {
		        var employeeId=document.getElementById("inpEmployeeId").value;
		        document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset?inpAction=GridView&inpEmployeeId='+employeeId;
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
    }
function onClickNew() {
	if( document.getElementById('linkButtonNew').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled')
    {
       return false;
    }
    document.getElementById("inpAction").value = "EditView";
    document.getElementById("inpehcmPersonalPaymethdId").value = "";
    //document.getElementById("inpIsActive").value = "";
    submitCommandForm('DEFAULT', true, null, 'PersonalPaymentMethod', '_self', null, true);
    return false;
}
function onClickEditView() {
    if (document.getElementById('buttonEdition').className == 'Main_ToolBar_Button' && document.getElementById("inpehcmPersonalPaymethdId").value != "") {
        document.getElementById("inpAction").value = "EditView";
        submitCommandForm('DEFAULT', true, null, 'PersonalPaymentMethod', '_self', null, true);
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
    if (document.getElementById('buttonDelete').className == 'Main_ToolBar_Button' && document.getElementById("inpehcmPersonalPaymethdId").value != "") {
        OBAsk(confirmdelete, function(result) {
            if (result) {

                xmlhttp = new getXMLObject();
                if (xmlhttp) {
                    var urlPath = "&inpehcmPersonalPaymethdId=" + document.getElementById("inpehcmPersonalPaymethdId").value;

                    xmlhttp.open("GET", contextPath+'/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=deletePersonalPayment' + urlPath, true);
                    xmlhttp.onreadystatechange = function() {
                        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                            var resource = xmlhttp.responseXML.getElementsByTagName("deletePersonalPayment");
                            var response = resource[0].getElementsByTagName("Response")[0].childNodes[0].nodeValue;
                            if (response == 'false') {
                                displayMessage("E", error, deletefailure);
                            }
                            else {
                                displayMessage("S", success, deletesuccess);
                                document.getElementById("inpehcmPersonalPaymethdId").value = "";
                                PaymentMethodListGrid.trigger("reloadGrid");
                            }
                        }
                    };
                    xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                    xmlhttp.send(null);
                }
            }
            else
                document.getElementById(document.getElementById("inpehcmPersonalPaymethdId").value).focus();
        });
    }
}
