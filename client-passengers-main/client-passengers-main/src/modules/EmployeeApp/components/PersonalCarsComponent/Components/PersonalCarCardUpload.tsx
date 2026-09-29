import { UploadOutlined } from '@ant-design/icons';
import {
  EmployeeModel, IOsagoUploadResponseModel, OrUndefined, PersonalCar
} from '@sber-sbertransport/mf-core';
import { Button, Upload } from 'antd';
import { UploadProps } from 'antd/lib/upload/interface';

import { UploadRequestOption as RcCustomRequestOptions } from 'rc-upload/lib/interface';
import React, { FC } from 'react';

import { useUploadOsagoCarFile } from 'api/personalCars';

import { UpdateWithOsago } from 'shared/hooks/useSpecificCar';

interface TPersonalCarCardUpload {
  updateWithOsago: UpdateWithOsago<PersonalCar>;
}

const PersonalCarCardUpload: FC<TPersonalCarCardUpload> = ({
  updateWithOsago,
}: TPersonalCarCardUpload): JSX.Element => {
  const [uploadFile] = useUploadOsagoCarFile();

  const uploadImage = (options: RcCustomRequestOptions): void => {
    const { file } = options;

    const formData = new FormData();
    formData.append('image', file);

    uploadFile({
      body: formData,
    }).then((osagoModel: OrUndefined<IOsagoUploadResponseModel>): void => updateWithOsago((employee: EmployeeModel): OrUndefined<PersonalCar> => osagoModel?.collectByPersonalCar(employee))
    );
  };

  const props: UploadProps = {
    customRequest: uploadImage,
    maxCount: 1,
    accept: 'image/jpg, image/heic',
    showUploadList: !1,
    className: 'upload-list-inline',
  };

  return (
    <Upload {...props}>
      <Button icon={<UploadOutlined />}>Загрузить полис</Button>
    </Upload>
  );
};

export default PersonalCarCardUpload;
