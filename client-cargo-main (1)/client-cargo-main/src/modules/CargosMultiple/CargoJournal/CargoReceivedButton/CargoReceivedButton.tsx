import React, { FC } from 'react';
import { useModalState } from 'shared/hooks/useModal';
import TButton from 'shared/ui/Button/Button';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';

import { OrderReceiveModal } from './OrderReceiveModal';

interface Props {
  request: CargoRequestModel;
  onSuccess?: () => void;
}

export const CargoReceivedButton: FC<Props> = ({ request, onSuccess }) => {
  const [modal, modalActions] = useModalState();

  const handleOpenModal = () => {
    modalActions.show();
  };

  const handleCancelModal = () => {
    modalActions.hide();
  };

  const modalRequest = {
    requestId: request.id,
    data: [
      request.humanReadableId,
      request.waypoints[1]?.addressStringRepresentation || '',
      request.desiredDate,
    ],
  };

  return (
    <>
      <TButton
        $size="small"
        onClick={handleOpenModal}
      >
        Получить груз
      </TButton>
      <OrderReceiveModal
        visible={modal}
        request={modalRequest}
        onCancel={handleCancelModal}
        onSuccess={onSuccess}
      />
    </>
  );
};
