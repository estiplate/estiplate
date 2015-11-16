var gCookies = new Array();

function setCookie(key, value){
	var found = false;
	for( var i=0; i<gCookies.length; i++){
		if( gCookies[i].key.trim() === key){
			gCookies[i].value = value;
			found = true;
		}
	}
	if ( !found ) {
		var obj = new Object;
		obj.key=key;
		obj.value=value;
		gCookies.push(obj);
	}
	saveCookies();
}

function getCookie(key){
	getCookies();
	for( var i=0; i<gCookies.length; i++){
		if( gCookies[i].key.trim() === key){
			return gCookies[i].value;
		}
	}
	return null;
}

function getCookies()
{
	if ( document.cookie == null ) {
		return "";
	}
	gCookies.length = 0;
	var ca = document.cookie.split(';');

	for(var i=0; i<ca.length; i++) 
	{
		var c = ca[i].split('=');
		var obj = new Object;
		obj.key = c[0];
		obj.value = c[1];
		gCookies.push(obj);
	}
}

function saveCookies(){
	var cookieString="";
	for( var i=0; i<gCookies.length; i++){
		cookieString = gCookies[i].key + "=" + gCookies[i].value;
		document.cookie=cookieString;
	}
}

function userLogin(){
	document.getElementById("username").style.display = 'block';
	document.getElementById("submitbutton").value = "Log in";
	gLogin = true;
}

function userSignup(){
	document.getElementById("username").style.display = 'block';
	document.getElementById("submitbutton").value = "Sign Up";
	gLogin = false;
}

function toggleLogin(){
	if ( gLogin ) {
		userSignup();
	} else {
		userLogin();
	}
}

function userAction(){

	if ( gLogin ) {
		return login();
	} else {
		return createUser();
	}
}

function login() {

	gName = document.forms["userForm"]["username"].value;
	var password = document.forms["userForm"]["password"].value;
	if (password == null || password == "") {
		alert("Password cannot be empty");
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
	params.username = gName;
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
				setCookie("username", gName);
				setCookie("best", jsonResp.best);
				setCookie("guest",false);
				document.getElementById("user").style.display = 'none';
				location.href='upload.html';
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
	var password = document.forms["userForm"]["password"].value;
	if (password == null || password == "") {
		alert("Password cannot be empty");
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
	var paramString = JSON.stringify(params);

	userRequest.open("POST", "user", true);
	userRequest.onreadystatechange = function() {
		if (userRequest.readyState == 4) {
			if ( userRequest.status == 401 ) {
				loginFailed("User Create Failed!");
			} else {
				setCookie("token",userRequest.responseText);
				setCookie("username",gName);
				setCookie("guest",false);
				setCookie("best",-1);
				document.getElementById("user").style.display = 'none';
				location.href='upload.html';
			}
		}
	};

	//Send the proper header information along with the request
	userRequest.setRequestHeader("Content-type",
	"application/x-www-form-urlencoded");
	userRequest.send(paramString);
	return false;
}

function requestToken() {

	var token = getCookie("token");

	var userRequest;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		userRequest = new XMLHttpRequest();
	} else {// code for IE6, IE5
		userRequest = new ActiveXObject("Microsoft.XMLHTTP");
	}

	var params = new Object();
	params.command = "request_token";
	params.token = token;
	var paramString = JSON.stringify(params);

	userRequest.open("POST", "game", true);
	userRequest.onreadystatechange = function() {
		if (userRequest.readyState == 4) {
			if ( userRequest.status == 200 ) {
				setCookie("token",userRequest.responseText);
				setCookie("guest",true);
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