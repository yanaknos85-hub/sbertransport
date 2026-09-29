import React, { FC } from 'react';
import { DownloadOutlined } from '@ant-design/icons';

import { GET_MASS_REQUEST_MULTI } from '../../../constants/constants.api';
import { useDownloadUrl } from '../../../shared/hooks/useDownloadUrl';
import CargoMassUploadMultiple from '../CargoMassUploadMultiple/CargoMassUploadMultiple';
import { ReactComponent as CloseIcon } from '../static/images/closeCross.svg';
import { ReactComponent as TrucksIcon } from '../static/images/trucks.svg';
import { ModalStyled } from './MassModal.style';

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
  const { download } = useDownloadUrl(GET_MASS_REQUEST_MULTI);

  return (
    <ModalStyled
      open={isVisible}
      onCancel={onClose}
      title="Массовая доставка"
      destroyOnClose={true}
      footer={null}
      closeIcon={<CloseIcon />}
      width={535}
    >
      <div className="descriptionWrapper">
        <p className="description">
          Сервис позволяет создать множество заявок и сэкономить ваше время. Загрузите заполненный шаблон, и заявка будет сформирована
        </p>
        <div className="img"><TrucksIcon /></div>
      </div>
      <div className="downLoad">
        <a
          href="/"
          className="downLoad"
          onClick={e => download(e)}
        >
          <DownloadOutlined />
          {' '}
          Скачать шаблон для заполнения
        </a>
      </div>
      <CargoMassUploadMultiple onUpload={onUpload} setVisible={setVisible} />
    </ModalStyled>
  );
};
