import React, {
  useRef, useState, useLayoutEffect
} from 'react';
import ExecutorGroupsTable from './Components/Table/Table';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useGetExecutorGroupsDef /* , useGetPlannerAll */ } from 'api/executor-group';
import styles from './styles.module.scss';
import PlannerSettingsProvider from './Components/Table/context/PlannerSettingsProvider';
import { StyledFooter } from './Components/Table/styled.table';
import Empty from './Components/Empty/Empty';

const PlannerSRMContent = () => {
  const [executorGroups, setExecutorGroups] = useState([]);
  const [getExecutorGroup, { isLoading }] = useGetExecutorGroupsDef();
  // const data = useGetPlannerAll();
  const footerRef = useRef<HTMLDivElement>(null);

  useLayoutEffect(() => {
    // console.log(data);
    // @ts-ignore
    getExecutorGroup().then(data => setExecutorGroups(data?.data?.content));
    // getExecutorGroup().then(data => setExecutorGroups(data?.content));
  }, []);

  return (
    <div className={styles.container}>
      {isLoading ? <SpinWrapped />
        : (
          <PlannerSettingsProvider value={{ footer: footerRef }}>
            {executorGroups.length
              ? <ExecutorGroupsTable />
              : <Empty />}
          </PlannerSettingsProvider>

        )}
      <StyledFooter ref={footerRef} />
    </div>
  );
};

export default PlannerSRMContent;
