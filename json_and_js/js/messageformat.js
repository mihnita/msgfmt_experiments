class MessageFormat {
  constructor (message, locale) {
    this.message = message
    this.locale = locale
  }

  format (args) {
    var result = ''
    for (var idx in this.message) {
      var part = this.message[idx]
      if (part) {
        if (typeof part === 'string' || part instanceof String) {
          result += part
        } else {
          var name = part.name
          var type = part.type
          var options = part.style
          var value = args[name] // the runtime value of the placeholder
          var fmt = MessageFormat.default_known_formatters[type]
          if (fmt) {
            result += fmt(this.locale, options).format(value)
          } else {
            result += value
          }
        }
      }
    }
    return result
  }
}

MessageFormat.default_known_formatters = {
  date: Intl.DateTimeFormat,
  time: Intl.DateTimeFormat,
  number: Intl.NumberFormat
}

exports.MessageFormat = MessageFormat
