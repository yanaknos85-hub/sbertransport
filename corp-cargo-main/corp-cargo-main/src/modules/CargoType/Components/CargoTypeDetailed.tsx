import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useHistory } from '@sber-sbertransport/mf-core';
import { useTranslation } from 'i18n';
import { observer } from 'mobx-react';

import {
  useCreateCargoType,
  useUpdateCargoType,
  useDeleteCargoType,
  useCargoTypes,
} from 'api/cargo-types';
import { ModelDetailPage } from 'shared/models/ModelDetail/ModelDetailPage';
import { useFields } from './Fields';
import { useProfile } from '../../../api/profile';
import { CargoCategory } from 'stores/CargoType/CargoType.interface';
import type { CargoType } from 'stores/CargoType/CargoType.interface';
import { AccessControl } from './types/types';

interface Props { cargoTypeId: string; hide: () => void }

export const CargoTypeDetailed: React.FC<Props> = observer(({ cargoTypeId, hide }) => {
  const history = useHistory();
  const adding = cargoTypeId === 'adding';

  const { organizationId } = useProfile().data;
  const { t } = useTranslation();

  const [createCargoType] = useCreateCargoType(organizationId);
  const [updateCargoType] = useUpdateCargoType(organizationId);
  const [deleteCargoType] = useDeleteCargoType();

  const { data } = useCargoTypes(organizationId);
  const { byId: cargoTypesById } = data;

  const [cargoId, setCargoId] = useState<string>(cargoTypeId);

  const initialDataRef = useRef<Partial<CargoType> | null>(null);

  const [currentAccessLevel, setCurrentAccessLevel] = useState<AccessControl | undefined>(() => {
    if (adding) return undefined;
    return cargoTypesById[cargoId]?.accessLevel as AccessControl || undefined;
  });

  useEffect(() => {
    if (!adding && cargoTypesById[cargoId]) {
      initialDataRef.current = { ...cargoTypesById[cargoId] };
    } else if (adding) {
      initialDataRef.current = null;
    }
  }, [adding, cargoTypesById, cargoId]);

  const isOversized = (category?: string) => (
    category === CargoCategory.BULK || category === CargoCategory.LIQUID
  );

  const getVolume = (value = 0) => {
    const MIN_VALUE = 0.001;
    const resValue = value / (1000 * 1000 * 1000);
    const val = resValue < MIN_VALUE ? MIN_VALUE : resValue.toFixed(3);
    return `${val}`;
  };

  const initialValues = useMemo((): Partial<CargoType> => {
    if (adding) {
      return {}
    }

    const data = cargoTypesById[cargoId];
    if (!data) return {}

    return {
      ...data,
      volume: Number(getVolume(data.volume as number)),
    };
  }, [adding, cargoTypesById[cargoId], cargoId]);

  const handleAccessLevelChange = useCallback((level: AccessControl) => {
    setCurrentAccessLevel(level);
  }, []);

  const fields = useFields(initialValues.category, currentAccessLevel, adding, handleAccessLevelChange);

  const handleSave = useCallback(
    async values => {
      if (values.accessLevel !== 'ORGANIZATION') {
        values.organizationId = undefined;
      }
      // Нужно для того, чтобы с фронта уходили корректные данные объёма в мм3
      const isOversizedCategory = isOversized(values.category);
      const volume = isOversizedCategory ? values.volume * 1_000_000_000 : values.volume;
      const width = isOversizedCategory ? 0 : values.width;
      const height = isOversizedCategory ? 0 : values.height;
      const length = isOversizedCategory ? 0 : values.length;
      const weight = isOversizedCategory ? 0 : values.weight;

      const cargoTypeDetails = {
        ...(initialDataRef.current || {}),
        ...values,
        width,
        height,
        length,
        weight,
        volume,
        accessLevel: currentAccessLevel,
      } as CargoType;

      if (adding) {
        await createCargoType(cargoTypeDetails).then(() => hide());
      } else {
        await updateCargoType(cargoTypeDetails).then((data) => {
          if (data) {
            setCargoId(data.id);
            hide();
          }
        });
      }
    },
    [adding, createCargoType, updateCargoType, hide, currentAccessLevel, isOversized]
  );

  const handleDelete = useCallback(async () => {
    await deleteCargoType({ cargoTypeId, organizationId });
    return false;
  }, [cargoTypeId, deleteCargoType, organizationId]);

  return (
    <ModelDetailPage
      fields={fields}
      initialValues={initialValues}
      handleSave={handleSave}
      handleDelete={handleDelete}
      isNew={adding}
      pageHeader={t.Positions.backToPositions}
    />
  );
});

export default CargoTypeDetailed;
