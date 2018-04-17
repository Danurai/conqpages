/* qtip */

$(document).ready(function () {
	
	$("body").on("mouseenter",".card-tooltip", function() {
		var card = _cards({"code":$(this).data('code').toString()}).first();
					
		$(this).qtip({
			overwrite: false,
			show: {
				ready: true
			},
			content: {
				text:  '<div class="cardthumb cardthumb-' + card.type_code + '" style="background-image:url(' + card.img + ')"></div>'
					+ '<h4 class="card-title">' 
					+ (card.unique ? '&bull;&nbsp;' : '')
					+ card.name + '</h4>'
					+ '<br>'
					+ (typeof card.cost !== "undefined" ? 'Cost: ' + card.cost + ' ' : '' ) 
					+ (typeof card.command_icons !== "undefined" ? 'Cmd: ' + card.command_icons + ' ' : '' ) 
					+ (typeof card.shields !== "undefined" ? 'Shields: ' + card.shields + ' ' : '' ) 
					+ (typeof card.attack !== "undefined" ? 'Attk: ' + card.attack + ' ' : '') 	
					+ (typeof card.hp !== "undefined" ? 'Def: ' + card.hp + ' ' : '') 	
					+ '<div class="' + card.faction_code + '-card">' + card.text + '</div>'
          + '<div class="d-flex justify-content-around">'
					+ '<div>' 
          + (typeof card.faction !== "undefined" ? card.faction + (card.signature_loyal == "Loyal" ? ' (Loyal)' : '') : 'Neutral')
          + '</div>'
					+ '<div style="text-align: right;">' + card.pack + ' #' + parseInt(card.position,10) + '</div>'
			},
			style: {
				classes: 'qtip-bootstrap',
				tip: false
			},
			position: {
				my: 'left center',
				at: 'right center'
			},
			hide:	{
				//event: 'unfocus'
			}
		});
	});
	
/* CHECK Tab - Draw simulator */

	$("body").on("mouseenter",".check_card", function() {
		var card = _cards({"code":$(this).data('code').toString()}).first();
		
		var faction_s = typeof card.Faction !== "undefined" ? card.Faction.match(/\w+/) : "Neutral";
			
		$(this).qtip({
			overwrite: false,
			show: {
				ready: true
			},
			content: {
				text:  '<b>' 
					+ (card.Unique ? '&diams;&nbsp;' : '')
					+ card.name + '</b>'
					+ '<br>'
					+ (card.Type == "Plot" ? 'Gold: ' + card.Gold + ' Initiative: ' + card.Initiative + ' Claim: ' + card.Claim + ' Reserve: ' + card.Reserve : '')
					+ (typeof card.Cost !== "undefined" ? 'Cost: ' + card.Cost + ' ' : '' )
					+ (typeof card.Strength !== "undefined" ? 'Str: ' + card.Strength + ' ' : '')
//					+ (icons != '' ? icons : '')
					+ '<p class="' + faction_s + '-card">' + card.CardText
					+ (typeof card.Faction !== "undefined" ? '<div class="small" style="float: left;">' + card.Faction + (card.Loyal == true ? ' (Loyal)' : '') + '</div>' : 'Neutral')
					+ '<div class="small" style="text-align: right;">' + card.Set + ' #' + card.Number + '</div>'
			},
			style: {
				classes: 'qtip-bootstrap',
				tip: false
			},
			position: {
				my: 'top center',
				at: 'bottom center'
			},
			hide:	{
				//event: 'unfocus'
			}
		});
	});
	
	$('#filterlist').on('mouseenter', function () {
		$(this).qtip({
			overwrite: false,
			show: {
				ready: true
			},
			content: {
				text:  'Search tags:'
					+ '<br>e:&nbsp;&nbsp;Set Code'
					+ '<br>f:&nbsp;&nbsp;Faction Code'
					+ '<br>t:&nbsp;&nbsp;Type'
					+ '<br>x:&nbsp;&nbsp;Description'
					+ '<br>c:&nbsp;&nbsp;Claim'
			},
			style: {
				classes: 'qtip-bootstrap',
				tip: false
			},
			position: {
				my: 'right top',
				at: 'right top'
			},
			hide:	{
				//event: 'unfocus'
			}
		});
	});
	
});