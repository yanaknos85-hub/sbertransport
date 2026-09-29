import React, {
  ChangeEvent, FC, useEffect, useMemo, useState
} from 'react';
import { observer } from 'mobx-react';
import {
  Table,
  Input,
  Button,
  Row,
  Col,
  InputNumber,
  DatePicker,
  notification,
} from 'antd';
import { EditOutlined, SaveOutlined } from '@ant-design/icons';
import moment from 'moment';

import { useTranslation } from 'i18n';
import { WaypointModel } from 'stores/Geo/models/Waypoint.model';
import { Waypoint } from 'stores/Geo/Geo.interface';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { formatVolume } from 'utils/formatVolume';
import { DATE_FORMAT, emptySign } from 'constants/constants.app';
import { MapComponent } from 'shared/components/Map/MapComponent';
import { useOrganizationContext } from 'context/Organization.context';
import { useForceOrder } from 'api/order-execution';

import { ReactComponent as Cross } from 'modules/Planner/images/crossIcon.svg'
import { OrderTitle, Statuses, STATUSES, StatusNames } from '../../../constants/Cargo/Cargo';
import { CargoDetails, OrderDetailedProps, OrderParams, Point } from '../../../interfaces/Orders.types';
import { Section, Item, ItemComment } from '../../Item';
import OrderStatus from '../../OrderStatus';
import OrderHead from '../Header';
import { AddressBlockDetailed } from './AddressBlockDetailed';
import { OrderWaypointType, RouteWaypointType } from 'modules/OrderExecution/types';
import { TariffSection } from '../TariffCard/TariffCard';
import { QrsSection } from './QrsSection';
import { AddAdditionalContactModal } from './AddAdditionalContactModal';

import styles from './styles.module.scss';

const ShowOrder: FC<OrderDetailedProps> = props => {
  const { data, className } = props;
  const { cargoStore } = useAppStoreContext();
  const { changeOrder, tariffsListMulti, calculateAllTariffsMulti, clearTariffs, changeEngineerComment, addAdditionalContact } = cargoStore;
  const { organizationId } = useOrganizationContext();
  const [isEditMode, setIsEditMode] = useState(false);
  const [comment, setComment] = useState(data.commentEng || '');
  const [loaders, setLoaders] = useState(data.loaders || 0);
  const [desireDate, setDesireDate] = useState(data.desireDate || '');
  const [tariffId, setTariffId] = useState(data.calculatedTariff.id);
  const [currentOrderId, setCurrentOrderId] = useState<string | undefined>(data.id);
  const [isAddContactModalVisible, setIsAddContactModalVisible] = useState(false);
  const [selectedWaypointId, setSelectedWaypointId] = useState<string | undefined>(undefined);
  const { t } = useTranslation();
  const [forceOrder] = useForceOrder();

  const buttonText = 'Сохранить';
  const currentStatus = STATUSES.find(item => item.rusName === String(data.status));

  const handleCommentChange = (e: ChangeEvent<HTMLTextAreaElement>) => {
    setComment(e.target.value.slice(0, 500));
  };

  const editableStatus = currentStatus?.rusName === StatusNames.CARGO_APPROVED || currentStatus?.rusName === StatusNames.CARGO_AWAITING_APPROVAL;

  const getFinalStatusProps = () => {
    const finalStatuses = [
      StatusNames[Statuses.CARGO_SHIPMENT_FINISHED], // 'Доставлено'
      StatusNames[Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED], // 'Завершено'
      StatusNames[Statuses.CARGO_CANCELED], // 'Отменено'
    ];

    const isFinal = finalStatuses.includes(String(data?.status));

    return {
      disabled: isFinal,
      titleTooltip: isFinal ? 'Недоступно для статусов: Доставлено, Завершено, Отменено' : ''
    };
  };

  const title = useMemo(() => {
    const { disabled, titleTooltip } = getFinalStatusProps();
    return (
      <OrderHead caption={`Заявка ${data.humanReadableId}`}>
        <Button
          disabled={disabled}
          title={titleTooltip}
          onClick={() => {
          forceOrder({ orderId: data.humanReadableId })
          .then(() => {
            notification.success({
              message: `Заявка ${data.humanReadableId} отправлена контрагенту`,
            });
          })
          .catch((error) => {
            notification.error({
              message: `${error.response.data.message}`,
            })
          });
        }}>
          Принудительная отправка
        </Button>
      </OrderHead>
    )
  },
    [data.humanReadableId]
  );

  const columnsPackages = [
    {
      title: 'Наименование упаковки',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Количество, шт.',
      dataIndex: 'count',
      key: 'count',
    },
  ];

  const tablePackages = [
    {
      name: data?.packages?.name,
      count: data?.packages?.count,
    },
  ];

  const columnsCargo = [
    {
      title: 'Наименования груза',
      dataIndex: 'cargoName',
      key: 'cargoName',
    },
    {
      title: 'Тип груза',
      dataIndex: 'cargoType',
      key: 'cargoType',
    },
    {
      title: 'Габариты груза, м³',
      dataIndex: 'volume',
      key: 'volume',
    },
    {
      title: 'Вес груза, кг',
      dataIndex: 'weight',
      key: 'weight',
    },
    {
      title: 'Количество грузовых мест',
      dataIndex: 'occupiedPlacesCount',
      key: 'occupiedPlacesCount',
    },
  ];

  const tableCargo
    = Array.isArray(data.cargoDetails) && data.cargoDetails.length
      ? data.cargoDetails.map((cargo: CargoDetails) => {
        const volumeToCubicMeters = cargo.volume ? formatVolume(cargo.volume).replace('.', ',') : emptySign;
        const weight = cargo.weight ? cargo?.weight.toString().replace('.', ',') : emptySign;
        return {
          cargoName: cargo?.cargoName ? cargo?.cargoName : emptySign,
          cargoType: cargo?.cargoType,
          volume: volumeToCubicMeters,
          weight: weight,
          occupiedPlacesCount: cargo?.occupiedPlacesCount,
        };
      })
      : [];

  const costToRubles = String(data?.calculatedTariff?.cost / 100).replace('.', ',');
  const totalVolume = formatVolume(data?.totalSizes.volume).replace('.', ',');
  const transformedWaypoints: Waypoint[]
    = data?.waypoints?.map((waypoint: RouteWaypointType | OrderWaypointType) => ({
    latitude: waypoint.latitude,
    longitude: waypoint.longitude,
  })) || [];

  const handleSave = () => {
    const params: Partial<OrderParams> = {};
    if (loaders !== data.loaders) {
      params.loaders = loaders;
    }
    if (desireDate !== data.desireDate) {
      params.desireDate = desireDate;
    }
    if (data.calculatedTariff?.id !== tariffId) {
      params.calculatedTariff = tariffsListMulti.find((tariff) => tariff.id === tariffId) || '';
    }
    if (Object.keys(params).length > 0) {
      changeOrder(data.humanReadableId, params)
      .then(() => {
        setIsEditMode(false);
      })
      .catch (() => {
        setIsEditMode(false)
      })
      .finally(() =>{
        cargoStore.getCargoOrderActive(data.humanReadableId, data.source);
      })
    } else {
      setIsEditMode(false);
    }
  };

  const cancelEdit = () => {
    setIsEditMode(false);
  };

  const { waypoints = [] } = data;

  useEffect(() => {
    if(!isEditMode) return;
    setDesireDate(data.desireDate);
    if (currentOrderId !== data.id) {
      setCurrentOrderId(data.id);
      clearTariffs();
    }
    let startPoint = { ...waypoints[0], type: undefined };
    let stopPoint = { ...waypoints[waypoints.length - 1], type: undefined };
    calculateAllTariffsMulti({
      organizationId: organizationId || '',
      startPoint: startPoint as unknown as Point,
      stopPoint: stopPoint as unknown as Point,
      time: 0,
      distance: data.calculatedTariff?.distance || 0,
      weight: data.totalSizes.weight || 0,
      volume: data.totalSizes.volume || 0,
      countPoint: waypoints.length,
      loaders: data.loaders,
      packages: Array.isArray(data.packages) ? data.packages : [],
      tripDate: moment(data.desireDate, DATE_FORMAT.DATE_WITH_TIME_DOTS).format(DATE_FORMAT.DATE_WITH_TIME_ISO_SECONDS_SPACE),
      cargoCategory: data.cargoDetails?.[0]?.cargoCategory,
    })
  }, [isEditMode, data.id]);

  useEffect(() => {
    setComment(data.commentEng || '');
    setLoaders(data.loaders || 0)
    setDesireDate(data.desireDate || '');
    setTariffId(data.calculatedTariff.id);
  }, [data.id]);

  useEffect(() => {
    // Сбрасываем выбранный waypoint при изменении данных заявки
    if (!isAddContactModalVisible) {
      setSelectedWaypointId(undefined);
    }
  }, [data.id]);

  return (
    <>
      <Section className={`${className}__head`} title={title}>
        <Item title={OrderTitle.tariffId}>{data.calculatedTariff.transportType?.nameRus || data.transportTypeRus}</Item>
        <Item title={OrderTitle.status}>
          <OrderStatus status={currentStatus} />
        </Item>
        <Item title={OrderTitle.creationTime}>{data.creationTime || emptySign}</Item>
        <Item title={OrderTitle.plannedShipmentTime}>
        {isEditMode ? (
          <DatePicker
            showTime
            format = {DATE_FORMAT.DATE_WITH_TIME_DOTS}
            value={moment(desireDate, DATE_FORMAT.DATE_WITH_TIME_DOTS) }
            onChange={value => setDesireDate((value || moment()).format(DATE_FORMAT.DATE_WITH_TIME_DOTS))}
          />
        ) : (
          data.desireDate || emptySign
        )}
        </Item>
        {!editableStatus || data.editable === true || data.source === 'HOME_CLICK' ? null : (
           <div className={styles.editIconBlock}>
            {!isEditMode ? (
              <EditOutlined className={styles.icons} onClick={(e) => setIsEditMode(true)} />
            ) : (
              <Cross className={styles.icons} onClick={(e) => setIsEditMode(false)} />
            )}
        </div>
     )}
      </Section>
      {isEditMode && (
        <Section title={OrderTitle.tariff}>
          <TariffSection tariffs={tariffsListMulti} tariffId={tariffId} setTariffId={setTariffId} />
        </Section>
      )}
      <Section title={OrderTitle.initiator}>
        <Item title={OrderTitle.fullName}>{data?.author.fullName || emptySign}</Item>
        <Item title={OrderTitle.mobilePhone}>{formatPhoneNumber(data?.author.mobilePhone)}</Item>
        <Item title={OrderTitle.department}>{data?.author.department || emptySign}</Item>
        <Item title={OrderTitle.approvedBy}>{data?.author.approvedBy || emptySign}</Item>
      </Section>

      <Section title={OrderTitle.cargo}>
        <Table
          columns={columnsCargo}
          dataSource={tableCargo}
          pagination={false}
          tableLayout="fixed"
        />
      </Section>

      <Section title={OrderTitle.additionalServices}>
      <Item title={OrderTitle.loaders}>
        {isEditMode ? (
          <InputNumber
            value={loaders}
            onChange={value => {
              if (typeof value === 'number' && value >= 0 && value <= 9) {
                setLoaders(value);
              }
            }}
            onKeyPress={e => {
              if (!/[0-9]/.test(e.key)) {
                e.preventDefault();
              }
            }}
            maxLength={1}
          />
        ) : (
          data?.loaders ? data.loaders : emptySign
        )}
        </Item>
        <Table
          columns={columnsPackages}
          dataSource={tablePackages}
          pagination={false}
          tableLayout="fixed"
        />
      </Section>

      <Section title={OrderTitle.delivery}>
        <Item title={OrderTitle.transferTime}>
          {' '}
          {data?.transferTime || emptySign}
        </Item>
        <Item title={OrderTitle.shipmentTime}>
          {' '}
          {data?.shipmentTime || emptySign}
        </Item>
        <Item title={OrderTitle.distance}>
          {data?.calculatedTariff?.distance ? data?.calculatedTariff?.distance.toString().replace('.', ',') : emptySign}
        </Item>
        <Item title={OrderTitle.cost}>
          {' '}
          {costToRubles || emptySign}
        </Item>
        <Item title={OrderTitle.courier}>
          {' '}
          {emptySign}
        </Item>
        <Item title={OrderTitle.totalWeight}>
          {' '}
          {data?.totalSizes.weight || emptySign}
        </Item>
        <Item title={OrderTitle.totalVolume}>
          {' '}
          {totalVolume || emptySign}
        </Item>
        <Section className={styles.fullWidth} title={OrderTitle.route}>
          <Row>
            <Col xs={24} sm={24} md={12} lg={12} xl={12}>
              <AddressBlockDetailed
                waypoints={data?.waypoints || []}
                {...getFinalStatusProps()}
                onAddAdditionalContact={(waypointId) => {
                  setSelectedWaypointId(waypointId);
                  setIsAddContactModalVisible(true);
                }}
              />
            </Col>
            <Col xs={24} sm={24} md={12} lg={12} xl={12}>
              <div className={styles.tripDetailedView}>
                <div className={styles.mapWrapper}>
                  <MapComponent
                    markers={transformedWaypoints as WaypointModel[]}
                    polylines={data?.segments}
                    className={styles.map}
                    dragging
                    zoomControl
                    fitToShowAllGeometry
                  />
                </div>
              </div>
            </Col>
          </Row>
        </Section>
      </Section>

      <QrsSection qrs={data.qrs} />

      <Section title={OrderTitle.contractor}>
        <Item title={OrderTitle.car}>{data?.carInfo?.auto || emptySign}</Item>
        <Item title={OrderTitle.driver}>{data?.carInfo?.carDriver || emptySign}</Item>
        <Item title={OrderTitle.driverPhone}>{formatPhoneNumber(data?.carInfo?.carDriverPhone || emptySign)}</Item>
        <Item title={OrderTitle.tariffContractor}>{data?.calculatedTariff?.contragent || emptySign}</Item>
        <Item title={OrderTitle.carNumber}>{data?.carInfo?.registrationNumber || emptySign}</Item>
      </Section>

      <Section title={OrderTitle.commentOrder}>
        <ItemComment>{data.comment ? data.comment : '-'}</ItemComment>
      </Section>

      <Section title={OrderTitle.commentEng}>
        <div className="comment-section">
          {data.status !== StatusNames[Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED] ? (
            <>
              <ItemComment>
                <Input.TextArea
                  className="textarea-comment"
                  autoSize
                  value={comment}
                  onChange={handleCommentChange}
                  maxLength={500}
                  placeholder="Введите комментарий"
                  showCount
                />
              </ItemComment>
              <Button
                onClick={async () => {
                  try {
                    await changeEngineerComment(data.humanReadableId, comment);
                    await cargoStore.getCargoOrderActive(data.humanReadableId, data.source);
                  } catch (error) {
                    return;
                  }
                }}
                icon={<SaveOutlined />}
                size="middle"
                htmlType="submit"
              >
                {buttonText}
              </Button>
            </>
          ) : (
            <div>{comment}</div>
          )}
        </div>
      </Section>
      {isEditMode && (
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
          <Button type="text" onClick={cancelEdit}>
            {t.global.cancel}
          </Button>
          <Button
            htmlType="submit"
            type="primary"
            onClick={handleSave}
            icon={<SaveOutlined />}
          >
            {t.global.save}
          </Button>
        </div>
      )}

      <AddAdditionalContactModal
        visible={isAddContactModalVisible}
        onClose={() => {
          setIsAddContactModalVisible(false);
          setSelectedWaypointId(undefined);
        }}
        onConfirm={(payloadArray) => {
          addAdditionalContact(data.humanReadableId, payloadArray)
            .then(() => {
              setTimeout(() => {
                cargoStore.getCargoOrderActive(data.humanReadableId, data.source);
              }, 1000);
            })
        }}
        waypointId={selectedWaypointId || ''}
      />
    </>
  );
};

export default observer(ShowOrder);
