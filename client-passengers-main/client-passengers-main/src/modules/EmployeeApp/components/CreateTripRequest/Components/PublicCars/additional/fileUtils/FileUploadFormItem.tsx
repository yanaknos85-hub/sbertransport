/* eslint-disable @typescript-eslint/no-explicit-any */
import { ILogger } from '@sber-sbertransport/mf-core';
import { Form, Upload } from 'antd';
import { observer } from 'mobx-react';
import React, { Dispatch, FC, SetStateAction } from 'react';

import { ValidationRules } from 'shared/fieldValidationRules';

import { fieldTitles } from '../../constants';
import {
  beforeUpload, customRequest, getFileAvatar, uploadButton
} from './fileUploadUtils';
import { FormInstance } from 'antd/es/form/Form';
import { FileCompensation } from 'modules/EmployeeApp/components/CreateTripRequest/types/types';

export interface ITripFormProps {
  logger: ILogger;
  file?: FileCompensation;
  loading: boolean;
  imageUrl: string | undefined;
  setImageUrl: Dispatch<SetStateAction<string | undefined>>;
  handleLoadFileChange?: (info: any) => void;
  title: string;
  setFile: Dispatch<SetStateAction<FileCompensation | undefined>>;
  form: FormInstance;
  compensationType: string;
  index: number;
}

export const FileUploadFormItem: FC<ITripFormProps> = observer(
  ({
    logger,
    file,
    handleLoadFileChange,
    title,
    setFile,
    form,
    compensationType,
    index,
  }) => (
    <Form.Item
      name={fieldTitles.ticket}
      label={title}
      rules={[ValidationRules.general.required]}
    >
      <Upload
        name="avatar"
        listType="picture-card"
        className="avatar-uploader"
        showUploadList={false}
        customRequest={customRequest}
        beforeUpload={(e): boolean => beforeUpload(e, logger)}
        onChange={handleLoadFileChange}
      >
        {file ? getFileAvatar(file, setFile, form, compensationType, index) : uploadButton()}
      </Upload>
    </Form.Item>
  )
);
