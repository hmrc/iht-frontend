//=============================================
// Polyfill for input event on backspace/delete key for IE9
// https://github.com/buzinas/ie9-oninput-polyfill/blob/master/ie9-oninput-polyfill.js
//=============================================
(function (d) {
  if (navigator.userAgent.indexOf('MSIE 9') === -1) return;

  d.addEventListener('selectionchange', function() {
    var el = d.activeElement;

    if (el.tagName === 'TEXTAREA' || (el.tagName === 'INPUT' && el.type === 'text')) {
      var ev = d.createEvent('CustomEvent');
      ev.initCustomEvent('input', true, true, {});
      el.dispatchEvent(ev);
    }
  });
})(document);