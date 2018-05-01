$(document).ready(function () {
    
  var _cards;
   $.getJSON('/api/data/cards',function (data) {
     _cards = TAFFY(data.data);
   });
   
  $('a.list-group-item').on('mouseover', function () {
    var outp = '';
    var r = _cards({"code":$(this).data("code").toString()}).first();
    
    var sig = _cards({"signature_squad":r.signature_squad.toString(),"type_code":{"!is":"warlord_unit"}});
    
    outp += '<div class="row-fluid"><div class="col-sm-12 text-center">'
            + '<img class="my-2 img-fluid rounded mx-auto d-block" src="' + r.img + '" alt="' + r.name +'"></img>'
          + '</div></div>';
          
    outp += '<div class="row">';
    sig.each(function (s) {
      outp += '<div class="col-sm-6">'
            + '<img class="my-2 img-fluid" src="' + s.img + '" alt="' + s.name +'"></img>'
            + '</div>';
    });
    outp += '</div>';
    
    $('#warlordcards').html (outp);
    
  });  
});