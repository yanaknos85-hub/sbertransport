import React, { FC } from 'react';
import { Col, Form, Row } from 'antd';
import moment from 'moment';
import { useGetListRegions } from 'api/tariffs-cargo';
import FormField from 'shared/form/FormField/FormField';
import { searchSymbol } from 'utils/searchSymbol';
import { LabeledValue } from 'antd/lib/select';
import { usePlanner } from 'modules/Planner/context/PlannerContext';
import { useTranslation } from 'i18n';
import { FormInstance } from 'antd/es/form';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { monitorFields } from './fields';
import { MonitorFiltersType } from '../../../types';
import { ReactComponent as CrossIcon } from '../../../images/crossIcon.svg';
import { routesFields } from '../Filters/routesFields';
import { START_PAGE } from '../../../constants';
import { useContractors } from 'api/contractors';

import { Statuses } from '../../Monitor/constants';
import * as S from '../Filters/Filters.styles';

interface Props {
  form: FormInstance;
  visible?: boolean;
  handleOk: (params: MonitorFiltersType) => void;
  handleReset: () => void;
}

export const MonitorFilters: FC<Props> = props => {
  const { form, visible, handleOk, handleReset } = props;

  const {
    handleCloseMonitorFilters,
  } = usePlanner();

  const { plannerStore } = useAppStoreContext();
  const geoZonesList = useGetListRegions().data;
  const { t } = useTranslation();

  const contractors = useContractors();
  const contractorsOptions = contractors.data.contractors.map(item => ({ label: item.name, value: item.id }));
  const geoZonesOptions: LabeledValue[] = geoZonesList.reduce<LabeledValue[]>((acc, { name }) => {
    if (!acc.some(option => option.label === name)) {
      acc.push({
        label: name,
        value: name,
      });
    }
    return acc;
  }, []);


  return (
    <S.Modal
      title="Фильтры"
      visible={visible}
      onOk={handleOk}
      onCancel={() => {
        handleCloseMonitorFilters();
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
        initialValues={{
          ...plannerStore.monitorFilters,
          creationDateRange: plannerStore.monitorFilters.creationDateRange && Object.keys(plannerStore.monitorFilters.creationDateRange)?.length > 0 ? [moment(plannerStore.monitorFilters.creationDateRange?.start), moment(plannerStore.monitorFilters.creationDateRange?.end )] : [],
          desiredDateRange: plannerStore.monitorFilters.desiredDateRange && Object.keys(plannerStore.monitorFilters.desiredDateRange)?.length > 0 ? [moment(plannerStore.monitorFilters.desiredDateRange?.start), moment(plannerStore.monitorFilters.desiredDateRange?.end )] : [],
          statusSet: plannerStore.monitorFilters.statusSet,
        }}
      >
        <S.Title>Фильтр маршрутов в мониторе</S.Title>
        <Row gutter={32}>
          <Col span={8}>
            <FormField {...monitorFields.humanReadableId} />
          </Col>
          <Col span={8}>
            <FormField
              {...monitorFields.regionFrom}
              params={{
                mode: 'multiple',
                allowClear: true,
                options: geoZonesOptions,
                showSearch: true,
                optionFilterProp: 'label',
                filterOption: searchSymbol,
                filterSort: (optionA: { label: string; value: string }, optionB: { label: string; value: string }) => (optionA?.label ?? '').toLowerCase().localeCompare((optionB?.label ?? '').toLowerCase()),
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
                filterSort: (optionA: { label: string; value: string }, optionB: { label: string; value: string }) => (optionA?.label ?? '').toLowerCase().localeCompare((optionB?.label ?? '').toLowerCase()),
              }}
            />
          </Col>
          <Col span={8}>
            <FormField
              {...monitorFields.creationDateRange}
            />
          </Col>
          <Col span={8}>
            <FormField
              {...monitorFields.desiredDateRange}
            />
          </Col>
          <Col span={8}>
            <FormField
              {...monitorFields.statusSet}
            />
          </Col>
        </Row>
        <Row gutter={32}>
          <Col span={8}>
            <FormField
              {...monitorFields.contractors}
              params={{
                mode: 'multiple',
                options: contractorsOptions,
                showSearch: true,
                allowClear: true,
                optionFilterProp: 'label',
                filterOption: (input, option) => option.label.toLowerCase().includes(input.toLowerCase()),
              }}
            />
          </Col>
        </Row>
        <S.ButtonsBlock>
          <S.ResetButton onClick={handleReset}>
            {t.Planner.reset}
          </S.ResetButton>
          <S.ApplyButton
            onClick={() => {
              handleOk({ ...form.getFieldsValue() });
              handleCloseMonitorFilters();
              plannerStore.setMonitorPage(START_PAGE);
            }}
          >
            {t.Planner.apply}
          </S.ApplyButton>
        </S.ButtonsBlock>
      </Form>
    </S.Modal>
  );
};
