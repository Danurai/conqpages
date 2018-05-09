$(document).ready(function () {
  
  $('#roster').on('click','.btn-delete',function () {
    $('#deletealert').html ("Are you sure you want to delete the deck " + $(this).data('deckname') + "?");
    $('#deletedeckuid').val ($(this).data('deckuid'));
    $('#deletedeck').modal('show');
  });
})