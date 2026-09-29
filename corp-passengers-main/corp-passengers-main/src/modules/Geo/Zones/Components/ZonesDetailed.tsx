import React, { useCallback } from 'react';
import { useRouteMatch, useHistory } from 'react-router-dom';
import {
  useCreateZone, useUpdateZone, useDeleteZone, useZones
} from 'api/geo/zones';
import { UUID } from 'utils/io-ts';
import { ModelDetailPage } from 'shared/models/ModelDetail/ModelDetailPage';
import { useTranslation } from 'i18n';
import { GeoZones } from 'stores/GeoZones/GeoZones.interface';
import { useFields } from './Fields';

const GeoZonesDetailed: React.FC<JSX.Element> = (): JSX.Element => {
  const history = useHistory();
  const { id } = useRouteMatch<{ id: UUID }>().params;
  const adding = id === 'adding';

  const fields = useFields(adding);
  const { t } = useTranslation();
  const [createZone] = useCreateZone();
  const [updateZone] = useUpdateZone();
  const [deleteZone] = useDeleteZone();

  const { byId: zones } = useZones().data;
  const zone = zones[id] as GeoZones;

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const initialValues = {
    ...zone,
    name: zone?.name ?? '',
    code: zone?.code ?? '',
  };

  const handleSave = useCallback(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    async (values: any) => {
      const zoneDetails = { ...initialValues, ...values };
      if (adding) {
        const newZone = await createZone(zoneDetails);
        if (newZone) {
          history.push(newZone.id);
        }
      } else {
        await updateZone(zoneDetails);
      }
    },
    [initialValues, createZone, updateZone, history, adding]
  );

  const handleDelete = useCallback(async () => {
    await deleteZone(id);
    history.goBack();
    return false;
  }, [deleteZone, id, history]);

  return (
    <ModelDetailPage
      fields={fields}
      initialValues={initialValues}
      handleSave={handleSave}
      handleDelete={handleDelete}
      isNew={adding}
      pageHeader={t.contractors.backToContractors}
    />
  );
};

export default GeoZonesDetailed;
