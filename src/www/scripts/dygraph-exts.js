/*
 * Javascript wrapper around the dygraph graphing system to
 * allow selectable data traces. We save the selection in a
 * cookie so each reload only shows the current set of traces.
 *
 * We have buttons that allow easy manipulation of the traces.
 */
<!-- Preload the images please -->
var img_selectFull = new Image(); img_selectFull.src = "/images/selectFull.png";
var img_selectHalf = new Image(); img_selectHalf.src = "/images/selectHalf.png";
var img_selectNone = new Image(); img_selectNone.src = "/images/selectNone.png";
var img_arrowDown = new Image(); img_arrowDown.src = "/images/arrowDown.png";
var img_arrowRight = new Image(); img_arrowRight.src = "/images/arrowRight.png";
var defaultVisibility = "none";         // none or block
var defaultArrow = "";
if (defaultVisibility == "none") {
	defaultArrow = "arrowRight";    // arrowRight or arrowDown
} else {
	defaultVisibility = "block";
	defaultArrow = "arrowDown";
}
var menu = {};
menu.entries = [];      // list of menu entries in order (used to index the check box devices
menu.depth = [];        // depth of each entry
menu.groups = [];       // list of menu groups
menu.completed = [];    // keep track of which added to menu
var baseLabelsTmp = baseLabelsStr.split(',');
var baseLabels = [];
var ignored = 0;	// count of ignored entries
// remove date label and strip leading spaces

var max_depth = 0;
for (i = 1; i < baseLabelsTmp.length; i++) {
	var l = baseLabelsTmp[i].trim();
	var d = l.split('/').length;
	baseLabels.push(l);
	menu.entries.push(l);
	menu.depth.push(d);
	if (d > max_depth) {
		max_depth = d;
	}
}

baseLabelsTmp = [];	// release memory
baseLabelsLen = baseLabels.length;
var visList = []
for (var i = 0; i < baseLabelsLen; i++) {
	visList.push(true);
}

for (var d = 2; d <= max_depth; d++) {
	for (var i = 0; i < menu.entries.length; i++) {
		// add group names of 'd' depth
		if (menu.depth[i] != d) {
			continue;
		}
		var l = menu.entries[i];
		var index = l.lastIndexOf('/');
		var group = l.substring(0, index);
		var found = false;
		for (var j = 0; j < menu.groups.length; j++) {
			if (menu.groups[j] == group) {
				found = true;
			}
		}
		if (!found) {
			menu.groups.push(group);
			menu.completed.push(0);
		}
	}
}

// Create the html to perform the menu and push it into the division.

var mstr = "<ul>\n";
for (var i = 0; i < menu.entries.length; i++) {
	if (menu.entries[i].indexOf('/') == -1) {
		// base level entry (global), put first
		mstr += '<li style="list-style-type:none"><img src="/images/blank.png"> <img src="/images/selectFull.png" id="cb' + i + '" onclick="doSelect(' + "'cb" + i + "', ''" + ')" checked="Full"> </img><span onclick="doSelect(' + "'cb" + i + "', ''" + ')">' + menu.entries[i] + '</span><br></li>\n';
	}
}

function doSetupGroup(g) {
	if (menu.completed[g] == 1) {
		return;
	}
	var gs = menu.groups[g];
	var gsl = gs.length;
	var gs_name = gs;
	if (gs.lastIndexOf('/') != -1) {
		gs_name = gs.substring(gs.lastIndexOf('/') + 1);
	}
	mstr += '<li style="list-style-type:none"><img id="im_div_' + gs + '" src="/images/' + defaultArrow + '.png" onclick="doCascade(' + "'" + gs + "'" + ')"> <img src="/images/selectFull.png" id="cc_' + gs + '" onclick="doCascadeSelect(' + "'cc_" + gs + "', '" + gs + "'" + ')" checked="Full"> </img><span onclick="doCascade(' + "'" + gs + "'" + ')">' + gs_name + '</span><br></li>\n';
	mstr += '<div id="div_' + gs + '" style="display: ' + defaultVisibility + '">\n<ul>\n';
	for (var i = 0; i < menu.entries.length; i++) {
		if (menu.entries[i].lastIndexOf('/') == gsl && menu.entries[i].substring(0, gsl) == gs) {
			var me = menu.entries[i].substring(menu.entries[i].lastIndexOf('/') + 1);
			mstr += '<li style="list-style-type:none"><img src="/images/blank.png"> <img src="/images/selectFull.png" id="cb' + i + '" onclick="doSelect(' + "'cb" + i + "', '" + gs + "'" + ')" checked="Full"> </img><span onclick="doSelect(' + "'cb" + i + "', '" + gs + "'" + ')">' + me + '</span><br></li>\n';
		}
	}
	var gss = gs + '/';
	var gssl = gss.length;
	for (var gsi = g + 1; gsi < menu.groups.length; gsi++) {
		// add in the sub/sub/groups here
		if (menu.groups[gsi].length > gssl && menu.groups[gsi].substring(0, gssl) == gss) {
			doSetupGroup(gsi);
		}
	}
	mstr += '</ul>\n</div>\n'
	menu.completed[g] = 1;
}

for (var g = 0; g < menu.groups.length; g++) {
	doSetupGroup(g);
}
mstr += "</ul>\n";
document.getElementById("optionHTML").innerHTML=mstr;
button_set_all();	// set the checked values for all elements

function doSetGroup(group) {
	// set the group value for this group (and it's parent too!)
	var fgroup = group + '/';
	var len = fgroup.length;
	// now set the group to the correct value
	var checked = 0;
	var unchecked = 0;
	for (var i = 0; i < menu.entries.length; i++) {
		if (menu.entries[i].substring(0, len) == fgroup) {
			if (document.getElementById('cb' + i).checked == "Full") {
				checked++;
			} else {
				unchecked++;
			}
		}
	}
	var e = document.getElementById("cc_" + group);
	if (e != null) {
		if (checked > 0 && unchecked == 0) {
			e.src = "/images/selectFull.png";
			e.checked = "Full";
		} else if (checked == 0 && unchecked > 0) {
			e.src = "/images/selectNone.png";
			e.checked = "None";
		} else {
			e.src = "/images/selectHalf.png";
			e.checked = "Half";
		}
	}
}

function doSetUpGroup(group) {
	doSetGroup(group);
	var i = group.indexOf('/');
	if (i != -1) {
		doSetUpGroup(group.substring(0, i));
	}
}

function doSetDownGroup(group) {
	doSetGroup(group);
	var gs = group + '/';
	var gl = gs.length;
	for (var i = 0; i < menu.groups.length; i++) {
		// check in the sub/sub/groups here
		if (menu.groups[i].length > gl && menu.groups[i].substring(0, gl) == gs) {
			doSetGroup(menu.groups[i]);
		}
	}
}

function doSet(id) {
	var i=document.getElementById(id);
	i.checked = "Full";
	i.src = "/images/selectFull.png";
}
function doClear(id) {
	var i=document.getElementById(id);
	i.checked = "None";
	i.src = "/images/selectNone.png";
}
function doSelect(id, group) {
	var i=document.getElementById(id);
	if (i.checked == "Full") {
		i.checked = "None";
		i.src = "/images/selectNone.png";
	} else {
		i.checked = "Full";
		i.src = "/images/selectFull.png";
	}
	if (group != '') {
		doSetUpGroup(group);
	}
}

function doCascadeSelect(id, group) {
	var e=document.getElementById(id);
	if (e.checked == "Full") {
		e.checked = "None";
		e.src = "/images/selectNone.png";
	} else {
		e.checked = "Full";
		e.src = "/images/selectFull.png";
	}
	// now set all members of the group
	var fgroup = group + '/';
	var len = fgroup.length;
	for (var i = 0; i < menu.entries.length; i++) {
		if (menu.entries[i].substring(0, len) == fgroup) {
			c = document.getElementById('cb' + i);
			c.checked = e.checked;
			c.src = e.src;
		}
	}
	doSetDownGroup(group);
	doSetUpGroup(group);
}

function doShowCascade(id) {
	var p=document.getElementById("div_" + id);
	var i=document.getElementById("im_div_" + id);
	p.style.display = "block";
	i.src = "/images/arrowDown.png";
}

function doHideCascade(id) {
	var p=document.getElementById("div_" + id);
	var i=document.getElementById("im_div_" + id);
	p.style.display = "none";
	i.src = "/images/arrowRight.png";
}

function doShowAllCascade() {
	for (var i = 0; i < menu.groups.length; i++) {
		doShowCascade(menu.groups[i]);
	}
}

function doHideAllCascade() {
	for (var i = 0; i < menu.groups.length; i++) {
		doHideCascade(menu.groups[i]);
	}
}

function doCascade(id) {
	var p=document.getElementById("div_" + id);
	var i=document.getElementById("im_div_" + id);
	if (p.style.display == "none") {
		p.style.display = "block";
		i.src = "/images/arrowDown.png";
	} else {
		p.style.display = "none";
		i.src = "/images/arrowRight.png";
	}
}
function doResetFromCookies() {
	//
	// We can use saved cookies to preserve the selections and open cascades between reloads.
	// Set the default selections here (cb#)
	var tdu_cookie = getCookie("dataLoggerIgnore");
	if (tdu_cookie != "") {
		var tdu_ignore_array = tdu_cookie.split(',');
		for (var i = 0; i < tdu_ignore_array.length; i++) {
			var index = parseInt(tdu_ignore_array[i]);
			if (index < menu.entries.length) {
				doClear('cb' + index);
				visList[index] = false;
				ignored++;
			}
		}
	}

	// Set the default cascades (menu.groups[#])
	var tdu_cascade = getCookie("dataLoggerCascade");
	if (tdu_cascade != "") {
		var tdu_cascade_array = tdu_cascade.split(',');
		for (var i = 0; i < tdu_cascade_array.length; i++) {
			var index = parseInt(tdu_cascade_array[i]);
			if (index < menu.groups.length) {
				doCascade(menu.groups[index]);
			}
		}
	}
	doRefreshGroups();
}

function setIgnoreCookie() {
	var cvalue = "";
	for (var i = 0; i < menu.entries.length; i++) {
		e = document.getElementById('cb' + i);
		if (e.checked != "Full") {
			if (cvalue != "") {
				cvalue += ",";
			}
			cvalue += i;
		}
	}
	setCookie("dataLoggerIgnore", cvalue, 30);
}

function setCascadeCookie() {
	var cvalue = "";
	for (var i = 0; i < menu.groups.length; i++) {
		e = document.getElementById("div_" + menu.groups[i]);
		if (e.style.display != "none") {
			if (cvalue != "") {
				cvalue += ",";
			}
			cvalue += i;
		}
	}
	setCookie("dataLoggerCascade", cvalue, 30);
}

// from http://www.w3schools.com/js/js_cookies.asp
function setCookie(cname, cvalue, exdays) {
	var d = new Date();
	d.setTime(d.getTime() + (exdays*24*60*60*1000));
	var expires = "expires="+d.toUTCString();
	document.cookie = cname + "=" + cvalue + "; " + expires;
}

function getCookie(cname) {
	var name = cname + "=";
	var ca = document.cookie.split(';');
	for(var i=0; i<ca.length; i++) {
		var c = ca[i].trim();
		if (c.indexOf(name) == 0) {
			return c.substring(name.length, c.length);
		}
	}
	return "";
}

function doRefreshGroups() {
	for (var i = 0; i < menu.groups.length; i++) {
		doSetGroup(menu.groups[i]);
	}
}


function button_set_all() {
	for (var i = 0; i < menu.entries.length; i++) {
		doSet('cb' + i);
	}
	doRefreshGroups();
}

function button_clear_all() {
	for (var i = 0; i < menu.entries.length; i++) {
		doClear('cb' + i);
	}
	doRefreshGroups();
}

function button_set_visible() {
	doRefreshGroups();
	setIgnoreCookie();
	setCascadeCookie();
}

doResetFromCookies();

// initially show the dygraph with all traces to set the labels
dyg = new Dygraph(document.getElementById("graphdiv"), fn, {
  legend: 'always',
  showRoller: true,
  visibility: visList
});

function button_set_visible() {
	// set the visibility from the overlay and set ignored to the new value
	var l;

	document.getElementById("optionDiv").style.display = "none";
	ignored = 0;
	for (l = 0; l < baseLabelsLen; l++) {
		var vis = (document.getElementById("cb" + l).checked == "Full");
		dyg.changeVisibility(l, vis);
		if (vis != visList[l]) {
			dyg.changeVisibility(l, vis);
		}
		visList[l] = vis;
		if (vis == false) {
			ignored++;
		}
	}
	dyg.finalVisibility();
	doRefreshGroups();
	setIgnoreCookie();
	setCascadeCookie();
	checkCookie();
}
function button_remaining_visible() {
	// set the visibility of all traces
	for (l = 0; l < baseLabelsLen; l++) {
		if (visList[l] == false) {
			dyg.changeVisibility(l, true);
			visList[l] = true;
		}
	}
	dyg.finalVisibility();
	button_set_all();
	ignored = 0;
	setIgnoreCookie();
	checkCookie();
}
function button_select() {
	document.getElementById("optionDiv").style.display = "block";
}
function button_reload() {
	dyg.updateOptions({file: fn}, true);
}
function checkCookie() {
	var b6 = document.getElementById("b6");
	if (ignored != 0) {
		b6.innerHTML = 'Show Other ' + ignored + ' Traces';
		b6.disabled = false;
	} else {
		b6.innerHTML = 'All Traces Shown';
		b6.disabled = true;
	}
}
