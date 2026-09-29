import React, { CSSProperties, useState } from 'react';
import { useForm } from 'antd/lib/form/Form';
import { observer } from 'mobx-react';
import { CARGO_CANCELLATION_CONFIG } from 'shared/constants/constants';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ApprovalStateStatuses } from 'shared/models/types';
import TButton from 'shared/ui/Button/Button';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';
import { StoreNames } from 'stores/StoreNames.enum';
import { CargosTabsFilters } from 'constants/Cargo.constants';
import {
  CancelReasons,
  CargoRequestStatusesEnum,
  journalCancelStatuses
} from 'constants/CargoRequestStatuses.constants';
import {
  APPROVEMENT_CARGOS_JOURNAL,
  APPROVEMENT_REGULAR_CARGOS_JOURNAL,
  CARGOS_JOURNAL, REGULAR_CARGOS_JOURNAL
} from 'constants/constants.routes';

import ApprovalReasonModal from '../../ApprovalPage/ApprovalReasonModal/ApprovalReasonModal';
import { OrderCancelModal } from '../CargoDetailed/OrderCancelModal';
import { performAndRedirect } from '../utils';

const CargoApprovalBlock: React.FC<{
  requestId: string;
  isJournal: boolean;
  isApproval?: boolean;
  isRegular?: boolean;
  activeTab?: string;
  approvalState?: ApprovalStateStatuses;
  request?: CargoRequestModel;
  isMobile?: boolean;
}> = observer(({
  requestId,
  isJournal,
  isApproval,
  isRegular,
  activeTab,
  approvalState,
  request,
  isMobile,
}) => {
  const { [StoreNames.cargosStore]: cargosStore } = useAppStoreContext();
  if (activeTab === CargosTabsFilters.final) {
    return null;
  }
  /* eslint-disable react-hooks/rules-of-hooks */
  const [modal, setModal] = useState(false);
  const [reason, setReason] = useState('');
  const [textAreaVisible, setTextAreaVisible] = useState(false);
  const [form] = useForm();
  const [reasonVisible, setReasonVisible] = useState(false);
  const {
    checkedListApproval,
    checkedItem,
    setCheckedItem,
  } = cargosStore;

  const toggleReasonModal = (): void => setReasonVisible(x => !x);

  const styleJournal = (backgroundColor: string, color: string): CSSProperties => ({
    backgroundColor,
    color,
    borderColor: '#F2F3F6',
  });

  const styleWidget = (backgroundColor: string, color: string): CSSProperties => ({
    backgroundColor,
    color,
    borderColor: '#F2F3F6',
    width: '100%',
    marginBottom: '5px',
  });

  const getDeclineIds = () => checkedListApproval.length
    ? checkedListApproval.map(item => item.id)
    : [checkedItem];

  const approveRequest = () => performAndRedirect(
    () => cargosStore.approveRequest([requestId]),
    APPROVEMENT_CARGOS_JOURNAL
  );

  const approveRegularRequest = () => performAndRedirect(
    () => cargosStore.approveRegularRequest([requestId]),
    APPROVEMENT_REGULAR_CARGOS_JOURNAL
  );

  const declineRequest = () => performAndRedirect(
    () => cargosStore.declineRequest(getDeclineIds()),
    APPROVEMENT_CARGOS_JOURNAL
  );

  const declineRegularRequest = (reason: string) => performAndRedirect(
    () => cargosStore.declineMultipleRegularRequest(getDeclineIds(), reason),
    APPROVEMENT_REGULAR_CARGOS_JOURNAL
  );

  const cancelRequest = () => performAndRedirect(
    () => cargosStore.cancelRequest(
      requestId,
      reason,
      CARGO_CANCELLATION_CONFIG.code,
      CARGO_CANCELLATION_CONFIG.field,
      CARGO_CANCELLATION_CONFIG.value
    ),
    CARGOS_JOURNAL
  );

  const cancelRegularRequest = () => performAndRedirect(
    () => cargosStore.cancelRegularRequest(
      requestId,
      reason,
      CARGO_CANCELLATION_CONFIG.code,
      CARGO_CANCELLATION_CONFIG.field,
      CARGO_CANCELLATION_CONFIG.value
    ),
    REGULAR_CARGOS_JOURNAL
  );

  const cancelOrder = () => {
    ((isRegular && !isApproval) ? cancelRegularRequest : cancelRequest)();
  };

  const handleConfirmModal = async () => {
    setModal(false);
    await cancelOrder();
    setReason('');
    setTextAreaVisible(false);
  };

  const handleCancelModal = () => {
    setReason('');
    setTextAreaVisible(false);
    setModal(false);
    form.setFieldsValue({ reason: '' });
  };

  const handleSelectModal = (value: string, { label }: Record<string, string>) => {
    if (value === CancelReasons.OTHER_REASON) {
      setTextAreaVisible(true);
      setReason(form.getFieldValue('description'));
    } else {
      setReason(label);
      setTextAreaVisible(false);
    }
  };

  const handleChangeReason = (value: React.ChangeEvent<HTMLTextAreaElement>) => {
    setReason(value.currentTarget.value);
  };

  const getButtonText = () => {
    if (isJournal && !isApproval) {
      if (request?.status && journalCancelStatuses.some(status => request.status === status)) {
        return 'Отменить';
      }
    } else if (isApproval || (isRegular && request?.status === CargoRequestStatusesEnum.CARGO_AWAITING_APPROVAL)) {
      return 'Отклонить';
    } else {
      return null;
    }
  };

  const buttonText = getButtonText();

  const handleApprove = (event): void => {
    event.stopPropagation();
    if (isRegular) {
      approveRegularRequest();
    } else {
      approveRequest();
    }
  };

  return (
    <>
      {/* Отклонение заявки на этапе согласования */}
      {buttonText && (
        <TButton
          $size="small"
          $isMobile={isMobile}
          onClick={(event): void => {
            setCheckedItem(requestId);
            event.stopPropagation();
            isJournal && !isApproval && (
              request?.status && journalCancelStatuses.some(status => request.status === status)
            ) ? setModal(true) : toggleReasonModal();
          }}
          style={isJournal
            ? styleJournal('#F2F3F6', '#FF5743')
            : styleWidget('#F2F3F6', '#FF5743')}
        >
          {buttonText}
        </TButton>
      )}
      {isApproval && approvalState !== 'APPROVED' && (
        <TButton
          $size="small"
          $isMobile={isMobile}
          onClick={handleApprove}
          style={isJournal
            ? styleJournal('#10bf6a', '#F2F3F6')
            : styleWidget('#10bf6a', '#F2F3F6')}
        >
          Согласовать
        </TButton>
      )}
      {modal && (
        /// /////// Модальное окно отмены заявки //////////
        <OrderCancelModal
          form={form}
          reason={reason}
          visible={modal}
          textAreaVisible={textAreaVisible}
          onOk={handleConfirmModal}
          onCancel={handleCancelModal}
          onSelect={handleSelectModal}
          onChange={handleChangeReason}
        />
      )}
      <ApprovalReasonModal
        id={requestId}
        visible={reasonVisible}
        onOk={(id, reason): void => {
          if (isRegular && isApproval) {
            if (typeof reason !== 'string') {
              declineRegularRequest(reason.reason);
            }
          } else if (isRegular && !isApproval) {
            if (typeof reason !== 'string') {
              cancelRegularRequest();
            }
          } else {
            declineRequest();
          }
          toggleReasonModal();
        }}
        onCancel={toggleReasonModal}
        isJournal={isJournal}
        isApproval={isApproval}
      />
    </>
  );
});

export default CargoApprovalBlock;
