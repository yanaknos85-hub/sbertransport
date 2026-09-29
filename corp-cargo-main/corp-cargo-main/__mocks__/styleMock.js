module.exports = new Proxy(
  {},
  {
    get: function (target, property) {
      if (property === '__esModule') {
        return true;
      }
      return property.toString();
    },
  }
);