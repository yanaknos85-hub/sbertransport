import React, { FC } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { Timeline } from 'antd';
import HistoryTimeline from 'shared/components/Cargo/HistoryTimeline/HistoryTimeline';
import TModal from 'shared/ui/Modal/Modal';

import { useGetHistory } from 'modules/Exchange/api/exchange';

import { ReactComponent as CloseIcon } from '../static/closeIcon.svg';

import styles from './HistoryModal.module.scss';

interface Props {
  visible: boolean;
  onCancel: () => void;
}

export const HistoryModal: FC<Props> = props => {
  const { visible, onCancel } = props;

  const { params: { id } } = useRouteMatch();
  const { data: history } = useGetHistory(id);

  return (
    <TModal
      visible={visible}
      title="Детальный статус заявки"
      onCancel={() => onCancel()}
      closeIcon={<CloseIcon />}
      footer={null}
      centered
      closable
      keyboard
      width={400}
      className={styles.modal}
    >
      <Timeline
        mode="left"
      >
        {history?.map((cargoHistoryType, index) => (
          <HistoryTimeline
            key={cargoHistoryType.status}
            cargoHistoryType={cargoHistoryType}
            isLast={history?.length - 1 === index}
          />
        ))}
      </Timeline>
    </TModal>
  );
};
