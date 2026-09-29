import React, { useEffect } from 'react';
import { Col, Form } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { Store } from 'antd/lib/form/interface';
import { Input, Modal, Text } from '@sber-sbertransport/ui-kit/src';
import moment from 'moment';

import { Button } from 'components/Button';
import Flex from 'components/Flex/Flex';
import { SearchPanel } from 'components/SearchPanel/SearchPanel';
import withErrorBoundary from 'components/withErrorBoundary';
import { FormItem } from 'components/FormItem/FormItem';
import { StatusSelect } from 'components/StatusSelect';
import DateRangeField from 'components/ModelFormField/FieldTypes/DateRangeField';
import { ModelFormFieldType } from 'components/ModelFormField/ModelFormField';
import { RowWithContainer } from 'components/Row/Row';
import { useModalState } from 'hooks/useModal';
import useFiltersCount from 'hooks/useFiltersCount';
import { ReactComponent as CloseIcon } from 'assets/icons/close-small.svg';
import { TripTypes } from 'constants/app.constants';
import { finalTripStatuses } from 'constants/trips.constants';
import { ALLOWED_VEHICLE_REGISTRY_CHARS, matchPattern } from 'utils/fieldValidationRules/fieldValidationRules';

import { useTripsTabQuery } from '../../context/TripsTab.queryContext';
import useFilters from '../../hooks/useFilters';
import { isValidDate } from '../../utils';

const Filters = withErrorBoundary(() => {
  const { query, setQuery } = useTripsTabQuery();
  const {
    page, size, direction, field, ...filters
  } = query;
  const { filtersCount } = useFiltersCount(filters);

  const onSearch = (requestHumanReadableId: string) => setQuery({ requestHumanReadableId });

  const [visible, { show, hide }] = useModalState();

  const [form] = useForm();

  const { defaultValues } = useFilters();

  const resetFilters = () => {
    setQuery(defaultValues);
    hide();
  };

  const onFinish = (values: Store) => {
    setQuery(values);
    hide();
  };

  useEffect(() => {
    if (visible) {
      setTimeout(() => form.resetFields());
    }
  }, [query, visible, form]);

  const formValues = {
    ...filters,
    ...(filters.startTimeFrom && {
      startTimeFrom: [
        isValidDate(filters.startTimeFrom?.[0]) ? moment(filters.startTimeFrom![0]) : null,
        isValidDate(filters.startTimeFrom?.[1]) ? moment(filters.startTimeFrom![1]) : null,
      ],
    }),
  };

  return (
    <>
      <Flex>
        <SearchPanel
          placeholder="Поиск по номеру заявки"
          onSearch={onSearch}
          value={query.requestHumanReadableId}
        />
        <Button
          filter
          counter={filtersCount}
          onClick={show}
        >
          Фильтры
        </Button>
        {!!filtersCount && (
          <Button type="text" onClick={resetFilters}>
            <Text type="secondary">Сбросить фильтры</Text>
            <CloseIcon />
          </Button>
        )}
      </Flex>

      <Modal
        title="Параметры реестра"
        open={visible}
        onCancel={hide}
        width={1100}
        footer={() => (
          <Flex justifyContent="flex-end">
            <Button
              type="text"
              onClick={resetFilters}
              danger
            >
              Сбросить
            </Button>
            <Button type="primary" onClick={form.submit}>
              Применить
            </Button>
          </Flex>
        )}
        afterClose={() => form.resetFields()}
      >
        <Form
          form={form}
          initialValues={formValues}
          onFinish={onFinish}
        >
          <RowWithContainer>
            <Col span={24}>
              <FormItem label="Статус" name="statuses">
                <StatusSelect
                  mode="multiple"
                  allowClear
                  placeholder="Выберите статусы"
                  tripMode={TripTypes.Passenger}
                  restrict={finalTripStatuses}
                  showSearch
                />
              </FormItem>
            </Col>
            <Col span={8}>
              <DateRangeField
                editable
                name="startTimeFrom"
                label="Отчетный период"
                showTime={false}
                form={form}
                allowEmpty={[true, true]}
                fieldType={ModelFormFieldType.DATE_RANGE}
              />
            </Col>
            <Col span={8}>
              <FormItem label="Номер ТС" name="vehicleNumber">
                <Input
                  allowClear
                  onInput={matchPattern(new RegExp(`${ALLOWED_VEHICLE_REGISTRY_CHARS.replace(']', '0-9]')}+`))}
                />
              </FormItem>
            </Col>
            <Col span={8}>
              <FormItem label="Номер договора" name="contractNumber">
                <Input
                  allowClear
                  onInput={matchPattern(/[0-9]+/)}
                />
              </FormItem>
            </Col>
          </RowWithContainer>
        </Form>
      </Modal>
    </>
  );
});

export default Filters;
