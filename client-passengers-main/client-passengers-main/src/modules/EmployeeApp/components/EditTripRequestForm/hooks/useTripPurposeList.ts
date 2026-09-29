
import { useEffect, useState } from 'react';

import useEmployeePurposeList from 'shared/hooks/purpose/useEmployeePurposeList';
import { TripRequestModel } from 'stores/Trip/models';
import { TripPurpose } from 'stores/Trip/Trip.interface';

export interface TripPurposeList {
  purposes: TripPurpose[];
  getById: (purposeId: string | undefined) => TripPurpose | undefined;
}

export const useTripPurposeList = (request?: TripRequestModel | null): TripPurposeList => {
  const [_purposes, _setPurposes] = useState<TripPurpose[]>([]);
  const purposeList = useEmployeePurposeList();
  const {
    getPurposeFromList, purposes,
  } = purposeList;

  const reqPurpose = request?.purpose;

  useEffect(() => {
    const purposeFromList = getPurposeFromList(reqPurpose?.id);
    const purposeExistInList = purposeFromList !== undefined;

    _setPurposes([
      ...purposes,
      ...(!purposeExistInList && reqPurpose !== undefined
        ? [{ ...reqPurpose, label: `${reqPurpose.label} (Не активна)` }]
        : []),
    ]);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [purposeList.purposes, reqPurpose]);
  // FIXME react-hooks/exhaustive-deps

  const getById = (purposeId: string | undefined): TripPurpose | undefined => _purposes.find(({ id }) => purposeId === id);

  return {
    purposes: _purposes, getById,
  };
};
