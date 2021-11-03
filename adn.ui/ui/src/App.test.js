// let groupBy = function(xs, key) {
//   return xs.reduce(function(rv, x) {
//     (rv[x[key]] = rv[x[key]] || []).push(x);
//     return rv;
//   }, {});
// };

// const groups = ['color', 'namedSize'];
// const grouped = {};

// list.forEach(function (a) {
//     groups.reduce(function (o, g, i) {                            // take existing object,
//         o[a[g]] = o[a[g]] || (i + 1 === groups.length ? [] : {}); // or generate new obj, or
//         return o[a[g]];                                           // at last, then an array
//     }, grouped).push(a);
// });

let list = [
  {
    "product": {
      "images": [
        "1633366800_GNs2RzYkkX.jpg",
        "1633366800_Kkqv8zCE3q.jpg"
      ],
      "name": "Plain White T-Shirt",
      "id": 1041
    },
    "color": "#49d123",
    "namedSize": "XXL",
    "id": 939
  },
  {
    "product": {
      "images": [
        "1633280400_kOYBDH0jCs.jpg",
        "1633280400_7g1hQBfsiN.jpg"
      ],
      "name": "Nike Air d'Jordan",
      "id": 1035
    },
    "color": "#282b1c",
    "namedSize": "L",
    "id": 948
  },
  {
    "product": {
      "images": [
        "1633280400_kOYBDH0jCs.jpg",
        "1633280400_7g1hQBfsiN.jpg"
      ],
      "name": "Nike Air d'Jordan",
      "id": 1035
    },
    "color": "#282b1c",
    "namedSize": "L",
    "id": 948
  },
  {
    "product": {
      "images": [
        "1633280400_kOYBDH0jCs.jpg",
        "1633280400_7g1hQBfsiN.jpg"
      ],
      "name": "Nike Air d'Jordan",
      "id": 1035
    },
    "color": "#282b1c",
    "namedSize": "L",
    "id": 948
  },
  {
    "product": {
      "images": [
        "1633280400_kOYBDH0jCs.jpg",
        "1633280400_7g1hQBfsiN.jpg"
      ],
      "name": "Nike Air d'Jordan",
      "id": 1035
    },
    "color": "#ffffff",
    "namedSize": "L",
    "id": 958
  },
  {
    "product": {
      "images": [
        "1633280400_kOYBDH0jCs.jpg",
        "1633280400_7g1hQBfsiN.jpg"
      ],
      "name": "Nike Air d'Jordan",
      "id": 1035
    },
    "color": "#ff0000",
    "namedSize": "L",
    "id": 968
  },
  {
    "product": {
      "images": [
        "1633366800_GNs2RzYkkX.jpg",
        "1633366800_Kkqv8zCE3q.jpg"
      ],
      "name": "Plain White T-Shirt",
      "id": 1041
    },
    "color": "#49d123",
    "namedSize": "XXL",
    "id": 939
  },
  {
    "product": {
      "images": [
        "1633366800_GNs2RzYkkX.jpg",
        "1633366800_Kkqv8zCE3q.jpg"
      ],
      "name": "Plain White T-Shirt",
      "id": 1041
    },
    "color": "#49d123",
    "namedSize": "XXL",
    "id": 939
  },
  {
    "product": {
      "images": [
        "1633366800_GNs2RzYkkX.jpg",
        "1633366800_Kkqv8zCE3q.jpg"
      ],
      "name": "Plain White T-Shirt",
      "id": 1041
    },
    "color": "#49d123",
    "namedSize": "XXL",
    "id": 939
  },
  {
    "product": {
      "images": [
        "1633366800_GNs2RzYkkX.jpg",
        "1633366800_Kkqv8zCE3q.jpg"
      ],
      "name": "Plain White T-Shirt",
      "id": 1041
    },
    "color": "#49d123",
    "namedSize": "XXL",
    "id": 939
  },
  {
    "product": {
      "images": [
        "1633366800_GNs2RzYkkX.jpg",
        "1633366800_Kkqv8zCE3q.jpg"
      ],
      "name": "Plain White T-Shirt",
      "id": 1041
    },
    "color": "#49d123",
    "namedSize": "XXL",
    "id": 939
  },
  {
    "product": {
      "images": [
        "1633366800_GNs2RzYkkX.jpg",
        "1633366800_Kkqv8zCE3q.jpg"
      ],
      "name": "Plain White T-Shirt",
      "id": 1041
    },
    "color": "#49d123",
    "namedSize": "XXL",
    "id": 939
  },
  {
    "product": {
      "images": [
        "1633366800_GNs2RzYkkX.jpg",
        "1633366800_Kkqv8zCE3q.jpg"
      ],
      "name": "Plain White T-Shirt",
      "id": 1041
    },
    "color": "#49d123",
    "namedSize": "XXL",
    "id": 939
  },
  {
    "product": {
      "images": [
        "1633366800_GNs2RzYkkX.jpg",
        "1633366800_Kkqv8zCE3q.jpg"
      ],
      "name": "Plain White T-Shirt",
      "id": 1041
    },
    "color": "#49d123",
    "namedSize": "XXL",
    "id": 939
  },
  {
    "product": {
      "images": [
        "1633366800_GNs2RzYkkX.jpg",
        "1633366800_Kkqv8zCE3q.jpg"
      ],
      "name": "Plain White T-Shirt",
      "id": 1041
    },
    "color": "#49d123",
    "namedSize": "XXL",
    "id": 939
  },
  {
    "product": {
      "images": [
        "1633280400_kOYBDH0jCs.jpg",
        "1633280400_7g1hQBfsiN.jpg"
      ],
      "name": "Nike Air d'Jordan",
      "id": 1035
    },
    "color": "#ff0000",
    "namedSize": "L",
    "id": 968
  }
];

const getSpec = (item) => `${item.product.id}-${item.color}-${item.namedSize}`;