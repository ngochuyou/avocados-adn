// acowiemz Stock
// aaldredkq Sale
// grosiello8i Personnel
// abisson5m Finance
export const range = (max = 0) => [...Array(max)];

export const intRange = ({ min = undefined, max = 0 }) => {
	if (min == null) {
		return [...Array(max)].map((ele, index) => index);
	}

	let val = min;

	return [...Array(Math.abs(min - max) + 1)].map(ele => val++);
};

export const spread = (max = 0, value = {}) => [...Array(max)].map(ele => value);

export const isEmpty = (obj = {}) => {
	for (let i in obj) {
		return false;
	}

	return true;
}

export const toMap = ({ array = [], map = {}, key = null, value = null }) => {
	array.forEach(ele => map[ele[key]] = (value == null ? ele : value));

	return map;
};

export const isImage = (blob) => {
	if (!(blob instanceof Blob)) {
		return false;
	}

	return blob.type.startsWith('image');
}

export const linear = (list = [], propName = null, value = null) => {
	if (propName == null) {
		return null;
	}

	for (let element of list) {
		if (element[propName] === value) {
			return element;
		}
	}

	return null;
}

export const VIETNAMESE_CHARACTERS = "ÁáÀàẢảÃãẠạĂăẮắẰằẲẳẴẵẶặÂâẤấẦầẨẩẪẫẬậĐđÉéÈèẺẻẼẽẸẹÊêỂểẾếỀềỄễỆệÍíÌìỊịỈỉĨĩỊịÓóÒòỎỏÕõỌọÔôỐốỒồỔổỖỗỘộƠơỚớỜờỞởỠỡỢợÚùÙùỦủŨũỤụƯưỨứỪừỬửỮữỰựÝýỲỳỶỷỸỹỴỵ";
// HTML currently not supporting alpha channel in color input
export const HEX_PATTERN = /^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/;
const HEX3_PATTERN = /#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])/gm;

export const hex3tohex6 = (hex3) => {
	if (!HEX3_PATTERN.test(hex3)) {
		return [null, "Invalid hex3 pattern"];
	}

	try {
		// since HEX3_PATTERN is global, we need to reset it
		// everytime we do exec
		HEX3_PATTERN.lastIndex = 0;

		let match = HEX3_PATTERN.exec(hex3);

		return [`#${match[1]}${match[1]}${match[2]}${match[2]}${match[3]}${match[3]}`, null];
	} catch (exception) {
		return [null, exception];
	}
}

export const normalize = (string) => string.trim().replaceAll(/\s+/g, ' ');

export const negateNegative = (event) => {
	const { value } = event.target;

	if (value < 0) {
		event.target.value = value * -1;
	}
}

export const isNegative = (number) => !isNaN(number) && Number(number) < 0;

export const isObj = (payload) => payload != null && typeof payload === 'object';

export const isString = (payload) => typeof payload === 'string';

export const isBool = (payload) => typeof payload === 'boolean';

export const hasLength = (payload = null) => payload != null && payload.length !== 0;

const ACCEPTABLE_PHONE_NUMBER_PATTERN = /^[\w\d._()+\s-]{4,}$/g;

export const isAcceptablePhoneNumber = (phoneNumber) => {
	ACCEPTABLE_PHONE_NUMBER_PATTERN.lastIndex = 0;

	return ACCEPTABLE_PHONE_NUMBER_PATTERN.test(phoneNumber);
}

export const isAcceptablePhoneNumberErr = "Phone number can only contain: spaces, characters, numbers, '.', '_', '(', ')', '+', '-'";
// eslint-disable-next-line
const EMAIL_PATTERN = /^(([^<>()\[\]\.,;:\s@\"]+(\.[^<>()\[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i;

export const isEmail = (email) => {
	EMAIL_PATTERN.lastIndex = 0;

	return EMAIL_PATTERN.test(email);
}

export const asIf = (predicate = false) => new AsIf(predicate);

export const join = (elements) => elements.join(',');

class AsIf {
	#predicate;
	#callbackWhenTrue;

	constructor(predicate = false) {
		this.predicate = predicate;
	}

	then(callback = () => null) {
		this.callbackWhenTrue = callback;
		
		return this;
	}

	else(callback = () => null) {
		if (this.predicate) {
			return this.callbackWhenTrue(this.predicate);
		}

		return callback(this.predicate);
	}
}

const DAY_NAMES = [
	"Sunday", "Monday", "Tuesday",
	"Wednesday", "Thursday", "Friday",
	"Saturday"
];

const DATE_NAMES = [
	'00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10',
	'11', '12', '13', '14', '15', '16', '17', '18', '19', '20', 
	'21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31'
];

const MONTH_NAMES = [
	"Jan", "Feb", "Mar",
	"Apr", "May", "Jun", "Jul",
	"Aug", "Sep", "Oct",
	"Nov", "Dec"
];

const NUMERIC_MONTH_NAMES = [
	"01", "02", "03",
	"04", "05", "06", "07",
	"08", "09", "10",
	"11", "12"
];

const HOURS_NAMES = [
	"00", "01", "02", "03", "04",
	"05", "06", "07", "08",
	"09", "10", "11", "12",
	"13", "14", "15", "16",
	"17", "18", "19", "20",
	"21", "22", "23",
];

const MINUTE_AND_SECOND_NAMES = [
	'00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10',
	'11', '12', '13', '14', '15', '16', '17', '18', '19', '20',
	'21', '22', '23', '24', '25', '26', '27', '28', '29', '30',
	'31', '32', '33', '34', '35', '36', '37', '38', '39', '40',
	'41', '42', '43', '44', '45', '46', '47', '48', '49', '50',
	'51', '52', '53', '54', '55', '56', '57', '58', '59'
];

export const formatServerDatetime = (datetime) => {
	if (!(datetime instanceof Date) && !isString(datetime)) {
		return null;
	}

	const instant = datetime instanceof Date ? datetime : new Date(datetime);

	return `${instant.getFullYear()}-${NUMERIC_MONTH_NAMES[instant.getMonth()]}-${DATE_NAMES[instant.getDate()]} ${HOURS_NAMES[instant.getHours()]}:${MINUTE_AND_SECOND_NAMES[instant.getMinutes()]}:${MINUTE_AND_SECOND_NAMES[instant.getSeconds()]}`;
}

export const formatDatetime = (datetime) => {
	if (!(datetime instanceof Date) && !isString(datetime)) {
		return null;
	}

	const instant = datetime instanceof Date ? datetime : new Date(datetime);

	return `${DAY_NAMES[instant.getDay()]} ${DATE_NAMES[instant.getDate()]} ${MONTH_NAMES[instant.getMonth()]} ${instant.getFullYear()} at ${HOURS_NAMES[instant.getHours()]}:${MINUTE_AND_SECOND_NAMES[instant.getMinutes()]}:${MINUTE_AND_SECOND_NAMES[instant.getSeconds()]}`;
}

export const formatDate = (date) => {
	if (!(date instanceof Date) && !isString(date)) {
		return null;
	}

	const instant = date instanceof Date ? date : new Date(date);

	return `${DAY_NAMES[instant.getDay()]} ${DATE_NAMES[instant.getDate()]} ${MONTH_NAMES[instant.getMonth()]} ${instant.getFullYear()}`;
}

export const result = (message) => { return { result: message } };

const VND_FORMATTER = new Intl.NumberFormat('de-DE', { style: 'decimal' });

export const formatVND = (value) => isNaN(value) ? null : `${VND_FORMATTER.format(value)}\u20AB`;

class Optional {
	#optional;

	constructor(optional = null) {
		this.optional = optional;
	}

	else(orElse = null) {
		if (this.optional == null) {
			return orElse;
		}

		return this.optional;
	}
}

export const optional = (opt) => new Optional(opt);

export const atom = (array, identifier = "id") => Object.fromEntries(array.map(ele => [ele[identifier], ele]));

export const mtokeys = (map) => Object.entries(map).map(ele => ele[0]);

export const locateBookmarks = () => {
	const bookmarks = JSON.parse(asIf(localStorage.bookmarks == null).then(() => "{}").else(() => localStorage.bookmarks));

	mergeBookmarks(bookmarks);

	return bookmarks;
};

export const mergeBookmarks = (newBookmarks) => {
	try {
		localStorage.bookmarks = JSON.stringify(asIf(newBookmarks == null).then(() => {}).else(() => newBookmarks));
	} catch(err) {
		console.error(err);
		delete localStorage.bookmarks;
	}
};

const isDate = (val) => Object.prototype.toString.call(val) === '[object Date]';

export const now = () => new Date();

export const datetimeLocalString = (date) => {
	if (isDate(date)) {
		let isoString = date.toISOString();

		return isoString.substring(0, isoString.length - 5);
	}

	if (isString(date)) {
		return date.substring(0, date.length - 5);
	}

	throw new Error();
}

export const isPast = timestamp => {
	const current = now();

	if (isDate(timestamp)) {
		return timestamp < current;
	}

	if (isString(timestamp)) {
		return new Date(timestamp) < current;
	}

	throw new Error("Invalid date");
}

export class ErrorTracker {
	#hasError;

	constructor() {
		this.hasError = false;
	}

	add(next = null) {
		if (this.hasError) {
			return next;
		}

		this.hasError = next != null;
		return next;
	}

	foundError() {
		return this.hasError;
	}
}