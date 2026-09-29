import React, {
  Suspense, useEffect, useState, useMemo
} from 'react';
import { observer } from 'mobx-react';
import {
  Divider, Drawer, Modal, Pagination, Form,
  InputNumber
} from 'antd';
import moment from 'moment';

import { TripsTabsFilters } from 'modules/EmployeeApp/EmployeeApp.constants';
import { type TabsNavOption, TabsNav } from 'shared/components/TabsNav';
import { RenderTripYandexListItem } from './TripYandexList/renderTripYandexListItem';
import { SpinWrapped } from 'shared/components';
import { TripStatusAlert } from './components/TripStatusAlert/TripStatusAlert';

import { useRouteParamSub } from 'shared/hooks/useRouteParamSub';
import { useTabsNavProps } from 'shared/components/TabsNav/useTabsNavProps';
import { useRequestFilterProps } from 'shared/components/RequestFilter/useRequestFilterProps';
import TButton from 'shared/ui/Button/Button';
import RouteMap from 'utils/RouteMap/RouteMap';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useYandexTaxiTripsQuery, useYandexTripFieldUpdate } from 'api/yandexTaxi/yandex-taxi.api';
import { YandexTaxiRequest } from 'api/yandexTaxi/yandex-taxi.types';
import { YandexTaxiRequestStatus } from 'api/yandexTaxi/yandex-taxi.constants';
import { StoreNames } from 'stores/StoreNames.enum';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { plainToNew } from 'utils';
import { DATE_FORMAT, RUBLE_SIGN } from 'constants/constants.app';
import { TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripFraudMarker } from '../TripsPage/TripRequestDetailedView/TripFraudMarker/TripFraudMarker';

import { ValidationRules } from 'shared/fieldValidationRules';
import { TRIPS_YANDEX_ACTIVE_STATUSES, TRIPS_YANDEX_FINISHED_STATUSES } from './constants/constants';

import styles from './TripsYandexPage.module.scss';
import { getFraudComments } from 'utils/fraud/getFraudComments';

const tabs: TabsNavOption[] = [
  {
    key: TripsTabsFilters.planned,
    label: 'Активные',
  },
  {
    key: TripsTabsFilters.final,
    label: 'Завершённые',
  },
];

const requestStatuses = {
  [TripsTabsFilters.planned]: TRIPS_YANDEX_ACTIVE_STATUSES,
  [TripsTabsFilters.final]: TRIPS_YANDEX_FINISHED_STATUSES,
};

export const TripsYandexPage = observer(() => {
  const {
    [StoreNames.tripStore]: tripStore,
    [StoreNames.corporateStore]: corporateStore,
  } = useAppStoreContext();
  const [current, setPage] = React.useState(1);
  const [pageSize, setPageSize] = React.useState(10);
  const [total, setTotal] = useState(0);

  const tabProps = useTabsNavProps();
  const filterProps = useRequestFilterProps();

  const {
    setStatusFilter,
  } = filterProps;
  const { activeTab } = tabProps;

  const isFinishedStatus = activeTab === TripsTabsFilters.final;

  const [statusFilter] = useRouteParamSub('filter', { options: Object.values(TripsTabsFilters), isStrict: true });
  const [updateTripStatus, { isLoading: isTripUpdateLoading }] = useYandexTripFieldUpdate();

  useEffect(() => {
    setStatusFilter(statusFilter);
  });

  useEffect(() => {
    corporateStore.loadAllPositions(tripStore.selfEmployee.organizationId);
  }, []);

  const handleSizeChange = (_: number, sizePage: number) => setPageSize(sizePage);

  const handleClearFilters = () => {
    setPage(1);
  };

  const yandexTaxiRequestParams = useMemo(() => {
    return ({
      requestStatusSet: requestStatuses[activeTab],
      sort: 'humanReadableId',
      pageSetting: {
        page: current > 0 ? current - 1 : current,
        size: pageSize,
      },
      sortSetting: {
        property: 'humanReadableId',
      },
    });
  }, [current, pageSize, activeTab]);

  const {
    data: { content, page }, isLoading,
  } = useYandexTaxiTripsQuery(yandexTaxiRequestParams);

  useEffect(() => {
    setTotal(page.total);
  }, [page]);

  useEffect(() => {
    tripStore.initStore();
  }, []);

  const [form] = Form.useForm();

  const purposes = tripStore.purposes;

  const mappedPurposes: Record<string, string> = useMemo(() => {
    if (!purposes || !purposes.length) return {};

    return purposes.reduce((acc, purpose) => {
      return {
        ...acc,
        [purpose.id]: purpose.label,
      };
    }, {} as Record<string, string>);
  }, [tripStore.purposes]);

  const [isCancelModalOpen, setCancelModalOpen] = useState(false);
  const [isConfirmDrawerOpen, setConfirmDrawerOpen] = useState(false);
  const [activeTrip, setActiveTrip] = useState<YandexTaxiRequest | null>(null);

  const isAbleToCancelTrip = !!activeTrip && [YandexTaxiRequestStatus.CONFIRMATION, YandexTaxiRequestStatus.CONFIRMED].includes(activeTrip.status as YandexTaxiRequestStatus);
  const isButtonsAvailable = !isFinishedStatus && activeTrip?.status !== YandexTaxiRequestStatus.ORDER_PAYMENT_FORMATION;

  const onCancel = (request: YandexTaxiRequest) => {
    setActiveTrip(request);
    setCancelModalOpen(true);
  };

  const handleResetActiveRequest = () => {
    setActiveTrip(null);
  };

  const handleOnCloseDrawer = () => {
    setConfirmDrawerOpen(false);
    handleReset();
  };

  const handleConfirm = (request: YandexTaxiRequest) => {
    setActiveTrip(request);
    setConfirmDrawerOpen(true);
  };

  const handleReset = () => {
    form.resetFields();
    setActiveTrip(null);
    setConfirmDrawerOpen(false);
    setCancelModalOpen(false);
  };

  const onCancelTrip = () => {
    if (!activeTrip) return;

    updateTripStatus({
      id: activeTrip.id!, fields: [{ value: YandexTaxiRequestStatus.CANCELLED, path: '/status' }],
    });

    handleReset();
  };

  const onModalClose = () => {
    setCancelModalOpen(false);
    handleResetActiveRequest();
  };

  const onFinish = (values: { factCost: number }) => {
    form.validateFields().then(() => {
      if (!activeTrip) return;

      updateTripStatus({
        id: activeTrip.id!, fields: [
          { value: YandexTaxiRequestStatus.CONFIRMATION, path: '/status' },
          { value: values?.factCost, path: '/factCost' },
        ],
      });
    });

    handleReset();
  };

  const isAbleToExtend = [YandexTaxiRequestStatus.NEW, YandexTaxiRequestStatus.CONFIRMATION_NEEDED].includes(activeTrip?.status as YandexTaxiRequestStatus);

  const plannedCostElement = (activeTrip?.plannedCost || activeTrip?.factCost) && (
    <div>
      {activeTrip?.plannedCost && (
        <p className={styles.plannedCostTitle}>
          Плановая стоимость
          <span>{`${activeTrip?.plannedCost} ${RUBLE_SIGN}`}</span>
        </p>
      )}
      {activeTrip?.factCost && (
        <p className={styles.factCostTitle}>
          Стоимость поездки
          <span>{`${activeTrip?.factCost} ${RUBLE_SIGN}`}</span>
        </p>
      )}
    </div>
  );

  const submitButtonTitle = activeTrip?.status === YandexTaxiRequestStatus.CONFIRMATION_NEEDED
    ? 'Подтвердить заявку'
    : 'Создать заявку';

  const fraudComments = getFraudComments(activeTrip?.fraudComment);
  const hasFraud = fraudComments.length !== 0;

  return (
    <div className={styles.wrapper}>
      <TabsNav
        currentKey={statusFilter}
        options={tabs}
        defaultActiveTab={tabs[0]}
        onTabClick={handleClearFilters}
        {...tabProps}
      />
      <>
        {
            isLoading && (
            <div style={{ height: 200 }}>
              <SpinWrapped />
            </div>
            )
        }
        <Drawer
          open={isConfirmDrawerOpen && !!activeTrip}
          onClose={handleOnCloseDrawer}
          placement="bottom"
          className={styles.tripDrawer}
          destroyOnClose
        >
          <Form form={form} onFinish={onFinish}>
            <div className={styles.header}>

              {hasFraud && (
              <>
                <TripFraudMarker />
                <Divider className={styles.fraudDivider} />
              </>
              )}
              <p className={styles.title}>
                Поездка №
                {activeTrip?.humanReadableId}
              </p>
              <span className={styles.date}>
                {moment(activeTrip?.tripDate).format(DATE_FORMAT.MONTH_NAME_NOT_YEAR_WITH_TIME)}
                {' • '}
                {mappedPurposes?.[activeTrip?.purposeId as string]}
              </span>
            </div>
            <Divider />
            <div className={styles.transportType}>
              <div className={styles.transportTypeWrapper}>
                <div className={styles.TransportPictureWrapper}>
                  <div className={styles.yandexTaxiImage} />
                </div>
                <span>{TransportTypeTitlesEnum.YANDEX}</span>
              </div>
            </div>
            <Divider />
            <div className={styles.mapWrapper}>
              <p className={styles.routeTitle}>Маршрут</p>
              {
                activeTrip?.waypoints && (
                <RouteMap
                  className={styles.routeMap}
                  addresses={plainToNew<WaypointModel[]>(WaypointModel, activeTrip!.waypoints)}
                />
                )
              }
            </div>
            {!isFinishedStatus && <Divider />}
            <div className={styles.bottomWrapper}>
              {!isFinishedStatus && isAbleToExtend ? (
                <div className={styles.inputContainer}>
                  <div className={styles.costWrapper}>
                    <Form.Item
                      name="factCost"
                      rules={[
                        ValidationRules.general.required,
                        ValidationRules.general.maxMoneyValue(50000),
                        ValidationRules.general.minMoneyValue,
                      ]}
                    >
                      <InputNumber
                        step={0.01}
                        className={styles.inputNumberPrice}
                        precision={2}
                        placeholder="Стоимость поездки, руб."
                        formatter={value => `${value}`.replace('.', ',')}
                        parser={value => Number(value?.replace(',', '.'))}
                      />
                    </Form.Item>
                    { activeTrip?.plannedCost && (
                      <div className={styles.costInputSubtitle}>{`Плановая стоимость ${activeTrip?.plannedCost} ${RUBLE_SIGN}`}</div>
                    )}
                  </div>
                  <TripStatusAlert />
                </div>
              ) : plannedCostElement}
              {
                isButtonsAvailable && (
                <div className={styles.buttonsWrapper}>
                  {!isAbleToCancelTrip && (
                  <TButton onClick={form.submit} loading={isTripUpdateLoading}>
                    {submitButtonTitle}
                  </TButton>
                  )}
                  <TButton
                    onClick={onCancelTrip}
                    loading={isTripUpdateLoading}
                    $makeLikeLink
                  >
                    Отменить
                  </TButton>
                </div>
                )
              }
            </div>
          </Form>
        </Drawer>
        <Modal
          open={isCancelModalOpen}
          onCancel={onModalClose}
          onOk={onCancelTrip}
          okText="Да"
          cancelText="Нет"
        >
          <div className={styles.modalWrapper}>
            <p className={styles.modalTitle}>
              Отменить заявку
            </p>
            <span>
              Вы уверены, что хотите отменить созданную заявку?
            </span>
          </div>
        </Modal>

        <div style={isLoading ? { display: 'none' } : { display: 'block' }}>
          <Suspense fallback={<></>}>
            <div style={isLoading ? { display: 'none' } : { display: 'block' }}>
              {content?.map(item => (
                <RenderTripYandexListItem
                  request={item}
                  key={item.id}
                  onCancel={onCancel}
                  onConfirm={handleConfirm}
                  purposeLabel={mappedPurposes?.[item.purposeId!] ?? ''}
                  isFinishedStatus={isFinishedStatus}
                />
              ))}

              <div className={styles.pagination}>
                <Pagination
                  defaultCurrent={current}
                  current={current}
                  total={total}
                  onChange={setPage}
                  hideOnSinglePage
                  pageSize={pageSize}
                  onShowSizeChange={handleSizeChange}
                  totalBoundaryShowSizeChanger={10}
                />
              </div>
            </div>
          </Suspense>
        </div>
      </>
    </div>
  );
});
