/* Deckbuilder Script */

var _cards;

$(document).ready(function () {
	
	var filter = {};
	var typFilter = [];
	var facFilter = [];
	var setFilter = [];
  
  var _planets;
  var _sets;
	
	var decklist = TAFFY();
	
  $.getJSON("js/data/wh40k_cards.json",{_: new Date().getTime()}, function (data) {
    _cards = TAFFY(data.data);
    _planets = JSON.parse(_cards({"type_code":"planet"}).stringify());
    
    $.getJSON("js/data/wh40k_packs.json",{_: new Date().getTime()}, function (data) {
      _sets = TAFFY(data.data);
      
      updateSets();
      
      //loadDeck();
      //writedeck();
      //writeoutput();
      //updateTableBody();
    });
  });

/* Decklist */
	$('#decklist').on('click','.card',function () {
		var outp = '';
		var card = _cards({"code":String($(this).data("code"))}).first();
		
		var maxallowed = maxindeck(card);
		var inset = decklist({"code":card.code}).count() != 0 ? decklist({"code":card.code}).first().qty : 0;
		
		var btns = '<div class="btn-group" data-toggle="buttons">';
		for (var i=0; i<=maxallowed; i++) {
			btns += '<label class="btn btn-md btn-default' + (i==inset?' active':'') + '"><input type="radio" name="qty-' + card.code + '" value="' + i + '">' + i + '</label>';
		}
		btns += '</div>';
		
		
		outp = '<div class="modal-dialog">'
			+ '<div class="modal-content">'
			+ '<div class="modal-header">'
			+ '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>'
			+ '<h4 class="modal-title">' + card.name  + '</h4>'
			+ '<div class="row"><div class="col-sm-12">' + btns + '</div></div>'
			+ '</div>'
			+ '<div class="modal-body">'
			+ '<div class="row"><div class="col-sm-12"><img src="' + card.img + '"></div>'
			//+ '<div class="col-sm-6">' + card.Type + '</div>'
			+ '</div>'
			+ '</div>'
			+ '</div>';
		
		$('#cardmodal').html (outp);
	});
	$('#cardmodal').on('change','input[type=radio]',function () {
		updateDeck(this.name.substring(4), parseInt($(this).val(),10));
		$('#cardmodal').modal('hide');
	});


/* LOAD DECK */
	
	
/* BUILD TAB */
	$('#tablebody').on('change','input[type=radio]',function() {
		updateDeck(this.name.substring(4), parseInt($(this).val(),10));
	});
	
	$('#factionfilter').on('change','input[type=checkbox]', function() {
		var idx = $.inArray($(this).attr('name'),facFilter);
		if (idx == -1 && this.checked) {
			facFilter.push ($(this).attr('name'));
		} else {
			if (!this.checked) {
				facFilter.splice(idx,1);
			}
		}
		//TODO - EMPTY FACTIONS
		if (facFilter.length > 0) {
			filter['Faction'] = facFilter;
		} else {
			delete filter.Faction;
		}
		updateTableBody();
	});
	$('#typefilter').on('change','input[type=checkbox]', function() {
		var idx = $.inArray($(this).attr('name'),typFilter);
		if (idx == -1 && this.checked) {
			typFilter.push ($(this).attr('name'));
		} else {
			if (!this.checked) {
				typFilter.splice(idx,1);
			}
		}
		//TODO - EMPTY FACTIONS
		if (facFilter.length > 0) {
			filter['Faction'] = facFilter;
		} else {
			delete filter.Faction;
		}
		updateTableBody();
	});
	$('#filterlist').on('input', function () {
		updateTableBody()
	});

	function loadDeck() {
		var deck = $.parseJSON($('#deck-content').val());
		$.each(deck, function(id,item) {
			card = _cards({"code":item.code}).first();
			card.qty = item.qty;
			decklist.insert(card);
		})
	}
	function updateDeck(cardcode, cardqty)	{
		var card = _cards({"code":cardcode}).first();
		card["qty"] = cardqty;
		
    decklist({"code":card.code}).remove();
		if (card.qty>0) {decklist.insert(card);}
		
    //switch (card.Type) {
		//	case ("Faction"):
		//		decklist({"Type":"Faction"}).remove();
		//		if (card.qty>0) {decklist.insert(card)};
		//		break;
		//	case ("Agenda"):
		//		decklist({"Type":"Agenda"}).remove();
		//		if (card.qty>0) {decklist.insert(card)};
		//		break;
		//	default:
		//		decklist({"code":card.code}).remove();
		//		if (card.qty>0) {decklist.insert(card);}
		//}
		
		updateTableBody();
		writedeck();
		writeoutput();
	}
	
	function updateTableBody() {
		$('#tablebody').empty();
		
    //default exclude planet and token
    filter = {"type_code":["army_unit","warlord_unit","attachment","event","synapse_unit","support"]};
    
    if (facFilter != '') {filter["faction_code"] = facFilter;}
    if (typFilter != '') {filter["type_code"] = typFilter;}
    if (setFilter.length > 0) {filter["pack_code"] = setFilter}
    $('#factionfilter label').each(function(id,btn) {
      $(btn).removeClass('btn-disabled');
    });
		
		// Add text filter
		
		var f = parsefilter($('#filterlist').val());
		if ( $.isEmptyObject(f) == false) {
			$.extend(filter, f);
		}
		
		//console.log (filter);
		_cards(filter).order("type, name").each(function(r) {
			$('#tablebody').append (buildRow(r));
		});
	}
	function buildRow (r) {
	/* Row: House, Name, Cost, Strength */
		var maxallowed = maxindeck(r);
		var inset = decklist({"code":r.code}).count() != 0 ? decklist({"code":r.code}).first().qty : 0;
		
		var vns = _cards({"name":r.name}).count();
		
		var btns = '<div class="btn-group btn-group-toggle" data-toggle="buttons">';
		for (var i=0; i<=maxallowed; i++) {
			btns += '<label class="btn btn-sm btn-outline-secondary' + (i==inset?' active':'') + '">'
            + '<input type="radio" name="qty-' + r.code + '" value="' + i + '">' + i 
            + '</label>';
		}
		btns += '</div>';
		
		outp = '<tr>'
			+ '<td>' + btns
			+ '<td>'
				+ '<span class="card-tooltip" data-code="' + r.code + '"><a href="">' 
				+ (r.unique ? '&bull;&nbsp;' : '') 
				+ r.name
				+ (vns > 1 ? '&nbsp;(' + r.Set + ')' : '')
        + '</a></span>'
			+ '</td>'
			+ '<td>' + (typeof r.type !== "undefined" ? r.type : "") + '</td>'
			+ '<td>' + r.faction.slice(0,2) + '</td>'
			+ '<td>' + (typeof r.cost !== "undefined" ? r.cost : "") + '</td>'
			+ '<td>' + (typeof r.command_icons !== "undefined" ? r.command_icons : "") + (typeof r.shields !== "undefined" ? r.shields : "") + '</td>'
			+ '<td>' + (typeof r.attack !== "undefined" ? r.attack : "") + '</td>'
			+ '<td>' + (typeof r.hp !== "undefined" ? r.hp : "") + '</td>'
			+ '</tr>';
		return (outp);
	}
	function maxindeck(r) {
		var core = localStorage.getItem('whk-set-core') == null ? 1 : localStorage.getItem('whk-set-core') ;
		var maxallowed = 3;
		
		switch (r.type_code) {
			case ("warlord_unit"):
				maxallowed = 1;
				break;
			default:
        if (r.signature_loyal == "signature")  {
          maxallowed = r.quantity;
        }
				maxallowed = (r.pack_code == "core" ? Math.min(r.quantity * core, maxallowed) : maxallowed);
		}
		return maxallowed;
	}
	
	function writedeck()	{
		var outp = '';
		var faction; 
		
    var faction_code = "neutral"
    
		//// Faction and Agenda
		//if (decklist({"Type":"Faction"}).count() > 0) {
		//	faction = decklist({"Type":"Faction"}).first().name;
		//	faction_short = faction.match(/\w+$/);
		//	if (faction_short != "Watch") {faction = faction_short;}
		//}
		//outp += '<h4>Faction: '
		//	+ (decklist({"Type":"Faction"}).count() > 0 ? '<span class="card" data-code="' + decklist({"Type":"Faction"}).first().code + '">' + decklist({"Type":"Faction"}).first().name + '</span>': '')
		//	+ (decklist({"Type":"Agenda"}).count() > 0 ? ' (<span class="card" data-code="' + decklist({"Type":"Agenda"}).first().code + '">' + decklist({"Type":"Agenda"}).first().name + '</span>)': '')
		//	+ '</h4>';
		//
		//// Deck Validity 
		//outp += checkDeck();
		//
		
		
		// Cards
		deckTypes = ["Warlord Unit","Army Unit","Attachment","Event","Support","Synapse Unit"];
		outp += '<h4>Total Cards: (' + decklist({"Type":deckTypes}).sum("qty") + ')</h4>';
			
		$.each(deckTypes, function (id,cardtype) {
			outp += (id < 2 ? '<div class="col-sm-6">' : '');
			if (decklist({"type":cardtype}).count() > 0) {
				outp += '<div><b>' + cardtype + ' (' + decklist({"type":cardtype}).sum("qty") + ')</b>';
				decklist({"type":cardtype}).order("name").each(function (card) {
					outp += '<br>' + card.qty + 'x <a href="/card/' + card.code + '" class="card-tooltip" data-code="' + card.code + '" data-toggle="modal" data-remote="false" data-target="#cardmodal">' + card.name + '</a>';
					if (card.faction_code != faction_code) { outp += ' <i class="fa fa-flag ' + card.faction_code + '"></i>'}
				});
				outp += '</div>';
			}
			outp += (id == 0 || id == 3 ? '</div>' : '');
		});
				
		$('#decklist').html(outp);
		
		content = '';
		decklist().each(function(card) {
			content += '{"code":"' + card.code + '","qty":' + card.qty + '},';
		})
		
		$('#deck-content').val('[' + content.slice(0,-1) + ']');
		
		//newGame();
		//updateCharts();
	}
	function writeoutput() {
		var exp = '';
		
		exp = $('#deck-name').val() + '\n\n';
		exp += decklist({"Type":"Faction"}).first().name + '\n';
		exp += decklist({"Type":"Agenda"}).first().name + '\n';
		exp += '\nTotal Cards: (' + decklist({"Type":["Character","Attachment","Event","Location"]}).sum("qty") + ')';
		$.each(["Plot","Character","Attachment","Event","Location"],function (id,type) {
			exp += '\n\n' + type;
			decklist({"Type":type}).order("name").each(function (card) {
				exp += '\n' + card.qty + 'x ' + card.name + ' (' + card.Set + ')'
			});
		});
		$('#deckload').val(exp);
	}

	function checkDeck()	{
	// Validates the deck against Conventional and Agenda Limitations
		var pass = '<i style="color: green;" class="fa fa-check-circle-o" aria-hidden="true"></i>';
		var fail = '<i style="color: red;" class="fa fa-times-circle-o" aria-hidden="true"></i>';
		var deckTypes = ["Character","Attachment","Event","Location"];
		var checkoutp = '<b>Card Count Validations:</b> ';
		var x = 0;
		
		// Faction !?!?
		x = decklist({"Type":"Faction"}).sum("qty");
		checkoutp += (x < 1 ? 'Faction: ' + fail + ' ' : '');		
		
		// Plot Count
		x = decklist({"Type":"Plot"}).sum("qty");
		checkoutp += '7 Plots: ' + x + ' ' + (x != 7 ? fail : pass) + ' ';
		
		// Card Count
		x = decklist({"Type":deckTypes}).sum("qty");
		checkoutp += '60+ cards: ' + x + ' ' + (x < 60 ? fail : pass) + ' ';
		
		// Agenda
		// - Fealty
		var agenda = decklist({"Type":"Agenda"}).first();
		
		if (agenda) {	// Deck has Agenda
			if (agenda.name == 'Fealty') {
				x = decklist({"Type":deckTypes,"Faction":"Neutral"}).sum("qty");
				checkoutp += '15- Neutral: '  + x + ' ' +  (x > 15 ? fail : pass) + ' ';
			}
			if (agenda.name.substring(0,6) == "Banner") {
				x = decklist({"Type":deckTypes,"Faction":agenda.Faction}).sum("qty");
				checkoutp += '12+ Non-Loyal ' + agenda.Faction + ': ' + x + ' ' + (x < 12 ? fail : pass) + ' ';
				x = decklist({"Type":deckTypes,"Faction":agenda.Faction,"Loyal":true}).sum("qty");
				checkoutp += 'loyal ' + x + ' ' + (x == 0 ? pass : fail);
			}
		}
		
		return '<div class="small">' + checkoutp + '</div><br>';
	}
		
/* SETS TAB */
	// SETS: Json {Set: {code: code: number: number}}
	// <div id="setlist"></div>
	
	$('#setlist').on('click','input',function() {
		localStorage.setItem($(this).attr('name'),this.checked);
		updateSetFilter();
	});
	$('#setlist').on('change','input[type=radio]',function() {
		localStorage.setItem('whk-set-core',$(this).val());
		updateSetFilter();
	});
	
	function updateSets()	{
		var outp = '';
		var core = localStorage.getItem('whk-set-core') == null ? 1 : localStorage.getItem('set-core') ;
		
    _sets().each(function (set) {
      if (set.name == "Core Set") {
        outp += '<div><span class="mr-2">Core Sets</span>'
              + '<div class="btn-group btn-group-toggle" data-toggle="buttons">';
        for (var i=1; i<4; i++) {
          outp += '<label class="btn btn-sm btn-light' + (i==core?' active':'') + '">'
                + '<input type="radio" name="whk-set-core" value="' + i + '">' + i 
                + '</label>';
        }
        outp += '</div></div>';
      } else {
        var setowned = localStorage.getItem('whk-set-' + set.code);
        outp += '<div class="form-check">'
              + '<input class="form-check-input" type="checkbox" value="" name="whk-set-' + set.code + '"' + (setowned=="true"?' checked':'') + '>'
              + '<label class="form-check-label">' + set.name + '</label></div>';
      }
    });
    $('#setlist').html (outp);
    updateSetFilter();
	}
	
	function updateSetFilter() {
		/* build set filter */
		setFilter = [];
		_sets().each( function(set) {
			if (set.code == 'core') {
				setFilter.push ('core');
			} else {
				if (localStorage.getItem('whk-set-' + set.code) == "true") {
					setFilter.push (set.code)
				}
			}
		});
		filter['setcode'] = setFilter;
		//console.log (filter);
		updateTableBody();
	}
		
/* CHECK TAB */
	$('#loaddeck').on('click',function () {
	// Create decklist from cards
		var str = $('#deckload').val();
		var crd;
		
		decklist().remove();
		
		var res = str.match(/(.+)/g);
		// Deck Name
		$('#deck-name').val(res[0]);
		// Faction
		crd = _cards({"name": res[1]}).first();
		crd["qty"] = 1;
		decklist.insert(crd);
		// Agenda
		crd = _cards({"name": res[2]}).first();
		crd["qty"] = 1;
		decklist.insert(crd);
		
		var regex = /([0-9])x\s(.+)\s\((.+)\)/g;
		var res = str.match(regex);
				
		$.each(res, function (id, item) {
			item.match(regex);
			var qty = parseInt(RegExp.$1, 10);
			crd = _cards({"name":RegExp.$2,"Set":RegExp.$3}).first();
			crd.qty = qty;
			
			decklist.insert(crd);
		});
		console.log('Deck Loaded');
		console.log (decklist);
		
		updateTableBody();
		writedeck();
		
		
		// _deck[], deck[], plot[]
		newGame();
	});
	
	$('.btn-draw').on('click',function() {
		if ($(this).attr('val') == 0) {
			newGame();
		} else {
			var n = $(this).attr('val') == "all" ? deck.length : $(this).attr('val');
			drawCards(n);
		}
		$('#draw1').prop('disabled',deck.length == 0);
		$('#drawall').prop('disabled',deck.length == 0);
		$('#draw2').prop('disabled',deck.length < 2);
		$('#draw7').prop('disabled',deck.length < 7);
	});
	
	// Make cards 50% opaque when clicked
	$('#plot_cards').on('click','.check_card',function() {
		$(this).css('opacity', 1.5 - parseFloat($(this).css('opacity')));
		//var id = $(this).data('cardno');
		//var set = $(this).data('cardset')
		//var card = _cards({"Number":id,"Set":set}).first();
		
		var card = _cards({"code":$(this).data('code').toString()}).first();
		$('#plot_data').html ('<h4>Plot: ' + card.name + '</h4>Gold: ' + card.Gold + ' Initiative: ' + card.Initiative + ' Claim: ' + card.Claim + ' Reserve: ' + card.Reserve); // + '<br><br>' + card.CardText);
	});
	$('#hand').on('click','.check_card',function() {
		$(this).css('opacity', 1.5 - parseFloat($(this).css('opacity')));
	})
			
	$('#deckload').on("mouseenter",function () {
		$(this).qtip({
			overwrite: false,
			show: {
				ready: true
			},
			content: {
				text: 'Paste in a decklist in <a href="http://www.cardgamedb.com">CardGameDB.com</a> format.'
				//+ '<br><br>Faction should be in the format:<br><em>Faction:<br>&nbsp;&lt;faction&gt;</em>'
				//+ '<br><br>Cards should be in the format:<br><em>3x &lt;card name&gt; (&lt;Card Set&gt;)</em>'
				//+ '<br><br>Agenda listed as a standard card e.g. <em>1x Fealty (Core Set)</em>'
			},
			style: {
				classes: 'qtip-bootstrap',
				tip: false,
				width: 500
			},
			position: {
				my: 'left-center',
				at: 'right-center'
			},
			hide:	{
				//event: 'unfocus'
			}
		});
	});
	
	function newGame() {
		_deck = [];
		decklist({"Type":["Character","Event","Attachment","Location"]}).each(function(card) {
			for (i=0; i<card.qty; i++) {
				_deck.push({"code":card.code,"img":card.img,"name":card.name});
			}
		});
		_deck = shuffle(_deck);
		deck = _deck.slice();
		
		drawFactionPlots();
		$('#hand').html ('');
		drawCards(7);
	}
	
	function drawFactionPlots()	{
		var outp = '';
		
		// Faction and Agenda
		var faction = decklist({"Type":"Faction"}).count()>0 ? decklist({"Type":"Faction"}).first() : '';
		var agenda = decklist({"Type":"Agenda"}).count()>0 ? decklist({"Type":"Agenda"}).first() : '';
		
		outp += faction != '' ? '<h2 class="card faction" data-code="' + faction.code + '">' + faction.name + '</h2>'  : '';
		outp += agenda != '' ? '<h4 class="card agenda" data-code="' + agenda.code + '">' + agenda.name + '</h4>' : '';
				
		$('#faction_name').html (outp);
		
		outp='';
		// Plots
		decklist({"Type":"Plot"}).each(function (plot) {
			for (var i=0; i<plot.qty; i++) {
				outp += '<img src="' + plot.img + '" class="check_card plot_card" data-code="' + plot.code + '"></img>';
			}
		});
		$('#plot_cards').html (outp);
		$('#plot_data').html("Gold: 8");
	}
	
	function drawCards(n) {
		var card;
		n = Math.min(n, deck.length);
		for (var i = 0; i < n; i++) {
			card = deck.shift();
			$('#hand').append('<img src="' + card.img + '" class="check_card deck_card" data-code="' + card.code + '"></img>');
			updateChooseList();
		}
	}
	
	function updateChooseList()	{
		var outp='';
		outp += '<div class="btn-group"><button type="button" class="btn btn-default btn-sm dropdown-toggle" data-toggle="dropdown">Choose<span class="caret"></span></button>';
		outp += '<ul class="dropdown-menu scrollable-menu" role="menu">';
		var x = 0;
		$.each(deck,function (id,card) {
			outp += '<li role="presentation"><a role="menuitem" class="card" data-code="' + card.code + '" data-index="' + x + '">#' + (x+1) + ' ' + card.name + '</a></li>';
			if (x++ == 9) {
				outp +='<li role="presentation" class="divider"></li>';
			}
		});
		outp += '</ul>';
		outp += '</div>';
		$('#choosecardlist').html (outp);
	}
	
	$('#choosecardlist').on('click','a',function () {
		var card = deck.splice($(this).data('index'),1)[0];
		$('#hand').append('<img src="' + card.img + '" class="check_card deck_card" data-code="' + card.code + '"></img>');
		updateChooseList();
	});
	
/* DELETE Confirm */
	$('#delete_deck').on('click',function () {
		if (confirm("Are you sure you want to delete " + $('#deckname') + '?')) {
			alert ("Dodelerte");
		}
	});
	
/* STATS TAB */
	// Code to compile data and call Charts scripts
	function updateCharts()	{
		barIcons();
		lineCost();
		lineStrength();
	}
	
	function barIcons()	{
		var icons = ["Military","Intrigue","Power"];
		var counts = [0,0,0];
		
		decklist({"Type":"Character"}).each(function (char) {
			$.each(icons, function(idx,icon)	{
				counts[idx] += ($.inArray(icon,char.Icons) != -1 ? char.qty : 0);
			});
		});
		
		var ctx = $('#barIcons').get(0).getContext("2d");
		var bi = new Chart(ctx, {
			type:	"bar",
			data:	{
				labels:	icons,
				datasets: [{
					data:		counts,
					backgroundColor: [
						'rgb(204, 0, 0)',
						'rgb(0, 153, 0)',
						'rgb(0, 0, 153)'
					]					
				}]
			},
			options:	{
			  title: {
					display: true,
					text: 'Icons',
					fontSize: 24
				},
				legend:	{
					display: false
				},
				responsive: true,
				maintainAspectRatio: true,
				scales:	{
					yAxes: [{
						ticks: {
							min:	0,
							stepSize: 1
						}
					}]
				}
			}
		});
	}
	function lineCost()	{
		var maxVal = decklist({"Cost":{"!is":"X"}}).max("Cost");
		var datavals = [];
		var datacounts = [];
		for (var i=0; i<= maxVal; i++)	{
			datavals.push(String(i));
			datacounts.push(decklist({"Type":"Character","Cost":String(i)}).sum("qty"));
		}
		
		var ctx = $('#lineCost').get(0).getContext("2d");
		var ls = new Chart(ctx, {
			type: 'line',
			data: {
				labels: datavals,
				datasets:	[{
					label: "Cost",
					fill: false,
					data: datacounts,
					tension: 0
				}]
			},
			options: {
			  title: {
					display: true,
					text: 'Cost',
					fontSize: 24
				},
				legend:	{
					display: false
				},
				responsive: true,
				maintainAspectRatio: true,
				scales:	{
					yAxes: [{
						ticks: {
							min:	0,
							stepSize: 1
						},
						scaleLabel:	{
							display: true,
							labelString: "Number of cards"
						}
					}],
					xAxes: [{
						scaleLabel: {
							display: true,
							labelString: "Cost (Cost X excluded)"
						}
					}]
				}
			}
		});
	}
	function lineStrength()	{
		var _deck = decklist({"Type":["Character","Event","Attachment","Location"]});
		var maxVal = decklist({"Type":"Character","Strength":{"!is":"X"}}).max("Strength");
		var datavals = [];
		var datacounts = [];
		for (var i=0; i<= maxVal; i++)	{
			datavals.push(String(i));
			datacounts.push(decklist({"Type":"Character","Strength":String(i)}).count());
		}
		
		var ctx = $('#lineStr').get(0).getContext("2d");
		var ls = new Chart(ctx, {
			type: 'line',
			data: {
				labels: datavals,
				datasets:	[{
					label: "Strength",
					fill: false,
					data: datacounts,
					tension: 0
				}]
			},
			options: {
				responsive: true,
				maintainAspectRatio: true,
				scales:	{
					yAxes: [{
						ticks: {
							min:	0,
							stepSize: 1
						}
					}]
				}
			}
		});
	}
	
	function showSnack(message) {
		$('#snackbar').html (message);
		$('#snackbar').fadeIn(1000).delay(5000).fadeOut(1000);
	}
	
});
