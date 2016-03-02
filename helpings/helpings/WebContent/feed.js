//Keep track of how much data we have received from the server
var gResponsePtr = 0;
var gInput;
var gUsername = "";
var gToken = "";

window.onload = function() {

	gToken = getCookie("token");
	if ( gToken == undefined || gToken.length == 0) {
		document.getElementById("postbutton").innerHTML = "Sign Up";
		document.getElementById("postbutton").href = "/login.html";
		document.getElementById("logout").style.display = "none";
	} else {
		document.getElementById('postbutton').onclick = function() {
			document.getElementById('uploadinput').click();
		};
	}
	sendFeedRequest();
}

function logout() {
	setCookie("token", "");
	window.location = "/";
}

function nextPage() {
	var url = window.location.pathname;
	if ( url == "/" ) {
		url = "/feed"
	}
	var lastSlash = url.lastIndexOf('/');
	var lastSegment = url.substring(lastSlash + 1, url.length);
	if ( !isNaN(lastSegment) ) {
		var nextPage = parseInt(lastSegment) + 1;
		var base = url.substring(0,lastSlash);
		window.location = base + "/" +  nextPage;
	} else {
		window.location = url + "/2";
	}
	
}

function addNewPost(postInfo) {

	var post = postInfo.post;

	var template = document.getElementById("templatepost");
	var newpost = template.cloneNode(true);
	var imageName = "https://s3-us-west-2.amazonaws.com/estiplate/thumbs/thumb"
		+ post.beforeimage;
	newpost.querySelector(".foodPic").src = imageName;
	newpost.querySelector(".foodThumb").src = imageName;
	newpost.querySelector("#title").innerHTML = post.title;
	newpost.querySelector("#titletwo").innerHTML = post.title;
	var tagDiv = newpost.querySelector("#tags");
	var tagTwoDiv = newpost.querySelector("#tagstwo");
	if ( post.tags != undefined) {
		var tagList = JSON.parse(post.tags);
		addTags( tagTwoDiv, tagList);
		addTags( tagDiv, tagList);
	} else {
		tagDiv.style.display = "none";
		tagTwoDiv.style.display = "none";
	}
	var username = post.username;
	if ( post.mostaccurate ) {
		username += " ☆";
		newpost.querySelector("#username").title = "Most Accurate";
	}
	if ( post.mostguesses ) {
		username += " ①";
		newpost.querySelector("#username").title = "Most Guesses";
	}

	newpost.querySelector("#username").innerHTML = username;
	newpost.querySelector("#username").href = "/users/"
			+ post.username;
	newpost.querySelector("#date").innerHTML = timeConverter(post.date);
	if ( post.username == gUsername ) {
		newpost.querySelector("#deletepost").style.display = "block";
	}
	
	var form = newpost.querySelector("#calform");
	form.setAttribute("data_postid", post.rowid);
	newpost.style.display = "";
	newpost.id="post_" + post.rowid;
	renameChildren(newpost, post.rowid)
	document.getElementById("posts").appendChild(newpost);
	if (postInfo.userguess != undefined && postInfo.userguess > 0) {
		newpost.querySelector(".card").classList.toggle('flipped');
		populateGuess( postInfo.average, postInfo.userguess, postInfo.guesscount, post.rowid);
		addComments( postInfo.comments, post.rowid);
	} else {
		showComments(post.rowid, false);
		showGuess(post.rowid, false);
	}
}

function addTags( tagDiv, tagList ) {
	for (var i = 0; i < tagList.length; i++ ) {
		var tag = tagList[i];
		var tagSpan = document.createElement('a')
		tagSpan.href = "/tag/" + tag;
		tagSpan.innerHTML = tag;
		tagSpan.className = "tag";
		tagDiv.appendChild(tagSpan);
	}
}

function populateGuess(average, guess, guesscount, post) {
	document.getElementById("calories_" + post).innerHTML = "Ave: "
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
		document.getElementById("commentarea_" + post).style.display = 'block';
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

	var url = window.location.pathname;
	if ( url == "/" ) {
		url = "/feed";
	}
	url = url + "?json=true" + "&token=" + encodeURIComponent(gToken) + "&username=" + gUsername;
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

	if ( gToken == undefined || gToken.length == 0) {
		alert("Please create an account to guess!");
		return;
	}

	gInput = input;
	console.log(input);
	var calories = input["calories"].value;
	var post_id = input.getAttribute("data_postid");
	document.getElementById('card_' + post_id).classList.toggle('flipped');

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

	xmlhttp.open("POST", "/guess", true);
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
		return;
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

	xmlhttp.open("POST", "/comment", true);
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

function deletePost(post_id) {

	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	} else {// code for IE6, IE5
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}

	var params = new Object();
	params.post = post_id;
	params.username = getCookie("username");
	params.token = getCookie("token");
	var paramString = JSON.stringify(params);

	xmlhttp.open("POST", "/delete", true);
	xmlhttp.onreadystatechange = handleDeleteResponse;

	// Send the proper header information along with the request
	xmlhttp.setRequestHeader("Content-type",
			"application/x-www-form-urlencoded");
	xmlhttp.send(paramString);
	console.log(paramString);
	return false;
}

function handleDeleteResponse() {
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
		var el = document.getElementById("post_" + postId);
		el.parentNode.removeChild( el );
		gResponsePtr = len;

	} else if (xmlhttp.readyState == 4) {
		gResponsePtr = 0;
	}
}

function onCardClick(card){
	var post_id = card.id.split("_")[1];
	console.log(event.target + " " + event.target.itemType + " " + event.target.class );
	if ( event.target.id == "deletepost_" +  post_id) {
		if ( confirm ("Are you sure you want to delete this post?") ) {
			deletePost(post_id);
		}
		return;
	}
	if ( event.target.className == "tag" ) { 
		return;
	}
	if ( ( event.target.type != 'textarea') && ( event.target.type != 'submit' ) )  {
		if( document.getElementById("postinfo_" + post_id).style.display != 'none' ) {
			card.classList.toggle('flipped');
		}
	}
}

var img;
function readURL(input) {

	window.scrollTo(0, 0);
	if (input.files && input.files[0]) {
		var reader = new FileReader();

		reader.addEventListener("load", function() {
			img = new Image;
			img.onload = loadImage;
			img.src = reader.result;
		});

		reader.readAsDataURL(input.files[0]);
		document.getElementById("uploadContainer").style.display = 'block';
		document.getElementById("cover").style.display = 'block';
	}

}

function loadImage(){ 
	var img = this;
	var orientation;
	EXIF.getData(img, function() {
		orientation = EXIF.getTag(img, "Orientation");
    });
	var myCanvas = document.getElementById('preview');
	var ctx = myCanvas.getContext('2d');
	ctx.clearRect(0, 0, myCanvas.width, myCanvas.height);
	var imageSize = myCanvas.width;
	var h = img.height;
	var w = img.width;
	var unscaledSize;
	var x = 0;
	var y = 0;
	if ( w > h ) {
		x = ( w - h ) / 2;
		unscaledSize = h;
	} else if ( h > w ) {
		y = ( h - w ) / 2;
		unscaledSize = w;
	}
	ctx.setTransform(1, 0, 0, 1, 0, 0);
	fixRotation(ctx, orientation, imageSize);
	ctx.drawImage(img, x, y, unscaledSize, unscaledSize, 0, 0, imageSize, imageSize);
	myCanvas.style.display = 'block';
}

function fixRotation( ctx, orientation, imageSize ) {

	if ( orientation == 2 ) {
		ctx.setTransform(-1, 0, 0, 1, imageSize, 0);
	} else if ( orientation == 3 ) {
		ctx.setTransform(-1, 0, 0, -1, imageSize, imageSize );
	} else if ( orientation == 4 ) {
		ctx.setTransform(1, 0, 0, -1, 0, imageSize );
	} else if ( orientation == 5 ) {
		ctx.setTransform(0, 1, 1, 0, 0, 0);
	} else if ( orientation == 6 ) {
		ctx.setTransform(0, 1, -1, 0, imageSize , 0);
	} else if ( orientation == 7 ) {
		ctx.setTransform(0, -1, -1, 0, imageSize , imageSize);
	} else if ( orientation == 8 ) {
		ctx.setTransform(0, -1, 1, 0, 0, imageSize);
	} else {
		ctx.setTransform(1, 0, 0, 1, 0, 0);
	}
}
function cancelUpload() {
	document.getElementById("cover").style.display = 'none';
	document.getElementById("uploadContainer").style.display = 'none';
}

function addTag(e) {
	if (e && e.keyCode == 13) {
		var taginput = document.getElementById("addtag");
		var tag = taginput.value.toLowerCase();
		taginput.value = "";
		var tags = document.getElementById("tags");
		var tagSpan = document.createElement('span')
		tagSpan.innerHTML = tag + " x";
		tagSpan.className = "tag";
		tagSpan.setAttribute('onclick', 'removeTag(this)');
		tags.appendChild(tagSpan);
		return false;
	}
	return true;
}

function removeTag(span) {
	var tag = span.innerHTML;
	var tags = document.getElementById("tags");
	var taglist = tags.children;
	for (var i = 0; i < taglist.length; i++) {
		if (taglist[i].innerHTML == tag) {
			tags.removeChild(taglist[i]);
		}
	}
}

var submitting = false;
function addTagsAndSubmit() {

	if ( submitting ) {
		return;
	}
	submitting = true;
	document.getElementById("uploadOverlay").style.display = 'block';

	var tags = document.getElementById("tags");
	var taglist = tags.children;
	var tagarray = [];
	for (var i = 0; i < taglist.length; i++) {
		if (taglist[i].className == "tag") {
			var tag = taglist[i].innerHTML;
			tagarray.push(tag.substring(0, tag.length - 2));
		}
	}
	var tagJSON = JSON.stringify(tagarray);

	var canvas = document.getElementById('preview');
	var dataURL = canvas.toDataURL('image/jpeg');
	var blob = dataURItoBlob(dataURL);

	var description = document.getElementById('titleinput').value;

	var fd = new FormData();
	fd.append("canvasImage", blob);
	fd.append("username", getCookie("username"));
	fd.append("token", getCookie("token"));
	fd.append("tags", tagJSON);
	fd.append("title", description);
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	} else {// code for IE6, IE5
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}	
	xmlhttp.onreadystatechange = handlePostResponse;
	xmlhttp.open("POST", "upload");
	xmlhttp.send(fd);
}

function dataURItoBlob(dataURI) {
    var byteString = atob(dataURI.split(',')[1]);
    var ab = new ArrayBuffer(byteString.length);
    var ia = new Uint8Array(ab);
    for (var i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }
    return new Blob([ab], { type: 'image/jpeg' });
}

function handlePostResponse(){
	if (xmlhttp.readyState == 4) {
		location.reload();
	}
}
