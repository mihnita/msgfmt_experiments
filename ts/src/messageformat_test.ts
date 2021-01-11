import { PlainText, Placeholder, SimpleMessage } from './messageformat';

// TODO: some helper functions to make things less verbose?
var parts = [
	new PlainText('Hello '),
	new Placeholder('user', '', new Map<string, string>()),
	new PlainText(', using locale '),
	new Placeholder('locale', '', new Map<string, string>()),
	new PlainText(' the date is '),
	new Placeholder('theDay', 'date', new Map<string, string>([
		['year', 'numeric'],
		['month', 'short'],
		['day', 'numeric']
	])),
	new PlainText('.\nA large currency amount is '),
	new Placeholder('bigCount', 'number', new Map<string, string>([
		['style', 'currency'],
		['currency', 'EUR']
	])),
	new PlainText('\nA percentage is '),
	new Placeholder('count', 'number', new Map<string, string>([['style', 'percent']])),
	new PlainText('.\n')
]

var msg = new SimpleMessage(parts);
console.log(msg);

var locale = 'en-IN';
// Also a friendliner method, something that takes a JS `Object`?
var result = msg.format(locale, new Map<string, Object>([
	['user', 'John'],
	['count', 14.2],
	['locale', locale],
	['count1', 1],
	['bigCount', 1234567890.97531],
	['theDay', new Date(2019, 11, 29)]
]));

console.log(result);

var expected = 'Hello John, using locale en-IN the date is 29 Dec 2019.\n' +
	'A large currency amount is €1,23,45,67,890.98\n' +
	'A percentage is 1,420%.\n';

if (result == expected) {
	console.log('\u001b[32mALL GOOD\u001b[m');
} else {
	console.log('\u001b[91mFAILURE:\u001b[m\n' +
		'\u001b[93mexpected:\u001b[m ' + expected + '\n' +
		'\u001b[93mbut got :\u001b[m ' + result + '\n');
}
