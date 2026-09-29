import React from 'react';
import { Table, Tabs } from 'antd';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { formatRubles } from 'utils';

import { useDetailedLimitsMapper } from '../../useDetailedLimitsMapper';
import { StyledList } from './styled';

const columns = [
  {
    title: 'ID заявки',
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: 'Дата создания заявки',
    dataIndex: 'creationTime',
    key: 'creationTime',
  },
  {
    title: 'ФИО инициатора',
    dataIndex: 'fullName',
    key: 'fullName',
  },
  {
    title: 'Сумма',
    dataIndex: 'sum',
    key: 'sum',
  },
];

const TableWithStatistics: React.FC = () => {
  const { [StoreNames.transportTypesStore]: transportTypes } = useAppStoreContext();
  const footer = (amount: number): JSX.Element => (
    <>
      Итого:
      {formatRubles(amount)}
    </>
  );
  const { getSpentActions, getSpentAmountsByTransportType } = useDetailedLimitsMapper();

  //    Выбираем объекты Tabs TransportTypesModel по их порядку - Такси, Личный, Общественный, Грузовик
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
    <StyledList header={<div className="list-header">Статистика расходования лимита</div>}>
      <Tabs>
        {tabTransportTypes.map(transportTypeModel => (
          <Tabs.TabPane
            tab={TransportTypeTitlesEnum[`${transportTypeModel.name}` as keyof typeof TransportTypeTitlesEnum]}
            key={transportTypeModel.id}
          >
            <Table
              key={transportTypeModel.name}
              dataSource={getSpentActions().filter(request => request.transportType === transportTypeModel.name)}
              columns={columns}
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
    </StyledList>
  );
};

export default TableWithStatistics;
