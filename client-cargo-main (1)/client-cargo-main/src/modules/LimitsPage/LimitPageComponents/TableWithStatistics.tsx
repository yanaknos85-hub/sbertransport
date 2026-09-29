import React from 'react';
import { List, Table, Tabs } from 'antd';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { formatRubles } from 'utils';

import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeTitlesEnum } from 'stores/Trip/Trip.interface';

import { columns } from '../Components/Columns';
import { useDetailedLimitsMapper } from '../useDetailedLimitsMapper';

import styles from '../page.module.scss';

export const TableWithStatistics: React.FC = () => {
  const { [StoreNames.transportTypesStore]: transportTypes } = useAppStoreContext();

  const footer = (amount: number): JSX.Element => (
    <>
      Итого:
      {formatRubles(amount)}
    </>
  );
  const { getSpentActions, getSpentAmountsByTransportType } = useDetailedLimitsMapper();

  //    Выбираем объекты Tabs TransportTypesModel по их порядку - Такси, Личный, Общественный, Доставка сборного груза
  const tabTransportTypes = transportTypes.transportTypes?.filter(
    type => TransportTypeTitlesEnum[`${type.name}` as keyof typeof TransportTypeTitlesEnum]
      === TransportTypeTitlesEnum.TAXI
      || TransportTypeTitlesEnum[`${type.name}` as keyof typeof TransportTypeTitlesEnum]
      === TransportTypeTitlesEnum.PERSONAL
      || TransportTypeTitlesEnum[`${type.name}` as keyof typeof TransportTypeTitlesEnum]
      === TransportTypeTitlesEnum.PUBLIC
      || TransportTypeTitlesEnum[`${type.name}` as keyof typeof TransportTypeTitlesEnum]
      === TransportTypeTitlesEnum.DEDICATED
  );
  return (
    <List className={styles.list} header={<div className="list-header">Статистика расходования лимита</div>}>
      <Tabs>
        {tabTransportTypes.map(transportTypeModel => (
          <Tabs.TabPane
            tab={TransportTypeTitlesEnum[`${transportTypeModel.name}` as keyof typeof TransportTypeTitlesEnum]}
            key={transportTypeModel.id}
          >
            <Table
              key={transportTypeModel.name}
              dataSource={getSpentActions().filter(request => request.transportType === transportTypeModel.name)}
              columns={columns()}
              rowClassName="editable-row"
              rowKey="id"
              size="small"
              tableLayout="fixed"
              className="transport_types_table"
              footer={(): JSX.Element => footer(getSpentAmountsByTransportType[transportTypeModel.id])}
            />
          </Tabs.TabPane>
        ))}
      </Tabs>
    </List>
  );
};
