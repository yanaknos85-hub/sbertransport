import React from 'react';
import { EmptyView } from 'shared/components/EmptyView';
import { /* useModal, */ ModalProvider } from '../Table/context/modal.context';
// import { Button } from 'shared/components/Button/Button';
import { AddEditModal } from '../Table/AddEditModal';

// const ButtonCreate = () => {
//   const { openAdd } = useModal();

//   return (
//     <Button type="primary" onClick={openAdd}>
//       Создать
//     </Button>
//   );
// };

const Empty = () => {
  return (
    <ModalProvider>
      <EmptyView
        title="Список пуст"
        description="Будущие поездки еще не запланированы"
      >
        {/* <ButtonCreate /> */}
      </EmptyView>
      <AddEditModal />
    </ModalProvider>
  );
};

export default Empty;
