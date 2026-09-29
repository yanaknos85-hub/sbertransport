import * as React from 'react';
import { ToolbarElement } from 'components/Toolbar';
import DownloadButton from 'components/DownloadButton';

const currentYear = new Date().getUTCFullYear();
const years = [currentYear, currentYear + 1, currentYear + 2, currentYear + 3];

const ExportEmployee: React.FC = () => (
  <ToolbarElement>
    <DownloadButton
      caption="Экспорт"
      url="/limits/limits/export"
      formats={years.map(_ => ({ ext: _.toString(), icon: null }))}
      mkUrl={(url, year) => `${url}/${year}`}
      fileName="limits"
      mkFileName={fileName => `${fileName}.xlsx`}
    />
  </ToolbarElement>
);

export { ExportEmployee };
