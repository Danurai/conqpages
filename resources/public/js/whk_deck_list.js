var _cards;

$.getJSON("/api/data/cards", function (data) { 
  _cards = TAFFY(data.data);   //,{_: new Date().getTime()} to force update
});

$(document).ready(function () {
  
  $('#roster')
    .on('click','.btn-delete',function () {
      $('#deletealert').html ("Are you sure you want to delete the deck " + $(this).data('deckname') + "?");
      $('#deletedeckuid').val ($(this).data('deckuid'));
      $('#deletedeck').modal('show');
    })
    
    .on('click','.btn-export',function() {
      $.getJSON("/api/deck/" + $(this).data("deckid"), function (data) {
        $('#exportdeck').find('textarea').val(write_export (data));
      });
    });
  /*
  $('#loaddeck').on('show.bs.modal',function () {
    $('#importdecklist').val("");
    $('#parseddecklist').html('<div>&nbsp;</div>');
  });
  */
  
  $('#importdecklist').on('input',function() {
    var decklist = parseDeckList($(this).val());
    $('#parseddecklist').html(write_export(decklist));
    $('#deckjson').val(JSON.stringify(decklist));
  });
  
  
  function parseDeckList(data)	{
		var crd;
		var deck = {};
		
	// Deck name and ID
		var res = data.match(/(.+)/g);
		deck.name = (res ? res[0] : "");
		deck.tags = "";
		deck.notes = "";
		deck.data = {};
			
		var regex = /([1-3])x\s((.+)\s\((.+)\)|(.+))/g;				// Don't look for 4x because they'll be picked up as signature cards
		var res = data.match(regex);
		
		$.each(res, function (id, item) {
			item.match(regex);
			
			var qty = parseInt(RegExp.$1, 10);
			var cname = (RegExp.$4 == "" ? RegExp.$2 : RegExp.$3);
      //var pack = RegExp.$3;
			
      crd = _cards({"name":cname}).first();
      if (crd)  {
      // Funky Warlord Stuff
        if (crd.type_code == "warlord_unit") {
        // Erase old Warlord - there can be only one
          $.each(deck.data, function(k, v) {
            if (crd.type_code == "warlord_unit" || crd.signature_loyal == "Signature")  {
              delete deck.data[k];
            }
          });
        // Add Signature cards
          _cards({"signature_squad":crd.signature_squad}).each(function(crd) {
            deck.data[crd.code] = crd.quantity;
          });
        } else if (crd.signature_loyal != "Signature") {
          deck.data[crd.code] = qty;
        }
      }
        
    });
		return deck;
	}
	
  function write_export (data) {
    var deck = [];
    var decklist;
    var outp;
    $.each(data.data, function (k,v) {
       deck.push ( $.extend (_cards({"code":k}).first(), {"qty":v}) );
    });
    decklist = TAFFY(deck);
    outp = data.name 
        + "\n\n\n"
        + "Total Cards " + (decklist({"type_code":{"!=":"warlord_unit"}}).sum("qty"))
        + "\n\n";
    if (decklist({"type_code":"warlord_unit"}).first()) {
      outp += "Warlord:\n"
        + decklist({"type_code":"warlord_unit"}).first().name + " (" + decklist({"type_code":"warlord_unit"}).first().pack + ")"
        + "\n";
    }
    $.each(decklist({"type_code":{"!=":"warlord_unit"}}).distinct("type_code"),function (id, tc) {
      outp += "\n" + decklist({"type_code":tc}).first().type + ": (" + decklist({"type_code":tc}).sum("qty") + ")\n";
      decklist({"type_code":tc}).each(function (crd) {
        outp += crd.qty + "x " + crd.name + " (" + crd.pack + ")\n";
      });
    });
    return outp; 
  }

  function decklistJSON(decklist) {
    var data = {};
    $.each(decklist.data, function (id, crd)  {
      data[crd.code] = crd.qty;
    });
  }
/*
Deck Created with CardGameDB.com  Warhammer 40,000: Conquest Deckbuilder


Total Cards: (17)

Warlord: 
1x Baharroth (Descendants of Isha) 


Army Unit: (10)
4x Baharroth’s Hawks (Descendants of Isha)
3x Rogue Trader (Core Set)
3x Biel-Tan Guardians (Core Set)

Attachment: (1)
1x The Shining Blade (Descendants of Isha)

Event: (5)
2x Cry of the Wind (Descendants of Isha)
3x Doom (Core Set)

Support: (1)
1x Banner of the Ashen Sky (Descendants of Isha)
*/
})