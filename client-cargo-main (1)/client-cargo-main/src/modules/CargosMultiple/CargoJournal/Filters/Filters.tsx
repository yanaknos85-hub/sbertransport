import React, { FC, useEffect } from 'react';
import { Col, Form, Row } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { LabeledValue } from 'antd/lib/select';
import { observer } from 'mobx-react';
import FormField from 'shared/form/FormField/FormField';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { useGetListRegions } from 'api/regions';
import { FilterSettingsType } from 'stores/Cargos/types';

import { ReactComponent as CrossIcon } from '../../static/images/closeCross.svg';
import { searchSymbol } from '../../utils';
import { useSaveFilters } from '../hooks/useSaveFilters';
import { fields } from './fields';
import * as S from './styles';

interface Props {
  visible?: boolean;
  hideModal: () => void;
  refetchData: (data: FilterSettingsType) => void;
}

export const Filters: FC<Props> = observer(props => {
  const {
    visible,
    hideModal,
    refetchData,
  } = props;

  const {
    setFilterState,
    resetFilerState,
    saveCount,
    getFilterState,
  } = useSaveFilters();

  const { cargosStore } = useAppStoreContext();
  const geoZonesList = useGetListRegions().data;
  const [form] = useForm();

  const geoZonesOptions: LabeledValue[] = geoZonesList?.reduce<LabeledValue[]>((acc, { name }) => {
    if (!acc.some(option => option.label === name)) {
      acc.push({
        label: name,
        value: name,
      });
    }
    return acc;
  }, []) || [];

  const saveToSessionStorage = () => {
    const values = form.getFieldsValue();
    setFilterState({ ...values });
  };

  const handleReset = () => {
    resetFilerState();
    cargosStore.resetFilterSettings();
    form.resetFields();
  };

  // Загрузка сохранённых фильтров при открытии модального окна
  useEffect(() => {
    if (visible) {
      const savedFilters = getFilterState();
      form.setFieldsValue(savedFilters);
    }
  }, [visible]);

  return (
    <S.Modal
      title="Фильтры"
      visible={visible}
      onCancel={() => {
        hideModal();
      }}
      centered
      closable
      width={1136}
      closeIcon={<CrossIcon />}
      footer={null}
    >
      <Form
        form={form}
        layout="vertical"
        name="monitorFilters"
      >
        <Row gutter={32}>
          <Col span={8}>
            <FormField {...fields.requestHumanId} />
          </Col>
          <Col span={8}>
            <FormField {...fields.desiredDate} />
          </Col>
          <Col span={8}>
            <FormField {...fields.shipmentTime} />
          </Col>
        </Row>
        <Row gutter={32}>
          <Col span={8}>
            <FormField {...fields.requestStatusSet} />
          </Col>
          <Col span={8}>
            <FormField {...fields.transportTypeEnum} />
          </Col>
          <Col span={8}>
            <FormField {...fields.role} />
          </Col>
        </Row>
        <Row gutter={32}>
          <Col span={8}>
            <FormField
              {...fields.regionFrom}
              params={{
                mode: 'multiple',
                allowClear: true,
                options: geoZonesOptions,
                showSearch: true,
                optionFilterProp: 'label',
                filterOption: searchSymbol,
                filterSort: (optionA, optionB) => (optionA?.label ?? '').toLowerCase().localeCompare((optionB?.label ?? '').toLowerCase()),
              }}
            />
          </Col>
          <Col span={8}>
            <FormField
              {...fields.regionTo}
              params={{
                mode: 'multiple',
                allowClear: true,
                options: geoZonesOptions,
                showSearch: true,
                optionFilterProp: 'label',
                filterOption: searchSymbol,
                filterSort: (optionA, optionB) => (optionA?.label ?? '').toLowerCase().localeCompare((optionB?.label ?? '').toLowerCase()),
              }}
            />
          </Col>
        </Row>
        <S.ButtonsBlock>
          <S.Button reset onClick={handleReset}>
            Сбросить
          </S.Button>
          <S.Button
            onClick={() => {
              const values = form.getFieldsValue();
              saveToSessionStorage();
              saveCount(values);
              cargosStore.setFilterSettings(values);
              refetchData(cargosStore.filterSettings);
              hideModal();
            }}
          >
            Применить
          </S.Button>
        </S.ButtonsBlock>
      </Form>
    </S.Modal>
  );
});
