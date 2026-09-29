/* eslint-disable @typescript-eslint/no-explicit-any */
import '../../../../styles/override.css';
import { ILogger, RequestCanceler } from '@sber-sbertransport/mf-core';
import {
  Form, InputNumber, Select, Upload, Spin
} from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import React, {
  Dispatch, FC, SetStateAction, useMemo, useRef, useState
} from 'react';
import ChevronSmall from 'shared/components/Images/view/menu 2.0/ChevronSmall';
import debounce from 'lodash/debounce';
import axios from 'axios';

import { PublicApprovals } from 'api/trip-requests';

import { ValidationRules } from 'shared/fieldValidationRules';

import {
  beforeUpload, customRequest, getFileAvatar, uploadButton
} from '../../additional/fileUtils/fileUploadUtils';
import {
  applicationNumber,
  costLabel,
  documentConfirmation,
  fieldTitles,
  publicTransportServiceTitle,
  publicTransportTypeTitle,
  selectRequestTrip,
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
  minSearchLength?: number;
  debounceTimeout?: number;
}

export const PaidServicesFormItem: FC<ITripFormProps> = observer(
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
    minSearchLength = 3,
    debounceTimeout = 750,
  }) => {
    const [cost, setCost] = useState(0);
    const compensationType = TransportCompensations.PAID_SERVICES_COMPENSATION;
    const [searchNumbers, setSearchNumbers] = useState([]);
    const cancelRequestRef = useRef<RequestCanceler>();
    const [isFetching, setIsFetching] = useState(false);
    const [searchValue, setSearchValue] = useState('');

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

    const ticketLabelRule = {
      ...ValidationRules.general.required,
      required: approvalDocumentCheck,
    };

    const handleSearch = useMemo(
      () => debounce((search: string) => {
        setSearchValue(search);
        const cancelRequest = cancelRequestRef.current;

        if (cancelRequest) {
          cancelRequest();
        }

        setIsFetching(true);

        (search && search.length >= minSearchLength
          ? tripStore.searchNumberTrip(search) : Promise.resolve<[]>([])
        )
          .then(list => setSearchNumbers(list))
          .catch(error => {
            if (!axios.isCancel(error)) {
              throw error;
            }
          })
          .finally(() => {
            cancelRequestRef.current = undefined;
            setIsFetching(false);
          });
      }, debounceTimeout),
      [debounceTimeout, setSearchNumbers, setIsFetching, minSearchLength]
    );

    const numbersTripToOption = (searchNumbers: any): LabeledValue => ({
      value: searchNumbers.id,
      label: searchNumbers.humanReadableId,
    });

    const options = useMemo(() => searchNumbers.map(numbersTripToOption), [searchNumbers]);

    const helpValidation = () => {
      if (searchValue && !/^OT-/.test(searchValue)) {
        return 'Неверное значение';
      }
    };

    const initialValueForHumanReadableId = (value: PublicInfoCard[]) => {
      const result = value.find(item => item.compensationType === TransportCompensations.PAID_SERVICES_COMPENSATION);
      return result && result.humanReadableId && result.humanReadableId;
    };

    const handleSelectChange = (value, option) => {
      const selectedOption = { value: option.value, label: option.label };
      const tripsInfo = form.getFieldValue('tripsInfo');

      tripsInfo.map((item, index) => {
        if (item.compensationType === TransportCompensations.PAID_SERVICES_COMPENSATION) {
          tripsInfo[index].humanReadableId = selectedOption;
        }
      });

      form.setFieldsValue({ tripsInfo: tripsInfo });
    };

    const handleSelectClear = () => {
      const tripsInfo = form.getFieldValue('tripsInfo');

      tripsInfo.map((item, index) => {
        if (item.compensationType === TransportCompensations.PAID_SERVICES_COMPENSATION) {
          if (item.humanReadableId) {
            tripsInfo[index].humanReadableId = undefined;
          }
        }
      });

      form.setFieldsValue({ tripsInfo: tripsInfo });
    };

    return (
      <div className="wrapper_compensation">
        <span className="compensation_title_item">{publicTransportServiceTitle}</span>
        <Form.Item
          {...field}
          key="paidServices_public_tt"
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
        <span className="compensation_title_item">{applicationNumber}</span>
        <Form.Item
          shouldUpdate={true}
        >
          {({ getFieldValue }) => {
            return (
              <Form.Item
                {...field}
                key={fieldTitles.humanReadableId}
                name={[field.name, fieldTitles.humanReadableId]}
                help={helpValidation()}
                initialValue={initialValueForHumanReadableId(getFieldValue('tripsInfo'))}
              >
                <Select
                  suffixIcon={(
                    <div style={{ padding: '5px 18px' }}>
                      <ChevronSmall />
                    </div>
            )}
                  allowClear
                  onClear={handleSelectClear}
                  showSearch
                  onSearch={handleSearch}
                  placeholder={selectRequestTrip}
                  className="EmployeeBaseAutoComplete"
                  onChange={handleSelectChange}
                  filterOption={false}
                  options={options}
                  notFoundContent={isFetching ? <Spin size="small" /> : null}
                />
              </Form.Item>
            );
          }}
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
