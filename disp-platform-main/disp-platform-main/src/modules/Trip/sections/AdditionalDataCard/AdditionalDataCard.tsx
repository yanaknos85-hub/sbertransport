import React from 'react';
import { List } from 'antd';

import withErrorBoundary from 'components/withErrorBoundary';
import Panel from 'components/Panel/Panel';

import { useAdditionalParams } from './useAdditionalParams';
import { ROW_GUTTER } from 'modules/Trip/constants/passTrip.constants';
import { useTripInfo } from 'modules/Trip/context/TripInfo.context';

/** Дополнительные параметры (трансфер) */
export const AdditionalDataCard = withErrorBoundary(() => {
  const { trip } = useTripInfo();
  const { data } = useAdditionalParams(trip);

  return (
    <Panel
      title="Дополнительные параметры"
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
