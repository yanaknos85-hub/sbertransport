import React from 'react';
import './overwrite.scss';
import { Modal } from 'antd';

import Close from 'shared/components/Images/Close.svg';

import { TripRequestModel } from 'stores/Trip/models';
import { Delegate } from 'stores/Delegates/Delegates.interface';
import { IDepartmentHead } from 'stores/Corporate/Corporate.interface';

import StepperDetailed from '../StepperDetailed/StepperDetailed';

interface ModalDetailedStatusesProps {
  visible: boolean;
  onCancel: () => void;
  request: TripRequestModel;
  delegates: Delegate[];
  supervisor?: IDepartmentHead;
  isNotSharedOwner?: boolean;
}

const ModalDetailedStatuses: React.FC<ModalDetailedStatusesProps> = ({
  visible,
  onCancel,
  request,
  delegates,
  supervisor,
  isNotSharedOwner,
}) => (
  <Modal
    open={visible}
    onCancel={onCancel}
    className="ModalDetailedStatuses"
    footer
  >
    <div className="cardWrapper">
      <div className="header">
        <div className="numberApplication">
          Детальный статус заявки
        </div>
        <div className="cancel" onClick={onCancel}>
          <img alt="Close" src={Close} />
        </div>
      </div>
      <StepperDetailed
        isNotSharedOwner={isNotSharedOwner}
        delegates={delegates}
        supervisor={supervisor}
        request={request}
        currentType={request.status}
        type={request.transportType}
      />
    </div>
  </Modal>
);

export default ModalDetailedStatuses;
