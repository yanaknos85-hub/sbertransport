import { Button, Dropdown } from 'antd';
import React, { useState } from 'react';
import { useContractors } from 'api/contractors';
import { useTranslation } from 'i18n';
import { InfoContractor } from 'modules/TaxiRegistry/types/types';
import { DownOutlined } from '@ant-design/icons';
import styles from '../asset/styles.module.scss';

import { UploadXLSModal } from '../../Modal/UploadXLSModal';
import { DropDownContractorList } from './DropDown/DropDownContractorList';

export const UploadContractorRegistry = (): JSX.Element => {
  const { contractors } = useContractors().data;

  const [visible, setVisible] = useState<boolean>(false);

  const { t } = useTranslation();

  const [menuVisible, setMenuVisible] = useState<boolean>(true);

  const [modalData, setModalData] = useState<InfoContractor>({
    contractorId: '', monthName: '', contractorName: '',
  });
  return (
    <div>
      <Dropdown
        overlay={(
          <DropDownContractorList
            contractors={contractors}
            setVisible={setVisible}
            setMenuVisible={setMenuVisible}
            menuVisible={menuVisible}
            setModalData={setModalData}
          />
        )}
        getPopupContainer={trigger => trigger.parentNode as HTMLElement}
      >
        <Button className={styles.btnContract}>
          {t.Forms.RegistryXLSModal.xlsExportContractors}
          {' '}
          <DownOutlined />
        </Button>
      </Dropdown>
      {visible && (
        <UploadXLSModal
          visible={visible}
          setVisible={setVisible}
          setMenuVisible={setMenuVisible}
          modalData={modalData}
        />
      )}
    </div>
  );
};
