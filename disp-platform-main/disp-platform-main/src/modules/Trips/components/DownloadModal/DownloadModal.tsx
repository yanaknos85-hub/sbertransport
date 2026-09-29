import React, { FC, useState, useMemo } from 'react';
import { Form, Modal } from 'antd';
import { useTranslation } from 'i18n';
import { useForm } from 'antd/lib/form/Form';
import moment from 'moment/moment';

import { StatusSelect } from 'components/StatusSelect';
import { Button } from 'components/Button';
import DateRangeField from 'components/ModelFormField/FieldTypes/DateRangeField';
import { AVAILABLE_PASSENGER_STATUSES } from '../../constants';
import { useTripsModal } from '../../context/TripsModal';
import DownloadButton from 'components/DownloadButton';
import { useValues } from './useValues';
import styles from './index.module.scss';
import { b64EncodeUnicode } from 'utils/utils';
import { EXPORT_TRIPS, TRIPS_SERVICE } from 'api/trips/trips.constants';
import { TripTypes } from 'constants/app.constants';
import { ModelFormFieldType } from 'components/ModelFormField/ModelFormField';
import { useProfile } from 'api/profile/profile.api';

export const DownloadModal: FC = () => {
  const { t } = useTranslation();
  const [form] = useForm();

  const [exportLoading, setExportLoading] = useState(false);
  const { isOpened, closeModal } = useTripsModal();
  const { formValues, setValues } = useValues();

  const { contractorId } = useProfile().data;

  const handleCancel = () => {
    closeModal();
  };

  const filters = useMemo(() => {
    if (formValues.statuses?.length && formValues.startTime?.[0]) {
      return b64EncodeUnicode(
        JSON.stringify({
          statuses: formValues.statuses,
          duration: {
            start: moment.utc(formValues.startTime[0]).startOf('day'),
            end: moment.utc(formValues.startTime[1]).endOf('day'),
          },
          contractorId,
        })
      );
    }

    return '';
  }, [formValues, contractorId]);

  const handleLoad = (busy: boolean) => {
    if (exportLoading && !busy) {
      handleCancel();
    }
    setExportLoading(busy);
  };

  return (
    <Modal
      title={t.Requests.Modal.Registry.title}
      className={styles.modal}
      visible={isOpened('download')}
      onCancel={handleCancel}
      destroyOnClose
      footer={[
        <>
          <Button type="text" onClick={handleCancel}>
            {t.global.cancel}
          </Button>
          <DownloadButton
            url={EXPORT_TRIPS}
            directoryUrl={TRIPS_SERVICE}
            onLoad={handleLoad}
            mkUrl={url => `${url}?filters=${filters}`}
            customElement={(
              <Button
                type="primary"
                loading={exportLoading}
                disabled={!filters}
              >
                {t.global.upload}
              </Button>
            )}
          />
        </>,
      ]}
    >
      <Form
        form={form}
        initialValues={formValues}
        onValuesChange={setValues}
      >
        <Form.Item label={t.Requests.Filters.status} name="statuses">
          <StatusSelect
            mode="multiple"
            allowClear
            placeholder={t.Requests.Filters.chooseStatus}
            tripMode={TripTypes.Passenger}
            restrict={AVAILABLE_PASSENGER_STATUSES}
          />
        </Form.Item>
        <DateRangeField
          editable
          name="startTime"
          label={t.Requests.Filters.period}
          showTime={false}
          form={form}
          fieldType={ModelFormFieldType.DATE_RANGE}
        />
      </Form>
    </Modal>
  );
};
