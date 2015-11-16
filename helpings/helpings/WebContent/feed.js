//Keep track of how much data we have received from the server
var gResponsePtr = 0;
var gInput;
var gCookies = new Array();
var gLogin = false;


window.onload = function() {
	sendFeedRequest();
	var username = getCookie("username");
	if ( username == undefined || username == "" ) {
		document.getElementById("loggedout").style.display= 'block';
	} else {
		document.getElementById("loggedin").style.display= 'block';
		document.getElementById("loggedinuser").innerHTML = username;
	}
	userSignup();
}

function logout(){
	setCookie("token","");
	setCookie("username","");
	document.getElementById("loggedin").style.display= 'none';
	document.getElementById("loggedout").style.display= 'block';
	location.reload();
}

function addNewPost(postInfo){

	var post = postInfo.post;

	var template = document.getElementById("templatepost");
	var newpost = template.cloneNode(true);
	newpost.querySelector("#beforethumb").src = "/helpings/image/thumb" + post.beforeimage;
	newpost.querySelector("#title").innerHTML = post.title;
	newpost.querySelector("#username").innerHTML = post.username;
	newpost.querySelector("#username").href = "/helpings/users/" + post.username;
	newpost.querySelector("#date").innerHTML = timeConverter(post.date);

	var form = newpost.querySelector("#calform");
	form.setAttribute("data_postid", post.rowid);
	newpost.style.display = "block";
	var children = newpost.children;
	for (var i = 0; i < children.length; i++) {
		var child = children[i];
		console.log("child.id = " + child.id);
		if ( child.id != "" ) {
			child.id = child.id + "_" + post.rowid;
		}
	}
	document.body.appendChild(newpost);
	if ( postInfo.userguess != undefined && postInfo.userguess > 0 ) {
		document.getElementById("calories_" + post.rowid).innerHTML = "Average: " + postInfo.average;
		document.getElementById("guess_" + post.rowid).style.display = 'block';
		document.getElementById("calories_" + post.rowid).style.display = 'block';
		document.getElementById("guess_" + post.rowid).innerHTML = "Your Guess: " + postInfo.userguess;
		document.getElementById("calform_" + post.rowid).style.display = 'none';
	}
}

function sendFeedRequest() {

	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	} else {// code for IE6, IE5
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	var username = getCookie("username");
	var token = getCookie("token");

	var url = window.location.href;
	if ( location.search.lastIndexOf('?') > 0 ) {
		url = url + "&";
	} else {
		url = url + "?";
	}
	url = url + "json=true&username=" + username + "&token=" + token;
	xmlhttp.open("GET", url, true);
	xmlhttp.onreadystatechange = handleFeedResponse;

	//Send the proper header information along with the request
	xmlhttp.setRequestHeader("Content-type",
	"application/x-www-form-urlencoded");
	xmlhttp.send(null);
	return false;
}

function handleFeedResponse() {
	var len = xmlhttp.responseText.length;
	var xmlResp = xmlhttp.responseText.substring(gResponsePtr, len);

	if (xmlhttp.readyState == 3) {

		// Success! Reset retries
		gRetryCount = 0;

		// This is really kind of ugly. We keep feeding more data in and seeing
		// if it parses sucessfully.  When it does, we know we have a complete command
		var jsonResp = JSON.parse(xmlResp);
		if (jsonResp == null) {
			return;
		}
		console.log(jsonResp);
		for( var i = 0; i < jsonResp.length; i++ ) {
			var postInfo = jsonResp[i];
			addNewPost(postInfo);
			console.log(postInfo);
		}
		gResponsePtr = len;

	} else if (xmlhttp.readyState == 4) {
		gResponsePtr = 0;
	}
}

function sendGuess(input) {

	gInput = input;
	console.log(input);
	var calories = input["calories"].value;
	var post_id = input.getAttribute("data_postid");

	document.getElementById("guess_" + post_id).innerHTML = "Your Guess: " + calories;
	input.style.display = 'none';

	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	} else {// code for IE6, IE5
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}

	var params = new Object();
	params.calories = calories;
	params.post = post_id;
	params.username = getCookie("username");
	params.token = getCookie("token");
	var paramString = JSON.stringify(params);

	xmlhttp.open("POST", "guess", true);
	xmlhttp.onreadystatechange = handleGuessResponse;

	//Send the proper header information along with the request
	xmlhttp.setRequestHeader("Content-type",
	"application/x-www-form-urlencoded");
	xmlhttp.send(paramString);
	console.log(paramString);
	return false;
}

function handleGuessResponse() {
	var len = xmlhttp.responseText.length;
	var xmlResp = xmlhttp.responseText.substring(gResponsePtr, len);

	if (xmlhttp.readyState == 3) {

		// Success! Reset retries
		gRetryCount = 0;

		// This is really kind of ugly. We keep feeding more data in and seeing
		// if it parses sucessfully.  When it does, we know we have a complete command
		var jsonResp = JSON.parse(xmlResp);
		if (jsonResp == null) {
			return;
		}
		console.log(jsonResp);
		var post = jsonResp.post;
		document.getElementById("calories_" + post).innerHTML = "Average: " + jsonResp.calories;
		document.getElementById("guess_" + post).style.display = 'block';
		document.getElementById("calories_" + post).style.display = 'block';
		gResponsePtr = len;

	} else if (xmlhttp.readyState == 4) {
		gResponsePtr = 0;
	}
}

function timeConverter(millis){
	var a = new Date(millis);
	var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
	var year = a.getFullYear();
	var month = months[a.getMonth()];
	var date = a.getDate();
	var hour = a.getHours();
	var min = a.getMinutes();
	var sec = a.getSeconds();
	var time = date + ' ' + month + ' ' + year + ' ' + hour + ':' + min + ':' + sec ;
	return time;
}

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
	document.getElementById("toggleLoginButton").innerHTML = "Sign Up";
	document.getElementById("loginheader").innerHTML = "Log in";
	gLogin = true;
}

function userSignup(){
	document.getElementById("username").style.display = 'block';
	document.getElementById("submitbutton").value = "Sign Up";
	document.getElementById("toggleLoginButton").innerHTML = "Log in";
	document.getElementById("loginheader").innerHTML = "Sign Up";
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
		loginFailed("Password cannot be empty");
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
				window.location.reload(true); 
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
		loginFailed("Password cannot be empty");
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
				window.location.reload(true); 
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
	document.getElementById("messageText").innerHTML = message;
}

function clearMessage(){
	document.getElementById("message").style.display = 'none';
}