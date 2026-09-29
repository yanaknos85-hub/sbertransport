import React, { useCallback } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import { useTranslation } from 'i18n';
import { observer } from 'mobx-react';
import { ModelDetailPage } from 'shared/models/ModelDetail/ModelDetailPage';
import { UUID } from 'utils/io-ts';
import {
  useCargoAuto, useCreateCargoAuto, useDeleteCargoAuto, useUpdateCargoAuto
} from 'api/cargo-auto';
import { CargoAuto } from 'stores/CargoAuto/CargoAuto.interface';
import { useFields } from './Fields';
import * as routes from 'constants/constants.routes';

interface CargoAutoDetailedParams { id: UUID }

const CargoAutoDetailed: React.FC = observer(() => {
  const history = useHistory();
  const { id } = useRouteMatch<CargoAutoDetailedParams>().params;
  const adding = id === 'adding';
  const fields = useFields();
  const { t } = useTranslation();

  const [createCargoAuto] = useCreateCargoAuto();
  const [updateCargoAuto] = useUpdateCargoAuto();
  const [deleteCargoAuto] = useDeleteCargoAuto();

  const { byId: cargoAutoById } = useCargoAuto().data;

  // eslint-disable-next-line react-hooks/exhaustive-deps, @typescript-eslint/no-explicit-any
  const initialValues: any = {
    ...cargoAutoById[id],
    capacity: JSON.stringify(cargoAutoById[id]?.capacity || undefined),
  };

  const handleSave = useCallback(
    async values => {
      const cargoAutoDetails = {
        ...initialValues, ...values, capacity: JSON.parse(values.capacity),
      };

      if (adding) {
        const newCargoAuto: CargoAuto | undefined = await createCargoAuto(cargoAutoDetails);
        if (newCargoAuto) {
          history.push(`${routes.AUTO_DIRECTORY}`);
        }
      } else {
        await updateCargoAuto(cargoAutoDetails);
      }
    },
    [initialValues, createCargoAuto, updateCargoAuto, history, adding]
  );

  const handleDelete = useCallback(async () => {
    await deleteCargoAuto({ autoId: id });
    return false;
  }, [id, deleteCargoAuto]);

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

export default CargoAutoDetailed;
