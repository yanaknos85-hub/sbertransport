import React, { FC } from 'react';
import { RLayerTile } from 'rlayers';

import { IS_DEV } from 'constants/constants.env';
import { DIRECT_TILES_LINK, MAP_TILES } from 'constants/constants.geo';

const ATTRIBUTION = '<a href="https://dev.2gis.ru/" target="_blank">2GIS</a>'; // копирайт элемент
const TILES_LINK = IS_DEV ? DIRECT_TILES_LINK : MAP_TILES; // на local: не прокидывается прокси ссылка на тайлы, ходим напрямую

// const maxExtent = new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34);
// maxExtent вроде нужно для нормирования гео расчетов в проекции,
// но и так работает. оставил тут, чтобы потом не искать эти значения
const CROSS_ORIGIN = 'anonymous';
const PROJECTION = 'EPSG:900913';

/** слой тайлов от 2гис */
export const TileSource2GIS: FC = () => {
  return (
    <RLayerTile
      url={TILES_LINK}
      projection={PROJECTION}
      crossOrigin={CROSS_ORIGIN}
      attributions={ATTRIBUTION}
    />
  );
};
