import { Button } from '@sber-sbertransport/ui-kit/src';
import React, { FC, useEffect, useMemo, useState } from 'react';
import { useUpdateLoaders, useGetSearchMonitorRoute } from 'api/planner';
import { useParams } from 'react-router-dom';
import moment from 'moment';
import { notification, Button as ButtonAntd} from 'antd';
import { FORMAT } from 'modules/Engineers/constants/Engineers.constants';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { convertToRubles, formatPhoneNumber, formatVolume, ignore } from 'utils';
import { Item, Section, SubItem, SubSection } from './Item';
import OrderDetailedHeader from './Header';
import OrderTitle, { STATUSES, StatusNames } from '../constants';
import OrderStatus from './OrderStatus';
import AddressBlock from './AddressBlock/AddressBlock';
import { EditButton } from './EditButton';
import { LoadersCount } from './LoadersCount/LoadersCount';
import { emptySign } from 'constants/constants.app';

import './styles.scss';

const ShowOrder: FC = () => {
  const { id: humanReadableId } = useParams();
  const { cargoStore } = useAppStoreContext();
  const { data, refetch } = useGetSearchMonitorRoute(humanReadableId);
  const [updateLoaders] = useUpdateLoaders(humanReadableId);
  const className = 'routeDetailed';

  const [isEditMode, setIsEditMode] = useState(false);
  const [value, setValue] = useState<number>(data.loaders);

  useEffect(() => {
    setValue(data.loaders);
  }, [data]);

  const currentStatus = STATUSES.find(item => item.rusName === String(data.status));
  const isActiveEdit = currentStatus?.rusName === StatusNames.CARGO_PLANNING || currentStatus?.rusName === StatusNames.CARGO_PLANNING_FINISHED;
  // todo: вынести в отдельный компонент
  const title = useMemo(() => (
      <OrderDetailedHeader caption={`Маршрут ${data.humanReadableId}`}>
        <div className={`${className}__controls`}>
          <ButtonAntd
            onClick={async () => {
              await cargoStore.sendRouteToContractor(data.id)
              .then(() => {
                notification.success({
                  message: `Маршрут ${data.humanReadableId} отправлен контрагенту`,
                });
                // Нужна задержка, т.к. есть таймаут обновления статуса на бэке,
                // иначе статус на фронте не успевает обновиться
                setTimeout(() => {
                  refetch();
                }, 2000)
              })
              .catch((error) => {
                notification.error({
                  message: `${error.response.data.message}`,
                })
              })
          }}>
            Отправить контрагенту
          </ButtonAntd>
          <EditButton
            activeEdit={isActiveEdit}
            editMode={isEditMode}
            setIsEditMode={setIsEditMode}
          /></div>
      </OrderDetailedHeader>
    ),
    [
      data.humanReadableId,
      isEditMode
    ]);

  const cancelEdit = () => {
    setIsEditMode(false);
    setValue(data.loaders);
  };

  const onChangeLoaders = (value) => {
    setValue(value);
  };

  return (
    <>
      <Section className={`${className}__head`} title={title}>
        <Item title={OrderTitle.humanReadableId}>{data.humanReadableId}</Item>
        <Item title={OrderTitle.status}>
          <OrderStatus status={currentStatus} />
        </Item>
        <Item title={OrderTitle.creationTime}>{moment(data.creationTime).locale('ru').format(FORMAT) || emptySign}</Item>
        <Item title={OrderTitle.departureTime}>{moment(data.desiredDate).locale('ru').format(FORMAT) || emptySign}</Item>
        <Item title={OrderTitle.shipmentTime}>
          {data.actualShipmentTime ? moment(data.actualShipmentTime).utcOffset(0).locale('ru').format(FORMAT) : emptySign}
        </Item>
        <Item title={OrderTitle.author}>{data.author}</Item>
      </Section>
      <Section title={OrderTitle.contractor}>
        <Item title={OrderTitle.contractorName}>{data.contractorName || emptySign}</Item>
        <Item title={OrderTitle.driver}>{data.driver || emptySign}</Item>
        <Item title={OrderTitle.driverPhone}>{formatPhoneNumber(data.driverPhone) || emptySign}</Item>
        <Item title={OrderTitle.registrationNumber}>{data.registrationNumber || emptySign}</Item>
        <Item title={OrderTitle.capacity}>{data.capacity || emptySign}</Item>
        <Item title={OrderTitle.autoVolume}>{data.autoVolume || emptySign}</Item>
      </Section>
      <Section title={OrderTitle.additionalServices}>
        {isEditMode ? (
          <LoadersCount value={value} onChange={onChangeLoaders} />
        ) : (
          <Item title={OrderTitle.loaders}>{data?.loaders ? data.loaders : emptySign}</Item>
        )}
      </Section>
      <Section title={OrderTitle.applications}>
        {data.requestsForOto.map((order, index) => (
          <SubSection key={order.id} title={`${index + 1}. ${order.humanReadableId}`}>
            <div className="order-content">
              <div className="order-content-left">
                <AddressBlock from={order.addressFrom} to={order.addressTo} />
              </div>
              <div className="order-content-right">
                <SubItem title={OrderTitle.cargoType}>{order.cargoType || emptySign}</SubItem>
                <SubItem title={OrderTitle.occupiedPlacesCount}>{order.occupiedPlacesCount || emptySign}</SubItem>
                <SubItem title={OrderTitle.weightOfOne}>
                  {order.weight ?
                    `${Number(order.weight.toFixed(3)).toString().replace('.', ',')} кг`
                    : emptySign
                  }
                </SubItem>
                <SubItem title={OrderTitle.volumeOfOne}>
                  {order.volume ? `${formatVolume(order.volume).replace('.', ',')} м³` : emptySign}
                </SubItem>
              </div>
            </div>
            <div className="time-content">
              <Item title={OrderTitle.desiredDate}>
                {moment(order.desiredDate).startOf('day').locale('ru').format(FORMAT) || emptySign}
              </Item>
              <Item title={OrderTitle.shippingDate}>
                {order.deliveryTimeDate ? moment(order.deliveryTimeDate).locale('ru').format(FORMAT) : emptySign}
              </Item>
              <Item title={OrderTitle.factDesiredDate}>
                {order.transferTime ? moment(order.transferTime).locale('ru').format(FORMAT) : emptySign}
              </Item>
              <Item title={OrderTitle.factShippingDate}>
                {order.shipmentTime ? moment(order.shipmentTime).locale('ru').format(FORMAT) : emptySign}
              </Item>
            </div>
          </SubSection>
        ))}
      </Section>
      <Section title={OrderTitle.additionalInfo}>
        <Item title={OrderTitle.cost}>{convertToRubles(data.cost).toFixed(2).toString().replace('.', ',') || emptySign}</Item>
        <Item title={OrderTitle.actualCost}>{data?.actualCost ? convertToRubles(data.actualCost).toFixed(2).toString().replace('.', ',') : emptySign}</Item>
        <Item title={OrderTitle.weight}>{data.weight ? `${Number(data.weight.toFixed(3)).toString().replace('.', ',')} кг` : emptySign}</Item>
        <Item title={OrderTitle.volume}>{data.volume ? `${formatVolume(data.volume).replace('.', ',')} м³` : emptySign}</Item>
        <Item title={OrderTitle.distance}>{data.distance?.toFixed(1).toString().replace('.', ',') || emptySign}</Item>
        <Item title={OrderTitle.actualDistance}>{data.actualDistance?.toFixed(1).toString().replace('.', ',') || emptySign}</Item>
        <Item title={OrderTitle.creationType}>{data.creationType || emptySign}</Item>
      </Section>
      {isEditMode && (
        <div className={`${className}__actions`}>
          <Button type="text" onClick={cancelEdit}>
            Отмена
          </Button>
          <Button
            htmlType="submit"
            type="primary"
            onClick={() => {
              updateLoaders({ requestId: humanReadableId, loaders: value })
                .then(() => {
                  setIsEditMode(false);
                })
                .catch(ignore)
            }}
          >
            Сохранить
          </Button>
        </div>
      )}
    </>
  )
};

export default ShowOrder;
