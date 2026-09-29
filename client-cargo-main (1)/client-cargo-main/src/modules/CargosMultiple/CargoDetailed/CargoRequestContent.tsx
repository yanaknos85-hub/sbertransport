/* eslint-disable jsx-a11y/anchor-is-valid */
import React, { SyntheticEvent, useState } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import {
  Button, Col, Divider, Row, Space, Timeline
} from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { observer } from 'mobx-react';
import moment from 'moment';

import {
  CancelReasons, CargoRequestStatusesTitles, DRIVER_INFO_STATUSES, journalCancelStatuses, statusDictionary, Statuses
} from 'constants/CargoRequestStatuses.constants';
import { IS_REMOTE } from 'constants/constants.env';
import { UUID } from 'utils/io-ts';

if (!IS_REMOTE) {
  import('antd/dist/antd.css'); // для девелопа! если микро запущен как главный контейнер
}

import './override.scss';

import { SpinWrapped } from 'shared/components';
import AddressBlockMultipleDetailed from 'shared/components/Cargo/AddressBlockMulti/AddressBlockMultipleDetailed';
import CargoComment from 'shared/components/Cargo/CargoComment';
import CargoEngComment from 'shared/components/Cargo/CargoEngComment';
import {
  CargoContainer,
  CargoMainContentFull,
  CargoMainInnerContent
} from 'shared/components/Cargo/CargoLayout';
import CargoTariffTag from 'shared/components/Cargo/CargoTariffTag/CargoTariffTag';
import HistoryTimeline from 'shared/components/Cargo/HistoryTimeline/HistoryTimeline';
import Stepper from 'shared/components/Cargo/Stepper';
import UserBlock from 'shared/components/Cargo/UserBlock/UserBlock';
import { MapComponent } from 'shared/components/Map/MapComponent';
import { ScheduleDetailed } from 'shared/components/SchedulerMulti/SchedulerDetailed/ScheduleDetailed';
import { useUiContext } from 'shared/components/UI';
import { emptySign } from 'shared/constants/constants';
import { CARGO_CANCELLATION_CONFIG } from 'shared/constants/constants';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { Segment } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { RequestType } from 'shared/models/types';
import TButton from 'shared/ui/Button/Button';
import { declOfNum, toRubles } from 'utils';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';
import { CargoHistoryType } from 'stores/Cargos/types';
import { StoreNames } from 'stores/StoreNames.enum';
import { DATE_FORMAT } from 'constants/constants.app';
import { CARGOS_JOURNAL, REGULAR_CARGOS_JOURNAL } from 'constants/constants.routes';
import { formatDistance } from 'utils/DistanceUtils';

import { ReactComponent as EditIcon } from '../static/images/editButton.svg';
import { performAndRedirect } from '../utils';
import { CargoList } from './CargoList';
import CargoModal from './CargoModal';
import { FileComponent } from './FileComponent';
import { OrderCancelModal } from './OrderCancelModal';
import { PackagesSection } from './PackagesSection/PackagesSection';
import { QrCodesSection } from './QrCodesSection/QrCodesSection';

import styles from './styles.module.scss';

/**
 * По полученному из запроса типу транспорта и типу тарифу выбираем отображаемый текст для элемента CargoTariffTag
 * @param  request запрос
 */
const getTariffText = (request: CargoRequestModel): string | undefined => (
  request.calculatedTariff ? request.calculatedTariff?.transportType.nameRus : 'Не определен'
);

const CargoRequestContent = observer(
  ({
    request,
    history,
    isApproval,
    isRegular,
  }: {
    request: CargoRequestModel;
    history: CargoHistoryType[];
    isApproval: boolean;
    isRegular: boolean;
    isJournal: boolean;
  }): JSX.Element | null => {
    const {
      [StoreNames.cargosStore]: cargosStore,
    } = useAppStoreContext();

    const { isMobile } = useUiContext();
    const match = useRouteMatch<{ reqId: UUID }>();
    const { reqId } = match.params;
    const [modalTimeline, setModalTimeline] = useState(false);
    const [form] = useForm();
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [reason, setReason] = useState('');
    const [textAreaVisible, setTextAreaVisible] = useState(false);

    if (!request) {
      return <SpinWrapped />;
    }

    const cancelRequest = () => performAndRedirect(
      () => cargosStore.cancelRequest(
        reqId,
        reason,
        CARGO_CANCELLATION_CONFIG.code,
        CARGO_CANCELLATION_CONFIG.field,
        CARGO_CANCELLATION_CONFIG.value
      ),
      CARGOS_JOURNAL
    );

    const cancelRegularRequest = () => performAndRedirect(
      () => cargosStore.cancelRegularRequest(
        reqId,
        reason,
        CARGO_CANCELLATION_CONFIG.code,
        CARGO_CANCELLATION_CONFIG.field,
        CARGO_CANCELLATION_CONFIG.value
      ),
      REGULAR_CARGOS_JOURNAL
    );

    const cancelOrder = () => {
      if (isRegular) {
        cancelRegularRequest();
      } else {
        cancelRequest();
      }
      setShowDeleteModal(false);
    };
    const handleConfirmModal = async () => {
      setShowDeleteModal(false);
      await cancelOrder();
      setReason('');
      setTextAreaVisible(false);
    };

    const handleCancelModal = () => {
      setReason('');
      setTextAreaVisible(false);
      setShowDeleteModal(false);
      form.setFieldsValue({ reason: '' });
    };

    const handleSelectModal = (value: string, { label }: Record<string, string>) => {
      if (value === CancelReasons.OTHER_REASON) {
        setTextAreaVisible(true);
        setReason(form.getFieldValue('description'));
      } else {
        setReason(label);
        setTextAreaVisible(false);
      }
    };

    const handleChangeReason = (value: React.ChangeEvent<HTMLTextAreaElement>) => {
      setReason(value.currentTarget.value);
    };
    const customHistory = History();

    const handleGoBack = (e: SyntheticEvent) => {
      e.preventDefault();
      customHistory.goBack();
    };

    const activeStep = statusDictionary[request.status || 'CARGO_AWAITING_APPROVAL'];
    const deliveryTime = request.calculatedTariff?.deliveryTime
      ? `${request.calculatedTariff?.deliveryTime} ${declOfNum(request.calculatedTariff?.deliveryTime, [
        'день',
        'дня',
        'дней',
      ])}`
      : emptySign;
    const cost = request.calculatedTariff?.cost ? toRubles(request.calculatedTariff?.cost) : emptySign;
    const distance = request.calculatedTariff?.distance ? formatDistance(request.calculatedTariff?.distance as number) : emptySign;

    const createdDate = moment(request.creationTime).format(`${DATE_FORMAT.BASE_REVERTED_DOTS}`);

    const desiredDate = isRegular
      ? moment(request?.desiredDate).format(DATE_FORMAT.BASE_REVERTED_DOTS) : request?.desiredDate;

    return (
      <CargoContainer>
        <CargoMainContentFull>
          <CargoMainInnerContent>
            <Row style={{ gap: '8px', alignItems: 'center' }}>
              <Col flex="300px">
                <div className={styles.titleContainer}>
                  <ArrowLeftOutlined className={styles.arrowIcon} onClick={handleGoBack} />
                  <h3 className={styles.title}>{`Заявка ${request.humanReadableId}`}</h3>
                </div>
              </Col>
              <Col flex="auto" className={styles.headerCol}>
                <CargoTariffTag
                  $isMobile={isMobile}
                  express="true"
                  style={{ marginLeft: '10px' }}
                >
                  {getTariffText(request)}
                </CargoTariffTag>
                {request.requestType === RequestType.RELOCATION && (
                  <p className={styles.relocationTag}>
                    Переезд
                  </p>
                )}
                {isApproval ? (
                  <Space />
                ) : (
                  request.status !== Statuses.CARGO_CANCELED && (
                    <div className={styles.controlButtonsDiv}>
                      {!isMobile ? (
                        <Button
                          type="text"
                          size="small"
                          disabled={true}
                        >
                          <EditIcon />
                        </Button>
                      ) : null}
                      <TButton
                        type="text"
                        size="small"
                        style={{
                          width: isMobile ? 'auto' : '146px',
                          padding: isMobile ? '0px 8px' : '0px',
                          height: isMobile ? '28px' : '32px',
                          backgroundColor: '#F2F3F6',
                          color: '#4D4D4D',
                          borderColor: '#F2F3F6',
                          borderRadius: '8px',
                          marginRight: '8px',
                          fontWeight: '600',
                        }}
                        disabled={!journalCancelStatuses.some(status => request.status === status)}
                        onClick={() => setShowDeleteModal(true)}
                      >
                        Отменить
                      </TButton>
                    </div>
                  )
                )}
              </Col>
              <Col span={24}>
                <div className={styles.creationTime}>
                  <p>
                    Создано:
                    {createdDate}
                  </p>
                  {request.desiredDate && (
                    <p>
                      Дата отправления:
                      {desiredDate}
                    </p>
                  )}
                  {request.requestType === RequestType.RELOCATION && (
                    <p>
                      № Служебной записки:
                      {' '}
                      {request.internalNote}
                    </p>
                  )}
                </div>
              </Col>
            </Row>
            <Row />
            <Divider />
            {isApproval ? (
              <Row>
                <Col span={24}>
                  <UserBlock author={request.author} innerElement={<Space />} />
                </Col>
              </Row>
            ) : (
              <Row>
                <Col span={isMobile ? 24 : 14}>
                  <div className={styles.statusContainer}>
                    <p>
                      Статус заявки:
                      {' '}
                      <span className={styles.statusText}>
                        {request?.status && CargoRequestStatusesTitles[request?.status]}
                      </span>
                    </p>
                    <a
                      className={styles.modalText}
                      href="#"
                      onClick={e => {
                        e.preventDefault();
                        setModalTimeline(true);
                      }}
                    >
                      Подробнее
                    </a>
                  </div>
                  <Stepper steps={7} active={activeStep} />
                  <CargoModal
                    visible={modalTimeline}
                    title="Детальный статус заявки"
                    onCancel={() => setModalTimeline(false)}
                  >
                    <Timeline mode="left">
                      {history.map((cargoHistoryType, index) => (
                        <HistoryTimeline
                          key={cargoHistoryType.status}
                          cargoHistoryType={cargoHistoryType}
                          isLast={history.length - 1 === index}
                        />
                      ))}
                    </Timeline>
                  </CargoModal>
                </Col>
                <Col span={isMobile ? 24 : 8} offset={isMobile ? 0 : 2}>
                  <Row>
                    <Col span={8}>
                      <div className={styles.totalBlockTitle}>
                        <span>Срок доставки</span>
                        <div className={styles.totalBlockValue}>{deliveryTime}</div>
                      </div>
                    </Col>
                    <Col span={8}>
                      <div className={styles.totalBlockTitle}>
                        <span>Расстояние</span>
                        <div className={styles.totalBlockValue}>{distance}</div>
                      </div>
                    </Col>
                    <Col span={8}>
                      <div className={styles.totalBlockTitle}>
                        <span>Стоимость</span>
                        <div className={styles.totalBlockValue}>
                          {cost}
                          {' '}
                          руб.
                        </div>
                      </div>
                    </Col>
                  </Row>
                </Col>
              </Row>
            )}
          </CargoMainInnerContent>
          <CargoMainInnerContent>
            <Row>
              <Col className={styles.addressCol}>
                <AddressBlockMultipleDetailed
                  waypoints={request?.waypoints}
                  employees={[request?.sender, request?.recipient, request?.recipient2, request?.recipient3]}
                  senderOrganization={request?.senderOrganization}
                  recipientOrganization={request?.recipientOrganization}
                />
              </Col>
              <Col className={styles.addressCol}>
                <MapComponent
                  markers={(request?.waypoints as WaypointModel[]).filter(point => point.isValid)}
                  polylines={request?.segments as Segment[]}
                  className={styles.map}
                  dragging={true}
                  zoomControl={true}
                />
              </Col>
            </Row>
          </CargoMainInnerContent>
          {isRegular && (
            <CargoMainInnerContent>
              <Col className={styles.addressCol}>
                <ScheduleDetailed period={request.period} />
              </Col>
            </CargoMainInnerContent>
          )}
          <CargoMainInnerContent>
            <CargoList
              cargoDetails={request.listCargo || request.cargoDetails}
              senderAddress={request?.waypoints[0]?.addressString as string}
              receiverAddress={request?.waypoints[1]?.addressString as string}
              occupiedPlacesCount={request?.occupiedPlacesCount as number}
              volume={request.volume as number}
              weight={request.weight as number}
              isRegular={isRegular}
            />
          </CargoMainInnerContent>
          <CargoMainInnerContent>
            <QrCodesSection qrs={request.qrs} />
          </CargoMainInnerContent>
          {!isMobile && (
            <CargoMainInnerContent>
              <div className={styles.ttnRow}>Подтверждающие документы</div>
              <Row>
                <Col span={8}>
                  <FileComponent status={request.status} />
                </Col>
                {request.status && DRIVER_INFO_STATUSES.has(request.status) && (
                  <Col span={16} className={styles.driverInfoCol}>
                    <Row gutter={[16, 16]}>
                      <Col span={12}>
                        <div className={styles.driverField}>
                          <span className={styles.driverLabel}>ФИО водителя:</span>
                          <span className={styles.driverValue}>{request?.carInfo?.carDriver || emptySign}</span>
                        </div>
                      </Col>
                      <Col span={12}>
                        <div className={styles.driverField}>
                          <span className={styles.driverLabel}>Телефон водителя:</span>
                          <span className={styles.driverValue}>{request?.carInfo?.carDriverPhone || emptySign}</span>
                        </div>
                      </Col>
                      <Col span={12}>
                        <div className={styles.driverField}>
                          <span className={styles.driverLabel}>Марка авто:</span>
                          <span className={styles.driverValue}>{request?.carInfo?.auto || emptySign}</span>
                        </div>
                      </Col>
                      <Col span={12}>
                        <div className={styles.driverField}>
                          <span className={styles.driverLabel}>Номер авто:</span>
                          <span className={styles.driverValue}>{request?.carInfo?.registrationNumber || emptySign}</span>
                        </div>
                      </Col>
                    </Row>
                  </Col>
                )}
              </Row>
            </CargoMainInnerContent>
          )}
          {request.requestType === RequestType.RELOCATION && (
            <>
              {!!request?.loaders && (
                <CargoMainInnerContent>
                  <div className={styles.ttnRow}>Дополнительные услуги</div>
                  <Col span={8}>
                    {`Грузчики x ${request?.loaders}`}
                  </Col>
                </CargoMainInnerContent>
              )}
              {!!request.packages?.length && (
                <CargoMainInnerContent>
                  <div className={styles.ttnRow}>Упаковка</div>
                  <PackagesSection packages={request.packages} />
                </CargoMainInnerContent>
              )}
            </>
          )}
          <CargoComment comment={request.comment} />
          <CargoEngComment comment={request?.commentEng} />
        </CargoMainContentFull>
        <OrderCancelModal
          form={form}
          reason={reason}
          visible={showDeleteModal}
          textAreaVisible={textAreaVisible}
          onOk={handleConfirmModal}
          onCancel={handleCancelModal}
          onSelect={handleSelectModal}
          onChange={handleChangeReason}
        />
      </CargoContainer>
    );
  }
);

export default CargoRequestContent;
