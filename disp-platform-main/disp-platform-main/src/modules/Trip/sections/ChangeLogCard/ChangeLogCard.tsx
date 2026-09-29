import React from 'react';
import { Col, List, Row } from 'antd';
import moment from 'moment';

import Panel from 'components/Panel/Panel';
import withErrorBoundary from 'components/withErrorBoundary';

import { useTripInfo } from 'modules/Trip/context/TripInfo.context';
import { ROW_GUTTER } from 'modules/Trip/constants/passTrip.constants';

const FORMAT = 'DD.MM.YYYY HH:mm';

const formatDate = (value: string | null | undefined): string => value ? moment(value).format(FORMAT) : '—';

/** Журнал изменений заявки */
export const ChangeLogCard = withErrorBoundary(() => {
  const { trip } = useTripInfo();

  const changeLogData = [
    {
      title: 'Дата и время просмотра заявки диспетчером',
      desc: formatDate(trip.dispatcherTakeToWork),
    },
    {
      title: 'Дата и время изменения статуса диспетчером',
      desc: formatDate(trip.statusChangedAt),
    },
  ];

  return (
    <Panel
      marginTop
      marginBottom
      title="Журнал изменений"
      smallVerticalPadding
    >
      <Row>
        <Col span={12}>
          <List
            grid={{ gutter: ROW_GUTTER, column: 2 }}
            dataSource={changeLogData}
            rowKey="title"
            renderItem={({ title, desc }) => (
              <List.Item>
                <List.Item.Meta
                  title={title}
                  description={desc}
                />
              </List.Item>
            )}
          />
        </Col>
      </Row>
    </Panel>
  );
});
