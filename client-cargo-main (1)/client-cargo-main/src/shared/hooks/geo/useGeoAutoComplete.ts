import { useEffect, useState } from 'react';
import { useDebounceFn } from 'ahooks';
import { mapKeys } from 'lodash';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { plainToNew } from 'utils';

import { useGetCoordinateByAddress } from 'api/geo';
import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';

const addressExistInList = ({ list, value }: { list: WaypointModel[]; value: string }): boolean => list.some((x: WaypointModel) => x.addressString === value);

const minStringLength = 3;

const useGeoAutoComplete = (): {
  autoCompleteList: WaypointModel[];
  clearList: () => void;
  editSingleAddress: (value: string) => void;
} => {
  const [autoCompleteList, _setAutoCompleteList] = useState<WaypointModel[]>([]);
  const [location, setLocation] = useState<string | undefined>(undefined);

  const { data } = useGetCoordinateByAddress(location, {
    enabled: location !== undefined,
    ...CLEAR_QUERY_CONFIG,
  });

  useEffect(() => {
    const models = plainToNew<WaypointModel[]>(WaypointModel, data) ?? [];
    // Исключим адреса только с городом, без номера дома, без улицы
    const noBlank
      = models?.filter(
        x => x.street !== undefined && x.house !== undefined && x.addressString !== '' && x.addressString !== x.city
      ) || [];
    _setAutoCompleteList(
      // Удалим одинаковые адреса
      Object.values(mapKeys(noBlank, 'addressString'))
    );
  }, [data]);

  const clearList = (): void => {
    _setAutoCompleteList([]);
  };

  const searchLocation = (value: string): void => {
    if (value.length > minStringLength && !addressExistInList({ list: autoCompleteList, value })) {
      setLocation(value);
      clearList();
    }
  };

  const { run: editSingleAddress } = useDebounceFn(searchLocation, { wait: 1000 });

  return {
    autoCompleteList,
    clearList,
    editSingleAddress,
  };
};

export default useGeoAutoComplete;
