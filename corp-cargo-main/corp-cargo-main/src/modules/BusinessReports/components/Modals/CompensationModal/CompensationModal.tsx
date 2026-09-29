import React, { FC, useState } from 'react';
import {
  Button, Checkbox, Col, Modal, Row
} from 'antd';
import { useTranslation } from 'i18n';

import styles from './Modal.module.scss';

export const CompensationModal: FC = () => {
  const { t } = useTranslation();
  const [visible, setVisible] = useState(false);
  const [withFilters, setWithFilters] = useState<boolean>(false);

  const handleCancel = () => {
    setVisible(false);
  };

  return (
    <>
      <Button type="primary" onClick={() => setVisible(true)}>
        {t.Forms.RegistryXLSModal.xlsCompensations}
      </Button>
      <Modal
        className={styles.modal}
        centered
        visible={visible}
        // onOk={handleOk}
        // confirmLoading={isLoading}
        onCancel={handleCancel}
        footer={[
          <Button
            key="submit"
            type="primary"
            // loading={isLoading}
            // onClick={handleOk}
          >
            {t.global.closingConfirmation}
          </Button>,
          <Button
            key="back"
            danger
            onClick={handleCancel}
          >
            {t.Forms.RegistryXLSModal.cancel}
          </Button>,
        ]}
      >
        <p className={styles.warning}>{t.Forms.RegistryXLSModal.importWarning}</p>
        <p>{t.Forms.RegistryXLSModal.proceedImport}</p>
        <Row>
          <Col span={24}>
            <Checkbox value={withFilters} onChange={() => setWithFilters(!withFilters)}>
              {t.Forms.RegistryXLSModal.withFilters}
            </Checkbox>
          </Col>
        </Row>
      </Modal>
    </>
  );
};
