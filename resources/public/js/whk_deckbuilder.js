/* Deckbuilder Script */

var _cards;

$(document).ready(function () {
	
	var filter = {};
	var typFilter = [];
	var facFilter = [];
	var setFilter = [];
  
  var LS_pre = 'whk-pack';    // Local Storage Prefix
  
  var _planets;
  var _sets;
  var _cycles;
  var _factions;
	
	var decklist = TAFFY();
	
/* INITIALISATION */
  $.getJSON("js/data/wh40k_cards.json", function (data) {   //,{_: new Date().getTime()}
    _cards = TAFFY(data.data);
    _planets = JSON.parse(_cards({"type_code":"planet"}).stringify());
    
    $.getJSON("js/data/wh40k_factions.json", function (data) {
      _factions = TAFFY(data.data);
      
      $.getJSON("js/data/wh40k_cycles.json", function (data) {
        _cycles = TAFFY(data.data);
        
        $.getJSON("js/data/wh40k_packs.json", function (data) {
          _sets = TAFFY(data.data);
          
          $('#deck-content').val('[{"code":"010001","qty":1},{"code":"010009","qty":1},{"code":"010008","qty":4},{"code":"010011","qty":1},{"code":"010010","qty":2},{"code":"010015","qty":2},{"code":"010016","qty":1},{"code":"010023","qty":1},{"code":"010020","qty":1},{"code":"010014","qty":1},{"code":"010021","qty":1},{"code":"010017","qty":1},{"code":"010022","qty":1},{"code":"010013","qty":2},{"code":"010018","qty":1},{"code":"010019","qty":1},{"code":"010012","qty":1},{"code":"010027","qty":1},{"code":"010028","qty":1},{"code":"010024","qty":1},{"code":"010026","qty":1},{"code":"010025","qty":1},{"code":"010029","qty":1},{"code":"010030","qty":1},{"code":"010174","qty":1},{"code":"010172","qty":1},{"code":"010169","qty":1},{"code":"010173","qty":1},{"code":"010040","qty":1},{"code":"010046","qty":1},{"code":"010045","qty":2},{"code":"010039","qty":1},{"code":"010044","qty":1},{"code":"010035","qty":2},{"code":"010037","qty":1},{"code":"010041","qty":1},{"code":"010049","qty":1},{"code":"010170","qty":1},{"code":"010048","qty":1},{"code":"010053","qty":1},{"code":"010051","qty":1},{"code":"010171","qty":1}]');
          updateSets();      
          loadDeck();
          writedeck();
          writeoutput();
          
          updateTableBody();
        });
      });
    });
  });

/* DELETE Confirm */
	$('#delete_deck').on('click',function () {
		if (confirm("Are you sure you want to delete " + $('#deckname') + '?')) {
			alert ("Dodelerte");
		}
	});
	
/* Decklist Listeners*/
	$('#decklist').on('click','.card-tooltip',function () {
		var outp = '';
		var card = _cards({"code":String($(this).data("code"))}).first();
		
		var maxallowed = maxindeck(card);
		var inset = decklist({"code":card.code}).count() != 0 ? decklist({"code":card.code}).first().qty : 0;
		
    var btns = '<div class="btn-group btn-group-toggle mx-2" data-toggle="buttons">';
    if (card.signature_loyal == "Signature")  {
      btns += '<label class="btn btn-md btn-info">'
            + '<input type="radio">' + inset + '</label>';
    } else {
      for (var i=0; i<=maxallowed; i++) {
        btns += '<label class="btn btn-md btn-outline-secondary' + (i==inset?' active':'') + '">'
        + '<input type="radio" name="qty-' + card.code + '" value="' + i + '">' + i + '</label>';
      }
    }
		btns += '</div>';
		
		
		outp = '<div class="modal-dialog">'
            + '<div class="modal-content">'
              + '<div class="modal-header justify-content-between">'
                + '<h4 class="modal-title">' + card.name  + '</h4>'
                + btns
                + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span>x</span>'
              + '</div>'
              + '<div class="modal-body">'
              + '<div class="row"><div class="col"><img src="' + card.img + '"></div>'
              + '</div>'
            + '</div>'
          + '</div>';
		
		$('#cardmodal').html (outp);
	});
	$('#cardmodal').on('change','input[type=radio]',function () {
		updateDeck(this.name.substring(4), parseInt($(this).val(),10));
		$('#cardmodal').modal('hide');
	});
  $('#newdeck').on('click',function() {
    if (confirm("Are you sure you want to create a new deck?")) {
      decklist().remove();
      writedeck();
      writeoutput();
      updateTableBody();
    }
  });

/* BUILD TAB */

	$('#tablebody').on('change','input[type=radio]:enabled',function() {
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

  // Deck Format
  
	function loadDeck() {
		var deck = $.parseJSON($('#deck-content').val());
		$.each(deck, function(id,item) {
			card = _cards({"code":item.code}).first();
			card.qty = item.qty;
      decklist.insert(card);
			//updateDeck(item.code, item.qty);
		})
	}
	function updateDeck(cardcode, cardqty)	{
		var card = _cards({"code":cardcode}).first();
		card["qty"] = cardqty;
		
		
    switch (card.type_code) {
			case ("warlord_unit"):
				decklist({"type_code":"warlord_unit"}).remove();
				decklist({"signature_loyal":"Signature"}).remove();
				if (card.qty>0) {
            // Add Signature Cards
            _cards({"signature_squad":card.signature_squad}).each (function (sig) {
              sig.qty = sig.quantity;
              decklist.insert(sig);
            });
          };
				break;
			default:
				decklist({"code":card.code}).remove();
				if (card.qty>0) {decklist.insert(card);}
		}
		
    //console.log (decklist().stringify());
    
		updateTableBody();
		writedeck();
		writeoutput();
	}
	
	function updateTableBody() {
    
    var faction_code = decklist({"type_code":"warlord_unit"}).first().faction_code;
    if (typeof faction_code == 'undefined') { faction_code = ""; }
    // Filter Buttons
    var ally_codes = getAllyCodes(faction_code);
    
    $('#factionfilter label').each(function (id,lbl) {
      $(lbl).removeClass('disabled');
    });
    
    if (ally_codes.length > 0)  {
      ally_codes.push("neutral");
      $('#factionfilter label').each(function (id,lbl) {
        if ($.inArray($(lbl).find('input')[0].name, ally_codes) == -1) {
          $(lbl).addClass('disabled');
          $($(lbl).find('input')[0]).prop('checked', false);
        } 
      });
    } 
		
    
    //default exclude planet and token
    filter = {"type_code":["army_unit","warlord_unit","attachment","event","synapse_unit","support"]};
    
    if (facFilter != '') {filter["faction_code"] = facFilter;} else {
      if (ally_codes.length > 0) {filter["faction_code"] = ally_codes;}
    }
    if (typFilter != '') {filter["type_code"] = typFilter;}
    if (setFilter.length > 0) {filter["pack_code"] = setFilter}
		
    // Add text filter		
		var f = parsefilter($('#filterlist').val());   	
		if ( $.isEmptyObject(f) == false) {
			$.extend(filter, f);
		}
		
		$('#tablebody').empty();
		
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
    if (r.signature_loyal == "Signature") {
       btns += '<label class="btn btn-sm btn-info" disabled>'
              + '<input type="radio" name="qty-' + r.code + '" value="' + i + '" disabled>' + inset 
              + '</label>';
    } else {
      for (var i=0; i<=maxallowed; i++) {
        btns += '<label class="btn btn-sm btn-outline-secondary' + (i==inset?' active':'') + '">'
              + '<input type="radio" name="qty-' + r.code + '" value="' + i + '">' + i 
              + '</label>';
      }
    }
		btns += '</div>';
		
		outp = '<tr>'
			+ '<td>' + btns
			+ '<td>'
				+ '<span class="card-tooltip" data-code="' + r.code + '"><a href="">' 
				+ (r.unique ? '&bull;&nbsp;' : '') 
				+ r.name
				+ (vns > 1 ? '&nbsp;(' + r.Set + ')' : '')
        + '</a>'
        + (r.signature_loyal == "Loyal" ? '<i class="fas fa-crosshairs icon-loyal ml-2" title="Loyal"></i>' : '')
        + '</span>'
			+ '</td>'
			+ '<td>' + (typeof r.type !== "undefined" ? r.type : "") + '</td>'
			+ '<td class="text-center">' + r.faction.slice(0,2) + '</td>'
			+ '<td class="text-center">' + (typeof r.cost !== "undefined" ? r.cost : "") + '</td>'
			+ '<td class="text-center">' + (typeof r.command_icons !== "undefined" ? r.command_icons : "") + '</td>'
      + '<td class="text-center">' + (typeof r.shields !== "undefined" ? r.shields : "") + '</td>'
			+ '<td class="text-center">' + (typeof r.attack !== "undefined" ? r.attack : "") + '</td>'
			+ '<td class="text-center">' + (typeof r.hp !== "undefined" ? r.hp : "") + '</td>'
			+ '</tr>';
		return (outp);
	}
	function maxindeck(r) {
		var core = localStorage.getItem( LS_pre + '-core-count') == null ? 1 : localStorage.getItem( LS_pre + '-core-count') ;
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
	function getAllyCodes(faction_code) {
    var ally_codes = _factions({"code":faction_code}).first().ally_codes;
    if (typeof ally_codes == 'undefined') {ally_codes = [];}
    if (faction_code != "") {ally_codes.push(faction_code)};    
    return ally_codes;
  }
	function writedeck()	{
		var outp = '';
		var faction; 
    var faction_code;
    
    var identity = decklist({"type_code":"warlord_unit"}).first();
    faction_code = typeof identity == 'undefined' ? 'neutral' : identity.faction_code;
    
    var deckTypes = ["Army Unit","Synapse Unit","Attachment","Event","Support"];
    
    if (decklist().count() == 0) {
      outp = '<div class="row h5">Empty Deck</div>';
    } else {
      // Header
      outp += '<div class="row">'
            + '<div class="col-3">'
              + '<img class="rounded img-fluid" src="' + identity.img + '"></img>'
            + '</div>'
            + '<div class="col">'
              + '<div><a href="/card/' + identity.code + '" class="card-tooltip h4" data-code="' + identity.code + '">'
              + identity.name + '</a>'
              + '<div class="h5 text-muted">' 
              + /^<b>(.+)<\/b><br>/.exec(identity.text)[1]
              + '</div>'
              + '<div class="h5">Total Cards: (' + decklist({"type":deckTypes}).sum("qty") + ')</div>'
            // Check Validity
              + '<div>' + checkValidity() + '</div>'
            + '</div></div>';
      outp += '<div class="row"><div class="col-sm">';	

      // Content
      $.each(deckTypes, function (id,cardtype) {
        outp += (id == 2 ? '</div><div class="col-sm">' : '');
        if (decklist({"type":cardtype}).count() > 0) {
          outp += '<div class="my-2"><b>' + cardtype + ' (' + decklist({"type":cardtype}).sum("qty") + ')</b>';
          decklist({"type":cardtype}).order("name").each(function (card) {
            outp += '<br>' + card.qty + 'x <a href="/card/' + card.code + '" class="card-tooltip" data-code="' + card.code + '" data-toggle="modal" data-remote="false" data-target="#cardmodal">' + card.name + '</a>';
            if (card.faction_code != faction_code) { outp += ' <i class="fa fa-flag ' + card.faction_code + '"></i>'; }
            if (card.signature_loyal == "Signature") { outp += ' <i class="fa fa-cog icon-sig"></i>'; }
            //if (card.signature_loyal == "Loyal") { outp += ' <i class="fa fa-crosshairs icon-loyal"></i>'; }
          });
          outp += '</div>';
        }
      });
      outp += '</div></div>';
		}		
		$('#decklist').html(outp);
		
		//updateCharts();
    
		// Game Prep
    var content = '';
		decklist().each(function(card) {
			content += '{"code":"' + card.code + '","qty":' + card.qty + '},';
		})
		
		$('#deck-content').val('[' + content.slice(0,-1) + ']');
		newGame();
	}
	function writeoutput() {
		var exp = '';
    var n = 0;
		
		exp = $('#deck-name').val() + '\n\n';
		exp += '\nTotal Cards: (' + decklist({"type_code":{"!is":"warlord_unit"}}).sum("qty") + ')';
		$.each(["Warlord Unit","Army Unit","Synapse Unit","Attachment","Event","Support",],function (id,type) {
			n = decklist({"type":type}).sum("qty");
      if (n > 0) {
        exp += '\n\n' + type + ': (' + n + ')';
        decklist({"type":type}).order("name").each(function (card) {
          exp += '\n' + card.qty + 'x ' + card.name + ' (' + card.pack + ')'
        });
      }
		});
		$('#deckload').val(exp);
	}
  function checkValidity()  {
    // Returns html formatted string
    var validresult = [];
    //1 deck size 
    var numcards = decklist({"type_code":{"!is":"warlord_unit"}}).sum("qty");
    if (numcards < 50)  {
      validresult.push ('Minimum 50 cards:&nbsp;<span class="text-danger">' + numcards + '</span>/50 ');
    }
    
    //2 cards from multiple factions
    var factions = decklist({"faction_code":{"!is":"neutral"}}).distinct("faction");
    if (factions.length > 2) {
      validresult.push ('Only inlcude cards from one allied faction: <span class="text-danger">' + factions.join(', ') + '</span>');
    }
    
    //3 loyal cards from other factions
    var faction_code = decklist({"type_code":"warlord_unit"}).first().faction_code;
    if (typeof faction_code !== 'undefined')  {
      var sigloyal = [];
      decklist({"faction_code":{"!is":faction_code},"signature_loyal":["Signature","Loyal"]}).each(function (r) {
          sigloyal.push ('<a href="/card/' + r.code + '" data-code="' + r.code + '" class="card-tooltip" data-toggle="modal" data-target="#cardmodal">' + r.name + '</a>');
      });
      if (sigloyal.length > 0) {
        validresult.push ('Cannot include Loyal cards from other factions: <span class="text-danger">' + sigloyal.join(', ') + '</span>');
      }
    }
    return (validresult.length > 0 ? validresult.join('<p>') : "Deck is Valid");
  }
  
  
/* SETS TAB */
	// SETS: Json {Set: {code: code: number: number}}
	// <div id="setlist"></div>
	
	$('#setlist')
    .on('change','input[type=radio]',function() {
      localStorage.setItem($(this).attr('name'),$(this).val());
      updateSetFilter();
    })
    .on('click','input.pack',function () {
      localStorage.setItem("whk-pack-" + $(this).data('code'),$(this).prop('checked').toString() );
      updateSetFilter();
    })
  // Parent/child checkboxes
    .on("click","div.form-check.pa",function()	{
      var packcode;
      var checkchild = $(this).find("input[type='checkbox']").prop("checked");
      var cyclecode = $(this).find("input[type='checkbox']").data("cycle");
      $(this).parent().find("[data-cycle='" + cyclecode + "']input[type='checkbox'].pack").each(function (id,chkbox)	{
        $(chkbox).prop("checked",checkchild);
        packcode = $(chkbox).data('code');
        localStorage.setItem("whk-pack-" + packcode,checkchild);
      });
      updateSetFilter();
    })
    .on("click","div.form-check.ch",function()	{
      var checkpar = $(this).parent().find("input[type='checkbox']:checked").length == $(this).parent().find("input[type='checkbox']").length;
      var cyclecode = $(this).find("input[type='checkbox']").data("cycle");
      $(this).parent().parent().find("div.form-check.pa [data-cycle='" + cyclecode + "']input[type='checkbox']").prop("checked",checkpar);
      updateSetFilter();
    });
    
	
  function updateSets() {
    var outp = '';
		var core = localStorage.getItem(LS_pre + '-core-count') == null ? 1 : localStorage.getItem(LS_pre + '-core-count');
    
    _cycles().order("position").each( function (cycle) {
      indent = false;
      cyclepacks = _sets({"cycle_code":cycle.code}).select("code");
      _sets({"cycle_code":cycle.code}).order("position").each( function (pack,idx) {
        if (pack.name == "Core Set") {
          outp += '<div class="my-2"><span class="mr-2">Core Sets</span>'
                + '<div class="btn-group btn-group-toggle" data-toggle="buttons">';
          for (var i=1; i<4; i++) {
            outp += '<label class="btn btn-sm btn-light' + (i==core?' active':'') + '">'
                  + '<input type="radio" name="whk-pack-core-count" value="' + i + '">' + i 
                  + '</label>';
          }
          outp += '</div></div>';
        } else {
                      
          if (cycle.name != pack.name && idx == 0) {
            indent = true;
            outp += '<div class="form-check pa">'
              + '<input class="form-check-input cycle" type="checkbox" data-cycle="' + pack.cycle_code + '" checked="checked">'
              + '<label class="form-check-label"><b>'
              + cycle.name
              + '</b></label></div>';
              outp += '<ul>';
          }
          outp += '<div class="form-check ch">'
            + '<input class="form-check-input pack" type="checkbox" data-code="' + pack.code + '" data-cycle="' + pack.cycle_code + '" checked="checked">'
            + '<label class="form-check-label">'
            + (cycle.name == pack.name ? '<b>' : '') 
            + pack.name 
            + (cycle.name == pack.name ? '</b>' : '')
            + ' (' + _cards({"pack_code":pack.code}).sum("quantity") + ')'
            + '</label></div>';
        }
      });
      if (indent == true)	{
        outp += '</ul>';
      }
    });
    $('#setlist').html (outp);
    
    // Set PACKS Checkboxes
    if (typeof(Storage) !== "undefined")	{
      _sets({"code":{"!in":"core"}}).order("position").each( function (pack,idx) {
        // get localstorage value
        checked = localStorage.getItem("whk-pack-" + pack.code) == null ? true : localStorage.getItem("whk-pack-" + pack.code) == "true";
        // un-check child and parent
        if (checked == false)	{
          $('#setlist').find('input[data-code="' + pack.code + '"]').prop("checked",checked);
          $('#setlist').find('input.cycle[data-cycle="' + pack.cycle_code + '"]').prop("checked",checked);
        }
      });
    }
		
    updateSetFilter();				
  }
  
	function updateSetFilter() {
		/* build set filter */
		setFilter = [];
		_sets().each( function(set) {
			if (set.code == 'core') {
				setFilter.push ('core');
			} else {
				if (localStorage.getItem(LS_pre + '-' + set.code) == "true") {
					setFilter.push (set.code)
				}
			}
		});
		filter['setcode'] = setFilter;
		//console.log (filter);
		updateTableBody();
	}
		
/* CHECK TAB */

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
	$('#hand').on('click','.check_card',function() {
		$(this).css('opacity', 1.5 - parseFloat($(this).css('opacity')));
	})
	
	function newGame() {
		_deck = [];
    var warlord = decklist({"type_code":"warlord_unit"}).first()
    var starting_hand = (typeof warlord === 'undefined' ? 0 : warlord.starting_hand);
    
		decklist({"type_code":{"!is":"warlord_unit"}}).each(function(card) {
			for (i=0; i<card.qty; i++) {
				_deck.push({"code":card.code,"img":card.img,"name":card.name});
			}
		});
		_deck = shuffle(_deck);
		deck = _deck.slice();
		
		$('#hand').html ('');
		drawCards(starting_hand);
    drawPlanets();
	}
	
  function drawPlanets()  {
    var outp = '';
    
    _cards({"type_code":"planet"}).each( function (r) {
      outp += '<img src = "' + r.img + '" class="deck_card_planet" data-code="' + r.code + '"></img>';
    });
    
    $('#planets').html (outp);
  }
  
	function drawCards(n) {
		var card;
		n = Math.min(n, deck.length);
		for (var i = 0; i < n; i++) {
			card = deck.shift();
			$('#hand').append('<img src="' + card.img + '" class="check_card deck_card" data-code="' + card.code + '"></img>');
		}
	}

	
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
	

/* LOAD TAB */

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
	$('#loaddeck').on('click',function () {
	// Create decklist from cards
		var str = $('#deckload').val();
		var crd;
		
		decklist().remove();
		
		var res = str.match(/(.+)/g);
		// Deck Name
		$('#deck-name').val(res[0]);
		
		var regex = /([0-9])x\s(.+)\s\((.+)\)/g;
		var res = str.match(regex);
				
		$.each(res, function (id, item) {
			item.match(regex);
			var qty = parseInt(RegExp.$1, 10);
			crd = _cards({"name":RegExp.$2,"pack":RegExp.$3}).first();
			crd.qty = qty;
			
			//decklist.insert(crd);
      updateDeck(crd.code,qty);
		});
		console.log('Deck Loaded');
		console.log (decklist);
		
		updateTableBody();
		writedeck();
		
		newGame();
	});
			
	
	
});
