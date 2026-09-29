import React from 'react';
import { ToolbarProvider } from 'components/Toolbar';
import { EmptyView } from 'shared/components/EmptyView/EmptyView';
import { WorkingGroupsHandbookComponent } from 'modules/WorkingGroups';

const WorkingGroupsSettings: React.FC = () => (
  <ToolbarProvider>
    <EmptyView title="Скачать рабочие группы в формате XLS">
      <WorkingGroupsHandbookComponent theme="secondary" />
    </EmptyView>
  </ToolbarProvider>
);

export default WorkingGroupsSettings;
