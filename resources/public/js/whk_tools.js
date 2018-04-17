/* Shared Functions */


/* Parse Filter */
//e: setcode
//f: faction
//t: Type
//x: Text (one word)
//x: "Some Text" (quotes)
//u: true/false (unique)

function parsefilter(f)	{
	var res;
	var outp = {};
	
	var set = /e:(\S+)/;
	res = RegExp(set).exec(f)
	if (res !== null)	{
		outp["setcode"] = {likenocase:res[1].split('|')};
	}
	
	var faction = /f:(\S+)/;
	res = RegExp(faction).exec(f)
	if (res !== null)	{
		outp["Faction"] = {likenocase:res[1].split('|')};
	}
	
	var type = /t:(\S+)/;
	res = RegExp(type).exec(f)
	if (res !== null)	{
		outp["Type"] = {likenocase:res[1].split('|')};
	}
	
	var text = /x:(\S+)/;
	res = RegExp(text).exec(f)
	if (res !== null)	{
		outp["CardText"] = {likenocase:res[1].split('|')};
	}
	
	var claim = /c:([0-9])/;
	res = RegExp(claim).exec(f);
	if (res != null)	{
		outp["Claim"] = res[1];
	}
	
	var unique = /u:(true|false)/;
	res = RegExp(unique).exec(f)
	if (res != null)	{
		outp["Unique"] = res[1] == "true";
	}
	
	var loyal = /l:(true|false)/;
	res = RegExp(loyal).exec(f)
	if (res != null)	{
		outp["Loyal"] = res[1] == "true";
	}
	
	if ($.isEmptyObject(outp) && f != "")	{
		outp["name"] = {likenocase:f.split('|')};
	}
	
	return outp;
}


/* Fisher-Yates Shuffle  */
function shuffle(array) {
	var m = array.length, t, i;

	// While there remain elements to shuffle…
	while (m) {

		// Pick a remaining element…
		i = Math.floor(Math.random() * m--);

		// And swap it with the current element.
		t = array[m];
		array[m] = array[i];
		array[i] = t;
	}

	return array;
}