import React from 'react';
import GeoZonesHandbookComponent from 'modules/Geo/Zones/Zones';
import { ToolbarProvider } from 'components/Toolbar';
import { EmptyView } from 'shared/components/EmptyView/EmptyView';

const GeoSettings: React.FC = () => (
  <ToolbarProvider>
    <EmptyView title="Скачать геозоны в формате XLS">
      <GeoZonesHandbookComponent theme="secondary" />
    </EmptyView>
  </ToolbarProvider>
);

export default GeoSettings;
