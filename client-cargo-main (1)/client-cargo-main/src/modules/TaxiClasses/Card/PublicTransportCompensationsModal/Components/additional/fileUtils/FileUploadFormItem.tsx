import React, { Dispatch, FC, SetStateAction } from 'react';
import { ILogger } from '@sber-sbertransport/mf-core';
import { Form, Upload } from 'antd';
import { observer } from 'mobx-react';
import { ValidationRules } from 'shared/fieldValidationRules';

import { fieldTitles } from '../../constants';
import {
  beforeUpload, customRequest, getFileAvatar, uploadButton
} from './fileUploadUtils';

export interface ITripFormProps {
  logger: ILogger;
  file?: File;
  loading: boolean;
  imageUrl: string | undefined;
  setImageUrl: Dispatch<SetStateAction<string | undefined>>;
  handleLoadFileChange?: (info: any) => void;
  title: string;
}

export const FileUploadFormItem: FC<ITripFormProps> = observer(
  ({
    logger, file, loading, imageUrl, handleLoadFileChange, setImageUrl, title,
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
        beforeUpload={(e): boolean => beforeUpload(e, logger, setImageUrl)}
        onChange={handleLoadFileChange}
      >
        {file && imageUrl ? getFileAvatar(file, imageUrl) : uploadButton(loading)}
      </Upload>
    </Form.Item>
  )
);
