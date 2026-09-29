module.exports = new Proxy(
  {},
  {
    get: function (target, property) {
      if (property === '__esModule') {
        return true;
      }
      // Возвращаем имя свойства как строку для удобства тестирования
      return property.toString();
    },
  }
);
