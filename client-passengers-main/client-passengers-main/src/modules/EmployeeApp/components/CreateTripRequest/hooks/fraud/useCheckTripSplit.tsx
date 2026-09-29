import React, { useRef } from 'react';
import { FormInstance } from 'antd';
import moment from 'moment';

import { useCheckSplitMutation } from 'api/fraud/check-split';
import { CheckSplitMutationErrorPayload } from 'api/fraud/types';

import { HttpStatus } from 'shared/constants/http';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useModalState } from 'shared/hooks/useModal';

import { StoreNames } from 'stores';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { SYSTEM_MESSAGES } from 'constants/constants.app';

import { isAxiosError } from 'utils';

import { TripSplitWarningModal } from '../../Components/TripSplitWarningModal/TripSplitWarningModal';

import { getTimeZone } from '../../utils/utils';

import { FormValues } from '../../types/types';
import { PromiseConstructorParams } from './types';

const { PERSONAL } = TransportTypeEnum;
const EXPECTED_COST_LIMIT = 10000; // В копейках

export const useCheckTripSplit = (form: FormInstance<FormValues>) => {
  const {
    [StoreNames.geoStore]: geo,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.selfStore]: selfStore,
    [StoreNames.fraudStore]: fraudStore,
  } = useAppStoreContext();

  const [isOpenedSplitWarningModal, { show: showSplitWarningModal, hide: hideSplitWarningModal }]
  = useModalState(false);

  const conflictTripPayload = useRef<CheckSplitMutationErrorPayload | null>(null);
  const resolverRef = useRef<PromiseConstructorParams<unknown> | null>(null);

  const [checkSplitMutation, { isLoading }] = useCheckSplitMutation();

  const handleCheck = async () => {
    fraudStore.personalTripSplit = null;

    const { date: desiredDate = moment() } = form.getFieldsValue();

    const expectedCost = tripStore.classCosts.find(({ transportType }) => transportType.name === PERSONAL)?.cost;
    const expectedDuration = geo.calculatedRoute?.time;

    if (!expectedCost || !expectedDuration || expectedCost > EXPECTED_COST_LIMIT) {
      return;
    }

    try {
      await checkSplitMutation({
        desiredDate: desiredDate.valueOf(),
        employeeId: selfStore.empId,
        timeZone: getTimeZone(),
        expectedCost,
        expectedDuration,
      });
    } catch (error) {
      if (isAxiosError(error)) {
        const status = error.response?.status;

        if (status === HttpStatus.Conflict) {
          const payload = error.response?.data as CheckSplitMutationErrorPayload;
          conflictTripPayload.current = payload;

          try {
            await showModal();
          } catch {
            fraudStore.personalTripSplit = SYSTEM_MESSAGES.tripSplitFraudMessage;
            return;
          }

          throw error;
        }
      }
    }
  };

  const showModal = () => new Promise((resolve, reject) => {
    showSplitWarningModal();
    resolverRef.current = { resolve, reject };
  });

  const handleModalSuggestionAccepted = () => {
    resolverRef.current?.resolve();
    resolverRef.current = null;
    hideSplitWarningModal();
  };

  const handleModalSuggestionDeclined = () => {
    resolverRef.current?.reject();
    resolverRef.current = null;
    hideSplitWarningModal();
  };

  const ModalElement = conflictTripPayload.current ? (
    <TripSplitWarningModal
      open={isOpenedSplitWarningModal}
      onAccept={handleModalSuggestionAccepted}
      onDecline={handleModalSuggestionDeclined}
      humanReadableId={conflictTripPayload.current.humanReadableId}
      id={conflictTripPayload.current.id}
      status={conflictTripPayload.current.status}
    />
  ) : null;

  return {
    isChecking: isLoading,
    ModalElement,
    check: handleCheck,
  };
};
