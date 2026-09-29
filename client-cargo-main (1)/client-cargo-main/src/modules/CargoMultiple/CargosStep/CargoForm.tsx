/* eslint-disable jsx-a11y/click-events-have-key-events */
import React, { FC, useEffect, useState } from 'react';
import { QuestionCircleOutlined } from '@ant-design/icons';
import {
  Col, Form, notification, Row, Tooltip
} from 'antd';
import { observer } from 'mobx-react';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import { CargoListItem } from 'types/Cargo';

import * as S from './Cargos.style';

interface Props {
  cargo?: CargoListItem;
  position: number;
  initialValues?: any;
  logger: any;
  error: string;
  onDelete?: () => void;
  onCancel?: () => void;
  onSave: (data: CargoListItem) => void;
}

const CargoForm: FC<Props> = observer(props => {
  const {
    cargo, position, initialValues, error, onSave, onCancel, onDelete,
  } = props;

  const {
    [StoreNames.cargoTypeStore]: cargoTypeStore,
  } = useAppStoreContext();

  const [cargoId, setCargoId] = useState(cargo?.id);
  const [form] = Form.useForm();

  const field = {
    cargoName: {
      label: 'Название',
      name: 'cargoName',
      type: FieldType.cargoType,
      rules: [ValidationRules.general.required],
      params: {
        placeholder: 'Выберите из списка',
      },
    },
    occupiedPlacesCount: {
      label: 'Количество',
      name: 'occupiedPlacesCount',
      type: FieldType.number,
      rules: [ValidationRules.general.required, ValidationRules.general.min(1)],
      initialValue: initialValues?.occupiedPlacesCount || 1,
      params: {
        min: 1,
      },
    },
    needPackage: {
      label: 'Упаковать груз',
      name: 'needPackage',
      type: FieldType.checkbox,
      initialValue: !!initialValues?.needPackage,
    },
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();

      const cargo = cargoTypeStore.cargoTypeAutocompleteList.find(x => x.name === values.cargoName);

      if (cargoTypeStore.cargoType && cargo) {
        const cargoFromStore = cargoId ? cargoTypeStore.cargoTypes[cargoId] : cargoTypeStore.cargoType;

        const { cargoName, needPackage } = values;
        const occupiedPlacesCount = Number(values.occupiedPlacesCount) || 1;

        const data = {
          ...cargoFromStore,
          cargoName: cargoFromStore.name,
          cargoType: cargoFromStore.type,
          cargoCategory: cargoFromStore.category,
        };

        onSave({
          ...data,
          cargoName, // Ставим value только для того чтобы отлавливать ошибку дубликатов
          width: data.width, // Вне зависимости от кол-ва грузов - показываем значение одного
          length: data.length,
          height: data.height,
          weight: data.weight,
          volume: data.volume,
          position,
          occupiedPlacesCount,
          needPackage,
        });

        form.resetFields();
      } else {
        notification.error({
          message: 'Ошибка',
          description: 'Указанного наименования нет в списке. Пожалуйста, заполните данные груза, выбрав "Создать новый груз" в выпадающем списке',
        });
      }
    } catch (errorInfo) {
      // console.log('Failed:', errorInfo);
    }
  };

  // Теперь груз сохраняется сразу после выбора в селекте
  field.cargoName.params = {
    ...field.cargoName.params as any,
    onSelect: (name: string) => {
      const cargo = cargoTypeStore.cargoTypeAutocompleteList.find(x => x.name === name);
      setCargoId(cargo?.id);
    },
  };
  useEffect(() => {
    if (cargoId) {
      handleSave();
    }
  }, [cargoId]);

  return (
    <S.Container>
      <Form name={`cargoForm_${position}`} form={form}>
        <Row style={{ display: 'flex', justifyContent: 'space-between' }}>
          <Col>
            <S.CargoHeader>Новый груз</S.CargoHeader>
          </Col>
          <Col style={{ display: 'flex' }}>
            {cargo && <S.CargoFormBtnCancel onClick={onCancel}>Отменить</S.CargoFormBtnCancel>}
            {!cargo && position !== 0 && (
              <S.CargoFormBtnDelete onClick={onDelete}>Удалить</S.CargoFormBtnDelete>
            )}
          </Col>
        </Row>
        <Row>
          <Col span={24}>
            <FormField
              {...field.cargoName}
              error={error}
              label={(
                <span>
                  Название
                  <Tooltip title="Если нужного груза нет в списке, создайте новый">
                    <QuestionCircleOutlined
                      style={{
                        marginLeft: 8, color: '#909090', cursor: 'pointer',
                      }}
                    />
                  </Tooltip>
                </span>
              )}
            />
          </Col>
        </Row>
        {/* Cкрыто в рамках задачи TRANSPORT-11153
       <FormField {...field.needPackage} /> */}
      </Form>
    </S.Container>
  );
});

export default CargoForm;
