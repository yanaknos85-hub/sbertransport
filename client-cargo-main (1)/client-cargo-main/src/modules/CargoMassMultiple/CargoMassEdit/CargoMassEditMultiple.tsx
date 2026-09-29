/* eslint-disable no-redeclare */
/* eslint-disable react-hooks/exhaustive-deps */
/* eslint-disable react/destructuring-assignment */
import React, {
  FC, useEffect, useState
} from 'react';
import { Form, notification } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import { SpinWrapped } from 'shared/components';
import Button from 'shared/form/Button/Button';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { RequestListItem } from 'stores/CargoMassMultiple/CargoMassMultiple.interface';
import { AddressLoadType } from 'stores/Cargos/typesMulti';
import { TariffCost } from 'stores/CargoTariff/CargoTariff.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import {
  CargoListItem,
  DeliveryUrgencyEnum,
  Point,
  TransportTypeEnum,
  transportTypeTitles
} from 'types/Cargo';
import { DATE_FORMAT } from 'constants/constants.app';

import { FormValues, TariffsOption } from '../types';
import Address from './Address/Address';
import {
  Block,
  BlockContent,
  BlockName,
  BlockPadding,
  Buttons,
  Container,
  Content,
  Header
} from './CargoMassEditMultiple.style';
import Cargos from './Cargos/Cargos';
import TariffDateMultiple from './TariffDate/TariffDate';

interface Props {
  data: RequestListItem;
  onCancel: () => void;
  onEdit: (requestId: string, data: RequestListItem) => void;
  isRegular: boolean;
}

const CargoEditMultiple: FC<Props> = observer(({
  data, onEdit, onCancel, isRegular,
}) => {
  const [form] = Form.useForm();

  const {
    id,
    humanReadableId,
    sender,
    recipient,
    tariffs = [],
    expected,
    express,
    desiredDate,
    listCargo,
  } = data;

  const {
    [StoreNames.geoStore]: {
      calculatedRoute, loadFromCargoMassRequestMulti, waypoints,
    },
    [StoreNames.selfStore]: selfStore,
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.cargoTariffStore]: cargoTariffStore,
  } = useAppStoreContext();

  const initialTariff = tariffs.find(t => t.active) || tariffs[0];

  const initialTariffsList: Record<string, TariffCost> = tariffs.reduce((acc, cur) => ({
    ...acc,
    [cur.transportType.name]: cur,
  }), {});

  const { tariffs: calculatedTariffs, tariffsListMulti } = cargoTariffStore;
  const [cargos, setCargos] = useState<CargoListItem[]>(listCargo);
  const [disabled, setDisabled] = useState(false);
  const [preloader, setPreloader] = useState(false);
  const [tariff, setTariff] = useState<TariffCost | null>(initialTariff);
  const [currentTariff, setCurrentTariff] = useState<TariffCost>(initialTariff);
  const [transportType, setTransportType] = useState<TransportTypeEnum>(initialTariff?.transportType?.name);
  const [isAddressInteracting, setIsAddressInteracting] = useState(false);
  const [isTariffsLoading, setIsTariffsLoading] = useState(true);

  const [calculatedTariffsMulti, setCalculatedTariffsMulti] = useState<Record<string, TariffCost>>(
    tariffs
      .filter(t => t.active)
      .reduce((acc, cur) => ({
        ...acc,
        [cur.transportType.name]: cur,
      }), {})
  );

  const [tariffsOptions, setTariffsOptions] = useState<TariffsOption[]>(
    tariffs.map((currentTariffItem: TariffCost) => ({
      label: transportTypeTitles[currentTariffItem.transportType.name as TransportTypeEnum],
      value: currentTariffItem.transportType.name,
    }))
  );

  const isSaveDisabled = disabled || isAddressInteracting || isTariffsLoading;

  useEffect(() => {
    // Сеттим маршрут по 2-м уже известным точкам при первом запуске
    // Далее расчёт нового маршрута происходит через вызов calculateTariffs.
    loadFromCargoMassRequestMulti(data as any);
    setTariff(calculatedTariffsMulti[transportType]);
  }, [data]);

  useEffect(() => {
    // calculatedRoute высчитывается автоматически когда выбраны 2 точки адресов
    if (calculatedRoute) {
      calculateTariffs(desiredDate);
    }
  }, [calculatedRoute]);

  useEffect(() => {
    // вызов при удалении груза
    if (cargos.length !== listCargo.length) {
      calculateTariffs(desiredDate);
    }
  }, [cargos.length]);

  useEffect(() => {
    // настройка списка опций тарифов
    if (tariffsListMulti.length) {
      const options: TariffsOption[] = tariffsListMulti.map((currentTariffItem: TariffCost) => ({
        label: transportTypeTitles[currentTariffItem.transportType.name as TransportTypeEnum],
        value: currentTariffItem.transportType.name,
      }));

      setCalculatedTariffsMulti(calculatedTariffs);
      setTransportType(
        tariffsListMulti.find(t => t.active)?.transportType.name || tariffsListMulti[0].transportType.name
      );
      setTariffsOptions(options);
      // Установка тарифа в поле tariff, если в списке нет ранее выбранного маршрута
      setTariff(calculatedTariffs[tariffsListMulti[0].transportType.name]);
      setDisabled(false);
    }
  }, [calculatedRoute, calculatedTariffs]);

  useEffect(() => {
    if (calculatedRoute) {
      // настройка списка опций тарифов, если сервис route вернул ошибку
      if (!tariffsListMulti.length) {
        setDisabled(true);
        setTariffsOptions([]);
        notification.error({
          message: 'Создание заявки для массовой доставки',
          description: 'Не найдено ни одного контрагента на этом маршруте',
        });
        setTariff(null);
      } else {
        setDisabled(false);
      }
    }
  }, [tariffsListMulti]);

  const calculateTariffs = async (tripDate?: number) => {
    try {
      setIsTariffsLoading(true);
      const values = form.getFieldsValue();
      const fields = Object.keys(values);

      const isExpress = !!(
        fields.includes('deliveryUrgency')
          ? values.deliveryUrgency === DeliveryUrgencyEnum.express
          : express
      );

      await cargoTariffStore.calculateAllTariffsMulti({
        organizationId: selfStore.selfEmployee?.organizationId || '',
        startPoint: waypoints[0] as unknown as Point,
        stopPoint: waypoints[1] as unknown as Point,
        time: calculatedRoute?.time || 0,
        distance: !calculatedRoute ? expected.distance : calculatedRoute?.distance ?? 0,
        express: isExpress,
        weight: Number(
          cargos
            .reduce((acc: number, curr: CargoListItem) => acc + (curr.weight * curr.occupiedPlacesCount || 0), 0)
            .toFixed(3)
        ),
        maxWeightOfOnePlace: Math.max(...cargos.map(cargoItem => cargoItem.weight)),
        volume: Number(
          cargos
            .reduce((acc: number, curr: CargoListItem) => acc + (curr.volume * curr.occupiedPlacesCount || 0), 0)
            .toFixed(5)
        ),
        countPoint: waypoints.length,
        loadersNeeded: form.getFieldValue('sourceLoaders'),
        loaders: tariff?.loaders,
        occupiedPlacesCount: cargos.reduce((acc: number, curr: CargoListItem) => acc + curr.occupiedPlacesCount, 0),
        tripDate: moment(tripDate).format(DATE_FORMAT.MONTH_NAME_WITH_TIME_SECONDS_REVERTED),
        includeTemplate: isRegular,
      });

      setDisabled(false);
    } finally {
      setIsTariffsLoading(false);
      setTimeout(() => {
        setPreloader(false);
      }, 600);
    }
  };

  const handleFormChange = async (values: FormValues) => {
    const { tariff: selectedTariff } = values;
    const fields = Object.keys(values);

    // Если поле требует пересчёта тарифов — блокируем и запускаем расчёт
    if (
      fields.includes('deliveryUrgency')
      || fields.includes('sourceLoaders')
      || fields.includes('destinationLoaders')
    ) {
      setIsTariffsLoading(true);
      calculateTariffs(desiredDate);
    } else if (
      !fields.includes('recipientAddress')
      && !fields.includes('senderAddress')
    ) {
      // Для не-адресных полей (телефон, ФИО, организация) — сбрасываем блокировку
      setIsTariffsLoading(false);
    }
    // Для полей адреса — не трогаем isTariffsLoading,
    // он будет сброшен в calculateTariffs по useEffect на calculatedRoute

    if (fields.includes('tariff')) {
      if (!calculatedTariffsMulti[selectedTariff]) {
        setTransportType(selectedTariff);
        setTariff(initialTariffsList[selectedTariff]);
        setCurrentTariff(initialTariffsList[selectedTariff]);
      } else {
        setCurrentTariff(calculatedTariffsMulti[selectedTariff]);
        setTransportType(selectedTariff);
        setTariff(calculatedTariffsMulti[selectedTariff]);
      }
    }
  };

  const handleDelete = (position: number) => {
    setCargos(prevData => prevData.filter(cargo => cargo.position !== position));
  };

  const handleCancel = () => {
    cargoTariffStore.clearState();
    onCancel();
  };

  const onSave = async () => {
    try {
      const values = await form.validateFields();
      const [senderStore, recipientStore] = employeeStore.employeeAutocompleteSelected;

      // Сохраняем тарифы в зависимости от вызова calculate.
      const savedTariffs = tariffsListMulti.length
        ? tariffsListMulti.map(t => (
          t.transportType.name === currentTariff.transportType.name
            ? { ...calculatedTariffs[transportType], active: true }
            : { ...t, active: false }
        ))
        : tariffs.map(t => (
          t.transportType.name === currentTariff.transportType.name
            ? { ...initialTariffsList[transportType], active: true }
            : { ...t, active: false }
        ));

      const newData = {
        ...data,
        listCargo: cargos,
        sender: {
          address: values.senderAddress,
          mobilePhone: values.senderPhone,
          fullName: values.senderName,
          firstName: senderStore?.firstName || sender.firstName,
          lastName: senderStore?.lastName || sender.lastName,
          humanReadableId: senderStore?.humanReadableId || sender.humanReadableId,
          id: senderStore?.id || sender.id,
          patronymic: senderStore?.patronymic || sender.patronymic,
          personnelNumber: senderStore?.personnelNumber || sender.personnelNumber,
          positionId: senderStore?.positionId || sender.positionId,
          userId: senderStore?.userId || sender.userId,
          organizationId: senderStore?.organizationId || sender.organizationId,
        },
        senderOrganization: values.senderOrganization,
        recipient: {
          address: values.recipientAddress,
          mobilePhone: values.recipientPhone,
          fullName: values.recipientName,
          firstName: recipientStore?.firstName || recipient.firstName,
          lastName: recipientStore?.lastName || recipient.lastName,
          patronymic: recipientStore?.patronymic || recipient.patronymic,
          id: recipientStore?.id || recipient.id,
          humanReadableId: recipientStore?.humanReadableId || recipient.humanReadableId,
          personnelNumber: recipientStore?.personnelNumber || recipient.personnelNumber,
          positionId: recipientStore?.positionId || recipient.positionId,
          userId: recipientStore?.userId || recipient.userId,
          organizationId: recipientStore?.organizationId || recipient.organizationId,
        },
        recipientOrganization: values.recipientOrganization,
        sourceLoaders: values.sourceLoaders,
        destinationLoaders: values.destinationLoaders,
        desiredDate: moment(values.desiredDate).valueOf(),
        express: values.express,
        tariffs: savedTariffs,
        expected: {
          ...expected,
          cost: calculatedTariffs[transportType]?.cost,
          // Тип адреса LOAD, UNLOAD, указывается хардкодом, т.к. бэк не возвращает эту информацию
          waypoints: [
            {
              ...(sender.address !== values.senderAddress
                ? {
                  ...waypoints[0],
                  type: AddressLoadType.LOAD,
                }
                : {
                  ...expected.waypoints[0],
                  type: AddressLoadType.LOAD,
                }),
              addressStringRepresentation: values.senderAddress,
            },
            {
              ...(recipient.address !== values.recipientAddress
                ? {
                  ...waypoints[1],
                  type: AddressLoadType.UNLOAD,
                }
                : {
                  ...expected.waypoints[1],
                  type: AddressLoadType.UNLOAD,
                }),
              addressStringRepresentation: values.recipientAddress,
            },
          ],
        },
      };

      onEdit(id, newData);
      cargoTariffStore.clearState();
    } catch (errorInfo) {
      // console.log('Failed:', errorInfo);
    }
  };

  if (preloader) {
    return <SpinWrapped />;
  }

  return (
    <Container>
      <Content>
        <Form form={form} onValuesChange={handleFormChange}>
          <Block>
            <Header>
              <BlockPadding>
                {`Редактирование заявки ${humanReadableId}`}
              </BlockPadding>
            </Header>

            <BlockPadding>
              <BlockName>Адрес</BlockName>
              <BlockContent>
                <Address
                  data={data}
                  form={form}
                  onAddressInteractionChange={setIsAddressInteracting}
                />
              </BlockContent>
            </BlockPadding>
          </Block>

          <Block>
            <BlockPadding>
              <BlockName>
                Грузы
                {' '}
                <span>{cargos.length}</span>
              </BlockName>
              <BlockContent>
                <Cargos data={cargos} onDelete={handleDelete} />
              </BlockContent>
            </BlockPadding>
          </Block>

          <Block>
            <BlockPadding>
              <BlockName>Тариф и дата отправления</BlockName>
              <BlockContent>
                <TariffDateMultiple
                  form={form}
                  date={desiredDate}
                  express={express}
                  tariff={tariff}
                  tariffsOptions={tariffsOptions}
                  isRegular={isRegular}
                  calculateTariffs={calculateTariffs}
                />
              </BlockContent>
            </BlockPadding>
          </Block>

          <Buttons>
            <Button color="transparent" onClick={handleCancel}>
              Отмена
            </Button>
            <Button disabled={isSaveDisabled} onClick={onSave}>
              Сохранить
            </Button>
          </Buttons>
        </Form>
      </Content>
    </Container>
  );
});

export default CargoEditMultiple;
