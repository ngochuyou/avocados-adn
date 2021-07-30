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