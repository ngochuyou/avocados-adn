export const intRange = (max = 0) => [...Array(max)].map((ele, index) => index);

export const spread = (max = 0, value = {}) => [...Array(max)].map(ele => value);