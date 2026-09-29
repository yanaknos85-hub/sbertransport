import './override.scss';

import React, { FC } from 'react';
import { List } from 'antd';
import { observer } from 'mobx-react';
import PageLayout from 'shared/components/PageLayout/PageLayout';

import { DelegatesTexts, DelegatesTextsCyrillic } from './Delegates.constants';
import { useDelegateCard } from './useDelegateCard';

const DelegateCard: FC = observer(() => {
  const {
    spesificDelegate, goDelegatesList, rusName, delegateId, delegatePeriod,
  } = useDelegateCard();
  return (
    <PageLayout title={DelegatesTextsCyrillic[DelegatesTexts.pageHeader]} onBack={goDelegatesList}>
      {spesificDelegate ? (
        <List>
          <List.Item>
            <List.Item.Meta title={spesificDelegate.fullNameString} />
          </List.Item>
          {delegateId && (
            <List.Item>
              <List.Item.Meta title={delegatePeriod} description="Период делегирования" />
            </List.Item>
          )}
          <List.Item>
            <List.Item.Meta title={rusName} description="Вид транспорта" />
          </List.Item>
        </List>
      ) : null}
    </PageLayout>
  );
});

export default DelegateCard;
