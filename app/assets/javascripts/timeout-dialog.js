/*
 * Script adapted from timeout-dialog.js v1.0.1, 01-03-2012
 *
 * @author: Rodrigo Neri (@rigoneri)
 *
 * (The MIT License)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

function secondsToTime (secs) {
  var hours = Math.floor(secs / (60 * 60))

  var divisorForMinutes = secs % (60 * 60)
  var minutes = Math.floor(divisorForMinutes / 60)

  var divisorForSeconds = divisorForMinutes % 60
  var seconds = Math.ceil(divisorForSeconds)

  var obj = {
    'h': hours,
    'm': minutes,
    's': seconds
  }
  return obj
}

String.prototype.format = function () {
  var s = this
  var i = arguments.length

  while (i--) {
    s = s.replace(new RegExp('\\{' + i + '\\}', 'gm'), arguments[i])
  }
  return s
}

!function ($) {
  $.timeoutDialog = function (options) {
    var settings = {
      timeout: 900,
      countdown: 60,
      time: 'minutes',
      title: 'You’re about to be signed out',
      message: 'For your security, you’ll be signed out in <span class="countdown">{0} {1}</span> if there’s no activity on your account. ',
      keep_alive_url: '/keep-alive',
      logout_url: '/sign-out',
      restart_on_yes: true,
      dialog_width: 340,
      close_on_escape: true,
      background_no_scroll: true,
      keep_alive_button_text: 'Get another {0} minutes'
    }

    $.extend(settings, options)

    var TimeoutDialog = {
      init: function () {
        this.setupDialogTimer()
      },

      setupDialogTimer: function () {
        var self = this
        window.setTimeout(function () {
          self.setupDialog()
        }, ((settings.timeout) - (settings.countdown)) * 1000)
      },

      setupDialog: function () {
        // check this
        var dialogOpen = true
        var self = this
        self.destroyDialog()
        if (settings.background_no_scroll) {
          $('html').addClass('noScroll')
        }
        var time = secondsToTime(settings.countdown)
        var timeout = secondsToTime(settings.timeout)
         // ignored seconds time.m used below
        $('<div id="timeout-dialog" class="timeout-dialog" role="dialog" aria-labelledby="timeout-message" tabindex="-1">' + 
            '<h2 class="heading-medium">' + settings.title + '</h2>' + 
            '<p id="timeout-message">' + settings.message.format('<span id="timeout-countdown">' + time.m + '</span>' ,
                '<span id="timeout-Seconds">' + settings.time + '</span>') + '</p>' + 
            '<button id="timeout-keep-signin-btn" class="button">' + settings.keep_alive_button_text.format('<span id="timeout-countdown">' + settings.timeout / 60 + '</span>') + '</button>' + 
        '</div>' + 
        '<div id="timeout-overlay" class="timeout-overlay"></div>') 
        .appendTo('body')

        var activeElement = document.activeElement

        var modalFocus = document.getElementById("timeout-dialog")
        modalFocus.focus()

        document.addEventListener("focus", function (event) {
        var modalFocus = document.getElementById("timeout-dialog")
          if(dialogOpen && !modalFocus.contains(event.target)) {
            event.stopPropagation()
            modalFocus.focus()
          }
        }, true)

        self.startCountdown()

        self.escPress = function (event) {
          if (dialogOpen && event.keyCode === 27) {
            // close the dialog
            self.keepAlive()
            activeElement.focus()
          }
        }

        self.closeDialog = function () {
          if (dialogOpen) {
            self.keepAlive()
            activeElement.focus()
          }
        }

        document.addEventListener('keydown', self.escPress, true)
        document.getElementById('timeout-keep-signin-btn').addEventListener('click', self.closeDialog, true)
      },

      destroyDialog: function () {
        if ($('#timeout-dialog').length) {
          dialogOpen = false
          $('.timeout-overlay').remove();
          $('#timeout-dialog').remove()
          if (settings.background_no_scroll) {
            $('html').removeClass('noScroll')
          }
        }
      },

      startCountdown: function () {
        var self = this
        var counter = settings.countdown

        this.countdown = window.setInterval(function () {
          counter -= 1
          if (counter < 60) {
            $('#timeout-countdown').html(counter)
            $('#timeout-Seconds').html('seconds')
          } else if (counter % 60 === 0) {
            $('#timeout-countdown').html(counter / 60)
            $('#timeout-Seconds').html('minutes')
          }

          if (counter <= 0) {
            window.clearInterval(self.countdown)
            self.signOut()
          }
        }, 1000)
      },

      keepAlive: function () {
        var self = this
        this.destroyDialog()
        window.clearInterval(this.countdown)
        document.removeEventListener('keydown', self.escPress)

        $.get(settings.keep_alive_url, function (data) {
          if (data === 'OK') {
            if (settings.restart_on_yes) {
              self.setupDialogTimer()
            }
          }
          else {
            self.signOut()
          }
        })
      },

      signOut: function () {
        var self = this
        this.destroyDialog()

        window.location = settings.logout_url

       }

    }

    TimeoutDialog.init()
  }
}(window.jQuery)