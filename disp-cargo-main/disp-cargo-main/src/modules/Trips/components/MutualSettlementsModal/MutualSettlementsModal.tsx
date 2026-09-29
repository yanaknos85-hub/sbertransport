import React, { FC, useState, useMemo } from 'react';
import { Form, Modal } from 'antd';
import { useTranslation } from 'i18n';
import { useForm } from 'antd/lib/form/Form';
import moment from 'moment/moment';

import { Button } from 'components/Button';
import DateRangeField from 'components/ModelFormField/FieldTypes/DateRangeField';
import DownloadButton from 'components/DownloadButton';
import { useValues } from './useValues';
import styles from './index.module.scss';
import { b64EncodeUnicode } from 'utils/utils';
import {
  CARGO_EXPORT_TRIPS_CARGO,
  CARGO_TRIPS_SERVICE
} from 'api/trips-cargo/trips-cargo.constants';
import { ModelFormFieldType } from 'components/ModelFormField/ModelFormField';
import { useTripsModal } from '../../context/TripsModal';
import { useProfile } from 'api/profile/profile.api';

export const MutualSettlementsModal: FC = () => {
  const { t } = useTranslation();
  const [form] = useForm();

  const { contractorId } = useProfile().data;

  const [exportLoading, setExportLoading] = useState(false);
  const { isOpened, closeModal } = useTripsModal();
  const { formValues, setValues } = useValues();

  const handleCancel = () => {
    closeModal();
  };

  const filters = useMemo(() => {
    if (formValues.startTime?.[0]) {
      return b64EncodeUnicode(
        JSON.stringify({
          duration: {
            start: moment.utc(formValues.startTime[0]).startOf('day'),
            end: moment.utc(formValues.startTime[1]).endOf('day'),
          },
          contractorId,
        })
      );
    }

    return '';
  }, [formValues]);

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
      visible={isOpened('mutualSettlements')}
      onCancel={handleCancel}
      destroyOnClose
      footer={[
        <>
          <Button type="text" onClick={handleCancel}>
            {t.global.cancel}
          </Button>
          <DownloadButton
            url={CARGO_EXPORT_TRIPS_CARGO}
            directoryUrl={CARGO_TRIPS_SERVICE}
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
