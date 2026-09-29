import React, {
  useRef
} from 'react';
import ExecutorGroupsTable from './Components/Table/Table';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useGetExecutorGroups } from 'api/executor-group';
import styles from './styles.module.scss';
import ExecutorSettingsProvider from './Components/Table/context/ExecutorSettingsProvider';
import { StyledFooter } from './Components/Table/styled.table';
import Empty from './Components/Empty/Empty';

const ExecutorGroupContent = () => {
  const footerRef = useRef<HTMLDivElement>(null);
  const executorGroupsData = useGetExecutorGroups();
  const { data, isLoading } = executorGroupsData;

  if (isLoading) {
    return <SpinWrapped />;
  }
  const typedData = data as unknown as {
    data?: {
      content?: unknown[];
    };
  } | undefined;

  const hasData = typedData?.data?.content && typedData.data.content.length > 0;

  return (
    <div className={styles.container}>
      <ExecutorSettingsProvider value={{ footer: footerRef }}>
        {hasData ? <ExecutorGroupsTable /> : <Empty />}
      </ExecutorSettingsProvider>
      <StyledFooter ref={footerRef} />
    </div>
  );
};

export default ExecutorGroupContent;
