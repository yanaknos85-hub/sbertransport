import React, { FC, SyntheticEvent } from 'react';
import { useParams } from 'react-router-dom';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { Col, Divider, Row } from 'antd';
import cn from 'classnames';
import moment from 'moment';
import CargoTariffTag from 'shared/components/Cargo/CargoTariffTag/CargoTariffTag';
import Stepper from 'shared/components/Cargo/Stepper';
import { Icon } from 'shared/components/Icon/Icon';
import { useUiContext } from 'shared/components/UI';
import { emptySign } from 'shared/constants/constants';
import Rate from 'shared/form/Rate/Rate';
import { useModalState } from 'shared/hooks/useModal';
import { toRubles } from 'utils';

import { DATE_FORMAT } from 'constants/constants.app';
import { formatDistance } from 'utils/DistanceUtils';

import { CargoRequestStatusesTitlesExchange, statusDictionary } from '../../../ExchangeStatuses.constants';
import { ContractsTabs, DetailedViewExchange, Statuses } from '../../../types';
import { RateModal } from '../../Modals/RateModal/RateModal';
import { Button } from '../Button/Button';

import styles from './Header.module.scss';

interface Props {
  request: DetailedViewExchange;
}

export const Header: FC<Props> = props => {
  const { request } = props;

  const customHistory = History();
  const { isMobile } = useUiContext();

  const [modal, modalActions] = useModalState();

  const { type } = useParams<{ type: string; id: string }>();

  const handleGoBack = (e: SyntheticEvent) => {
    e.preventDefault();
    customHistory.goBack();
  };

  const getTariffText = (request: DetailedViewExchange): string | undefined => (
    request.transportTypeRus || 'Не определен'
  );

  const createdDate = moment(request.creationTime).format(`${DATE_FORMAT.BASE_REVERTED_DOTS}`);
  const desiredDateFormatted = `
    ${moment(request.desiredDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)} в
    ${moment(request.desiredDate).format(DATE_FORMAT.TIME_FULL)}
  `;
  const activeStep = statusDictionary[request.status || Statuses.CARGO_AWAITING_APPROVAL];
  const distance = request.distance ? formatDistance(request.distance as number) : emptySign;

  const COST = toRubles(request.cost);

  const handleModal = () => {
    modalActions.show();
  };

  return (
    <div className={styles.cargoMainInnerContent}>
      <Row>
        <Col flex="auto">
          <div className={cn(styles.titleContainer, {
            [styles['titleContainerMobile']]: isMobile,
          })}
          >
            <div className={styles.arrow}>
              <ArrowLeftOutlined
                className={styles.arrowIcon}
                onClick={handleGoBack}
              />
              <h3 className={styles.title}>{`Заявка ${request.humanReadableId}`}</h3>
            </div>
            <span className={styles.creationTime}>
              {`От: ${createdDate}`}
            </span>
          </div>
        </Col>
        <Col flex="auto" className={styles.headerCol}>
          {(type === ContractsTabs.AVAILABLE || type === ContractsTabs.NON_TERMINAL) && (
            <Button request={request} />
          )}
          {type === ContractsTabs.TERMINAL && request?.evaluation && (
            <div
              className={cn(styles.rateWrapper, {
                [styles['rateWrapperMobile']]: isMobile,
              })}
              onClick={handleModal}
            >
              <Rate
                className={styles.rate}
                disabled
                value={request?.evaluation?.rating}
              />
            </div>
          )}
        </Col>
        <Col span={24} style={{ marginTop: '8px' }}>
          <CargoTariffTag
            $isMobile={isMobile}
            icon={<Icon transportType={request.transportType} />}
            express="true"
          >
            {getTariffText(request)}
          </CargoTariffTag>
        </Col>
      </Row>
      <Divider />
      <Row>
        <Col span={isMobile ? 24 : 10}>
          <div className={styles.statusContainer}>
            <p>
              {`Статус заявки: `}
              <span className={styles.statusText}>
                {request?.status && CargoRequestStatusesTitlesExchange[request?.status]}
              </span>
            </p>
          </div>
          <Stepper steps={3} active={activeStep} />
        </Col>
        <Col span={isMobile ? 24 : 12} offset={isMobile ? 0 : 2}>
          <Row style={{ alignItems: 'anchor-center' }}>
            <Col span={10}>
              <div className={styles.totalBlockTitle}>
                <span>Необходимо доставить</span>
                <div className={styles.totalBlockValue}>
                  {desiredDateFormatted}
                </div>
              </div>
            </Col>
            <Divider type="vertical" style={{ height: '50px' }} />
            <Col span={5}>
              <div className={styles.totalBlockTitle}>
                <span>Расстояние</span>
                <div className={styles.totalBlockValue}>{distance}</div>
              </div>
            </Col>
            <Divider type="vertical" style={{ height: '50px' }} />
            <Col span={6}>
              <div className={styles.totalBlockTitle}>
                <span>Оплата за доставку</span>
                <div className={styles.totalBlockValue}>
                  {`${COST} руб.`}
                </div>
              </div>
            </Col>
          </Row>
        </Col>
      </Row>
      <RateModal
        visible={modal}
        id={request.id}
        transportType={request.transportType}
        transportTypeRus={request.transportTypeRus}
        hide={modalActions.hide}
      />
    </div>
  );
};
