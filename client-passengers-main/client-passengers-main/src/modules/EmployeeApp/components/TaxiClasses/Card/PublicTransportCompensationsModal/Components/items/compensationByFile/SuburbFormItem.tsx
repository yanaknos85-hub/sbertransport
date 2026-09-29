/* eslint-disable @typescript-eslint/no-explicit-any */

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

import { useGetApprovalsPublic } from 'api/trip-requests';

import { ValidationRules } from 'shared/fieldValidationRules';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import {
  beforeUpload, customRequest, getFileAvatar, uploadButton
} from '../../additional/fileUtils/fileUploadUtils';
import {
  costLabel, fieldTitles, publicTransportTypeTitle, ticketLabel, tripsInfoTitle
} from '../../constants';

import styles from '../../style.module.scss';
import { FormInstance } from 'antd/es/form/Form';
import { PublicInfoCard, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';

export interface ITripFormProps {
  field: FormListFieldData;
  logger: ILogger;
  file?: File;
  loading: boolean;
  imageUrl: string | undefined;
  setImageUrl: Dispatch<SetStateAction<string | undefined>>;
  handleLoadFileChange: (info: any) => void;
  updateGeneralSum: () => void;
  currentTransportTypes: LabeledValue[];
  form: FormInstance;
}

export const SuburbFormItem: FC<ITripFormProps> = observer(
  ({
    logger,
    file,
    field,
    loading,
    imageUrl,
    handleLoadFileChange,
    setImageUrl,
    updateGeneralSum,
    currentTransportTypes,
    form,
  }) => {
    const [cost, setCost] = useState(0);

    const onBlur = () => {
      updateGeneralSum();
      form.setFieldsValue({
        tripsInfo: form
          .getFieldValue(tripsInfoTitle)
          .map((el: PublicInfoCard) => el.compensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION ? {
            ...el,
            // cost: test // Что за test ?????!!!!
          } : el
          ),
      });
    };

    const { selfStore } = useAppStoreContext();
    const { selfEmployee } = selfStore;
    /**
     * publicApprovals - "настройки согласований" можно менять из корп. клиента. Привязаны к organizationId.
     * Сейчас можно создавать заявку только сотрудникам одной организации.
     * Если появится возможность создавать завку сотрудникам другой организации, нужно будет доставать
     * organizationId организации, сотруднику которой создаётся поездка
     */
    const { data: publicApprovals } = useGetApprovalsPublic(selfEmployee.organizationId);

    const approvalDocumentCheck = publicApprovals ? publicApprovals.approvalDocumentCheck : true;

    const ticketLabelRule = { ...ValidationRules.general.required, required: approvalDocumentCheck };

    return (
      <>
        <Form.Item
          {...field}
          key="suburb_public_tt"
          name={[field.name, fieldTitles.publicTransportType]}
          // @ts-ignore
          fieldKey={[field.fieldKey, fieldTitles.publicTransportType]}
          label={publicTransportTypeTitle}
          rules={[ValidationRules.general.required]}
        >
          <Select placeholder={publicTransportTypeTitle} options={currentTransportTypes} />
        </Form.Item>

        <Form.Item
          {...field}
          key="suburb_cost"
          name={[field.name, fieldTitles.cost]}
          // @ts-ignore
          fieldKey={[field.fieldKey, fieldTitles.cost]}
          label={costLabel}
          rules={[ValidationRules.general.required]}
        >
          <InputNumber
            step={0.01}
            className={styles.fullScreen}
            value={cost}
            defaultValue={cost}
            onBlur={onBlur}
            formatter={value => `${value}`.replace('.', ',')}
            parser={value => Number(value?.replace(',', '.'))}
            onChange={e => setCost(Number(e))}
          />
        </Form.Item>

        <Form.Item
          key={fieldTitles.ticket}
          name={[field.name, fieldTitles.ticket]}
          // @ts-ignore
          fieldKey={[field.fieldKey, fieldTitles.ticket]}
          label={ticketLabel}
          rules={[ticketLabelRule]}
        >
          <Upload
            name="avatar"
            listType="picture-card"
            className="avatar-uploader"
            showUploadList={false}
            customRequest={customRequest}
            beforeUpload={(e): boolean => beforeUpload(e, logger, setImageUrl)}
            onChange={handleLoadFileChange}
          >
            {file && imageUrl ? getFileAvatar(file, imageUrl) : uploadButton(loading)}
          </Upload>
        </Form.Item>
      </>
    );
  }
);
