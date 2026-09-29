import { Button } from 'components/Button';
import React, { FC, useMemo, useState } from 'react';
import { Modal } from '@sber-sbertransport/ui-kit/src';
import { useModalState } from 'hooks/useModal';
import DownloadButton from 'components/DownloadButton';
import { useProfile } from 'api/profile/profile.api';
import moment from 'moment';
import { useForm } from 'antd/lib/form/Form';
import { useValues } from './useValues';
import { b64EncodeUnicode } from 'utils/utils';
import { Form } from 'antd';
import { StatusSelect } from 'components/StatusSelect';
import { TripTypes } from 'constants/app.constants';
import DateRangeField from 'components/ModelFormField/FieldTypes/DateRangeField';
import { ModelFormFieldType } from 'components/ModelFormField/ModelFormField';
import Flex from 'components/Flex/Flex';
import withErrorBoundary from 'components/withErrorBoundary';
import { FormItem } from 'components/FormItem/FormItem';
import { finalTripStatuses } from 'constants/trips.constants';

interface ExportBtnProps {
  url: string;
  directoryUrl: string;
}

const ExportBtn: FC<ExportBtnProps> = withErrorBoundary(
  ({ url, directoryUrl }) => {
    const [form] = useForm();
    const { formValues, setValues } = useValues();

    const [visible, { show, hide }] = useModalState();

    const { contractorId } = useProfile().data;

    const [exportLoading, setExportLoading] = useState(false);

    const filters = useMemo(() => b64EncodeUnicode(
      JSON.stringify({
        ...(formValues.statuses?.length
        && formValues.startTime && {
          statuses: formValues.statuses,
          duration: {
            start: formValues.startTime[0] ? moment.utc(formValues.startTime[0]).startOf('day') : undefined,
            end: formValues.startTime[1] ? moment.utc(formValues.startTime[1]).endOf('day') : undefined,
          },
        }),
        contractorId,
      })
    ),
    [formValues, contractorId]
    );

    const onCancel = () => {
      hide();
      form.setFieldsValue({ startTime: undefined });
      setValues({ startTime: undefined });
    };

    const handleLoad = (busy: boolean) => {
      if (exportLoading && !busy) {
        onCancel();
      }
      setExportLoading(busy);
    };

    return (
      <>
        <Button onClick={show} type="primary">Выгрузить отчет в XLS</Button>

        <Modal
          title="Параметры реестра"
          open={visible}
          onCancel={onCancel}
          destroyOnClose
          footer={() => (
            <Flex justifyContent="flex-end">
              <Button type="text" onClick={onCancel}>
                Отмена
              </Button>
              <DownloadButton
                url={url}
                directoryUrl={directoryUrl}
                onLoad={handleLoad}
                mkUrl={url => `${url}?filters=${filters}`}
                customElement={(
                  <Button
                    type="primary"
                    loading={exportLoading}
                    disabled={!formValues.statuses?.length || !formValues.startTime}
                  >
                    Скачать
                  </Button>
                )}
              />
            </Flex>
          )}
        >
          <Form
            form={form}
            initialValues={formValues}
            onValuesChange={setValues}
          >
            <FormItem
              label="Статус"
              name="statuses"
              required
            >
              <StatusSelect
                mode="multiple"
                allowClear
                placeholder="Выберите статусы"
                tripMode={TripTypes.Passenger}
                restrict={finalTripStatuses}
              />
            </FormItem>
            <DateRangeField
              editable
              required
              name="startTime"
              label="Период"
              showTime={false}
              form={form}
              allowEmpty={[true, true]}
              fieldType={ModelFormFieldType.DATE_RANGE}
            />
          </Form>
        </Modal>
      </>
    );
  }
);

export default ExportBtn;
