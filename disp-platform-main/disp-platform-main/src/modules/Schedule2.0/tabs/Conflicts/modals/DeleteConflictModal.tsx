import React, { FC } from 'react';

import { Modal } from '@sber-sbertransport/ui-kit/src';

import { useDeleteConflictShift } from 'api/shift-conflicts/shift-conflicts.api';

import { ignore } from 'utils/utils';

import { useTranslation } from 'i18n';

import { useModals } from 'modules/Schedule2.0/context/modal.context';
import { useSelectedShift } from 'modules/Schedule2.0/context/selectedShift.context';

import { Button } from 'components/Button';
import Flex from 'components/Flex/Flex';

export const DeleteConflictModal: FC = () => {
  const { t } = useTranslation();

  const { isDeleteConflictOpened, handleClose } = useModals();
  const { conflictShift } = useSelectedShift();

  const [deleteConflict, { isLoading }] = useDeleteConflictShift();

  const handleDelete = () => {
    if (!conflictShift?.routeId) return;

    deleteConflict({ routeId: conflictShift.routeId })
      .then(() => {
        handleClose();
      })
      .catch(ignore);
  };

  return (
    <Modal
      title={t.Shifts.removeShit}
      open={isDeleteConflictOpened}
      onCancel={handleClose}
      footer={() => (
        <Flex justifyContent="flex-end">
          <Button
            key="cancel"
            onClick={handleClose}
            type="text"
          >
            {t.global.cancel}
          </Button>
          <Button
            key="delete"
            type="primary"
            danger
            loading={isLoading}
            onClick={handleDelete}
          >
            {t.global.delete}
          </Button>
        </Flex>
      )}
    />
  );
};
