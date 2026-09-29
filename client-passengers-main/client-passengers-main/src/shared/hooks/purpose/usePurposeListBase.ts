import { useEffect, useState } from 'react';

import { TripPurpose } from 'stores/Trip/Trip.interface';

export interface PurposeListBase {
  purposes: TripPurpose[];
  getPurposeFromList: (purposeId: string | undefined) => TripPurpose | undefined;
}

/**
 * Хук для получения списка целей
 */
const usePurposeListBase = (
  defaultPurposes: TripPurpose[]
): PurposeListBase => {
  const [purposes, _setPurposes] = useState<TripPurpose[]>([]);

  const getPurposeFromList = (purposeId: string | undefined): TripPurpose | undefined => (defaultPurposes || []).find(({ id }) => purposeId === id);

  useEffect(() => {
    _setPurposes(defaultPurposes || []);
  }, [defaultPurposes]);

  return {
    purposes, getPurposeFromList,
  };
};

export default usePurposeListBase;
