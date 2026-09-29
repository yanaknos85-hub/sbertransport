import React, { useState, useEffect, FC } from 'react';
import { Modal, Checkbox } from 'antd';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import { ReactComponent as CloseIcon } from 'components/Evaluation/static/icons/closeIcon.svg';

import styles from './CourierAgreementModal.module.scss';

interface CourierAgreementModalProps {
  visible: boolean;
  onAgree: () => void;
  onCancel: () => void;
}

const CourierAgreementModal: FC<CourierAgreementModalProps> = ({
  visible, onAgree, onCancel,
}) => {
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    if (visible) {
      setChecked(false);
    }
  }, [visible]);

  const handleCheckboxChange = (e: CheckboxChangeEvent) => {
    if (e.target.checked) {
      setChecked(true);
      onAgree();
    } else {
      setChecked(false);
    }
  };

  return (
    <Modal
      title={<span className={styles.boldTitle}>Памятка внутреннего курьера</span>}
      className={styles.modal}
      open={visible}
      onCancel={onCancel}
      footer={null}
      width={800}
      centered
      closeIcon={<CloseIcon />}
    >
      <div className={styles.boldSubtitle}>
        Памятка внутреннего курьера (сервис доставки малогабаритных грузов)
      </div>
      <div className={styles.textContainer}>
        <span>Уважаемый коллега!</span>
        <p>Подключая роль «Внутренний курьер», помните: участие осуществляется с согласия руководителя и не должно препятствовать выполнению основных служебных обязанностей.</p>
        <p>Вознаграждение является дополнительным доходом и облагается НДФЛ. Доставка осуществляется исключительно между корпоративными адресами Банка.</p>
        <p>Перевозка грузов на личные или иные адреса не допускается. Разрешены только малогабаритные грузы (документы, канцелярия, корпоративные сувениры).</p>
        <p>Запрещено перевозить денежные средства, ценности, личные вещи и опасные предметы. Принимая груз, вы несёте полную материальную ответственность за его сохранность.</p>
        <p>В случае утраты или порчи стоимость груза возмещается вами. Передача груза — только при предъявлении удостоверения получателем. Соблюдайте ПДД и конфиденциальность служебной информации.</p>
        <p>Нарушение правил влечёт отключение от сервиса и дисциплинарные меры.</p>
      </div>
      <Checkbox checked={checked} onChange={handleCheckboxChange} />
      <span className={styles.boldCheckbox}>
        Я ознакомлен(а) и согласен(на) с условиями сервиса «Внутренний курьер»
      </span>
    </Modal>
  );
};

export default CourierAgreementModal;
