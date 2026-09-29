import React, { useEffect, useState, FC } from 'react';
import { Table, TableProps } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';

import { defaultDataPaymentsTable } from 'modules/DashboardCards/constants/widgets.constants';
import { TripStatusesEnum } from 'constants/TripRequestStatuses.constants';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { CompensationStatisticsQuery } from 'shared/models/Approval.interface';
import { StoreNames } from 'stores';
import { ITripRequestData } from 'stores/Trip/Trip.interface';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { formatRubles } from 'utils/MoneyUtils';
import { useCargoCompensationStatistics } from 'api/compensations';

import MonthSelect from '../components/monthSelect/MonthSelect';

import {
  DataValueCost,
  DataValueCount,
  DataValueName,
  QuickPaymentsWidgetTitle,
  WrapperOrderWidget,
  WrapperPaymentsWidget,
  WrapperPaymentsWidgetTitle
} from './styledPaymentsWidget';
import './styles.scss';

interface DataType {
  key: string;
  name: string;
  count: number;
  cost: number;
}

const columns: TableProps<DataType>['columns'] = [
  {
    title: 'Статус',
    dataIndex: 'name',
    key: 'name',
    render: text => <DataValueName title={text}>{text}</DataValueName>,
  },
  {
    title: 'Заявок',
    dataIndex: 'count',
    key: 'count',
    render: count => <DataValueCount>{count}</DataValueCount>,
  },
  {
    title: 'К выплате до вычета НДФЛ',
    dataIndex: 'cost',
    key: 'cost',
    align: 'right',
    render: cost => <DataValueCost>{formatRubles(cost / 100)}</DataValueCost>,
  },
];

interface PaymentsWidgetProps {
  fullWidth?: boolean;
}

const PaymentsWidget: FC<PaymentsWidgetProps> = observer(({ fullWidth }) => {
  const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();
  const [month, setMonth] = useState(moment().subtract('months'));
  const [tripList, setTripList] = useState([]);
  const [dataTable, setDataTable] = useState(defaultDataPaymentsTable);
  const [loading, setLoading] = useState(false);

  const handleChangeMonth = e => {
    setMonth(e);
  };

  const getMonthBoundary = (date, isEndOfMonth) => {
    if (isEndOfMonth) {
      return date.endOf('month').toDate();
    } else {
      return date.startOf('month').toDate();
    }
  };

  const compensationQuery: CompensationStatisticsQuery = {
    creationTimeRange: {
      start: Number(getMonthBoundary(month, false)),
      end: Number(getMonthBoundary(month, true)),
    },
  };

  const [fetchCompensation, { data: compensationData, isLoading: compensationLoading }] = useCargoCompensationStatistics(compensationQuery);

  useEffect(() => {
    fetchCompensation(compensationQuery);
  }, [month, fetchCompensation]);

  useEffect(() => {
    const data: ITripRequestData = {
      sortSetting: { directionAsc: false },
      desiredDate: {
        start: Number(getMonthBoundary(month, false)),
        end: Number(getMonthBoundary(month, true)),
      },
      pageSetting: {
        page: 0,
        size: 100,
      },
      transportTypeSet: [TransportTypeEnum.PERSONAL, TransportTypeEnum.PUBLIC],
    };

    tripStore.clearAllRequestList();
    setTripList([]);
    setDataTable(defaultDataPaymentsTable);
    setLoading(true);
    const fetchData = async () => {
      await Promise.all([
        tripStore.loadRequestListNonTerminal(data),
        tripStore.loadRequestFinishedListTerminal(data),
      ]).then(() => setLoading(false));
      setLoading(false);
    };
    fetchData();
  }, [month]);

  useEffect(() => {
    if (tripStore.tripRequestList.length && tripStore.tripRequestFinishedList.length) {
      const tripFinalList = tripStore.tripRequestList.concat(tripStore.tripRequestFinishedList);
      setTripList(tripFinalList);
    }
  }, [tripStore.tripRequestList, tripStore.tripRequestFinishedList]);

  const createAllData = (inputArray): DataType[] => {
    const dataAll: DataType[] = [
      {
        key: '1',
        name: 'в работе',
        count: 0,
        cost: 0,
      },
      {
        key: '2',
        name: 'Формирование приказа',
        count: 0,
        cost: 0,
      },
      {
        key: '3',
        name: 'Ожидание выплаты',
        count: 0,
        cost: 0,
      },
      {
        key: '4',
        name: 'Выплачено',
        count: 0,
        cost: 0,
      },
    ];

    inputArray.forEach(item => {
      switch (item.status) {
        case TripStatusesEnum.PERSONAL_TRIP_IN_PROGRESS:
        case TripStatusesEnum.PERSONAL_AWAITING_TRIP_APPROVAL:
          dataAll[0].count += 1;
          dataAll[0].cost += item.expected.cost;
          break;

        case TripStatusesEnum.PERSONAL_ORDER_PAYMENT_FORMATION:
        case TripStatusesEnum.PUBLIC_ORDER_PAYMENT_FORMATION:
          dataAll[1].count += 1;
          dataAll[1].cost += item.expected.cost;
          break;

        case TripStatusesEnum.PERSONAL_PAYMENT_AWAITING:
        case TripStatusesEnum.PUBLIC_PAYMENT_AWAITING:
          dataAll[2].count += 1;
          dataAll[2].cost += item.expected.cost;
          break;

        case TripStatusesEnum.PERSONAL_PAYMENT_DONE:
        case TripStatusesEnum.PUBLIC_PAYMENT_DONE:
          dataAll[3].count += 1;
          dataAll[3].cost += item.expected.cost;
          break;

        default:
          break;
      }
    });

    if (compensationData) {
      // dataAll[0] - в работе
      dataAll[0].count += compensationData.countInProcess;
      dataAll[0].cost += compensationData.costInProcess;

      // dataAll[1] - Формирование приказа
      dataAll[1].count += compensationData.countApproved;
      dataAll[1].cost += compensationData.costApproved;

      // dataAll[2] - Ожидание выплаты
      dataAll[2].count += compensationData.countOrderPreparing;
      dataAll[2].cost += compensationData.costOrderPreparing;

      // dataAll[3] - Выплачено
      dataAll[3].count += compensationData.countCompleted;
      dataAll[3].cost += compensationData.costCompleted;
    }
    return dataAll;
  };

  useEffect(() => {
    if (tripList.length || compensationData) {
      setDataTable(createAllData(tripList));
    }
  }, [tripList, compensationData]);

  const isLoading = loading || compensationLoading;

  return (
    <WrapperPaymentsWidget fullWidth={fullWidth}>
      <WrapperPaymentsWidgetTitle>
        <WrapperOrderWidget>
          <QuickPaymentsWidgetTitle>Выплаты</QuickPaymentsWidgetTitle>
          <MonthSelect onChange={handleChangeMonth} />
        </WrapperOrderWidget>
      </WrapperPaymentsWidgetTitle>
      <Table
        columns={columns}
        dataSource={dataTable}
        pagination={false}
        loading={isLoading}
      />
    </WrapperPaymentsWidget>
  );
});

export default PaymentsWidget;
