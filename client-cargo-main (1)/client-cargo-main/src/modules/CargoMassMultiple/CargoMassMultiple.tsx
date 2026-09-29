/* eslint-disable react-hooks/exhaustive-deps */
// @ts-nocheck
import React, {
  FC, RefObject, useEffect, useMemo, useRef, useState
} from 'react';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { notification } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import { SpinWrapped } from 'shared/components';
import { CargoFixed, CargoSideInnerContent } from 'shared/components/Cargo/CargoLayout';
import Scheduler from 'shared/components/SchedulerMulti/Scheduler';
import Select from 'shared/form/Select/Select';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { ICargoMassStoreMultiple, RequestListItem, RequestStatus } from 'stores/CargoMassMultiple/CargoMassMultiple.interface';
import { AddressLoadType } from 'stores/Cargos/typesMulti';
import { TariffCost, TariffType } from 'stores/CargoTariff/CargoTariff.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeEnum, transportTypeTitles } from 'types/Cargo';
import { DATE_FORMAT } from 'constants/constants.app';
import { CARGOS, REGULAR_CARGOS } from 'constants/constants.routes';
import { TariffsOption } from 'modules/CargoMassMultiple/types';

import CargoEditMultiple from './CargoMassEdit/CargoMassEditMultiple';
import CargoItemMultiple from './CargoMassItem/CargoMassItemMultiple';
import * as S from './CargoMassMultiple.style';
import Preview from './CargoMassPreviewMultiple/CargoMassPreviewMultiple';
import { ModalStyled, ModalSubTitleStyled, ModalTitleStyled } from './CargoMassPreviewMultiple/CargoMassPreviewMultiple.style';
import UploadFileButton from './CargoMassUploadMultiple/CargoMassUploadMultiple';
import CargoTotal from './components/CargoTotal/CargoTotal';
import SortOrder, { SortType, SortTypeOrder } from './components/SortOrder/SortOrder';
import { TARIFF_SYSTEM_TYPE_LABEL, TARIFF_SYSTEM_TYPE_VALUE } from './constants';
import { Periodicity, RequestObjectMulti } from './types';

interface Props {
  tariffCost?: number;
}

const CargoListMultiple: FC<Props> = observer(() => {
  const {
    [StoreNames.selfStore]: selfStore,
    [StoreNames.cargoStore]: cargoStore,
    [StoreNames.cargoMassStoreMultiple]: cargoMassStoreMultiple,
    [StoreNames.geoStore]: { loadFromCargoMassRequestMulti },
  }: {
    cargoMassStoreMultiple: ICargoMassStoreMultiple;
  } = useAppStoreContext();

  const { isRegular } = cargoStore;
  const {
    request, requestList = [], totalData,
  } = cargoMassStoreMultiple;

  const { periodValues } = cargoStore;

  const status = request?.status;

  const history = History();
  const [preloader, setPreloader] = useState(true);
  const [error] = useState(false);
  const [showPreview, setShowPreview] = useState(false);
  const [showList, setShowList] = useState(true);
  const [allTariff, setAllTariff] = useState(TARIFF_SYSTEM_TYPE_VALUE);
  const [headerWidth, setHeaderWidth] = useState(0);
  const [sort, setSort] = useState({ type: SortType.CREATION_TIME, order: SortTypeOrder.DESC });
  const [editItem, setEditItem] = useState<RequestListItem | null>(null);
  const [schedulerError, setSchedulerError] = useState(false);

  const contentRef: RefObject<HTMLDivElement> = useRef(null);
  const timer = useRef<any>(null);

  const SOURCE_APP = 'WEB';

  const loadRequest = (timeoutMs?: number) => {
    timer.current = setTimeout(async () => {
      const request = await cargoMassStoreMultiple.loadRequest();
      const newStatus = request?.status;

      if (!newStatus || newStatus === RequestStatus.IN_PROGRESS) {
        loadRequest();
      } else if (newStatus === RequestStatus.ERROR) {
        notification.error({
          message: 'Создание массовой доставки!',
          description: request?.message || 'Что-то пошло не так',
        });
        setPreloader(false);
      } else {
        setPreloader(false);
      }
    }, timeoutMs || 1500); // далее таймаут будет меньше между запросами
  };

  const countRequests = requestList.length;

  // eslint-disable-next-line @stylistic/no-mixed-operators
  const isErrors = isRegular && !cargoStore.totalRegularCost || schedulerError;
  const buttonText = isErrors ? 'Необходимо задать период' : 'Оформить заказ';

  // Тут загрузка нужна исключительно, если сразу открывается страница редактирования. Например, для разработки.
  // todo необходимо продумать случай, когда пользователь перезагружает страницу
  useEffect(() => {
    if (!status) {
      loadRequest();
    }
  }, [status]);

  useEffect(() => {
    if (status && status !== RequestStatus.IN_PROGRESS && requestList.length) {
      cargoMassStoreMultiple.prepareRequest();

      // Только для разработки редактирования
      // setEditItem(requestList[0]);
      setPreloader(false);
    }
  }, [status, requestList.length]);

  useEffect(() => {
    if (requestList.length) {
      cargoMassStoreMultiple.sortRequestList(sort.type, sort.order);
    }
  }, [sort]);

  const allTariffsOptions: TariffsOption[] = useMemo(() => {
    if (!requestList.length) {
      return [];
    }

    // Выбираем все тарифы которые есть у заявок
    type TariffsObj = Record<TransportTypeEnum, TariffsOption> | {};

    const tariffsObj: TariffsObj = (requestList || []).reduce(
      (_acc: TariffsObj, _curr: any) => ({
        ..._acc,
        ...(_curr.tariffs || []).reduce(
          (acc: TariffsObj, curr: TariffCost) => ({
            ...acc,
            [curr.transportType.name]: {
              label: transportTypeTitles[curr.transportType.name as TransportTypeEnum],
              value: curr.transportType.name,
            },
          }),
          {}
        ),
      }),
      {}
    );

    return Object.values(tariffsObj);
  }, [requestList]);

  useEffect(() => {
    const resize = () => {
      setHeaderWidth(contentRef.current?.getBoundingClientRect().width || 0);
    };
    resize();
    window.addEventListener('resize', resize);
    return () => window.removeEventListener('resize', resize);
  }, [preloader, editItem]);

  const scrollTop = () => {
    const content = document.querySelector('#layout_content');
    if (content) {
      content.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handleChangeTariff = (requestId: string, tariffType: TariffType) => {
    setAllTariff(TARIFF_SYSTEM_TYPE_VALUE);
    cargoMassStoreMultiple.selectTariffCargo(requestId, tariffType);
  };

  const handleChangeAllTariff = (tariffType: any) => {
    if (tariffType !== TARIFF_SYSTEM_TYPE_VALUE) {
      setAllTariff(tariffType);
      cargoMassStoreMultiple.selectAllTariffCargo(tariffType);
    }
  };

  const handleOnSort = (type: SortType, order: SortTypeOrder) => {
    setSort({ type, order });
    scrollTop();
  };

  const handleOnDelete = (requestId: string) => {
    const count = cargoMassStoreMultiple.deleteRequestItem(requestId);

    if (!count) {
      setShowPreview(false);
      setShowList(false);
    }
    scrollTop();
  };

  const handleOnCancel = () => {
    setEditItem(null);
    loadFromCargoMassRequestMulti(editItem);
    scrollTop();
  };

  const handleOnEdit = (data: RequestListItem) => {
    setEditItem(data);
    scrollTop();
  };

  const handleOnSaveEdit = (requestId: string, data: RequestListItem) => {
    // Если каким то боком из заявки удалили все грузы, то и заявку грохаем
    // таки зачем она нам без грузов
    if (!data.listCargo.length) {
      cargoMassStoreMultiple.deleteRequestItem(requestId);
    } else {
      cargoMassStoreMultiple.saveRequestItem(requestId, data);
      loadFromCargoMassRequestMulti(editItem);
    }
    setEditItem(null);
    setAllTariff(TARIFF_SYSTEM_TYPE_VALUE);
    scrollTop();
  };

  const doneRedirect = () => {
    if (isRegular) {
      history.push(REGULAR_CARGOS);
    } else {
      history.push(CARGOS);
    }
  };

  const handleOrder = async () => {
    const {
      periodType, dayOfWeek, weekOfMonth, monthOfQuartal, beginDate, endDate,
    } = periodValues;

    if (isRegular) {
      const checkDate = () => {
        if (!beginDate || !endDate) {
          return false;
        }

        if (periodType === Periodicity.week) {
          if (dayOfWeek.length === 0) {
            return false;
          }
        }
        return true;
      };

      if (!checkDate()) {
        notification.error({
          message: 'Ошибка!',
          description: 'Выбран не корректный период, заполните данные',
        });
        return false;
      }
    }

    const requestObjectMulti: RequestObjectMulti[] = requestList.map(r => {
      const calculatedTariff = r.tariffs.find(t => t.active);

      const senderContact = {
        fullName: r.sender?.fullName,
        mobilePhone: r.sender?.mobilePhone,
        employeeId: r.sender?.id,
        organizationId: r.sender?.organizationId,
        firstName: r.sender?.firstName,
        lastName: r.sender?.lastName,
        patronymic: r.sender?.patronymic,
      };

      const recipientContact = {
        employeeId: r.recipient?.id,
        mobilePhone: r.recipient?.mobilePhone,
        fullName: r.recipient?.fullName,
        organizationId: r.recipient?.organizationId,
        firstName: r.recipient?.firstName,
        lastName: r.recipient?.lastName,
        patronymic: r.recipient?.patronymic,
      };

      const senderWaypoint = {
        ...r.expected.waypoints[0],
        addressStringRepresentation: r.expected.waypoints[0].addressString,
        contacts: [senderContact],
        type: AddressLoadType.LOAD,
        organization: r.senderOrganization,
        orderingIndex: r.expected.waypoints[0].orderingIndex,
      };

      const recipientWaypoint = {
        ...r.expected.waypoints[1],
        addressStringRepresentation: r.expected.waypoints[1].addressString,
        contacts: [recipientContact],
        type: AddressLoadType.UNLOAD,
        organization: r.recipientOrganization,
        orderingIndex: r.expected.waypoints[1].orderingIndex,
      };

      return {
        desiredDate: moment(r.desiredDate).format(DATE_FORMAT.MONTH_NAME_WITH_TIME_SECONDS_REVERTED),
        author: r.author,
        humanReadableId: r.humanReadableId,
        organizationId: selfStore.selfEmployee.organizationId,
        cargoDetails: r.listCargo,
        waypoints: [senderWaypoint, recipientWaypoint],
        /// todo нужна доработка бэка: в tariffs необходимо указывать дистанции для всех типов транспорта
        calculatedTariff: {
          ...calculatedTariff,
          distance: calculatedTariff.distance > 0 ? calculatedTariff.distance : undefined,
          priceDetails: {
            ...calculatedTariff.priceDetails, distance: r.expected.distance,
          },
        },
        source: SOURCE_APP,
        cost: r.expected.cost,
        segments: r.expected.segments,
        comment: r.comment,
        requestNumber: r.requestNumber,
        approver: r.approver,
        loaders: calculatedTariff.loaders,
      };
    });

    const requestRegularObject = requestList.map((r, idx) => {
      return {
        active: true,
        period: {
          periodType,
          dayOfWeek,
          weekOfMonth,
          monthOfQuartal,
          beginDate,
          endDate,
        },
        template: requestObjectMulti[idx],
      };
    });

    setPreloader(true);

    try {
      if (isRegular) {
        await cargoMassStoreMultiple.postRegularData(requestRegularObject);
        await cargoMassStoreMultiple.loadRegularRequestMulti();
        doneRedirect();
      } else {
        await cargoMassStoreMultiple.postData(requestObjectMulti);
        doneRedirect();
      }
    } catch (err) {
      setPreloader(false);
    }
  };

  if (preloader) {
    return <SpinWrapped />;
  }

  if (editItem) {
    return (
      <CargoEditMultiple
        data={editItem}
        onCancel={handleOnCancel}
        onEdit={handleOnSaveEdit}
        isRegular={isRegular}
      />
    );
  }

  if (error) {
    return (
      <ModalStyled
        centered={true}
        closable={true}
        visible={true}
        title={<ModalTitleStyled>Ошибка!</ModalTitleStyled>}
        footer={<>&nbsp;</>}
      >
        <ModalSubTitleStyled>Ваши заявки не были отправлены, попробуйте снова</ModalSubTitleStyled>
      </ModalStyled>
    );
  }

  return (
    <S.Container>
      <S.Header>
        <S.HeaderInner style={{ width: headerWidth }}>
          <S.HeaderItem>
            <span>Заявки на доставку</span>
            <S.Count>{countRequests}</S.Count>
          </S.HeaderItem>
          <S.HeaderItem>{showList && <SortOrder sort={sort} onSort={handleOnSort} />}</S.HeaderItem>
        </S.HeaderInner>
      </S.Header>
      <S.Content ref={contentRef}>
        {!showPreview && !showList && (
          <UploadFileButton
            onUpload={() => {
              setShowPreview(true);
            }}
          />
        )}

        {showPreview && (
          <Preview
            onConfirm={() => {
              setShowList(true);
              setShowPreview(false);
            }}
            onClose={() => {
              setShowPreview(false);
            }}
          />
        )}

        {showList && (
          <S.List>
            {requestList.map(item => (
              <S.Item key={item.id}>
                <CargoItemMultiple
                  item={item}
                  // index={i}
                  // prevItem={requestList[i - 1]}
                  isRegular={isRegular}
                  onChangeTariff={handleChangeTariff}
                  onEdit={handleOnEdit}
                  onDelete={handleOnDelete}
                />
              </S.Item>
            ))}
          </S.List>
        )}
      </S.Content>
      <S.Aside>
        <CargoFixed>
          {isRegular && (
            <CargoSideInnerContent>
              <Scheduler
                tariffCost={totalData.tariffCost?.totalCost}
                setSchedulerError={setSchedulerError}
              />
            </CargoSideInnerContent>
          )}
          {showList && (
            <CargoTotal
              sizes={totalData.sizes}
              distance={totalData.expected.distance}
              tariffCost={totalData.tariffCost}
              allCost={isRegular ? cargoStore.totalRegularCost : totalData.tariffCost?.totalCost}
              countRequests={countRequests}
              topElement={(
                <S.SelectTariff>
                  <S.LabelSelectTariff>Тип доставки (применяется ко всем заявкам)</S.LabelSelectTariff>
                  <Select
                    value={allTariff === TARIFF_SYSTEM_TYPE_VALUE ? TARIFF_SYSTEM_TYPE_LABEL : allTariff}
                    options={allTariffsOptions}
                    placeholder={TARIFF_SYSTEM_TYPE_LABEL}
                    onChange={handleChangeAllTariff}
                  />
                </S.SelectTariff>
              )}
              bottomElement={(
                <S.ButtonOrder
                  disabled={isErrors}
                  onClick={handleOrder}
                >
                  {buttonText}
                </S.ButtonOrder>
              )}
            />
          )}
        </CargoFixed>
      </S.Aside>
    </S.Container>
  );
});

export default CargoListMultiple;
