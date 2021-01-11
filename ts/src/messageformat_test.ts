import { PlainText, Placeholder, SimpleMessage } from './messageformat';

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
