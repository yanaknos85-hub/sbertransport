import React, { useEffect, useState } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';

const ApprovalTabsLayout: React.FC = observer(({ children }) => {
  const match = useRouteMatch();

  // Определяем активную вкладку по URL
  const [activeKey, setActiveKey] = useState(() => {
    return match.path.includes('compensation') ? 'compensation' : 'cargos';
  });

  // Синхронизация с URL
  useEffect(() => {
    const key = match.path.includes('compensation') ? 'compensation' : 'cargos';
    if (key !== activeKey) {
      setActiveKey(key);
    }
  }, [match.path, activeKey]);

  return (
    <div style={{ marginTop: '16px' }}>
      {children}
    </div>
  );
});

export default ApprovalTabsLayout;
