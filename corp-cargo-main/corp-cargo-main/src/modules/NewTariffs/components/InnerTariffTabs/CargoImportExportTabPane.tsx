import React from 'react';
import { Row, Tabs } from 'antd';
import DownloadButton from 'components/DownloadButton';
import { useUploadTariffs } from 'api/tariffs-cargo';
import { tabPaneStyles } from '../../styles/tariffTabStyles';
import { TariffStrings } from '../../constants/Tariffs.constants';
import { importExportEndpointMap, UploadButton } from '../../../UploadButton';

/**
 * Таб для импорта/экспорта 1го тарифа сложной формы (
 * @param entity вид транспорта, будет использоваться в построение url
 * @param downloadUrl ссылка на скачивание шаблона или файла данных по текущему тарифу
 * @param useUploadEntity keyof typeof importExportEndpointMap
 * @param initialId id тарифа
 * @constructor
 */
export const CargoImportExportTabPane: React.FC<{
  entity: string;
  useUploadEntity: keyof typeof importExportEndpointMap;
  downloadUrl: string;
  isEditable: boolean;
}> = ({ entity, downloadUrl, useUploadEntity, isEditable }): JSX.Element => {
  const { TabPane } = Tabs;
  return (
    <Tabs style={tabPaneStyles.formWrapper} defaultActiveKey="1">
      <TabPane tab={TariffStrings.parametersTab} key="1">
        <Row>
          <UploadButton entity={entity} useUpload={useUploadTariffs(useUploadEntity)} />
          <DownloadButton 
            url={downloadUrl} 
            disabled={!isEditable}
            skipFormat={true}
          />
        </Row>
      </TabPane>
    </Tabs>
  );
};
