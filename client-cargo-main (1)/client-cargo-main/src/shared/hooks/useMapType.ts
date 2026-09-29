import { useAppStore } from 'stores';

import { MapComponentType } from 'constants/constants.app';
import { IS_DEV } from 'constants/constants.env';
import { isTestMode } from 'utils/isTestMode';

const MAP_TYPE = 'MAP_TYPE';

/** определяем тип компонента карты.
 * для дзо - 2гис, для банка (альфа, сигма, дельта) - openlayers.
 * в дев/тест режиме - можно переключать принудительно через local storage
 * (ключ MAP_TYPE, значения '2gis'/ 'openlayers')
 */
export const useMapType = (): MapComponentType => {
  const { configStore } = useAppStore();
  const IS_SDO = configStore.env.IS_SDO;

  if (IS_SDO) return MapComponentType.webGL2GIS;

  const IS_TEST_MODE = isTestMode();
  if (IS_DEV || IS_TEST_MODE) {
    const lsMapTypeSettting = localStorage.getItem(MAP_TYPE);
    if (lsMapTypeSettting) {
      switch (lsMapTypeSettting) {
        case '2gis': return MapComponentType.webGL2GIS;
        case 'ol': return MapComponentType.openlayers;
        default: return MapComponentType.openlayers;
      }
    } else {
      localStorage.setItem(MAP_TYPE, 'unset');
      return MapComponentType.openlayers;
    }
  }

  const host = window.location.hostname;

  return (
    host.includes('.sigma.') || host.includes('.delta.') || host.includes('.ca.')
      ? MapComponentType.openlayers
      : MapComponentType.webGL2GIS
  );
};
