import React, { FC } from 'react';
import { Col, Form, Row } from 'antd';
import { useForm, FormInstance } from 'antd/lib/form/Form';
import FormField from 'shared/form/FormField/FormField';

import { useGetListRegions } from 'api/tariffs-cargo';
import { LabeledValue } from 'antd/lib/select';
import { searchSymbol } from 'utils/searchSymbol';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { ReactComponent as CrossIcon } from '../../../images/crossIcon.svg';
import { ordersFields } from './ordersFields';
import { OrdersFiltersType } from '../../../types';
import { usePlanner } from 'modules/Planner/context/PlannerContext';
import { START_PAGE } from '../../../constants';

import * as Styled from './Filters.styles';

interface Props {
  visible: boolean;
  handleOk: (prams: OrdersFiltersType, form: FormInstance) => void;
}

export const OrdersFilters: FC<Props> = props => {
  const { visible, handleOk } = props;

  const [form] = useForm();

  const { handleCloseOrdersFilters } = usePlanner();
  const { plannerStore } = useAppStoreContext();

  const geoZonesList = useGetListRegions().data;

  const geoZonesOptions: LabeledValue[] = geoZonesList.reduce<LabeledValue[]>((acc, { name }) => {
    if (!acc.some(option => option.label === name)) {
      acc.push({
        label: name,
        value: name,
      });
    }
    return acc;
  }, []);

  const handleReset = () => {
    form.resetFields();
  };

  return (
    <Styled.Modal
      title="Фильтры"
      visible={visible}
      onOk={handleOk}
      onCancel={handleCloseOrdersFilters}
      centered
      closable
      width={1136}
      closeIcon={<CrossIcon />}
      footer={null}
    >
      <Form
        form={form}
        layout="vertical"
        name="logistic-filter"
      >
        <Styled.Title>Заявка</Styled.Title>
        <Row gutter={32}>
          <Col span={8}>
            <FormField {...ordersFields.humanReadableId} />
          </Col>
          <Col span={8}>
            <FormField {...ordersFields.creationDateRange} />
          </Col>
          <Col span={8}>
            <FormField {...ordersFields.desiredDateRange} />
          </Col>
          <Col span={8}>
            <FormField {...ordersFields.departmentEmployeeId} fieldName="departmentEmployeeId" />
          </Col>
          <Col span={8}>
            <FormField {...ordersFields.departmentSenderId} fieldName="departmentSenderId" />
          </Col>
          <Col span={8}>
            <FormField {...ordersFields.departmentOrderId} fieldName="departmentOrderId" />
          </Col>
          <Col span={8}>
            <FormField
              {...ordersFields.regionFrom}
              params={{
                mode: 'multiple',
                allowClear: true,
                options: geoZonesOptions,
                showSearch: true,
                optionFilterProp: 'children',
                filterOption: searchSymbol,
                filterSort: (optionA: any, optionB: any) => (optionA?.label ?? '').toLowerCase().localeCompare((optionB?.label ?? '').toLowerCase()),
              }}
            />
          </Col>
          <Col span={8}>
            <FormField
              {...ordersFields.regionTo}
              params={{
                mode: 'multiple',
                allowClear: true,
                options: geoZonesOptions,
                showSearch: true,
                optionFilterProp: 'children',
                filterOption: searchSymbol,
                filterSort: (optionA: any, optionB: any) => (optionA?.label ?? '').toLowerCase().localeCompare((optionB?.label ?? '').toLowerCase()),
              }}
            />
          </Col>
        </Row>
        <Styled.Title>Заявитель</Styled.Title>
        <Row gutter={32}>
          <Col span={8}>
            <FormField {...ordersFields.authorEmployeeId} />
          </Col>
          <Col span={8}>
            <FormField {...ordersFields.departmentId} />
          </Col>
        </Row>
        <Styled.ButtonsBlock>
          <Styled.ResetButton onClick={() => handleReset()}>
            Сбросить
          </Styled.ResetButton>
          <Styled.ApplyButton
            onClick={() => {
              handleOk({ ...form.getFieldsValue() }, form);
              plannerStore.setOrderPage(START_PAGE);
            }}
          >
            Применить
          </Styled.ApplyButton>
        </Styled.ButtonsBlock>
      </Form>
    </Styled.Modal>
  );
};
