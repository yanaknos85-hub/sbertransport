import React, { useRef } from 'react';

import { useModalState } from 'shared/hooks/useModal';

import { AbsenceWarningModal } from '../../Components/AbsenceWarningModal/AbsenceWarningModal';
import { PromiseConstructorParams } from './types';

export const useAbsenceWarningModal = () => {
  const [isOpenedAbsenceWarningModal, { show: showAbsenceWarningModal, hide: hideAbsenceWarningModal }] = useModalState(false);
  const resolverRef = useRef<PromiseConstructorParams<unknown> | null>(null);

  const showModal = () => new Promise((resolve, reject) => {
    showAbsenceWarningModal();
    resolverRef.current = { resolve, reject };
  });

  const handleModalSuggestionAccepted = () => {
    resolverRef.current?.resolve();
    resolverRef.current = null;
    hideAbsenceWarningModal();
  };

  const handleModalSuggestionDeclined = () => {
    resolverRef.current?.reject();
    resolverRef.current = null;
    hideAbsenceWarningModal();
  };

  const ModalElement = (
    <AbsenceWarningModal
      open={isOpenedAbsenceWarningModal}
      onAccept={handleModalSuggestionAccepted}
      onDecline={handleModalSuggestionDeclined}
    />
  );

  return { ModalElement, showModal };
};
