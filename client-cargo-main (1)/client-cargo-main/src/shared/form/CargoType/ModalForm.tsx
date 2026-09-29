import React, { useEffect, useRef, useState } from 'react';
import { IEmployeeStore, ILogger } from '@sber-sbertransport/mf-core';
import {
  Col, Form, notification, Row
} from 'antd';
import { FormInstance } from 'antd/lib/form';
import { StoreNames } from 'ioc/ioc.storeNames';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { useCargoCategoryNames } from 'api/category';
import { ICargoStore } from 'stores/Cargo/Cargo.interface';
import {
  CargoTypeCategoryNameEnum,
  cargoTypeCategoryNameTitle,
  ICargoTypeStore
} from 'stores/CargoType/CargoType.interface';
import {
  AccessControlEnum,
  CargoCategoryEnum,
  CargoPersonalListItemType
} from 'types/Cargo';
import { sizesToUnits } from 'utils/Misc';

import FormField from '../FormField/FormField';
import { Modal } from './CargoType.style';
import { useFields } from './useFields';

interface ModalFormProps {
  visible: boolean;
  onSave: () => void;
  onCancel: () => void;
  initialCargoName?: string;
}

const useResetFormOnCloseModal = ({ form, visible }: { form: FormInstance; visible: boolean }) => {
  const prevVisibleRef = useRef<boolean>();

  useEffect(() => {
    prevVisibleRef.current = visible;
  }, [visible]);
  const prevVisible = prevVisibleRef.current;

  useEffect(() => {
    if (!visible && prevVisible) {
      form.resetFields();
    }
  }, [form, prevVisible, visible]);
};

const ModalForm: React.FC<ModalFormProps> = observer(({
  visible,
  onSave,
  onCancel,
  initialCargoName = '',
}) => {
  const [form] = Form.useForm();

  const {
    [StoreNames.cargoStore]: cargoStore,
  }: {
    logger: ILogger;
    cargoStore: ICargoStore;
    cargoTypeStore: ICargoTypeStore;
    employeeStore: IEmployeeStore;
  } = useAppStoreContext();

  const { cargoList } = cargoStore.stepCargosValues;

  const { data: cargoCategoryNames } = useCargoCategoryNames();
  const { categoryNames } = cargoCategoryNames;

  const [unit, setUnit] = useState('');
  const [isHiddenDemensions, setIsHiddenDemensions] = useState(true);

  const categoryChange = (value: string) => {
    const unit = categoryNames.find(category => category.name === value)?.unit as string;
    setUnit(unit);
  };

  const isOversized = (category: string) => {
    return category === CargoTypeCategoryNameEnum.BULK || category === CargoTypeCategoryNameEnum.LIQUID;
  };

  const field = useFields(categoryChange, isOversized, setIsHiddenDemensions, unit);

  useResetFormOnCloseModal({
    form,
    visible,
  });

  const initialValues = {
    checkboxSizes: false,
    name: initialCargoName,
  };

  useEffect(() => {
    if (visible && initialCargoName) {
      form.setFieldsValue({ name: initialCargoName });
    }
  }, [visible, initialCargoName, form]);

  const showCargoExistWarn = (description: string) => {
    notification.warning({
      message: 'Внимание!',
      description,
    });
  };

  const existCargoText = (name: string) => `Груз "${name}" уже добавлен`;

  const differentCargoType = (name: string, category: string) => (
    `Груз "${name}" отличается по категории. Категория груза ${name} - "${cargoTypeCategoryNameTitle[category]}"`
  );

  const handleAddCargo = async () => {
    const formFields = Object.keys(field);
    await form.validateFields(formFields);
    form.submit();

    const {
      length, width, height,
    } = form.getFieldsValue(['length', 'width', 'height']);

    // Параметры груза задаются на UI в сантиметрах
    // Наливные и насыпные грузы на UI задаются в кубических метрах.
    // При создании нового типа груза все размеры (длина, ширина, высота, объём) передаются в миллиметрах:
    // - габариты обычных грузов конвертируются из см (sizesToUnits), поэтому объём считается как см³ → мм³
    // - объём наливных/насыпных грузов вводится в м³, поэтому приводится к мм³ (м³ → мм³)
    // !!! Здесь размеры приводятся только для создания нового типа груза !!!
    const volumeCargo = isHiddenDemensions
      ? (Number(length) * Number(width) * Number(height)) * 1000 // cm³ → mm³
      : Number(form.getFieldValue('volume')) * 1_000_000_000; // m³ → mm³

    const cargo = {
      ...form.getFieldsValue(),
      /*
        cargoCategory обязательный параметр, без него нельзя создать заявку,
        если один груз из справочника, а другой создан вручную
      */
      cargoCategory: form.getFieldValue('category'),
      cargoName: form.getFieldValue('name'),
      occupiedPlacesCount: 1,
      position: cargoList.length,
      // Данные в сантиметрах для UI и для бэка
      // Все размеры (длина, ширина, высота, объём), при создании заявки уходят на бэк в см
      // Для наливных/насыпных грузов поля габаритов скрыты, объём приходит из поля "volume" в м³
      volume: isHiddenDemensions
        ? Number(length) * Number(width) * Number(height) // cm³
        : Number(form.getFieldValue('volume')) * 1_000_000,
      length: Number(length) || 0,
      width: Number(width) || 0,
      height: Number(height) || 0,
      weight: Number(form.getFieldValue('weight')) || 0,
    };

    // При создании нового типа груза мы должны передать все размеры (длина, ширина, высота, объем) в мм
    const personalCargoItem: CargoPersonalListItemType = {
      name: form.getFieldValue('name'),
      category: form.getFieldValue('category'),
      // Конвертируем в миллиметры для API
      ...sizesToUnits({
        length: Number(length) || 0,
        width: Number(width) || 0,
        height: Number(height) || 0,
      }),
      weight: Number(form.getFieldValue('weight')) || 0,
      volume: volumeCargo,
      // По-умолчанию тип груза - "другое"
      type: CargoCategoryEnum.OTHER,
      // По-умолчанию груз считается личным
      accessLevel: AccessControlEnum.PERSONAL,
    };

    if (cargoList.find(item => item.cargoName === cargo.cargoName)) {
      showCargoExistWarn(existCargoText(cargo.cargoName));
      return;
    }

    if (cargoList.find(item => item.category !== cargo.category)) {
      showCargoExistWarn(differentCargoType(cargo.cargoName, cargo.category));
      return;
    }

    if (cargo) {
      cargoStore.createCargoItemPersonal(personalCargoItem)
        .then(() => {
          cargoStore.setCargoList([...cargoList, cargo]);
          onSave();
        })
        .catch(err => {
          if (err.response.status === 409) {
            notification.error({
              message: 'Ошибка',
              description: 'Груз с таким именем уже существует',
            });
          } else {
            notification.error({
              message: 'Ошибка',
              description: 'Не удалось добавить груз',
            });
          }
        });
    }
  };

  return (
    <Modal
      title="Введите параметры груза"
      visible={visible}
      onOk={handleAddCargo}
      onCancel={onCancel}
      centered={true}
      closable={true}
      keyboard={true}
      width={620}
      okText="Сохранить"
      cancelText="Отмена"
    >
      <p>Если вы не нашли нужный груз в списке, вы можете вручную указать его габариты</p>
      <Form
        form={form}
        layout="vertical"
        name="cargoNewForm"
        initialValues={initialValues}
      >
        <FormField {...field.name} />
        <Row gutter={8}>
          <FormField {...field.category} />
        </Row>
        {isHiddenDemensions && (
          <Row gutter={16}>
            <Col span={6}>
              <FormField {...field.length} />
            </Col>
            <Col span={6}>
              <FormField {...field.width} />
            </Col>
            <Col span={6}>
              <FormField {...field.height} />
            </Col>
            <Col span={6}>
              <FormField {...field.weight} />
            </Col>
          </Row>
        )}
        {!isHiddenDemensions && (
          <Col span={6}>
            <FormField {...field.volume} />
          </Col>
        )}
      </Form>
    </Modal>
  );
});

export default ModalForm;
