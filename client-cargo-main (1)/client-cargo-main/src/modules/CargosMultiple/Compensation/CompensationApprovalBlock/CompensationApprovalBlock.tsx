import React, { CSSProperties, useState } from 'react';
import { observer } from 'mobx-react';
import DeclineReasonModal from 'shared/components/DeclineReasonModal/DeclineReasonModal';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ApprovalStateStatuses } from 'shared/models/types';
import TButton from 'shared/ui/Button/Button';

import { CompensationRequestModel } from 'stores/Compensations/models/CargoRequest.model';
import { StoreNames } from 'stores/StoreNames.enum';
import { CargosTabsFilters } from 'constants/Cargo.constants';
import { APPROVEMENT_COMPENSATION_JOURNAL } from 'constants/constants.routes';

import { performAndRedirect } from '../../utils';

const CompensationApprovalBlock: React.FC<{
  requestId: string;
  activeTab?: string;
  approvalState?: ApprovalStateStatuses;
  request?: CompensationRequestModel;
  isMobile?: boolean;
}> = observer(({
  requestId,
  activeTab,
  approvalState,
  isMobile,
}) => {
  const {
    [StoreNames.compensationStore]: compensationStore,
  } = useAppStoreContext();

  if (activeTab === CargosTabsFilters.final) {
    return null;
  }

  const [reasonVisible, setReasonVisible] = useState(false);

  const {
    checkedListApproval,
    checkedItem,
    setCheckedItem,
  } = compensationStore;

  const toggleReasonModal = (): void => setReasonVisible(x => !x);

  const styleJournal = (backgroundColor: string, color: string): CSSProperties => ({
    backgroundColor,
    color,
    borderColor: '#F2F3F6',
  });

  const getSelectedIds = (): string[] => checkedListApproval.length ? checkedListApproval.map(item => item.id) : [checkedItem];

  const approveRequest = () => performAndRedirect(
    () => compensationStore.approveRequest({ requestIds: [requestId] }),
    APPROVEMENT_COMPENSATION_JOURNAL
  );

  const declineRequest = reason => performAndRedirect(
    () => compensationStore.declineRequest({ requestIds: getSelectedIds(), reason }),
    APPROVEMENT_COMPENSATION_JOURNAL
  );

  const handleApprove = (event): void => {
    event.stopPropagation();
    approveRequest();
  };

  return (
    <>
      <TButton
        $size="small"
        $isMobile={isMobile}
        onClick={(event): void => {
          setCheckedItem(requestId);
          event.stopPropagation();
          toggleReasonModal();
        }}
        style={styleJournal('#F2F3F6', '#FF5743')}
      >
        Отклонить
      </TButton>
      {approvalState !== 'APPROVED' && (
        <TButton
          $size="small"
          $isMobile={isMobile}
          onClick={handleApprove}
          style={styleJournal('#10bf6a', '#F2F3F6')}
        >
          Согласовать
        </TButton>
      )}
      <DeclineReasonModal
        requestIds={requestId}
        visible={reasonVisible}
        onOk={(id, reason): void => {
          declineRequest(reason);
          toggleReasonModal();
        }}
        onCancel={toggleReasonModal}
        isJournal
        isApproval
      />
    </>
  );
});

export default CompensationApprovalBlock;
