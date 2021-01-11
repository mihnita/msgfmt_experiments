import {ICase} from './imessageformat';
import {IPart} from './imessageformat';
import {IPlaceholder} from './imessageformat';
import {IPlainText} from './imessageformat';
import {ISelectorMessage, } from './imessageformat';
import {ISimpleMessage} from './imessageformat';
import {ISwitch} from './imessageformat';

export class SimpleMessage implements ISimpleMessage {
	parts: IPart[];
	constructor(parts: IPart[]) {
		this.parts = parts;
	}
	format(locale: string, parameters: Map<string, object>): string {
		var result = '';
		for (var idx in this.parts) {
			var part = this.parts[idx];
			if (part instanceof PlainText) {
				result += part.format();
			} else if (part instanceof Placeholder) {
				result += part.format(locale, parameters);
			}
		}
		return result;
	}
}

export class SelectorMessage implements ISelectorMessage {
	switches: ISwitch[];
	// The order matters. So we need a "special map" that keeps the order
	messages: Map<ICase[], ISimpleMessage>;
	constructor(switches: Switch[], messages: Map<ICase[], ISimpleMessage>) {
		this.switches = switches;
		this.messages = messages;
	}
	format(locale: string, parameters: Map<string, object>): string {
		throw new Error('Method not implemented.');
	}
}

export class Switch implements ISwitch {
	name: string; // the variable to switch on
	type: string; // plural, ordinal, gender, select, ...
	constructor(name: string, type: string) {
		this.name = name;
		this.type = type;
	}
}

export class PlainText implements IPlainText {
	value: string;
	constructor(value: string) {
		this.value = value;
	}
	format(): string {
		return this.value;
	}
}

interface IFormatter {
	format(value: object) : string;
}

class DateTimeFormater implements IFormatter {
	locale: string;
	flags: Object;
	constructor (locale: string, flags: Map<string, string>) {
		this.locale = locale;
		this.flags = flags;
	}
	format(value: object): string {
		if (value instanceof Date) {
			return Intl.DateTimeFormat(this.locale, this.flags).format(value);
		} else {
			return '{' + value + '}';
		}
	}
}

export class Placeholder implements IPlaceholder {
	name: string;
	type: string;
	flags: Map<string, string>;

	// _default_known_formatters = new Map<string, IFormatter>([
	// 	['date', Intl.DateTimeFormat],
	// 	['time', Intl.DateTimeFormat],
	// 	['number', Intl.NumberFormat]
	// ]);

	constructor(name: string, type: string, flags: Map<string, string>) {
		this.name = name;
		this.type = type;
		this.flags = flags;
	}
	format(locale: string, parameters: Map<string, object>): string {
		var value = parameters.get(this.name); // the runtime value of the placeholder

		var options: {[k: string]: any} = {};
		this.flags.forEach((val: string, key: string) => {
			options[key] = val;
		});

		// Need to figure out how to create a map of formatters.
		// Did in in JavaScript, have to figure out how to do in in TypeScript.
		// So that we can do something like this:
		//     fmt = this._default_known_formatters.get(this.type);
		// and support custom formatter types.
		if (this.type == 'date' || this.type == 'time') {
			if (value instanceof Date) {
				return Intl.DateTimeFormat(locale, options).format(value);
			}
			if (value instanceof Number) {
				return Intl.DateTimeFormat(locale, options).format(value.valueOf());
			}
		} else if (this.type == 'number') {
			if (value instanceof Number || typeof value === "number") {
				return Intl.NumberFormat(locale, options).format(value.valueOf());
			}
		} else if (value) {
			return value.toString();
		}
		return '<undefined ' + this.name + '>';
	}
}
