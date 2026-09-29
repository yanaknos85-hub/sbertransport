import React, {
  FC, useEffect, useMemo, useState
} from 'react';
import { Form, Input } from '@sber-sbertransport/ui-kit/src';
import { useHistory } from '@sber-sbertransport/mf-core';
import { ArrowLeftOutlined } from '@ant-design/icons';

import { useTranslation } from 'i18n';
import moment, { Moment } from 'moment';
import { UUID } from 'utils/io-ts';
import { DATE_FORMAT } from 'constants/app.constants';
import { RELEASE_ON_LINE_LINK } from 'constants/routes.constants';
import { isTestStand } from 'utils/isTestStand';
import createUUID from 'utils/uuid';

import { Button } from 'components/Button';
import { Col, Divider } from 'antd';
import DatePicker from 'components/DatePicker/DatePicker';
import { FormItem } from 'components/FormItem';
import Panel from 'components/Panel/Panel';
import { RowWithContainer } from 'components/Row/Row';

import { useCreateFirstTitle, useGetUUID } from 'api/waybill/waybill.api';
import { TCreateFirstTitleRequest, TCreateFirstTitleResponse } from 'api/waybill/waybill.types';
import { useGetSelfOrganizationDispatcher } from 'api/telemechanicDispatchers/telemechanicDispatchers.api';

import { CreationFormValues, CreationPayload } from './WaybillCreate.interface';
import {
  Communication,
  CommunicationInfo,
  DispatcherOrganizationField,
  Transportation,
  TransportationInfo
} from '../Waybill.constants';

import DriverSection from './sections/DriverSection/DriverSection';
import OwnerSection from './sections/OwnerSection/OwnerSection';
import TransportSection from './sections/TransportSection/TransportSection';

import ConfirmModal from './modals/Confirm/Confirm';
import SignModal from './modals/Sign/SignModal';

import styles from './WaybillCreate.module.scss';
import { ignore } from 'utils/utils';

const WaybillCreate: FC = () => {
  const [form] = Form.useForm();
  const history = useHistory();
  const { create: i18 } = useTranslation().t.Waybill;

  const [getUUIDD, { isLoading: isUuidLoading }] = useGetUUID();
  const [createFirstTitle, { isLoading: isTitleLoading }] = useCreateFirstTitle();

  const state = useGetSelfOrganizationDispatcher().data;

  const [uuid, setUuid] = useState<UUID | null>(null);
  const [startDate, setStartDate] = useState<Moment | null>(null);
  const [transportId, setTransportId] = useState<UUID | null>(null);
  const [titleForm, setTitleForm] = useState<TCreateFirstTitleRequest | null>(null);
  const [titleResult, setTitleResult] = useState<TCreateFirstTitleResponse | null>(null);
  const [confirmationData, setConfirmationData] = useState<CreationPayload | null>(null);
  const [isConfirmed, setIsConfirmed] = useState(false);

  const initialValues = useMemo(() => ({
    [DispatcherOrganizationField.name]: state.organization.name,
    [DispatcherOrganizationField.regionCode]: state.regionCode,
    [DispatcherOrganizationField.phone]: state.organization.phone,
    [DispatcherOrganizationField.ogrn]: state.organization.msrn,
    [DispatcherOrganizationField.tin]: state.organization.tin,
  }), [state]);

  useEffect(() => {
    form.setFieldsValue(initialValues);
  }, [initialValues, form]);

  const handleGetUUID = () => {
    if (isTestStand()) {
      setUuid(createUUID());
    } else {
      getUUIDD()
        .then(result => {
          if (result) setUuid(result.uuid);
        })
        .catch(ignore);
    }
  };

  const handleSubmit = async (values: CreationFormValues) => {
    const request = {
      ewbUuid: values.ewbUuid as UUID,
      startDate: moment(values.startDate).format(DATE_FORMAT.BASE),
      finishDate: moment(values.finishDate).format(DATE_FORMAT.BASE),
      transportationType: Transportation.OwnNeeds,
      communicationType: Communication.Urban,
      tariffDepartmentId: values.driverDepartmentId as UUID,
      transportId: values.transportId as UUID,
      driverId: values.driverId as UUID,
    };

    createFirstTitle(request)
      .then(result => {
        if (result) {
          setTitleForm(request);
          setTitleResult(result);
          setConfirmationData({
            humanReadableId: values.ewbUuid,
            ewbDate: moment().format(DATE_FORMAT.DATE_WITH_TIME_DOTS),
            ewbId: values.ewbUuid,
            startDate: moment(values.startDate).format(DATE_FORMAT.BASE_REVERTED_DOTS),
            finishDate: moment(values.finishDate).format(DATE_FORMAT.BASE_REVERTED_DOTS),
            transportationType: Transportation.OwnNeeds,
            communicationType: Communication.Urban,
            organizationName: values.organizationName,
            ogrn: values.organizationMsrn,
            tin: values.organizationTin,
            organization: {
              organizationName: values.organizationName,
              subjectCode: values.organizationRegionCode,
              ogrn: values.organizationMsrn,
              tin: values.organizationTin,
            },
            transport: {
              brand: values.brand,
              model: values.model,
              stateNumber: values.stateNumber,
              type: values.transportType,
            },
            driver: {
              fullName: values.driverFullName,
              personnelNumber: values.driverPersonnelNumber,
              series: values.drivingLicenseSeries,
              number: values.drivingLicenseNumber,
              issueDate: moment(values.drivingLicenseIssueDate).format(DATE_FORMAT.DATE_WITH_TIME_DOTS),
            },
          });
        }
      })
      .catch(ignore);
  };

  useEffect(() => {
    if (uuid) {
      form.setFieldsValue({
        ewbUuid: uuid,
      });
    }
  }, [uuid, form]);

  const isSubmitDisabled = useMemo(() => {
    const errors = form.getFieldsError().filter(({ errors }) => errors.length).length > 0;
    return !uuid || errors || !transportId;
  }, [form, uuid, transportId]);

  return (
    <div className={styles.container}>
      <Form
        form={form}
        initialValues={initialValues}
        layout="vertical"
        onFinish={handleSubmit}
      >
        <Panel className={styles.header}>
          <div className={styles.header__container}>
            <div className={styles.title}>
              <ArrowLeftOutlined onClick={() => history.push(RELEASE_ON_LINE_LINK)} />
              <span>{i18.title}</span>
            </div>

            <FormItem shouldUpdate noStyle>
              {() => (
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={isTitleLoading}
                  disabled={isSubmitDisabled}
                >
                  {i18.createBtnText}
                </Button>
              )}
            </FormItem>
          </div>

          <Divider className={styles.divider} />

          <RowWithContainer gutter={[58, 20]}>
            <Col span={12}>
              <FormItem
                name="ewbUuid"
                label={i18.uuid}
              >
                {!uuid ? (
                  <Button
                    type="primary"
                    loading={isUuidLoading}
                    onClick={handleGetUUID}
                  >
                    {i18.getBtnText}
                  </Button>
                )
                  : (
                    <Input disabled />
                  )}
              </FormItem>
            </Col>

            <Col span={6}>
              <FormItem label={i18.transportationType}>
                {TransportationInfo[Transportation.OwnNeeds]}
              </FormItem>
            </Col>

            <Col span={6}>
              <FormItem label={i18.communicationType}>
                {CommunicationInfo[Communication.Urban]}
              </FormItem>
            </Col>

            <Col span={12}>
              <FormItem
                name="startDate"
                label={i18.startDate}
                rules={[
                  {
                    required: true,
                    message: i18.dateRequired,
                  },
                ]}
              >
                <DatePicker
                  disabled={!uuid}
                  className={styles.datepicker}
                  format={DATE_FORMAT.BASE_REVERTED_DOTS}
                  disabledDate={date => date.isBefore(moment().startOf('day'))}
                  onChange={value => {
                    setStartDate(value);
                    if (!value) form.setFieldsValue({ finishDate: undefined });
                  }}
                  size="large"
                />
              </FormItem>
            </Col>

            <Col span={12}>
              <FormItem
                name="finishDate"
                label={i18.finishDate}
                rules={[
                  {
                    required: true,
                    message: i18.dateRequired,
                  },
                ]}
              >
                <DatePicker
                  disabled={!uuid || !startDate}
                  className={styles.datepicker}
                  format={DATE_FORMAT.BASE_REVERTED_DOTS}
                  disabledDate={date => (
                    date.isBefore(moment(startDate)) || date.isAfter(moment(startDate).add(2, 'd'))
                  )}
                  size="large"
                />
              </FormItem>
            </Col>
          </RowWithContainer>
        </Panel>

        <OwnerSection />

        <TransportSection
          disabled={!uuid}
          form={form}
          changeTransportId={setTransportId}
        />

        <DriverSection
          disabled={!uuid || !transportId}
          transportId={transportId}
          form={form}
        />
      </Form>

      <ConfirmModal
        visible={!!confirmationData && !isConfirmed}
        data={confirmationData}
        onConfirm={() => setIsConfirmed(true)}
        onCancel={() => setConfirmationData(null)}
      />

      {
        isConfirmed && (
          <SignModal
            visible={isConfirmed}
            ewbUuid={uuid}
            titleForm={titleForm}
            titleResult={titleResult}
            onClose={() => setIsConfirmed(false)}
          />
        )
      }
    </div>
  );
};

export default WaybillCreate;
