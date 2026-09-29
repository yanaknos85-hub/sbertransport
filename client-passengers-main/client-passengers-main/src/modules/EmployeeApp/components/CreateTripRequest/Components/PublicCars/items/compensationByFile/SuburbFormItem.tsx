/* eslint-disable @typescript-eslint/no-explicit-any */
import '../../../../styles/override.css';
import { ILogger } from '@sber-sbertransport/mf-core';
import {
  Form, InputNumber, Select, Upload
} from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import React, {
  Dispatch, FC, SetStateAction, useState
} from 'react';
import ChevronSmall from 'shared/components/Images/view/menu 2.0/ChevronSmall';

import { PublicApprovals } from 'api/trip-requests';

import { ValidationRules } from 'shared/fieldValidationRules';

import {
  beforeUpload, customRequest, getFileAvatar, uploadButton
} from '../../additional/fileUtils/fileUploadUtils';
import {
  costLabel,
  documentConfirmation,
  fieldTitles,
  publicTransportTypeTitle,
  ticketLabel,
  tripsInfoTitle,
  falsificationAlert
} from '../../constants';

import { FormInstance } from 'antd/es/form/Form';
import { PublicInfoCard, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { FileCompensation } from 'modules/EmployeeApp/components/CreateTripRequest/types/types';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { SpinWrapped } from 'shared/components';
import Alert from 'modules/EmployeeApp/components/Alert/Alert';
import styles from './styles.module.scss';

export interface ITripFormProps {
  field: FormListFieldData;
  logger: ILogger;
  file?: FileCompensation;
  loading: boolean;
  imageUrl: string | ArrayBuffer | null | undefined;
  setImageUrl: Dispatch<SetStateAction<string | ArrayBuffer | null | undefined>>;
  handleLoadFileChange: (info: any) => void;
  updateGeneralSum: () => void;
  currentTransportTypes: LabeledValue[];
  form: FormInstance;
  publicApprovals: PublicApprovals | null;
  setIsSaveCompensation: Dispatch<SetStateAction<boolean>>;
  index: number;
  setFile: Dispatch<SetStateAction<FileCompensation | undefined>>;
}

export const SuburbFormItem: FC<ITripFormProps> = observer(
  ({
    logger,
    file,
    field,
    handleLoadFileChange,
    updateGeneralSum,
    currentTransportTypes,
    form,
    publicApprovals,
    setIsSaveCompensation,
    index,
    setFile,
  }) => {
    const [cost, setCost] = useState(0);
    const compensationType = TransportCompensations.SUBURB_TRIP_COMPENSATION;
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

    const approvalDocumentCheck = publicApprovals ? publicApprovals.approvalDocumentCheck : true;

    const ticketLabelRule = { ...ValidationRules.general.required, required: approvalDocumentCheck };

    return (
      <div className="wrapper_compensation">
        <span className="compensation_title_item">{publicTransportTypeTitle}</span>
        <Form.Item
          {...field}
          key="suburb_public_tt"
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
            onChange={() => setIsSaveCompensation(false)}
          />
        </Form.Item>
        <span className="compensation_title_item">{costLabel}</span>
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
            precision={2}
            formatter={value => `${value}`.replace('.', ',')}
            parser={value => Number(value?.replace(',', '.'))}
            onChange={e => setCost(Number(e))}
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
