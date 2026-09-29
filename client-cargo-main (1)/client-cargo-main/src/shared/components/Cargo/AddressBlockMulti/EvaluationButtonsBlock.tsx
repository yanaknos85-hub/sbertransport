import React, { FC } from 'react';
import TButton from 'shared/ui/Button/Button';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';
import { isFinishedShipment, renderButtonsBlock } from 'modules/Evaluation/utils';

interface Props {
  request: CargoRequestModel;
  handleFeedbackVisible?: () => void;
  isMobile?: boolean;
  isApproval?: boolean;
}

export const EvaluationButtonsBlock: FC<Props> = props => {
  const {
    request, handleFeedbackVisible, isMobile, isApproval,
  } = props;
  if (request.status !== 'CARGO_SHIPMENT_FINISHED' || isApproval) return null;

  return (
    renderButtonsBlock(request.status) && (
      request.status === 'CARGO_SHIPMENT_FINISHED') ? (
        <TButton
          $statusEvaluation={!isFinishedShipment(request.status)}
          $size="small"
          $isMobile={isMobile}
          onClick={handleFeedbackVisible}
          style={{
            marginRight: '10px',
          }}
        >
          Оценить
        </TButton>
      ) : null
  );
};
