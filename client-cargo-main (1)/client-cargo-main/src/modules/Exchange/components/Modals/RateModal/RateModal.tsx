import React, { FC } from 'react';
import {
  Card, Col, Rate, Row
} from 'antd';
import cn from 'classnames';
import CargoTariffTag from 'shared/components/Cargo/CargoTariffTag/CargoTariffTag';
import { Icon } from 'shared/components/Icon/Icon';
import TModal from 'shared/ui/Modal/Modal';

import { useEvaluation } from '../../../api/exchange';
import { TransportTypeEnum } from '../../../types';
import { ReactComponent as CloseIcon } from './icons/closeIcon.svg';
import { convertDate, icons } from './utils';

import styles from './RateModal.module.scss';

interface Props {
  id: string;
  transportType: TransportTypeEnum;
  transportTypeRus: string;
  visible: boolean;
  hide: () => void;
}

export const RateModal: FC<Props> = props => {
  const {
    id, transportTypeRus, transportType, visible, hide,
  } = props;

  const evaluation = useEvaluation(id, { enabled: visible }).data;

  if (!evaluation) {
    return null;
  }

  const rate = { ...evaluation };

  const tariffText = transportTypeRus || 'Не определен';
  const filteredIcons = icons.filter(icon => rate?.reasons?.includes(icon.type));
  const rating = rate?.rating ?? 0;
  const isPositive = rating > 3;

  return (
    <TModal
      title="Оценка вашей доставки"
      visible={visible}
      onCancel={() => hide()}
      footer={null}
      closeIcon={<CloseIcon />}
    >
      {!evaluation ? <div>Нет данных</div> : (
        <>
          <div className={styles.modalInfo}>
            <CargoTariffTag icon={<Icon transportType={transportType} />}>
              {tariffText}
            </CargoTariffTag>
            <span>{convertDate(1746451017608)}</span>
          </div>
          <h3 className={styles.title}>{isPositive ? 'Хорошо' : 'Плохо'}</h3>
          <div className={styles.rateWrapper}>
            <Rate
              className={cn({
                [styles.positive]: isPositive,
                [styles.negative]: !isPositive,
              })}
              value={rating}
              disabled
            />
          </div>
          <h3 className={styles.title}>{isPositive ? 'Особенно понравилось' : 'Разочаровало'}</h3>
          <div className={styles.iconBlock}>
            {filteredIcons.map(({ type, icon }) => (
              <Card
                bordered={false}
                key={type}
                bodyStyle={{ padding: 0 }}
              >
                {icon({
                  fillBackground: isPositive ? '#10BF6A' : '#F2F3F6',
                  fillFont: isPositive ? 'white' : 'black',
                  stroke: isPositive ? 'white' : '#262626',
                  fillOpacity: isPositive ? 1 : 0.6,
                })}
              </Card>
            ))}
          </div>
          <div>
            <div className={styles.commentTitle}>Комментарий</div>
            <div className={styles.commentWrapper}>
              <Row>
                <Col span={24}>
                  <div className={styles.commentText}>{rate.comment || 'Нет комментария'}</div>
                </Col>
              </Row>
            </div>
          </div>
        </>
      )}
    </TModal>
  );
};
