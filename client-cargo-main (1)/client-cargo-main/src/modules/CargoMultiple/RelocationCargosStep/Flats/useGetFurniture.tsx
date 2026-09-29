import { useEffect } from 'react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { useGetFlatFurniture } from 'api/relocation';
import { CargoListItem } from 'types/Cargo';

import { calculateCargo } from './utils';

export const useGetFurniture = () => {
  const { cargoStore } = useAppStoreContext();
  const getFlat = useGetFlatFurniture(cargoStore.selectedFlat.type);

  useEffect(() => {
    if (
      cargoStore.stepCargosValues.cargoList.length > 0
      && cargoStore.lastLoadedFlatType === cargoStore.selectedFlat.type
    ) {
      return;
    }

    if (getFlat.data && getFlat.data.length > 0) {
      const furnitureList = getFlat.data
        // Для корректного расчёта тарифа, необходимо переводить размеры груза из мм в см
        // Бэк принимает параметры груза в см
        .map(item => calculateCargo(item))
        .map((item, idx) => ({
          id: item.id,
          position: idx,
          cargoName: item.name,
          cargoType: item.type,
          cargoCategory: item.category,
          category: item.category,
          occupiedPlacesCount: item.count,
          length: item.length,
          width: item.width,
          height: item.height,
          weight: item.weight,
          volume: item.volume,
        })) as CargoListItem[];

      cargoStore.setCargoList(furnitureList);
      cargoStore.setLastLoadedFlatType(cargoStore.selectedFlat.type);
    }
  }, [getFlat.data, cargoStore.selectedFlat.type]);
};
