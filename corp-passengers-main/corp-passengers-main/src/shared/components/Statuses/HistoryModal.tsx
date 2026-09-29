import React from 'react';
import type { FC, ReactNode } from 'react';
import { Steps } from 'antd';
import type { Status } from 'rc-steps/lib/interface';
import moment from 'moment';
import cn from 'classnames';

import type { UUID } from 'utils/io-ts';
import { DATE_FORMAT } from 'constants/constants.app';
import { Modal } from 'shared/components/Modal/Modal';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';
import Approver from './Approver/Approver';
import { ReactComponent as Now } from 'shared/assets/svg/now.svg';
import { ReactComponent as Canceled } from 'shared/assets/svg/canceled.svg';
import { ReactComponent as Done } from 'shared/assets/svg/done.svg';
import { ReactComponent as WillBe } from 'shared/assets/svg/willBe.svg';
import styles from './styles.module.scss';

interface IHistoryStatus {
  date: number | null;
  status: string;
  statusName: string;
}

export interface IHistory {
  historyStatuses: IHistoryStatus[];
  currentIndex: number;
  canceled: boolean;
  finished: boolean;
}

const { Step } = Steps;

interface Props {
  visible: boolean;
  loading: boolean;
  title: ReactNode;
  history: IHistory;
  cancelDescription: string | undefined;
  approver: {
    id: UUID;
    fullName: string;
  } | null;
  onClose: () => void;
}

const HistoryModal: FC<Props> = ({
  visible,
  loading,
  title,
  history,
  cancelDescription,
  approver,
  onClose,
}) => {
  const {
    historyStatuses,
    currentIndex,
    canceled,
    finished,
  } = history;

  const getStepIcon = (targetIndex: number): ReactNode => {
    if ((finished || canceled) && currentIndex > targetIndex) {
      return null;
    } else if (canceled) {
      return <Canceled />;
    } else if (finished || currentIndex > targetIndex) {
      return <Done />;
    } else if (currentIndex < targetIndex) {
      return <WillBe />;
    } else if (currentIndex === targetIndex) {
      return <Now />;
    }
  };

  const getStepStatus = (targetIndex: number): Status => {
    if (canceled) {
      return 'error';
    } else if (currentIndex === targetIndex) {
      return 'process';
    } else if (finished || currentIndex > targetIndex) {
      return 'finish';
    } else {
      return 'wait';
    }
  };

  const createDescription = (targetIndex: number) => {
    if (canceled && cancelDescription && currentIndex === targetIndex) {
      return (
        <span className={styles.cancelDescription}>{cancelDescription}</span>
      );
    } else if (approver && ((!currentIndex && !targetIndex)
    || (!canceled && currentIndex && targetIndex === 1))) {
      return (
        <Approver
          id={approver.id}
          fullName={approver.fullName}
          hasDone={!!currentIndex}
        />
      );
    }
  };

  return (
    <Modal
      className={styles.modal}
      visible={visible}
      title={title}
      width={716}
      footer={null}
      onCancel={onClose}
    >
      {loading ? (
        <SpinWrapped />
      ) : (
        <Steps
          className={styles.steps}
          direction="vertical"
        >
          {historyStatuses.map(({
            date,
            status,
            statusName,
          }, targetIndex) => (
            <Step
              key={status}
              className={cn(styles.steps__entry, {
                [styles.steps__entry_toProcess]: !canceled && !finished && (targetIndex + 1) === currentIndex,
                [styles.steps__entry_start]: !currentIndex && !targetIndex,
                [styles.steps__entry_beforeFinished]: finished && targetIndex < currentIndex,
                [styles.steps__entry_beforeCanceled]: canceled && targetIndex < currentIndex,
              })}
              title={(
                <div className={styles.stepTitle}>
                  <span className={cn(
                    styles.stepTitle__status,
                    currentIndex === targetIndex && styles.stepTitle__status_current
                  )}
                  >
                    {statusName}
                  </span>

                  {!!date && (
                  <span className={styles.stepTitle__date}>
                    {moment(date).format(DATE_FORMAT.DATE_WITH_TIME_DOTS)}
                  </span>
                  )}
                </div>
              )}
              description={createDescription(targetIndex)}
              status={getStepStatus(targetIndex)}
              icon={getStepIcon(targetIndex)}
            />
          ))}
        </Steps>
      )}
    </Modal>
  );
};

export default HistoryModal;
