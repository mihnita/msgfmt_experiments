const { expect } = require('chai')
const { describe } = require('mocha')
const { it } = require('mocha')
const MessageFormat = require('../js/messageformat.js').MessageFormat

describe('Test for MessageFormat:', function () {
  it('Simple test for MessageFormat', function () {
    var msg = [
      'Hello ',
      { name: 'user' },
      ', using locale ',
      { name: 'locale' },
      ' the date is ',
      { name: 'theDay', type: 'date', style: { year: 'numeric', month: 'short', day: 'numeric' } },
      '.\nA large currency amount is ',
      { name: 'bigCount', type: 'number', style: { style: 'currency', currency: 'EUR' } },
      '\nA percentage is ',
      { name: 'count', type: 'number', style: { style: 'percent' } },
      '.\n'
    ]

    var locale = 'en-IN'
    var msgArgs = {
      user: 'John',
      count: 14.2,
      locale: locale,
      count1: 1,
      bigCount: 1234567890.97531,
      theDay: new Date(2019, 11, 29)
    }

    var expectedMsg = 'Hello John, using locale en-IN the date is 29 Dec 2019.\n' +
        'A large currency amount is €\u00A01,23,45,67,890.98\n' +
        'A percentage is 1,420%.\n'

    var mf = new MessageFormat(msg, locale)
    var actual = mf.format(msgArgs)

    expect(expectedMsg).to.equal(actual)
  })
})
