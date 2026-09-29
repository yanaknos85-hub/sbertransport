import React from 'react';
import { List } from 'antd';
import Panel from 'components/Panel/Panel';
import withErrorBoundary from 'components/withErrorBoundary';
import { ROW_GUTTER } from 'modules/Trip/constants/passTrip.constants';
import { useTripInfo } from 'modules/Trip/context/TripInfo.context';
import { useTransferData } from 'modules/Trip/sections/TransferDataCard/useTransferData';

/** Информация о поездке (трансфер) */
export const TransferDataCard = withErrorBoundary(() => {
  const { trip } = useTripInfo();
  const { data } = useTransferData(trip);

  return (
    <Panel
      title="Информация о поездке"
      marginTop
      marginBottom
      smallVerticalPadding
    >
      <List
        grid={{ gutter: ROW_GUTTER, column: 4 }}
        dataSource={data}
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
    </Panel>
  );
});
