<%@ page import="org.opencms.workplace.editors.*" buffer="none" %><%	

	// initialize the workplace class
	CmsDialogElements wp = new CmsDialogElements(pageContext, request, response);
	
	String refreshOpener = "false";
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialogElements.ACTION_UPDATE_ELEMENTS:

//////////////////// ACTION: update the enabled/disabled elements

	wp.actionUpdateElements();
%>

	<html><head>
	<script type="text/javascript">
		function closeAction() {
			this.elemName = "<%= wp.getChangeElement() %>";
			this.elemLocale = "<%= wp.getElementLocale() %>";
		}
		var closeObj = new closeAction();
		if (window.opener.popupCloseAction) {
			window.opener.popupCloseAction(closeObj);
		}
		window.close();
	</script>
	</head></html>

<%
break;
case CmsDialogElements.ACTION_DELETECONTENT:

//////////////////// ACTION: delete element content action

	wp.actionDeleteElementContent();
	refreshOpener = "true";

case CmsDialogElements.ACTION_DEFAULT:
default:

//////////////////// ACTION: show initial template dialog (default)

	wp.setParamAction(wp.DIALOG_UPDATE_ELEMENTS);

%><%= wp.htmlStart(null, wp.getParamTitle()) %>
<script type="text/javascript">
<!--

function confirmDelete(elementName) {
	var confirmText = "<%= wp.key("editor.dialog.elements.confirmdelete") %>";
	confirmText = confirmText.replace(/%element%/, elementName);
	if (confirm(confirmText)) {
		document.main.action.value = "<%= wp.DIALOG_DELETECONTENT %>";		
		document.main.deleteelement.value = elementName; 
		document.main.submit();
	}
}

if (<%= refreshOpener %>) {
	window.opener.deleteEditorContent("<%= wp.getParamDeleteElement() %>", "<%= wp.getParamElementlanguage() %>");
	window.opener.buttonAction(1);
}

<%
if ("true".equals(refreshOpener)) {
	wp.setParamDeleteElement(null);
}
%>

window.resizeTo(400, 170+(27*<%= wp.computeElements().size() %>));

//-->
</script>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">

<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">
<input type="hidden" name="deleteelement">

<%= wp.buildElementList() %>

<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOk() %>

</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>