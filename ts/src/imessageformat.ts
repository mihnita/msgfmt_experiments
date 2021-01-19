export interface IMessage {
	id: string;
	locale: string;
	format(parameters: Map<string, unknown>): string;
}

export interface ISimpleMessage extends IMessage {
	parts: IPart[];
}

export interface ISelectorMessage extends IMessage {
	switches: ISwitch[];
	// The order matters. So we need a "special map" that keeps the order
	messages: Map<ICase[], ISimpleMessage>;
}

/** This has a function associated with it. */
export interface ISwitch {
	name: string; // the variable to switch on
	type: string; // plural, ordinal, gender, select, ..
}

export type ICase = string | number;

export type IPart = IPlainText | IPlaceholder;

export interface IPlainText {
	value: string;
}

/** This also has a function associated with it. */
export interface IPlaceholder {
	name: string;
	type: string;
	flags: Map<string, string>;
	// I don't think we want this in the data model, but keeping it for now
	format(locale: string, parameters: Map<string, unknown>): string;
}
