import React, { FC } from 'react';
import { Col, Row } from 'antd';
import { observer } from 'mobx-react';
import { CargoMainInnerContent } from 'shared/components/Cargo/CargoLayout';

import styles from './styles.module.scss';

interface Props {
  comment: string | undefined;
}

const CargoEngComment: FC<Props> = observer(({ comment }) => {
  if (!comment) {
    return null;
  }

  const commentWithLineBreak = comment.split('\n');

  return (
    <CargoMainInnerContent>
      <Row>
        <Col span={24}>
          <div className={styles.commentHeaderText}>Комментарий инженера ОТО</div>
          {commentWithLineBreak.map(word => (
            <div key={word} className={styles.commentText}>{word}</div>
          ))}
        </Col>
      </Row>
    </CargoMainInnerContent>
  );
});

export default CargoEngComment;
