import React, { Suspense } from 'react';
import { observer } from 'mobx-react';
import { useRouteMatch, Redirect } from 'react-router-dom';
import { useSharedRideDetailed } from 'api/shared-rides';
import { RUBLE_SIGN } from 'constants/constants.app';
import {
  Timeline, Divider, Descriptions, List, Table
} from 'antd';
import { useTranslation } from 'i18n';
import Title from 'antd/lib/typography/Title';
import { formatAddress } from 'utils/formatAddress';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { formatPassengerName } from 'utils/formatPassengerName';
import { TaxiClassDescriptions } from 'stores/Trip/Trip.interface';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { columnsPropsFactory } from 'shared/columnsPropsFactory';
import { SharedRideOrderKpi } from 'stores/SharedRide/SharedRide.interface';
import * as routes from 'constants/constants.routes';
import columns from './OrdersKpiColumns';

const SharedRideDetailedInner = observer(
  (): JSX.Element => {
    const { t } = useTranslation();
    const { id } = useRouteMatch<any>().params;
    const sharedRideDetailed = useSharedRideDetailed(id).data;

    const columnsProps = columnsPropsFactory<SharedRideOrderKpi>([
      ...columns.map(column => ({
        ...column,
        title: t.SharedRidesDetailed.Columns[column.key as keyof typeof t.SharedRidesDetailed.Columns], // TODO: refactor
      })),
    ]);

    return (
      <div className="content">
        <Title level={4}>{t.SharedRidesDetailed.TitleOrder({ id })}</Title>
        <Divider orientation="left">{t.SharedRidesDetailed.TitleInfo}</Divider>
        <Descriptions title="" bordered>
          <Descriptions.Item label={t.SharedRidesDetailed.Info.passengers}>
            {sharedRideDetailed.passengers}
          </Descriptions.Item>
          <Descriptions.Item label={t.SharedRidesDetailed.Info.totalCost}>
            {`${sharedRideDetailed.kpi.totalCost} ${RUBLE_SIGN}`}
          </Descriptions.Item>
          <Descriptions.Item label={t.SharedRidesDetailed.Info.totalDistanceKm}>
            {`${sharedRideDetailed.kpi.totalDistanceKm} км.`}
          </Descriptions.Item>
          <Descriptions.Item label={t.SharedRidesDetailed.Info.totalTimeMin}>
            {`${sharedRideDetailed.kpi.totalTimeMin} мин.`}
          </Descriptions.Item>
          <Descriptions.Item label={t.SharedRidesDetailed.Info.tariff}>
            {TaxiClassDescriptions[sharedRideDetailed.tariffId]}
          </Descriptions.Item>
        </Descriptions>
        <Divider orientation="left">{t.SharedRidesDetailed.TitleOrdersKpi}</Divider>
        <Table
          columns={columnsProps}
          dataSource={sharedRideDetailed.kpi.ordersKpi}
          bordered
          style={{ overflow: 'scroll' }}
          scroll={{ x: 600 }}
          rowKey="orderId"
          size="small"
          tableLayout="auto"
          className="expanded_filters_table"
          pagination={false}
        />
        <Divider orientation="left">{t.SharedRidesDetailed.TitlePassengers}</Divider>
        <List
          bordered
          dataSource={sharedRideDetailed.employeePassengers}
          renderItem={passenger => <List.Item>{formatPassengerName(passenger)}</List.Item>}
        />
        <Divider orientation="left">{t.SharedRidesDetailed.TitleStops}</Divider>
        {sharedRideDetailed.stops && sharedRideDetailed.stops.length ? (
          <Timeline>
            {sharedRideDetailed.stops.map(stop => (
              <Timeline.Item key={stop.orderId}>
                {`${formatAddress(stop.waypoint)} ${stop.startTime} - ${stop.endTime}`}
              </Timeline.Item>
            ))}
          </Timeline>
        ) : null}
      </div>
    );
  }
);

const SharedRideDetailed = (): JSX.Element => (
  <ErrorBoundary fallback={() => <Redirect to={routes.SHARED_RIDES} />}>
    <Suspense fallback={<SpinWrapped />}>
      <SharedRideDetailedInner />
    </Suspense>
  </ErrorBoundary>
);

export default SharedRideDetailed;
