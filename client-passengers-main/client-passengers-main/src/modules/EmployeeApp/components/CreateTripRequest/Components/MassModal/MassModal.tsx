import React, { FC } from 'react';
import SRMMassUploadMultiple from '../SRMMassUploadMultiple/SRMMassUploadMultiple';
import { ModalStyled } from './MassModal.style';

import { ReactComponent as CloseIcon } from 'shared/components/Images/closeCross.svg';

interface Props {
  onUpload: any;
  isVisible: boolean;
  onClose: () => void;
  setVisible: (value: boolean) => void;
}

export const MassModal: FC<Props> = props => {
  const {
    onUpload, isVisible, onClose, setVisible,
  } = props;

  return (
    <ModalStyled
      visible={isVisible}
      onCancel={onClose}
      title="Массовая загрузка заявок"
      destroyOnClose={true}
      footer={null}
      closeIcon={<CloseIcon />}
      width={535}
    >
      <div className="descriptionWrapper">
        <p className="description">
          Сервис позволяет создать множество заявок на перевозку сотрудников для автоматического объединения маршрутов. Загрузите заполненный шаблон, и заявки будут сформированы
        </p>
      </div>
      <SRMMassUploadMultiple onUpload={onUpload} setVisible={setVisible} />
    </ModalStyled>
  );
};
