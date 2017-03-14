var Autobox = function(selectInput, enhancedInput, suggestionList, statusContainer){
    this.select = selectInput;      //the (hidden) select input the autocomplete will be searching
    this.input = enhancedInput;     //the text field the user will be typing into
    this.list = suggestionList;     //the list to hold the suggestions
    this.status = statusContainer;  //the container to populate with status updates for screenreaders
    this.cList;                     //the currently selected suggestion
    this.cListVal;                  //the related value
    this.listItems;                 //the list of suggestions

    this.setInitialValue();
    this.addEvents();
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
        console.log('event - input');
        _autobox.update();
    });

    //capture non-input keys (suggestion navigation, suggestion selection, space)
    _autobox.input.on('keydown', function(e){
        var hasActiveOptions = _autobox.list.find('.suggestion--active').length > 0;
        switch(e.keyCode) {
        case 32: //space
            _autobox.update();
            break;
        case 27: //escape, removes suggestion list
            if(_autobox.list.hasClass('suggestions--with-options')){
                _autobox.list.html("");
                _autobox.closeSuggestionList();
                _autobox.status.html("Suggestions list closed");
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
    _autobox.status.html(_autobox.cListVal + " selected");
    // update select
    _autobox.select.val("").change();

    var selectedValue = _autobox.select.find('option[data-title="' + _autobox.cListVal.toLowerCase() + '"]').val();
    console.log(_autobox.cList);
    console.log(selectedValue);
    _autobox.select.val(selectedValue).change();
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
    _autobox.status.html(foundMatches.length + " matches found. Use arrow keys or swipe to navigate the list");
}

//=============================================
// Returns a description of the suggestion and its place in the list
//=============================================
Autobox.prototype.displayPosition = function(item){
    var _autobox = this;
    return " (" + ( _autobox.listItems.index(item) + 1) + " of " + _autobox.listItems.length + ")"
}