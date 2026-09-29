import React, { FC, useEffect, useState } from 'react';
import { Checkbox } from 'antd';
import { StoreNames } from 'ioc/ioc.storeNames';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { CargosTabsFilters } from 'constants/Cargo.constants';
import {
  APPROVEMENT_CARGOS_JOURNAL,
  APPROVEMENT_REGULAR_CARGOS_JOURNAL
} from 'constants/constants.routes';

import { performAndRedirect } from '../../utils';
import DeclineReasonModal from '../DeclineReasonModal/DeclineReasonModal';
import * as S from './SelectApprovalControls.styled';

interface SelectApprovalControlsProps {
  isRegular?: boolean;
  isApproval?: boolean;
  activeTab: string;
  requestIds: string[];
}

export const SelectApprovalControls: FC<SelectApprovalControlsProps> = observer(props => {
  const {
    isRegular,
    isApproval,
    activeTab,
    requestIds,
  } = props;

  const {
    [StoreNames.cargosStore]: cargosStore,
  } = useAppStoreContext();

  const {
    checkedListApproval,
    setCheckedListApproval,
    isCheckAll,
    setIsCheckAll,
    isIndeterminate,
    setIsIndeterminate,
    checkedItem,
  } = cargosStore;
  const [reasonVisible, setReasonVisible] = useState(false);

  const cargosData = cargosStore['cargoMultipleApprovalList'];

  useEffect(() => {
    setIsCheckAll(cargosData.length === checkedListApproval?.length);
    setIsIndeterminate(checkedListApproval.length > 0 && checkedListApproval.length < cargosData.length);
  }, [cargosData.length, checkedListApproval.length]);

  const toggleReasonModal = (): void => setReasonVisible(!reasonVisible);

  const fillRoutesList = e => {
    setCheckedListApproval(e.target.checked ? cargosStore['cargoMultipleApprovalList']?.map(item => item) : []);
  };

  const clearRoutesList = () => {
    setCheckedListApproval([]);
  };

  const getApprovalIds = () => checkedListApproval.length
    ? checkedListApproval.map(item => item.id)
    : [checkedItem];

  const approveRequest = () => performAndRedirect(
    () => cargosStore.approveRequest(getApprovalIds()),
    APPROVEMENT_CARGOS_JOURNAL
  );

  const approveRegularRequest = () => performAndRedirect(
    () => cargosStore.approveRegularRequest(getApprovalIds()),
    APPROVEMENT_REGULAR_CARGOS_JOURNAL
  );

  const declineRequest = () => performAndRedirect(
    () => cargosStore.declineRequest(getApprovalIds()),
    APPROVEMENT_CARGOS_JOURNAL
  );

  const declineRegularRequest = (reason: string) => performAndRedirect(
    () => cargosStore.declineMultipleRegularRequest(getApprovalIds(), reason),
    APPROVEMENT_REGULAR_CARGOS_JOURNAL
  );

  const handleApprove = (event: React.MouseEvent): void => {
    event.stopPropagation();
    (isRegular ? approveRegularRequest : approveRequest)();
  };

  if ((isApproval && activeTab !== CargosTabsFilters.active) || cargosData.length === 0) {
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
          />
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
        onOk={(id, reason): void => {
          if (isRegular && isApproval) {
            if (typeof reason !== 'string') {
              declineRegularRequest(reason.reason);
            }
          } else {
            declineRequest();
          }
          toggleReasonModal();
        }}
        onCancel={toggleReasonModal}
        isApproval={isApproval}
      />
    </S.Wrapper>
  );
});
