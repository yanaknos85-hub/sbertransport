import React, { FC } from 'react';
import { Col, Form, Row } from 'antd';
import { useForm, FormInstance } from 'antd/lib/form/Form';
import FormField from 'shared/form/FormField/FormField';

import { useGetListRegions } from 'api/tariffs-cargo';
import { LabeledValue } from 'antd/lib/select';
import { searchSymbol } from 'utils/searchSymbol';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { ReactComponent as CrossIcon } from '../../../images/crossIcon.svg';
import { routesFields } from './routesFields';
import { RoutesFiltersType } from '../../../types';
import { usePlanner } from 'modules/Planner/context/PlannerContext';
import { START_PAGE } from '../../../constants';

import * as Styled from './Filters.styles';

interface Props {
  visible?: boolean;
  handleOk: (params: RoutesFiltersType, form: FormInstance) => void;
}

export const RoutesFilters: FC<Props> = props => {
  const { visible, handleOk } = props;

  const [form] = useForm();

  const { handleCloseRoutesFilters } = usePlanner();
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
      onCancel={() => {
        handleCloseRoutesFilters();
        handleReset();
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
        name="logistic-filter"
      >
        <Styled.Title>Маршрут</Styled.Title>
        <Row gutter={32}>
          <Col span={8}>
            <FormField {...routesFields.humanReadableId} />
          </Col>
          <Col span={8}>
            <FormField
              {...routesFields.regionFrom}
              params={{
                mode: 'multiple',
                allowClear: true,
                options: geoZonesOptions,
                showSearch: true,
                optionFilterProp: 'label',
                filterOption: searchSymbol,
                filterSort: (optionA: any, optionB: any) => (optionA?.label ?? '').toLowerCase().localeCompare((optionB?.label ?? '').toLowerCase()),
              }}
            />
          </Col>
          <Col span={8}>
            <FormField
              {...routesFields.regionTo}
              params={{
                mode: 'multiple',
                allowClear: true,
                options: geoZonesOptions,
                showSearch: true,
                optionFilterProp: 'label',
                filterOption: searchSymbol,
                filterSort: (optionA: any, optionB: any) => (optionA?.label ?? '').toLowerCase().localeCompare((optionB?.label ?? '').toLowerCase()),
              }}
            />
          </Col>
          <Col span={8}>
            <FormField {...routesFields.creationDateRange} />
          </Col>
          <Col span={8}>
            <FormField {...routesFields.desiredDateRange} />
          </Col>
          <Col span={8}>
            <FormField {...routesFields.statusSet} />
          </Col>
        </Row>
        <Styled.ButtonsBlock>
          <Styled.ResetButton onClick={handleReset}>
            Сбросить
          </Styled.ResetButton>
          <Styled.ApplyButton
            onClick={() => {
              handleOk({ ...form.getFieldsValue() }, form);
              plannerStore.setRoutePage(START_PAGE);
            }}
          >
            Применить
          </Styled.ApplyButton>
        </Styled.ButtonsBlock>
      </Form>
    </Styled.Modal>
  );
};
