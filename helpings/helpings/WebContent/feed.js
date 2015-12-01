//Keep track of how much data we have received from the server
var gResponsePtr = 0;
var gInput;
var gLogin = false;
var gUsername = "";
var gToken = "";

window.onload = function() {
	sendFeedRequest();
	gUsername = getCookie("username");
	gToken = getCookie("token");
}

function logout() {
	setCookie("token", "");
	document.getElementById("loggedin").style.display = 'none';
	document.getElementById("loggedout").style.display = 'block';
	window.location = "/helpings/";
}

function addNewPost(postInfo) {

	var post = postInfo.post;

	var template = document.getElementById("templatepost");
	var newpost = template.cloneNode(true);
	newpost.querySelector("#beforethumb").src = "/helpings/image/thumb"
			+ post.beforeimage;
	newpost.querySelector("#title").innerHTML = post.title;
	newpost.querySelector("#username").innerHTML = post.username;
	newpost.querySelector("#username").href = "/helpings/users/"
			+ post.username;
	newpost.querySelector("#date").innerHTML = timeConverter(post.date);

	var form = newpost.querySelector("#calform");
	form.setAttribute("data_postid", post.rowid);
	newpost.style.display = "";
	var children = newpost.children;
	for (var i = 0; i < children.length; i++) {
		var child = children[i];
		console.log("child.id = " + child.id);
		if (child.id != "") {
			child.id = child.id + "_" + post.rowid;
		}
	}
	document.body.appendChild(newpost);
	if (postInfo.userguess != undefined && postInfo.userguess > 0) {
		document.getElementById("calories_" + post.rowid).innerHTML = "Average: "
				+ postInfo.average;
		document.getElementById("guess_" + post.rowid).style.display = 'block';
		document.getElementById("calories_" + post.rowid).style.display = 'block';
		document.getElementById("guess_" + post.rowid).innerHTML = "Your Guess: "
				+ postInfo.userguess;
		document.getElementById("calform_" + post.rowid).style.display = 'none';
	}
}

function sendFeedRequest() {

	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	} else {// code for IE6, IE5
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}

	var url = window.location.href;
	if (location.search.lastIndexOf('?') > 0) {
		url = url + "&";
	} else {
		url = url + "?";
	}
	url = url + "json=true&username=" + gUsername + "&token=" + gToken;
	console.log(url);
	xmlhttp.open("GET", url, true);
	xmlhttp.onreadystatechange = handleFeedResponse;

	// Send the proper header information along with the request
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
		// if it parses sucessfully. When it does, we know we have a complete
		// command
		console.log(xmlResp);

		var jsonResp = JSON.parse(xmlResp);
		if (jsonResp == null) {
			return;
		}
		console.log(jsonResp);
		for (var i = 0; i < jsonResp.length; i++) {
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

	document.getElementById("guess_" + post_id).innerHTML = "Your Guess: "
			+ calories;
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

	// Send the proper header information along with the request
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
		// if it parses sucessfully. When it does, we know we have a complete
		// command
		var jsonResp = JSON.parse(xmlResp);
		if (jsonResp == null) {
			return;
		}
		console.log(jsonResp);
		var post = jsonResp.post;
		document.getElementById("calories_" + post).innerHTML = "Average: "
				+ jsonResp.calories;
		document.getElementById("guess_" + post).style.display = 'block';
		document.getElementById("calories_" + post).style.display = 'block';
		gResponsePtr = len;

	} else if (xmlhttp.readyState == 4) {
		gResponsePtr = 0;
	}
}