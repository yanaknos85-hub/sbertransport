import React, { useCallback } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { useTranslation } from 'i18n';
import { observer } from 'mobx-react';

import { useProfile } from 'api/profile';
import {
  useCreatePosition, useDeletePosition, usePositions, useUpdatePosition
} from 'api/positions';
import { Position } from 'stores/Position/Position.interface';
import { ModelDetailPage } from 'shared/models/ModelDetail/ModelDetailPage';
import { UUID } from 'utils/io-ts';
import { useFields } from './Fields';

const PositionDetailed: React.FC = observer(() => {
  const history = useHistory();
  const { id } = useRouteMatch<{ id: UUID }>().params;
  const adding = id === 'adding';

  const fields = useFields(adding);

  const { t } = useTranslation();

  const [createPosition] = useCreatePosition();
  const [updatePosition] = useUpdatePosition();
  const [deletePosition] = useDeletePosition();

  const { organizationId } = useProfile().data;

  // @ts-ignore
  const { byId: positionsById } = usePositions(organizationId).data;

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const initialValues: Partial<Position> = {
    ...positionsById[id],
    // @ts-ignore
    organizationId,
  };

  const handleSave = useCallback(
    async values => {
      const positionDetails = { ...initialValues, ...values };

      if (adding) {
        const newPosition: Position | undefined = await createPosition(positionDetails);
        if (newPosition) {
          history.push(newPosition.id);
        }
      } else {
        await updatePosition(positionDetails);
      }
    },
    [initialValues, createPosition, updatePosition, history, adding]
  );

  const handleDelete = useCallback(async () => {
    // @ts-ignore
    await deletePosition({ orgId: organizationId, posId: id });
    return false;
  }, [id, organizationId, deletePosition]);

  return (
    <ModelDetailPage
      fields={fields}
      initialValues={initialValues}
      handleSave={handleSave}
      handleDelete={initialValues.active ? handleDelete : undefined}
      isNew={adding}
      pageHeader={t.Positions.backToPositions}
    />
  );
});

export default PositionDetailed;
