import React from 'react';
import { ToolbarProvider } from 'components/Toolbar';
import { EmptyView } from 'shared/components/EmptyView/EmptyView';
import VSPHandbookComponent from "./VSPHandbookComponent";

const VSPSettings: React.FC = () => (
  <ToolbarProvider>
    <EmptyView title="Скачать объекты организации (справочник ВСП)">
      <VSPHandbookComponent theme="secondary" />
    </EmptyView>
  </ToolbarProvider>
);

export default VSPSettings;
