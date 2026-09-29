import React, { FC, Suspense } from 'react';
import { ignore } from "utils";
import { mkUseUploadVSPEntity } from "api/upload";
import DownloadButtonVsp from 'shared/components/DownloadButtonVsp/DownloadButtonVsp';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { ButtonLoad } from "shared/components/ButtonLoad";
import {
  UploadButton,
  importExportEndpointMap,
} from "modules/UploadButton";

import styles from './VSPSettings.module.scss';

const VSPHandbookComponent: FC<{ theme?: 'primary' | 'secondary' }> = ({ theme = 'primary' }) => {
  const useUploadVSPCargo = mkUseUploadVSPEntity('vsp', {
    onSuccess: ignore,
    suspense: false,
  });
  return (
    <Suspense fallback={<SpinWrapped/>}>
    <div className={styles.wrapper}>
      <DownloadButtonVsp
        url={`${importExportEndpointMap.vsp}/files/vsp`}
        mkUrl={() => `${importExportEndpointMap.vsp}/files/vsp`}
        mkFileName={fileName => `${fileName}.xlsx`}
        theme={theme}
      />
      <UploadButton
        entity="vsp"
        useUpload={useUploadVSPCargo}
        title="Загрузить"
        customElement={<ButtonLoad type="export" theme="secondary" >Загрузить</ButtonLoad>}
      />
    </div>
  </Suspense>)
};

export default VSPHandbookComponent;
