import React, { useState } from 'react';
import { useHistory } from 'react-router-dom';
import { PageHeader } from 'antd';
import { useTranslation } from 'i18n';
import { Methods } from './Component/Methods';
import { Services } from './Component/Services';
import * as routes from 'constants/constants.routes';

import styles from './styles.module.scss';

const Permissions: React.FC<JSX.Element> = () => {
  const [selectedService, setSelectedService] = useState<string>();
  const history = useHistory();
  const { t } = useTranslation();

  return (
    <>
      <PageHeader
        className={styles.pageHeader}
        title={t.global.goBack}
        onBack={() => history.replace(routes.ROLES)}
      />
      <div className={styles.container}>
        <Services onServiceSelected={setSelectedService} />
        <Methods service={selectedService} />
      </div>
    </>
  );
};

export default Permissions;
