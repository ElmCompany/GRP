 function reSizeGrid() {
    if (window.innerWidth) {
        gridW = window.innerWidth - 52;
        gridH = window.innerHeight - 350;
    }
    else if (document.body) {
        gridW = document.body.clientWidth - 52;
        gridH = document.body.clientHeight - 350;
    }
    if (gridW < 800)
        gridW = 800;
    if (gridH < 200)
        gridH = 200;
    bankdetailGrid.setGridWidth(gridW, true);
    bankdetailGrid.setGridHeight(gridH, true);

}
 $( document ).ready(function() {
	    if($('#inpEmployeeIsActive').val()=="false"){
	    	$("#inppaycode").prop("disabled", true);
	    	$("#inppayname").prop("disabled", true);
	    	$("#inppaycurrency").prop("disabled", true);
	    $('#inpdefaultflag').prop('disabled',true);
	    }
	});
 function onClickRefresh() {
	 
	    document.getElementById("inpAction").value = "EditView";
	    //reloadTab(currentTab);

	    if ((changesFlag == 1) && $("#inppaycode").val() != '0'  && $("#inppayname").val() != '0' && $("#inppaycurrency").val() != '0' && $("#inppaycurrency").val() != '') 
	        OBAsk(changedvaluessave, function(result) {
	            if (result){
	               if (savevaliddata()) {
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
	        reloadTab('PERPAYMETHD');
	}
 
 function reloadWindow(tab) {
     //hideMessage();
      if (changesFlag == 1) OBAsk(changedvaluessave, function (result) {
        if (result) reloadTabSave(tab);
        else {
          reloadTab(tab);
        }
      });
      else reloadTab(tab);
}

 function reloadTab(tab) {

	    if (tab == 'EMP') {
	 //   	submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employee.header/Employee', '_self', null, true);
	   //     return false;
	    	var url="";
	    	document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView' + url;
	        document.frmMain.submit();
	    }
	    else if (tab == 'EMPINF') {
	        var url="";
	        document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView' + url;
	        document.frmMain.submit();
	    }
	    else if (tab=='EMPADD') {
	                document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView';
	               document.frmMain.submit();
	            }
	   else if (tab == 'Dependent') {
	        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents', '_self', null, true);
	        return false;
	    }
	   else if (tab == 'EMPCTRCT') {
	       var url="";
	         var employeeId=document.getElementById("inpEmployeeId").value;
	         document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
	         document.frmMain.submit();
	    }
	    else if (tab == 'EMPQUAL') {
	        var url="";
	        document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification?inpAction=GridView' + url;
	        document.frmMain.submit();
	    }
	    else if(tab == 'Asset'){
	        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset', '_self', null, true);
	        return false;
	    }
	    else if (tab == 'PREEMP') {
	        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
	         return false;
	    }
	    else if (tab == 'DOC') {
	        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.documents.header/Documents', '_self', null, true);
	         return false;
	    }
	    else if (tab == 'PERPAYMETHD') {
            submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.header/PersonalPaymentMethod', '_self', null, true);
             return false;
        }
	    else if (tab == 'MEDIN') {
	        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.MedicalInsurance.header/MedicalInsurance', '_self', null, true);
	         return false;
	    }
	}
 
//Load Bank Details Grid
 var bankValue=document.getElementById("strbank").value;
 var branchValue=document.getElementById("strbranch").value;
// var branchalue= document.getElementById("strbranch").value
 //var branchalue=" 0:select;58D20D7073CF46D2B23867E7D836EFF9:gopal";
 var bankdetailUrl = "&inpehcmPersonalPaymethdId=" + document.getElementById("inpehcmPersonalPaymethdId").value;
 bankdetailUrl += "&inpEmployeeId=" + document.getElementById("inpEmployeeId").value;
 jQuery(function () {
  bankdetailGrid.jqGrid({
	  direction :direction,
     url: contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=getbankdetaillist' + bankdetailUrl,
     colNames: ['Bank Name', 'Bank Branch', 'Account Name', 'Account Number', 'Percentage', 'Start Date', 'End Date', 'Default'],
     colModel: [{
       name: 'Bank_Name',
       index: 'Bank_Name',
       width: 160,
       editable: true,
       edittype: 'select', 
       formatter:'select',
/*       editoptions:{value: bankValue,
    	   dataInit: function(elem) {
    	        $(elem).width(400);
    	        fn:getBankBranch
    	    },
    	    dataEvents :[{
    	    	fn:getBankBranch
    	    }]}*/
     

     editoptions: {
    	        value: bankValue,
    	    	   dataInit: function(elem) {
    	    	        $(elem).width(200);
    	    	    },
    	        dataEvents: [{
    	          type: 'change',
    	          fn: getBankBranch
    	        }]
    	      }
     },
     {
     name: 'Bank_Branch',
     index: 'Bank_Branch',
     width: 160,
     editable: true,
     edittype: 'select', 
     formatter:'select',
   editoptions: {
  	        value: document.getElementById("strbranch").value,
  	      dataInit: function(elem) {
  	        $(elem).width(200);
  	    },
  	     
  	      }
   },
     {
         name: 'Account_Name',
         index: 'Account_Name',
         width: 40,
         editable: true
       },
       {
           name: 'Account_Number',
           index: 'Account_Number',
           width: 40,
           editable: true,
           //formatter: 'integer'
         },
       {
           name: 'Percentage',
           index: 'Percentage',
           width: 40,
           editable: true
         },
         {
             name: 'Start_Date',
             index: 'Start_Date',
             width: 50,
             editable: true,
            // formatter:'date',
             editoptions:{ dataInit : function(e) {
                 $(e).calendarsPicker({
                     calendar: $.calendars.instance('ummalqura'),
                     dateFormat: 'dd-mm-yyyy',
                     showTrigger:  
                         '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
                     changeMonth : true,
                     changeYear : true,
                     onClose : function(dateText, inst) {
                   
//                        if (dateText != "")
//                        	 bankdetailGrid[0].triggerToolbar();
                     }
                 });
                 e.style.width = "40%";
            }},
            searchoptions:{ dataInit : function(e) {
                $(e).calendarsPicker({
                    calendar: $.calendars.instance('ummalqura'),
                    dateFormat: 'dd-mm-yyyy',
                    showTrigger:  
                        '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
                    changeMonth : true,
                    changeYear : true,
                    onClose : function(dateText, inst) {
                  
                      if (dateText != "")
                       	 bankdetailGrid[0].triggerToolbar();
                    }
                });
                e.style.width = "40%";
                setTimeout(
                        "$('#gs_Start_Date').before('<select onchange=\"searchList(\\\'SD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_Start_Date_s\"><option value=\"=\">" + equalto + "</option><option value=\">=\">" + greaterthanequalto + "</option><option value=\"<=\">" + lessthanequalto + "</option></select>');",
                        10);
           }},
            
           },
           {
               name: 'End_Date',
               index: 'End_Date',
               width: 40,
               editable: true,
              // formatter:'date',
               editoptions:{ dataInit : function(e) {
         	      $(e).calendarsPicker({
         	          calendar: $.calendars.instance('ummalqura'),
         	          dateFormat: 'dd-mm-yyyy',
         	          showTrigger:  
         	              '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
         	          changeMonth : true,
         	          changeYear : true,
         	          onClose : function(dateText, inst) {
         	             // if (dateText != "")
         	            	 // bankdetailGrid[0].triggerToolbar();
         	          }
         	      });
         	      e.style.width = "40%";
         	     
         	 }},
               searchoptions:{ dataInit : function(e) {
            	      $(e).calendarsPicker({
            	          calendar: $.calendars.instance('ummalqura'),
            	          dateFormat: 'dd-mm-yyyy',
            	          showTrigger:  
            	              '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
            	          changeMonth : true,
            	          changeYear : true,
            	          onClose : function(dateText, inst) {
            	             if (dateText != "")
            	            	bankdetailGrid[0].triggerToolbar();
            	          }
            	      });
            	      e.style.width = "40%";
            	      setTimeout(
            	              "$('#gs_End_Date').before('<select onchange=\"searchList(\\\'SD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_End_Date_s\"><option value=\"=\">" + equalto + "</option><option value=\">=\">" + greaterthanequalto + "</option><option value=\"<=\">" + lessthanequalto + "</option></select>');",
            	              10);
            	 }}
             
             },

     {
       name: 'Default',
       index: 'Default',
       width: 30,
       editable: true,
       edittype: 'checkbox', 
     //	stype : 'select',
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
       },
       editoptions: { value: "True:False" }, 
       formatter: "checkbox",
     //  formatoptions: { disabled: true}
     }],
     rowNum: 50,
     mtype: 'POST',
     rowList: [20, 50, 100, 200, 500],
     pager: '#pager',
     sortname: 'Bank_Name',
     datatype: 'xml',
     rownumbers: true,
     viewrecords: true,
     sortorder: "asc",
     //scrollOffset: 17,
     editurl: contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=SaveBankDetailList' + bankdetailUrl,
     loadComplete: function () {
    	 if($('#inpEmployeeIsActive').val()=="false"){
    	 $("#bankdetailslist_iladd").addClass('ui-state-disabled');
    	 $("#del_bankdetailslist").addClass('ui-state-disabled');
    	 $("#bankdetailslist_iledit").addClass('ui-state-disabled');
    	 $("#bankdetailslist_ilsave").addClass('ui-state-disabled');
    	 $("#bankdetailslist_ilcancel").addClass('ui-state-disabled');
    	 }
    	 $("#new_row_Bank_Name").css("width", "500");
      // $("#pager_left").css("width", "");
       if ($('#inpTrxStatus').val() == 'ISS') {
         $("#del_getbankdetaillist").addClass('ui-state-disabled');
         $("#refresh_getbankdetaillist").addClass('ui-state-disabled');
         $("#getbankdetaillist_iladd").addClass('ui-state-disabled');
         $("#getbankdetaillist_iledit").addClass('ui-state-disabled');
       }
     },
    onSelectRow: function(id){ 
    	 getbankBranchOnSelection(id);
     },
  beforeRequest: function () {
	  //alert( document.getElementById("strbranch").value);
   //$("#bankdetailslist").jqGrid('setColProp', 'Bank_Branch', { editoptions: { value: document.getElementById("strbranch").value} });
	  getbankbranchOnSelectionOfBank();
  jQuery("#bankdetailslist").setPostDataItem("inpEmployeeId", document.getElementById("inpEmployeeId").value);
    if ("" + jQuery("#bankdetailslist").getPostDataItem("_search") == "true") {
      if ("" + jQuery("#bankdetailslist").getPostDataItem("Start_Date") != "") {
        var date = "" + jQuery("#bankdetailslist").getPostDataItem("Start_Date");
        var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
        if (!validformat.test(date)) jQuery("#bankdetailslist").setPostDataItem("Start_Date", "");
      }
      if ("" + jQuery("#bankdetailslist").getPostDataItem("End_Date") != "") {
        var date = "" + jQuery("#bankdetailslist").getPostDataItem("End_Date");
        var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
        if (!validformat.test(date)) jQuery("#bankdetailslist").setPostDataItem("End_Date", "");
      }
      jQuery("#bankdetailslist").setPostDataItem("Start_Date_s", document.getElementById("gs_Start_Date_s").value);
     jQuery("#bankdetailslist").setPostDataItem("End_Date_s", document.getElementById("gs_End_Date_s").value);
    }
   jQuery("#bankdetailslist").setPostDataItem("_search", 'true');

  }
   });
 
 
  bankdetailGrid.jqGrid('navGrid', '#pager', {
     edit: false,
     add: false,
     del: true,
     search: false,
     view: false,
     delfunc: function (rowids) {
	      deleterow(rowids);
	    }
   }, {}, {}, {}, {});
  bankdetailGrid.jqGrid('inlineNav', '#pager', {
	   edit: true,
	    add: true,
	   editParams: {
		      oneditfunc: function (rowid, response, options) {
		        saveFlag = 1;		     
		      },
		      beforeSaveRow: function (options, rowid) {	
		        OBConfirm(askSave, function (a) {
		          if (a) {
		            successflag = saverow(options, rowid);
		          }
		        });
		        return false;
		      },
		      successfunc: function () {
		    	  
		        var $self = $(this);
		        setTimeout(function () {
		          $self.trigger("reloadGrid");
		        }, 50);
		      }
		    },
	 addParams: {
		      position: "last",
		      addRowParams: {
		        keys: true,
		        //position: "last",
		        oneditfunc: function (rowid, response, options) {		        
		        },
		        //aftersavefunc  oneditfunc
		        beforeSaveRow: function (options, rowid) {
		       
		          OBConfirm(askSave, function (a) {
		            if (a) {
		              successflag = saverow(options, rowid);		             
		            }
		          });
		          return false;
		        },
		        successfunc: function () {		       
		          var $self = $(this);
		          setTimeout(function () {
		            $self.trigger("reloadGrid");
		          }, 50);
		        }
		      }

		    },
		    reloadAfterSubmit: true,
		  });

//  bankdetailGrid.jqGrid('filterToolbar', {
//     searchOnEnter: false
//   });
//  bankdetailGrid[0].triggerToolbar();

   jQuery("#bankdetailslist").trigger("reloadGrid");
   bankdetailGrid.jqGrid('filterToolbar', {
	    searchOnEnter: false
	  });
   changeJQGridDisplay("getbankdetaillist", "inpPerPayMethodId");
 });


getBankBranch = function (e) {
	 var $this = $(e.target),
     rowid, cellId,url;
	 // inline editing
	  rowid = $this.closest("tr.jqgrow").attr("id");
	  var row = $this.closest('tr.jqgrow');
		var branchList='<option role="option" value="0">' + select + '</option>';
        $('select[id*="' + rowid + '_Bank_Branch"]', row).empty();
        
		 $.ajax({
			    url: contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=bankdetails',
			    contentType: 'application/json; charset=utf-8',
			    dataType: 'json',
			    type: 'GET',
			    async: false,
			    data: {
			    	inpbankId: $this.val()
			    },
			    success: function (result) {
			      //console.log(result.length);
			      if (result != undefined && result.length > 0) {
			        for (var i = 0; i < result.length; i++) {
			        	branchList += '<option role="option" value="' + result[i].BankId + '">' + result[i].Branchcode + '</option>';
			        }
			        //console.log(branchList);
					 
				    $('select[id*="' + rowid + '_Bank_Branch"]', row).html(branchList);
				    //$("#bankdetailslist").jqGrid('setColProp', 'Bank_Branch', { editoptions: { value: document.getElementById("strbranch").value} });
			      }else{
			    	  $('select[id*="' + rowid + '_Bank_Branch"]', row).html(branchList);
			      }
			    },
			  });

}

function getbankBranchOnSelection(rowid){
	 var row=$('tr#' + rowid);//$("#bankdetailslist").jqGrid('getGridParam',"selrow");
	 var bankId= $("#bankdetailslist").jqGrid ('getCell', rowid, 'Bank_Name');
		var branchList= '0:' + select + ';';
	 
		

		 $.ajax({
			    url: contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=bankdetails',
			    contentType: 'application/json; charset=utf-8',
			    dataType: 'json',
			    type: 'GET',
			    async: false,
			    data: {
			    	inpbankId: bankId
			    },
			    success: function (result) {
			      if (result != undefined && result.length > 0) {
			        for (var i = 0; i < result.length; i++) {
			        	  branchList +=  result[i].BankId + ':' + result[i].Branchcode + ';';
			        }
			     
			      }
			    },
			  });
		 //DT.GETELBYID
		 //$("#bankdetailslist").jqGrid('setColProp', 'Bank_Branch', { editoptions: { value:branchValue} });
		 $("#bankdetailslist").jqGrid('setColProp', 'Bank_Branch', { editoptions: { value:branchList.substring(0,branchList.lastIndexOf(";"))} });
}

//getbankbranchOnSelectionOfBank();
function getbankbranchOnSelectionOfBank(){
		var branchList= '0:' + select + ';';
		 $.ajax({
			    url: contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=bankdetailsofbranch',
			    contentType: 'application/json; charset=utf-8',
			    dataType: 'json',
			    type: 'GET',
			    async: false,
			    data: {
			    },
			    success: function (result) {
			      if (result != undefined && result.length > 0) {
			        for (var i = 0; i < result.length; i++) {
			        	  branchList +=  result[i].BranchId + ':' + result[i].Branchcode + ';';
			        }
			     
			      }
			    },
			  });
		 document.getElementById("strbranch").value=branchList.substring(0,branchList.lastIndexOf(";"));
		 $("#bankdetailslist").jqGrid('setColProp', 'Bank_Branch', { editoptions: { value:document.getElementById("strbranch").value } });
}


 function enableForm() {
	changesFlag = 1;
	if ($("#inpvalue").val() != '0' && $("#inpvalue").val() != ''
			&& $("#inpname").val() != '' && $("#inpname").val() != '0'
			&& $("#inppaycode").val() != '0' && $("#inppayname").val() != '0'
			&& $("#inppaycurrency").val() != '0'
			&& $("#inppaycurrency").val() != '') {
		enableSaveButton("true");
	} else {
		enableSaveButton("false");
	}
}
 function reloadGrid(result) {
	  setTimeout(function () {
		 // location.reload();
	    $("#bankdetailslist").trigger("reloadGrid");
	    saveFlag = 0;
	  }, 50);
	}
 
 function changepaymentcodename(code,name){
	 var paymethodId ="";
	   if (code==null){
			 document.getElementById("inppaycode").value= name;	
			 paymethodId = document.getElementById("inppaycode").value;
	   }
	   if (name==null){
			document.getElementById("inppayname").value= code;	
			 paymethodId = document.getElementById("inppayname").value;
	   }

		getcurrency(paymethodId);
		enableSaveButton("false");
}
 
 function getcurrency(id){
	 $.post( contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax', {
         action : 'getpayrollpaymenttypemethodcurrency',
         payrollpaytypemethodId:id
     }, function(data) {
    	 document.getElementById("inppaycurrency").length = 1;
    	    $(data).find("CurrencyList").each(function(){
        		 var CurrencyId=$(this).find("CurrencyId").text();
                 var CurrencyName=$(this).find("CurrencyName").text();  

                 var newOption = document.createElement("option");         
                 document.getElementById("inppaycurrency").options.add(newOption);                                
                 newOption.text = CurrencyName;
                 newOption.value = CurrencyId;
        	 }); 
     });
     return; 
 }
 
 function onClickSave(index, type) {
 //  if (preValidation()) {
     if (index.className != 'Main_ToolBar_Button') return false;
     if (changesFlag == 1 &&  savevaliddata()) {
       if (type == "Grid") {
      //   showProcessBar(true, 2);
         document.getElementById("inpAction").value = "GridView";
         document.getElementById("SubmitType").value = "SaveGrid";
       }
       if (type == "New") {
        // showProcessBar(true, 2);
         document.getElementById("inpAction").value = "EditView";
         document.getElementById("SubmitType").value = "SaveNew";
       }
       if (type == "Save") {
         // showProcessBar(true, 2);
         document.getElementById("inpAction").value = "EditView";
         document.getElementById("SubmitType").value = "Save";

       }
       reloadTab('PERPAYMETHD');
     }
   

 }

 function reloadTabSave(tab) {
	    if (savevaliddata()) {
	       document.getElementById("SubmitType").value = "Save";
	      // document.getElementById("inpAction").value = "GridView";

	       var url = "&inpNextTab=" + tab;
	       submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.header/PersonalPaymentMethod?' + url, '_self', null, true);
	       return false;
	    }
	}  
 function onClickGrid() {
	  
	    document.getElementById("inpAction").value = "GridView";
	 
	    if ((changesFlag == 1) && $("#inppaycode").val() != '0'  && $("#inppayname").val() != '0' && $("#inppaycurrency").val() != '0' && $("#inppaycurrency").val() != '')
	        OBAsk(changedvaluessave, function(result) {
	            if (result)
	                reloadTabSave("PERPAYMETHD");
	            else {
	                reloadTab("PERPAYMETHD");
	            }
	        });
	    else
	        reloadTab('PERPAYMETHD');
	}
 
 function enableSaveButton(flag) {
	  if (flag == 'true' && document.getElementById('linkButtonSave_Relation').className != 'Main_ToolBar_Button') {
	    document.getElementById('buttonSave_Relation').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_Relation';
	    document.getElementById('buttonSave_New').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_New';
	    document.getElementById('buttonSave').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save';
	    $('#linkButtonSave_Relation, #linkButtonSave_New, #linkButtonSave').attr("class", "Main_ToolBar_Button");
	  } else if (flag == 'false') {
	    document.getElementById('buttonSave_Relation').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_Relation_disabled';
	    document.getElementById('buttonSave_New').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_New_disabled';
	    document.getElementById('buttonSave').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_disabled';
	    $('#linkButtonSave_Relation, #linkButtonSave_New, #linkButtonSave').attr("class", "Main_ToolBar_Button_disabled");
	  }
	}
 
 function searchList(type) {
	  if (type == "SD" && document.getElementById("gs_Start_Date").value != "") medicalInsuGrid[0].triggerToolbar();
	  if (type == "ED" && document.getElementById("gs_End_Date").value != "") medicalInsuGrid[0].triggerToolbar();
	}
	
	
 function saverow(options, rowid) {
	  var percentage = $("#" + $.jgrid.jqID(rowid + "_Percentage")).val();
      var accnum = $("#" + $.jgrid.jqID(rowid + "_Account_Number")).val();
      var startdate =  $("#" + $.jgrid.jqID(rowid + "_Start_Date")).val();
      var enddate =  $("#" + $.jgrid.jqID(rowid + "_End_Date")).val();
      var Bank_id = $("#" + $.jgrid.jqID(rowid + "_Bank_Name")).val();
      var Is_default = $("#" + $.jgrid.jqID(rowid + "_Default")).prop('checked');
      
      
      
      if(enddate!==''&&Is_default){
    	OBAlert(EnddateShouldbenull);  
    	return false;
      }
      else{
      var duplicateRowBankDetail=alreadyExistPPMBankDetail(document.getElementById("inpehcmPersonalPaymethdId").value,Is_default,rowid);
 }
      if(duplicateRowBankDetail){
    	  OBAlert(DefaultValidation);
    	  return false;
      }
      
      
      function alreadyExistPPMBankDetail(paymentmethodId,Is_Default,rowid){
 		 var isExists=false;
 	    
 	         $.ajax({     
 	             type:'GET',
 	             url:contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=checkDefaultPPMAlreadyExistBankDetail',            
 	             data:{inppersonalpaymethodId:document.getElementById("inpehcmPersonalPaymethdId").value,inpIsDefault :Is_Default,inprowid:rowid},
 	             dataType:'xml',
 	             async:false,
 	             success:function(data)
 	             {       	                    
 	                 $(data).find("checkDefaultPPMAlreadyExistBankDetail").each(function()
 	                 {    
 	                     var result=$(this).find("value").text();
 	                     if(result=="true" && Is_Default){
 	                    	 isExists=true;	                            
 	                     }
 	                     else{
 	                    	 isExists=false;
 	                     }
 	                 });  
 	             }
 	         });
   	       return isExists;
 	  	   
 	}
    
	  var percentagevalidation = percentageValidation(percentage, document.getElementById("inpehcmPersonalPaymethdId").value,rowid);
	        if (!percentagevalidation) {
	          OBAlert(percentageValidation);
	          return false;
	        }
	     if(startdate===""){
	    	 OBAlert(startdatenotnull);
             return false;
	     }
	    	 
        if(accnum===""||accnum==="0"){
                OBAlert(accountnumbernotnull);
                return false;
        }
        else{
                var accountnumbervalidation = accountNumbervalidation(accnum, $("#" + $.jgrid.jqID(rowid + "_Bank_Name")).val(), document.getElementById("inpehcmPersonalPaymethdId").value,rowid,startdate ,enddate);
                if(!accountnumbervalidation){
                        OBAlert(accountnumberunique);
                        return false;
                }
                var checkibanvalidation = ibanValidation(accnum);
                if(checkibanvalidation==="failed"){
                	 OBAlert(InvalidIban);
                	return false;
                }
	}
	  
	  jQuery("#bankdetailslist").saveRow(rowid, {
	    aftersavefunc: reloadGrid
	  });
	  saveFlag = 0;
	  return true;	  
	}
 //percentage validation
    function percentageValidation(percentage, paymentmethodId,bankDetailId) {
		  var validPercentage = "N";
		  if(percentage != ""){
		  $.ajax({
		    type: 'GET', 
		    url: contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=checkPercentageValidation',
		    data: {
		      inpPercentage: percentage,
		      inppaymentmethodId: paymentmethodId,
		      inpbankdetailId: bankDetailId		  
		    },
		    dataType: 'json',
		    async: false
		  }).done(function (response) {
			  validPercentage = response.validPercentage;
		  });
       }
		  return validPercentage;
		}
    // account number validation whether unique or not
    function accountNumbervalidation(accnum , bankDetailId , paymentmethodId ,rowids ,startdate ,enddate){
	var validaccnumber = "N";
	if (accnum != "") {
        $.ajax({
            type: 'GET',
            url: contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=validaccountnumber',
            data: {
                inpbankdetailId: bankDetailId,
                inpaccountNumber: accnum,
                inpPaymentMethodId:paymentmethodId,
                inprowid:rowids,
                inpstartdate:startdate,
                inpenddate:enddate
            },
            dataType: 'json',
            async: false
        }).done(function(response) {
        	validaccnumber = response.validaccnumber;
        	
        });
    }
    return validaccnumber;
}
    //Iban validation (Account number must be Iban)
    function ibanValidation(accnum){
    	var checkibanvalidation;
    	if (accnum != "") {
            $.ajax({
                type: 'GET',
                url: contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=Ibanvalidaiton',
                data: {
                    inpaccountNumber: accnum
                },
                dataType: 'json',
                async: false
            }).done(function(response) {
            	checkibanvalidation = response.checkibanvalidation;
            	
            });
        }
        return checkibanvalidation;
    }
	
	function deleterow(rowids) {
		  OBConfirm(askDelete, function (result) {
		    if (result) {
		        $.ajax({
		          url: contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=SaveBankDetailList' + bankdetailUrl + "&oper=del&id=" + rowids,
		        }).done(function (msg) {
		          successflag = "Y";
		          if (successflag == 'Y') $("#bankdetailslist").trigger("reloadGrid");
		        });		    
		    }
		  });
		  return false;
		}

	function savevaliddata(){
	    var isdefault=document.getElementById("inpdefaultflag").value;
	    var employeeId=document.getElementById("inpEmployeeId").value;
	    var payCode=document.getElementById("inppaycode").value;
	    var currency=document.getElementById("inppaycurrency").value;
	    
	    
	    // Check already PPM is exist or not 
	   var duplicate=AlreadyExistDefaultPPM(isdefault,employeeId);
	   var duplicateRow=AlreadyExistPPM(employeeId,payCode,currency);
	  
	  
	   if((duplicate == 1)||(duplicateRow == 1)){	
	       return false;
	   }
	   else{
	       return true;
	   }
	} 
 
	
	function AlreadyExistDefaultPPM(isdefault,employeeId){
	    var validationchk=0;   
	     if(document.getElementById("inpdefaultflag").checked==true){
	         $.ajax({     
	             type:'GET',
	             url:contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=checkDefaultPPMAlreadyExist',            
	             data:{inpempId:employeeId,inppersonalpaymethodId:document.getElementById("inpehcmPersonalPaymethdId").value},
	             dataType:'xml',
	             async:false,
	             success:function(data)
	             {       
	                 $(data).find("checkDefaultPPMAlreadyExist").each(function()
	                 {    
	                     var result=$(this).find("value").text();          
	                     if(result =="true"){                 
	                         validationchk=1;	                            
	                         OBAlert(alreadyDefaultPPM);	                     
	                         document.getElementById("inpdefaultflag").checked=false;
	                     }
	                     else{
	                         validationchk=0;
	                     }
	                 });             
	             }
	         });
	         
	    }
	 
	      return validationchk;
	}
	
	
	
	function AlreadyExistPPM(employeeId,payCode,currency){
	    var validationchk=0;
	     if((document.getElementById("inppaycode").value != null) &&(document.getElementById("inppaycurrency").value != null)){
	    	 
	         $.ajax({     
	             type:'GET',
	             url:contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax/PersonalPaymentMethodAjax?action=checkPPMAlreadyExist',            
	             data:{inpempId:employeeId,inppersonalpaymethodId:document.getElementById("inpehcmPersonalPaymethdId").value,
	            	 inppaycode:payCode,inppaycurrency:currency},
	             dataType:'xml',
	             async:false,
	             success:function(data)
	             {       
	                 $(data).find("checkPPMAlreadyExist").each(function()
	                 {    
	                     var result=$(this).find("value").text();          
	                     if(result =="true"){      
	                         validationchk=1;	 
	                         OBAlert(alreadyExistPPM);
	                     }
	                     else{
	                         validationchk=0;
	                     }
	                 });             
	             }
	         });
	         
	    }
	      return validationchk;
	}
 