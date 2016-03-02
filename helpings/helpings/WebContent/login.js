var gLogin = false;

function userAction(){

	if ( gLogin ) {
		return login();
	} else {
		return createUser();
	}
}

function login() {

	var email = document.forms["userForm"]["email"].value;
	if (email == null || email == "") {
		alert("Please enter your email address");
		return false;
	}
	var password = document.forms["userForm"]["password"].value;
	if (password == null || password == "") {
		alert("Please enter your password");
		return false;
	}

	var userRequest;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		userRequest = new XMLHttpRequest();
	} else {// code for IE6, IE5
		userRequest = new ActiveXObject("Microsoft.XMLHTTP");
	}

	var params = new Object();
	params.command = "login";
	params.email = email;
	params.password = password;
	var paramString = JSON.stringify(params);

	userRequest.open("POST", "user", true);
	userRequest.onreadystatechange = function() {
		if (userRequest.readyState == 4) {
			if ( userRequest.status == 401 ) {
				loginFailed("Login Failed!");
			} else {
				var jsonResp = JSON.parse(userRequest.responseText);
				setCookie("token", jsonResp.token);
				setCookie("username", jsonResp.username);
				document.getElementById("user").style.display = 'none';
				location.href='/feed';
			}
		}
	};

	//Send the proper header information along with the request
	userRequest.setRequestHeader("Content-type",
	"application/x-www-form-urlencoded");
	userRequest.send(paramString);
	return false;
}

function createUser() {

	gName = document.forms["userForm"]["username"].value;
	if (gName == null || gName == "") {
		alert("Username cannot be empty");
		return false;
	}

	var password = document.forms["userForm"]["password"].value;
	if (password == null || password == "") {
		alert("Password cannot be empty");
		return false;
	}

	if ( !testPassword(password) ) {
		return false;
	}

	var email = document.forms["userForm"]["email"].value;
	if (email == null || email == "") {
		alert("Email cannot be empty");
		return false;
	}

	var userRequest;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		userRequest = new XMLHttpRequest();
	} else {// code for IE6, IE5
		userRequest = new ActiveXObject("Microsoft.XMLHTTP");
	}

	var params = new Object();
	params.command = "new_user";
	params.username = gName;
	params.password = password;
	params.email = email;
	var paramString = JSON.stringify(params);

	userRequest.open("POST", "user", true);
	userRequest.onreadystatechange = function() {
		if (userRequest.readyState == 4) {
			if ( userRequest.status == 401 ) {
				loginFailed("User Create Failed!");
			} else {
				setCookie("token",userRequest.responseText);
				setCookie("username",gName);
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