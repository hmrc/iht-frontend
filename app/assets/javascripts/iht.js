$(document).ready(function() {

// =====================================================
// Sets the lang attribute of html to the language selected
// =====================================================
if($('article').attr('lang')){
    $("html").attr('lang', $("article").attr('lang'));
}

// =====================================================
// Remove hidden attribute from js-visible elements
// =====================================================
$('.js-visible').removeAttr('hidden');

// =====================================================
// Initialise show-hide-content
// Toggles additional content based on radio/checkbox input state
// =====================================================
var showHideContent = new GOVUK.ShowHideContent()
showHideContent.init()


// =====================================================
// Combine two values to display a third
// e.g. used on Gifts details
// =====================================================
  combinedValue();


// =====================================================
// Check for hashed url and jump to input if needed
// The non-error-list focus will not focus on the input for iOS due to security restrictions
// The in-page link-click focus setting is not affected
// =====================================================
    var hash = window.location.hash.substring(1);
    if(hash > ""){
        hashJump(hash);
    }

    // error summary jump links
    $('.error-list a').on('click', function(e){
        var linkhash = $(this).attr("href").replace('#','');
        hashJump(linkhash);
    });


// =====================================================
// Declaration
// =====================================================
    function runDec(btn){
      if(btn[0].checked){
          $(".toDisableButton").prop( "disabled", false );
      }else{
          $(".toDisableButton").prop( "disabled", true );
      }
    }
    if($("#isDeclared").length > 0){
        runDec($("#isDeclared"));

        $(".toDisableButton").attr('id', 'accept-button');
        $("#isDeclared").attr('aria-controls', 'accept-button');
        $("#isDeclared").click(function(){
            runDec($(this));
        });
    }


// =====================================================
// reset an input when a radio button changes value
// =====================================================
  $("[data-clear-target]").click(function(){
      clearInput($(this));
  });

// =====================================================
// polyfill for inline name wrapping on IE < 10
// Fixes where IE doesn't wrap if the container is not display: block
// This finds the inline name and passes a class to the parent container
// Not used by default as causes ugly wrapping of non-name copy
// Wrapped in browser UA sniffing as feature detection would not work here
// =====================================================
if(typeof window.navigator != "undefined" && typeof window.navigator.userAgent != "undefined"){
    if(window.navigator.userAgent.indexOf("MSIE 9") > 0 || window.navigator.userAgent.indexOf("MSIE 8") > 0){
        $('span.copy--restricted').not("a > .copy--restricted").each(function(){
            var wrapItem = $(this);
            var parentContents = wrapItem.parent().html();
            wrapItem.parent().html('<div class="copy--restricted-wrapper">' + parentContents + '</div>');

        });
    }
}

// =====================================================
// Country code autocomplete
// =====================================================
if($('[data-enhanced-select]').length > 0){
    var countryCode = new Autobox($('select'));
}

// =====================================================
// Handle the IHT progressive disclosure open/close functionality
// =====================================================
 $("#get-help").one("click", function() {
    $("#get-help-action").click();
 });

// =====================================================

// Submit trigger
// Used on Gifts section
// Should be replaced by links without the POST through
// =======================================================
  $(".js-submitButtonAction").click(function(){
      var submitValue = $(this).attr("data-submitValue");
      $("form").append("<input type='hidden' name='action' value='"+submitValue+"'>");
      $("form").trigger('submit');
  });

// =====================================================
// Handle number inputs
// =====================================================
  numberInputs();


// =====================================================
// Handle the IHT UR panel dismiss link functionality
// =====================================================
    var cookieData=GOVUK.getCookie("mdtpurr");
    if (cookieData==null) {
        $("#ur-panel").addClass("banner-panel--show");
    }

    $(".banner-panel__close").on("click", function(e) {
        e.preventDefault();
         GOVUK.setCookie("mdtpurr", "suppress_for_all_services", 99999999999);
         $("#ur-panel").removeClass("banner-panel--show");
    });
  // end of on doc ready
});


/// =====================================================
/// Hash jump
/// Takes user to location of input which needs error fixing
/// Also looks for a parent label or fieldset and ensures the page is scrolled to leave that visible
/// =====================================================
function hashJump(hash){
    hash = hash.replace('.','\\\.');
    if (hash.length != 0) {
        if($('#' + hash).length > 0){
            var el = $('#' + hash);
            if(el[0].tagName == "INPUT"){
                // set fallback scroll of the input
                var scrollPos = el.offset().top;
                if (el[0].type == "radio" || el[0].type == "checkbox"){
                    // focus on the group legend if present, else on the label
                    if(el.parents('fieldset').length > 0){
                        scrollPos = el.parents('fieldset').first().find('legend').offset().top;
                    }
                } else {
                    if($('label[for="' + hash + '"]').length > 0){
                        scrollPos = $('label[for="' + hash + '"]').offset().top;
                        setFocus($('#' + hash));
                    }
                }
                setTimeout(function(){
                    $(window).scrollTop(scrollPos);
                }, 50);

            } else {
                 // scroll and set focus on inputs when anchor is on container
                 setInputFocus(hash);
            }
        }
    }
}


/// =====================================================
/// Set Focus
/// cross-device focus set
/// We set via a timeout for desktop browsers
/// We also set immediately as iOS will not support a timeout set due to security rules
/// Both are needed as support is not cross-device for a single implementation
/// =====================================================
function setFocus(el){
    el.focus();
    setTimeout(function(){
        el.focus();
    }, 10);
}



/// =====================================================
/// Set Input Focus
/// finds an input in a given element with id of elid and sets focus
/// Will focus on either:
/// 1. An input *outside* of a fieldset which is related to the given label id
/// This captures standard label + input[text] combinations
/// 2. An input of type[text||tel||number] which is the first visible input inside a fieldset
/// This captures patterns such as date-of-birth where it is desirable to have focus on the first input
/// 3. An already selected option in a group of checkboxes/radio-buttons
/// We can safely target this as it will playback the selected option
///
/// In all other instances focus will not be set
/// =====================================================
function setInputFocus(elid) {
    var labelTarget = $('label[id="' + elid + '"], div[id="' + elid + '"]');
    var fieldsetTarget = $('[id="' + elid + '"] fieldset, fieldset[id="' + elid + '"]').first();

    if(labelTarget.length > 0 && fieldsetTarget.length == 0){
       setFocus(labelTarget.find('input').first());
    }

    if(fieldsetTarget.length > 0){
        // focus on first text field if it is the first visible input in a fieldset
        var firstInput = fieldsetTarget.find('input:visible').first();
        if(firstInput.attr('type') == "text" || firstInput.attr('type') == "tel" || firstInput.attr('type') == "number"){
            // text input inside a fieldset
            setFocus(firstInput);
        } else {
            // non-focus-able inputs
            // but we can focus on a checked radio button or checkbox
            if(fieldsetTarget.find(':checked').length > 0){
                 setFocus(fieldsetTarget.find(':checked').first());
            } else {
                fieldsetTarget.first().click();
            }
        }
    }
}

// =====================================================
// Clear inputs
// Used to reset an input when a radio button is changed
// eg clear a value text input when a Yes is changed to a No
// =======================================================
function clearInput(el){
  var target = el.attr("data-clear-target");
  if($("#"+target+"").length){
      $('#'+target).val("");
  }
}



// =====================================================
// Takes an element and subtracts one input from another to show a combined message
// e.g. used on gifts details
// =====================================================
  function checkforCombinedValue(el){
    // input we are taking as a start value
    var addValue = $('#' + el.attr('data-combine-add'));
    var addValueVal = 0;
    if(isNumeric(addValue.val())){
      addValueVal = addValue.val();
    }
    // input we are going to subtract from the start value
    var subtractValue = $('#' + el.attr('data-combine-subtract'));
    var subtractValueVal = 0;
    if(isNumeric(subtractValue.val())){
      subtractValueVal = subtractValue.val();
    }
    // calc
    var total = addValueVal - subtractValueVal;

    // conditionally show the message if we have a valid number
    if(total >= 0){
     if( (addValueVal.toString().indexOf('.')==-1) && (subtractValueVal.toString().indexOf('.')==-1) ) {
      el.html(el.attr('data-combine-copy') + total);
      } else {
      el.html(el.attr('data-combine-copy') + total.toFixed(2));}
    } else {
      // maybe need to show a message here if the calc is invalid to update screen readers
      el.html(el.attr('data-combine-copy') + 0);
    }
  }



// =====================================================
// Fires checkforCombinedValue on page load and on input change
// =====================================================
  function combinedValue(){
    $('.js-combined-value').each(function(){
      var el = $(this);
      var timer;
      function trackKey(){
        // gives a small delay after key input before updating value
        timer = setInterval(function(){
          clearInterval(timer);
          checkforCombinedValue(el);
        }, 500);
      }


      // check on page load
      checkforCombinedValue(el);

      // check when either input changes
      $('[aria-controls="' + el.attr('id') + '"]').each(function(){
        $(this).on('keyup blur', function(){
          if(timer != undefined){
            clearInterval(timer);
          }
          trackKey();
        });
      });

    });
  }



// ======================================================
// checks if a value is numeric
// ======================================================
function isNumeric(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}



function numberInputs() {
    // =====================================================
    // Set currency fields to number inputs on touch devices
    // this ensures on-screen keyboards display the correct style
    // don't do this for FF as it has issues with trailing zeroes
    // =====================================================
    if($('html.touchevents').length > 0 && window.navigator.userAgent.indexOf("Firefox") == -1){
        $('[data-type="currency"]').each(function(){
          $(this).attr('type', 'number');
          $(this).attr('step', 'any'); 
          $(this).attr('min', '0');
        });
    }

    // =====================================================
    // Disable mouse wheel and arrow keys (38,40) for number inputs to prevent mis-entry
    // also disable commas (188) as they will silently invalidate entry on Safari 10.0.3 and IE11
    // =====================================================
    $("form").on("focus", "input[type=number]", function(e) {
        $(this).on('wheel', function(e) {
            e.preventDefault();
        });
    });
    $("form").on("blur", "input[type=number]", function(e) {
        $(this).off('wheel');
    });
    $("form").on("keydown", "input[type=number]", function(e) {
        if ( e.which == 38 || e.which == 40 || e.which == 188 )
            e.preventDefault();
    });
}

//=======================================================================
//GA for get help form submit button
//=======================================================================

$( document ).ajaxComplete(function() {
    $( "#report-submit" ).on('click', function () {
        ga('send', 'event','page', 'Click' , 'Get help with this page Submit');
    });
});
