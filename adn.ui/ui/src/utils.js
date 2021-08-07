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

export const toMap = (array = [], map = {}, idName) => {
	array.forEach(ele => map[ele[idName]] = ele);

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