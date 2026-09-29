import './override.scss';

import React, { FC } from 'react';
import { PlusCircleOutlined } from '@ant-design/icons';
import { Button, List } from 'antd';
import { observer } from 'mobx-react';
import PageLayout from 'shared/components/PageLayout/PageLayout';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import { EmployeeAppLinks, EmployeeAppLinksTitles } from 'constants/EmployeeApp.constants';

import { DelegatesTexts, DelegatesTextsCyrillic } from './Delegates.constants';
import { useDelegateList } from './useDelegatesList';

import style from './delegates.module.scss';

const DelegatesList: FC = observer(() => {
  const { [StoreNames.delegatesStore]: delegatesStore } = useAppStoreContext();
  const {
    path, namesWithInitials, goToNewDelegate, deleteHandler,
  } = useDelegateList();

  return (
    <PageLayout title={EmployeeAppLinksTitles[EmployeeAppLinks.delegates]}>
      <List
        className="delegate_page"
        dataSource={delegatesStore.delegates}
        renderItem={(delegate: any): JSX.Element => (
          <List.Item
            actions={[
              <TableEditButtons
                path={path}
                id={delegate.id}
                onDelete={(): void => deleteHandler(delegate.id)}
                title={DelegatesTextsCyrillic[DelegatesTexts.deleteConfirm]}
                okText={DelegatesTextsCyrillic[DelegatesTexts.remove]}
                cancelText={DelegatesTextsCyrillic[DelegatesTexts.cancel]}
              />,
            ]}
          >
            <List.Item.Meta
              key={delegate.id}
              title={namesWithInitials[delegate.delegateId]}
              description={delegate.delegateEmployee.positionName}
            />
          </List.Item>
        )}
      />
      <Button
        icon={<PlusCircleOutlined />}
        type="primary"
        size="middle"
        onClick={goToNewDelegate}
        className={style.assign_button}
      >
        {DelegatesTextsCyrillic[DelegatesTexts.addNew]}
      </Button>
    </PageLayout>
  );
});

export default DelegatesList;
