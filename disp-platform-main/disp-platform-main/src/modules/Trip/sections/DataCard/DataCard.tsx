import React from 'react';
import { Col, List, Row } from 'antd';

import EditButton from 'components/EditButton/EditButton';
import Panel from 'components/Panel/Panel';
import withErrorBoundary from 'components/withErrorBoundary';
import { useModalState } from 'hooks/useModal';

import { EditModal } from 'modules/Trip/components/EditModal';
import { ROW_GUTTER } from 'modules/Trip/constants/passTrip.constants';
import { useTripInfo } from 'modules/Trip/context/TripInfo.context';
import { useData } from 'modules/Trip/sections/DataCard/useData';

/** Плановые и фактические данные */
export const DataCard = withErrorBoundary(() => {
  const { trip, checkinInfo } = useTripInfo();
  const { expectedData, factData } = useData(trip, checkinInfo);

  const [visible, { show, hide }] = useModalState();

  return (
    <Panel marginTop marginBottom>
      <Row gutter={ROW_GUTTER}>
        <Col span={12}>
          <Panel
            title="Плановые данные"
            fullHeight
            smallVerticalPadding
          >
            <List
              grid={{ gutter: ROW_GUTTER, column: 2 }}
              dataSource={expectedData}
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
        </Col>
        <Col span={12}>
          <Panel
            title="Фактические данные"
            fullHeight
            smallVerticalPadding
            actions={<EditButton onClick={show} />}
          >
            <List
              grid={{ gutter: ROW_GUTTER, column: 2 }}
              dataSource={factData}
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
        </Col>
      </Row>

      <EditModal
        trip={trip}
        visible={visible}
        closeModal={hide}
      />
    </Panel>
  );
});
