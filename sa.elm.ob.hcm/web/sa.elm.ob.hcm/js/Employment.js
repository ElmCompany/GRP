/*
 *************************************************************************
 * All Rights Reserved.
 * Contributor(s): Gopalakrishnan
 *************************************************************************
*/


//issuance done make all field as readonly
$( document ).ready(function() {
	if($('#inpIssuance').val()=='I' ||  $('#inpIssuance').val()=='TE'){
	$("#inpGrade").next().attr("disabled", "disabled");
	$("#inpGrade").next().next().children().attr("disabled","disabled");
	$("#inpEmpGrade").next().attr("disabled", "disabled");
	$("#inpEmpGrade").next().next().children().attr("disabled","disabled");
	$("#inpPayRoll").next().attr("disabled", "disabled");
	$("#inpPayRoll").next().next().children().attr("disabled","disabled");
	document.getElementById("inpEmpGrade").setAttribute("disabled", "true");
	document.getElementById("inpPayRoll").setAttribute("disabled", "true");
	document.getElementById("inpGrade").setAttribute("disabled", "true");
	document.getElementById("inpJobno").setAttribute("disabled", "true");
	document.getElementById("inpEmpCat").setAttribute("disabled", "true");
	document.getElementById("inpPayScale").setAttribute("disabled", "true");
	document.getElementById("inpPayScale").setAttribute("disabled", "true");
	document.getElementById("inpGradeStep").setAttribute("disabled", "true");
	/*document.getElementById("inpReason").setAttribute("disabled", "true");*/
	document.getElementById("inpReasonLabel").setAttribute("readonly", "true");
	document.getElementById("inpStartDate").setAttribute("disabled", "true");
	document.getElementById('buttonNew').className = 'Main_ToolBar_Button_disabled';
	document.getElementById('linkButtonNew').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled';
	}
	if(document.getElementById('inpCancelHiring').value == "false")
	{
     document.getElementById('LinkButtonCancel').className = 'OBToolbarIconButton_icon_undo OBToolbarIconButtonDisabled';
	}
   else
	{
	document.getElementById('LinkButtonCancel').className = 'OBToolbarIconButton_icon_undo OBToolbarIconButton';
	}
	
});

//Load Grade
getGradeOnLoad();
function getGradeOnLoad(){
	setTimeout(function () {
        $("#inpGrade").select2(selectBoxAjaxPaging({
            url : function() {
            		return contextPath+'/EmploymentAjax?action=getGrade';
            	},
           size : "small"
       }));
        $("#inpGrade").on("select2:unselecting", function (e) {
  	      document.getElementById("inpGrade").options.length = 0;
  	    });
    }, 100);
}
if(gradeId!=null && gradeId!=""){
	var data = [{
		id: gradeId,
	    text: gradeName
	}];
	$("#inpGrade").select2({
		data: data
	});
}

//On Loading Fields
getJobNoOnLoad();
function getJobNoOnLoad(){
    setTimeout(function () {
        $("#inpJobno").select2(selectBoxAjaxPaging({
            url : function() {
            		return url;
           },
           size : "small"
       }));
        $("#inpJobno").on("select2:unselecting", function (e) {
        		document.getElementById("inpJobno").options.length = 0;
        });
	}, 100);
}
if(jobNo!=null && jobNo!=""){
	var data = [{
        id: jobNo,
		text: jobNoName
	}];
	$("#inpJobno").select2({
		data: data
	});
	generateFields(jobNo);
}
	
getPayScaleOnLoad();
function getPayScaleOnLoad(){
	setTimeout(function () {
		$("#inpPayScale").select2(selectBoxAjaxPaging({
			url : function() {
	            	return url;
	        },
	        size : "small"
	    }));
	    $("#inpPayScale").on("select2:unselecting", function (e) {
	  	    document.getElementById("inpPayScale").options.length = 0;
	  	});
	}, 100);
		
}
if(payScale!=null && payScale!=""){
	var data = [{
		id: payScale,
		text: payScaleName
	}];
	$("#inpPayScale").select2({
		data: data
	});
}
	
getEmpGradeOnLoad();
function getEmpGradeOnLoad(){
    setTimeout(function () {
        $("#inpEmpGrade").select2(selectBoxAjaxPaging({
           url : function() {
	            	return url;
           },
           size : "small"
        }));
        $("#inpEmpGrade").on("select2:unselecting", function (e) {
        		document.getElementById("inpEmpGrade").options.length = 0;
        });
    }, 100);
}
if(empGrade!=null && empGrade!=""){
	var data = [{
		id: empGrade,
		text: empGradeName
	}];
	$("#inpEmpGrade").select2({
		data: data
	});
}
	
getEmpGradeStepOnLoad();
function getEmpGradeStepOnLoad(){
    setTimeout(function () {
        $("#inpGradeStep").select2(selectBoxAjaxPaging({
           url : function() {
	            	return url;
           },
           size : "small"
        }));
	    $("#inpGradeStep").on("select2:unselecting", function (e) {
  	      	document.getElementById("inpGradeStep").options.length = 0;
	  	});
	}, 100);
}
if(gradeStep!=null && gradeStep!=""){
	var data = [{
		id: gradeStep,
		text: gradeStepName
	}];
	$("#inpGradeStep").select2({
		data: data
	});
}

//Load Employee Category
getEmpCatOnLoad();
function getEmpCatOnLoad(){
    setTimeout(function () {
        $("#inpEmpCat").select2(selectBoxAjaxPaging({
            url : function() {
            		return contextPath+"/EmploymentAjax?action=getEmploymentCategory";
            	},
           size : "small"
        }));
        $("#inpEmpCat").on("select2:unselecting", function (e) {
        		document.getElementById("inpEmpCat").options.length = 0;
  	    });
    }, 100);
}
if(empCat!=null && empCat!=""){
	var data = [{
		id: empCat,
		text: empCatName
	}];
	$("#inpEmpCat").select2({
		data: data
	});
}

// load Payroll  
getPayrollOnLoad();
function getPayrollOnLoad(){
    setTimeout(function () {
        $("#inpPayRoll").select2(selectBoxAjaxPaging({
            url : function() {
            		return contextPath+'/EmploymentAjax?action=getPayroll';
	        },
	        size : "small"
        }));
	    $("#inpPayRoll").on("select2:unselecting", function (e) {
	    		document.getElementById("inpPayRoll").options.length = 0;
	  	});
	}, 100);
}
if(payRollId!=null && payRollId!=""){
	var data = [{
		id: payRollId,
		text: payRollName
	}];
	$("#inpPayRoll").select2({
		data: data
	});
}

/**
 * Generate pay scale on change grade
 * @param gradeId
 */
	function generatePositonList(startDate){
		jobNo='0';
		document.getElementById("inpJobCode").value='';
		document.getElementById("inpJobTitle").value='';
		document.getElementById("inpDeptCode").value='';
		document.getElementById("inpDeptName").value='';
		document.getElementById("inpSectionCode").value='';
		document.getElementById("inpSectionName").value='';
		document.getElementById("inpLocation").value='';
		
		document.getElementById("inpEmpGrade").value='';
		document.getElementById("inpPayScale").value='';
		document.getElementById("inpGradeStep").value='';
		$("#inpEmpGrade").next().val("");
		var url = contextPath+"/EmploymentAjax?action=getJobNo&gradeId="+document.getElementById("inpGrade").value;
		url += "&inpEmploymentId="+document.getElementById("inpEmploymentId").value;
		url += "&inpEmployeeId="+document.getElementById("inpEmployeeId").value;
		url += "&inpStartDate="+document.getElementById("inpStartDate").value;
		$.getJSON(url, function(result){
			
		}).done(function(data){

			$("#inpJobno")
	   		.find("option")
	   		.remove()
	   		.end();
			
			$("#inpJobno").append("<option value='0' selected>select</option>");
			$.each( data, function( i, item ) {
				$("#inpJobno").append("<option value='"+item.jobId+"'>"+item.jobNo+"</option>");
		      });
			if(jobNo!=null && jobNo!='' && jobNo!='null'){
				$("#inpJobno").val(jobNo);
				generateFields(jobNo);
			}
				
		});
	}
function generateJob(gradeId){
	var urlpath = contextPath+"/EmploymentAjax?action=getJobNo";
	urlpath += "&gradeId="+document.getElementById("inpGrade").value;
	urlpath += "&inpEmployeeId="+document.getElementById("inpEmployeeId").value;
	urlpath += "&inpEmploymentId="+document.getElementById("inpEmploymentId").value;

	
	console.log(urlpath);
	urlpath += "&inpStartDate="+document.getElementById("inpStartDate").value;
	
	$("#inpJobno").append("<option value='0' selected>select</option>");
	$("#inpJobno").select2(selectBoxAjaxPaging({
		url : function() {
			   return urlpath;
			generateFields($("#inpJobno"));
		},
	    size : "small"
	}));		
}

/**
 * Generate pay scale on change empgrade
 * @param empGrade
 */
function generatePayscale(empGrade){
	payScale='0';
	var urlpath = contextPath+"/EmploymentAjax?action=getPayscale&empGrade="+empGrade;
	$("#inpPayScale").append("<option value='0' selected>select</option>");
	$("#inpPayScale").select2(selectBoxAjaxPaging({
		url : function() {
			   return urlpath;
			   generateGradeStep($("#inpPayScale"));
		},
	    size : "small"
	}));
}


/**
 * generate Empgrade while change position Grade
 * @param gradeId
 */
function generateEmpgrade(gradeId){
	console.log("Grade ID"+gradeId);
	console.log("Emp Grade"+$("#inpEmpGrade"));
	var urlpath = contextPath+"/EmploymentAjax?action=getEmploymentGrade";
	urlpath += "&gradeId="+document.getElementById("inpGrade").value;
	console.log(urlpath);
	$("#inpEmpGrade").append("<option value='0' selected>select</option>");
	$("#inpEmpGrade").select2(selectBoxAjaxPaging({
		url : function() {
			   return urlpath;
				generatePayscale($("#inpEmpGrade"));
		},
	    size : "small"
	}));
}

/**
 * generate fields while changing job no
 * @param positionId
 */
function generateFields(positionId){
	if(jobCode!=null && jobCode!='' && jobCode!='null'){
		$("#inpJobCode").val(jobCode);
		$("#inpJobCode").append("<option value='"+jobCode+"'>"+inpJobCodeValue+"</option>");     
		if(deptCode!=null && deptCode!='' && deptCode!='null'){
			$("#inpDeptCode").val(deptCode);
			$("#inpDeptCode").append("<option value='"+deptCode+"'>"+inpDeptCodeValue+"</option>"); 
			if(sectCode!=null && sectCode!='' && sectCode!='null'){
				$("#inpSectionCode").val(sectCode);
				$("#inpSectionCode").append("<option value='"+sectCode+"'>"+inpSectionCodeValue+"</option>"); 
		    }
		}
	}
	else{
		document.getElementById("inpJobCode").value='';
		document.getElementById("inpJobTitle").value='';
		document.getElementById("inpDeptCode").value='';
		document.getElementById("inpDeptName").value='';
		document.getElementById("inpSectionCode").value='';
		document.getElementById("inpSectionName").value='';
		document.getElementById("inpLocation").value='';
		
		var url=contextPath+"/EmploymentAjax?action=getJobDetails&positionId="+positionId;
		url +="&gradeId="+document.getElementById("inpGrade").value;

		$.ajax({
			url:url,
			type:'GET',
			dataType:'json',
			success:function(data){
		    $("#inpJobCode").append("<option value='"+data.jobCode+"'>"+data.jobname+"</option>");
			document.getElementById("inpJobTitle").value=data.jobTitle;
			$("#inpDeptCode").append("<option value='"+data.DeptCode+"'>"+data.deptName+"</option>");
			document.getElementById("inpDeptName").value=data.DeptName;
			if(data.secCode !=null && data.secCode!='' && data.secCode!='null')
				$("#inpSectionCode").append("<option value='"+data.secCode+"'>"+data.secname+"</option>");
			document.getElementById("inpSectionName").value=data.secName;
			document.getElementById("inpLocation").value=data.location;
			 }
		});
	}
}
	
//load grade Step 
function generateGradeStep(payScaleId){
	gradeStep='O';
	var urlpath = contextPath+"/EmploymentAjax?action=getGradeStep&payScaleId="+payScaleId;
	$("#inpGradeStep").append("<option value='0' selected>select</option>");
	$("#inpGradeStep").select2(selectBoxAjaxPaging({
		url : function() {
			   return urlpath;
		},
	    size : "small"
	}));
	if(gradeStep!=null && gradeStep !='' && gradeStep!=null)
		$("#inpGradeStep").val(gradeStep);	
}

//Main Functions
var currentTab = 'EMPINF';
var changesFlag=0,checkDate='N',checkActiveRecord='N';


function preValidation(){
	var hireDate=$("#inpHireDate").val(),
	startDate=$("#inpStartDate").val();
	
	var fdate = hireDate.split("-");
	var fromdate=new Date();
	     
	fromdate.setDate(fdate[0]);
	fromdate.setMonth(fdate[1]-1);
	fromdate.setFullYear(fdate[2]);

	var tdte = startDate.split("-");
	var todate=new Date();
	     
	todate.setDate(tdte[0]);
	todate.setMonth(tdte[1]-1);
	todate.setFullYear(tdte[2]);
	
	if (fromdate.getTime() > todate.getTime()){
		OBAlert("Start Date should Be greater Than The Hire Date");
		checkDate='Y';
	} 
	else
		checkDate='N'
			
	$.ajax({
		type:'GET',
        	url:contextPath+'/EmploymentAjax?action=checkActiveEmployment',            
        	data:{inpEmployeeId:document.getElementById("inpEmployeeId").value,inpEmploymentId:document.getElementById("inpEmploymentId").value},
        	dataType:'json',
        async:false
		}).done(function(response) {
			checkActiveRecord=response.isExists;
		});
	if(checkActiveRecord=='Y'){
		 OBAlert('Already there exists an active employment information, so its not possible to save current record.');
		 checkActiveRecord='Y';
	}
	else{
		checkActiveRecord='N';
	}
	if(checkDate=='Y' || checkActiveRecord=='Y'){
		return false;
	}
	else{
		return true;
	}
}

function reloadFunction(tab){
	hideMessage();
	if (changesFlag == 1 && document.getElementById('linkButtonSave').className=='Main_ToolBar_Button' && document.getElementById('buttonSave').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save'){
		OBAsk(changevalueask, function(result) {
            if (result){
            		if(preValidation()){
            			reloadTabSave(tab);
            		}
            }
            else {
                reloadTab(tab);
            }
        });
	}
    else
        reloadTab(tab);
}

function onClickRefresh() {
    hideMessage();
    document.getElementById("inpAction").value = "EditView";
    if (changesFlag == 1 && document.getElementById('linkButtonSave').className=='Main_ToolBar_Button' 
    	&& document.getElementById('buttonSave').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save')
        OBAsk(changevalueask, function(result) {
            if (result){
            		if(preValidation()){
            			reloadTabSave(currentTab);
            		}
            }
            else {
                reloadTab(currentTab);
            }
        });
    else
        reloadTab('EMPINF');
}

function enableSaveButton(flag) {
    if (flag == 'true' && document.getElementById('linkButtonSave_Relation').className != 'Main_ToolBar_Button' && $('#inpIssuance').val()!='I') {   	
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
    if($("#inpGrade").val() !='0' && $("#inpJobno").val() != '0' && $("#inpGradeStep").val() !='0' && $("#inpPayScale").val() != '0'
    	&& $("#inpEmpGrade").val() !='0' && $("#inpPayRoll").val() !='0'){
    		enableSaveButton("true");
    }
    else{
    		enableSaveButton("false");
    }
}

function enableFormSupervisor(value) {
    changesFlag = 1;
    hideMessage();
    if($("#inpGrade").val() !='0' && $("#inpJobno").val() != '0' && $("#inpGradeStep").val() !='0' && $("#inpPayScale").val() != '0'
    	&& $("#inpEmpGrade").val() !='0' && $("#inpPayRoll").val() !='0'){
    		if (document.getElementById('linkButtonSave_Relation').className != 'Main_ToolBar_Button') {
    	        document.getElementById('buttonSave_Relation').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_Relation';
    	        document.getElementById('buttonSave').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save';
    	        $('#linkButtonSave_Relation, #linkButtonSave_New, #linkButtonSave').attr("class", "Main_ToolBar_Button");
    	    }
    }
    else{
    		enableSaveButton("false");
    }
}

function hideMessage() {
    $("#messageBoxID").hide();
}

function reloadTabSave(tab) {
	var url="";
	document.getElementById("inpNextTab").value = tab;
    document.getElementById("SubmitType").value = "Save";
    document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employment.header/Employment' + url;
    document.frmMain.submit();
}

function reloadTab(tab) {
	var url="";
	document.getElementById("inpEmpNo").value=document.getElementById("inpEmpNo").value;
    if (tab == 'EMP') {
    		document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView' + url;
        document.frmMain.submit();
    }
    else if (tab == 'EMPINF') {
    		document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employment.header/Employment' + url;
        document.frmMain.submit();
    }
    else if(tab == 'EMPADD') {
    		document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView';
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
    else if (tab == 'PREEMP') {
        	submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
        	return false;
    }
    else if (tab == 'EMPCTRCT') {
        	var url="";
        	var employeeId=document.getElementById("inpEmployeeId").value;
        	document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
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

function onClickSave(index, type) {
	if(document.getElementById('buttonSave').className != 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save'){
		 return false;
	}		
	if(preValidation()){
		 if (index.className != 'Main_ToolBar_Button')
		        return false;
		 if (changesFlag == 1 && document.getElementById('linkButtonSave').className=='Main_ToolBar_Button' 
		    	&& document.getElementById('buttonSave').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save') {
			 if (type == "Grid") {
				 showProcessBar(true, 2);
				 document.getElementById("inpAction").value = "GridView";
				 document.getElementById("SubmitType").value = "Save";
			 }
		     if (type == "New") {
		    	 	showProcessBar(true, 2);
		    	 	document.getElementById("inpAction").value = "EditView";
		    	 	document.getElementById("SubmitType").value = "SaveNew";
		     }
		     if (type == "Save") {
		    	 	// showProcessBar(true, 2);
		    	 	document.getElementById("inpAction").value = "EditView";
		    	 	document.getElementById("SubmitType").value = "Save";
		    	 	document.getElementById("inpEmpNo").value=document.getElementById("inpEmpNo").value;    
		     }
		     reloadTab('EMPINF');
		 }
	}
}

function onClickGridView() {
    hideMessage();
    document.getElementById("inpAction").value = "GridView";
    if (changesFlag == 1 && document.getElementById('linkButtonSave').className=='Main_ToolBar_Button' 
    	&& document.getElementById('buttonSave').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save'){
        OBAsk(changevalueask, function(result) {
            if (result){
            		if(preValidation()){
            			reloadTabSave(currentTab);
            		}
            }          
            else {
                reloadTab(currentTab);
            }
        });
    }
    else
        reloadTab('EMPINF');
}

function onClickNew() {
	if(document.getElementById('buttonNew').className != 'Main_ToolBar_Button'){
		return false;
	}	
	document.getElementById("inpAction").value = "EditView";
	 if (changesFlag == 1 && document.getElementById('linkButtonSave').className=='Main_ToolBar_Button' 
	    	&& document.getElementById('buttonSave').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save')
        OBAsk(changevalueask, function(result) {
            if (result){
            		if(preValidation()){
            			reloadTabSave(currentTab);	
            		}
            }
            else {
            	document.getElementById("inpEmploymentId").value = '';
                document.getElementById("inpAction").value = "EditView";
                submitCommandForm('DEFAULT', true, null, 'Employment', '_self', null, true);
                return false;
            }
        });
    else{
    		document.getElementById("inpEmploymentId").value = '';
        document.getElementById("inpAction").value = "EditView";
        submitCommandForm('DEFAULT', true, null, 'Employment', '_self', null, true);
        return false;
    }
}
 			
getSupervisor();	
function getSupervisor(){
    document.getElementById("inpSupervisorId").value = "0";
    setTimeout(function () {
        $("#inpSupervisorId").select2(selectBoxAjaxPaging({
            url : function() {
                return  contextPath+"/EmploymentAjax?action=getSupervisor&inpEmployeeId=" + document.getElementById("inpEmployeeId").value 
            	},
            	data: [{
            		id: inpSupervisorId,
            		text: inpSupervisorName
            	}],
            	size : "small"
        }));
        $("#inpSupervisorId").on("select2:unselecting", function (e) {
        		document.getElementById("inpSupervisorId").options.length = 0;
  	    });
    }, 100);
 }

$(document).ready(function (){
	if(status !=null && status!="ACT"){
		$("#inpSupervisorId").prop("disabled", true);
	}
});