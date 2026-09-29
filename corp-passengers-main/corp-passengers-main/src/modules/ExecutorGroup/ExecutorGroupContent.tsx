import React, {
  useRef, useState, useLayoutEffect
} from 'react';
import ExecutorGroupsTable from './Components/Table/Table';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useGetExecutorGroupsDef } from 'api/executor-group';
import styles from './styles.module.scss';
import ExecutorSettingsProvider from './Components/Table/context/ExecutorSettingsProvider';
import { StyledFooter } from './Components/Table/styled.table';
import Empty from './Components/Empty/Empty';

const ExecutorGroupContent = () => {
  const [executorGroups, setExecutorGroups] = useState([]);
  const [getExecutorGroup, { isLoading }] = useGetExecutorGroupsDef();
  const footerRef = useRef<HTMLDivElement>(null);

  useLayoutEffect(() => {
    // @ts-ignore
    getExecutorGroup().then(data => setExecutorGroups(data?.data?.content));
  }, []);

  return (
    <div className={styles.container}>
      {isLoading ? <SpinWrapped />
        : (
          <ExecutorSettingsProvider value={{ footer: footerRef }}>
            {executorGroups.length
              ? <ExecutorGroupsTable />
              : <Empty />}
          </ExecutorSettingsProvider>
        )}
      <StyledFooter ref={footerRef} />
    </div>
  );
};

export default ExecutorGroupContent;
