
$(".ga-track-anchor-click").click(function(event) {
  var target = $(this).attr('target')
  if ( $(this).is('a') && (target == '' || target == '_self') ) {
    event.preventDefault();
    var redirectUrl = $(this).attr('href');
    gaWithCallback('send', 'event', $(this).data('ga-event-category'), $(this).data('ga-event-action'), $(this).data('ga-event-label'), function() {
      window.location.href = redirectUrl;
    });

  } else {
    ga('send', 'event', $(this).data('ga-event-category'), $(this).data('ga-event-action'), $(this).data('ga-event-label'));
  }
});

function gaWithCallback(send, event, category, action, label, callback) {
  ga(send, event, category, action, label, {
    hitCallback: gaCallback
  });
  var gaCallbackCalled = false;
  setTimeout(gaCallback, 5000);

  function gaCallback() {
    if(!gaCallbackCalled) {
      callback();
      gaCallbackCalled = true;
    }
  }
}
