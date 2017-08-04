var Autobox = function(selectInput){
    if($('html.lte-ie8').length > 0){ return false; }

    this.select = selectInput;      //the (hidden) select input the autocomplete will be searching

    this.cList;                     //the currently selected suggestion
    this.cListVal;                  //the related value
    this.listItems;                 //the list of suggestions

    this.messageMatches = this.select.attr("data-matches");
    this.messageOf = this.select.attr("data-options");
    this.messageClose = this.select.attr("data-close");
    this.messageSelected = this.select.attr("data-selected");

    this.addUI();
    this.setInitialValue();
    this.addEvents();
}


Autobox.prototype.addUI = function(){
    var _autobox = this;
    var autoUI, inputBox, listBox, statusBox;
    inputBox = '<input type="text" name="iht-auto-complete" id="iht-auto-complete" class="form-control form-control--block js-iht-auto-complete" autocomplete="off" spellcheck="false" data-rule-suggestion="true" role="combobox" aria-owns="iht-suggestions-list" aria-expanded="false" />';
    listBox = '<ul role="listbox" class="suggestions js-suggestions" id="iht-suggestions-list"></ul>';
    statusBox = '<span role="status" aria-live="assertive" aria-relevant="text" class="visually-hidden js-suggestions-status-message" id="iht-autoCompleteSuggestionStatus"></span>';

    autoUI = inputBox + '<div class="suggestions-input-container">' + listBox + statusBox + '</div>';

    _autobox.select.addClass('js-hidden');
    _autobox.select.after(autoUI);

    // update label to point to the enhanced input
    $('[for="' + _autobox.select.attr('id') + '"]').attr('for', 'iht-auto-complete')

    _autobox.input = $('#iht-auto-complete');                   //the text field the user will be typing into
    _autobox.list = $('#iht-suggestions-list');                 //the list to hold the suggestions
    _autobox.status = $('#iht-autoCompleteSuggestionStatus');  //the container to populate with status updates for screenreaders


}


//=============================================
// If the select input has a selected value this assigns it to the input also
//=============================================
Autobox.prototype.setInitialValue = function(){
    var _autobox = this;
    _autobox.select.find('option').each(function(){
        $(this).attr('data-title', $(this).text().toLowerCase())
        if($(this).attr('value') == _autobox.select.val()){
            _autobox.input.val($(this).text());
        }
    });
}

//=============================================
// Add event listeners
//=============================================
Autobox.prototype.addEvents = function(){
    var _autobox = this;
    //select item from list of suggestions
    _autobox.list.on('click', 'li', function(){
        _autobox.updateCurrentSuggestion($(this));
        _autobox.selectOption();
    });

    //listen for changes to the input
    _autobox.input.on('input', function(e){
        _autobox.update();
    });

    //add focus item for screenreader touch
    _autobox.list.on('focus', 'li', function(){
        _autobox.updateCurrentSuggestion($(this));
        _autobox.status.html($(this).text() + _autobox.displayPosition($(this)));
    });

    //capture non-input keys (suggestion navigation, suggestion selection, space)
    _autobox.input.on('keydown', function(e){
        var hasActiveOptions = _autobox.list.find('.suggestion--active').length > 0;
        switch(e.keyCode) {
        case 32: //space
            _autobox.update();
            break;
        case 9: //tab
        case 27: //escape, removes suggestion list
            if(_autobox.list.hasClass('suggestions--with-options')){
                _autobox.list.html("");
                _autobox.closeSuggestionList();
                _autobox.status.html(_autobox.messageClose);
            }
            break;
        case 40: //down arrow
        case 38: //up arrow
            e.preventDefault();
            _autobox.listItems = _autobox.list.find("li");
            if(hasActiveOptions) {
                _autobox.updateCurrentSuggestion(_autobox.list.find('.suggestion--active'));
                if(e.which == 40){
                    //down
                    if(_autobox.cList.next('li').length > 0){
                        var nList = _autobox.cList.next('li');
                        _autobox.updateCurrentSuggestion(nList);
                        _autobox.status.html(nList.text() + _autobox.displayPosition(nList));
                    }
                }
                if(e.which == 38){
                    //up
                    if(_autobox.cList.prev('li').length > 0){
                        var nList = _autobox.cList.prev('li');
                        _autobox.updateCurrentSuggestion(nList);
                        _autobox.status.html(nList.text() + _autobox.displayPosition(nList));
                    }
                }
            } else {
                if(e.which == 40){
                    //highlight first item
                    var nList = _autobox.list.find('li').first();
                    _autobox.updateCurrentSuggestion(nList);
                    _autobox.status.html(nList.text() + _autobox.displayPosition(nList));
                }
            }
            break;
        default:
            // select on enter if suggestions are present and one is highlighted
            if(e.which == 13 && hasActiveOptions) {
                e.preventDefault();
                _autobox.updateCurrentSuggestion(_autobox.list.find('.suggestion--active'));
                _autobox.selectOption()
            }
            break;
        }

    });
}

//=============================================
// Updates currently selected suggestion
//=============================================
Autobox.prototype.updateCurrentSuggestion = function(currentSuggestion){
    var _autobox = this;
    if(_autobox.cList){
        _autobox.cList.removeClass('suggestion--active');
    }
    if(currentSuggestion){
        _autobox.cList = currentSuggestion;
        _autobox.cListVal = currentSuggestion.text();
        currentSuggestion.addClass('suggestion--active');
    }
}

//=============================================
// Makes suggestions list visible
//=============================================
Autobox.prototype.openSuggestionList = function(){
   this.list.addClass('suggestions--with-options');
}

//=============================================
// Hides suggestions list
//=============================================
Autobox.prototype.closeSuggestionList = function(){
   this.list.removeClass('suggestions--with-options');
}

//=============================================
// Updates input and select with selected option
//=============================================
Autobox.prototype.selectOption = function(){
    var _autobox = this;
    // update input
    _autobox.input.val(_autobox.cListVal);
    // clear list
    _autobox.list.html("");
    _autobox.list.removeClass('suggestions--with-options');
    // update status with selected item
    _autobox.status.html(_autobox.messageSelected.replace("{0}", _autobox.cListVal));
    // update select
    _autobox.select.val("").change();

    var selectedValue = _autobox.select.find('option[data-title="' + _autobox.cListVal.toLowerCase() + '"]').val();
    _autobox.select.val(selectedValue).change();
    _autobox.input.focus();
}

//=============================================
// Update list of suggestions based on text from input
//=============================================
Autobox.prototype.update = function(){
    var _autobox = this;
    var inputEntered = _autobox.input.val().toLowerCase().trim();
    // clear suggestions and select
    _autobox.list.html("");
    _autobox.select.val("").change();

    // find matches to text
    var foundMatches = _autobox.select.find('option[data-title^="' + inputEntered + '"]');
    if(foundMatches.length > 0){
        _autobox.openSuggestionList();
    } else {
        _autobox.closeSuggestionList();
    }

    foundMatches.each(function(){
        // add suggestion to the list
        _autobox.list.append('<li role="option" tabindex="-1" class="suggestion">' + $(this).text() + '</li>');
        // update select if this value is an exact match, so the user does not have to select from the list if they choose
        if($(this).text().toLowerCase().trim() == inputEntered){
            _autobox.select.val($(this).val()).change();
        }
    })

    // update status
    _autobox.status.html(_autobox.messageMatches.replace("{0}", foundMatches.length));
}

//=============================================
// Returns a description of the suggestion and its place in the list
//=============================================
Autobox.prototype.displayPosition = function(item){
    var _autobox = this;
    return " " + _autobox.messageOf.replace("{0}", ( _autobox.listItems.index(item) + 1)).replace("{1}", _autobox.listItems.length)
}