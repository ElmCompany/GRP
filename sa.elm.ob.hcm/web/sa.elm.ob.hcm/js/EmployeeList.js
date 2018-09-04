/*
 *************************************************************************
 * All Rights Reserved.
 * Contributor(s): Gopalakrishnan
 *************************************************************************
*/

addCSS(".ui-autocomplete-loading { background-repeat: no-repeat !important; background-position: right center !important; }");
addCSS(".ui-autocomplete { max-height: 400px; max-width: 278px !important; background-color: #f5f7f1 !important; border-radius: 0px; padding: 0px; }");
addCSS(".ui-autocomplete li { background-color: #f5f7f1 !important; }");
addCSS(".ui-menu .ui-menu-item a { cursor: pointer; }");
addCSS(".ui-menu .ui-menu-item a span { font-weight: bold; }");
addCSS(".ui-menu-item a.ui-state-hover { border-radius: 0px; background: none !important; background-color: #cdd7bb !important; font-weight: normal; color: #212121; }");
addCSS(".ui-autocomplete.ui-menu.ui-widget-content { border: 1px solid #aaa; background: #fff url(../web/js/themes/base/images/ui-bg_flat_75_ffffff_40x100.png) 50% 50% repeat-x; color: #222; }");
addCSS("input.HeaderSearchBox { padding-right:20px;background-image:url('../web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/org.openbravo.client.application/images/form/search_picker.png'); background-repeat:no-repeat !important; background-position: right !important; }");
addCSS("input.ArabicHeaderSearchBox { padding-left:20px;background-image:url('../web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/org.openbravo.client.application/images/form/search_picker.png'); background-repeat:no-repeat !important; background-position: left !important; }");
var resetHeader = false;
$(function() {
	function preg_quote(str) {
		return (str + '').replace(/([\\\.\+\*\?\[\^\]\$\(\)\{\}\=\!\<\>\|\:])/g, "\\$1");
	}
	$.ui.autocomplete.prototype._renderItem = function(ul, item) {
		var reg = new RegExp(this.term, 'ig'), val = '';
		if (this.element.context.id == 'inpName1')
			val = (item.aname.replace(new RegExp("(" + preg_quote(this.term) + ")", 'gi'), '<span>$1</span>')) + ' ' + item.fname + ' (' + item.empno + ')';
		else if (this.element.context.id == 'inpName2')
			val = item.aname + ' ' + (item.fname.replace(new RegExp("(" + preg_quote(this.term) + ")", 'gi'), '<span>$1</span>')) + ' (' + item.empno + ')';
		else if (this.element.context.id == 'inpEmpNo')
			val = item.aname + ' ' + item.fname + ' (' + (item.empno.replace(new RegExp("(" + preg_quote(this.term) + ")", 'gi'), '<span>$1</span>')) + ')';
		return $("<li></li>").data("item.autocomplete", item).append("<a>" + val + "</a>").appendTo(ul);
	};
	$("#inpName1, #inpName2, #inpEmpNo").click(function() {
		this.select();
	});
	$("#inpName1").autocomplete({
		source : function(request, respond) {
			$.post(contextPath + '/EmploymentAjax', {
				action : 'SearchEmployee',
				col : 'aname',
				term : request.term
			}, function(res) {
				respond(res);
			});
		},
		minLength : 1,
		select : function(event, ui) {
			resetHeader = true;
			$('#inpEmployeeId').val(ui.item.id);
			$('#inpName1').val(ui.item.fname);
			$('#inpName2').val(ui.item.lname);
			$('#inpEmpNo').val(ui.item.empno);
			resetTabHeader();
			reloadTab(currentTab);
		}
	});
	$("#inpName2").autocomplete({
		source : function(request, respond) {
			$.post(contextPath + '/EmploymentAjax', {
				action : 'SearchEmployee',
				col : 'fname',
				term : request.term
			}, function(res) {
				respond(res);
			});
		},
		minLength : 1,
		select : function(event, ui) {
			resetHeader = true;
			$('#inpEmployeeId').val(ui.item.id);
			$('#inpName1').val(ui.item.fname);
			$('#inpName2').val(ui.item.lname);
			$('#inpEmpNo').val(ui.item.empno);
			resetTabHeader();
			reloadTab(currentTab);
		}
	});
	$("#inpEmpNo").autocomplete({
		source : function(request, respond) {
			$.post(contextPath + '/EmploymentAjax', {
				action : 'SearchEmployee',
				col : 'empno',
				term : request.term
			}, function(res) {
				respond(res);
			});
		},
		minLength : 1,
		select : function(event, ui) {
			resetHeader = true;
			$('#inpEmployeeId').val(ui.item.id);
			$('#inpName1').val(ui.item.fname);
			$('#inpName2').val(ui.item.lname);
			$('#inpEmpNo').val(ui.item.empno);
			resetTabHeader();
			reloadTab(currentTab);
		}
	});
	$("#inpName2, #inpEmpNo").addClass('HeaderSearchBox');
	$("#inpName1").addClass('ArabicHeaderSearchBox');
});
function resetTabHeader() {
	if (currentWindow == 'EMP') {
		if (currentTab == 'EMPINF'){
			$('#inpEmploymentId').val('');
			$('#inpEmpStatus').val('');
			$('#inpEmployeeStatus').val('');
			$('#inpissued').val('');
			$('#inpStatus').val('');
		}
		if(currentTab=='Dependent')
			$('#inpDependentId').val('');
		if(currentTab=='EMPQUAL')
			$('#inpQualificationId').val('');
	}
	
}
function resetHeaderValues(index) {
	setTimeout(function() {
		if (!resetHeader)
			index.value = index.nextSibling.nextSibling.value;
	}, 10);
}
function onKeydownHeadeValues(index, e) {
	e = e || window.event;
	var charCode = e.keyCode || e.which;
	if (charCode == 9 || (charCode >= 112 && charCode <= 123))
		return true;
	if (typeof changesFlag != "undefined" && (changesFlag == 1 || changesFlag == true)) {
		$("#inpName1, #inpName3, #inpEmpNo").autocomplete("disable");
		return false;
	}
	return true;
}
