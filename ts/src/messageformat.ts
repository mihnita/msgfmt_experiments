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
	format(locale: string, parameters: Map<string, unknown>): string {
		let result = '';
		for (const idx in this.parts) {
			const part = this.parts[idx];
			if (part instanceof PlainText) {
				result = result.concat(part.format());
			} else if (part instanceof Placeholder) {
				result = result.concat(part.format(locale, parameters));
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
	format(locale: string, parameters: Map<string, unknown>): string {
		console.log(locale);
		console.log(parameters);
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
	format(locale: string, parameters: Map<string, unknown>): string {
		const value = parameters.get(this.name); // the runtime value of the placeholder

		const options: {[k: string]: unknown} = {};
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
			return String(value);
		}
		return '<undefined ' + this.name + '>';
	}
}
