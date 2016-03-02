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
		var index = ca[i].indexOf('=');
		if ( index > 0 ) {
			var obj = new Object;
			obj.key = ca[i].substring(1, index);
			obj.value = ca[i].substring(index + 1, ca[i].length);
			gCookies.push(obj);
		}
	}
}

function saveCookies(days){
	 
	var date = new Date();

	// Default at 365 days.
	days = days || 365;

	// Get unix milliseconds at current time plus number of days
	date.setTime(+ date + (days * 86400000)); //24 * 60 * 60 * 1000

	var cookieString="";
	for( var i=0; i<gCookies.length; i++){
		cookieString = gCookies[i].key + '=' + gCookies[i].value + '; expires=' + date.toUTCString() +  '; path=/';
		document.cookie=cookieString;
	}
}

function testPassword(password){
	if ( password.length < 8 ) {
		alert("Your password must be at least eight characters.");
		return false;
	}
	return true;
}

function timeConverter(millis) {
	var a = new Date(millis);
	var months = [ 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug',
			'Sep', 'Oct', 'Nov', 'Dec' ];
	var year = a.getFullYear();
	var month = months[a.getMonth()];
	var date = a.getDate();
	var hour = a.getHours();
	var pm = false;
	if ( hour > 12 ) {
		hour -= 12;
		pm = true;
	}
	if ( hour == 0 ) {
		hour = 12;
	}
	var min = a.getMinutes();
	min = min < 10 ? ('0' + min) : min;
	var sec = a.getSeconds();
	var time = ' ' + hour + ':' + min;
	if ( pm ) {
		time = time + " PM";
	} else {
		time = time + " AM";
	}
	var date = month + ' ' + date;
	if ( year != new Date().getFullYear() ) {
		date += ' ' +  year;
	}
	return time + " " + date;
}

function mobileCheck() {
	var check = false;
	(function(a) {
		if (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i
				.test(a)
				|| /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i
						.test(a.substr(0, 4)))
			check = true
	})(navigator.userAgent || navigator.vendor || window.opera);
	return check;
}