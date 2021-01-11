export type MFDM = // For `MessageFormat Data Model`, but need a better name
	| SimpleMessage
	| SelectorMessage;

export class SimpleMessage {
	parts: Part[];
	constructor(parts: Part[]) {
		this.parts = parts;
	}
}

export class SelectorMessage {
	switches: Switch[];
	// The order matters. So we need a "special map" that keeps the order
	messages: Map<Case[], SimpleMessage>;
	constructor(switches: Switch[], messages: Map<Case[], SimpleMessage>) {
		this.switches = switches;
		this.messages = messages;
	}
}

export class Switch {
	name: string; // the variable to switch on
	type: string; // plural, ordinal, gender, select, ...
	constructor(name: string, type: string) {
		this.name = name;
		this.type = type;
	}
}

export type Case =
	| string
	| number;

export type Part =
	| PlainText // we can probably use `string` here
	| Placeholder;

export class PlainText {
	value: string;
	constructor(value: string) {
		this.value = value;
	}
}

export class Placeholder {
	name: string;
	type: string;
	flags: Map<string, string>;
	constructor(name: string, type: string, flags: Map<string, string>) {
		this.name = name;
		this.type = type;
		this.flags = flags;
	}
}
