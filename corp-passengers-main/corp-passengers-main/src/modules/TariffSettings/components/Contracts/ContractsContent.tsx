import React, { FC } from 'react';
import ContractsTab from './components/ContractsTab';
import { ModalProvider } from './context/modal.context';

const ContractsContent: FC = () => {
  return (
    <ModalProvider>
      <ContractsTab />
    </ModalProvider>
  );
};

export default ContractsContent;
