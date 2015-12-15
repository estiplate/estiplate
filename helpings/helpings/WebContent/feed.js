//Keep track of how much data we have received from the server
var gResponsePtr = 0;
var gInput;
var gLogin = false;
var gUsername = "";
var gToken = "";

window.onload = function() {

	gToken = getCookie("token");
	if ( gToken == undefined || gToken.length == 0) {
		document.getElementById("postbutton").value = "Login";
	}
	sendFeedRequest();
}

function logout() {
	setCookie("token", "");
	window.location = "/helpings/";
}

function nextPage() {
	var url = window.location.href;
	var lastSlash = url.lastIndexOf('/');
	var lastSegment = url.substring(lastSlash + 1, url.length);
	if ( !isNaN(lastSegment) ) {
		var nextPage = parseInt(lastSegment) + 1;
		var base = url.substring(0,lastSlash);
		window.location = base + "/" +  nextPage;
	} else {
		window.location = window.location.href + "/2";
	}
	
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
	newpost.id="post_" + postInfo.rowid;
	renameChildren(newpost, post.rowid)
	document.getElementById("posts").appendChild(newpost);
	if (postInfo.userguess != undefined && postInfo.userguess > 0) {
		populateGuess( postInfo.average, postInfo.userguess, postInfo.guesscount, post.rowid);
		addComments( postInfo.comments, post.rowid);
	} else {
		if ( gToken == undefined || gToken.length == 0) { 
			showGuessInput(post.rowid, false);
		}
		showComments(post.rowid, false);
		showGuess(post.rowid, false);
	}
}

function populateGuess(average, guess, guesscount, post) {
	document.getElementById("calories_" + post).innerHTML = "Average: "
		+ average;
	document.getElementById("guess_" + post).innerHTML = "Your Guess: "
		+ guess;
	var averageText;
	if ( guesscount == 1) {
		averageText = "on 1 guess"
	} else {
		averageText = "on " + guesscount + " guesses";
	}
	document.getElementById("guesscount_" + post).innerHTML = averageText;
	if ( guesscount > 1 ) {
		var g = parseInt(guess);
		var a = parseInt(average);
		var error = (g - a) / a * 100;
		var errorString;
		if ( error > 0 ) {
			errorString = "▲" +  error.toFixed(0) + "%";
		} else {
			errorString = "▼" +  Math.abs(error).toFixed(0) + "%";
		}
		var accuracySpan = document.getElementById("guessaccuracy_" + post);
		accuracySpan.innerHTML = errorString;
		error = Math.abs(error);
		if ( error < 10 ) {
			accuracySpan.style.color = "darkgreen";
		} else if ( error < 25 ) {
			accuracySpan.style.color = "gold";
		} else {
			accuracySpan.style.color = "crimson";
		}
	}
	showGuess(post, true);
	showGuessInput(post, false);
}

function renameChildren( element, id ){
	var children = element.children;
	for (var i = 0; i < children.length; i++) {
		var child = children[i];
		if (child.id != "") {
			child.id = child.id + "_" + id;
		}
		if (child.form != "") {
			child.form = child.form + "_" + id;
		}
		renameChildren(child, id);
	}
}

function showComments(post, show) {
	if ( !show ) {
		document.getElementById("commentarea_" + post).style.display = 'none';
	} else {
		document.getElementById("commentarea_" + post).style.display = '';
	}
}

function showGuessInput(post, show) {
	if ( !show ) {
		document.getElementById("calform_" + post).style.display = 'none';
	} else {
		document.getElementById("calform_" + post).style.display = '';
	}
}

function showGuess(post, show) {
	if ( !show ) {
		document.getElementById("postinfo_" + post).style.display = 'none';
	} else {
		document.getElementById("postinfo_" + post).style.display = '';
	}
}

function sendFeedRequest() {

	gUsername = getCookie("username");
	gToken = getCookie("token");
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

	xmlhttp.open("POST", "/helpings/guess", true);
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
		var post = jsonResp.rowid;
		populateGuess( jsonResp.average, jsonResp.userguess, jsonResp.guesscount, post);
		showGuess(post, true);
		showGuessInput(post, false);
		showComments(post, true);

		addComments(jsonResp.comments, post);
		gResponsePtr = len;

	} else if (xmlhttp.readyState == 4) {
		gResponsePtr = 0;
	}
}

function postComment(button) {

	var post_id = button.id.split("_")[1];
	var input = document.getElementById("commentinput_" + post_id);
	var comment = input.value;
	input.value = "";

	if ( comment.length == 0 ) {
		alert("Comment field is empty");
	}
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	} else {// code for IE6, IE5
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}

	var params = new Object();
	params.post = post_id;
	params.username = getCookie("username");
	params.token = getCookie("token");
	params.comment = comment;
	var paramString = JSON.stringify(params);

	xmlhttp.open("POST", "/helpings/comment", true);
	xmlhttp.onreadystatechange = handleCommentResponse;

	// Send the proper header information along with the request
	xmlhttp.setRequestHeader("Content-type",
			"application/x-www-form-urlencoded");
	xmlhttp.send(paramString);
	console.log(paramString);
	return false;
}

function handleCommentResponse() {
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
		var postId = jsonResp.postId;
		var commentList = jsonResp.comments;
		if ( commentList != null ) {
			addComments(commentList, postId);
		}
		gResponsePtr = len;

	} else if (xmlhttp.readyState == 4) {
		gResponsePtr = 0;
	}
}

function addComments( commentList, postId ) {
	console.log(commentList);
	if ( commentList == null ) {
		return;
	}
	var commentWrapper = document.getElementById("comments_" + postId);
	for (var i = 0; i < commentList.length; i++) {
		var comment = commentList[i];
		addComment(commentWrapper, comment);
		console.log(comment);
	}
}

function addComment( commentWrapper, comment ) {
	var template = document.getElementById("templatecomment");
	var newcomment = template.cloneNode(true);
	newcomment.querySelector("#commenttext").innerHTML = comment.comment;
	newcomment.querySelector("#commentmetadata").innerHTML = comment.username + " at " +  timeConverter(comment.date);
	newcomment.style.display = "";
	commentWrapper.appendChild(newcomment);
}

function getMoreComments( link ) {
	console.log(link.id);
}