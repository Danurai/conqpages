/* qtip */

$(document).ready(function () {
  
	function isTouchDevice(){
    return true == ("ontouchstart" in window || window.DocumentTouch && document instanceof DocumentTouch);
  }	
  
	$("body").on("mouseenter",".card-tooltip", function() {
		var card = _cards({"code":$(this).data('code').toString()}).first();
		
    if (isTouchDevice()===false) {
      $(this).qtip({
        overwrite: false,
        show: {
          ready: true
        },
        content: {
          text:  '<div class="cardthumb cardthumb-' + card.type_code + '" style="background-image:url(' + card.img + ')"></div>'
            + '<h4 class="card-title">' 
            + (card.unique ? '&diams;&nbsp;' : '')
            + card.name + '</h4>'
            + '<br>'
            + '<div class="my-2">'
            + (typeof card.cost !== "undefined" ? '<i class="fas fa-cog"></i>: ' + card.cost + ' ' : '' ) 
            + (typeof card.command_icons !== "undefined" ? '<i class="fas fa-gavel"></i>: ' + card.command_icons + ' ' : '' ) 
            + (typeof card.shields !== "undefined" ? '<i class="fas fa-shield-alt"></i>: ' + card.shields + ' ' : '' ) 
            + (typeof card.attack !== "undefined" ? '<i class="fas fa-bolt"></i>: ' + card.attack + ' ' : '') 	
            + (typeof card.hp !== "undefined" ? '<i class="fas fa-heartbeat"></i>: ' + card.hp + ' ' : '') 	
            + '</div>'
            + '<div class="' + card.faction_code + '-card">' + card.text + '</div>'
            + '<div class="d-flex justify-content-around mt-3">'
            + '<div>' 
            + (typeof card.faction !== "undefined" ? card.faction + (card.signature_loyal == "Loyal" ? ' (Loyal)' : '') : 'Neutral')
            + '</div>'
            + '<div style="text-align: right;">' + card.pack + ' #' + parseInt(card.position,10) + '</div>'
        },
        style: {
          classes: 'qtip-bootstrap',
          tip: false,
          width:  350
        },
        position: {
          my: 'left center',
          at: 'right center'
        },
        hide:	{
          // event: 'unfocus'
        }
      });
    }
	});
	
/* CHECK Tab - Draw simulator */

	$("body").on("mouseenter",".check_card", function() {
		// Mini-qtip
    var card = _cards({"code":$(this).data('code').toString()}).first();

    if (isTouchDevice()===false) {
      $(this).qtip({
        overwrite: false,
        show: {
          ready: true
        },
        content: {
          text:  '<div class="h5">' 
            + (card.Unique ? '&diams;&nbsp;' : '') + card.name + '</div>'
            + '<div class="mt-2">' + card.type + '</div>'
            + '<div class="my-2">'
              + (typeof card.cost !== "undefined" ? '<i class="fas fa-cog"></i>: ' + card.cost + ' ' : '' ) 
              + (typeof card.command_icons !== "undefined" ? '<i class="fas fa-gavel"></i>: ' + card.command_icons + ' ' : '' ) 
              + (typeof card.shields !== "undefined" ? '<i class="fas fa-shield-alt"></i>: ' + card.shields + ' ' : '' ) 
              + (typeof card.attack !== "undefined" ? '<i class="fas fa-bolt"></i>: ' + card.attack + ' ' : '') 	
              + (typeof card.hp !== "undefined" ? '<i class="fas fa-heartbeat"></i>: ' + card.hp + ' ' : '') 	
            + '</div>'
            + '<small>' + (typeof card.faction !== "undefined" ? card.faction + (card.signature_loyal == "Loyal" ? ' (Loyal)' : '') : 'Neutral')
            + ' :: ' + card.pack + ' #' + parseInt(card.position,10) + '</small>'
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
    }
	});
	
	$('.qtip-search').on('mousemove', function () {
		$(this).qtip({
			overwrite: false,
			show: {
				ready: true
			},
			content: {
				text:  'Search tags:'
          + '<br>Card Name'
					+ '<br>a:&nbsp;&nbsp; Attack Value'
          + '<br>c:&nbsp;&nbsp; Command Icons'
          + '<br>e:&nbsp;&nbsp; Pack Code'
          + '<br>f:&nbsp;&nbsp; Faction Code'
          + '<br>h:&nbsp;&nbsp; HP'
          + '<br>r:&nbsp;&nbsp; Cost\Resources'
          + '<br>s:&nbsp;&nbsp; Shields'
          + '<br>t:&nbsp;&nbsp; Type Code'
          + '<br>x:&nbsp;&nbsp; Card Text'
          + '<br>y:&nbsp;&nbsp; Cycle ID'
			},
			style: {
				classes: 'qtip-dark qtip-rounded whk-tooltip',
				tip: false
			},
			position: {
				my: 'left top',
				at: 'right top'
			},
			hide:	{
				//event: 'unfocus'
			}
		});
	});
	
});