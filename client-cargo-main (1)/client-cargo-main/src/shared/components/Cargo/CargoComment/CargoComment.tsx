import React, { FC } from 'react';
import { Col, Row } from 'antd';
import { observer } from 'mobx-react';
import { CargoMainInnerContent } from 'shared/components/Cargo/CargoLayout';

import styles from './styles.module.scss';

interface Props {
  comment: string | undefined;
}

const CargoComment: FC<Props> = observer(({ comment }) => {
  if (!comment) {
    return null;
  }

  return (
    <CargoMainInnerContent>
      <Row>
        <Col span={24}>
          <div className={styles.commentHeaderText}>Комментарий к заказу</div>
          <div className={styles.commentText}>{comment}</div>
        </Col>
      </Row>
    </CargoMainInnerContent>
  );
});

export default CargoComment;
