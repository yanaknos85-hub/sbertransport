enum PointType {
  LOAD = 'LOAD',
  UNLOAD = 'UNLOAD',
}

const Point = {
  LOAD: 'Сбор',
  UNLOAD: 'Доставка',
};

export const getTypePointLabel = (type: PointType) => {
  switch (type) {
    case PointType.LOAD:
      return Point[type];
    case PointType.UNLOAD:
      return Point[type];
    default:
      return '';
  }
};
