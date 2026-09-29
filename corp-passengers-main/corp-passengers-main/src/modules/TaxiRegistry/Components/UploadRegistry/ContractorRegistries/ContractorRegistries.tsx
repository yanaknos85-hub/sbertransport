import React, { useState } from 'react';
import { Button, Dropdown } from 'antd';
import { useContractors } from 'api/contractors';
import { useTranslation } from 'i18n';

import { DownOutlined } from '@ant-design/icons';
import { InfoContractor } from 'modules/TaxiRegistry/types/types';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';

import { ShowXLSModal } from '../../Modal/ShowXLSModal';
import { ShowContractorList } from './DropDown/ShowContractorList';

const ContractorRegistries = (): JSX.Element => {
  const { isLoading: isContractorsLoading } = useContractors();
  const { contractors } = useContractors().data;
  const [visible, setVisible] = useState<boolean>(false);
  const [menuVisible, setMenuVisible] = useState<boolean>(true);
  const [modalData, setModalData] = useState<InfoContractor>({
    contractorId: '', monthName: '', contractorName: '',
  });

  const { t } = useTranslation();
  return (
    <div>
      <Dropdown
        overlay={(
          <ShowContractorList
            contractors={contractors}
            setVisible={setVisible}
            setMenuVisible={setMenuVisible}
            setModalData={setModalData}
            menuVisible={menuVisible}
            isContractorsLoading={isContractorsLoading}
          />
        )}
        getPopupContainer={trigger => trigger.parentElement as HTMLElement}
      >
        <Button>
          {t.Forms.RegistryXLSModal.showContractor}
          {' '}
          <DownOutlined />
        </Button>
      </Dropdown>
      {visible && (
        <ShowXLSModal
          visible={visible}
          setVisible={setVisible}
          setMenuVisible={setMenuVisible}
          modalData={modalData}
        />
      )}
    </div>
  );
};
export default withErrorBoundary(ContractorRegistries);
