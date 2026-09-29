import React, { FC } from 'react';
import { Button, Modal } from 'antd';

import styles from './styles.module.scss';

interface ModalAddServiceProps {
  isVisible?: boolean;
  handleDone: () => void;
}

const ModalAddServiceOld: FC<ModalAddServiceProps> = ({ isVisible, handleDone }) => (
  <Modal
    visible={isVisible}
    onCancel={handleDone}
    className={styles.modalAddService}
    footer={[
      <Button
        key="done"
        onClick={handleDone}
        type="primary"
      >
        Хорошо
      </Button>,
    ]}
  >
    <div className={styles.modalAddService__title}>Подключить новый сервис</div>
    <div className={styles.modalAddService__content}>Свяжитесь с Вашим менеджером для подключения нового сервиса</div>
    <div className={styles.modalAddService__contacts}>
      <ul>
        <li>Корпоративные перевозки сотрудников: Никита Никифоров +7 905 360 09 40</li>
        <li>Логистика: Алексей Каргин +7 925 671 79 90 </li>
        <li>Управление корпоративным автопарком: Михаил Иночкин +7 905 079 19 88</li>
        <li>Корпоративный паркинг: Юрий Бондаренко +7 928 185 11 25 </li>
      </ul>
      <p>или направьте заявку по электронной почте catransport@sberbank.ru</p>
    </div>
  </Modal>
);

export default ModalAddServiceOld;
