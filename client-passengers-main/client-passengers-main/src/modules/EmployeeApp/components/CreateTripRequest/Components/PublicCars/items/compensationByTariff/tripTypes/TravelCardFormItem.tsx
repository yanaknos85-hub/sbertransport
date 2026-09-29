/* eslint-disable @typescript-eslint/no-explicit-any */
import '../../../../../styles/override.css';
import {
  DatePicker, Form, FormInstance, InputNumber, Select, Upload
} from 'antd';
import locale from 'antd/es/date-picker/locale/ru_RU';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue, SelectValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import moment from 'moment';
import React, {
  Dispatch, FC, SetStateAction, useState
} from 'react';
import ChevronSmall from 'shared/components/Images/view/menu 2.0/ChevronSmall';

import { DATE_FORMAT } from 'constants/constants.app';

import { ValidationRules } from 'shared/fieldValidationRules';

import {
  costTravelCard,
  documentConfirmation,
  fieldTitles,
  periodTravel,
  publicTransportTypeTitle,
  ticketLabel,
  tripsInfoTitle,
  falsificationAlert
} from '../../../constants';
import { PublicInfoCard, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { PublicApprovals } from 'api/trip-requests';
import {
  beforeUpload,
  customRequest,
  getFileAvatar,
  uploadButton
} from '../../../additional/fileUtils/fileUploadUtils';
import { ILogger } from '@sber-sbertransport/mf-core';
import { FileCompensation } from 'modules/EmployeeApp/components/CreateTripRequest/types/types';
import { SpinWrapped } from 'shared/components';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import Alert from 'modules/EmployeeApp/components/Alert/Alert';
import styles from './styles.module.scss';

const { RangePicker } = DatePicker;
export interface Props {
  form: FormInstance;
  field: FormListFieldData;
  handleTransportTypeChange: (selectedTransportType: SelectValue, index: number) => void;
  currentTransportTypes: LabeledValue[];
  spareDate: any;
  updateSum: (index: number, quantity: string | number | null, price: number, typeChange: string) => void;
  tripCost: number;
  updateGeneralSum: () => void;
  setIsSaveCompensation: Dispatch<SetStateAction<boolean>>;
  index: number;
  publicApprovals: PublicApprovals | null;
  handleLoadFileChange: (info: any) => void;
  file?: FileCompensation;
  logger: ILogger;
  setFile: Dispatch<SetStateAction<FileCompensation | undefined>>;
  loading: boolean;
}

export const TravelCardFormItem: FC<Props> = observer(
  ({
    form,
    field,
    currentTransportTypes,
    spareDate,
    updateGeneralSum,
    setIsSaveCompensation,
    index,
    publicApprovals,
    handleLoadFileChange,
    file,
    logger,
    setFile,
  }) => {
    const [cost, setCost] = useState(0);
    const approvalDocumentCheck = publicApprovals ? publicApprovals.approvalDocumentCheck : true;
    const ticketLabelRule = { ...ValidationRules.general.required, required: approvalDocumentCheck };
    const compensationType = TransportCompensations.TRAVEL_CARD_COMPENSATION;
    const {
      [StoreNames.tripStore]: tripStore,
    } = useAppStoreContext();

    const onBlur = () => {
      updateGeneralSum();
      form.setFieldsValue({
        tripsInfo: form
          .getFieldValue(tripsInfoTitle)
          .map((el: PublicInfoCard, i: number) => index === i && el.compensationType === compensationType ? { ...el, cost } : el
          ),
      });
    };

    const getDisabledDate = (value: moment.Moment): boolean => {
      const now = moment().add(-6, 'days');
      return value < now || value > now.add(1, 'years');
    };

    return (
      <div className="wrapper_compensation">
        <span className="compensation_title_item">{publicTransportTypeTitle}</span>
        <Form.Item
          {...field}
          key={fieldTitles.publicTransportType}
          name={[field.name, fieldTitles.publicTransportType]}
          rules={[ValidationRules.general.required]}
        >
          <Select
            suffixIcon={(
              <div style={{ padding: '5px 18px' }}>
                <ChevronSmall />
              </div>
            )}
            placeholder={publicTransportTypeTitle}
            options={currentTransportTypes}
            onChange={(): void => setIsSaveCompensation(false)}
          />
        </Form.Item>
        <span className="compensation_title_item">{periodTravel}</span>
        <Form.Item
          {...field}
          key={fieldTitles.calendar}
          name={[field.name, fieldTitles.calendar]}
          rules={[ValidationRules.general.required]}
          initialValue={[moment(new Date(), DATE_FORMAT.MONTH_AND_YEAR), moment(spareDate, DATE_FORMAT.MONTH_AND_YEAR)]}
        >
          <RangePicker
            disabledDate={getDisabledDate}
            defaultValue={[moment(new Date(), DATE_FORMAT.BASE), moment(spareDate, DATE_FORMAT.BASE)]}
            picker="week"
            placeholder={['Начало', 'Конец']}
            format={DATE_FORMAT.BASE}
            onChange={() => setIsSaveCompensation(false)}
            locale={locale}
          />
        </Form.Item>
        <span className="compensation_title_item">{costTravelCard}</span>
        <Form.Item
          {...field}
          key={fieldTitles.cost}
          name={[field.name, fieldTitles.cost]}
          rules={[
            ValidationRules.general.required,
            ValidationRules.general.maxMoneyValue(50000),
            ValidationRules.general.minMoneyValue,
          ]}
        >
          <InputNumber
            step={0.01}
            className="numberCost"
            value={cost}
            defaultValue={cost}
            onBlur={onBlur}
            formatter={value => `${value}`.replace('.', ',')}
            parser={value => Number(value?.replace(',', '.'))}
            onChange={e => setCost(Number(e))}
            maxLength={5}
          />
        </Form.Item>
        <span className="compensation_title_item">{ticketLabel}</span>
        <Form.Item
          key={fieldTitles.ticket}
          name={[field.name, fieldTitles.ticket]}
          rules={[
            ticketLabelRule,
            ValidationRules.general.checkImageFormatForPublicDocuments(),
            ValidationRules.general.checkImageSizeForPublicDocuments(),
          ]}
        >
          <Upload
            name="avatar"
            listType="picture-card"
            className={file ? 'avatar-uploader-on' : 'avatar-uploader'}
            showUploadList={false}
            customRequest={customRequest}
            beforeUpload={(e): boolean => beforeUpload(e, logger)}
            onChange={e => handleLoadFileChange(e.file)}
          >
            {tripStore.isSaveFile ? <SpinWrapped /> : file ? getFileAvatar(file, setFile, form, compensationType, index) : uploadButton()}
          </Upload>
        </Form.Item>
        <Alert
          className={styles.alert}
          showIcon
          type="warning"
          description={falsificationAlert}
        />
        <span className="documentConfirmation">{documentConfirmation}</span>
      </div>
    );
  }
);
