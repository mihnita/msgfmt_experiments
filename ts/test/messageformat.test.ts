import { expect } from 'chai';
import 'mocha';
import { PlainText, Placeholder, SimpleMessage } from '../src/messageformat';

describe('Test for MessageFormat:', () => {
	// Common for all tests
	const locale = 'en-IN';

	// The tests
    it('Simple placeholder test', () => {
		const expectedMsg = 'Hello John!\n';
		// TODO: some helper functions to make things less verbose
		const parts = [
			new PlainText('Hello '),
			new Placeholder('user', '', new Map<string, string>()),
			new PlainText('!\n')
		];

		const msgArgs = new Map<string, Object>([
			['user', 'John']
		]);

		// TODO: locale should be passed to the constructor, not to format(...)
		const mf = new SimpleMessage(parts);
		// Also a friendliner method, something that takes a JS `Object`, not a Map
		const actual = mf.format(locale, msgArgs);

        expect(expectedMsg).to.equal(actual);
    });

    it('Date formatting test', () => {
		const expectedMsg = 'Using locale en-IN the date is 29 Dec 2019.\n';
		const parts = [
			new PlainText('Using locale '),
			new Placeholder('locale', '', new Map<string, string>()),
			new PlainText(' the date is '),
			new Placeholder('theDay', 'date', new Map<string, string>([
				['year', 'numeric'],
				['month', 'short'],
				['day', 'numeric']
			])),
			new PlainText('.\n')
		];

		const msgArgs = new Map<string, Object>([
			['locale', locale],
			['theDay', new Date(2019, 11, 29)]
		]);

		const mf = new SimpleMessage(parts);
		const actual = mf.format(locale, msgArgs);

        expect(expectedMsg).to.equal(actual);
    });

    it('Currency formatting test', () => {
		const expectedMsg = 'A large currency amount is €1,23,45,67,890.98\n';
		const parts = [
			new PlainText('A large currency amount is '),
			new Placeholder('bigCount', 'number', new Map<string, string>([
				['style', 'currency'],
				['currency', 'EUR']
			])),
			new PlainText('\n')
		];

		const msgArgs = new Map<string, Object>([
			['bigCount', 1234567890.97531]
		]);

		const mf = new SimpleMessage(parts);
		const actual = mf.format(locale, msgArgs);

        expect(expectedMsg).to.equal(actual);
    });

    it('Percentage formatting test', () => {
		const expectedMsg = 'A percentage is 1,420%.\n';
		const parts = [
			new PlainText('A percentage is '),
			new Placeholder('count', 'number', new Map<string, string>([['style', 'percent']])),
			new PlainText('.\n')
		];

		const msgArgs = new Map<string, Object>([
			['count', 14.2]
		]);

		const mf = new SimpleMessage(parts);
		const actual = mf.format(locale, msgArgs);

        expect(expectedMsg).to.equal(actual);
    });
});
