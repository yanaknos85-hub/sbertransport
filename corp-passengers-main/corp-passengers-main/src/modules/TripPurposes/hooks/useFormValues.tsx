import { useProfile } from 'api/profile';
import { useActiveTripPurposes } from 'api/purposes';
import { TripPurpose } from 'stores/TripPurposes/TripPurpose.interface';
import { Dispatch, SetStateAction, useState } from 'react';
import { UUID } from 'utils/io-ts';
import { Condition } from '../types/types';
import { getInitialConditionArray, parseInitialConditions } from '../utils/utils';

interface Export {
  initialConditions: Condition[];
  initialName?: string;
  purposeType?: string;
  purposeId?: UUID;
  conditions: Condition[];
  setConditions: Dispatch<SetStateAction<Condition[]>>;
  organizationId: UUID;
}

export const useFormValues = (purposeId?: string): Export => {
  const { organizationId } = useProfile().data;
  // @ts-ignore
  const { byId: purposes } = useActiveTripPurposes(organizationId).data;

  // Получение данных по выбранной цели поездки
  let initialValues;
  let initialConditions: Condition[] = getInitialConditionArray();
  if (purposeId && purposeId !== 'adding') {
    const currentPurpose: TripPurpose = purposes[purposeId] || {};
    initialValues = { ...currentPurpose };
    // Пересобираем initialValues в объект, пригодный для итерации (редактирование)
    initialConditions = parseInitialConditions(initialValues);
  }
  const [conditions, setConditions] = useState(initialConditions);

  return {
    initialConditions,
    initialName: initialValues?.label,
    purposeId: initialValues?.id,
    purposeType: initialValues?.purposeType,
    conditions,
    setConditions,
    // @ts-ignore
    organizationId,
  };
};
