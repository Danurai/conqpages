var _cards;

$(document).ready(function () {
  $.getJSON("/api/data/cards",function (data) {
    _cards = TAFFY(data.data);
  });
});