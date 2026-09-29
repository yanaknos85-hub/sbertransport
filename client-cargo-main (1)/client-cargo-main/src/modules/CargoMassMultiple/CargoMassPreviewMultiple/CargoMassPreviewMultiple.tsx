/* eslint-disable react-hooks/exhaustive-deps */
/* eslint-disable arrow-body-style */
import React, {
  FC, useEffect, useRef, useState
} from 'react';
import { notification } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import { SpinWrapped } from 'shared/components';
import { emptySign } from 'shared/constants/constants';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { getVolumeWithoutUnits } from 'utils';

import { ICargoMassStoreMultiple, RequestStatus } from 'stores/CargoMassMultiple/CargoMassMultiple.interface';
import type { CargoListItem } from 'stores/Cargos/typesMulti';
import { StoreNames } from 'stores/StoreNames.enum';
import { DATE_FORMAT } from 'constants/constants.app';

import { Field, namedField } from '../constants';
import * as S from './CargoMassPreviewMultiple.style';

interface Props {
  onConfirm: () => void;
  onClose: () => void;
}

const CargoMassPreviewMultiple: FC<Props> = observer(({ onConfirm, onClose }) => {
  const { [StoreNames.cargoMassStoreMultiple]: cargoMassStoreMultiple }: { cargoMassStoreMultiple: ICargoMassStoreMultiple } = useAppStoreContext();

  const { requestList = [] } = cargoMassStoreMultiple;

  const [preloader, setPreloader] = useState(true);
  const [error, setError] = useState(false);
  const [errorStatus, setErrorStatus] = useState<boolean>(false);

  const timer = useRef<any>(null);
  const isDescription = requestList.map(item => item.description).some(description => description !== '');

  const isDisabled = isDescription || errorStatus;

  const handleConfirm = () => {
    onConfirm();
  };

  const handleClose = () => {
    onClose();
  };

  const loadRequest = (timeoutMs?: number) => {
    timer.current = setTimeout(async () => {
      try {
        const request = await cargoMassStoreMultiple.loadRequest();
        const errorRequest = request?.data.filter(item => item.description === 'null');
        const newStatus = request?.status;

        /* todo флаг об ошибке, если поле "description" пустое,
         *       необходима доработка бэка
         */
        if (request) {
          setErrorStatus(request.data.some(item => {
            return item.status === RequestStatus.ERROR;
          }));
        }

        if (!newStatus || newStatus === RequestStatus.IN_PROGRESS) {
          loadRequest();
        } else if (newStatus === RequestStatus.ERROR) {
          setError(true);
          setPreloader(false);
          if (!request?.data?.length || errorRequest?.length) {
            setError(true);
          }
          // eslint-disable-next-line no-console
          console.error('Ошибка, внесите корректные данные', error);
        } else {
          setPreloader(false);
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error('Необработанная ошибка', error);
        setError(true);
        setPreloader(false);
      }
    }, timeoutMs || 1500); // далее таймаут будет меньше между запросами
  };

  useEffect(() => {
    setPreloader(true);
    loadRequest(1000); // первоначальная загрузка с таймаутом, чтобы реквеcт прошел

    return () => clearTimeout(timer.current);
  }, []);

  const getCargoName = (list: CargoListItem[]) => {
    return list?.reduce((acc, cur) => {
      return acc?.concat(`${cur.cargoName}, \n`);
    }, '');
  };

  const orderedData = requestList.map(itemRequest => ({
    requestID: itemRequest.id,
    fields: [
      {
        name: Field.requestId, value: itemRequest?.id, hidden: true,
      },
      {
        name: Field.externalId, value: itemRequest?.externalId, hidden: true,
      },
      { name: Field.errors, value: itemRequest.description ? itemRequest.description : emptySign },
      { name: Field.requestNumber, value: itemRequest.requestNumber ? itemRequest.requestNumber : emptySign },
      {
        name: Field.desiredDate, value: (
          itemRequest?.desiredDate ? moment(itemRequest?.desiredDate).format(DATE_FORMAT.DATE_WITH_TIME_DOTS) : ''
        ),
      },
      { name: Field.senderAddress, value: itemRequest.expected?.waypoints[0].addressStringRepresentation },
      { name: Field.senderName, value: itemRequest.sender?.fullName },
      { name: Field.senderMobilePhone, value: itemRequest.sender?.mobilePhone },
      { name: Field.senderOrganization, value: itemRequest.senderOrganization },
      { name: Field.sourceLoaders, value: itemRequest?.sourceLoaders },
      { name: Field.recipientAddress, value: itemRequest.expected?.waypoints[1].addressStringRepresentation },
      { name: Field.recipientName, value: itemRequest.recipient?.fullName },
      { name: Field.recipientMobilePhone, value: itemRequest.recipient?.mobilePhone },
      { name: Field.recipientOrganization, value: itemRequest?.recipientOrganization },
      { name: Field.destinationLoaders, value: itemRequest?.destinationLoaders },
      { name: Field.approver, value: itemRequest?.approver ? itemRequest?.approver : emptySign },

      { name: Field.occupiedPlacesCount, value: itemRequest.totalSizes?.occupiedPlacesCount },
      { name: Field.cargoName, value: getCargoName(itemRequest?.listCargo) },
      { name: Field.width, value: itemRequest.totalSizes?.width },
      { name: Field.length, value: itemRequest.totalSizes?.length },
      { name: Field.height, value: itemRequest.totalSizes?.height },
      { name: Field.weight, value: itemRequest.totalSizes?.weight },
      { name: Field.volume, value: getVolumeWithoutUnits(itemRequest.totalSizes?.volume) },

      { name: Field.express, value: itemRequest?.express },
      { name: Field.comment, value: itemRequest?.comment },
    ],
  }));

  // eslint-disable-next-line @stylistic/comma-dangle
  const renderField = <T,>(name: any, value: T) => {
    switch (name) {
      case Field.sourceLoaders:
      case Field.destinationLoaders:
      case Field.express:
        return value ? 'ДА' : 'НЕТ';
      default:
        return value;
    }
  };

  if (preloader) {
    return (
      <S.SpinContainer>
        <S.SpinWrapper>
          <SpinWrapped />
        </S.SpinWrapper>
      </S.SpinContainer>
    );
  }

  /* todo сообщение об ошибке, если поле "description" пустое,
  *       необходима доработка бэка
  */
  if (errorStatus) {
    notification.error({
      message: 'Создание массовой доставки',
      description: 'Необработанная ошибка',
    });
  }

  if (error && !requestList.length) {
    return (
      <S.ModalStyled
        centered={true}
        closable={true}
        visible={true}
        onCancel={handleClose}
        title={<S.ModalTitleStyled>Ошибка!</S.ModalTitleStyled>}
        footer={<>&nbsp;</>}
      >
        <S.ModalSubTitleStyled>Проверьте корректность загружаемых данных</S.ModalSubTitleStyled>
      </S.ModalStyled>
    );
  }

  return (
    <S.ModalStyled
      width="100%"
      centered={true}
      closable={true}
      visible={true}
      title={(
        <>
          <S.ModalTitleStyled>Превью таблицы Excel</S.ModalTitleStyled>
          <S.ModalSubTitleStyled>Проверьте корректность данных перед оформлением заявки</S.ModalSubTitleStyled>
        </>
      )}
      onCancel={handleClose}
      footer={(
        <S.ButtonsStyled>
          <S.ButtonCancelStyled onClick={handleClose}>Отмена</S.ButtonCancelStyled>
          <S.ButtonApproveStyled disabled={isDisabled} onClick={handleConfirm}>
            Подтвердить и оформить доставку
          </S.ButtonApproveStyled>
        </S.ButtonsStyled>
      )}
    >
      <S.ViewportStyled>
        <S.TableStyled>
          <thead>
            <tr>
              <S.ThStyled>№</S.ThStyled>
              {orderedData[0] && orderedData[0].fields
                .filter(({ hidden }) => !hidden)
                .map(({ name }) => <S.ThStyled key={name}>{namedField[name]}</S.ThStyled>)}
            </tr>
          </thead>
          <tbody>
            {orderedData.map((cargos, i) => (
              <S.TrStyled key={cargos.requestID + i}>
                <S.TdStyled key={cargos.requestID + 1}>{i + 1}</S.TdStyled>
                {cargos.fields
                  .filter(({ hidden }) => !hidden)
                  .map(({ name, value }) => (
                    <S.TdStyled key={name + i}>{renderField(name, value)}</S.TdStyled>
                  ))}
              </S.TrStyled>
            )
            )}
          </tbody>
        </S.TableStyled>
      </S.ViewportStyled>
    </S.ModalStyled>
  );
});

export default CargoMassPreviewMultiple;
