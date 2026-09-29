import React, { FC } from 'react';
import TariffsTab from './components/TariffsTab';
import { ModalProvider } from './context/modal.context';

const TariffsContent: FC = () => {
  return (
    <ModalProvider>
      <TariffsTab />
    </ModalProvider>
  );
};

export default TariffsContent;
