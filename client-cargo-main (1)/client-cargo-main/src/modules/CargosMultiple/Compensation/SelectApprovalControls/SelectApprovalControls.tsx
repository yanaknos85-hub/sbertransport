import React, { FC, useEffect, useState } from 'react';
import { Checkbox } from 'antd';
import { StoreNames } from 'ioc/ioc.storeNames';
import { observer } from 'mobx-react';
import DeclineReasonModal from 'shared/components/DeclineReasonModal/DeclineReasonModal';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { CargosTabsFilters } from 'constants/Cargo.constants';
import { APPROVEMENT_COMPENSATION_JOURNAL } from 'constants/constants.routes';

import { performAndRedirect } from '../../utils';
import * as S from './SelectApprovalControls.styled';

interface SelectApprovalControlsProps {
  requestIds: string[];
  isRegular?: boolean;
  isApproval?: boolean;
  activeTab: string;
}

export const SelectApprovalControls: FC<SelectApprovalControlsProps> = observer(props => {
  const {
    requestIds,
    isApproval,
    activeTab,
  } = props;

  const {
    [StoreNames.compensationStore]: compensationStore,
  } = useAppStoreContext();

  const {
    checkedListApproval,
    setCheckedListApproval,
    isCheckAll,
    setIsCheckAll,
    isIndeterminate,
    setIsIndeterminate,
    checkedItem,
  } = compensationStore;
  const [reasonVisible, setReasonVisible] = useState(false);

  const cargosData = compensationStore['compensationList'].content;

  useEffect(() => {
    setIsCheckAll(cargosData.length === checkedListApproval?.length);
    setIsIndeterminate(checkedListApproval.length > 0 && checkedListApproval.length < cargosData.length);
  }, [cargosData.length, checkedListApproval.length]);

  const toggleReasonModal = (): void => setReasonVisible(!reasonVisible);

  const fillRoutesList = e => {
    setCheckedListApproval(e.target.checked ? compensationStore['compensationList'].content?.map(item => item) : []);
  };

  const clearRoutesList = () => {
    setCheckedListApproval([]);
  };

  const getSelectedIds = (): string[] => checkedListApproval.length ? checkedListApproval.map(item => item.id) : [checkedItem];

  const approveRequest = () => performAndRedirect(
    () => compensationStore.approveRequest({ requestIds: checkedListApproval?.map(item => item.id) }),
    APPROVEMENT_COMPENSATION_JOURNAL
  );

  const declineRequest = (reason: string) => performAndRedirect(
    () => compensationStore.declineRequest({ requestIds: getSelectedIds(), reason }),
    APPROVEMENT_COMPENSATION_JOURNAL
  );

  const handleApprove = (event): void => {
    event.stopPropagation();
    approveRequest();
  };

  if ((activeTab !== CargosTabsFilters.active)) {
    return null;
  }

  return (
    <S.Wrapper $isChecked={checkedListApproval.length}>
      {!!checkedListApproval.length && (
        <S.ButtonsContainer>
          <S.Button
            $size="small"
            onClick={handleApprove}
          >
            Согласовать
          </S.Button>
          <S.DeclineButton
            $size="small"
            onClick={toggleReasonModal}
          >
            Отклонить
          </S.DeclineButton>
        </S.ButtonsContainer>
      )}
      <S.Container>
        <span>Выбрать все</span>
        {isCheckAll ? (
          <Checkbox
            style={{ marginLeft: '8px' }}
            onChange={clearRoutesList}
            indeterminate={isIndeterminate}
            checked={isCheckAll}
          >
          </Checkbox>
        ) : (
          <Checkbox
            style={{ marginLeft: '8px' }}
            onChange={fillRoutesList}
            indeterminate={isIndeterminate}
            checked={isCheckAll}
          />
        )}
      </S.Container>
      <DeclineReasonModal
        requestIds={requestIds}
        visible={reasonVisible}
        onOk={(_, reason): void => {
          declineRequest(reason as string);
          toggleReasonModal();
        }}
        onCancel={toggleReasonModal}
        isApproval={isApproval}
      />
    </S.Wrapper>
  );
});
