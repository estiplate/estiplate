function changePassword() {

	var username = getCookie("username");
	var old_password = document.forms["userForm"]["old_password"].value;
	if (old_password == null || old_password == "") {
		alert("Please enter your password");
		return false;
	}

	var new_password = document.forms["userForm"]["new_password"].value;
	if (new_password == null || new_password == "") {
		alert("Password cannot be empty");
		return false;
	}
	if ( !testPassword(new_password) ) {
		return false;
	}

	var confirm_password = document.forms["userForm"]["confirm_password"].value;
	if (confirm_password == null || confirm_password == "") {
		alert("Please confirm your new password");
		return false;
	}
	if ( confirm_password != new_password ) {
		alert("Confirmation does not match");
		return false;
	}

	var userRequest;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		userRequest = new XMLHttpRequest();
	} else {// code for IE6, IE5
		userRequest = new ActiveXObject("Microsoft.XMLHTTP");
	}

	var params = new Object();
	params.command = "change_password";
	params.username = username;
	params.password = old_password;
	params.new_password = new_password;
	var paramString = JSON.stringify(params);

	userRequest.open("POST", "user", true);
	userRequest.onreadystatechange = function() {
		if (userRequest.readyState == 4) {
			if ( userRequest.status == 401 ) {
				loginFailed("Please re-enter your password and try again");
				document.forms["userForm"]["old_password"].value = "";
			} else {
				setCookie("token",userRequest.responseText);
				document.getElementById("user").style.display = 'none';
				location.href='feed';
			}
		}
	};

	//Send the proper header information along with the request
	userRequest.setRequestHeader("Content-type",
	"application/x-www-form-urlencoded");
	userRequest.send(paramString);
	return false;
}

function loginFailed(message){
	document.getElementById("message").style.display = 'block';
	document.getElementById("user").style.display = 'none';
	document.getElementById("messageText").innerHTML = message;
}

function clearMessage(){
	document.getElementById("message").style.display = 'none';
	document.getElementById("user").style.display = 'block';	
}