export type MFDM = // For `MessageFormat Data Model`, but need a better name
	| ISimpleMessage
	| ISelectorMessage;

export interface ISimpleMessage {
	parts: IPart[];
	format(locale: string, parameters: Map<string, unknown>): string;
}

export interface ISelectorMessage {
	switches: ISwitch[];
	// The order matters. So we need a "special map" that keeps the order
	messages: Map<ICase[], ISimpleMessage>;
	format(locale: string, parameters: Map<string, unknown>): string;
}

export interface ISwitch {
	name: string; // the variable to switch on
	type: string; // plural, ordinal, gender, select, ...
}

export type ICase =
	| string
	| number;

export type IPart =
	| IPlainText // we can probably use `string` here
	| IPlaceholder;

export interface IPlainText {
	value: string;
	format(): string;
}

export interface IPlaceholder {
	name: string;
	type: string;
	flags: Map<string, string>;
	format(locale: string, parameters: Map<string, unknown>): string;
}
